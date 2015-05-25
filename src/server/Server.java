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

import menu.MainMenu;
import menu.ServerMenu;
import controller.GameController;
import ucigame.Sprite;
import ucigame.Ucigame;
import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.MessageQueue;
import utilies.PacketMessage;

public class Server{
		
	DatagramSocket serverSocket;
	MainGame game;
	
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
			System.out.println("UDP server port: " + serverSocket.getLocalPort() + " address: " + serverSocket.getLocalAddress());
			
			Thread read = new Thread(){
				
				public void run(){
					while(true){
						PacketMessage message = receiveData();
						
						MessageQueue.pushMessage(message);
						//System.out.println(ClientMap.getAvailableId());						
					}
				}				
			};
			read.setDaemon(true);
			read.start();
			
			EventHandler eventHandler = new EventHandler(game);
			eventHandler.setDaemon(true);
			eventHandler.start();
			
			System.out.println("SERVER START");
		} catch (SocketException se){
			
			se.printStackTrace();
			
		}
	}
	
    public void sendData(byte[] buf, InetAddress address, int port){
		try{
			DatagramPacket packet = new DatagramPacket( buf, buf.length, address, port);
			serverSocket.send(packet);
			//serverSocket.close();
		}catch(Exception e){
			// TODO: error? the server is not responding
		}
	}
    
    public void sendDataToAll(byte[] buf){
    	for(ClientConfig client: ClientMap.getClients()){
    		//System.out.println("Sent data to client " + client.getId() + " with address " + client.getAddress() + " and port " + client.getPort());
    		sendData(buf, client.getAddress(), client.getPort());
    	}
    }
    
    public PacketMessage receiveData(){
    	PacketMessage result = null;
		try{
			byte[] receiveData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(packet);
			//serverSocket.close();
			System.out.println(packet.getAddress());
			System.out.println(packet.getPort());
			//ClientMap.logClient(ClientMap.getAvailableId(), messagePack.getAddress(), messagePack.getPort());
			
			result = new PacketMessage(packet.getAddress(), packet.getPort(), new String(packet.getData()).trim());
			
		}catch(Exception e){
			// TODO: error? the server is not responding
			e.printStackTrace();
		}		
		return result;
	}
    
    public void close(){
    	serverSocket.close();
    }
}
