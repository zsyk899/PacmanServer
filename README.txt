-------------------------------------------------------------------------------
Multiplar Pacman 
-------------------------------------------------------------------------------
Date: 26/05/2015
-------------------------------------------------------------------------------
Team: 
Siyuan Zhang 		  668506
Ivan Matheu Malo Robinson 661332
Chibin Zhang		  616795

-------------------------------------------------------------------------------
Project description

This project aims to implement a distributed algorithm in a multiplayer computer
game. The algorithm we choose is the Lamport Algorithm, which used for the event
ordering part of the game. The game is designed in a client-server architecture 
where client simply acts as a monitor displaying the game and taking inputs from
the player while server will handle the game itself including collision detection
and state update.

The game is designed with a MVC model. Model defines all behaviours of a object,
View renders the object on the screen and Controller handles all other stuffs.
-------------------------------------------------------------------------------
Third-Party Libraries

json-simple-1.1.1.jar:

	Description: A simple JAVA toolkit for JSON. 
	It can be used to encode and decode JSON String.
	
	Code License: Apache License 2.0 All Right reserved.
	
	Source: https://code.google.com/p/json-simple/

ucigame.jar :

	Description: A open-sourced simple java game library.
	It can be easily used for 


-------------------------------------------------------------------------------
File Description

/src/server:
	Server.java - A UDP server used to communicate with the clients
	TCPServer.java - A TCP server used to build connections with the clients
	TCPServerConnection.java - handles the message reading and writing for each client

/src/controller:
	GameController - Defines all functions used in the game loop
	WallController - Defines all functions used for walls (collision detection and stop behavior)
	
/src/game:
	Direction - An enum class defines all directions that a player is able to move to
	GameState.java - Manages all objects in the game
	MainGame.java - The main class that manages all scene transitions and configurations
	SceneMode - An enum class defines all scenes that the game has

/src/menu:
	GameMenu.java - the game screen (contains timer)
	GameOverScreen.java - show a game over board
	ServerMenu.java - the main screen where waits for clients to connect
/src/model
	ControllableObject.java - defines all behaviours of a controllable object
	Ghost.java - a ghost that can catch pacman but due to time limit, it is
			not implemented in the game
	Pacman.java - a pacman that player can control
	Wall.java - a wall

/src/msg	 
	Msg.java - helps to bind the counter value to the message
	MsgFactory.java - factory class for Msg

src/utilities
	ConnectionMessageQueue.java - a global blocking FIFO queue that used to store connection requests	
	MessageQueue.java - a global blocking FIFO queue that used to store game update messages
	ClientConfig.java - used to save IP address and Port information of each client
	ClientMap.java - a map of ClientConfig
	PacketMessage.java - a wrapper class that stores both message data and information about the message sender
	StatusCode.java - stores all status codes used in the communication
	


/resources/
	all images used in the game

/lib
	json-simple-1.1.1.jar - an external library used to encode and decode JSON
	ucigame.jar - a java game library
	
	-------------------------------------------------------------------------------
Installation instructions

(It is hard to run the game from terminal as it requires some external libraries
and a single jar file is not appropriate since some setting values are hard-coded)

Import the source file into a JAVA IDE.

1. Create a new Java Project
2. Import the Zip file into the Project
3. Open "lib" folder and right-click on the json-simple-1.1.1.jar and ucigame.jar, 
	follows Build Path -> Add to Build Path.
	

-------------------------------------------------------------------------------