import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class ChatClient {
    // Main class for handling the chat client
    private static int connectionPort = 8989;
    private static Socket socket = null;
    private static int guiSize = 500;
    private static ClientInputThread inThread;
    private static ChatGui clientGui;
    private PrintStream ps;
    private String waitingToSendFile;
    private String waitingToSendTo;

    public static void login(){
        // Login prompt, asking for a username
        JFrame parent = new JFrame();
        ClientLogin loginPrompter = new ClientLogin(parent, socket);
        loginPrompter.setVisible(true);
        while(!loginPrompter.loggedIn);
    }

    public static void initGUI(){
        login();
        JFrame frame = new JFrame("Simple Chat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(guiSize,guiSize));
        frame.setLayout(new BorderLayout());
        
        clientGui = new ChatGui(inThread, socket);
        frame.add(clientGui);
        frame.pack();
        frame.setVisible(true);  

        clientGui.addChatText("Welcome"); 
        clientGui.addChatText("To send a message to \"nickname\" write \"/msg:nickname:\" before your message");  
    }

    public static void main(String[] args) throws IOException {
        String adress = "";
        BufferedReader in = null;
        PrintStream ps = null;

        try {
            adress = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Missing argument IP-address");
            System.exit(1);
        }

        // Connects to chat server at given address
        try {
            socket = new Socket(adress, connectionPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " +adress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't open connection to " + adress);
            System.exit(1);
        }


        // Starts GUI and input thread
        // Runs separately from GUI because of the way Swing works
        initGUI();
        inThread = new ClientInputThread(socket, clientGui);
        clientGui.setInputThread(inThread);
        inThread.run();
    }
}
