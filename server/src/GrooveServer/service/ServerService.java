package GrooveServer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.protobuf.ByteString;

import GrooveServer.exceptions.*;
import GrooveServer.handlers.RequestHandler;
import GrooveServer.service.exceptions.DatabaseExecStatementException;
import contracts.client.ClientAppServer;
import contracts.client.ClientAppServer.*;
import contracts.client.ClientServiceGrpc.ClientServiceImplBase;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import CryptographicLibrary.structs.ProtectReturnStruct;

public class ServerService extends ClientServiceImplBase {

    private RequestHandler _requestHandler;

    public ServerService(){
        _requestHandler = new RequestHandler();
    }  

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterReply> responseObserver) {
    // Extract client ID and password from the request
        String clientId = request.getClientId();
        String password = request.getPassword(); // Assuming password is sent as bytes

        RegisterReply.Builder responseBuilder = RegisterReply.newBuilder();

        try {
            _requestHandler.register(clientId, password);
        } catch (Exception e) {
            responseObserver.onError(exceptionHandler(e));
            return;
        } finally {
            // Build and send the response back to the client
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void login(LoginRequest request, StreamObserver<LoginReply> responseObserver) {
        // Extract client ID and password from the request
        String clientId = request.getClientId();
        String password = request.getPassword();

        LoginReply.Builder responseBuilder = LoginReply.newBuilder();

        try {
            // Perform login logic and generate session key, encrypted content, IV, and digital signature
            _requestHandler.login(clientId, password);

            password = null; // Clear the password from memory
        } catch (Exception e) {
            responseObserver.onError(exceptionHandler(e));
            return;
        } finally {
            // Build and send the response back to the client
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void searchSong(SearchSongRequest request, StreamObserver<SearchSongReply> responseObserver) {
        try {
            // Call the method that returns a map of grouped Media objects
            Map<String, List<String>> resultMap = _requestHandler.searchMedia(request.getSong());

            // Build the response with the grouped file formats
            SearchSongReply.Builder replyBuilder = SearchSongReply.newBuilder();
            for (Map.Entry<String, List<String>> entry : resultMap.entrySet()) {
                String songName = entry.getKey();
                List<String> fileFormats = entry.getValue();

                SongAndFormat.Builder songAndFormatBuilder = SongAndFormat.newBuilder()
                        .setSong(songName)
                        .addAllFileFormats(fileFormats);

                replyBuilder.addSongAndFormat(songAndFormatBuilder.build());
            }

            // Send the response back to the client
            responseObserver.onNext(replyBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }


    @Override
    public void searchArtist(SearchArtistRequest request, StreamObserver<SearchArtistReply> responseObserver) {
        try {
            // Call the method that returns a list of media titles for the given artist
            List<String> songs = _requestHandler.searchArtist(request.getArtist());

            // Build the response with the list of songs
            SearchArtistReply reply = SearchArtistReply.newBuilder()
                    .addAllSongs(songs)
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }


    @Override
    public void searchGenre(SearchGenreRequest request, StreamObserver<SearchGenreReply> responseObserver) {
        try {
            // Call the method that returns a list of media titles for the given genre
            List<String> songs = _requestHandler.searchGenre(request.getGenre());

            // Build the response with the list of songs
            SearchGenreReply reply = SearchGenreReply.newBuilder()
                    .addAllSongs(songs)
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void purchaseSong(PurchaseSongRequest request, StreamObserver<PurchaseSongReply> responseObserver) {
        try {
            // Extract details from the request
            String songName = request.getSong();
            String artist = request.getArtist();
            String clientId = request.getClientId();

            // Call the method that purchases the song for the given client
            _requestHandler.purchaseSong(clientId, songName, artist);

            // Build the response
            PurchaseSongReply reply = PurchaseSongReply.newBuilder()
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void viewMyPurchases(ViewMyPurchasesRequest request, StreamObserver<ViewMyPurchasesReply> responseObserver) {
        try {
            // Call the method that returns a list of media titles for the given genre
            List<String> songs = _requestHandler.getUserPurchases(request.getClientId());

            // Build the response with the list of songs
            ViewMyPurchasesReply reply = ViewMyPurchasesReply.newBuilder()
                    .addAllMyPurchases(songs)
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void addPreference(AddPreferenceRequest request, StreamObserver<AddPreferenceReply> responseObserver) {
        try {
            // Extract details from the request
            String songName = request.getSong();
            String artist = request.getArtist();
            String clientId = request.getClientId();

            // Call the method that adds the song to the client's preference list
            _requestHandler.addPreference(clientId, songName, artist);

            // Build the response
            AddPreferenceReply reply = AddPreferenceReply.newBuilder()
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }

    }

    @Override
    public void viewMyPreferences(ViewMyPreferencesRequest request, StreamObserver<ViewMyPreferencesReply> responseObserver) {
        try {
            // Call the method that returns a list of media titles for the given genre
            List<String> songs = _requestHandler.getUserPreferences(request.getClientId());

            // Build the response with the list of songs
            contracts.client.ClientAppServer.ViewMyPreferencesReply reply = contracts.client.ClientAppServer.ViewMyPreferencesReply.newBuilder()
                    .addAllMyPreferences(songs)
                    .build();

            // Send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }

    }

    @Override
    public void downloadSong(DownloadSongRequest request, StreamObserver<DownloadSongReply> responseObserver) {
        try {
            // Extract details from the request
            String songName = request.getSong();
            String artist = request.getArtist();
            String clientId = request.getClientId();
            String fileFormat = request.getFileFormat();
    
            // Call the method that downloads the song for the given client
            ProtectReturnStruct protectReturnStruct = _requestHandler.downloadSong(clientId, songName, artist, fileFormat);

            ClientAppServer.ProtectReturnStruct.Builder protectReturnStructBuilder = ClientAppServer.ProtectReturnStruct.newBuilder()
                    .setEncryptedContent(ByteString.copyFrom(protectReturnStruct.getEncryptedContent()))
                    .setIv(ByteString.copyFrom(protectReturnStruct.getIv()))
                    .setDigitalSignature(ByteString.copyFrom(protectReturnStruct.getDigitalSignature()))
                    .setTempEncryptedKey(ByteString.copyFrom(protectReturnStruct.getTempKeyEncrypted()));


            // Build the response
            DownloadSongReply reply = DownloadSongReply.newBuilder()
                    .setSongDocument(protectReturnStructBuilder)
                    .build();
    
            // Build and send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }

    // @Override
    // public void streamSong(String clientId, String songName, String artist, String fileFormat, StreamObserver<StreamSongReply> responseObserver) {
    //     try {
    //         ProtectReturnStruct protectReturnStruct = _coreServer.GetSongForUser(clientId, songName, artist, fileFormat);
    
    //         // Assuming you have a method to get the audio in chunks
    //         List<byte[]> audioChunks = getAudioChunks(protectReturnStruct.getEncryptedContent());
    
    //         // Build the initial response with IV and audio size
    //         StreamSongReply.Builder replyBuilder = StreamSongReply.newBuilder()
    //                 .setIv(ByteString.copyFrom(protectReturnStruct.getIv()))
    //                 .setAudioSize(protectReturnStruct.getEncryptedContent().length);
    
    //         // Send the initial response back to the client
    //         responseObserver.onNext(replyBuilder.build());
    
    //         // Send audio chunks in subsequent messages
    //         for (byte[] audioChunk : audioChunks) {
    //             StreamSongReply audioChunkReply = StreamSongReply.newBuilder()
    //                     .setAudio(ByteString.copyFrom(audioChunk))
    //                     .build();
    
    //             responseObserver.onNext(audioChunkReply);
    //         }
    
    //         // Notify the client that the stream is complete
    //         responseObserver.onCompleted();
    //     } catch (DatabaseExecStatementException | OtherErrorException e) {
    //         // Handle exceptions as needed
    //         responseObserver.onError(Status.INTERNAL.withDescription("Internal Server Error").asRuntimeException());
    //     }
    // }
    
    // // Method to simulate getting audio in chunks
    // private List<byte[]> getAudioChunks(byte[] encryptedContent) {
    //     // Split the encrypted content into chunks (adjust the chunk size as needed)
    //     int chunkSize = 1024; // You may need to adjust this based on your requirements
    //     List<byte[]> chunks = new ArrayList<>();
    
    //     for (int i = 0; i < encryptedContent.length; i += chunkSize) {
    //         int end = Math.min(i + chunkSize, encryptedContent.length);
    //         chunks.add(Arrays.copyOfRange(encryptedContent, i, end));
    //     }
    
    //     return chunks;
    // }
    


    @Override
    public void previewSong(PreviewSongRequest request, StreamObserver<PreviewSongReply> responseObserver) {
        try {
            // Extract details from the request
            String songName = request.getSong();
            String artist = request.getArtist();
            String fileFormat = request.getFileFormat();
    
            // Call the method that downloads the song for the given client
            ProtectReturnStruct protectReturnStruct = _requestHandler.previewSong(songName, artist, fileFormat);

            ClientAppServer.ProtectReturnStruct.Builder protectReturnStructBuilder = ClientAppServer.ProtectReturnStruct.newBuilder()
                    .setEncryptedContent(ByteString.copyFrom(protectReturnStruct.getEncryptedContent()))
                    .setIv(ByteString.copyFrom(protectReturnStruct.getIv()))
                    .setDigitalSignature(ByteString.copyFrom(protectReturnStruct.getDigitalSignature()))
                    .setTempEncryptedKey(ByteString.copyFrom(protectReturnStruct.getTempKeyEncrypted()));
            
            // Build the response
            PreviewSongReply reply = PreviewSongReply.newBuilder()
                    .setEncryptedAudio(protectReturnStructBuilder)
                    .build();

            // Build and send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void createFamily(FamilyCreationRequest request, StreamObserver<FamilyCreationReply> responseObserver) {
        try {
            // Extract details from the request
            String clientId = request.getClientId();
            String familyName = request.getFamilyName();
    
            // Call the method that creates the family
            String familyCode = _requestHandler.createFamily(clientId, familyName);

            // Build the response
            FamilyCreationReply reply = FamilyCreationReply.newBuilder()
                    .setFamilyCode(familyCode)
                    .build();
    
            // Build and send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            System.out.println(e.getMessage());
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void joinFamily(JoinFamilyRequest request, StreamObserver<JoinFamilyReply> responseObserver) {
        try {
            // Extract details from the request
            String clientId = request.getClientId();
            String familyName = request.getFamilyName();
            String familyCode = request.getFamilyCode();
    
            // Call the method that joins the family
            _requestHandler.joinFamily(clientId, familyName, familyCode);

            // Build the response
            JoinFamilyReply reply = JoinFamilyReply.newBuilder()
                    .build();
    
            // Build and send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            System.out.println(e.getMessage());
            responseObserver.onError(exceptionHandler(e));
        }
    }

    @Override
    public void leaveFamily(LeaveFamilyRequest request, StreamObserver<LeaveFamilyReply> responseObserver) {
        try {
            // Extract details from the request
            String clientId = request.getClientId();
    
            // Call the method that leaves the family
            _requestHandler.leaveFamily(clientId);

            // Build the response
            LeaveFamilyReply reply = LeaveFamilyReply.newBuilder()
                    .build();
    
            // Build and send the response back to the client
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an appropriate gRPC status
            System.out.println(e.getMessage());
            responseObserver.onError(exceptionHandler(e));
        }
    }

    public StatusRuntimeException exceptionHandler(Exception e) {
        if (e.getClass().equals(UserNotLoggedInException.class)) {
            return Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(MusicNotFoundException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UserAlreadyOwnsMusicException.class)) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(AccountNotFoundException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(WrongPasswordException.class)) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UserAlreadyLoggedInException.class)) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(SecretKeyForUserNotFoundException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UsernameAlreadyExistsException.class)) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(FamilyDoesNotExistException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(FamilyNameAlreadyExistsException.class)) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UserAlreadyBelongsToAFamilyException.class)) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(WrongFamilyPasswordException.class)) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UserDoesNotBelongToAFamilyException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(UserDoesntOwnMusicException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(MusicFormatNotFoundException.class)) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        } else if (e.getClass().equals(DatabaseExecStatementException.class)) {
            return Status.INTERNAL.withDescription("Internal Server Error").asRuntimeException();
        } else if (e.getClass().equals(OtherErrorException.class)) {
            return Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException();
        } else {
            return Status.INTERNAL.withDescription("Internal Server Error").asRuntimeException();
        }
    }
}
