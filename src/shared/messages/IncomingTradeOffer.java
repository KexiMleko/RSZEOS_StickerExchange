package shared.messages;

import java.io.Serializable;

import shared.TradeOffer;

public class IncomingTradeOffer implements Serializable {
	private static final long serialVersionUID = 1L;

	public TradeOffer offer;

	public IncomingTradeOffer(TradeOffer offer) {
		this.offer = offer;
	}
}
