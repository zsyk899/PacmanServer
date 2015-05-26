package model;

import game.Direction;
import game.MainGame;

/**
 *	This model defines all behaviors that a ghost should have.
 *	However due to time limit, this was not implemented in the game
 */
public class Ghost extends ControllableObject{
	
	private static final String pacImagePath = "resources/ghost.png";
	private static final int PACWIDTH = 30;
	private static final int PACHEIGHT = 24;
	private static final int PACFRAMERATE = 10;
	private static final int PACSPEED = 3;
	private double angle; // 0,90,180,270
	

	public Ghost(int id, int x, int y) {
		super(pacImagePath, new int[] {0, 0, 30, 0}, PACWIDTH, PACHEIGHT, PACFRAMERATE, x, y, id);	
		super.speed = PACSPEED;
		angle = 0;
	}	
	
	/**
	 * Draws ghost on the screen.
	 */
	public void draw() {
		super.rotate(angle);
		super.draw();
	}

	@Override
	public void spriteForDirection(Direction d) {
		// TODO Auto-generated method stub
		//Do nothing, ghost does not have to rotate		
	}
}
