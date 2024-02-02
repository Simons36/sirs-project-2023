package GrooveServer;

import GrooveServer.service.DatabaseService;
import GrooveServer.service.exceptions.AudioTransferConnectionException;
import GrooveServer.service.exceptions.AudioTransferErrorException;
import GrooveServer.service.exceptions.DatabaseConnectionException;
import GrooveServer.service.exceptions.DatabaseExecStatementException;


import GrooveServer.cli.CommandLineInterface;

import GrooveServer.core.CoreServer;

import GrooveServer.database.DatabaseOperations;

import GrooveServer.objects.DatabaseServiceConfigStruct;

public class ServerMain {

    public static final int APP_SERVER_PORT = 5001;

    public static void main(String[] args) throws Exception{

        if(args.length != 13){
            System.out.println("You need to provide 13 arguments:\n<ip>\n" +
                                                                "<database name>\n" +
                                                                "<username>\n" +
                                                                "<password>\n" +
                                                                "<database server username>\n" +
                                                                "<private key path>\n" +
                                                                "<audio-files-directory-in-database>\n" +
                                                                "<seconds-to-reset-keys>\n" +
                                                                "<keystore-path>\n" +
                                                                "<private-key-for-ds-path>\n" +
                                                                "<scripts-path>\n" +
                                                                "<music-audio-path>\n" +
                                                                "<music-lyrics-path>\n"
                                                                );
            System.exit(1);
        }

        final String IP = args[0];
        final String DATABASE_NAME = args[1];
        final String USERNAME = args[2];
        final String PASSWORD = args[3];
        final String DATABASE_SERVER_USERNAME = args[4];
        final String PRIVATE_KEY_PATH = args[5];
        final String AUDIO_FILES_DIRECTORY_IN_DATABASE = args[6];
        final int SECONDS_TO_RESET_KEYS = Integer.parseInt(args[7]);
        final String KEYSTORE_PATH = args[8];
        final String PRIVATE_KEY_FOR_DS_PATH = args[9];
        final String SCRIPTS_PATH = args[10];
        final String MUSIC_PATH = args[11];
        final String LYRICS_PATH = args[12];
        System.out.println("Launching GrooveGalaxy application server...");

        
        
        try {
            
            DatabaseServiceConfigStruct databaseServiceConfigStruct = new DatabaseServiceConfigStruct(IP,
                                                                                                    DATABASE_NAME,
                                                                                                    USERNAME, 
                                                                                                    PASSWORD, 
                                                                                                    DATABASE_SERVER_USERNAME,
                                                                                                    PRIVATE_KEY_PATH,
                                                                                                    AUDIO_FILES_DIRECTORY_IN_DATABASE
                                                                                                    );

            //create class for communicating with database
            DatabaseService databaseService = new DatabaseService(databaseServiceConfigStruct);


            //create class for performing operations on database using databaseService
            DatabaseOperations databaseOperations = new DatabaseOperations(databaseService);

            //will check if proper tables exist, if not, it will create them
            databaseOperations.initializeDatabase();

            CoreServer.init(SECONDS_TO_RESET_KEYS, databaseOperations, KEYSTORE_PATH, PRIVATE_KEY_FOR_DS_PATH);

            //create service for clien

            Runnable appServer = new AppServerInit(APP_SERVER_PORT);
            new Thread(appServer).start();
    
            CommandLineInterface cli = new CommandLineInterface(databaseOperations, SCRIPTS_PATH, MUSIC_PATH, LYRICS_PATH);

            //starting command line interface
            cli.ParseInput();
            
            System.out.println("Shutting down server...");
            databaseService.Shutdown();
            System.exit(0);


        }catch (DatabaseConnectionException e) {

            System.out.println(e);
            System.exit(1);

        }catch (DatabaseExecStatementException e){
            System.out.println(e);
            System.exit(1);
        }catch (AudioTransferConnectionException e){
            System.out.println(e);
            System.exit(1);
        }
        catch (AudioTransferErrorException e){
            System.out.println(e);
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage() +  " Exiting...");
            System.exit(1);
        }
    }
}
