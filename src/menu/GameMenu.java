package menu;

import game.MainGame;
import ucigame.Image;
import ucigame.Sprite;

public class GameMenu {

	private Sprite timeboard;	

	MainGame game;
	/**
	 * 
	 * Constructor for MainMenu, creates all the buttons in the menu 
	 * 
	 */
	
	public GameMenu(MainGame game) {
		this.game = game;
		Image connectButtonImage = game.getImage("resources/timeboard.png");
		// create a connect button		
		timeboard = game.makeButton("TimeBoard", connectButtonImage,
				connectButtonImage.width(), connectButtonImage.height());
		timeboard.position(MainGame.windowCentreX - timeboard.width()/2, MainGame.windowHeight - timeboard.height());			
	}
	
	/**
	 * 
	 * Draws all the buttons in the MainMenu. Each button corresponds to a OnClick state located 
	 * in PacManGame.java.
	 * 
	 */
	public void draw(){
		timeboard.draw();
	}
}
