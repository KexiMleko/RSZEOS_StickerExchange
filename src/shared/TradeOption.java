package shared;

import java.io.Serializable;
import java.util.Set;

public class TradeOption implements Serializable {
	private static final long serialVersionUID = 1L;

	public String peerUsername;
	public Set<Integer> toGive;
	public Set<Integer> toGet;

	public TradeOption(String peerUsername, Set<Integer> toGive, Set<Integer> toGet) {
		this.peerUsername = peerUsername;
		this.toGive = toGive;
		this.toGet = toGet;
	}
}
