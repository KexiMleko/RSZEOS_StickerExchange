package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import shared.TradeOffer;
import shared.TradeOption;
import shared.User;
import shared.messages.IncomingTradeOffer;
import shared.messages.LoginRequest;
import shared.messages.LoginResponse;
import shared.messages.PossibleTradesRequest;
import shared.messages.PossibleTradesResponse;
import shared.messages.RemoveStickersRequest;
import shared.messages.RemoveStickersRequest.ListType;
import shared.messages.TradeDecisionRequest;
import shared.messages.TradeOfferRequest;
import shared.messages.TradeResult;

public class ServerConnection {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private User user;
	private Thread readerThread;
	private ServerListener listener;

	private final BlockingQueue<PossibleTradesResponse> possibleTradesQueue = new LinkedBlockingQueue<>();

	public User connect(String host, int port, String username) throws IOException, ClassNotFoundException {
		socket = new Socket(host, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());

		out.writeObject(new LoginRequest(username));
		out.flush();
		LoginResponse resp = (LoginResponse) in.readObject();
		this.user = resp.user;

		readerThread = new Thread(this::readLoop, "ServerConnection-reader");
		readerThread.setDaemon(true);
		readerThread.start();

		return user;
	}

	public User getUser() {
		return user;
	}

	public void setListener(ServerListener listener) {
		this.listener = listener;
	}

	public void removeStickers(ListType list, Set<Integer> numbers) throws IOException {
		out.writeObject(new RemoveStickersRequest(list, numbers));
		out.flush();
	}

	public void sendTradeOffer(TradeOffer offer) throws IOException {
		out.writeObject(new TradeOfferRequest(offer));
		out.flush();
	}

	public void sendTradeDecision(TradeOffer offer, boolean accepted) throws IOException {
		out.writeObject(new TradeDecisionRequest(offer, accepted));
		out.flush();
	}

	public List<TradeOption> requestPossibleTrades() throws IOException, InterruptedException {
		out.writeObject(new PossibleTradesRequest());
		out.flush();
		return possibleTradesQueue.take().options;
	}

	public void close() throws IOException {
		socket.close();
	}

	private void readLoop() {
		try {
			while (true) {
				Object msg = in.readObject();
				dispatch(msg);
			}
		} catch (EOFException e) {
		} catch (IOException | ClassNotFoundException e) {
			if (!socket.isClosed()) {
				e.printStackTrace();
			}
		}
	}

	private void dispatch(Object msg) {
		if (msg instanceof PossibleTradesResponse) {
			possibleTradesQueue.offer((PossibleTradesResponse) msg);
		} else if (msg instanceof IncomingTradeOffer) {
			if (listener != null) {
				listener.onIncomingOffer(((IncomingTradeOffer) msg).offer);
			}
		} else if (msg instanceof TradeResult) {
			TradeResult r = (TradeResult) msg;
			if (listener != null) {
				listener.onTradeResult(r.offer, r.accepted);
			}
		}
	}
}
