package GrooveServer.service.audio_transfer;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class AudioTransferUtil {
    //creates missing directories on the remote server
    protected static void changeToCorrectRemoteDirectory(ChannelSftp channelSftp, String remoteFilePath) throws SftpException{

        String[] directories = remoteFilePath.split("/");

        //last element is the file name, so we don't need to create a directory for it
        for (int i = 0; i < directories.length - 1; i++) {
            if (!directories[i].isEmpty()) {
                try {
                    channelSftp.cd(directories[i]);
                } catch (SftpException e) {
                    // Directory does not exist, so create it
                    channelSftp.mkdir(directories[i]);
                    channelSftp.cd(directories[i]);
                }
            }
        }

    }


    /**
     * Tests connection to the remote server; if not successful, throws a JSchException
     * @param jsch jsch object
     * @param username
     * @param host
     * @param port
     * @throws JSchException
     */
    protected static void TestConnection(JSch jsch, String username, String host, int port) throws JSchException{
        try {
            Session session = jsch.getSession(username, host, port);
            session.setConfig("PreferredAuthentications", "publickey");
            session.setConfig("StrictHostKeyChecking", "no");
            
            // Connect to the server
            session.connect();

            //disconnect
            session.disconnect();
        } catch (JSchException e) {
            throw e;
        }
    }

}
