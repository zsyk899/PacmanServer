package game;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.WallController;
import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.StatusCode;
import model.ControllableObject;
import model.Pacman;
import msg.Msg;
import msg.MsgFactory;

/**
 * This class controls all objects in the game
 * @author Siyuan Zhang
 *
 */
public class GameState {
	private static GameState gameInstance;
	private int lives;
	private int scores;
	private HashMap<String, ControllableObject> players;
	//private Pacman pacman;
	private MainGame game;
	private int counter;
    private MsgFactory msgFactory;
	private WallController walls;
	
	public GameState(MainGame game){
		this.game = game;
		this.players = new HashMap<String, ControllableObject>();
		this.counter = 0;
		this.msgFactory = new MsgFactory();
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
		walls = new WallController();
		for(ClientConfig config: ClientMap.getClients()){
			Pacman pacman = new Pacman(config.getId(), 200, 200);
			System.out.println("client added with id: " + config.getId());
			players.put(config.getAddress().getHostAddress(), pacman);
		}		
	}

	/**
	 * 
	 * Gets the instance of player whose machine uses given IP address.
	 * 
	 * @return the ControllableObject instance inside the game world.
	 */
	public ControllableObject getPlayer(String address) {
		return players.get(address);
	}
	
	public HashMap<String, ControllableObject> getPacmen(){
		return players;
	}

	
	/**
	 * Draws all objects in the game
	 */
	public void draw() {
//		for(ControllableObject player : players){
//			player.draw();
//		}
		//draw the game and make snapshot
		walls.drawWalls();
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
		for(ControllableObject player : players.values()){
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
		
		this.counter++;
		Msg msg = msgFactory.getNewInstance();
		msg.setInstruction(state.toJSONString());
		msg.setCounter(counter);
		
		return msg.toJString();
	}
}
