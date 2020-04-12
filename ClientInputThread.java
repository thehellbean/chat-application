import java.net.*;
import java.io.*;
import java.util.regex.*;

public class ClientInputThread extends Thread {
    // Thread for handling input to the client (i.e from server)
	private Socket socket = null;
	private BufferedReader in;
	private ChatGui clientGui;
	private String waitingToReceive;
	private String waitingToReceiveFile;
	
	public ClientInputThread(Socket socket, ChatGui gui) throws IOException{
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.clientGui = gui;
	}

	public void setWaitingToReceive(String nickname){
		waitingToReceive = nickname;
	}

	public void setWaitingToReceiveFile(String filename){
		waitingToReceiveFile = filename;
	}

	public void run(){
		try{
			while(!socket.isClosed()){
				String message = in.readLine();
				System.out.println("Received message");
				System.out.println(message);
				if (message == null) {
					socket.close();
				} else if(message.length() == 0){
					clientGui.addChatText("");
				} else if('/' == message.charAt(0)){
                    // Message command handling
					if('n' == message.charAt(1)){
                        // Nickname update
						clientGui.updateNicknames(new String(message.substring(11,message.length())));
					} else if('m' == message.charAt(1)){
                        // Targeted message. Precedes every other command as they must be targeted
						Pattern commandPattern = Pattern.compile("/msg:(.+): /(.+)");
                        Matcher commandMatcher = commandPattern.matcher(message);
                        if (commandMatcher.find()) {
                            String sender = commandMatcher.group(1);
                            String command = commandMatcher.group(2);

                            Pattern connectionPattern = Pattern.compile("connection:.+:(\\d+):(.+)");
                            Pattern filePattern = Pattern.compile("file:.+:(.+)");
                            Pattern acceptPattern = Pattern.compile("accept");
                            Pattern declinePattern = Pattern.compile("decline");

                            Matcher fileMatcher = filePattern.matcher(command);
                            Matcher acceptMatcher = acceptPattern.matcher(command);
                            Matcher connectionMatcher = connectionPattern.matcher(command);
                            Matcher declineMatcher = declinePattern.matcher(command);

                            if (acceptMatcher.find()) {
                                // If the message is accepting a file transfer
                                Pattern refinedSearch = Pattern.compile("/msg:(.+): /msg:.+: /accept");
                                Matcher refinedMatcher = refinedSearch.matcher(message);
                                if (refinedMatcher.matches()) {
                                    clientGui.startFileTransfer(refinedMatcher.group(1));
                                }
                            } else if (connectionMatcher.find()) {
                                // If the message is sending connection information for a file transfer
                                System.out.println("Connection found");

                                // Check that we actually wanted to receive a file from said person
                                if (sender.equals(waitingToReceive)) {

                                    // Receives file using lambda function
                                    Runnable r = () -> {
                                        Socket sok = null;
                                        try {
                                            // Connects to given connection information
                                            sok = new Socket(connectionMatcher.group(2), Integer.parseInt(connectionMatcher.group(1)));
                                        } catch (IOException e) {
                                            System.out.println("Couldn't connect to other client");
                                            e.printStackTrace();
                                        }

                                        try {
                                            System.out.println("Starting file download");

                                            // Downloads file into previously given filename
                                            OutputStream fileStream = new FileOutputStream(waitingToReceiveFile);
                                            InputStream in = sok.getInputStream();
                                            byte buffer[] = new byte[8192];
                                            int len = 0;
                                            while ((len = in.read(buffer)) != -1) {
                                                fileStream.write(buffer, 0, len);
                                            }

                                            fileStream.close();
                                            in.close();

                                            System.out.println("Finished downloading file");

                                        } catch (IOException e) {
                                            System.out.println("Failed to receive file");
                                        }
                                    };
                                    Thread thr = new Thread(r);
                                    thr.start();
                                } else {
                                    continue;
                                }
                            } else if (fileMatcher.find()) {
                                // If the message is a file request

                                // Open up a popup window asking if we want to accept or decline the file
                                ChatFileGui guiThread = new ChatFileGui(sender, fileMatcher.group(1), this, clientGui);
                                guiThread.start();
                            } else if (declineMatcher.find()) {
                                // If the message is a decline message. 
                                ////  setFileSend("");
                                clientGui.setFileSend("", "");

                                Pattern refinedSearch = Pattern.compile("/msg:(.+): /msg:.+: /decline");
                                Matcher refinedMatcher = refinedSearch.matcher(message);
                                if (refinedMatcher.matches()) {
                                    clientGui.addChatText(refinedMatcher.group(1) + " declined your file transfer");
                                }
                            } else {
                                // If no other special command it's just a normal private message
    							clientGui.addChatText("(Private message) "+command.substring(4, command.length()));
    						}
                        }
					}
				} else {
                    // Non-private message
					clientGui.addChatText(message);
				}
			}
		}catch (IOException e){
			System.err.println("IOException while reading from port.");
		}
	}
}
