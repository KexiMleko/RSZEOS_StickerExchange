package client;

import shared.TradeOffer;

public interface ServerListener {
	void onIncomingOffer(TradeOffer offer);
	void onTradeResult(TradeOffer offer, boolean accepted);
}
