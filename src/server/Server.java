package server;

import game.Direction;
import game.MainGame;
import game.SceneMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import menu.MainMenu;
import menu.ServerMenu;
import controller.GameController;
import ucigame.Sprite;
import ucigame.Ucigame;

public class Server{
		
	private ArrayList<Connection> clients;
    private LinkedBlockingQueue<String> messages;
    private ServerSocket serverSocket;
    private boolean listening = true;
    private boolean playing = true;
    private Server server;
    
	public Server(int port){
		try{
			serverSocket = new ServerSocket(port);
			clients = new ArrayList<Connection>();
			messages = new LinkedBlockingQueue<String>();
			server = this;
			//create a thread listening connections from client
			Thread accept = new Thread() {
	            public void run(){
	                while(listening){
	                    try{
	                    	//accept the connection and add to the list
	                        Socket s = serverSocket.accept();
	                        clients.add(new Connection(s, server));
	                    }
	                    catch(IOException e){ e.printStackTrace(); }
	                }
	            }
	        };

	        accept.setDaemon(true);
	        accept.start();

	        Thread messageHandling = new Thread() {
	            public void run(){
	                while(playing){
	                    try{
	                        String message = messages.take();
	                        // Do some handling here...
	                        System.out.println("Message Received: " + message);
	                    }
	                    catch(InterruptedException e){ }
	                }
	            }
	        };
	        
	        messageHandling.setDaemon(true);
	        messageHandling.start();
	        
	        System.out.println("START SERVER");
		}catch(IOException e){
			e.printStackTrace();
		}
    }	
	
	public void putMessage(String message){
		try {
			messages.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
