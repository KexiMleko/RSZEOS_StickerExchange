package shared.messages;

import java.io.Serializable;

import shared.TradeOffer;

public class TradeResult implements Serializable {
	private static final long serialVersionUID = 1L;

	public TradeOffer offer;
	public boolean accepted;

	public TradeResult(TradeOffer offer, boolean accepted) {
		this.offer = offer;
		this.accepted = accepted;
	}
}
