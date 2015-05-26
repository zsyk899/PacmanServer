package game;

import menu.GameMenu;
import menu.GameOverMenu;
import menu.ServerMenu;
import controller.GameController;
import server.Server;
import server.TCPServer;
import ucigame.Sprite;
import ucigame.Ucigame;
import utilies.ClientMap;

/**
 * Main game class for Pacman.
 *
 */
public class MainGame extends Ucigame{

	private static final long serialVersionUID = -8001318729185074318L;
	Sprite ball;
    Sprite paddle;
    private ServerMenu menu;
    private GameOverMenu gameover;
    private SceneMode currentScene;
    private GameController control;
    
    private final int TCPServerPort = 8899;
    private final int UDPServerPort = 18899;
    public final static int clientPort = 52443;
    private Server server;
    private GameMenu gamescreen;
    private TCPServer connectionServer;
       
    public static final int windowWidth = 600;
    public static final int windowHeight = 650;
    public static final int windowCentreX = windowWidth/2;
    public static final int windowCentreY = windowHeight/2;
    		
    
    /**
	 * Ucigame library requires to call the main method itself again
	 * @param args the list of command line args
	 */
	public static void main(String[] args)
	{
		String[] args2 = new String[1];
		args2[0] = "game.MainGame";
		System.out.println("Current User: " + System.getProperty("user.name") );
		Ucigame.main(args2);
	}
	
	/**
	 * Set up the game settings
	 */
    @Override
    public void setup()
    {
    	initializeWindow();

    	showServermenu();
    }

    /**
     * Initialze the game window
     */
    private void initializeWindow() {
		// set frame rate
		this.framerate(20);
		//Instantiate a global game controller
		control = GameController.setInstance(this);
		// set window size
		window.size(windowWidth, windowHeight);
		// set window title
		window.title("Pac Man Fever");
	}
    
    /**
     * Show server main menu
     */
    private void showServermenu(){
    	// initialize menu
		menu = new ServerMenu(this);
		menu.hideAllClientButton();
		connectionServer = new TCPServer(TCPServerPort, this);
		server = new Server(UDPServerPort, this);

		showScene(SceneMode.SERVERMENU);
    }
    
    /**
     * Show main game screen
     */
    private void showGameScreen(){  
    	gamescreen = new GameMenu(this);
    	control.startGame(); // start the game
    	canvas.background(0, 0, 0);
		showScene(SceneMode.GAME);
    }
    
    /**
     * Show game over screen
     */
    private void showGameOverScreen(){
    	gameover = new GameOverMenu(this);
    	showScene(SceneMode.GAMEOVER);
    }
    
    /**
     * Draw the server menu
     */
    public void drawServermenu(){
    	canvas.clear();
        menu.draw();
    }

    /**
	 * Draws the main game screen
	 */
	public void drawGame() {
		canvas.clear();
		gamescreen.draw();
		control.nextMove();
		control.drawState();
		canvas.font("Arial", BOLD, 30, 255, 100, 100);
		int time = GameState.getInstance().getCounter();
		if(time > 2400)
			sendStopMessage();
		canvas.putText("Time: " + time, 230, 635);		
	}
	
	/**
	 * Draw game over screen
	 */
	public void drawGameover(){
		canvas.clear();
		gameover.draw();
	}
	
	/**
	 * Capture the key actions when in Game mode
	 */
    public void onKeyPressGame()
    {
        // Arrow keys and WASD keys
    	if (keyboard.isDown(keyboard.UP, keyboard.W)){
    		
			control.setPacmenDirection(Direction.UP);

		}else if (keyboard.isDown(keyboard.DOWN, keyboard.S)){
			
			control.setPacmenDirection(Direction.DOWN);
			
		}else if (keyboard.isDown(keyboard.LEFT, keyboard.A)){
			
			control.setPacmenDirection(Direction.LEFT);
			
		}else if (keyboard.isDown(keyboard.RIGHT, keyboard.D)){
			
			control.setPacmenDirection(Direction.RIGHT);
			
		}
    }
    
    /**
	 * Click the start button to start the game
	 */
	public void onClickStart() {
		if (isShowingScene(SceneMode.SERVERMENU)) {
			//startPacManServer();
			if(!ClientMap.isEmpty()){
				System.out.println("Game Start!");
				//send start messages to all clients
				connectionServer.startGame();
				server.addCountersForClients();
				showGameScreen();
			}else{
				System.out.println("cannot start as no client connected.");
			}
		}
	}
    
    /**
     * Click to quit
     */
    public void onClickQuit(){
    	System.exit(0);
    }
    
    public void onClickClientOne(){}
    
	public void onClickClientTwo(){}
	
	public void onClickClientThree(){}
	
	public void onClickClientFour(){}
	
	public void onClickTimeBoard(){}
	
    public void onClickGameOver(){}

    /**
     * Display all connected players on the board
     * @param clients
     */
	public void showConnectedClients(int clients){
		menu.showButton(clients);
	}
	
	/**
	 * Remove a player from the board
	 */
	public void removeClient(){
		menu.showButton(connectionServer.getSize());
		System.out.println("Show "+ connectionServer.getSize() + "clients");
	}
	
	/**
	 * Send game state message to all clients
	 * @param state		game state to be sent
	 */
	public void updateClientState(String state){
		//System.out.println("sent game state: " + state);
		server.sendDataToAll(state.getBytes());
	}
	
	/**
	 * Handle all user inputs from clients
	 * @param address	the IP address from which the client sent messages
	 * @param d			the direction that the user want to move to
	 */
	public void handleUserInput(String address, Direction d){
		control.setPacManDirection(address, d);
	}
	
	/**
	 * Send stop messages to all client to stop the game
	 */
	public void sendStopMessage(){	
		server.sendDataToAll(GameState.getInstance().getStopMessage().getBytes());
		stopGame();
	}
	
	/**
	 * Stop the game and shut down all connections
	 */
	public void stopGame(){
		server.close();
		connectionServer.close();
		showGameOverScreen();
		System.out.println("Stop Game");
	}
	
	/**
	 * Displays the specified scene onto the window, while also storing the
	 * scene for later reference
	 * 
	 * @param scene		the scene to display
	 */
	public void showScene(SceneMode scene) {
		this.currentScene = scene;
		String sceneName = capitalize(scene.toString().toLowerCase());
		super.startScene(sceneName);
	}

	/**
	 * Check if the current scene is currently displayed
	 */
	public boolean isShowingScene(SceneMode scene) {
		return currentScene.equals(scene);
	}
	
	/**
	 * Capitalize the given word
	 * @param s
	 * @return
	 */
	private String capitalize(String s) {
		if (s.length() != 0 && s != null){
			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
		}
		
		return s;
	}
	
	
}
