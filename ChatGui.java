import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.net.*;
import java.io.*;
import java.util.regex.*;


public class ChatGui extends JPanel implements ActionListener{
    // GUI class for drawing GUI
    // Also takes input from user and handles sending it over socket

	private static int size = 500;
	private static JTextField entry;
	private static JSplitPane chatLeft;
	private static JSplitPane chatTot;
	private static DefaultListModel<String> nicknames;
	private static JList<String> nicknameList;
	private static JTextArea chatList;
	private static ClientInputThread inThread;
	private String waitingToSendTo;
	private String waitingToSendFile;
	private PrintStream ps;

	public ChatGui(ClientInputThread inThread, Socket socket){
		super(new BorderLayout());
		inThread = inThread;
		try {
			ps = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Left chat split pane
		// Text input field
		entry = new JTextField();
		entry.addActionListener(this);


		chatList = new JTextArea(5,20);
		System.out.println(chatList == null);
		chatList.setEditable(false);
		chatList.setFont(new Font("Helvetica", Font.PLAIN, 16));
		chatList.setLineWrap(true);
		chatList.setWrapStyleWord(true);
		JScrollPane chatScroll = new JScrollPane(chatList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// chatList.add(chatList);

		chatLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chatScroll, entry);
		chatLeft.setDividerLocation(size-75);


		// Add List
		nicknames = new DefaultListModel<String>();
		nicknameList = new JList<>(nicknames);
		nicknameList.setLayoutOrientation(JList.VERTICAL);
		nicknameList.setSelectionModel(new NoSelectionModel());

		chatTot = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatLeft, nicknameList);
		chatTot.setDividerLocation(size-100);
		add(chatTot);
		
	}

	public static void addNickname(String name){
		nicknames.addElement(name);
	}

	public static void updateNicknames(String raw){
		List<String> names = Arrays.asList(raw.split(":"));

		nicknames.removeAllElements();
		Collections.sort(names);
		for (String s : names) {
			if(s.equals("/nicknames"))continue;
			nicknames.addElement(s);
		}
	}

	public static void removeNickname(String name){
		for(int idx = 0; idx < nicknames.size(); idx++){
			if(nicknames.getElementAt(idx).equals(name)){
				nicknames.remove(idx);
			}
		}
	}

	public static void addChatText(String message){
		chatList.append(message + "\n");
		chatList.setCaretPosition(chatList.getDocument().getLength());
	}

	public void actionPerformed(ActionEvent evt) {
        // Event handler, sends messages when enter is pressed
        String text = entry.getText();
        sendMessage(text);
        chatList.append("Sent msg: " + text + "\n");
        entry.setText("");

    }

    public void setFileSend(String sendFile, String sendTo) {
        // Sets file waiting to be sent (waiting while the other person is accepting/declining)
        waitingToSendFile = sendFile;
        waitingToSendTo = sendTo;
    }

    public void setInputThread(ClientInputThread thread) {
    	inThread = thread;
    }

    public void sendMessage(String message){
        // Sends a message to the server
        Pattern file = Pattern.compile("/file:(.+):(.+)");
        Matcher m = file.matcher(message);
        if (m.matches()) {
            // Special handling if the message is a file send request
            // Saves the target and file to keep track
            setFileSend(m.group(2), m.group(1));
        	System.out.println(waitingToSendTo);
            // inThread.setFileSend(waitingToSendTo);
        }
        ps.println(message);
        System.out.println("Sent message: " + message);
    }

    public void startFileTransfer(String target) {
        // Initiates file transfer
    	System.out.println(target);
        if (target.equals(waitingToSendTo)) {
        	System.out.println("sending file");
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(0);
            } catch (IOException e) {
                System.out.println("Couldn't send file");
                e.printStackTrace();
            }
            final ServerSocket newsok = socket;
            int port = socket.getLocalPort();
            Runnable r = () -> {
                try {
                    // Starts socket connection and sends file over it
                    Socket sok = newsok.accept();
                    InputStream fileStream = new FileInputStream(waitingToSendFile);
                    OutputStream out = sok.getOutputStream();
                    byte buffer[] = new byte[8192];
                    int len = 0;
                    while ((len = fileStream.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }

                    fileStream.close();
                    out.close();

                }catch (IOException e) {
                    System.out.println("Failed to send file");
                }
            };
            Thread fileThread = new Thread(r);
            fileThread.start();
            // Sends connection information to receiver
            ps.println("/connection:"+target+":"+Integer.toString(port));
        }
    }
}
