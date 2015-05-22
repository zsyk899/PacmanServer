package menu;

import java.util.ArrayList;

import game.MainGame;
import ucigame.Image;
import ucigame.Sprite;

public class ServerMenu {
	private Sprite startButton;
	private Sprite quitButton;
	
	MainGame game;
	/**
	 * 
	 * Constructor for MainMenu, creates all the buttons in the menu 
	 * 
	 */
	
	public ServerMenu(MainGame game) {
		this.game = game;

		Image startButtonImage = game.getImage("resources/start_button.jpg");
		// create a connect button, it requires a onClickStart Method in main game		
		startButton = game.makeButton("Start", startButtonImage,
				startButtonImage.width(), startButtonImage.height()/3);
		startButton.position(MainGame.windowCentreX - startButton.width()/2, MainGame.windowCentreY + startButton.height());
				
		Image quitButtonImage = game.getImage("resources/quit_button.jpg");
		//create a quit button, it requires a onClickQuit Method in main game	
		quitButton = game.makeButton("Quit", quitButtonImage,
				quitButtonImage.width(), quitButtonImage.height()/3);
		quitButton.position(MainGame.windowCentreX - quitButton.width()/2,	MainGame.windowCentreY + 2 * startButton.height() + 50);			
	}
	
	/**
	 * 
	 * Draws all the buttons in the MainMenu. Each button corresponds to a OnClick state located 
	 * in PacManGame.java.
	 * 
	 */
	public void draw(){
		startButton.draw();
		quitButton.draw();
	}
}
