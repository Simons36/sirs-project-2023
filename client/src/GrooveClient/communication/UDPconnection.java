package GrooveClient.communication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import GrooveClient.exceptions.ErrorReceivingMessage;
import GrooveClient.exceptions.UnableToConnectException;

public class UDPconnection {
    
    public static final int UDP_PORT = 5768;
    public static final int PACKET_LENGHT = 1500;

    private String serverIp;
    private int serverPort;

    private DatagramSocket socket;

    /**
     * Creates a UDP socket bound to port {UDP_PORT} for song streaming.
     * 
     * @param ip
     * @param port
     * @throws SocketException
     */
    public UDPconnection(String ip, int port) throws SocketException {
        this.serverIp = ip;
        this.serverPort = port;
        try {
            socket = new DatagramSocket(UDP_PORT);
        } catch (SocketException e) {
            throw e;
        }
    }

    /**
     * Receives a UDP packet with lenght {PACKET_LENGHT=1500} from the app server.
     * 
     * @return
     * @throws ErrorReceivingMessage
     */
    public byte[] receiveUDPMessage() throws ErrorReceivingMessage {
        try {
            byte[] receive = new byte[PACKET_LENGHT];
            DatagramPacket dpReceive = new DatagramPacket(receive, PACKET_LENGHT);
            socket.receive(dpReceive);
            return receive;
        } catch (Exception e) {
            throw new ErrorReceivingMessage(serverIp, serverPort, "UDP");
        }
    }

}
