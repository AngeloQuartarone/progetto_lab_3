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
		ClientMain client = new ClientMain();
		client.init("./src/client/clientParameter.properties");
		Socket socket = null;
		// establish a connection
		try {
			socket = new Socket(ipAddr, Integer.parseInt(port));
			System.out.println("Connected");
			while (true) {
				// takes input from terminal
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

				// sends output to the socket
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());

				// takes input from the server
				DataInputStream in = new DataInputStream(socket.getInputStream());

				String line = "";
				while (!line.equals("Over")) {
					line = input.readLine();
					out.writeUTF(line);
					line = in.readUTF();
					System.out.println(line);
				}
				out.writeUTF("Over");
			}

		} catch (UnknownHostException u) {
			System.out.println(u);
			return;
		} catch (IOException i) {
			System.out.println(i);
			return;
		}

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
	}

	void init(String configFile) {
		InputStream input = null;
		try {
			input = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Properties prop = new Properties();

		try {
			prop.load(input);

			// Leggi le proprietà
			ipAddr = prop.getProperty("SERVER_IP");
			port = prop.getProperty("SERVER_PORT");
			System.out.println(ipAddr);
			System.out.println(port);
			input.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
