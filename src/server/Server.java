package server;

import game.Direction;
import game.MainGame;
import game.SceneMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import menu.ServerMenu;
import controller.GameController;
import ucigame.Sprite;
import ucigame.Ucigame;
import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.MessageQueue;
import utilies.PacketMessage;

/**
 * 
 * This UDP server handles communication with all clients
 *
 */
public class Server{
		
	DatagramSocket serverSocket;
	EventHandler eventHandler;
	MainGame game;
	boolean isRunning = false;
	
	/**
	 * Construct a client instance
	 * 
	 * @param ads	IP address
	 * @param p		IP port
	 */
	public Server(int serverPort, MainGame game){
		try {
			this.game = game;
			serverSocket = new DatagramSocket(serverPort);
			this.isRunning = true;
			System.out.println("UDP server port: " + serverSocket.getLocalPort() + " address: " + serverSocket.getLocalAddress());
			
			//create a thread for receiving data
			Thread read = new Thread(){
				
				public void run(){
					while(isRunning){
					    //System.out.println("Received: "+ message) ;
		            	try{
			            	PacketMessage message = receiveData();
		            		MessageQueue.pushMessage(message);			
					    } catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					    	System.out.println("Socket closed");
					    	break;

						} catch(NullPointerException e){
					    	//e.printStackTrace();
					    	System.out.println("Nothing received");
					    	break;
					    } 		   
					}
				}				
			};
			read.setDaemon(true);
			read.start();
			
			//create the event handler
			eventHandler = new EventHandler(game);
			eventHandler.setDaemon(true);
			eventHandler.start();
			
			System.out.println("SERVER START");
		} catch (SocketException se){
			
			se.printStackTrace();
			
		}
	}
	
	/**
	 * Send data using UDP protocol
	 * @param buf		the data to be sent
	 * @param address	the address of the client to be sent to
	 * @param port		the port of the client to be sent to
	 */
    public void sendData(byte[] buf, InetAddress address, int port){
		try{
			DatagramPacket packet = new DatagramPacket( buf, buf.length, address, port);
			serverSocket.send(packet);
			//serverSocket.close();
		}catch(Exception e){
			// TODO: error? the server is not responding
		}
	}
    
    /**
     * Send data to all clients using UDP protocol
     * @param buf	the data to be sent
     */
    public void sendDataToAll(byte[] buf){
    	for(ClientConfig client: ClientMap.getClients()){
    		//System.out.println("Sent data to client " + client.getId() + " with address " + client.getAddress() + " and port " + client.getPort());
    		sendData(buf, client.getAddress(), client.getPort());
    	}
    }
    
    /**
     * Listen for a port and receive message if there is any.
     * 
     * @return	message if successfully received any
     * @throws SocketException
     */
    public PacketMessage receiveData() throws SocketException{
    	PacketMessage result = null;
		try{
			byte[] receiveData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(packet);			
			result = new PacketMessage(packet.getAddress(), packet.getPort(), new String(packet.getData()).trim());
			
		}catch(Exception e){
			// TODO: error? the server is not responding
//			e.printStackTrace();
		}		
		return result;
	}
    
    /**
     * Add counters for all players
     */
    public void addCountersForClients(){
    	eventHandler.createCounters();
    }
    
    /**
     * Close the UDP server
     */
    public void close(){
    	isRunning = false;
    	serverSocket.close();
    }
}
