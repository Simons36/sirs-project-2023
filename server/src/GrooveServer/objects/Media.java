package GrooveServer.objects;

import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import GrooveServer.util.UtilClasses;

public class Media {

    private String owner;
    private String format;
    private String artist;
    private String title;
    private String[] genre;
    private String[] lyrics;
    private String audioBase64;

    public Media(String owner, String format, String artist, String title, String[] genre, String[] lyrics,
            String audioBase64) {
        this.owner = owner;
        this.format = format;
        this.artist = artist;
        this.title = title;
        this.genre = genre;
        this.lyrics = lyrics;
        this.audioBase64 = audioBase64;
    }

    //constructor that receive audioBase64 in byte[] and convert it to String

    public Media(String owner, String format, String artist, String title, String[] genre, String[] lyrics,
            byte[] audioInBytes) {
                
        this(owner, format, artist, title, genre, lyrics, UtilClasses.encodeByteToBase64(audioInBytes));
    }

    //constructor with only the media info; this is used when the client wants to
    // search for songs
    public Media(String format, String artist, String title, String[] genre){
        this.format = format;
        this.artist = artist;
        this.title = title;
        this.genre = genre;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getFormat() {
        return this.format;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getTitle() {
        return this.title;
    }

    public String[] getGenre() {
        return this.genre;
    }

    public String[] getLyrics() {
        return this.lyrics;
    }

    public String getAudioBase64() {
        return this.audioBase64;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void CutAudioBySomeFraction(double fraction){
        this.audioBase64 = this.audioBase64.substring(0, (int) (this.audioBase64.length() * fraction));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((artist == null) ? 0 : artist.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + Arrays.hashCode(genre);
        result = prime * result + Arrays.hashCode(lyrics);
        result = prime * result + ((audioBase64 == null) ? 0 : audioBase64.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Media other = (Media) obj;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        if (format == null) {
            if (other.format != null)
                return false;
        } else if (!format.equals(other.format))
            return false;
        if (artist == null) {
            if (other.artist != null)
                return false;
        } else if (!artist.equals(other.artist))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (!Arrays.equals(genre, other.genre))
            return false;
        if (!Arrays.equals(lyrics, other.lyrics))
            return false;
        if (audioBase64 == null) {
            if (other.audioBase64 != null)
                return false;
        } else if (!audioBase64.equals(other.audioBase64))
            return false;
        return true;
    }

    public JsonObject toJSON() {
        JsonObject rootJson = new JsonObject();

        JsonObject mediaObject = new JsonObject();
        
        JsonObject mediaInfoObject = new JsonObject();
        mediaInfoObject.addProperty("owner", this.owner);
        mediaInfoObject.addProperty("format", this.format);
        mediaInfoObject.addProperty("artist", this.artist);
        mediaInfoObject.addProperty("title", this.title);

        mediaInfoObject.add("genre", createJsonArray(this.genre));

        mediaObject.add("mediaInfo", mediaInfoObject);


        JsonObject mediaContent = new JsonObject();

        mediaContent.add("lyrics", createJsonArray(this.lyrics));

        mediaContent.addProperty("audioBase64", this.audioBase64);

        mediaObject.add("mediaContent", mediaContent);

        rootJson.add("media", mediaObject);

        return rootJson;
    }

    private JsonArray createJsonArray(String[] values) {
        JsonArray jsonArray = new JsonArray();
        Arrays.stream(values).forEach(jsonArray::add);
        return jsonArray;
    }

    //to string
    @Override
    public String toString() {

        String toReturn = "{" +
            // " owner='" + getOwner() + "'" +
            ", format='" + getFormat() + "'" +
            ", artist='" + getArtist() + "'" +
            ", title='" + getTitle() + "'";
            // ", lyrics='" + getLyrics() + "'" +
            // ", audioBase64='" + getAudioBase64() + "'" +
            
        toReturn += ", genre=[";

        for(int i = 0; i < genre.length; i++){
            toReturn += genre[i];
            if(i != genre.length - 1){
                toReturn += ", ";
            }
        }

        toReturn += "]}";
            
        return toReturn;
    }

}
