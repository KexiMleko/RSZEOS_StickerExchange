package server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shared.TradeOption;
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

	public List<TradeOption> possibleTradesFor(String username) {
		User currUser = userRegistry.get(username);
		List<TradeOption> options = new ArrayList<>();
		if (currUser == null) {
			return options;
		}
		for (User other : userRegistry.all()) {
			if (other.username.equals(username)) {
				continue;
			}
			Set<Integer> toGive = new HashSet<>(currUser.getDuplicateCards());
			toGive.retainAll(other.getMissingCards());
			Set<Integer> toGet = new HashSet<>(other.getDuplicateCards());
			toGet.retainAll(currUser.getMissingCards());
			if (!toGive.isEmpty() && !toGet.isEmpty()) {
				options.add(new TradeOption(other.username, toGive, toGet));
			}
		}
		return options;
	}
}
