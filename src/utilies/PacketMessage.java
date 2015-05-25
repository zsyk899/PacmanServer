package utilies;

import java.net.InetAddress;

public class PacketMessage {

	InetAddress address;
	int port;
	String data;
	
	public PacketMessage(InetAddress address, int port, String data) {
		this.address = address;
		this.port = port;
		this.data = data;
		System.out.println("Received user input: " + data);
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
