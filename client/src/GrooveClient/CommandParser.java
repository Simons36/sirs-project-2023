package GrooveClient;

import java.util.Scanner;

import GrooveClient.communication.ClientService;
import GrooveClient.exceptions.BadInputException;
import GrooveClient.exceptions.ServerIsDeadException;
import GrooveClient.exceptions.SongNotInLibraryException;
import GrooveClient.exceptions.SongUnavailableException;
import GrooveClient.exceptions.UnableToPlayAudioException;
import GrooveClient.songManagement.SongPlayer;

/**
 * Class to parse inputs from the client
 */
public class CommandParser {

	public static final String ERROR_MESSAGE = "Error! Bad Input given! Please type \"help+Enter\" to list the available commands.";

	// client command line commands to interact with GrooveGalaxy app
	private static final String SPACE = " ";
	private static final String SEARCH = "search";
	private static final String SONG = "song";
	private static final String ARTIST = "artist";
	private static final String GENRE = "genre";
	private static final String PREVIEW_SONG = "preview";
	private static final String DOWNLOAD_SONG = "download";
	private static final String PURCHASE_SONG = "purchase";
	private static final String MY_PURCHASES = "my_purchases";
	private static final String MY_PREFERENCES = "my_preferences";
	private static final String ADD_PREFERENCE = "add_preference";
	private static final String CREATE_FAMILY = "create_family";
	private static final String JOIN_FAMILY = "join_family";
	private static final String LEAVE_FAMILY = "leave_family";
	private static final String HELP = "help";
	private static final String EXIT = "exit";
	private static final String HEAR_SONG = "hear";

	private ClientService service;

	public CommandParser(ClientService service) {
		this.service = service;
	}

	public void startParsing() throws Exception {

		try (Scanner scanner = new Scanner(System.in)) {
			boolean exit = false;

			while (!exit) {
				System.out.print(service.getClientID() + "> ");
				String line = scanner.nextLine().trim();
				String cmd = line.split(SPACE)[0];

				try {
					switch (cmd) {
						case SEARCH:
							this.search(line, scanner);
							break;
						case PREVIEW_SONG:
							this.previewSong(line);
							break;
						case PURCHASE_SONG:
							this.purchaseSong(line, scanner);
							break;
						case MY_PURCHASES:
							this.viewMyPurchases();
							break;
						case MY_PREFERENCES:
							this.viewMyPreferences();
							break;
						case ADD_PREFERENCE:
							this.addPreference(line);
							break;
						case CREATE_FAMILY:
							this.createFamily(line);
							break;
						case JOIN_FAMILY:
							this.joinFamily(line);
							break;
						case LEAVE_FAMILY:
							this.leaveFamily(line);
							break;
						case HELP:
							this.printUsage();
							break;
						case EXIT:
							exit = true;
							break;
						default:
							this.printUsage();
							break;
					}
				} catch (BadInputException e) {
					System.err.println(e.getMessage());
				} catch (ServerIsDeadException e) {
					System.err.println(e.getMessage());
					exit = true;
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}

			}
		}
	}

	/**
	 * Requests the server to search for:
	 * - a song, if a song name is given. If the song exists then a list of formats
	 * the song is available in is returned.
	 * The user can then decide to preview the song or to play it
	 * - the songs of an artist if an artist name is given;
	 * - the songs corresponding to the genre(s) given, if a genre name is given.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void search(String line, Scanner scanner) throws BadInputException {

		String[] input = line.split(SPACE);

		if (input.length < 2 || input.length > 3) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		try {
			switch (input[1]) {
				case SONG:
					searchSong(scanner);
					break;
				case ARTIST:
					System.out.println(service.searchArtist(input[2]));
					break;
				case GENRE:
					System.out.println(service.searchGenre(input[2]));
					break;
				default:
					throw new BadInputException(ERROR_MESSAGE);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Requests the server for a song. Better explanantion above.
	 * 
	 * @param songName
	 * @throws BadInputException
	 */
	private void searchSong(Scanner scanner) throws BadInputException, ServerIsDeadException {
		String formats = "";

		System.out.println("Please provide the song name:");
		String songName = scanner.nextLine();

		try {
			formats = service.searchSong(songName, scanner);
		} catch (SongUnavailableException e) {
			System.err.println(e.getMessage());
			return;
		} catch (ServerIsDeadException e) {
			throw e;
		}

		System.out.println("Formats this song is available in: " + formats.toString());
		System.out.println(
				"If you whish to hear this song type 'hear <artist> <file format>', "+ 
				"to preview it type 'preview <artist> <file format>', " +
				"to download it type 'download <artist> <file format>'.");
		try {
			String userInput = scanner.nextLine();
			switch (userInput.split(SPACE)[0]) {
				case HEAR_SONG:
					if (!formats.contains(userInput.split(SPACE)[2])) {
						throw new BadInputException("Not an available file format!");
					}
					System.out.println("From which second to start? (Must be an integer)");
					int seconds = Integer.parseInt(scanner.nextLine());
					try {
						SongPlayer.playStoredSong(songName, userInput.split(SPACE)[1], seconds);
					} catch (UnableToPlayAudioException | SongNotInLibraryException e) {
						service.streamSong(songName, userInput.split(SPACE)[1], userInput.split(SPACE)[2], seconds);
					}
					break;
				case PREVIEW_SONG:
					if (!formats.contains(userInput.split(SPACE)[2])) {
						throw new BadInputException("Not an available file format!");
					}
					service.previewSong(songName, userInput.split(SPACE)[1], userInput.split(SPACE)[2]);
					break;
				case DOWNLOAD_SONG:
					if (!formats.contains(userInput.split(SPACE)[2])) {
						throw new BadInputException("Not an available file format!");
					}
					service.downloadSong(songName, userInput.split(SPACE)[1], userInput.split(SPACE)[2]);
					break;
				default:
					throw new BadInputException("Bad input!!!");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Requests the server to preview a song in a specific file format.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void previewSong(String line) throws BadInputException, Exception {

		if (line.split(SPACE).length != 3) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		String songToPreview = line.split(SPACE)[1];
		String artist = line.split(SPACE)[2];
		String fileFormat = line.split(SPACE)[3];

		try {
			service.previewSong(songToPreview, artist, fileFormat);
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Purchases a song (all file formats become available).
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void purchaseSong(String line, Scanner scanner) throws BadInputException, Exception {

		System.out.println("Please provide the song name:");
		String songToPurchase = scanner.nextLine();

		System.out.println("Please provide the artist name:");
		String artist = scanner.nextLine();

		try {
			service.purchaseSong(songToPurchase, artist);
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Requests the server for all the songs purchased by this client.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void viewMyPurchases() throws BadInputException, Exception {

		try {
			service.viewMyPurchases();
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Requests the server for all the songs in the clients preference list.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void viewMyPreferences() throws BadInputException, Exception {

		try {
			service.viewMyPreferences();
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Adds a song to the clients preference list
	 * 
	 * @param line
	 * @throws Exception
	 */
	private void addPreference(String line) throws Exception {

		if (line.split(SPACE).length != 2) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		String preferencedToAdd = line.split(SPACE)[1];

		try {
			service.addPreference(preferencedToAdd);
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Creates a new family with the given family name.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void createFamily(String line) throws BadInputException, Exception {

		if (line.split(SPACE).length != 2) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		String familyName = line.split(SPACE)[1];

		try {
			service.createFamily(familyName);
		} catch (Exception e) {
			 throw e;
		}
	}

	/**
	 * Joins an existing family with the given family name.
	 * 
	 * @param line
	 * @throws BadInputException
	 */
	private void joinFamily(String line) throws BadInputException, Exception {

		if (line.split(SPACE).length != 3) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		String familyCode = line.split(SPACE)[1];
		String familyName = line.split(SPACE)[2];

		try {
			service.joinFamily(familyCode, familyName);
		} catch (Exception e) {
			throw e;
		}
	}

	private void leaveFamily(String line) throws BadInputException, Exception {

		if (line.split(SPACE).length != 1) {
			throw new BadInputException(ERROR_MESSAGE);
		}

		try {
			service.leaveFamily();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * When the client types in help or inputs a wrong command.
	 * It is shown the list of available commands and their usage.
	 */
	private void printUsage() {
		System.out.println("Client side usage:" +
				"\n\tTo search for a song by name -> search song" +
				"\n\tTo search for songs by artist -> search artist <artist_name>" +
				"\n\tTo search for songs by genre -> search genre <genre_name>" +
				"\n\tTo preview a song -> preview <song_name> <file_format>" +
				"\n\tTo purchase a song -> purchase" +
				"\n\tTo view your owned songs -> my_purchases" +
				"\n\tTo view your preferences -> my_preferences" +
				"\n\tTo add a song to your preferences list -> add_preference <song_name>" +
				"\n\tTo create a new family -> create_family <family name>" +
				"\n\tTo join an existing family -> join_family <family code> <family name>" +
				"\n\tTo leave a family -> leave_family <family code>" +
				"\n\tTo exit -> exit");
	}

}
