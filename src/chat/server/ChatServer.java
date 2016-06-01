/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import chat.ChatFrame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Igor Gayvan
 */
public class ChatServer implements Runnable {

    private static final int SERVER_TIMEOUT = 1000;
    public static String message;
    //public static String message;
    public static DataOutputStream dos;
    private int port;
    private ServerSocket serverSocket;
    private boolean listen;

    public ChatServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setSoTimeout(SERVER_TIMEOUT);
    }

    @Override
    public void run() {
        listen = true;

        System.out.println("SERVER LISTEN ON " + port);
        while (listen) {
            try (Socket accept = serverSocket.accept()) {
                // CONFIG SOCKET
                accept.setSoLinger(true, SERVER_TIMEOUT / 2); // Waiting before close connection
                accept.setSoTimeout(SERVER_TIMEOUT); // Waiting before throws SocketTimeoutException when read data.

                // Print connected ip address.
                String clientAddress = accept.getInetAddress().getHostAddress();
                System.out.println("CONNECTED: " + clientAddress);

                // GET IO
                DataInputStream dis = new DataInputStream(accept.getInputStream());
                dos = new DataOutputStream(accept.getOutputStream());

                commandHandler(clientAddress, dis, dos, accept.getPort());

            } catch (SocketTimeoutException ex) {
                // NOOP
            } catch (IOException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Delegate command.
     *
     * @param dis input stream.
     * @param dos output stream.
     * @throws IOException
     */
    public void commandHandler(String address, DataInputStream dis, DataOutputStream dos, int clientPort) throws IOException {
        // WORKING WITH SOCKET

        String command = dis.readUTF();
        System.out.println("COMMAND: " + command);

        switch (command) {
            case "PING":
                pingHandler(dos);
                break;
            case "MSG":
                messageHandler(address, dis, dos, clientPort);
                break;
            default:
                defaultHandler(dos);
                break;
        }
    }

    public void pingHandler(DataOutputStream dos) throws IOException {
        dos.writeUTF("PONG");

        dos.flush();

    }

    public void messageHandler(String sender, DataInputStream dis, DataOutputStream dos, int clientPort) throws IOException {
        String recipient = dis.readUTF();
        String message = dis.readUTF();
        
        System.out.printf("  Address: %s\n  Message: %s\n", recipient, message);

        boolean success = true;

//        try (Socket client = new Socket(recipient, 5782)) {
//            DataOutputStream client_dos = new DataOutputStream(client.getOutputStream());
//            DataInputStream client_dis = new DataInputStream(client.getInputStream());
//
//            client_dos.writeUTF("MSG");
//            client_dos.flush();
//            client_dos.writeUTF(sender);
//            client_dos.flush();
//            client_dos.writeUTF(message);
//            client_dos.flush();
//
//            String response = client_dis.readUTF();
//
//            if ("OK".equals(response)) {
//                success = true;
//            }
//        }
        dos.writeUTF(success ? "OK" : "ERROR:While sending message.");
        
        dos.flush();
        
    }

    public void defaultHandler(DataOutputStream dos) throws IOException {
        dos.writeUTF("ERROR:Unknown command!");
        dos.flush();
    }

    /**
     * Set {@link #listen} to false.
     */
    public void stop() {
        listen = false;
    }

}
