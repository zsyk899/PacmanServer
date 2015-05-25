package utilies;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientMap {

	private static ArrayList<ClientConfig> clients = new ArrayList<ClientConfig>();
	private static int counter = 0;
		
	public static int logClient(InetAddress address, int port){
		ClientConfig client = new ClientConfig(counter, address, port);
		clients.add(client);
		counter++;
		return client.getId();
	}
	
	public static ArrayList<ClientConfig> getClients(){
		return clients;
	}
	
	public static boolean isEmpty(){
		return clients.isEmpty();
	}
	
	public static void removeClient(InetAddress address, int port){
		for(ClientConfig client:clients){
			if(client.getAddress().equals(address) && client.getPort() == port){
				clients.remove(client);
			}
			
		}
	}
	
	public static void removeClient(String address){
		Iterator<ClientConfig> iterator = clients.iterator();
		while(iterator.hasNext()){
			ClientConfig client = iterator.next();
			if(client.address.getHostAddress().equals(address)){
				iterator.remove();
			}
		}
	}
	
	public static void removeClient(int id){
		Iterator<ClientConfig> iterator = clients.iterator();
		while(iterator.hasNext()){
			ClientConfig client = iterator.next();
			if(client.id == id){
				iterator.remove();
			}
		}
	}
	
	public static int getSize(){
		return clients.size();
	}
	
	
}
