package shared.messages;

import java.io.Serializable;
import java.util.List;

import shared.TradeOption;

public class PossibleTradesResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<TradeOption> options;

	public PossibleTradesResponse(List<TradeOption> options) {
		this.options = options;
	}
}
