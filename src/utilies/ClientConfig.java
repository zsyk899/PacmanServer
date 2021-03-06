package utilies;

import java.net.InetAddress;

/**
 * 
 * Used to store the connection information of a connected client
 *
 */
public class ClientConfig {

	InetAddress address;
	int port;
	int id;
	
	public ClientConfig(int id, InetAddress address, int port){
		this.id = id;
		this.address = address;
		this.port = port;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public int getId(){
		return id;
	}
	
	
}
