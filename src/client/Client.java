package client;

import shared.User;

public class Client {

	public static void main(String[] args) throws Exception {
		String username = args.length > 0 ? args[0] : "miki";
		ServerConnection conn = new ServerConnection();
		User user = conn.connect("localhost", 9000, username);
		System.out.println("Logged in as: " + user.username);
		System.out.println("Duplicates: " + user.getDuplicateCards());
		System.out.println("Missing:    " + user.getMissingCards());
		conn.close();
	}
}
