package GrooveServer.service.audio_transfer;

import com.jcraft.jsch.*;

import GrooveServer.service.exceptions.AudioTransferConnectionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class will handle the transfer of the audio files from the application server to the database server.
 * It will use the FTPS protocol (FTP over SSL)
 * @throws JSchException
 * @throws SftpException
 * @throws IOException
 */
public class AudioTransfer implements AudioTransferInterface{

    private static final int SFTP_PORT = 22; 

    private String _host;
    private String _username;

    //this is the root directory on the remote server where the audio files will be stored (for example, /home/groovegalaxy/audio_files)
    private String _rootDirRemoteServer;

    private JSch _jsch;
    
    public AudioTransfer(String host, String username, String rootDirRemoteServer, String privateKeyPath) throws AudioTransferConnectionException{

        _host = host;
        _username = username;
        _rootDirRemoteServer = rootDirRemoteServer;
        
        _jsch = new JSch();
        
        try {
            //add private key
            _jsch.addIdentity(privateKeyPath);

            //test connection
            AudioTransferUtil.TestConnection(_jsch, _username, _host, SFTP_PORT);
        } catch (JSchException e) {
            throw new AudioTransferConnectionException(e.getMessage(), e.getCause());
        }

    }
    
    /**
     * Uploads a file to the remote server
     * @param localFilePath
     * @param remoteFilePath
     * @throws JSchException
     * @throws SftpException
     * @throws IOExceptionthe constructor
     */
    public void UploadAudioFile(String localFilePath, String remoteFilename) throws JSchException, SftpException, IOException{
        Session session = null;
        ChannelSftp channelSftp = null;
        
        String fullRemoteFilePath = _rootDirRemoteServer + "/" + remoteFilename;
        String filename = fullRemoteFilePath.split("/")[fullRemoteFilePath.split("/").length - 1];
        
        try {
            
            session = _jsch.getSession(_username, _host, SFTP_PORT);
            session.setConfig("PreferredAuthentications", "publickey");
            session.setConfig("StrictHostKeyChecking", "no");
            
            
            // Connect to the server
            session.connect();
            
            // Create SFTP channel
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            //create missing directories on the remote server
            AudioTransferUtil.changeToCorrectRemoteDirectory(channelSftp, fullRemoteFilePath);


            // Upload file
            try (FileInputStream fis = new FileInputStream(new File(localFilePath))) {
                channelSftp.put(fis, filename);
            }

            System.out.println("Music added successfully! It is stored at /home/" + _username + "/" + fullRemoteFilePath + ".");
    
        } catch (JSchException | SftpException | IOException e) {
            throw e;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        
    }


    public byte[] GetAudioFile(String filepath) throws JSchException, SftpException, IOException{
        Session session = null;
        ChannelSftp channelSftp = null;

        try {

            //create session
            session = _jsch.getSession(_username, _host, SFTP_PORT);
            session.setConfig("PreferredAuthentications", "publickey");
            session.setConfig("StrictHostKeyChecking", "no");

            
            // Connect to the server
            session.connect();
            
            // Create SFTP channel
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();


            String[] pathSplit = filepath.split("/");

            //the last element is the filename
            String filename = pathSplit[pathSplit.length - 1];
            
            //we add to root directory the path to the file
            String pathToFile = _rootDirRemoteServer;
            for(int i = 0; i < pathSplit.length - 1; i++){
                pathToFile += "/" + pathSplit[i];
            }
            
            //currently we are at /home/<username>
            //now we change to the directory where the music is stored
            channelSftp.cd(pathToFile);

            //now we get the file
            byte[] file = channelSftp.get(filename).readAllBytes();

            return file;

        } catch (JSchException | SftpException | IOException e) {
            throw e;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public void Shutdown(){

        try {
            _jsch.removeAllIdentity();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    
}
