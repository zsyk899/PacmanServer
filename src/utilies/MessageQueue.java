package utilies;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * An wrapper class for a FIFO blocking queue. 
 * It provides some static methods for accessing the queue.
 * It is used by TCP server to store the connection messages that the client sent.
 */
public class MessageQueue {
	
	/**
	 * A Thread-safe queue that shared by multiple threads
	 */
	public static LinkedBlockingQueue<PacketMessage> messages = new LinkedBlockingQueue<PacketMessage>();
	
	/**
	 * Add given message to the queue
	 * @param message	The message to be added to the queue
	 */
	public static void pushMessage(PacketMessage message){
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
	public static PacketMessage popMessage(){
		
		PacketMessage message = null;
		try {
			message = messages.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}
}
