package model;

import java.awt.Point;

import ucigame.Image;
import ucigame.Sprite;
import controller.GameController;

public class Wall extends Sprite {
	
    protected GameController control;
	private static final String imagePath = "resources/level.png";

	/**
	 * Declares a new Stationary Object without declaring a picture
	 * @param width the width of the object
	 * @param height the height of the object
	 */
	public Wall(int x, int y, int width, int height) {
		super(width, height);
		control = GameController.getInstance();
		super.addFrame(control.getGame().getImage(imagePath, 255,255,255), x, y);
		this.position(x, y);		
	}
	
	/**
	 * This is used for collision detection.
	 * @param c is the object that is being checked this sprite
	 * @return true if collided into the object and the object is visible
	 */
	public boolean collidedWith(ControllableObject c){
		super.checkIfCollidesWith(c);
		return super.collided() && super.isShown();
	}
	
	/**
	 * Sets the position of the Sprite to the given point
	 * @param p is used to set the position of the sprite.
	 */
	public void position(Point p) {
		super.position(p.x, p.y);
	}
	
	/**
	 * A sprite can have multiple frames, or images, assigned to it 
	 * with addFrame(). Each time the sprite is displayed on the 
	 * canvas with draw(), it will automatically cycle to the next 
	 * frame. image is an already loaded Image object. The x and 
	 * y parameters specify the upper left hand corner of a rectangle 
	 * in the image that will be sprite1's next frame. The width and 
	 * height of that rectangle are same as sprite1's width and height.
	 * @param imgPath is the path of the image
	 * @param x is the x coordinate of the upper left corner in the image
	 * @param y is the y coordinate of the upper left corner in the image
	 */
	public void addFrame(String imgPath, int x, int y){
		super.addFrame(getImage(imgPath), x, y);
	}
	
	/**
	 * This method reads in the specified image file from disk. The file 
	 * can be in GIF, JPEG, or PNG format. Since this version of getImage() 
	 * does not specify a transparent pixel color, no pixels will be transparent. 
	 * @param stringPath of the image to be retrieved
	 * @return the image that the path is pointing to.
	 */
	private static Image getImage(String stringPath){
		return GameController.getInstance().getGame().getImage(stringPath, 77,77,77);
	}
}
