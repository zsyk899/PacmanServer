package server;

import game.MainGame;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.ConnectionMessageQueue;

public class TCPServer {
	
    private ServerSocket serverSocket;
    private ArrayList<TCPServerConnection> clients;
    private JSONParser parser;
    private MainGame game;
    
    public TCPServer(int port, MainGame game) {
        try {
			serverSocket = new ServerSocket(port);
			System.out.println("TCP server port: " + serverSocket.getLocalPort());

			clients = new ArrayList<TCPServerConnection>();
			this.game = game;
	    	parser = new JSONParser();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        Thread accept = new Thread() {
            public void run(){
                while(true){
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
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        accept.setDaemon(true);
        accept.start();

        Thread messageHandler = new Thread() {
            public void run(){
            	while(true){
	        		String message = ConnectionMessageQueue.popMessage();
					// Do some handling here...
					if(message!=null)
						parseMessage(message);
				}
            }
        };

        messageHandler.setDaemon(true);
        messageHandler.start();
    }
    
    private void showConnectedClients(int clients){
    	game.showConnectedClients(clients);
    }
    
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
	
	public void startGame(){
		for(TCPServerConnection connection: clients){
			connection.startGame();
		}
	}
	
	public int getSize(){
		return clients.size();
	}
}
