package model;
import game.Direction;
import game.MainGame;
import controller.GameController;
import ucigame.Image;
import ucigame.Sprite;

/**
 *	This model defines all behaviors of a pacman
 */
public class Pacman extends ControllableObject{

	
	private static final String pacImagePath = "resources/pacman.png";
	private static final int PACWIDTH = 22;
	private static final int PACHEIGHT = 22;
	private static final int PACFRAMERATE = 10;
	private static final int PACSPEED = 6;
	private double angle; // 0,90,180,270
	

	public Pacman(int id, int x, int y) {
		super(pacImagePath, new int[] {0, 0, 22, 0, 44, 0, 66, 0}, PACWIDTH, PACHEIGHT, PACFRAMERATE, x, y, id);
		
		super.speed = PACSPEED;
		angle = 0;
	}	
	
	/**
	 * Draws Pacman on the screen.
	 */
	public void draw() {
		super.rotate(angle);
		super.draw();
	}

	/**
	 * Change the image according to the direction it is moving on
	 */
	@Override
	public void spriteForDirection(Direction d) {
		// TODO Auto-generated method stub
		if (d == Direction.UP) {
			angle = 270;
		} else if (d == Direction.DOWN) {
			angle = 90;
		} else if (d == Direction.LEFT) {
			angle = 180;
		} else if (d == Direction.RIGHT) {
			angle = 0;
		}
		
	}
}
