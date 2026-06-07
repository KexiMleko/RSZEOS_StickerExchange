package shared.messages;

import java.io.Serializable;

import shared.TradeOffer;

public class TradeDecisionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public TradeOffer offer;
	public boolean accepted;

	public TradeDecisionRequest(TradeOffer offer, boolean accepted) {
		this.offer = offer;
		this.accepted = accepted;
	}
}
