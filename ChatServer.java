import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ChatServer {
    // Main server class that listens for new client connections and starts threads

    private static int connectionPort = 8989;

    // ArrayList of all threads so that they can send messages between each other
    private static ArrayList<ChatServerThread> outputThreads;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        boolean listening = true;
        outputThreads = new ArrayList<ChatServerThread>();

        try {
            serverSocket = new ServerSocket(connectionPort);
        } catch (IOException e) {
            System.err.println("Could not listen on port " + connectionPort);
            System.exit(1);
        }

        while (listening) {
            try {
                Socket sok = serverSocket.accept();
                System.out.println("Connecting...");
                ChatServerThread thr = new ChatServerThread(sok, outputThreads);
                outputThreads.add(thr);
                thr.start();
                System.out.println("Opened connection");
            } catch(IOException e) {
                e.printStackTrace();
                listening = false;
            }
        }
    }
}
