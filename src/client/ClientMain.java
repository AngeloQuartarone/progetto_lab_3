package client;
// A Java program for a Client
import java.io.*;
import java.net.*;

public class ClientMain {
    // initialize socket and input output streams
    private static Socket socket = null;
    private static DataInputStream input = null;
    private static DataOutputStream out = null;
    private static String address = "localhost";
    private static int port = 5983;
    
    public static void main(String args[])
	{
        // establish a connection
		try {
			socket = new Socket(address, port);
			System.out.println("Connected");

			// takes input from terminal
			input = new DataInputStream(System.in);

			// sends output to the socket
			out = new DataOutputStream(
				socket.getOutputStream());
		}
		catch (UnknownHostException u) {
			System.out.println(u);
			return;
		}
		catch (IOException i) {
			System.out.println(i);
			return;
		}

		// string to read message from input
		String line = "";

		// keep reading until "Over" is input
		while (!line.equals("Over")) {
			try {
				line = input.readLine();
				out.writeUTF(line);
			}
			catch (IOException i) {
				System.out.println(i);
			}
		}

		// close the connection
		try {
			input.close();
			out.close();
			socket.close();
		}
		catch (IOException i) {
			System.out.println(i);
		}
	}
}
