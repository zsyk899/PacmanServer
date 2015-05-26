package model;

import game.Direction;
import controller.GameController;
import ucigame.Sprite;

/**
 *	Define all behaviors of a controllable object
 */
public abstract class ControllableObject extends Sprite{
	protected double speed;
	protected GameController control;
	protected Direction currentDirection;
	protected int id;
	
	public ControllableObject(String imgPath, int[] frames, int width, int height, int framerate, int x, int y, int id) {
		super(width, height);
		control = GameController.getInstance();	
		this.id = id;
		this.currentDirection = Direction.LEFT;
		position(x, y);
		addFrames(control.getGame().getImage(imgPath, 255,255,255), frames);
		framerate(framerate);
	}	
	
	/**
	 * @param s		the object to be checked
	 * @return true if this object collided with a object
	 */
	public boolean collidedWith(Sprite s) {
		super.checkIfCollidesWith(s);
		return super.collided();
	}
	
	/**
	 * It changes the moving direction of the object.
	 * @param d		the direction to move
	 */
	public void move(Direction d) {
		currentDirection = d;
		spriteForDirection(currentDirection);

		if (currentDirection == Direction.UP) {
			motion(0, 0 - speed);
		} else if (currentDirection == Direction.DOWN) {
			motion(0, speed);
		} else if (currentDirection == Direction.LEFT) {
			motion(0 - speed, 0);
		} else if (currentDirection == Direction.RIGHT) {
			motion(speed, 0);
		}		
	}
	
	/**
	 * This method is used to change the direction of sprite
	 * A sprite uses different images when moving along with different direction
	 * 
	 * @param d		the direction to change the sprite image to
	 */
	public abstract void spriteForDirection(Direction d);
	
	/**
	 * @return x position of the object
	 */
	public double getX(){
		return super.x();
	}
	
	/**
	 * @return y position of the object
	 */
	public double getY(){
		return super.y();
	}
	
	/**
	 * @return id of the object
	 */
	public int getId(){
		return this.id;
	}
	
	/**
	 * @return direction the object is moving on
	 */
	public String getDirection(){
		return this.currentDirection.toString();
	}
}
