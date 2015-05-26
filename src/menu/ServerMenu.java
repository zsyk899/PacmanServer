package menu;

import java.util.ArrayList;

import game.MainGame;
import ucigame.Image;
import ucigame.Sprite;

/**
 * Define the layout of the server main menu
 */
public class ServerMenu {
	private Sprite startButton;
	private Sprite quitButton;
	
	private Sprite clientOneButton;	
	private Sprite clientTwoButton;	
	private Sprite clientThreeButton;
	private Sprite clientFourButton;
	
	MainGame game;
	
	/**
	 * Constructor for MainMenu, creates all the buttons in the menu 
	 */	
	public ServerMenu(MainGame game) {
		this.game = game;

		Image clientOneImage = game.getImage("resources/Client1.png");
		// create a connect button, it requires a onClickStart Method in main game		
		clientOneButton = game.makeButton("ClientOne", clientOneImage,
				clientOneImage.width(), clientOneImage.height());
		clientOneButton.position(MainGame.windowCentreX - clientOneButton.width()/2, 20);
		
		Image clientTwoImage = game.getImage("resources/Client2.png");
		// create a connect button, it requires a onClickStart Method in main game		
		clientTwoButton = game.makeButton("ClientTwo", clientTwoImage,
				clientTwoImage.width(), clientTwoImage.height());
		clientTwoButton.position(MainGame.windowCentreX - clientTwoButton.width()/2, 20 + clientTwoButton.height());
		
		Image clientThreeImage = game.getImage("resources/Client3.png");
		// create a connect button, it requires a onClickStart Method in main game		
		clientThreeButton = game.makeButton("ClientThree", clientThreeImage,
				clientThreeImage.width(), clientThreeImage.height());
		clientThreeButton.position(MainGame.windowCentreX - clientThreeButton.width()/2, 20 + 2*clientThreeButton.height());
		
		Image clientFourImage = game.getImage("resources/Client4.png");
		// create a connect button, it requires a onClickStart Method in main game		
		clientFourButton = game.makeButton("ClientFour", clientFourImage,
				clientFourImage.width(), clientFourImage.height());
		clientFourButton.position(MainGame.windowCentreX - clientFourButton.width()/2, 20 + 3*clientFourButton.height());
		
		Image startButtonImage = game.getImage("resources/start_button.png");
		// create a connect button, it requires a onClickStart Method in main game		
		startButton = game.makeButton("Start", startButtonImage,
				startButtonImage.width(), startButtonImage.height()/3);
		startButton.position(MainGame.windowCentreX - startButton.width()/2, MainGame.windowCentreY + startButton.height());

		Image quitButtonImage = game.getImage("resources/quit_button.png");
		//create a quit button, it requires a onClickQuit Method in main game	
		quitButton = game.makeButton("Quit", quitButtonImage,
				quitButtonImage.width(), quitButtonImage.height()/3);
		quitButton.position(MainGame.windowCentreX - quitButton.width()/2,	MainGame.windowCentreY + 2 * startButton.height() + 20);
	}
	
	/**
	 * 
	 * Draws all the buttons in the MainMenu. Each button corresponds to a OnClick state located 
	 * in MainGame.java.
	 * 
	 */
	public void draw(){
		
		clientOneButton.draw();
		clientTwoButton.draw();
		clientThreeButton.draw();
		clientFourButton.draw();
		startButton.draw();
		quitButton.draw();
	}
	
	/**
	 * Hide the button for corresponding client on the board
	 * @param client
	 */
	public void hideButton(int client){
		switch(client){
			case 1:
				clientOneButton.hide();
				break;
			case 2:
				clientTwoButton.hide();
				break;
			case 3:
				clientThreeButton.hide();
				break;
			case 4:
				clientFourButton.hide();
				break;
		}
	}
	
	/**
	 * Hide all buttons on the board
	 */
	public void hideAllClientButton(){
		clientOneButton.hide();

		clientTwoButton.hide();

		clientThreeButton.hide();

		clientFourButton.hide();
	}
	
	/**
	 * Show given number of clients on the board
	 * @param clients
	 */
	public void showButton(int clients){
		switch(clients){
			case 0:
				clientOneButton.hide();
				clientTwoButton.hide();
				clientThreeButton.hide();
				clientFourButton.hide();
				break;
			case 1:
				clientOneButton.show();
				clientTwoButton.hide();
				clientThreeButton.hide();
				clientFourButton.hide();
				break;
			case 2:
				clientOneButton.show();
				clientTwoButton.show();
				clientThreeButton.hide();
				clientFourButton.hide();
				break;
			case 3:
				clientOneButton.show();
				clientTwoButton.show();
				clientThreeButton.show();
				clientFourButton.hide();
				break;
			case 4:
				clientOneButton.show();
				clientTwoButton.show();
				clientThreeButton.show();
				clientFourButton.show();
				break;
		}
	}
	
	/**
	 * Show all buttons on the board
	 */
	public void showAllClientButton(){
		clientOneButton.show();

		clientTwoButton.show();

		clientThreeButton.show();

		clientFourButton.show();
	}
}
