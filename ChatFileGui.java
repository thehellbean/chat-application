import javax.swing.*;

public class ChatFileGui extends Thread{
	// Pop up for accepting/declining file transfers

	private ClientInputThread in;
	private ChatGui out;
	private String sender;
	private String filename;

	public ChatFileGui(String sender, String filename){
		this.sender = sender;
		this.filename = filename;
	}

	public ChatFileGui(String sender, String filename, ClientInputThread in, ChatGui out){
		this.sender = sender;
		this.filename = filename;
		this.in = in;
		this.out = out;
	}

	private void filePopup(){
		JFrame frame = new JFrame();
		JOptionPane buttons = new JOptionPane("Would you like to accept the file \"" + filename + "\" from: \"" + sender + "\" ?");
		Object[] options = new String[]{"Yes, send me file", "No, don't send me file"};
		buttons.setOptions(options);
		JDialog acceptFile = buttons.createDialog(frame, "File transfer dialog");

		acceptFile.setVisible(true);
		Object answer = buttons.getValue();
		if(answer != null && answer.equals(options[0]))accept();
		else decline();
		frame.dispose();
	}

	private void accept(){
		// If file is accepted send an accept message to the sender 
		in.setWaitingToReceive(sender);
		in.setWaitingToReceiveFile(filename);
		out.sendMessage("/msg:" + sender + ": /accept");
	}
	private void decline(){
		// Send decline message if declined
		out.sendMessage("/msg:" + sender + ": /decline");
	}

	public void run(){
		this.filePopup();
	}
}
