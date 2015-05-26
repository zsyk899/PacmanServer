package game;

import java.util.concurrent.ConcurrentHashMap;

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
 *
 */
public class GameState {
	private static GameState gameInstance;
	private ConcurrentHashMap<String, ControllableObject> players;
	private MainGame game;
	private int counter;
    private MsgFactory msgFactory;
	private WallController walls;
	
	public GameState(MainGame game){
		this.game = game;
		this.players = new ConcurrentHashMap<String, ControllableObject>();
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
	
	/**
	 * Initialize the game
	 */
	public void initialize(){
		setupGame();
	}
	
	/**
	 * Create all objects
	 */
	public void setupGame(){
		walls = new WallController();
		for(ClientConfig config: ClientMap.getClients()){
			Pacman pacman = new Pacman(config.getId(), 300, 280);
			System.out.println("client added with id: " + config.getId());
			players.put(config.getAddress().getHostAddress(), pacman);
		}		
	}

	/**
	 * 
	 * @return the player with given IP address
	 */
	public ControllableObject getPlayer(String address) {
		return players.get(address);
	}
	
	/**
	 * @return all players
	 */
	public ConcurrentHashMap<String, ControllableObject> getPlayers(){
		return players;
	}

	/**
	 * Move the player and check if they collide with walls
	 */
	public void movePlayers(){
		for(ControllableObject player: players.values()){
			player.move();
			player.stopIfCollidesWith(MainGame.TOPEDGE, MainGame.BOTTOMEDGE, MainGame.LEFTEDGE, MainGame.RIGHTEDGE);
			walls.stopCollisions(player);
		}
	}
	
	/**
	 * Draws all objects in the game
	 */
	public void draw() {
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
	
	/**
	 * @return a stop message to be sent to the client
	 */
	@SuppressWarnings("unchecked")
	public String getStopMessage(){
		JSONObject message = new JSONObject();
		message.put("request", StatusCode.GAME_OVER);
		
		Msg msg = msgFactory.getNewInstance();
		msg.setInstruction(message.toJSONString());
		msg.setCounter(counter);
		
		return msg.toJString();
	}
	
	/**
	 * @return counter
	 */
	public int getCounter(){
		return counter;
	}
}
