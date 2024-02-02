package GrooveServer.objects;

public class DatabaseServiceConfigStruct {

    private final String IP;
    private final String DATABASE_NAME;
    private final String USERNAME;
    private final String PASSWORD;
    private final String DATABASE_SERVER_USERNAME;
    private final String PRIVATE_KEY_PATH;
    private final String AUDIO_FILES_DIRECTORY_IN_DATABASE;

    public DatabaseServiceConfigStruct(String IP,
                                       String DATABASE_NAME,
                                       String USERNAME,
                                       String PASSWORD,
                                       String DATABASE_SERVER_USERNAME,
                                       String PRIVATE_KEY_PATH,
                                       String AUDIO_FILES_DIRECTORY_IN_DATABASE){
        this.IP = IP;
        this.DATABASE_NAME = DATABASE_NAME;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.DATABASE_SERVER_USERNAME = DATABASE_SERVER_USERNAME;
        this.PRIVATE_KEY_PATH = PRIVATE_KEY_PATH;
        this.AUDIO_FILES_DIRECTORY_IN_DATABASE = AUDIO_FILES_DIRECTORY_IN_DATABASE;
    }

    //getters
    public String getIP(){
        return IP;
    }

    public String getDATABASE_NAME(){
        return DATABASE_NAME;
    }

    public String getUSERNAME(){
        return USERNAME;
    }

    public String getPASSWORD(){
        return PASSWORD;
    }

    public String getDATABASE_SERVER_USERNAME(){
        return DATABASE_SERVER_USERNAME;
    }

    public String getPRIVATE_KEY_PATH(){
        return PRIVATE_KEY_PATH;
    }
    
    public String getAUDIO_FILES_DIRECTORY_IN_DATABASE(){
        return AUDIO_FILES_DIRECTORY_IN_DATABASE;
    }

    
}
