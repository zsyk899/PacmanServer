package controller;
import java.util.ArrayList;

import model.ControllableObject;
import game.Direction;
import game.GameState;
import game.MainGame;

/**
 * This class controls all central functions of the game
 * 
 * @author Siyuan Zhang
 *
 */
public class GameController {
	
	private static GameController controller;
	private MainGame game;
	private GameState state;
	private boolean hasNextMove;
	
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
		hasNextMove = false;
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
		handleCollisions(); // handles the actors colliding
		checkStageClear(); // handle stage being clear (loading next stage)
	}

	private void checkStageClear() {
		// TODO Auto-generated method stub
		
	}

	private void movePlayers() {
		// TODO Auto-generated method stub
		state.movePlayers();
//		stopIfCollidesWith(MainGame.TOPEDGE, MainGame.BOTTOMEDGE, MainGame.LEFTEDGE, MainGame.RIGHTEDGE);
	}

	private void handleCollisions() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * Commands PacMan to move in the specified direction if he is able to do so
	 * without colliding into a wall. Note that if PacMan is commanded to move
	 * into a direction he cannot go in then the direction will be unchanged.
	 * This function is invoked primarily based on the keyboard input on the
	 * game by the user.
	 * 
	 * @param d
	 *            The direction in which PacMan will now move.
	 * 
	 */
	public void setPacManDirection(String address, Direction d) {
		state.getPlayer(address).move(d);
	}
	
	
	public void setPacmenDirection(Direction d) {
		for(ControllableObject player : state.getPlayers().values()){
			player.move(d);
		}
	}
	
	public MainGame getGame(){
		return game;
	}
}
