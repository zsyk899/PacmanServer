package controller;

import java.awt.Point;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import model.ControllableObject;
import model.Wall;
import ucigame.Sprite;
import game.GameState;

/**
 * 
 * This class controls all behaviors of walls in the game
 *
 */
public class WallController {
	protected static GameState state = GameState.getInstance(); 
	private ConcurrentHashMap<Point, Wall> hash;

	public WallController() {
       hash = new ConcurrentHashMap<Point, Wall>();
       makeWalls();
	}
	
	/**
	 * Add a wall to the game
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void addWall(int x, int y, int width, int height) {
		hash.put(new Point(x,y), new Wall(x, y, width, height));
	}	
	
	/**
	 * Draw each wall on the screen
	 */
	public void drawWalls() {
		for (Sprite s : this.getWalls())
		{
			s.draw();
		}
	}
	
	/**
	 * @return all walls
	 */
	public Collection<Wall> getWalls() {
		return hash.values();
	}
	
	/**
	 * To check if the next move of a player will collide with a wall
	 * @param player
	 * @param xCheck
	 * @param yCheck
	 * @return
	 */
	public boolean willCollideAtPos(ControllableObject player, int xCheck, int yCheck) {

		double curX = player.x() + player.xspeed();
		double curY = player.y() + player.yspeed();
		player.nextX(player.x() + xCheck);
		player.nextY(player.y() + yCheck);
		Sprite[] spriteWalls = getWalls().toArray(new Sprite[0]);
		player.checkIfCollidesWith(spriteWalls);
		boolean r = player.collided();
		player.nextX(curX);
		player.nextY(curY);
		if (r)
			return false;
		else
			return true;
	}

	/**
	 * If a object collided with walls then stop moving it
	 * 
	 * @param c the object to stop if collided with the wall
	 */
	public void stopCollisions(ControllableObject c) {
		c.stopIfCollidesWith(getWalls().toArray(new Sprite[0]));
	}
	
	/**
	 * Create walls for the maze
	 */
	private void makeWalls() {
	
		addWall(0,0,18,596);
		addWall(18,0,582,17);
		addWall(18,17,266,1);
		addWall(284,17,33,71);
		addWall(317,17,283,1);
		addWall(581,18,19,578);
		addWall(45,47,73,39);
		addWall(154,49,96,39);
		addWall(350,49,97,39);
		addWall(481,49,74,39);
		addWall(46,124,72,23);
		addWall(154,124,31,136);
		addWall(220,124,160,23);
		addWall(414,124,33,136);
		addWall(481,124,73,23);
		addWall(284,147,33,56);
		addWall(481,176,100,205);
		addWall(18,178,100,203);
		addWall(185,181,65,22);
		addWall(350,181,64,22);
		//
		addWall(220,239,60,10);
		addWall(320,239,60,10);
		addWall(220,249,11,75);
		addWall(370,249,10,59);
		addWall(154,294,31,87);
		addWall(414,294,33,87);
		addWall(231,308,149,16);
		addWall(220,353,160,28);
		addWall(284,381,33,53);
		addWall(45,411,73,23);
		addWall(154,411,96,23);
		addWall(350,411,97,23);
		addWall(481,411,30,79);
		addWall(511,411,44,23);
		addWall(88,434,30,56);
		addWall(18,467,38,23);
		addWall(154,467,31,57);
		addWall(220,467,160,23);
		addWall(414,467,33,57);
		addWall(544,467,37,23);
		addWall(284,490,33,58);
		addWall(45,524,205,24);
		addWall(350,524,205,24);
		addWall(18,574,563,22);
	}
}
