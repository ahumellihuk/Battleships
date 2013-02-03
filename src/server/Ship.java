/**The ship class represents one ship in game
 * 
 * @author Dmitri Samoilov
 *
 */
public class Ship {
	/** Number of cells alive */
	int noOfCells;
	/** Indicates whether the ship has been sunk or not */
	boolean alive;
	
	public Ship(int n) {
		noOfCells = n;
		alive = true;
	}
	/**
	 * Decreases number of cells by one
	 */
	public void hit() {
		noOfCells--;
		if (noOfCells==0)
			alive = false;
	}
	/**
	 * 
	 * @return Returns number of cells alive
	 */
	public int getLength() {
		return noOfCells;
	}

}
