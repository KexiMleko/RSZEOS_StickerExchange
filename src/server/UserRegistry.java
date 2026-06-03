package server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import shared.User;

public class UserRegistry {
	private final Map<String, User> users = new ConcurrentHashMap<>();

	public User loginOrRegister(String username) {
		return users.computeIfAbsent(username, User::createNew);
	}

	public User get(String username) {
		return users.get(username);
	}

	public boolean contains(String username) {
		return users.containsKey(username);
	}

	public Collection<User> all() {
		return users.values();
	}
}
