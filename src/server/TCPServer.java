package server;

import game.MainGame;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.ConnectionMessageQueue;

/**
 * This TCP server accepts connections from clients and handle messages from them
 *
 */
public class TCPServer {
	
    private ServerSocket serverSocket;
    private ArrayList<TCPServerConnection> clients;
    private JSONParser parser;
    private MainGame game;
    Thread accept;
    Thread messageHandler;
    boolean isRunning = false;
    
    public TCPServer(int port, MainGame game) {
        try {
			serverSocket = new ServerSocket(port);
			System.out.println("TCP server port: " + serverSocket.getLocalPort() + " address: " + serverSocket.getInetAddress());

			clients = new ArrayList<TCPServerConnection>();
			this.game = game;
	    	parser = new JSONParser();
	    	this.isRunning = true;

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        /*
         * Create a thread listening for connections from clients
         */
        accept = new Thread() {
            public void run(){
                while(ClientMap.getSize() < 4){
                    try{
                        Socket s = serverSocket.accept();
                        TCPServerConnection newConnection = new TCPServerConnection(s);
                        clients.add(newConnection);
                        newConnection.confirmConnection();
                        showConnectedClients(clients.size());
                        if(newConnection != null)
                        	System.out.println("client added, with address:" + s.getInetAddress() + "there are " + clients + " connected.");
                		for(ClientConfig config: ClientMap.getClients()){
                			System.out.println("address: " + config.getAddress() + " port: " + config.getPort());
                		}
                        //System.out.println(clients.size());
                    }catch(SocketException se){
                    	se.printStackTrace(); 
                    	System.out.println("Socket closed");
                    	break;
                    }
                    catch(IOException e){ 
                    	e.printStackTrace(); 
                    	break;
                    }
                }
            }
        };

        accept.setDaemon(true);
        accept.start();

        /*
         * Hand received message
         */
        messageHandler = new Thread() {
            public void run(){
            	while(isRunning){
            		try{
	        		String message = ConnectionMessageQueue.popMessage();
					// Do some handling here...
					if(message!=null)
						parseMessage(message);
					}catch(NullPointerException e){
						break;
					}
				}
            }
        };

        messageHandler.setDaemon(true);
        messageHandler.start();
    }
    
    /**
     * Show all connected clients on the screen board
     * @param clients
     */
    private void showConnectedClients(int clients){
    	game.showConnectedClients(clients);
    }
    
    /**
     * Process the received message
     * @param message
     */
    public void parseMessage(String message){
    	try {
    		//System.out.println("parsing");
			JSONObject object = (JSONObject) parser.parse(message);
			if(object.containsKey("request")){
				int code = Integer.valueOf(((Long) object.get("request")).intValue());
				System.out.println("code: " + code);

				switch(code){
					case 101:
						//StatusCode.DISCONNECT
						if(object.containsKey("address")){
							String address = (String)object.get("address");
							diconnectClient(address);
							System.out.println("Received disconnect message, client with IP address: "
							+ address + "removed. There are " + clients + " " + ClientMap.getSize()+ " connected");
						}
						break;
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    /**
     * Disconnect a client if received disconnect message from it
     * @param address
     */
	private void diconnectClient(String address) {
		// TODO Auto-generated method stub

		ClientMap.removeClient(address);
		TCPServerConnection target = null;
		for(TCPServerConnection connection: clients){

			if(connection.address.getHostAddress().equals(address)){
				target = connection;

			}
		}
		
		target.close();
		clients.remove(target);
		
		game.removeClient();
	}
	
	/**
	 * Close all TCP connections
	 */
	public void close(){

		try {
			isRunning = false;
			for(TCPServerConnection connection: clients){
				connection.close();
			}
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Send start game message to all clients
	 */
	public void startGame(){
		for(TCPServerConnection connection: clients){
			connection.startGame();
		}
	}
	
	/**
	 * 
	 * @return the number of connected clients
	 */
	public int getSize(){
		return clients.size();
	}
}
