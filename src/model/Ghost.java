package model;

import game.Direction;
import game.MainGame;

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
	 * Draws Pacman on the screen.
	 */
	public void draw() {
		super.rotate(angle);
		super.move();
		stopIfCollidesWith(MainGame.TOPEDGE, MainGame.BOTTOMEDGE, MainGame.LEFTEDGE, MainGame.RIGHTEDGE);
		super.draw();
	}

	@Override
	public void spriteForDirection(Direction d) {
		// TODO Auto-generated method stub
		//Do nothing, ghost does not have to rotate		
	}
	
	@Override
	public boolean canMove(Direction d) {
		// TODO Auto-generated method stub
		return true;
	}
}
