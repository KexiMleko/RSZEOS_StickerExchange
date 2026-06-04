package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.User;
import shared.messages.LoginRequest;
import shared.messages.LoginResponse;

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

	public void close() throws IOException {
		socket.close();
	}
}
