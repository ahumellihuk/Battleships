/**
 * The tile class represents one tile on a game board
 * Used mainly to determine which icon to display
 * 
 * @author Dmitri Samoilov
 *
 */
public class Tile {
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final int VERTICAL = 4;
	public static final int HORIZONTAL = 5;
	public static final int SINGLE = 6;
	
	/** Indicates which ship it belongs to */
	Ship ship;
	/** Shows which direction the tile is facing */
	int direction;
	
	public Tile(Ship ship, int direction) {
		this.ship = ship;
		this.direction = direction;
	}
}
