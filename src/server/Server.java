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

public class Server{
		
	DatagramSocket serverSocket;
	
	/**
	 * Construct a client instance
	 * 
	 * @param ads	IP address
	 * @param p		IP port
	 */
	public Server(int serverPort){
		try {
			
			serverSocket = new DatagramSocket(serverPort);
			System.out.println("UDP server port: " + serverSocket.getLocalPort());
			
			Thread read = new Thread(){
				
				public void run(){
					while(true){
						DatagramPacket messagePack = receiveData();
						
						//System.out.println(ClientMap.getAvailableId());
						System.out.println(messagePack.getAddress());
						System.out.println(messagePack.getPort());
						//ClientMap.logClient(ClientMap.getAvailableId(), messagePack.getAddress(), messagePack.getPort());
						System.out.println(new String(messagePack.getData()));
					}
				}				
			};

			read.start();
			
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
    		System.out.println("Sent data to client " + client.getId() + " with address " + client.getAddress() + " and port " + client.getPort());
    		sendData(buf, client.getAddress(), client.getPort());
    	}
    }
    
    public DatagramPacket receiveData(){
    	DatagramPacket result = null;
		try{
			byte[] receiveData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(packet);
			//serverSocket.close();
			result = packet;
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
