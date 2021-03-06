package server;

import game.MainGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.ConnectionMessageQueue;
import utilies.StatusCode;

/**
 * This TCP connection class handles all communication with a TCP client
 *
 */
public class TCPServerConnection {
    BufferedReader in;
    PrintWriter out;
    Socket socket;
    InetAddress address;
    int port;
    int id;
    Thread read;
    boolean isRunning = false;

    TCPServerConnection(Socket socket){
        this.socket = socket;
        this.address = socket.getInetAddress();
        //this.port = socket.getPort();
        
        this.id = ClientMap.logClient(address, MainGame.clientPort);
        this.isRunning = true;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        //create a thread reading messages from the input stream
        read = new Thread(){
            public void run(){
                while(isRunning){
                    try{
                    	if(in.ready()){
                            String message = in.readLine();
                            if(message != null)
                				System.out.println("Message Received: " + message);
                            	ConnectionMessageQueue.pushMessage(message);
                    	}
                    	Thread.sleep(100);
                    }catch (InterruptedException e) {
						// TODO Auto-generated catch block
                    	isRunning = false;
						e.printStackTrace();
					} catch(SocketException se){
						isRunning = false;
                    	ClientMap.removeClient(id);
                    	se.printStackTrace();
                    }catch(IOException e){
                    	isRunning = false;
                    	e.printStackTrace(); 
                    } 
                }
                
                System.out.println("server connection die");
            }
        };

        read.setDaemon(true);
        read.start();
    }

    public String toString(){
    	return address + " " + port + " " + id;
    }
    
    /**
     * Write message to the output stream
     * @param message	message to be sent
     */
    public void write(String message) {
        out.println(message);
		out.flush();
    }

    /**
     * Send connection confirmation to clients
     */
    @SuppressWarnings("unchecked")
	public void confirmConnection(){
    	JSONObject object = new JSONObject();
    	object.put("request", StatusCode.CONNECTION_SUCCESS);
    	this.write(object.toJSONString());
    	
    }
    
    /**
     * Send start game message to clients
     */
    @SuppressWarnings("unchecked")
	public void startGame(){
    	JSONObject object = new JSONObject();
    	object.put("request", StatusCode.START_GAME);
    	JSONArray players = new JSONArray();
    	for(ClientConfig client: ClientMap.getClients()){
        	JSONObject player = new JSONObject();
        	player.put("id", client.getId());
        	players.add(player);
    	}
    	object.put("players", players);
    	object.put("num", ClientMap.getSize());
    	this.write(object.toJSONString());
    }
        
    /**
     * Close the TCP connection
     */
    public void close(){
    	try {
    		isRunning = false;
    		read.join();
	    	socket.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	
    }
}
