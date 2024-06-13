package client;

// A Java program for a Client
import java.io.*;
import java.net.*;
import java.util.Properties;

public class ClientMain {

	private static String ipAddr = "";
	private static String port = "";
	// initialize socket and input output streams
	private static Socket socket = null;
	// private static DataInputStream in = null;
	// private static DataOutputStream out = null;

	public static void main(String args[]) {
		//ClientMain client = new ClientMain();
		//client.init("./src/client/clientParameter.properties");
		// establish a connection
		

		// close the connection
		/*
		 * try {
		 * in.close();
		 * out.close();
		 * socket.close();
		 * } catch (IOException i) {
		 * System.out.println(i);
		 * }
		 */
		HOTELIERCustomerClient client = new HOTELIERCustomerClient();
		client.runCLI();
		return;
		
	}

	
}
