package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import shared.User;
import shared.messages.LoginRequest;
import shared.messages.LoginResponse;
import shared.messages.PossibleTradesRequest;
import shared.messages.PossibleTradesResponse;
import shared.messages.RemoveStickersRequest;
import shared.messages.TradeDecisionRequest;
import shared.messages.TradeOfferRequest;

public class ConnectedClient implements Runnable {
	private final Socket socket;
	private final GameService gameService;
	private User user;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ConnectedClient(Socket socket, GameService gameService) {
		this.socket = socket;
		this.gameService = gameService;
	}

	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());

			LoginRequest req = (LoginRequest) in.readObject();
			user = gameService.loginOrRegister(req.username, this);
			out.writeObject(new LoginResponse(user));
			out.flush();

			while (true) {
				Object msg = in.readObject();
				handle(msg);
			}
		} catch (EOFException | SocketException e) {
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (user != null) {
				gameService.logout(user.username, this);
			}
			closeSocket();
		}
	}

	private void handle(Object msg) throws IOException {
		if (msg instanceof RemoveStickersRequest) {
			RemoveStickersRequest r = (RemoveStickersRequest) msg;
			gameService.removeStickers(user.username, r.list, r.numbers);
		} else if (msg instanceof PossibleTradesRequest) {
			out.writeObject(new PossibleTradesResponse(gameService.possibleTradesFor(user.username)));
			out.flush();
		} else if (msg instanceof TradeOfferRequest) {
			gameService.proposeTrade(((TradeOfferRequest) msg).offer);
		} else if (msg instanceof TradeDecisionRequest) {
			TradeDecisionRequest r = (TradeDecisionRequest) msg;
			gameService.respondToTrade(r.offer, r.accepted);
		}
	}

	public void send(Object msg) throws IOException {
		synchronized (out) {
			out.writeObject(msg);
			out.flush();
		}
	}
	public void disconnect(){
		closeSocket();
	}

	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
