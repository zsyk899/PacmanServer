package controller;

import model.ControllableObject;
import game.Direction;
import game.GameState;
import game.MainGame;

/**
 * This class controls all central functions of the game.
 *
 */
public class GameController {
	
	private static GameController controller;
	private MainGame game;
	private GameState state;
	
	private GameController(MainGame mainGame) {
		this.game = mainGame;
		GameState.setInstance(new GameState(mainGame));
		state = GameState.getInstance();
	}
	
	/**
	 * A static interface that other classes can use to get the global instance of GameController
	 * @return an instance of GameController
	 */
	public static GameController getInstance() {
		return controller;
	}

	/**
	 * A static interface that can be used to create a global instance of GameController
	 * @param mainGame	The Main Game class
	 * @return			An instance of GameController
	 */
	public static GameController setInstance(MainGame mainGame) {
		controller = new GameController(mainGame);
		return controller;
	}

	/**
	 * start the Pacman game
	 */
	public void startGame() {
		// TODO Auto-generated method stub
		state.initialize();
	}
	
	/**
	 * Draw the game
	 * (Invoke draw method in every objects)
	 * 
	 */
	public void drawState() {
		state.draw();
	}
	
	/**
	 * For each iteration in the game loop, events including collisions,
	 * moving chracters, etc. need to be checked before rendering them.
	 */
	public void nextMove() {
		movePlayers(); // moves the actors for tick
//		handleCollisions(); // handles the actors colliding
	}
	
	/**
	 * Move players
	 */
	private void movePlayers() {
		// TODO Auto-generated method stub
		state.movePlayers();
	}
	
//	/** 
//	 * This should be implemented if collision between pacmen is allowed
//	 */
//	private void handleCollisions() {
//		// TODO Auto-generated method stub
//	}
	
	/**
	 * To move the player with given IP address to move to the given direction
	 * @param d		The direction that the pacman will move to.
	 */
	public void setPacManDirection(String address, Direction d) {
		state.getPlayer(address).move(d);
	}
	
	/**
	 * This method is used only when the server want to control all pacman
	 * @param d		the direction that all pacmen will move to
	 */
	public void setPacmenDirection(Direction d) {
		for(ControllableObject player : state.getPlayers().values()){
			player.move(d);
		}
	}
	
	/**
	 * @return the game instance
	 */
	public MainGame getGame(){
		return game;
	}
}
