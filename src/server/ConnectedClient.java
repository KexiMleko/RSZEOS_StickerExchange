package server;

import java.net.Socket;

import shared.User;

public class ConnectedClient implements Runnable {
	private Socket socket;
	private User user;
	private final GameService gameService;

	public ConnectedClient(Socket socket, GameService gameService) {
		this.socket = socket;
		this.gameService = gameService;
	}

	@Override
	public void run() {
		while (true) {
			if (user == null) {

			}
		}
	}
}
