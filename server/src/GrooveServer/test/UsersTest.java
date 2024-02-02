package GrooveServer.test;

import GrooveServer.database.DatabaseOperations;
import GrooveServer.objects.DatabaseServiceConfigStruct;
import GrooveServer.objects.Media;
import GrooveServer.objects.User;
import GrooveServer.service.DatabaseService;
import GrooveServer.util.UtilClasses;

public class UsersTest {
    
    // public static void main(String[] args) throws Exception{
    //     final String IP = args[0];
    //     final String DATABASE_NAME = args[1];
    //     final String USERNAME = args[2];
    //     final String PASSWORD = args[3];
    //     final String DATABASE_SERVER_USERNAME = args[4];
    //     final String PRIVATE_KEY_PATH = args[5];
    //     final String AUDIO_FILES_DIRECTORY_IN_DATABASE = args[6];


    //     //create database service   
    //     DatabaseService databaseService =   new DatabaseService(new DatabaseServiceConfigStruct(IP,
    //                                                                                             DATABASE_NAME,
    //                                                                                             USERNAME, 
    //                                                                                             PASSWORD, 
    //                                                                                             DATABASE_SERVER_USERNAME,
    //                                                                                             PRIVATE_KEY_PATH,
    //                                                                                             AUDIO_FILES_DIRECTORY_IN_DATABASE
    //                                                                                             ));

    //     //create database operations
    //     DatabaseOperations databaseOperations = new DatabaseOperations(databaseService);

    //     databaseOperations.initializeDatabase();

    //     final String username = "usernameTest";
    //     final String hashedPassword = "passwordTest";
    //     final String songTitle = "Butterfly Effect";
    //     final String artist = "Travis Scott";

    //     //test add user
    //     //testAddUser(databaseOperations, "usernameTest", "passwordTest");

    //     //test get user
    //     User user = testGetUser(databaseOperations, username);

    //     //test user buys song
    //     //testUserBuysSong(databaseOperations, username, songTitle, artist);

    //     //test get song for user
    //     Media media = testGetSongForUser(databaseOperations, songTitle, artist, "mp3", user);
        
    //     //print title and artist of song
    //     System.out.println("Title: " + media.getTitle());
    //     System.out.println("Artist: " + media.getArtist());
    //     //System.out.println("Audio: " + media.getAudioBase64());
        
    // }

    // public static void testAddUser(DatabaseOperations databaseOperations, String username, String hashedPassword) throws Exception{
    //     databaseOperations.addNewUser(username, hashedPassword);
    // }

    // public static User testGetUser(DatabaseOperations databaseOperations, String username) throws Exception{
    //     User user = databaseOperations.getUser(username);

    //     System.out.println("User: " + user.getUsername());
    //     System.out.println("Password: " + user.getPassword());

    //     return user;
    // }

    // public static void testUserBuysSong(DatabaseOperations databaseOperations, String username, String songTitle, String artist) throws Exception{
    //     databaseOperations.executeUserPurchase(username, songTitle, artist);
    // }

    // public static Media testGetSongForUser(DatabaseOperations databaseOperations, String title, String artist, String format, User user) throws Exception{
    //     return databaseOperations.getSongForUser(title, artist, format, user);
    // }

}
