package net.x666c.simplereddit.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

public class CallbackServer {
	
	// I have no idea how this works but i'll try :)
	
	public static void main(String[] args) throws Exception { // Moved main() here so i can test without starting reddit/bot
		new CallbackServer().listen();
	}
	
	
	final ServerSocket socket;
	
	public CallbackServer() throws Exception {
		socket = new ServerSocket(80);
	}
	
	private void listen() throws Exception {
		Socket connection;
		connection = socket.accept();
		System.out.println("Connected to VK (hopefully), ip: " + connection.getInetAddress());
		
		String payload = readPayload(connection);
		
		System.out.println("Read some text:");
		System.out.println(payload);
		
		try {
			//JSONObject input = new JSONObject(payload); // If didn't throw an exception, then we know for sure that payload is some JSON
			System.out.println("Got a JSON!");
			
			PrintWriter out = new PrintWriter(connection.getOutputStream()); // Create an output stream
			
			final String msg = "POST / HTTP/1.1Host: 217.15.202.178User-Agent: curl/7.52.1 1515c4a2";
			
			out.write(msg); // Write the hardcoded response
			System.out.println("Wrote the message: " + msg);
			
			connection.close(); // Now we can close the connection (or maybe not xd)
		} catch (Exception e) {
			System.err.println("Payload is not a JSON"); // Oh no
		}
	}
	
	private String readPayload(Socket s) throws Exception {
		/*BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		
		char read = (char)in.read(); // Skip until JSON
		while(read != '{') {
			read = (char)in.read();
		}
		
		System.out.println(in.lines().reduce("{", String::concat));*/
		
		return "";
	}

}