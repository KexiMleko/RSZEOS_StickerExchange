package shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	public String username;
	private Set<Integer> duplicateCards;
	private Set<Integer> missingCards;

	public User(String username, HashSet<Integer> duplicateCards, HashSet<Integer> missingCards) {
		this.username=username;
		this.duplicateCards=duplicateCards;
		this.missingCards=missingCards;
	}

	public static User createNew(String username) {
		Random r = new Random();

		List<Integer> pool = IntStream.rangeClosed(1, 99)
				.boxed()
				.collect(Collectors.toList());
		Collections.shuffle(pool, r);

		int dupCount = r.nextInt(20) + 1;
		int missingCount = r.nextInt(20) + 1;

		HashSet<Integer> duplicates = new HashSet<>(pool.subList(0, dupCount));
		HashSet<Integer> missing = new HashSet<>(pool.subList(dupCount, dupCount + missingCount));

		return new User(username, duplicates, missing);
	}

	public Set<Integer> getDuplicateCards() {
		return duplicateCards;
	}

	public Set<Integer> getMissingCards() {
		return missingCards;
	}
}
