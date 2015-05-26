package menu;

import game.MainGame;
import ucigame.Image;
import ucigame.Sprite;

/**
 * Defines the layout of the game over screen
 */
public class GameOverMenu {

	private Sprite GameOverboard;	
	MainGame game;
	
	/**
	 * Constructor for MainMenu, creates all the buttons in the menu 
	 */	
	public GameOverMenu(MainGame game) {
		this.game = game;
		Image GameOverboardImage = game.getImage("resources/gameover.png");
		// create a connect button		
		GameOverboard = game.makeButton("GameOver", GameOverboardImage,
				GameOverboardImage.width(), GameOverboardImage.height());
		GameOverboard.position(MainGame.windowCentreX - GameOverboard.width()/2, MainGame.windowCentreX - GameOverboard.height()/2);			
	}
	
	/**
	 * Draws all the buttons in the GameOverMenu. Each button corresponds to a OnClick state located 
	 * in MainGame.java.
	 */
	public void draw(){
		GameOverboard.draw();
	}
}
