package GrooveClient.songManagement;

import com.google.gson.JsonObject;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import GrooveClient.exceptions.AuthenticityCheckFailed;
import GrooveClient.exceptions.SongNotInLibraryException;
import GrooveClient.exceptions.UnableToPlayAudioException;
import GrooveClient.exceptions.UnableToReadJsonException;
import GrooveClient.keyManagement.KeyStoreMan;

public class SongPlayer {

    /**
     * Plays a song, if it has benn downloaded then plays it from library.
     * 
     * @param songName
     * @param fileFormat
     * @param second
     * @throws UnableToPlayAudioException
     */
    public static void playStoredSong(String songName, String fileFormat, int second) 
                  throws SongNotInLibraryException, UnableToPlayAudioException {
        try {
            if (SongManager.songIsInLibrary(songName, fileFormat)) {
                String jsonFilePath = SongManager.SONG_LIBRARY_PATH + songName + "-" + fileFormat + ".json";
                byte[] audio = SongManager.getSongAudioFromLocalJson(jsonFilePath, second);
                playAudio(audio, fileFormat);
                String lyrics = SongManager.getSongLyricsFromJson(jsonFilePath);
                System.out.println("The song is being played. The lyrics are:");
                System.out.println(lyrics);
            } else {
                throw new SongNotInLibraryException(songName);
            }
        } catch (UnableToPlayAudioException e) {
            throw e;
        } catch (UnableToReadJsonException e) {
            throw new UnableToPlayAudioException();
        }
    }

    /**
     * Plays unencrypted audio
     * 
     * @param audio
     * @throws UnableToPlayAudioException
     */
    public static void playAudio(byte[] audio, String format) throws UnableToPlayAudioException {
        try {
            System.out.println("audio: " + audio);
        } catch (Exception e) {
            throw new UnableToPlayAudioException();
        }
    }

    /**
     * Play encrypted audio
     * 
     * @param audio
     * @param iv
     * @throws UnableToPlayAudioException
     */
    public static void playEncryptedAudio(byte[] encryptedAudio, byte[] iv, String format, 
                                        CryptographicLibraryClientAPI cryptoAPI) throws UnableToPlayAudioException {
        try {
            JsonObject rootJson = cryptoAPI.Unprotect(encryptedAudio, iv);
            playAudio(rootJson.get("mediaContent").getAsJsonObject().
                      get("audioBase64").getAsString().getBytes(), format);
        } catch (Exception e) {
            throw new UnableToPlayAudioException();
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
