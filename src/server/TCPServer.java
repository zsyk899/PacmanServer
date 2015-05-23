package server;

import game.MainGame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utilies.ConnectionMessageQueue;

public class TCPServer {
	
    private ServerSocket serverSocket;
    private ArrayList<TCPServerConnection> clients;
    private JSONParser parser;
    private MainGame game;

    public TCPServer(int port, MainGame game) {
        try {
			serverSocket = new ServerSocket(port);
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
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        accept.setDaemon(true);
        accept.start();

        Thread messageHandling = new Thread() {
            public void run(){
                while(true){
                    String message = ConnectionMessageQueue.popMessage();
					// Do some handling here...
					System.out.println("Message Received: " + message);
					
                }
            }
        };

        messageHandling.setDaemon(true);
        messageHandling.start();
    }
    
    private void showConnectedClients(int clients){
    	game.showConnectedClients(clients);
    }
    public void parseMessage(String message){
    	try {
			JSONObject object = (JSONObject) parser.parse(message);
			if (object.get("request") == "200"){

			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}
