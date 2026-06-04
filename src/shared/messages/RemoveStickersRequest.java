package shared.messages;

import java.io.Serializable;
import java.util.Set;

public class RemoveStickersRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum ListType {
		DUPLICATES, MISSING
	}

	public ListType list;
	public Set<Integer> numbers;

	public RemoveStickersRequest(ListType list, Set<Integer> numbers) {
		this.list = list;
		this.numbers = numbers;
	}
}
