package shared.messages;

import java.io.Serializable;

import shared.TradeOffer;

public class TradeOfferRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public TradeOffer offer;

	public TradeOfferRequest(TradeOffer offer) {
		this.offer = offer;
	}
}
