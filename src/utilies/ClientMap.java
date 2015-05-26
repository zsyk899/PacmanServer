package utilies;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A global class that stores all connection information of all clients
 *
 */
public class ClientMap {

	private static ArrayList<ClientConfig> clients = new ArrayList<ClientConfig>();
	private static int counter = 0;
		
	/**
	 * Log the connection information of a client
	 * @param address
	 * @param port
	 * @return
	 */
	public static int logClient(InetAddress address, int port){
		ClientConfig client = new ClientConfig(counter, address, port);
		clients.add(client);
		counter++;
		return client.getId();
	}
	
	/**
	 * @return information of all connected clients
	 */
	public static ArrayList<ClientConfig> getClients(){
		return clients;
	}
	
	/**
	 * @return true if there is any clients being logged
	 */
	public static boolean isEmpty(){
		return clients.isEmpty();
	}
	
	/**
	 * Remove the client if it is disconnected
	 * @param address	the address of the client that is disconnected
	 * @param port		the port of the client that is disconnected
	 */
	public static void removeClient(InetAddress address, int port){
		for(ClientConfig client:clients){
			if(client.getAddress().equals(address) && client.getPort() == port){
				clients.remove(client);
			}
			
		}
	}
	
	/**
	 * Remove the client if it is disconnected
	 * @param address	the address of the client that is disconnected
	 */
	public static void removeClient(String address){
		Iterator<ClientConfig> iterator = clients.iterator();
		while(iterator.hasNext()){
			ClientConfig client = iterator.next();
			if(client.address.getHostAddress().equals(address)){
				iterator.remove();
			}
		}
	}
	
	/**
	 * Remove the client if it is disconnected
	 * @param id	the id of the client that is disconnected
	 */
	public static void removeClient(int id){
		Iterator<ClientConfig> iterator = clients.iterator();
		while(iterator.hasNext()){
			ClientConfig client = iterator.next();
			if(client.id == id){
				iterator.remove();
			}
		}
	}
	
	/**
	 * 
	 * @return the number of logged clients 
	 */
	public static int getSize(){
		return clients.size();
	}
	
	
}
