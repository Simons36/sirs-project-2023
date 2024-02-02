package GrooveClient.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import com.google.rpc.Status;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import GrooveClient.exceptions.AuthenticityCheckFailed;
import GrooveClient.exceptions.ErrorDownloadingSong;
import GrooveClient.exceptions.ErrorReceivingMessage;
import GrooveClient.exceptions.ServerIsDeadException;
import GrooveClient.exceptions.SongUnavailableException;
import GrooveClient.exceptions.UnableToConnectException;
import GrooveClient.exceptions.UnableToLoginException;
import GrooveClient.exceptions.UnableToPlayAudioException;
import GrooveClient.exceptions.UnableToRegisterException;

import GrooveClient.keyManagement.KeyStoreMan;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.KeyStore;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;

import GrooveClient.songManagement.SongManager;
import GrooveClient.songManagement.SongPlayer;
import contracts.client.ClientServiceGrpc;
import contracts.client.ClientAppServer;

/**
 * Class to perform requests to the GrooveGalaxy service.
 */
public class ClientService {

	public static final String SECRET_KEY_PATH = "keys/secret.key";
	public static final String SESSION_KEY_PATH = "keys/sessionKey.key";
	public static final String SERVER_PUBLIC_KEY_PATH = "keys/server.pubkey";

	private String clientID;

	// Grpc communication
    private ClientServiceGrpc.ClientServiceBlockingStub appServerStub;
    private ManagedChannel appServerChannel;

	// UDP communication
	private UDPconnection songStreamer;

	// Client cryptographic API
	CryptographicLibraryClientAPI cryptoAPI;

	// KeyStore Manager
	KeyStoreMan keyStoreMan;

	/**
	 * Gets the IP of the application server and establishes a grpc connection
	 * with it.
	 * Also creates a cryptographic library using the secret key.
	 * 
	 * @param ip to connect to
	 * @param port to connect to
	 * @throws UnableToConnectException
	 */
	public ClientService(String ip, int port, String clientid) throws UnableToConnectException {
		try {
			this.clientID = clientid;
			this.keyStoreMan = new KeyStoreMan();
			this.cryptoAPI = new CryptographicLibraryClientAPI(
										this.keyStoreMan.getSymmetricKey(this.keyStoreMan.SECRET_KEY_ALIAS), 
										this.keyStoreMan.SERVER_PUBKEY_PATH, 3600*24*365);
			//this.songStreamer = new UDPconnection(ip, port);

			String keystorePath = "keys/user.p12";
            String trustStorePath = "keys/usertruststore.jks";
            String keystorePassword = "changeme";

            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keyStore.load(fis, keystorePassword.toCharArray());
            }
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(trustStorePath)) {
                trustStore.load(fis, keystorePassword.toCharArray());
            }

			// Create KeyManagerFactory for the server's keystore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            
            // Create the trust Manager Factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
			
            // Create SSL/TLS context for the client
            SslContext sslContext = GrpcSslContexts.configure(SslContextBuilder.forClient())
					.keyManager(keyManagerFactory)
					.trustManager(trustManagerFactory)
					.build();
			
            // Create gRPC channel with SSL/TLS using SslCredentials
            appServerChannel = NettyChannelBuilder.forAddress(ip, port)
					.sslContext(sslContext)
					.overrideAuthority("groove-app-server")
					.maxInboundMessageSize(100000000)
					.build();
			
			/*ManagedChannel appServerChannel = ManagedChannelBuilder.forAddress(ip, port)
																   .usePlaintext()
																   .maxInboundMessageSize(100000000).build();*/
			appServerStub = ClientServiceGrpc.newBlockingStub(appServerChannel);
			System.err.println("Connection to server ip: " + ip + " and port: " + port + " initiated");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw new UnableToConnectException(ip, port, "Grpc");
		}
	}

	/**
	 * @return the id the client chose as username
	 */
	public String getClientID() {
		return this.clientID;
	}

	public KeyStoreMan getKeyStoreMan() {
		return this.keyStoreMan;
	}

	/**
	 * Send a request for a song through the established grpc connection.
	 * 
	 * @param songName
	 * @return a list of file formats the song is available in or a error message.
	 * @throws SongUnavailableException
	 * @throws ServerIsDeadException
	 */
	public String searchSong(String songName, Scanner scanner) throws SongUnavailableException, ServerIsDeadException {
		try {
			ClientAppServer.SearchSongRequest request = ClientAppServer.SearchSongRequest
				.newBuilder().setSong(songName).build();
			ClientAppServer.SearchSongReply reply = appServerStub.searchSong(request);
			System.out.println("Song search result: ");
			for (int i = 0; i < reply.getSongAndFormatCount(); i++) {
				System.out.println("\t" + reply.getSongAndFormat(i).getSong());
			}
			System.out.print("Please choose the song you want to listen to: ");
			String song = scanner.nextLine();
			for (int i = 0; i < reply.getSongAndFormatCount(); i++) {
				if (song.equals(reply.getSongAndFormat(i).getSong().toString())) {
					return reply.getSongAndFormat(i).getFileFormatsList().toString();
				}
			}
			throw new SongUnavailableException(song);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			throw new SongUnavailableException(songName);
		}
	}

	/**
	 * Send a request for the songs by an artist through the
	 * established grpc connection.
	 * 
	 * @param artistName
	 */
	public String searchArtist(String artistName) throws ServerIsDeadException {
		try {
			ClientAppServer.SearchArtistRequest request = ClientAppServer.SearchArtistRequest
				.newBuilder().setArtist(artistName).build();
			ClientAppServer.SearchArtistReply reply = appServerStub.searchArtist(request);
			return reply.toString();
		} catch (StatusRuntimeException e) {
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			return e.getMessage();
		}
	}

	/**
	 * Send a request for the songs of a genre through the
	 * established TCP connection.
	 * 
	 * @param genreName
	 */
	public String searchGenre(String genreName) throws ServerIsDeadException {
		try {
			ClientAppServer.SearchGenreRequest request = ClientAppServer.SearchGenreRequest
				.newBuilder().setGenre(genreName).build();
			ClientAppServer.SearchGenreReply reply = appServerStub.searchGenre(request);
			return reply.toString();
		} catch (StatusRuntimeException e) {
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			return e.getMessage();
		}
	}

	/**
	 * Requests the server to send the entire jsonDocument with the song 
	 * audio and metadata.
	 * 
	 * @param songName
	 * @param fileFormat
	 */
	public void downloadSong(String songName, String artist, String fileFormat) throws ServerIsDeadException {
		try {
			ClientAppServer.DownloadSongRequest request = ClientAppServer.DownloadSongRequest
				.newBuilder().setClientId(this.clientID).setSong(songName).setArtist(artist).setFileFormat(fileFormat).build();
			ClientAppServer.DownloadSongReply reply = appServerStub.downloadSong(request);
			SongManager.storeSong(reply.getSongDocument().getEncryptedContent().toByteArray(),
								  reply.getSongDocument().getIv().toByteArray(),
								  reply.getSongDocument().getDigitalSignature().toByteArray(),
								  reply.getSongDocument().getTempEncryptedKey().toByteArray(),
								  cryptoAPI);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (ErrorDownloadingSong e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Asks the server to stream the song via grpc. 
	 * The reception is done via UDP as only the audio is sent and it is sent
	 * portion by portion.
	 * 
	 * @param songName
	 * @param fileFormat
	 * @param secondsOffset
	 */
	public void streamSong(String songName, String artist, String fileFormat, int secondsOffset) throws ServerIsDeadException {
		try {
			ClientAppServer.StreamSongRequest request = ClientAppServer.StreamSongRequest
				.newBuilder().setClientId(this.clientID).setSong(songName).setArtist(artist)
				.setFileFormat(fileFormat).setSecondsOffset(secondsOffset).build();
			ClientAppServer.StreamSongReply reply = appServerStub.streamSong(request);
			int audioReceived = 0;
			for (int i = 0; i < reply.getAudioSize(); i += audioReceived) {
				byte[] audio = songStreamer.receiveUDPMessage();
				SongPlayer.playEncryptedAudio(audio, reply.getIv().toByteArray(), 
											  fileFormat, cryptoAPI);
				audioReceived = audio.length;
			}
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (ErrorReceivingMessage e) {
			System.err.println(e.getMessage());
		} catch (UnableToPlayAudioException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Requests the preview of a song in a specific file format through
	 * the established grpc connection.
	 * 
	 * @param songName
	 * @param fileFormat
	 * @throws SongUnavailableException
	 */
	public void previewSong(String songName, String artist, String fileFormat) throws SongUnavailableException, ServerIsDeadException {
		try {
			ClientAppServer.PreviewSongRequest request = ClientAppServer.PreviewSongRequest
				.newBuilder().setSong(songName).setArtist(artist).setFileFormat(fileFormat).build();
			ClientAppServer.PreviewSongReply reply = appServerStub.previewSong(request);
			
			ClientAppServer.ProtectReturnStruct prs = reply.getEncryptedAudio();
			SongPlayer.checkAudio(prs.getEncryptedContent().toByteArray(), prs.getIv().toByteArray(), 
								   prs.getDigitalSignature().toByteArray(), cryptoAPI);
			SongPlayer.playEncryptedAudio(prs.getEncryptedContent().toByteArray(), 
								 prs.getIv().toByteArray(), fileFormat, cryptoAPI);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			throw new SongUnavailableException(songName, fileFormat);
		} catch (UnableToPlayAudioException e) {
			System.err.println(e.getMessage());
		} catch (AuthenticityCheckFailed e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Requests the purchase of a song through the established grpc connection.
	 * The song becomes available in all file formats.
	 * 
	 * @param songName
	 * @param fileFormat
	 * @throws SongUnavailableException
	 */
	public void purchaseSong(String songName, String artist) throws SongUnavailableException, ServerIsDeadException {
		try {
			ClientAppServer.PurchaseSongRequest request = ClientAppServer.PurchaseSongRequest
				.newBuilder().setClientId(this.clientID).setSong(songName).setArtist(artist).build();
			appServerStub.purchaseSong(request);
			System.out.println("Succesfully purchased " + songName);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			throw new SongUnavailableException(songName);
		}
	}

	/**
	 * Requests the server to show all the purchased songs by the client
	 * through the established TCP connection.
	 */
	public void viewMyPurchases() throws ServerIsDeadException {
		try {
			ClientAppServer.ViewMyPurchasesRequest request = ClientAppServer.ViewMyPurchasesRequest
				.newBuilder().setClientId(this.clientID).build();
			ClientAppServer.ViewMyPurchasesReply reply = appServerStub.viewMyPurchases(request);
			System.out.println("My Purchases:\n" + reply.toString());
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		}
	}

	/**
	 * Requests the server to show all the list of prefered songs of the client
	 * through the established TCP connection.
	 */
	public void viewMyPreferences() throws ServerIsDeadException {
		try {
			ClientAppServer.ViewMyPreferencesRequest request = ClientAppServer.ViewMyPreferencesRequest
				.newBuilder().setClientId(this.clientID).build();
			ClientAppServer.ViewMyPreferencesReply reply = appServerStub.viewMyPreferences(request);
			System.out.println("My Preferences:\n" + reply.toString());
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		}
	}

	/**
	 * Requests a server to add a song to the clients preference list
	 * 
	 * @param songName
	 */
	public void addPreference(String songName) throws ServerIsDeadException {
		try {
			ClientAppServer.AddPreferenceRequest request = ClientAppServer.AddPreferenceRequest
				.newBuilder().setClientId(this.clientID).setSong(songName).build();
			appServerStub.addPreference(request);
			System.out.println("Succesfully added " + songName + " to the preferences list.");
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		}
	}

	/**
	 * Registers a new client in the GrooveGalaxy application.
	 * If the registration is succesful it logins the client automatically.
	 * 
	 * @throws UnableToRegisterException
	 */
	public void register(String username, String pass) throws UnableToRegisterException, ServerIsDeadException {
		try {
            ClientAppServer.RegisterRequest request = ClientAppServer.RegisterRequest
				.newBuilder().setClientId(username).setPassword(pass).build();
			appServerStub.register(request);
			System.out.println("registered: " + username + " ; " + pass);
			this.login(username, pass);
		} catch (StatusRuntimeException e) {
			System.out.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (UnableToLoginException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Logs a client in the GrooveGalaxy application.
	 * Creates a new CryptographicLibrary API for the client using the session key
	 * returned by the server as the new symmetric (secret) key and the former's public key.  
	 * 
	 * @param id
	 * @param pass
	 * @throws UnableToLoginException
	 */
	public void login(String id, String pass) throws UnableToLoginException, ServerIsDeadException {
		try {
			ClientAppServer.LoginRequest request = ClientAppServer.LoginRequest
				.newBuilder().setClientId(id).setPassword(pass).build();
			appServerStub.login(request);
			this.clientID = id;
			System.out.println("Login operation was a success!");
		} catch (StatusRuntimeException e) {
			System.out.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
			throw new UnableToLoginException(id, pass);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new UnableToLoginException(id, pass);
		}
	}

	/**
	 * Asks the application server to create a family, printing the family code 
	 * associated with the newly created family.
	 */
	public void createFamily(String familyName) throws ServerIsDeadException {
		try {
			ClientAppServer.FamilyCreationRequest request = ClientAppServer.FamilyCreationRequest
				.newBuilder().setClientId(this.clientID).setFamilyName(familyName).build();
			ClientAppServer.FamilyCreationReply reply = appServerStub.createFamily(request);
			System.out.println("This is your family code, please remember it: " + reply.getFamilyCode());
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void joinFamily(String familyCode, String familyName) throws ServerIsDeadException {
		try {
			ClientAppServer.JoinFamilyRequest request = ClientAppServer.JoinFamilyRequest
				.newBuilder().setClientId(this.clientID).setFamilyCode(familyCode).setFamilyName(familyName).build();
			ClientAppServer.JoinFamilyReply reply = appServerStub.joinFamily(request);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void leaveFamily() throws ServerIsDeadException {
		try {
			ClientAppServer.LeaveFamilyRequest request = ClientAppServer.LeaveFamilyRequest
				.newBuilder().setClientId(this.clientID).build();
			appServerStub.leaveFamily(request);
		} catch (StatusRuntimeException e) {
			System.err.println(e.getMessage());
			if (e.getStatus().equals(io.grpc.Status.INTERNAL)) {
				throw new ServerIsDeadException();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Shuts down the communication channel to the application server.
	 */
	public void shutdownChannel() {
		if (appServerChannel != null) {
			appServerChannel.shutdownNow();
		}
	}

}
