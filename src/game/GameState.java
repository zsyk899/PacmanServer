package game;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.StatusCode;
import model.ControllableObject;
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
	private ArrayList<ControllableObject> players;
	private Pacman pacman;
	private MainGame game;
	
	public GameState(MainGame game){
		this.game = game;
		this.players = new ArrayList<ControllableObject>();
	}
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
		for(ClientConfig config: ClientMap.getClients()){
			pacman = new Pacman(config.getId(), 200, 200);
			System.out.println("client added with id: " + config.getId());
			players.add(pacman);
		}		
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
//		for(ControllableObject player : players){
//			player.draw();
//		}
		//draw the game and make snapshot
		String gamestate = snapshotGameState();
		if(game != null)
			game.updateClientState(gamestate);
	}
	
	/**
	 * Draw each player on screen and make a snapshot of the game state
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String snapshotGameState(){
		JSONObject state = new JSONObject();
		JSONArray playerState = new JSONArray();
		for(ControllableObject player : players){
			JSONObject playerInfo = new JSONObject();
			playerInfo.put("id", player.getId());
			playerInfo.put("x", player.getX());
			playerInfo.put("y", player.getY());
			playerInfo.put("direction", player.getDirection());
			playerState.add(playerInfo);
			
			player.draw();
		}	
		state.put("state", playerState);
		state.put("request", new Integer(StatusCode.GAME_STATE));
		state.put("num", new Integer(players.size()));
		return state.toJSONString();
	}
}
