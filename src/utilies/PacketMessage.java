package utilies;

import java.net.InetAddress;

/**
 * A wrapper class that stores the messages a client sent and informtion about this client
 */
public class PacketMessage {

	InetAddress address;
	int port;
	String data;
	
	public PacketMessage(InetAddress address, int port, String data) {
		this.address = address;
		this.port = port;
		this.data = data;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getData() {
		return data;
	}	
	
}
