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
			
			parseMessage(data, address);
			
		}
	}

	/**
	 * Initialize counters for all players
	 */
	public void createCounters(){
		System.out.println("clients: " + ClientMap.getSize());

		for(ClientConfig config: ClientMap.getClients()){
			counters.put(config.getAddress().getHostAddress(), 0);
		}
	}
	
	/**
	 * Update the counter value
	 * @param address	the counter of the player with given address will be updated
	 * @param counter	the new value of the counter
	 */
	private void updateCounter(String address, int counter){
		if(counters.containsKey(address)){
			counters.put(address, counter);
		}
	}
	
	/**
	 * Parse the messages from the clients and handle them according to the corresponding counter values
	 * @param message
	 * @param address
	 */
	private void parseMessage(String message, String address) {
		
		try {
			JSONObject msg = (JSONObject) parser.parse(message);
			if(msg.containsKey("counter")&&msg.containsKey("instruction")){
				//counter returned from client
				int counter = Integer.valueOf(((Long) msg.get("counter")).intValue());
				
				/*
				 * If the counter value indicate that the received message is the latest one that perform the action,
				 * otherwise discard it
				 */
				if(counter > counters.get(address)){
					JSONObject data = (JSONObject) parser.parse((String) msg.get("instruction"));
					if(data.containsKey("request")){
						int code = Integer.valueOf(((Long) data.get("request")).intValue());
						if(code == StatusCode.USER_INPUT && data.containsKey("direction")){
							Direction d = Direction.valueOf((String) data.get("direction"));
								updateCounter(address, counter);
								game.handleUserInput(address, d);
						}
					}
				}else{
					//display if some messages are discarded
					System.out.println("This message is discarded: " + msg + " " + address);
					System.out.println(counters.toString());
				}
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
