package utilies;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
	
	/**
	 * A Thread-safe queue that shared by multiple threads
	 */
	public static LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>();
	
	/**
	 * Add given message to the queue
	 * @param message	The message to be added to the queue
	 */
	public static void pushMessage(String message){
		try {
			messages.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Pop the first item in the queue
	 * 
	 * @return	the oldest message
	 */
	public static String popMessage(){
		
		String message = null;
		try {
			message = messages.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}
}
