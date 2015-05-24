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
                        System.out.println("client added");
                        System.out.println(clients.size());
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
					if(message!=null)
						parseMessage(message);
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
    		System.out.println("parsing");
			JSONObject object = (JSONObject) parser.parse(message);
			if(object.containsKey("request")){
				int code = Integer.parseInt((String) object.get("request"));
				System.out.println("code" + code);

				switch(code){
					case 101:
						if(object.containsKey("address")){
							String address = (String)object.get("address");
							System.out.println("InetAddress: " + address);
							diconnectClient(address);
							System.out.println("Received disconnect message, client removed");
							System.out.println(clients);
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
		game.removeClient();
		System.out.println("Client " + address + " removed");

		clients.remove(target);
		System.out.println("size:" + clients.size());
		
//		Iterator<TCPServerConnection> iterator = clients.iterator();
//		System.out.println(iterator.toString());
//		while(iterator.hasNext()){
//			TCPServerConnection connection = iterator.next();
//			if(connection.address == address){
//				iterator.remove();
//				game.removeClient();
//				System.out.println("Client " + address + " removed");
//			}
//		}
		

	}
	
	public int getClients(){
		return clients.size();
	}
}
