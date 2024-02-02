package GrooveClient.songManagement;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import CryptographicLibrary.util.CryptoIO;
import CryptographicLibrary.util.CryptoOperations;
import CryptographicLibrary.util.JsonOperations;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import GrooveClient.exceptions.AuthenticityCheckFailed;
import GrooveClient.exceptions.ErrorDownloadingSong;
import GrooveClient.exceptions.UnableToReadJsonException;
import GrooveClient.keyManagement.KeyStoreMan;

public class SongManager {

    public static final String SONG_LIBRARY_PATH = "src/GrooveClient/songLibrary";

    /**
     * Stores the song in the client song library.
     * It receives the protected json document and the crypto API to check and unprotect it.
     * 
     * @param documentBytes
     * @throws ErrorDownloadingSong
     */
    public static void storeSong(byte[] encryptedContent, byte[] iv, byte[] digitalSignature, byte[] newEncryptedKey,
                                 CryptographicLibraryClientAPI cryptoAPI) throws ErrorDownloadingSong {
        try {
            cryptoAPI.AddEncryptedTemporaryKey(newEncryptedKey);
            cryptoAPI.Check(encryptedContent, iv, digitalSignature);
            JsonObject rootJson = cryptoAPI.Unprotect(encryptedContent, iv);
            JsonObject mediaInfoJson = rootJson.get("media").getAsJsonObject().
                                                get("mediaInfo").getAsJsonObject();
            String title = mediaInfoJson.get("title").getAsString() + "-" + 
                           mediaInfoJson.get("format").getAsString() + ".json";
            CryptoIO.writeJsonToFile(rootJson, SONG_LIBRARY_PATH + "/" + title);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw new ErrorDownloadingSong();
        }
    }

    public static JsonObject bytesToJsonObject(byte[] jsonBytes) {
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

        return jsonObject;
    }

    public static String getTitleFromJson(JsonObject rootJson) {
        JsonObject mediaInfoJson = rootJson.get("Media").getAsJsonObject().
                                                get("mediaInfo").getAsJsonObject();
        String title = mediaInfoJson.get("title").getAsString() + "-" + 
                       mediaInfoJson.get("format").getAsString() + ".json";
        return title;
    }

    /**
     * Verifys if a song is in the clients local library, i.e. he has downloaded it.
     * 
     * @param songName
     * @param fileFormat
     * @return
     */
    public static boolean songIsInLibrary(String songName, String fileFormat) {
        try {
            File folder = new File(SONG_LIBRARY_PATH);
            File[] songs = folder.listFiles();
            for (File song : songs) {
                String title = song.getName().split("-")[0];
                String format = song.getName().split("-")[1].split(".")[0];
                if (songName == title && fileFormat == format) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the audio of a song stored in the form of a json document in the clients
     * local library.
     * 
     * @param jsonFilePath
     * @return
     * @throws UnableToReadJsonException
     */
    public static byte[] getSongAudioFromLocalJson(String jsonFilePath, int second) throws UnableToReadJsonException {
        try {
            JsonObject rootJson = JsonOperations.getJsonFromBytes(CryptoIO.readFile(jsonFilePath));
            JsonObject mediaContent = rootJson.get("mediaContent").getAsJsonObject();
            byte[] audio = mediaContent.get("audioBase64").getAsString().getBytes();
            byte[] res = new byte[audio.length - second];
            System.arraycopy(audio, second, res, 0, audio.length - second);
            return res;
        } catch (Exception e) {
            throw new UnableToReadJsonException(jsonFilePath);
        }
    }

    /**
     * Returns the lyrics of a song stored in the form of a json document in the clients
     * local library.
     * 
     * @param jsonFilePath
     * @return
     * @throws UnableToReadJsonException
     */
    public static String getSongLyricsFromJson(String jsonFilePath) throws UnableToReadJsonException {
        try {
            JsonObject rootJson = JsonOperations.getJsonFromBytes(CryptoIO.readFile(jsonFilePath));
            JsonObject mediaContent = rootJson.get("mediaContent").getAsJsonObject();
            return mediaContent.get("lyrics").getAsString();
        } catch (Exception e) {
            throw new UnableToReadJsonException(jsonFilePath);
        }
    }

    /**
     * Checks the integrity of the audio sent from the application server.
     * 
     * @param encryptedAudio
     * @param iv
     * @param digitalSignature
     * @throws AuthenticityCheckFailed
     */
    public static void checkAudio(byte[] encryptedAudio, byte[] iv, byte[] digitalSignature, 
                                  CryptographicLibraryClientAPI cryptoAPI) throws AuthenticityCheckFailed {
        try {
            if (!cryptoAPI.Check(encryptedAudio, iv, digitalSignature)) {
                throw new AuthenticityCheckFailed();
            }
        } catch (Exception e) {
            throw new AuthenticityCheckFailed();
        }
    }
    
}
