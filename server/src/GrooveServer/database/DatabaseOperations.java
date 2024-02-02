package GrooveServer.database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import GrooveServer.objects.Media;

import GrooveServer.service.DatabaseService;

import GrooveServer.service.exceptions.AudioTransferErrorException;
import GrooveServer.service.exceptions.DatabaseConnectionException;
import GrooveServer.service.exceptions.DatabaseExecStatementException;

import GrooveServer.exceptions.AccountNotFoundException;
import GrooveServer.exceptions.AudioFormatForMusicAlreadyExistsException;
import GrooveServer.exceptions.FamilyDoesNotExistException;
import GrooveServer.exceptions.FamilyNameAlreadyExistsException;
import GrooveServer.exceptions.MusicFormatNotFoundException;
import GrooveServer.exceptions.MusicNotFoundException;
import GrooveServer.exceptions.OtherErrorException;
import GrooveServer.exceptions.UserAlreadyBelongsToAFamilyException;
import GrooveServer.exceptions.UserAlreadyOwnsMusicException;
import GrooveServer.exceptions.UserDoesntOwnMusicException;
import GrooveServer.exceptions.UsernameAlreadyExistsException;


import GrooveServer.util.UtilClasses;

public class DatabaseOperations {

    private DatabaseService _databaseService;

    public DatabaseOperations(DatabaseService databaseService) {
        _databaseService = databaseService;
    }

    public void initializeDatabase() throws DatabaseExecStatementException{
        System.out.println("Checking tables integrity");

        try {
            
//---------------------------- SONGS ------------------------------------------------            
            //create song table (id, title, artist, genre (array of strings), lyrics) 
            //genre will be in a seperate table

            //SONGS TABLE
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS songs (\n" +
                                       "    song_id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                                       "    title VARCHAR(255),\n" +
                                       "    artists VARCHAR(255),\n" +
                                       "    lyrics TEXT\n" +
                                            ");");
                                            
                                            //GENRES TABLE
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS genres (\n" +
                                       "    genre_name VARCHAR(255) UNIQUE PRIMARY KEY\n" +
                                       ");");
                                       
            //SONG_GENRES TABLE
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS song_genres (\n" +
            "    song_id INT,\n" +
            "    genre_name VARCHAR(255),\n" +
            "    PRIMARY KEY (song_id, genre_name),\n" +
            "    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE,\n" +
            "    FOREIGN KEY (genre_name) REFERENCES genres(genre_name) ON DELETE CASCADE\n" +
            ");");
                                       
            System.out.println("Songs table: [OK]");
//------------------------------------------------------------------------------------------

//------------------------------- USERS ---------------------------------------------------

            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS users (\n" + //
                                       "    username VARCHAR(255) UNIQUE PRIMARY KEY,\n" + //
                                       "    hash_password VARCHAR(255),\n" + //
                                       "    permanent_key_base64 VARCHAR(255),\n" + //
                                       "    temporary_key_base64 VARCHAR(255)\n" + //
                                       ");");
            
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS user_songs (\n" + //
                                       "    username VARCHAR(255),\n" + //
                                       "    song_id INT,\n" + //
                                       "    PRIMARY KEY (username, song_id),\n" + //
                                       "    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,\n" + //
                                       "    FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE\n" + //
                                       ");");

            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS user_preferences (\n" + //
                                        "    username VARCHAR(255),\n" + //
                                        "    preferenced_song_id INT,\n" + //
                                        "    PRIMARY KEY (username, preferenced_song_id),\n" + //
                                        "    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,\n" + //
                                        "    FOREIGN KEY (preferenced_song_id) REFERENCES songs(song_id) ON DELETE CASCADE\n" + //
                                        ");");


            //families tables
            // PRIMARY KEY UNIQUE family_name
            // family_reg_code INT UNIQUE
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS families (\n" + //
                                        "    family_name VARCHAR(255) UNIQUE PRIMARY KEY,\n" + //
                                        "    hash_family_password VARCHAR(255),\n" + //
                                        "    temporary_key_family_base64 VARCHAR(255)\n" + //
                                        ");");

            //users_family
            // PRIMARY KEY (username, family_name)
            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS users_families (\n" + //
                                        "    username VARCHAR(255) UNIQUE,\n" + //
                                        "    family_name VARCHAR(255),\n" + //
                                        "    will_be_removed BOOLEAN,\n"  + //
                                        "    PRIMARY KEY (username, family_name),\n" + //
                                        "    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,\n" + //
                                        "    FOREIGN KEY (family_name) REFERENCES families(family_name) ON DELETE CASCADE\n" + //
                                        ");");

            System.out.println("Users table: [OK]");
//------------------------------------------------------------------------------------------

//--------------------------- SONG AUDIO ---------------------------------------------------

            _databaseService.execStatement("CREATE TABLE IF NOT EXISTS song_audio (\n" +
                                        "   song_id INT,\n" + 
                                        "   format VARCHAR(20),\n" +
                                        "   audio_location VARCHAR(255),\n" + 
                                        "   PRIMARY KEY (song_id, format),\n" +
                                        "   FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE\n" +
                                        ");");

            System.out.println("Audio table: [OK]");
//-------------------------------------------------------------------------------------------

            System.out.println("Tables integrity ok!");
            System.out.println();

        } catch (DatabaseExecStatementException e) {
            e.printStackTrace();   
            throw e;
        }
        //_databaseService.Tables();
        //_databaseService.insertDefaultData();
    }


    /**
     * Transfers a song to the fylesystem of the database server
     * @param title
     * @param artist
     * @param genre
     * @param lyricsPath
     * @param audioFormat
     * @param audioFilePath
     * @throws DatabaseExecStatementException
     * @throws IOException
     * @throws AudioTransferErrorException
     * @throws AudioFormatForMusicAlreadyExistsException
     */
    public void addMusic(String title, String artist, String[] genre, 
                         String lyricsPath, String audioFormat, String audioFilePath) throws DatabaseExecStatementException, 
                                                                                      IOException, AudioTransferErrorException,
                                                                                      AudioFormatForMusicAlreadyExistsException,
                                                                                      Exception{

        
        System.out.println("Adding '" + title + "' by '" + artist + "' in " + audioFormat + " format to database...");
                                                                                                
        try {
            if(CheckIfMusicExists(title, artist)){
                AddFormatToMusic(title, artist, audioFormat, audioFilePath);
                return;
            }
            
            //insert song into songs table
            String insertStatement = "INSERT INTO songs (title, artists, lyrics) VALUES (?, ?, ?);";
            String[] values = new String[]{title, artist, new String(UtilClasses.ReadFromFile(lyricsPath))};
            _databaseService.execPreparedStatement(insertStatement, values);
            
            //insert genre into genres table
            for (String genreName : genre) {
                _databaseService.execStatement("INSERT IGNORE INTO genres (genre_name) VALUES ('" + genreName + "');");

                //insert song-genre pair into song_genres table
                _databaseService.execStatement("INSERT INTO song_genres (song_id, genre_name) VALUES ((" +
    
                                                //song id insert
                                                "SELECT song_id " +
                                                "FROM songs " +
                                                "WHERE title = '" + title 
    
                                                + "'), (" +
    
                                                //genre id insert
                                                "SELECT genre_name " +
                                                "FROM genres " +
                                                "WHERE genre_name = '" + genreName
                                                
                                                + "'));");
            }
            
                                            
            //audioFilePath -> path IN THIS SERVER to file containing audio
            //audioDBPath -> path IN DATABASE SERVER to file containing audio (audio binary won't be stored in database, only path to file; it will instead
            //be stored in a file in the  database server's filesystem)

            String audioDBPath = artist + "/" + title;

            if(audioFormat.charAt(0) != '.'){
                audioDBPath += ".";
            }

            audioDBPath += audioFormat;
            
            //insert song-audio pair into song_audio table
            _databaseService.execStatement("INSERT INTO song_audio (song_id, format, audio_location) VALUES ((" +
                                           "SELECT song_id " +
                                           "FROM songs " + 
                                           "WHERE title = '" + title + "'), '" + audioFormat + "', '" + audioDBPath + "');");

            //transfer audio file to database server
            _databaseService.transferFileToDBServer(audioFilePath, audioDBPath);
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        }catch (AudioTransferErrorException e){
            throw e;
        }catch (AudioFormatForMusicAlreadyExistsException e){
            throw e;
        }catch (IOException e){
            throw e;
        }catch (Exception e){
            throw e;
        }

    }

    public void clearAllTables() throws DatabaseExecStatementException, DatabaseConnectionException{
        try {

            java.sql.DatabaseMetaData databaseMetaData = _databaseService.getDatabaseMetadata();

            // Disable foreign key checks
            _databaseService.execStatement("SET foreign_key_checks = 0;");

            // Get all table names
            ResultSet tables = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
            List<String> tableNames = new ArrayList<>();

            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            // Iterate through tables and clear them
            for (String tableName : tableNames) {
                String deleteStatement = "DELETE FROM " + tableName + ";";
                _databaseService.execStatement(deleteStatement);
            }

            // Enable foreign key checks
            _databaseService.execStatement("SET foreign_key_checks = 1;");

            System.out.println("All tables cleared successfully.");

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (DatabaseConnectionException e){
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    //clear all songs tables
    public void clearSongsTables() throws DatabaseExecStatementException{
        try {
            _databaseService.execStatement("DELETE FROM songs;");
            _databaseService.execStatement("DELETE FROM genres;");
            
            //song_genres and song_audio tables will be cleared automatically because of the ON DELETE CASCADE constraint

            System.out.println("Songs tables cleared successfuly.");

        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    public void clearUsersTables() throws DatabaseExecStatementException{
        try {
            _databaseService.execStatement("DELETE FROM users;");
            _databaseService.execStatement("DELETE FROM families;");

            System.out.println("Users table cleared successfuly.");
            
            //user_songs table will be cleared automatically because of the ON DELETE CASCADE constraint
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    /**
     * Adds a new user to the database; password must already be hashed
     * @param username
     * @param password
     * @throws DatabaseExecStatementException
     */
    public void addNewUser(String username, String hashedPassword) throws DatabaseExecStatementException, UsernameAlreadyExistsException{
        try {
            
            if(checkIfUserAlreadyExists(username))
                throw new UsernameAlreadyExistsException(username);
            

            _databaseService.execStatement("INSERT INTO users (username, hash_password) VALUES ('" + username + "', '" + hashedPassword + "');");
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    /**
     * Will seach for songs in the database that match the search input (title or artist)
     * @param searchInput
     * @return
     */
    public List<Media> searchForSongs(String searchInput){
        try {
            ResultSet rs = _databaseService.execQuery("SELECT * FROM songs WHERE title LIKE '%" + searchInput + "%' OR artists LIKE '%" + searchInput + "%';");
            List<Media> songs = new ArrayList<>();
            while(rs.next()){
                ResultSet genresRs = _databaseService.execQuery("SELECT genre_name FROM song_genres WHERE song_id = " + rs.getInt("song_id") + ";");
                List<String> genres = new ArrayList<>();
                while(genresRs.next()){
                    genres.add(genresRs.getString("genre_name"));
                }
                songs.add(new Media(rs.getString("format"),rs.getString("title"), rs.getString("artists"), (String[]) genres.toArray()));
            }
            return songs;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    /**
     * Execute a purchase of a song by a user
     * @param username
     * @param songTitle
     * @param songArtist
     * @throws DatabaseExecStatementException
     */
    public void executeUserPurchase(String username, String songTitle, String songArtist) throws DatabaseExecStatementException,
                                                                                                 UserAlreadyOwnsMusicException,
                                                                                                 MusicNotFoundException,
                                                                                                 Exception{
        try {
            int songId = getSongId(songTitle, songArtist);

            //check if user already owns song
            ResultSet rs = _databaseService.execQuery("SELECT * FROM user_songs WHERE username = '" + username + "' AND song_id = " + songId + ";");

            if(rs.next()){
                throw new UserAlreadyOwnsMusicException(username, songTitle, songArtist);
            }

            _databaseService.execStatement("INSERT INTO user_songs (username, song_id) VALUES ('" + username + "', " + songId + ");");
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        } catch (MusicNotFoundException e){
            throw e;
        } catch (Exception e){
            throw e;
        }
    }
    
    /**
     * Checks if user owns a particular song; if not, throws an exception; if yes, returns the song
     * @param title
     * @param artist
     * @param format
     * @param user
     * @return
     * @throws DatabaseExecStatementException
     * @throws UserDoesntOwnMusicException
     */
    public Media getSongForUser(String title, String artist, String format, String username) throws DatabaseExecStatementException, 
                                                                                              UserDoesntOwnMusicException,
                                                                                              MusicNotFoundException,
                                                                                              MusicFormatNotFoundException{

        try {

            // -------------------------------- Verify if song exists --------------------------------
            ResultSet resultSetMusic = _databaseService.execQuery("SELECT * " +
                                                                  "FROM songs " +                                                                
                                                                  "WHERE title = '" + title + "' AND artists = '" + artist + "';");

            if(!resultSetMusic.next()){
                throw new MusicNotFoundException(title, artist);
            }

            // -------------------------------- Verify if user owns song --------------------------------
            String songId = resultSetMusic.getString("song_id");

            
            ResultSet resultSetUser = _databaseService.execQuery("SELECT * " +
                                                                 "FROM user_songs " + 
                                                                 "WHERE username = '" + username + "' AND song_id = " + songId + ";");
            
            if(!resultSetUser.next()){
                throw new UserDoesntOwnMusicException(username, title, artist);
            }


            String songTitle = resultSetMusic.getString("title");
            String songArtist = resultSetMusic.getString("artists");
            String lyrics = resultSetMusic.getString("lyrics");
            
            ResultSet resultSetGenre = _databaseService.execQuery("SELECT * " +
                                                                  "FROM song_genres " +
                                                                  "WHERE song_id = " + songId + ";");


            List<String> genres  = new ArrayList<>();
            while (resultSetGenre.next()) {
                genres.add(resultSetGenre.getString("genre_name"));
            }

            //format always starts with a dot
            if(format.charAt(0) != '.'){
                format = "." + format;
            }

            ResultSet resultSetAudio = _databaseService.execQuery("SELECT * " +
                                                                  "FROM song_audio " +
                                                                  "WHERE song_id = " + songId + " AND format = '" + format + "';");

            if(!resultSetAudio.next()){
                throw new MusicFormatNotFoundException(title, artist, format);
            }
            
            byte[] musicFile = _databaseService.getAudioFile(resultSetAudio.getString("audio_location"));


            return new Media(resultSetUser.getString("username"),
                             resultSetAudio.getString("format"),
                             songArtist,
                             songTitle,
                             genres.toArray(new String[0]),
                             new String[]{lyrics},
                             musicFile
                             );
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    private int getSongId(String title, String artist) throws DatabaseExecStatementException, MusicNotFoundException, Exception{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT song_id FROM songs WHERE title = '" + title + "' AND artists = '" + artist + "';");

            if(!rs.next()){
                throw new MusicNotFoundException(title, artist);
            }

            int songId = rs.getInt("song_id");
            rs.close();

            return songId;
        }
        catch (Exception e){
            throw e;
        }
    }

    public boolean checkIfUserAlreadyExists(String username) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT * FROM users WHERE username = '" + username + "';");
            if(rs.next()){
                return true;
            }

            return false;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public void addPermanentKeyToUser(String username, String permanentKeyInBase64) throws DatabaseExecStatementException{
        try {
            _databaseService.execStatement("UPDATE users SET permanent_key_base64 = '" + permanentKeyInBase64 + "' WHERE username = '" + username + "';");
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    public void addTemporaryKeyToUser(String username, String temporaryKeyInBase64) throws DatabaseExecStatementException{
        try {
            _databaseService.execStatement("UPDATE users SET temporary_key_base64 = '" + temporaryKeyInBase64 + "' WHERE username = '" + username + "';");
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    public String GetPasswordHash(String username) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT hash_password FROM users WHERE username = '" + username + "';");
            rs.next();
            String passwordHash = rs.getString("hash_password");
            rs.close();
            return passwordHash;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    public String GetEncryptedPermanentKeyInBase64(String username) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT permanent_key_base64 FROM users WHERE username = '" + username + "';");
            rs.next();
            return rs.getString("permanent_key_base64");
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public String GetEncryptedTemporaryKeyInBase64(String username) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT temporary_key_base64 FROM users WHERE username = '" + username + "';");
            rs.next();
            return rs.getString("temporary_key_base64");
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }


    public boolean CheckIfMusicExists(String title, String artist) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT * FROM songs WHERE title = '" + title + "' AND artists = '" + artist + "';");

            boolean exists = rs.next();
            rs.close();

            return exists;

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }

    }

    public void AddFormatToMusic(String title, String artist, String format, String audioFilePath) throws DatabaseExecStatementException, 
                                                                                                           IOException, Exception,
                                                                                                           AudioTransferErrorException,
                                                                                                           AudioFormatForMusicAlreadyExistsException{
        try {
            
            int songId = getSongId(title, artist);
            
            if(CheckIfFormatExists(Integer.toString(songId), format)){
                throw new AudioFormatForMusicAlreadyExistsException(title, artist, format);
            }
            
            //audioFilePath -> path IN THIS SERVER to file containing audio
            //audioDBPath -> path IN DATABASE SERVER to file containing audio (audio binary won't be stored in database, only path to file; it will instead
            //be stored in a file in the  database server's filesystem)

            String audioDBPath = artist + "/" + title;

            if(format.charAt(0) != '.'){
                audioDBPath += ".";
            }

            audioDBPath += format;

            //insert song-audio pair into song_audio table
            _databaseService.execStatement("INSERT INTO song_audio (song_id, format, audio_location) VALUES ((" +
                                           "SELECT song_id " +
                                           "FROM songs " + 
                                           "WHERE title = '" + title + "'), " + 
                                           "'" + format + "', " + 
                                           "'" + audioDBPath + "');");

            //transfer audio file to database server
            _databaseService.transferFileToDBServer(audioFilePath, audioDBPath);
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (AudioTransferErrorException e){
            throw e;
        } catch (IOException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }

    public boolean CheckIfFormatExists(String songId, String format){
        try {

            //format starts with .
            if(format.charAt(0) != '.'){
                format = "." + format;
            }

            ResultSet rs = _databaseService.execQuery("SELECT * FROM song_audio WHERE song_id = " + songId + " AND format = '" + format + "';");
            if(rs.next()){
                return true;
            }

            return false;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    public List<Integer> GetSongsIdByGenre(String genre) throws DatabaseExecStatementException{
        try {

            ResultSet rs = _databaseService.execQuery("SELECT song_id FROM song_genres WHERE genre_name LIKE '%" + genre + "%';");
            List<Integer> songsId = new ArrayList<>();
            while(rs.next()){
                songsId.add(rs.getInt("song_id"));
            }
            rs.close();
            return songsId;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    //get songs id by artist
    public List<Integer> GetSongsIdByArtist(String artist) throws DatabaseExecStatementException{
        try {

            ResultSet rs = _databaseService.execQuery("SELECT song_id FROM songs WHERE artists LIKE '%" + artist + "%';");
            List<Integer> songsId = new ArrayList<>();
            while(rs.next()){
                songsId.add(rs.getInt("song_id"));
            }
            rs.close();
            return songsId;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    //get song title by id
    public String GetSongTitleById(int songId) throws DatabaseExecStatementException, MusicNotFoundException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT title FROM songs WHERE song_id = " + songId + ";");

            //should never happen
            if(!rs.next()){
                throw new OtherErrorException("Song with id " + songId + " not found.");
            }

            String title = rs.getString("title");
            rs.close();

            return title;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    //get song artist by id
    public String GetSongArtistById(int songId) throws DatabaseExecStatementException, MusicNotFoundException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT artists FROM songs WHERE song_id = " + songId + ";");

            //should never happen
            if(!rs.next()){
                throw new OtherErrorException("Song with id " + songId + " not found.");
            }

            String artist = rs.getString("artists");
            rs.close();

            return artist;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    public List<String> GetSongGenresById(int songId) throws DatabaseExecStatementException, MusicNotFoundException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT genre_name FROM song_genres WHERE song_id = " + songId + ";");
            List<String> genres = new ArrayList<>();
            while(rs.next()){
                genres.add(rs.getString("genre_name"));
            }
            rs.close();
            return genres;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    //get all formats for song id
    public List<String> GetSongFormatsById(int songId) throws DatabaseExecStatementException, MusicNotFoundException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT format FROM song_audio WHERE song_id = " + songId + ";");
            List<String> formats = new ArrayList<>();
            while(rs.next()){
                formats.add(rs.getString("format"));
            }
            rs.close();
            return formats;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    //search by title
    public List<Integer> GetSongsIdByTitle(String title) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT song_id FROM songs WHERE title LIKE '%" + title + "%';");
            List<Integer> songsId = new ArrayList<>();
            while(rs.next()){
                songsId.add(rs.getInt("song_id"));
            }
            rs.close();
            return songsId;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    public List<Integer> GetSongsIdsOfUserPurchasedSongs(String username) throws AccountNotFoundException{
        try {

            if(!checkIfUserAlreadyExists(username))
                throw new AccountNotFoundException(username);
            
            ResultSet rs = _databaseService.execQuery("SELECT song_id FROM user_songs WHERE username = '" + username + "';");
            List<Integer> songsId = new ArrayList<>();
            while(rs.next()){
                songsId.add(rs.getInt("song_id"));
            }
            rs.close();
            return songsId;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    
    public void AddSongToUserPreferences(String username, String songTitle, String songArtist) throws DatabaseExecStatementException, 
                                                                                                       MusicNotFoundException,
                                                                                                       UserDoesntOwnMusicException,
                                                                                                       Exception{
        try {
            int songId = getSongId(songTitle, songArtist);

            //check if user owns song
            ResultSet rs = _databaseService.execQuery("SELECT * FROM user_songs WHERE username = '" + username + "' AND song_id = " + songId + ";");

            if(!rs.next()){
                throw new UserDoesntOwnMusicException(username, songTitle, songArtist);
            }

            _databaseService.execStatement("INSERT INTO user_preferences (username, preferenced_song_id) VALUES ('" + username + "', " + songId + ");");
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e){
            throw e;
        } catch (UserDoesntOwnMusicException e){
            throw e;
        } catch (Exception e){
            throw e;
        }
    }


    public List<Integer> GetSongsIdsOfUserPreferences(String username) throws AccountNotFoundException, DatabaseExecStatementException{
        try {

            if(!checkIfUserAlreadyExists(username))
                throw new AccountNotFoundException(username);

            ResultSet rs = _databaseService.execQuery("SELECT preferenced_song_id FROM user_preferences WHERE username = '" + username + "';");
            List<Integer> songsId = new ArrayList<>();
            while(rs.next()){
                songsId.add(rs.getInt("preferenced_song_id"));
            }
            rs.close();
            return songsId;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    private boolean DoesFamilyExist(String familyName) throws DatabaseExecStatementException{
        try {

            String statement = "SELECT * FROM families WHERE family_name = (?)";
            String[] values = new String[]{familyName};

            ResultSet rs = _databaseService.execPreparedQuery(statement, values);
            
            boolean exists = rs.next();
            rs.close();

            return exists;

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }


    private boolean IsUserAlreadyInAFamily(String username) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT * FROM users_families WHERE username = '" + username + "';");
            
            boolean exists = rs.next();
            rs.close();

            return exists;

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }


    public void CreateNewFamilyForUser(String username, String familyName) throws DatabaseExecStatementException,
                                                                                  FamilyNameAlreadyExistsException,
                                                                                  AccountNotFoundException,
                                                                                  UserAlreadyBelongsToAFamilyException,
                                                                                  Exception{

        try {

            //user logged on
            if(!checkIfUserAlreadyExists(username)){
                throw new AccountNotFoundException(username);
            }

            if(DoesFamilyExist(familyName)){
                throw new FamilyNameAlreadyExistsException(familyName);
            }

            if(IsUserAlreadyInAFamily(username)){
                throw new UserAlreadyBelongsToAFamilyException(username, familyName);
            }

            String statement = "INSERT INTO families (family_name) VALUES (?);";
            _databaseService.execPreparedStatement(statement, new String[]{familyName});

            statement = "INSERT INTO users_families (username, family_name, will_be_removed) VALUES (?, ?, ?);";
            _databaseService.execPreparedStatement(statement, new String[]{username, familyName, "0"});


        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (FamilyNameAlreadyExistsException e){
            throw e;
        } catch (AccountNotFoundException e){
            throw e;
        } catch (Exception e){
            throw e;
        }


    }


    public void RegisterFamilyPassword(String familyName, String familyPassword) throws DatabaseExecStatementException{
        try {

            String statement = "UPDATE families SET hash_family_password = (?) WHERE family_name = (?);";

            String[] values = new String[]{familyPassword, familyName};

            _databaseService.execPreparedStatement(statement, values);
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    public List<String> getAllUsernames() throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT username FROM users;");
            List<String> usernames = new ArrayList<>();
            while(rs.next()){
                usernames.add(rs.getString("username"));
            }
            rs.close();
            return usernames;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }


    public void removeToBeRemovedMembersFromFamilies() throws DatabaseExecStatementException{
        try {
            _databaseService.execStatement("DELETE FROM users_families WHERE will_be_removed = 1;");
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }


    public List<String> getAllUsersInFamilies() throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT username FROM users_families;");
            List<String> usernames = new ArrayList<>();
            while(rs.next()){
                usernames.add(rs.getString("username"));
            }
            rs.close();
            return usernames;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }

    }


    public List<String> getAllFamiliesNames() throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT family_name FROM families;");
            List<String> familiesNames = new ArrayList<>();
            while(rs.next()){
                familiesNames.add(rs.getString("family_name"));
            }
            rs.close();
            return familiesNames;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }


    public void setFamilyTemporaryKey(String familyName, String temporaryKeyInBase64) throws DatabaseExecStatementException{
        try {
            String statement = "UPDATE families SET temporary_key_family_base64 = (?) WHERE family_name = (?);";

            String[] values = new String[]{temporaryKeyInBase64, familyName};

            _databaseService.execPreparedStatement(statement, values);
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }


    public String GetFamilyTemporaryKey(String familyName) throws DatabaseExecStatementException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT temporary_key_family_base64 FROM families WHERE family_name = '" + familyName + "';");
            rs.next();
            
            String temporaryKey = rs.getString("temporary_key_family_base64");
            rs.close();

            return temporaryKey;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    public String GetFamilyOfUser(String username) throws DatabaseExecStatementException{
        try {
            String statement = "SELECT family_name FROM users_families WHERE username = (?);";
            ResultSet rs = _databaseService.execPreparedQuery(statement, new String[]{username});

            rs.next();
            
            String familyName = rs.getString("family_name");
            rs.close();

            return familyName;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    private boolean DoesUserAlreadyBelongToAFamily(String username) throws DatabaseExecStatementException{
        try {
            String statement = "SELECT * FROM users_families WHERE username = (?);";
            ResultSet rs = _databaseService.execPreparedQuery(statement, new String[]{username});

            boolean exists = rs.next();
            rs.close();

            return exists;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }


    public String GetFamilyPassowrdHash(String familyName) throws DatabaseExecStatementException, FamilyDoesNotExistException{
        try {
            ResultSet rs = _databaseService.execQuery("SELECT hash_family_password FROM families WHERE family_name = '" + familyName + "';");
            
            if(!rs.next()){
                throw new FamilyDoesNotExistException(familyName);
            }
            
            String familyPasswordHash = rs.getString("hash_family_password");
            rs.close();

            return familyPasswordHash;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState()); 
        }
    }

    public void AddUserToFamily(String username, String familyName) throws DatabaseExecStatementException, 
                                                                           UserAlreadyBelongsToAFamilyException{
        try {
            
            if(DoesUserAlreadyBelongToAFamily(username)){
                throw new UserAlreadyBelongsToAFamilyException(username, familyName);
            }

            String statement = "INSERT INTO users_families (username, family_name, will_be_removed) VALUES (?, ?, ?);";
            _databaseService.execPreparedStatement(statement, new String[]{username, familyName, "0"});


        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }


    public boolean DoesUserBelongToAFamily(String username) throws DatabaseExecStatementException{
        try {
            String statement = "SELECT * FROM users_families WHERE username = (?);";
            ResultSet rs = _databaseService.execPreparedQuery(statement, new String[]{username});

            boolean exists = rs.next();
            rs.close();

            return exists;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (SQLException e){
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    
    public void SetUserToLeaveFamily(String username) throws DatabaseExecStatementException{
        try {
            String statement = "UPDATE users_families SET will_be_removed = 1 WHERE username = (?);";
            _databaseService.execPreparedStatement(statement, new String[]{username});
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }





}
