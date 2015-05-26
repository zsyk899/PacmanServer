package menu;
import java.util.ArrayList;

import game.MainGame;
import ucigame.Image;
import ucigame.Sprite;
import utilies.ClientConfig;
import utilies.ClientMap;

/**
 * 
 * @author Siyuan Zhang
 *
 */
public class MainMenu{

	private Sprite connectButton;
	private Sprite quitButton;
	

	MainGame game;
	/**
	 * 
	 * Constructor for MainMenu, creates all the buttons in the menu 
	 * 
	 */
	
	public MainMenu(MainGame game) {
		this.game = game;
		Image connectButtonImage = game.getImage("resources/connect_button.png");
		// create a connect button		
		connectButton = game.makeButton("Connect", connectButtonImage,
				connectButtonImage.width(), connectButtonImage.height()/3);
		connectButton.position(MainGame.windowCentreX - connectButton.width()/2, MainGame.windowCentreY - 2 * connectButton.height());
				
		Image quitButtonImage = game.getImage("resources/quit_button.png");
		//create a quit button
		quitButton = game.makeButton("Quit", quitButtonImage,
				quitButtonImage.width(), quitButtonImage.height()/3);
		quitButton.position(MainGame.windowCentreX - quitButton.width()/2,	MainGame.windowCentreY);			
	}
	
	/**
	 * Draws all the buttons in the MainMenu. Each button corresponds to a OnClick state located 
	 */
	public void draw(){
		connectButton.draw();
		quitButton.draw();
	}
}