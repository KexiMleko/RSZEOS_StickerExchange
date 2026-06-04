package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.List;
import java.util.Set;

import shared.TradeOption;
import shared.User;
import shared.messages.LoginRequest;
import shared.messages.LoginResponse;
import shared.messages.PossibleTradesRequest;
import shared.messages.PossibleTradesResponse;
import shared.messages.RemoveStickersRequest;
import shared.messages.RemoveStickersRequest.ListType;

public class ServerConnection {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private User user;

	public User connect(String host, int port, String username) throws IOException, ClassNotFoundException {
		socket = new Socket(host, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());

		out.writeObject(new LoginRequest(username));
		out.flush();
		LoginResponse resp = (LoginResponse) in.readObject();
		this.user = resp.user;
		return user;
	}

	public User getUser() {
		return user;
	}

	public void removeStickers(ListType list, Set<Integer> numbers) throws IOException {
		out.writeObject(new RemoveStickersRequest(list, numbers));
		out.flush();
	}

	public List<TradeOption> requestPossibleTrades() throws IOException, ClassNotFoundException {
		out.writeObject(new PossibleTradesRequest());
		out.flush();
		PossibleTradesResponse resp = (PossibleTradesResponse) in.readObject();
		return resp.options;
	}

	public void close() throws IOException {
		socket.close();
	}
}
