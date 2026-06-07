package shared;

import java.io.Serializable;
import java.util.Set;

public class TradeOffer implements Serializable {
	private static final long serialVersionUID = 1L;

	public String initiator;
	public String recipient;
	public Set<Integer> initiatorGives;
	public Set<Integer> recipientGives;

	public TradeOffer(String initiator, String recipient, Set<Integer> initiatorGives, Set<Integer> recipientGives) {
		this.initiator = initiator;
		this.recipient = recipient;
		this.initiatorGives = initiatorGives;
		this.recipientGives = recipientGives;
	}
}
