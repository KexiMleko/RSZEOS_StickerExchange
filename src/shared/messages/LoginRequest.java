package shared.messages;

import java.io.Serializable;

public class LoginRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public String username;

	public LoginRequest(String username) {
		this.username = username;
	}
}
