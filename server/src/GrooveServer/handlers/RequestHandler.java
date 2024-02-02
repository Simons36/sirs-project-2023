package GrooveServer.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CryptographicLibrary.structs.ProtectReturnStruct;
import GrooveServer.core.CoreServer;
import GrooveServer.exceptions.OtherErrorException;
import GrooveServer.objects.Media;
import GrooveServer.service.exceptions.DatabaseExecStatementException;



public class RequestHandler {

    private static CoreServer _coreServer;
    
    public RequestHandler() {
        _coreServer = CoreServer.getInstance();
    }

    public void register(String username, String password) {
        try {
            _coreServer.Register(username, password);
        } catch (Exception e) {
            throw e;
        }
    }

    public void login(String username, String password) {
        try {
            _coreServer.Login(username, password);
        } catch (Exception e) {
            throw e;
        }
    }

    public Map<String, List<String>> searchMedia(String mediaTitle) {
        try {
            // Call the method that returns a list of Media objects
            List<Media> allSongsMetadata = _coreServer.SearchSongByTitle(mediaTitle);

            // Create a map to store grouped results
            Map<String, List<String>> resultMap = new HashMap<>();

            // Process each Media object and group by title
            for (Media media : allSongsMetadata) {
                String title = media.getTitle();
                String format = media.getFormat();

                // If the title is not in the map, add a new entry with an empty list
                resultMap.putIfAbsent(title, new ArrayList<>());

                // Add the format to the list associated with the title
                resultMap.get(title).add(format);
            }

            return resultMap;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<String> searchArtist(String artist) {
        try {
            // Call the method that returns a list of Media objects for the given artist
            List<Media> allSongsByArtist = _coreServer.SearchByArtist(artist);
    
            // Extract and return the list of media titles
            List<String> mediaTitles = new ArrayList<>();
            for (Media media : allSongsByArtist) {
                mediaTitles.add(media.getTitle());
            }
            return mediaTitles;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<String> searchGenre(String genre) {
        try {
            // Call the method that returns a list of Media objects for the given genre
            List<Media> allSongsByGenre = _coreServer.SearchByGenre(genre);
    
            // Extract and return the list of media titles
            List<String> mediaTitles = new ArrayList<>();
            for (Media media : allSongsByGenre) {
                mediaTitles.add(media.getTitle());
            }
            return mediaTitles;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<String> getUserPurchases(String clientID) {
        try {
            // Call the method that returns a list of Media objects for the given clientID
            List<Media> allSongsByClient = _coreServer.GetUserPurchases(clientID);
    
            // Extract and return the list of media titles
            List<String> mediaTitles = new ArrayList<>();
            for (Media media : allSongsByClient) {
                mediaTitles.add(media.getTitle());
            }
            return mediaTitles;
        } catch (Exception e) {
            throw e;
        }
    }

    public void purchaseSong(String clientID, String songTitle, String artist) {
        try {
            _coreServer.PurchaseSong(clientID, songTitle, artist);
        } catch (Exception e) {
            throw e;
        }
    }

    public void addPreference(String clientId, String songName, String artist) {
        try {
            _coreServer.AddMusicToUserPreferences(clientId, songName, artist);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<String> getUserPreferences(String clientId) {
        try {
            // Call the method that returns a list of Media objects for the given clientID
            List<Media> allSongsByClient = _coreServer.GetUserPreferences(clientId);
    
            // Extract and return the list of media titles
            List<String> mediaTitles = new ArrayList<>();
            for (Media media : allSongsByClient) {
                mediaTitles.add(media.getTitle());
            }
            return mediaTitles;
        } catch (Exception e) {
            throw e;
        }
    }

    public ProtectReturnStruct downloadSong(String clientId, String songName, String artist, String fileFormat) {
        try {
            return _coreServer.GetSongForUser(clientId, songName, artist, fileFormat);
        } catch (Exception e) {
            throw e;
        }
    }

    public ProtectReturnStruct previewSong(String songName, String artist, String fileFormat) {
        try {
            return _coreServer.GetPreviewOfSong(null, songName, artist, fileFormat);
        } catch (Exception e) {
            throw e;
        }
    }

    public void streamSong(String clientId, String songName, String artist, String fileFormat) {
        try {
            ProtectReturnStruct protectReturnStruct = _coreServer.GetSongForUser(clientId, songName, artist, fileFormat);
        } catch (Exception e) {
            throw e;
        }
    }

    public String createFamily(String clientId, String familyName) {
        try {
            return _coreServer.CreateNewFamily(clientId, familyName);
        } catch (Exception e) {
            throw e;
        }
    }

    public void joinFamily(String clientId, String familyName, String familyCode) {
        try {
            _coreServer.UserJoinsFamily(clientId, familyName, familyCode);
        } catch (Exception e) {
            throw e;
        }
    }

    public void leaveFamily(String clientId) {
        try {
            _coreServer.UserLeavesCurrentFamily(clientId);
        } catch (Exception e) {
            throw e;
        }
    }

}

    

    
