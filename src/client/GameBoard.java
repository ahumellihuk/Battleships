import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Game Board is created when a game is launched and data is passed from the server
 * It creates Graphical User Interface and displays visual data for players
 * 
 * @author Dmitri Samoilov
 *
 */
public class GameBoard extends JFrame implements WindowListener,MouseListener,Runnable{

	private static final long serialVersionUID = 1L;
	
	protected static final int HIT = 1;
	protected static final int MISS = 0;
	protected static final int CONTINUE = 1;
	protected static final int WAIT = 0;
	protected static final int WIN = 3;
	protected static final int LOSE = 4;	
	protected static final int SHUTDOWN = -2;

	private static final int EXIT = -3;

	/** Used to send objects to server */
	protected ObjectOutputStream toServer;
	/** Used to receive objects from server */
	protected ObjectInputStream fromServer;
	/** Stores game field data - whether there is a ship on a particular tile or not */
	protected int[][] field, myField;
	
	/** Enemy ship data */
	protected int[] ships = {1,1,1,1,2,2,2,3,3,4};
	/** Player's ship data */
	protected int[] myShips = {1,1,1,1,2,2,2,3,3,4};
	/** Shows whether the game is running or not */
	protected boolean continuePlaying;
	/** Shows whether the player can make a move */
	protected boolean listenersActive;
	
	private JPanel grid, myGrid, panel, upperPanel, bottomPanel, myShipsDetails,shipsDetails;
	private JLabel[] myShipIcons, shipIcons;	
	private JLabel[][] icons,myIcons;
	
	public GameBoard(ObjectOutputStream toServer, ObjectInputStream fromServer, int[][] myField, int[][] field) {
		this.toServer = toServer;
		this.fromServer = fromServer;
		this.myField = myField;
		this.field = field;
	}
	
	/**
	 * When a Thread is started, calls methods to create gui and start game
	 */
	public void run() {
		gui();
		play();		
	}
	/**
	 * Game loop
	 * Receives input from server and does appropriate action
	 */
	protected void play() {
		continuePlaying = true;
		
		while (continuePlaying) {	
			
			Integer[][] input = new Integer[3][10];
			
			try {
				input = (Integer[][]) fromServer.readObject();
				}
			catch (Exception e) {
				System.out.println("Could not get input from server! The game will exit.");
				continuePlaying = false;
				System.exit(0);
			}
			
			if (input != null) {
				
				if (input[0][2] == SHUTDOWN) {
					continuePlaying = false;
					JOptionPane.showMessageDialog(null, "The server has been disconnected. The game will not exit.");
					System.exit(0);
				}
				else if (input[0][2] == EXIT) {
					continuePlaying = false;
					JOptionPane.showMessageDialog(null, "The opponent has left the game. The game will not exit.");
					System.exit(0);
				}
				
				for (int i = 0; i<10; i++) {
					myShips[i] = input[1][i];
					ships[i] = input[2][i];
				}
				shipDetails(ships, myShips);
				repaint();
				
				if (input[0][2] == CONTINUE) {
					listenersActive = true;	
					setTitle("Sea Battle - Your Turn!");
				}
				else if (input[0][2] == WAIT) {
					if (input[0][0] != -1) {
						if (myField[input[0][0]][input[0][1]] != -1) {
							if (myField[input[0][0]][input[0][1]] == 0)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-north.png"));
							else if (myField[input[0][0]][input[0][1]] == 1)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-east.png"));
							else if (myField[input[0][0]][input[0][1]] == 2)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-south.png"));
							else if (myField[input[0][0]][input[0][1]] == 3)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-west.png"));
							else if (myField[input[0][0]][input[0][1]] == 5)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-horizontal.png"));
							else if (myField[input[0][0]][input[0][1]] == 4)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-vertical.png"));
							else if (myField[input[0][0]][input[0][1]] == 6)
								myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/aim-ship.png"));
						}
						else myIcons[input[0][0]][input[0][1]].setIcon(new ImageIcon("img/empty.png"));	
						repaint();
					}
				}	
				else if (input[0][2] == WIN) {
					continuePlaying = false;
					JOptionPane.showMessageDialog(null, "You have won! Congratulations!");
				}
				else if (input[0][2] == LOSE) {
					continuePlaying = false;
					JOptionPane.showMessageDialog(null, "Sorry, You have lost.");
				}				
			}
						
		}
		System.exit(0);
	}
	
	/**
	 * Initialises graphical user interface
	 */
	protected void gui() {
		setTitle("Sea Battle - Enemy's Turn!");
		setSize(575, 410);
		addWindowListener(this);
		setResizable(false);
		
		panel = new JPanel(new BorderLayout());
		upperPanel = new JPanel(new BorderLayout());
		bottomPanel = new JPanel(new GridLayout(1,2));		
		
		shipsDetails = new JPanel(new GridLayout(4,4));
		myShipsDetails = new JPanel(new GridLayout(4,4));
		
		shipIcons = new JLabel[10];
		myShipIcons = new JLabel[10];
		
		for (int i=0; i<4; i++) {
			shipIcons[i] = new JLabel(new ImageIcon("img/1cell-1.png"));
			shipsDetails.add(shipIcons[i]);
			myShipIcons[i] = new JLabel(new ImageIcon("img/1cell-1.png"));
			myShipsDetails.add(myShipIcons[i]);
		}
		shipsDetails.add(new JLabel());
		for (int i=4; i<7; i++) {
			shipIcons[i] = new JLabel(new ImageIcon("img/2cell-2.png"));
			shipsDetails.add(shipIcons[i]);
			myShipIcons[i] = new JLabel(new ImageIcon("img/2cell-2.png"));
			myShipsDetails.add(myShipIcons[i]);
		}
		myShipsDetails.add(new JLabel());		
		shipsDetails.add(new JLabel());
		shipsDetails.add(new JLabel());
		for (int i=7; i<9; i++) {
			shipIcons[i] = new JLabel(new ImageIcon("img/3cell-3.png"));
			shipsDetails.add(shipIcons[i]);
			myShipIcons[i] = new JLabel(new ImageIcon("img/3cell-3.png"));
			myShipsDetails.add(myShipIcons[i]);
		}
		myShipsDetails.add(new JLabel());
		myShipsDetails.add(new JLabel());
		shipsDetails.add(new JLabel());
		shipsDetails.add(new JLabel());
		shipsDetails.add(new JLabel());
		shipIcons[9] = new JLabel(new ImageIcon("img/4cell-4.png"));
		shipsDetails.add(shipIcons[9]);
		myShipIcons[9] = new JLabel(new ImageIcon("img/4cell-4.png"));
		myShipsDetails.add(myShipIcons[9]);
		myShipsDetails.add(new JLabel());
		myShipsDetails.add(new JLabel());
		myShipsDetails.add(new JLabel());
		
		
		upperPanel.add(myShipsDetails, BorderLayout.WEST);	
		upperPanel.add(shipsDetails, BorderLayout.EAST);
		
		myGrid = new JPanel(new GridLayout(11,11));
		bottomPanel.add(myGrid);
		grid = new JPanel(new GridLayout(11,11));
		bottomPanel.add(grid);
		
		icons = new JLabel[11][11];
		myIcons = new JLabel[11][11];		
		
		//Initialise graphical game field
		for (int x=0; x<11; x++) {
			for (int y=0; y<11; y++) {	
				String letters = "ABCDEFGHIJ";
				if (x==0 && y==0) {
					icons[x][y] = new JLabel();
					myIcons[x][y] = new JLabel();
					grid.add(icons[x][y]);
					myGrid.add(myIcons[x][y]);
				}
				else if (x==0 && y!=0) {
					String letter = letters.charAt(y-1) + "";
					icons[x][y] = new JLabel(letter, JLabel.CENTER);
					myIcons[x][y] = new JLabel(letter, JLabel.CENTER);
					grid.add(icons[x][y]);
					myGrid.add(myIcons[x][y]);
				}
				else if (y==0 && x!=0) {
					icons[x][y] = new JLabel(x+"", JLabel.CENTER);
					myIcons[x][y] = new JLabel(x+"", JLabel.CENTER);
					grid.add(icons[x][y]);
					myGrid.add(myIcons[x][y]);
				}
				else {				
					icons[x][y] = new JLabel(new ImageIcon("img/tile.png"), JLabel.CENTER);
					icons[x][y].addMouseListener(this);
					grid.add(icons[x][y]);
					
					if (myField[x][y] == 0)
						myIcons[x][y] = new JLabel(new ImageIcon("img/north.png"), JLabel.CENTER);
					else if (myField[x][y] == 1)
						myIcons[x][y] = new JLabel(new ImageIcon("img/east.png"), JLabel.CENTER);
					else if (myField[x][y] == 2)
						myIcons[x][y] = new JLabel(new ImageIcon("img/south.png"), JLabel.CENTER);
					else if (myField[x][y] == 3)
						myIcons[x][y] = new JLabel(new ImageIcon("img/west.png"), JLabel.CENTER);
					else if (myField[x][y] == 5)
						myIcons[x][y] = new JLabel(new ImageIcon("img/horizontal.png"), JLabel.CENTER);
					else if (myField[x][y] == 4)
						myIcons[x][y] = new JLabel(new ImageIcon("img/vertical.png"), JLabel.CENTER);
					else if (myField[x][y] == 6)
						myIcons[x][y] = new JLabel(new ImageIcon("img/ship.png"), JLabel.CENTER);
					else myIcons[x][y] = new JLabel(new ImageIcon("img/tile.png"), JLabel.CENTER);
					myGrid.add(myIcons[x][y]);
				}
			}			
		}
		panel.add(upperPanel, BorderLayout.NORTH);
		panel.add(bottomPanel, BorderLayout.SOUTH);	
		add(panel);		
		setVisible(true);
		repaint();
	}
	
	/**	Manages ship detail icons
	 * 
	 * @param ship Array of opponent's ships
	 * @param myShip Array of player's ships
	 */
	protected void shipDetails(int [] ship, int [] myShip) {
		for (int i=0; i<4; i++) {
			shipIcons[i].setIcon(new ImageIcon("img/1cell-"+ship[i]+".png"));
			myShipIcons[i].setIcon(new ImageIcon("img/1cell-"+myShip[i]+".png"));
		}
		for (int i=4; i<7; i++) {
			shipIcons[i].setIcon(new ImageIcon("img/2cell-"+ship[i]+".png"));
			myShipIcons[i].setIcon(new ImageIcon("img/2cell-"+myShip[i]+".png"));
		}
		for (int i=7; i<9; i++) {
			shipIcons[i].setIcon(new ImageIcon("img/3cell-"+ship[i]+".png"));
			myShipIcons[i].setIcon(new ImageIcon("img/3cell-"+myShip[i]+".png"));
		}
		shipIcons[9].setIcon(new ImageIcon("img/4cell-"+ship[9]+".png"));
		myShipIcons[9].setIcon(new ImageIcon("img/4cell-"+myShip[9]+".png"));
	}
	
	/**
	 * Detects mouse click on a game field, does appropriate action
	 */
	public void mouseClicked(MouseEvent e) {		
		if (listenersActive) {
			for (int x=0; x<11; x++) {
				for (int y=0; y<11; y++) {
					if (e.getSource() == icons[x][y]) {
						int[] turn = new int[3];
						turn[0] = x-1;
						turn[1] = y-1;
						if (field[x][y] != -1) {
							if (field[x][y] == 0)
								icons[x][y].setIcon(new ImageIcon("img/aim-north.png"));
							else if (field[x][y] == 1)
								icons[x][y].setIcon(new ImageIcon("img/aim-east.png"));
							else if (field[x][y] == 2)
								icons[x][y].setIcon(new ImageIcon("img/aim-south.png"));
							else if (field[x][y] == 3)
								icons[x][y].setIcon(new ImageIcon("img/aim-west.png"));
							else if (field[x][y] == 5)
								icons[x][y].setIcon(new ImageIcon("img/aim-horizontal.png"));
							else if (field[x][y] == 4)
								icons[x][y].setIcon(new ImageIcon("img/aim-vertical.png"));
							else if (field[x][y] == 6)
								icons[x][y].setIcon(new ImageIcon("img/aim-ship.png"));
							icons[x][y].removeMouseListener(this);
							turn[2] = HIT;
						}
						else {
							icons[x][y].setIcon(new ImageIcon("img/empty.png"));
							icons[x][y].removeMouseListener(this);
							turn[2] = MISS;
							listenersActive = false;
							setTitle("Sea Battle - Enemy's turn!");
						}
						try {
							toServer.writeObject(turn);
							toServer.flush();
						} catch (IOException e1) {
							System.out.println("Could not send turn results to server!");
						}
					}
				}			
			}	
		}
	}

	/**
	 * When mouse enters a game tile, display aim scope
	 */
	public void mouseEntered(MouseEvent arg0) {
		JLabel tile = (JLabel) arg0.getSource();		
		tile.setIcon(new ImageIcon("img/aim.png"));		
	}

	/**
	 * When mouse exits game tile, display plain tile
	 */
	public void mouseExited(MouseEvent arg0) {
		JLabel tile = (JLabel) arg0.getSource();		
		tile.setIcon(new ImageIcon("img/tile.png"));			
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {	
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {	
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		int[] output = new int[3];
		output[2] = EXIT;
		try {
			toServer.writeObject(output);
			toServer.flush();
		} catch (IOException e1) {
			System.out.println("Could not inform server of exit");
		}
		System.exit(0);		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


}
