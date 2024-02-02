package GrooveClient.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import GrooveClient.exceptions.ErrorClosingConnection;
import GrooveClient.exceptions.ErrorReceivingMessage;
import GrooveClient.exceptions.ErrorSendingMessage;
import GrooveClient.exceptions.UnableToConnectException;

public class TCPconnection {

    private static final String TCP = "TCP";

    private String serverIp;
    private int serverPort;

    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;

    /**
     * Simple constructor (no logic).
     * 
     * @param ip   the application server IP address
     * @param port the port number of the proccess in the app server
     */
    public TCPconnection(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    /**
     * Creates the socket and establishes the connection to the ip:port given for
     * this connection.
     * 
     * @throws UnableToConnectException
     */
    public void establishTCPSession() throws UnableToConnectException {
        try {
            socket = new Socket(this.serverIp, this.serverPort);
            outStream = new DataOutputStream(socket.getOutputStream());
            inStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new UnableToConnectException(this.serverIp, this.serverPort, TCP);
        }
    }

    /**
     * Closes the socket connection to the server.
     * 
     * @throws ErrorClosingConnection
     */
    public void terminateTCPSession() throws ErrorClosingConnection {
        try {
            outStream.flush(); // devo?????
            outStream.close();
            socket.close();
        } catch (IOException e) {
            throw new ErrorClosingConnection(this.serverIp, this.serverPort, TCP);
        }
    }

    /**
     * Sends a request to a server.
     * 
     * @param message
     * @throws ErrorSendingMessage
     */
    public void sendTCPMessage(String message) throws ErrorSendingMessage {
        try {
            outStream.writeUTF(message);
        } catch (IOException e) {
            throw new ErrorSendingMessage(this.serverIp, this.serverPort, TCP);
        }
    }

    /**
     * Receive a message from the server connected by TCP.
     * 
     * @return the server reply
     * @throws ErrorReceivingMessage
     */
    public String receiveTCPMessage() throws ErrorReceivingMessage {
        try {
            byte[] reply = inStream.readAllBytes();
            return new String(reply, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorReceivingMessage(this.serverIp, this.serverPort, TCP);
        }
    }

}
