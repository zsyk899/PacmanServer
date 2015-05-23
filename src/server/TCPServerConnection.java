package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import utilies.ConnectionMessageQueue;

public class TCPServerConnection {
    BufferedReader in;
    PrintWriter out;
    Socket socket;

    TCPServerConnection(Socket socket){
        this.socket = socket;
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
                    }catch(IOException e){
                    	e.printStackTrace(); 
                    }
                }
            }
        };

        read.setDaemon(true);
        read.start();
    }

    public void write(String message) {
        out.println(message);
		out.flush();
    }

    @SuppressWarnings("unchecked")
	public void confirmConnection(){
    	JSONObject object = new JSONObject();
    	object.put("reply", "200");    	
    	this.write(object.toJSONString());
    	
    }
}
