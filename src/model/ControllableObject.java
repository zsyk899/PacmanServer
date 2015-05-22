package model;

import game.Direction;
import controller.GameController;
import ucigame.Sprite;

public abstract class ControllableObject extends Sprite{
	protected double speed;
	protected GameController control;
	protected Direction currentDirection;
	
	public ControllableObject(String imgPath, int[] frames, int width, int height, int framerate, int x, int y) {
		super(width, height);
		control = GameController.getInstance();	

		position(x, y);
		addFrames(control.getGame().getImage(imgPath, 255,255,255), frames);
		framerate(framerate);
	}	
	
	/**
	 * Returns true if the object collided with other objects
	 * 
	 * @param s		the sprite that the object might collide with
	 * @return true if this object collided with the specified sprite
	 */
	public boolean collidedWith(Sprite s) {
		super.checkIfCollidesWith(s);
		return super.collided();
	}
	
	/**
	 * It changes the moving direction of the object.
	 * 
	 * @param d		the direction to move
	 */
	public void move(Direction d) {
		if (canMove(d))
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
	 * This method checks if the object can move in given direction
	 * 
	 * @param d		the direction to check to for a valid move
	 * @return true if the move is allowed.
	 */
	public abstract boolean canMove(Direction d);
}
