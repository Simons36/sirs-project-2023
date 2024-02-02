package GrooveServer.service.audio_transfer;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public interface AudioTransferInterface {

    public void UploadAudioFile(String localFilePath, String remoteFilePath)  throws JSchException, SftpException, IOException;

    public byte[] GetAudioFile(String filepath) throws JSchException, SftpException, IOException;
    
}
