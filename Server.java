import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	//Runnable rende la classe passabile ad un thread
	
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private ExecutorService pool;
	
	public Server() {
		connections = new ArrayList<>();
	}
	
	@Override
	public void run() { //metodo eseguito quando la classe runnable e inizializzata
		try {
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			while(true) { //forse meglio usare flag boolean
				Socket client = server.accept(); //acceto richiesta connesione
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler); //assegna thread che poi eseguira la funzione run ovviamente
			}
		} catch (IOException e) {
			//TODO: Handle con shutdown()
		}
	}
	
	public void broadcast(String message) {
		for(ConnectionHandler ch : connections) { //forEach connection in connections
			if(ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	//TODO: Integrare funzione shutdown
	//TODO: Integrare funzione shutdown per tutti i client con forEach e metodo shutdown in ConnectionHandler

	/*
	public void shutdown() {
		if(!server.isClosed()) {
			server.close();
		}
	}
	*/
	
	class ConnectionHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;
		
		public ConnectionHandler(Socket client) {
			this.client = client;
		}
		
		@Override
		public void run() { 
			try {
				out = new PrintWriter(client.getOutputStream(), true); //true attiva l'autoflush dello stream
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				out.println("Enter Nickname: ");
				nickname = in.readLine();
				//TODO: Controllo nickname valido
				
				System.out.println(nickname + " connected");
				broadcast(nickname + " joined the chat");
				
				String message;
				while ((message = in.readLine()) != null) {
					if(message.startsWith("/nick ")) {
						String[] messageSplit = message.split(" ", 2);
						if(messageSplit.length == 2) {
							broadcast(nickname + " ha cambiaton il proprio nickname in: " + messageSplit[1]);
							System.out.println(nickname + " ha cambiaton il proprio nickname in: " + messageSplit[1]);
							nickname = messageSplit[1];
							out.println("Nickname cambiato con sussesso! :)");
						} else {
							out.println("Nessun nickname inserito :(");
						}
					}
					
					else if(message.startsWith("/exit ")) {
						//TODO: Handle exit con shutdown
						broadcast(nickname + " ha abbandonato la chat");
					} else {
						broadcast(nickname + ": " + message);
					}
				}
				
			} catch(IOException e) {
				//TODO: Handle con shutdown
			}
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
		//TODO: Integrare funzione shutdown per tutti i client con forEach e metodo shutdown in ConnectionHandler
		//TODO: chiudere in e out
		
		/*
		public void shutdown() {
			if(!client.isClosed()) {
				client.close();
			}
		}
		*/
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}
}
