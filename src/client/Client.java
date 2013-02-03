import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Client is used for connection to the server and starting the game
 * After the game is started a Game board is instantiated
 * 
 * @author Dmitri Samoilov
 *
 */
public class Client extends JFrame implements WindowListener,ActionListener {
		
	private static final long serialVersionUID = 1L;
	
	/** Displays status information */
	protected JLabel info;
	/** Continues with connection when pressed */
	protected JButton connectBtn;
	/** Used to send objects to server */
	protected ObjectOutputStream toServer;
	/** Used to receive objects from server */
	protected ObjectInputStream fromServer;
	/** Indicates whether the game has been started */
	protected boolean started;
	/** Game board object */
	protected GameBoard game;
	/** Server IP address */
	protected String serverIp;
	
	public static void main(String[] args) {
		new Client();
	}	
	
	/**
	 * Client constructor class
	 * Creates client window, allows to connect to server
	 */
	public Client() {
		started = false;
		addWindowListener(this);
		serverIp = JOptionPane.showInputDialog("Please enter server IP address. For localhost press Cancel");
		if (serverIp == null || serverIp == "") {
			serverIp = "127.0.0.1";
		}
		setSize(250, 200);
		setTitle("Battleships Client");
		JPanel panel = new JPanel();
		info = new JLabel("Press connect to start the game.");
		panel.add(info);
		connectBtn = new JButton("Connect");
		connectBtn.addActionListener(this);
		panel.add(connectBtn);
		add(panel);
		setVisible(true);	
	}
	/**
	 * Connects to server, changes interface for user to start the game when connected
	 */
	protected void connect() {
		Socket client = null;
		try {
			info.setText("Connecting to server...");
			client = new Socket(serverIp, 3319);
			toServer = new ObjectOutputStream(client.getOutputStream());
			fromServer = new ObjectInputStream(client.getInputStream());
			String input = (String)fromServer.readObject();
			if (input.equals("connect")) {
				info.setText("<html>Connected to server!<br>Waiting for Opponent to join...</html>");
				repaint();	
				input = (String)fromServer.readObject();
				if (input.equals("player2")) {
					info.setText("<html>Connected to server!<br>Opponent joined!<br><br>Hit Play to begin</html>");
					connectBtn.setText("Play!");
					connectBtn.setEnabled(true);
				}
			}
		} catch (Exception e) {
			System.out.println("Could not connect to server!");
			System.exit(0);
		}
	}
	
	
	@Override
	/**
	 * When "connect" button is clicked, client connects to server.
	 * When "play" button is clicked, client initialises game board.
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (connectBtn.getText().equals("Connect")) {
			connectBtn.setEnabled(false);
			connect();
		}
		else {
			started = true;
			try {
				toServer.writeObject("play");
				toServer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setVisible(false);
			int[][] myField = null,field = null;
			try {
				myField = (int[][])fromServer.readObject();
				field = (int[][])fromServer.readObject();
			} catch (Exception e) {
				System.out.println("Could not receive data from server!");
				System.exit(0);
			}
			
			game = new GameBoard(toServer,fromServer,myField,field);
			Thread t = new Thread(game);
			t.start();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {	
	}

	@Override
	/**
	 * When window is closed by user, a message is passed to the server
	 */
	public void windowClosing(WindowEvent arg0) {
		if (!started && toServer != null) {
			try {
				toServer.writeObject("close");
				toServer.flush();
			} catch (IOException e) {
				System.out.println("Could not send message to server!");
			}
			System.exit(0);
		}		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {	
	}	
}
