package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import shared.User;

public class Server {
	public int port;
	private ServerSocket serverSocket;
	private ArrayList<ConnectedClient> clients;
	private ArrayList<User> users;

	public Server(int port) {
		this.port=port;
		clients=new ArrayList<>();
		users=new ArrayList<>();
		try {
			serverSocket=new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptClients() {
		while(true) {
			try {
				Socket clientSocket=serverSocket.accept();
				ConnectedClient client= new ConnectedClient(clientSocket);
				clients.add(client);
				Thread thr=new Thread(client);
				thr.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	public static void main(String[] args) throws IOException {
		Server server=new Server(9000);
		server.acceptClients();
	}
}
