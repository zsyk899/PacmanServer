package utilies;

import java.net.InetAddress;
import java.util.ArrayList;

public class ClientMap {

	private static ArrayList<ClientConfig> clients = new ArrayList<ClientConfig>();
		
	public static void logClient(int id, InetAddress address, int port){
		ClientConfig client = new ClientConfig(id, address, port);
		clients.add(client);
	}
	
	public static ArrayList<ClientConfig> getClients(){
		return clients;
	}
	
	public static boolean isEmpty(){
		return clients.isEmpty();
	}
	
	public static int getAvailableId(){
		return clients.size() + 1;
	}
}
