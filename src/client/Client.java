package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {

	public static void main(String args[]){
	    // args give message contents and destination hostname
	    DatagramSocket aSocket = null;
	    try {
	    	aSocket = new DatagramSocket();
	    	String data = "heheh";
	    	byte [] m = data.getBytes();
	    	InetAddress aHost = InetAddress.getByName("localhost");
	    	int serverPort = 6789;
	    	DatagramPacket request =
	    			new DatagramPacket(m,  data.length(), aHost, serverPort);
	    	System.out.println("Sending data to server");
	    	aSocket.send(request);
	    	byte[] buffer = new byte[1000];
	    	DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
	    	System.out.println("Client waiting to receive a response");
	    	aSocket.receive(reply);
	    	System.out.println("Reply: " + new String(reply.getData()));
	    }catch (SocketException e){
	    	System.out.println("Socket: " + e.getMessage());
	    }catch (IOException e){
	    	System.out.println("IO: " + e.getMessage());
	    }finally {
	    	if(aSocket != null) aSocket.close();
	    }
	}
	
}
