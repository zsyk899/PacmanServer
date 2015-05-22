package game;

import java.util.ArrayList;

import model.Pacman;

/**
 * This class controls all objects in the game
 * @author Siyuan Zhang
 *
 */
public class GameState {
	private static GameState gameInstance;
	private int lives;
	private int scores;
	private Pacman pacman;
	
	/**
	 * A static interface that other classes can use to get the global instance of GameState
	 * @return an instance of GameState
	 */
	public static GameState getInstance() {
		return gameInstance;
	}

	/**
	 * A static interface that can be used to create a global instance of GameState
	 * @param state		The GameState class
	 * @return			An instance of GameState
	 */
	public static void setInstance(GameState state) {
		gameInstance = state;
	}
	
	public void initialize(){
		this.lives = 0;
		this.scores = 0;
		setupGame();
	}
	
	public void setupGame(){
		pacman = new Pacman(200, 200);
		
		
	}

	/**
	 * 
	 * Gets the instance of PacMan currently within the game world.
	 * 
	 * @return the PacMan instance inside the game world.
	 */
	public Pacman getPacMan() {
		return this.pacman;
	}

	
	/**
	 * Draws all objects in the game
	 */
	public void draw() {
		pacman.draw();
	}
	
}
