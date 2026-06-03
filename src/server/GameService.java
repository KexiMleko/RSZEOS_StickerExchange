package server;

import shared.User;

public class GameService {
	private final UserRegistry userRegistry = new UserRegistry();
	private final SessionRegistry sessionRegistry = new SessionRegistry();

	public User loginOrRegister(String username, ConnectedClient client) {
		User user = userRegistry.loginOrRegister(username);
		sessionRegistry.add(username, client);
		return user;
	}

	public void logout(String username) {
		sessionRegistry.remove(username);
	}
}
