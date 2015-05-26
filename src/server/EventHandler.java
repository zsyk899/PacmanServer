package server;

import java.util.HashMap;

import game.Direction;
import game.MainGame;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utilies.ClientConfig;
import utilies.ClientMap;
import utilies.MessageQueue;
import utilies.PacketMessage;
import utilies.StatusCode;

/**
 * This class handles all messages from clients
 * @author Siyuan Zhang
 *
 */
public class EventHandler extends Thread {

	JSONParser parser;
	MainGame game;
	HashMap<String, Integer> counters;
	
	public EventHandler(MainGame game){
		this.parser = new JSONParser();
		this.game = game;
		this.counters = new HashMap<String, Integer>();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			
			PacketMessage message = MessageQueue.popMessage();
			String data = message.getData();
			String address = message.getAddress().getHostAddress();
//			int port = message.getPort();	
			
			parseMessage(data, address);
			
		}
	}

	public void createCounters(){
		System.out.println("clients: " + ClientMap.getSize());

		for(ClientConfig config: ClientMap.getClients()){
			counters.put(config.getAddress().getHostAddress(), 0);
		}
	}
	
	private void incrementCounter(String address, int counter){
		if(counters.containsKey(address)){
			counters.put(address, counter);
		}
	}
	
	private void parseMessage(String message, String address) {
		
		try {
			JSONObject msg = (JSONObject) parser.parse(message);
			System.out.println("received message: " + msg + " " + address);
			System.out.println(counters.toString());
			if(msg.containsKey("counter")&&msg.containsKey("instruction")){
				//counter returned from client
				int counter = Integer.valueOf(((Long) msg.get("counter")).intValue());
				if(counter > counters.get(address)){
					JSONObject data = (JSONObject) parser.parse((String) msg.get("instruction"));
					if(data.containsKey("request")){
						int code = Integer.valueOf(((Long) data.get("request")).intValue());
						if(code == StatusCode.USER_INPUT && data.containsKey("direction")){
							Direction d = Direction.valueOf((String) data.get("direction"));
								System.out.println(counter + " updated");
								incrementCounter(address, counter);
								game.handleUserInput(address, d);
						}
					}
				}
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
