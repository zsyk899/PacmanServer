package server;

import game.Direction;
import game.MainGame;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	
	public EventHandler(MainGame game){
		this.parser = new JSONParser();
		this.game = game;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			
			PacketMessage message = MessageQueue.popMessage();
			String data = message.getData();
			String address = message.getAddress().getHostAddress();
			int port = message.getPort();	
			
			parseMessage(data, address);
			
		}
	}

	private void parseMessage(String message, String address) {
		
		try {
			JSONObject data = (JSONObject) parser.parse(message);
			if(data.containsKey("request")){
				int code = Integer.valueOf(((Long) data.get("request")).intValue());
				if(code == StatusCode.USER_INPUT && data.containsKey("direction")){
					Direction d = Direction.valueOf((String) data.get("direction"));
					game.handleUserInput(address, d);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
