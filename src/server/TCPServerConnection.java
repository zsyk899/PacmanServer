package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import utilies.ClientMap;
import utilies.ConnectionMessageQueue;

public class TCPServerConnection {
    BufferedReader in;
    PrintWriter out;
    Socket socket;
    InetAddress address;
    int port;
    int id;

    TCPServerConnection(Socket socket){
        this.socket = socket;
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
        
        this.id = ClientMap.logClient(ClientMap.getAvailableId(), address, port);
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        Thread read = new Thread(){
            public void run(){
                while(true){
                    try{
                        String message = in.readLine();
                        if(message != null)
                        	ConnectionMessageQueue.pushMessage(message);
                    }catch(SocketException se){
                    	ClientMap.removeClient(id);
                    	se.printStackTrace();
                    }catch(IOException e){
                    	e.printStackTrace(); 
                    }
                }
            }
        };

        read.setDaemon(true);
        read.start();
    }

    public String toString(){
    	return address + " " + port + " " + id;
    }
    public void write(String message) {
        out.println(message);
		out.flush();
    }

    @SuppressWarnings("unchecked")
	public void confirmConnection(){
    	JSONObject object = new JSONObject();
    	object.put("request", "201");
    	object.put("id", ""+id);
    	this.write(object.toJSONString());
    	
    }
}
