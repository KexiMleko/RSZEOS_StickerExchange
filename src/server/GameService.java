package server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;

import shared.TradeOffer;
import shared.TradeOption;
import shared.User;
import shared.messages.IncomingTradeOffer;
import shared.messages.RemoveStickersRequest.ListType;
import shared.messages.TradeResult;

public class GameService {
	private final UserRegistry userRegistry = new UserRegistry();
	private final SessionRegistry sessionRegistry = new SessionRegistry();

	public User loginOrRegister(String username, ConnectedClient client) {
		User user = userRegistry.loginOrRegister(username);
		if(sessionRegistry.contains(username)){
			ConnectedClient old=sessionRegistry.get(username);
			old.disconnect();
		}
		
		sessionRegistry.add(username, client);
		return user;
	}

	public void logout(String username, ConnectedClient self) {
		sessionRegistry.removeIfSame(username, self);
	}

	public void removeStickers(String username, ListType list, Set<Integer> numbers) {
		User user = userRegistry.get(username);
		if (user == null) {
			return;
		}
		Set<Integer> target = list == ListType.DUPLICATES ? user.getDuplicateCards() : user.getMissingCards();
		target.removeAll(numbers);
	}

	public void proposeTrade(TradeOffer offer) {
		ConnectedClient recipient = sessionRegistry.get(offer.recipient);
		ConnectedClient initiator = sessionRegistry.get(offer.initiator);
		if (recipient == null) {
			notify(initiator, new TradeResult(offer, false));
			return;
		}
		try {
			recipient.send(new IncomingTradeOffer(offer));
		} catch (IOException e) {
			e.printStackTrace();
			notify(initiator, new TradeResult(offer, false));
		}
	}

	public void respondToTrade(TradeOffer offer, boolean accepted) {
		if (accepted) {
			applyTrade(offer);
		}
		TradeResult result = new TradeResult(offer, accepted);
		notify(sessionRegistry.get(offer.initiator), result);
		notify(sessionRegistry.get(offer.recipient), result);
	}

	private synchronized void applyTrade(TradeOffer offer) {
		User initiator = userRegistry.get(offer.initiator);
		User recipient = userRegistry.get(offer.recipient);
		if (initiator == null || recipient == null) {
			return;
		}
		initiator.getDuplicateCards().removeAll(offer.initiatorGives);
		initiator.getMissingCards().removeAll(offer.recipientGives);
		recipient.getDuplicateCards().removeAll(offer.recipientGives);
		recipient.getMissingCards().removeAll(offer.initiatorGives);
	}

	private void notify(ConnectedClient client, Object msg) {
		if (client == null) {
			return;
		}
		try {
			client.send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<TradeOption> possibleTradesFor(String username) {
		User currUser = userRegistry.get(username);
		List<TradeOption> options = new ArrayList<>();
		if (currUser == null) {
			return options;
		}
		for (User other : userRegistry.all()) {
			if (other.username.equals(username)) {
				continue;
			}
			Set<Integer> toGive = new HashSet<>(currUser.getDuplicateCards());
			toGive.retainAll(other.getMissingCards());
			Set<Integer> toGet = new HashSet<>(other.getDuplicateCards());
			toGet.retainAll(currUser.getMissingCards());
			if (!toGive.isEmpty() && !toGet.isEmpty()) {
				options.add(new TradeOption(other.username, toGive, toGet));
			}
		}
		return options;
	}
}
