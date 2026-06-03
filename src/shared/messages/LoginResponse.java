package shared.messages;

import java.io.Serializable;

import shared.User;

public class LoginResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	public User user;

	public LoginResponse(User user) {
		this.user = user;
	}
}
