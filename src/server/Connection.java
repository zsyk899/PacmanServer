package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.parser.JSONParser;

public class Connection extends Thread{
	
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	JSONParser parser;
	boolean isDestination = true;
	File file;
	long timeStamp;
	int blocksize = 1024;
	Server server;
	
	/**
	 * Constructor
	 * 
	 * @param filename
	 * @param aClientSocket
	 */
	public Connection (Socket aClientSocket, final Server server) {
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream( clientSocket.getInputStream());
			out = new DataOutputStream( clientSocket.getOutputStream());
			this.parser = new JSONParser();
			this.server = server;
			this.start();
		} catch(IOException e) {
			System.out.println("Connection:"+e.getMessage());
		}
	
	
		Thread readThread = new Thread(){
			public void run(){		
				while(true){
		            try{
		                String message = in.readUTF();
		                server.putMessage(message);
		            }
		            catch(IOException e){ e.printStackTrace(); }
		        }
			}
			
		};
	
		readThread.setDaemon(true); // terminate when main ends
		readThread.start();
	}
}
