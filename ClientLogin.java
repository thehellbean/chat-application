import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ClientLogin extends JDialog{
	// Login prompt

	private JTextField usernameTF;
	private JLabel userLabel;
	public boolean loggedIn = false;

	public ClientLogin(Frame parent, Socket sckt){
		super(parent, "Login", true);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		userLabel = new JLabel("Enter username: ");

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		panel.add(userLabel, constraints);

		usernameTF = new JTextField(20);
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 2;

		

		usernameTF.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
					PrintStream out = new PrintStream(sckt.getOutputStream());
					String username = usernameTF.getText().trim();
					System.out.println(username);
					boolean proper = true;

					// Some basic checking for illegal characters
					for (char c: username.toCharArray()) {
						if(c == ':' || c == '\n' || c == '\r')proper = false;
					}
					String answer = "";
					if(proper){
						// Sends username to server and checks if it's accepted
						out.println(username);
						answer = in.readLine();
					}
					if(username.equals(answer) && proper){
						// Send the user on its way if it's accepted
						JOptionPane.showMessageDialog(ClientLogin.this, "Your nickname is now "+ username + ".", "Login", JOptionPane.INFORMATION_MESSAGE);
						loggedIn = true;
						dispose();
					}else{
						// Make the user pick again if it's not accepted
						JOptionPane.showMessageDialog(ClientLogin.this, "Username invalid, try another usename. ", "Login", JOptionPane.ERROR_MESSAGE);
						usernameTF.setText("");
						loggedIn = false;
					}
				}catch (IOException a){
					a.printStackTrace();
				}
			}

		});


		panel.add(usernameTF, constraints);
		add(panel);


		this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                System.exit(0);
            }
        });

		this.setSize(new Dimension(400,75));
		setResizable(false);
		setLocationRelativeTo(parent);

	}
}