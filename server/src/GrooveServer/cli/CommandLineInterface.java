package GrooveServer.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import GrooveServer.database.DatabaseOperations;
import GrooveServer.objects.User;

public class CommandLineInterface {

    private static final String HELP_MESSAGE = "\nAvailable commands:\n" +
                                                "add_music          - Adds music to database filesystem\n" +
                                                "clear-all-tables   - Clears all data in all tables in the database\n" +
                                                "clear-songs-tables - Clears all data in song related tables\n" +
                                                "clear-users-tables - Clears all data in user related tables\n" +
                                                "read_script        - Executes script from file\n" +
                                                "new_user           - Adds new user to database (TO REMOVE)\n" +
                                                "user_buys_song     - User buys song (TO REMOVE)\n" +
                                                "user_gets_song     - User gets song (TO REMOVE)\n" +
                                                "help               - Prints this message\n" +
                                                "exit               - Closes this command prompt and gracefully terminates the server\n";

    DatabaseOperations _databaseOperations;

    private String _scripts_path;

    private String _audio_path;

    private String _lyrics_path;

    public CommandLineInterface(DatabaseOperations databaseOperations, String scripts_path, String music_path, String lyrics_path) {
        _databaseOperations = databaseOperations;
        _scripts_path = scripts_path;
        _audio_path = music_path;
        _lyrics_path = lyrics_path;
    }
    
    public void ParseInput(){
        System.out.println("Starting command line interface...");
        System.out.println("Type 'help' for list of commands.");
        System.out.println("TODO: Remove temporary commands from the list.");
        System.out.println("TODO: Private key for SFTP and Digital Signature should be the same.");

        Scanner scanner = new Scanner(System.in);
        boolean condition = true;

        while (condition) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] arguments = line.split(" ");
            String command = arguments[0];

            switch (command) {
                case "add_music":

                    // if(line.split(" ").length != 7){
                    //     System.out.println("Wrong number of arguments. Usage: add_music <title> <artist> <genre> <path-to-lyics> <audio-format-name> <path-to-music-file>");
                    //     break;
                    // }
                    // try {
                    //     _databaseOperations.addMusic(arguments[1], arguments[2], arguments[3], arguments[4], arguments[5], arguments[6]);
                    // } catch (Exception e) {
                    //     //e.printStackTrace();
                    //     System.out.println("Error: " + e.getMessage());
                    // }
                    addMusicFunction(scanner);
                    break;

                case "clear-all-tables":
                    try {
                        System.out.println("DANGER: You are about to erase ALL DATA from ALL TABLES.\nAre you sure you want to proceed? (y/n)");
                        String answer = scanner.nextLine().trim();
                        if(answer.equals("y")){
                            _databaseOperations.clearAllTables();
                        }else{
                            System.out.println("Operation canceled.");
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "clear-songs-tables":
                    try {
                        System.out.println("DANGER: You are about to erase ALL DATA from the songs related tables.\nAre you sure you want to proceed? (y/n)");
                        String answer = scanner.nextLine().trim();
                        if(answer.equals("y")){
                            _databaseOperations.clearSongsTables();
                        }else{
                            System.out.println("Operation canceled.");
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "clear-users-tables":
                    try {
                        System.out.println("DANGER: You are about to erase ALL DATA from the users related tables.\nAre you sure you want to proceed? (y/n)");
                        String answer = scanner.nextLine().trim();
                        if(answer.equals("y")){
                            _databaseOperations.clearUsersTables();
                        }else{
                            System.out.println("Operation canceled.");
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "read_script":
                    try {
                        System.out.println("Enter filename of script (please put your scripts at '" + _scripts_path + "'):");
                        String path = scanner.nextLine().trim();
                        executeScript(path);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                
                case "help":
                    System.out.println(HELP_MESSAGE);
                    break;
                
                case "exit":
                    condition = false;
                    break;
                
                case "":
                    break;
                    
                default:
                    System.out.println("Unknown command. Type 'help' for list of commands.");
                    break;

                //TODO: Everything below this line is temporary and will be removed in the future
                //This is just for testing purposes 
                case "new_user":
                    addNewUserFunction(scanner);
                    break;

                case "user_buys_song":
                    userBuysSongFunction(scanner);
                    break;
                
                case "user_gets_song":
                    userGetsSongFunction(scanner);
                    break;

            }
        }

        scanner.close();
    }

    private void addMusicFunction(Scanner scanner){
        System.out.println("Please provide the following parameters to add the music:");
        System.out.println();

        System.out.println("Enter title:");
        String title = scanner.nextLine().trim();
        System.out.println();

        //enter artist
        System.out.println("Enter artist:");
        String artist = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter genre (if more than 1 genre, separate them with a space):");
        String genre = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter path to lyrics:");
        String pathToLyrics = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter audio format name:");
        String audioFormatName = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter path to music file:");
        String pathToMusicFile = scanner.nextLine().trim();
        System.out.println();

        try {
            _databaseOperations.addMusic(title, artist, genre.split(" "), pathToLyrics, audioFormatName, pathToMusicFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addNewUserFunction(Scanner scanner){
        System.out.println("Please provide the following parameters to add the user:");
        System.out.println();

        System.out.println("Enter username:");
        String username = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter password:");
        String password = scanner.nextLine().trim();
        System.out.println();

        try {
            _databaseOperations.addNewUser(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    //user buys song
    private void userBuysSongFunction(Scanner scanner){
        System.out.println("Please provide the following parameters to add the user:");
        System.out.println();

        System.out.println("Enter username:");
        String username = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter song title:");
        String songTitle = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter artist:");
        String artist = scanner.nextLine().trim();
        System.out.println();

        try {
            _databaseOperations.executeUserPurchase(username, songTitle, artist);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //user gets song
    private void userGetsSongFunction(Scanner scanner){
        System.out.println("Please provide the following parameters to add the user:");
        System.out.println();

        System.out.println("Enter song title:");
        String songTitle = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter artist:");
        String artist = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter format:");
        String format = scanner.nextLine().trim();
        System.out.println();

        System.out.println("Enter username:");
        String username = scanner.nextLine().trim();
        System.out.println();

        try {
            _databaseOperations.getSongForUser(songTitle, artist, format, username);

            System.out.println("Got song successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

    }

    private void executeScript(String filename) throws IOException{
        try {
            String[] lines = Files.lines(Path.of(_scripts_path + filename))
                                    .map(String::trim)
                                    .toArray(String[]::new);

            for (String line : lines) {
                if(line.charAt(0) != '#'){
                    String[] lineSplit = line.split("\\|");
                    String command = lineSplit[0];
                    //print all in linesplit
                    switch (command) {
                        case "add_music":
                            System.out.println("Adding music...");
                            _databaseOperations.addMusic(lineSplit[1], 
                                                         lineSplit[2], 
                                                         lineSplit[3].split(" "), 
                                                         _lyrics_path + lineSplit[4], 
                                                         lineSplit[5], 
                                                         _audio_path + lineSplit[6]);
                            Thread.sleep(20);
                            break;

                        case "clear-all-tables":
                            _databaseOperations.clearAllTables();
                            break;

                        case "clear-songs-tables":
                            _databaseOperations.clearSongsTables();
                            break;

                        case "clear-users-tables":
                            _databaseOperations.clearUsersTables();
                            break;

                        case "new_user":
                            _databaseOperations.addNewUser(lineSplit[1], lineSplit[2]);
                            break;

                        case "user_buys_song":
                            _databaseOperations.executeUserPurchase(lineSplit[1], lineSplit[2], lineSplit[3]);
                            break;

                        default:
                            break;
                    }

                }
            }
        } catch (IOException e) {
            throw e; // Handle the exception according to your needs
        } catch (Exception e) {
            //TODO: erase
        }
    }

}
