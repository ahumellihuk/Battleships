import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Game session is created for each pair of clients connected.
 * It controls the game flow, players'turns and manipulates game data
 * 
 * @author Dmitri Samoilov
 *
 */
public class GameSession extends Thread{
	
	protected static final int MISS = 0;
	protected static final int HIT = 1;
	protected static final int WAIT = 0;
	protected static final int CONTINUE = 1;
	protected static final int NONE = -2;
	protected static final int WIN = 3;
	protected static final int LOSE = 4;
	protected static final int UP = 0;
	protected static final int DOWN = 1;
	protected static final int LEFT = 2;
	protected static final int RIGHT = 3;
	private static final int EXIT = -3;
	
	/** Enable object output to players */
	protected ObjectOutputStream objectToPlayer1, objectToPlayer2;
	/** Enable object input from players */
	protected ObjectInputStream fromPlayer1, fromPlayer2;
	/** Store game board tile data */
	protected Tile[][] field1 = new Tile[10][10], field2 = new Tile[10][10];
	/** Store ship details */
	protected Ship [] ships1 = new Ship[10], ships2 = new Ship[10];
	/** Indicates whether the game is running */
	protected boolean gameIsRunning;	
	/** 
	 * GameSession constructor
	 * Gets input and output streams from Server class
	 * 
	 * @param objectToPlayer1 Output to player 1
	 * @param objectToPlayer2 Output to player 2
	 * @param fromPlayer1 Input from player 1
	 * @param fromPlayer2 Input from player 2
	 */
	public GameSession(ObjectOutputStream objectToPlayer1, ObjectOutputStream objectToPlayer2, ObjectInputStream fromPlayer1, ObjectInputStream fromPlayer2) {
		this.objectToPlayer1 = objectToPlayer1;
		this.objectToPlayer2 = objectToPlayer2;
		this.fromPlayer1 = fromPlayer1;
		this.fromPlayer2 = fromPlayer2;
	}
	/**
	 * When a Thread is run initialises ships, game board, sends initial data to players
	 * Runs a game loop
	 */
	public void run() {
		
		//Initialising game fields
		initShips();
		initField(field1, ships1);
		initField(field2, ships2);
		
		//Sending game fields to players
		sendField(objectToPlayer1, field1);
		sendField(objectToPlayer1, field2);
		sendField(objectToPlayer2, field2);		
		sendField(objectToPlayer2, field1);
		
		//Start the game
		gameIsRunning = true;
		runGame();
	}
	/**
	 * Game loop	
	 * Defines players' turns
	 * Sends and receives data to/from clients
	 * Modifies game data according to received data
	 */
	@SuppressWarnings("deprecation")
	protected void runGame() {
		
		boolean playerOneTurn = true;
		
		int[] input = new int[3];
		int row = NONE;
		int column = NONE;
		
		while (gameIsRunning) {	
			
			//Player 1 Turn
			if (playerOneTurn) {							
				boolean turn = true;
				try {
					sendTurnResults(objectToPlayer1, NONE, NONE, CONTINUE, 1);
					sendTurnResults(objectToPlayer2, NONE, NONE, WAIT, 2);
					
					while (turn) {	
						
						input = (int[])fromPlayer1.readObject();
						
						if (input != null) {
							switch (input[2]) {
								case HIT: {
									row = input[0];
									column = input[1];
									field2[input[0]][input[1]].ship.hit();
									field2[input[0]][input[1]] = null;
									sendTurnResults(objectToPlayer2, row, column, WAIT, 2);
									sendTurnResults(objectToPlayer1, NONE, NONE, WAIT, 1);
									break;
								}
								case MISS: {			
									row = input[0];
									column = input[1];
									sendTurnResults(objectToPlayer2, row, column, WAIT, 2);
									playerOneTurn=false;
									turn=false;
									break;
								}
								case EXIT: {
									sendTurnResults(objectToPlayer2, NONE, NONE, EXIT, 2);
									System.out.println("Player 1 has left the game. Terminating game session.");
									this.stop();
								}
							}
						}
						
						if (checkWin(ships2)) {
							turn = false;
							gameIsRunning = false;
						}
						
					}
				} catch (Exception e) {
					System.out.println("An error occured while sending messages to players!");
					gameIsRunning = false;
					System.exit(0);
				}
			}
			//Player 2 Turn
			else if (!playerOneTurn){
				boolean turn=true;
				
				try {
					sendTurnResults(objectToPlayer2, NONE, NONE, CONTINUE, 2);
					sendTurnResults(objectToPlayer1, NONE, NONE, WAIT, 1);
					
					while (turn) {
						input = (int[])fromPlayer2.readObject();
						
						if (input != null) {
							switch (input[2]) {
								case HIT: {
									row = input[0];
									column = input[1];
									field1[input[0]][input[1]].ship.hit();
									field1[input[0]][input[1]] = null;								
									sendTurnResults(objectToPlayer1, row, column, WAIT, 1);
									sendTurnResults(objectToPlayer2, NONE, NONE, WAIT, 2);
									break;
								}
								case MISS: {
									row = input[0];
									column = input[1];	
									sendTurnResults(objectToPlayer1, row, column, WAIT, 1);
									playerOneTurn=true;
									turn=false;
									break;
								}
								case EXIT: {
									sendTurnResults(objectToPlayer1, NONE, NONE, EXIT, 1);
									System.out.println("Player 2 has left the game. Terminating game session.");
									this.stop();
								}
							}
						}
						
						if (checkWin(ships1)) {
							turn = false;
							gameIsRunning = false;
						}
						
					}
				} catch (Exception e) {
					System.out.println("An error occured while sending messages to players!");
					gameIsRunning = false;
					System.exit(0);
				}
			}
		}
		
		if (checkWin(ships2)) {
			sendTurnResults(objectToPlayer1, NONE, NONE, WIN, 1);
			sendTurnResults(objectToPlayer2, NONE, NONE, LOSE, 2);
		}
		else if (checkWin(ships1)) {
			sendTurnResults(objectToPlayer2, NONE, NONE, WIN, 2);
			sendTurnResults(objectToPlayer1, NONE, NONE, LOSE, 1);
		}
		this.stop();
	}
	
	/**
	 * Checks whether there are any ships alive
	 * @param ships Player's ship set
	 * @return true if all ships have been sunk
	 */
	protected boolean checkWin(Ship[] ships) {
		for (int i=0; i<10; i++) {
			if (ships[i].alive) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Send turn results to player
	 * @param out Player's output stream
	 * @param x	Row number
	 * @param y Column number
	 * @param result Result code
	 */
	protected void sendTurnResults(ObjectOutputStream out, int x, int y, int result, int player) {
		Integer[][] results = new Integer[3][10];
		results[0][0] = x+1;
		results[0][1] = y+1;
		results[0][2] = result;
		
		for (int i=0; i<10; i++) {
			if (player == 1) {
				results[1][i] = ships1[i].noOfCells;
				results[2][i] = ships2[i].noOfCells;
			}
			else {
				results[2][i] = ships1[i].noOfCells;
				results[1][i] = ships2[i].noOfCells;
			}
		}
		try {
			out.writeObject(results);
			out.flush();
		} catch (IOException e) {
			System.out.println("Could not send turn results to client!");
		}
	}

	/**
	 * Send generated field to player
	 * @param out Player's output stream
	 * @param field Game field
	 */
	protected void sendField(ObjectOutputStream out, Tile[][] field) {
		int[][] output = new int[11][11];
		
		for (int x=0; x<11; x++) {
			for (int y=0; y<11; y++) {
				output[x][y] = -1;
			}
		}
		
		for (int x=0; x<10; x++) {
			for (int y=0; y<10; y++) {
				if (field[x][y] != null)
					output[x+1][y+1] = field[x][y].direction;
				else output[x+1][y+1] = -1;
			}
		}
		
		try {
			out.writeObject(output);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialises ships arrays
	 */
	protected void initShips() {
		for (int i=0; i<4; i++) {
			ships1[i] = new Ship(1);
			ships2[i] = new Ship(1);
		}
		for (int i=4; i<7; i++) {
			ships1[i] = new Ship(2);
			ships2[i] = new Ship(2);
		}
		for (int i=7; i<9; i++) {
			ships1[i] = new Ship(3);
			ships2[i] = new Ship(3);
		}
		ships1[9] = new Ship(4);
		ships2[9] = new Ship(4);
	}
	
	/** Sets the ships randomly on a game field
	 * 
	 * @param field Game field
	 * @param ships Ships to be distributed
	 */
	protected void initField(Tile[][] field, Ship[] ships) {
		Random r = new Random();		
		
		for (int i=0; i<10; i++) {
			int direction = UP;
			int row = 0, column = 0, n=0;
			int length = ships[i].getLength();
			Integer [][] coordinates = new Integer[length][2];
			
			while (n<length) {
				boolean empty = false;
				boolean emptyAround = false;
				
				if (n==0) {
					while (!empty || !emptyAround) {
						row = r.nextInt(10);
						column = r.nextInt(10);
					
						if (field[row][column] == null)
							empty = true;
						
						if (empty) {
							emptyAround = checkEmptyAround(row, column, field);
						}
					}
				}
				else {
					if (field[row][column] == null)
							empty = true;
						
					if (empty) {
							emptyAround = checkEmptyAround(row, column, field);
						}
					}
					
				
				if (empty && emptyAround) {					
				
					coordinates[n][0] = row;
					coordinates[n][1] = column;
					
					if (n != length-1) {
						boolean directionOk = false;
						
						while (!directionOk && length>1) {
							directionOk = true;
							if (n==0) direction = r.nextInt(4);
						
							switch (direction) {
								case UP: {
									if (row-1>=0) row--;
									else directionOk = false;
									break;
								}
								case DOWN: {
									if (row+1<10) row++;
									else directionOk = false;
									break;
								}
								case LEFT: {
									if (column-1>=0) column--;
									else directionOk = false;
									break;
								}
								case RIGHT: {
									if (column+1<10) column++;
									else directionOk = false;
									break;
								}
							}
							if (!directionOk && n>0) {
								directionOk = true;
								n=-1;
							}
								
						}						
					}
					n++;
				}
				else n=0;
			}
			
			for (int z=0; z<length; z++) {
				if (length==1) {
					field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.SINGLE);
				}
				else {
					if (direction == UP) {
						if (z==0)
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.SOUTH);
						if (z>0 && z<(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i], Tile.VERTICAL);
						if (z==(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i], Tile.NORTH);
					}
					else if (direction == RIGHT) {
						if (z==0)
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.WEST);
						if (z>0 && z<(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.HORIZONTAL);
						if (z==(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.EAST);
					}
					else if (direction == DOWN) {
						if (z==0)
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.NORTH);
						if (z>0 && z<(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.VERTICAL);
						if (z==(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.SOUTH);
					}
					else if (direction == LEFT) {
						if (z==0)
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.EAST);
						if (z>0 && z<(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.HORIZONTAL);
						if (z==(length-1))
							field[coordinates[z][0]][coordinates[z][1]] = new Tile(ships[i],Tile.WEST);
					}
				}
				
			}			
		}
	}
	
	/**
	 * Checks if the space around a specific tile if empty
	 * @param row row number
	 * @param column column number
	 * @param field Player's tile set
	 * @return true if tiles around are empty
	 */
	protected boolean checkEmptyAround(int row, int column, Tile[][] field) {
		boolean ok = true;
		
		if (ok) {
			ok = false;
			try {
				if (field[row+1][column] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row+1][column+1] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row][column+1] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row-1][column+1] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row-1][column] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row-1][column-1] == null) {
				ok = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row][column-1] == null) {
					ok = true;
				}	
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			ok = false;
			try {
				if (field[row+1][column-1] == null) {
					ok = true;
				}	
			} catch (ArrayIndexOutOfBoundsException e) {
				ok=true;
			}
			
		}
		if (ok) {
			return true;
		}
		else return false;
	}
}
