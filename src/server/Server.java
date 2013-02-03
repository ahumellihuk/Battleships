import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Server class is used by clients to connect to.
 * It creates game session for each pair of clients connected.
 * Displays server log window.
 * 
 * @author Dmitri Samoilov
 *
 */
public class Server extends JFrame implements WindowListener{
	
	private static final long serialVersionUID = 1L;
	protected static final int SHUTDOWN = -2;
	/** Enables network communication */
	protected ServerSocket server;
	/** Player 1 connection */
	protected Socket player1;
	/** Player 2 connection */
	protected Socket player2;
	/** Game Session */
	protected GameSession game;
	/** Enable object input from players */
	protected ObjectInputStream fromPlayer1,fromPlayer2;
	/** Enable object output to players */
	protected ObjectOutputStream toPlayer1,toPlayer2;
	
	public static void main(String[] args) {
		new Server();
	}
	/**
	 * Constructor creates server window, server socket and waits for players to connect.
	 * When 2 players are ready to play, it starts the game session.
	 */
	public Server() {
		
		//Creating window
		addWindowListener(this);
		JTextArea serverLog = new JTextArea();		
		JScrollPane scrollPane = new JScrollPane(serverLog);		
		add(scrollPane, BorderLayout.CENTER);		
		setSize(300,300);
		setTitle("Battleships Server");
		setVisible(true);
		
		try {
			//Server Socket on port 3319
			server = new ServerSocket(3319);
			
			serverLog.append("Server started at port 3319.\n");
			serverLog.append("SERVER IP ADDRESS : "+InetAddress.getLocalHost()+"\n");
			
			int noOfPlayers = 0;
			while (true) {
				//Wait for player 1 to connect
				if (noOfPlayers == 0) {
					player1 = server.accept();
					toPlayer1 = new ObjectOutputStream(player1.getOutputStream());		
					fromPlayer1 = new ObjectInputStream(player1.getInputStream());
					sendMessage("connect", toPlayer1);	
					
					serverLog.append("Player 1 has joined. Waiting for Player 2...\n");
					noOfPlayers++;
				}
				//Wait for player 2 to connect
				else if (noOfPlayers == 1) {
					player2 = server.accept();
					toPlayer2 = new ObjectOutputStream(player2.getOutputStream());
					fromPlayer2 = new ObjectInputStream(player2.getInputStream());
					sendMessage("player2", toPlayer1);
					sendMessage("connect", toPlayer2);
					sendMessage("player2", toPlayer2);
					serverLog.append("Player 2 has joined. Starting the game session...\n");
					noOfPlayers++;
				}
				//When 2 players are connected, start the game
				else if (noOfPlayers == 2) {
					String input1 = (String)fromPlayer1.readObject();
					String input2 = (String)fromPlayer2.readObject();
					if (input1.equals("play") && input2.equals("play")) {
						(new GameSession(toPlayer1,toPlayer2,fromPlayer1,fromPlayer2)).start();
						serverLog.append("Game session has been launched!\n");
						noOfPlayers = 0;
					}
					//If players disconnect, abort game launch
					else {
						serverLog.append("Game sesssion launch aborted.\n");
						sendMessage("disconnect", toPlayer1);
						sendMessage("disconnect", toPlayer2);
						noOfPlayers = 0;
					}
				}
			}			
		} catch (IOException e) {
			serverLog.append("Server has ran into an exception! Stopping server...\n");
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void sendMessage(String message, ObjectOutputStream player) {
		try {
			player.writeObject(message);
			player.flush();
		} catch (IOException e) {
			System.out.println("Error sending message to player!");
		}
		
	}
	@Override
	public void windowActivated(WindowEvent arg0) {		
	}
	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	@Override
	public void windowClosing(WindowEvent arg0) {
		if (toPlayer1 != null && toPlayer2 != null) {
			Integer [][] output = new Integer[1][3];
			output[0][2] = SHUTDOWN;
			try {
				toPlayer1.writeObject(output);
				toPlayer1.flush();
				toPlayer2.writeObject(output);
				toPlayer2.flush();
			} catch (IOException e) {
				System.out.println("Could not send shutdown command to clients");
			}
		}
		System.exit(0);		
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
