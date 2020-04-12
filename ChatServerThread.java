import java.net.*;
import java.io.*;
import java.util.regex.*;
import java.util.ArrayList;

public class ChatServerThread extends Thread {
    // Thread class for handling input/output to client
    private Socket socket = null;
    private BufferedReader reader;
    private PrintStream ps;
    private String username;
    private ArrayList<ChatServerThread> chatThreads;

    public ChatServerThread(Socket sok, ArrayList<ChatServerThread> chatThreads) throws IOException {
        socket = sok;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ps = new PrintStream(socket.getOutputStream());
        this.chatThreads = chatThreads;

        username = reader.readLine();
        while(usernameExists(username)){
            ps.println("");
            username = reader.readLine();
        }
        ps.println(username);
    }

    public void exit() {
        chatThreads.remove(this);
        sendNicknames();
    }

    public String getUsername() {
        return username;
    }

    public boolean usernameExists(String username){
        // Looks at rest of threads and sees if a client with the username is connected
        for(ChatServerThread thread : chatThreads){
            if(thread.getUsername().equals(username))return true;
        }
        return false;
    }

    public void sendNicknames(){
        // Updates the list of nicknames on all clients
        StringBuilder b = new StringBuilder();
        b.append("/nicknames");
        for (ChatServerThread thread : chatThreads) {
            b.append(":");
            b.append(thread.getUsername());
        }
        
        for (ChatServerThread thread : chatThreads) {
            thread.sendMessage(b.toString());
        }
    }

    public boolean sendTo(String message, String nickname){
        // Finds a client with a given nickname and sends the message to them (and only them)
        // Returns true if said client is found and false if no one with that name is on the server
        for (ChatServerThread thread : chatThreads) {
            if(nickname.equals(thread.getUsername())){
                thread.sendMessage("/msg:"+username + ": " + message);
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String message) {
        // Sends message to connected client
        ps.println(message);
    }

    public void run() {
        sendNicknames();
        while (!socket.isClosed()) {
            System.out.println("Trying to get a message");
            String message;
            try {
                message = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            // System.out.println(message);

            if (message == null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    continue;
                }
                break;
            } else if('/' == message.charAt(0)){
                // Special command handling
                System.out.println(message);
                if("/msg:".equals(message.substring(0,5))){
                    // /msg: indicates a private message
                    StringBuilder b = new StringBuilder();
                    String msg = "";
                    // Find the nickname of the person being messaged
                    for(int i = 5; i < message.length(); i++){
                        if(':' == message.charAt(i)){
                            msg = message.substring(i + 1, message.length());
                            break;
                        }
                        b.append(message.charAt(i));
                    }
                    msg = msg.trim();
                    // Try to send message and continue if it succeeded.
                    // Otherwise, don't continue which hits the for loop at the end (i.e sends to every connected client)
                    if(sendTo(message, b.toString()))continue;
                } else if ("/file:".equals(message.substring(0,6))) {
                    // File command for sending files
                    Pattern filePattern = Pattern.compile("file:(.+):");
                    Matcher m = filePattern.matcher(message);
                    // Find target client with regex and send them the file request
                    if (m.find()) {
                        String target = m.group(1);
                        sendTo(message, target);
                    }
                    continue;
                } else if ("/connection:".equals(message.substring(0, 12))) {
                    // Connection command for sending connection information to a client for file transfer
                    Pattern connectionPattern = Pattern.compile("connection:(.+):\\d+");
                    Matcher m = connectionPattern.matcher(message);
                    if (m.find()) {
                        // Extract port from client message and append IP-message, then send to target
                        String ip = socket.getInetAddress().getHostAddress();
                        sendTo(message+":"+ip, m.group(1));
                    }
                    continue;
                }
            }
            
            for (ChatServerThread i : chatThreads) {
                // Send message to every connected client (default if no command is specified)
                i.sendMessage((username + ": " + message));
            }
        }
        exit();
    }
}
