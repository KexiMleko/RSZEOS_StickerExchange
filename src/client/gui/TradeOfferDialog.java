package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import shared.TradeOffer;
import shared.TradeOption;

public class TradeOfferDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final String myUsername;
	private final TradeOption opt;
	private final Map<Integer, JCheckBox> giveBoxes = new HashMap<>();
	private final Map<Integer, JCheckBox> getBoxes = new HashMap<>();
	private final JLabel countLabel = new JLabel();
	private final JButton sendButton = new JButton("Pošalji");
	private final JButton cancelButton = new JButton("Otkaži");

	private boolean sent = false;
	private TradeOffer result;

	public TradeOfferDialog(JFrame owner, TradeOption opt, String myUsername) {
		super(owner, "Razmena sa korisnikom " + opt.peerUsername, true);
		this.opt = opt;
		this.myUsername = myUsername;

		setLayout(new BorderLayout(5, 5));
		setPreferredSize(new Dimension(600, 400));

		JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
		center.add(buildSection("Sličice koje daješ", opt.toGive, giveBoxes));
		center.add(buildSection("Sličice koje dobijaš", opt.toGet, getBoxes));
		add(center, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		bottom.add(countLabel);
		bottom.add(sendButton);
		bottom.add(cancelButton);
		add(bottom, BorderLayout.SOUTH);

		sendButton.addActionListener(e -> onSend());
		cancelButton.addActionListener(e -> dispose());

		updateCount();
		pack();
		setLocationRelativeTo(owner);
	}

	private JPanel buildSection(String title, Set<Integer> numbers, Map<Integer, JCheckBox> map) {
		JPanel grid = new JPanel(new GridLayout(0, 5, 4, 4));
		for (int num : numbers.stream().mapToInt(Integer::intValue).sorted().toArray()) {
			JCheckBox cb = new JCheckBox(String.valueOf(num), true);
			cb.addActionListener(e -> updateCount());
			map.put(num, cb);
			grid.add(cb);
		}
		JPanel section = new JPanel(new BorderLayout());
		section.setBorder(BorderFactory.createTitledBorder(title));
		section.add(new JScrollPane(grid), BorderLayout.CENTER);
		return section;
	}

	private void updateCount() {
		int giveCount = countSelected(giveBoxes);
		int getCount = countSelected(getBoxes);
		boolean valid = giveCount == getCount && giveCount > 0;
		countLabel.setText("Daješ: " + giveCount + "   Dobijaš: " + getCount + (valid ? "" : "  (nejednako)"));
		sendButton.setEnabled(valid);
	}

	private int countSelected(Map<Integer, JCheckBox> map) {
		int n = 0;
		for (JCheckBox cb : map.values()) {
			if (cb.isSelected()) {
				n++;
			}
		}
		return n;
	}

	private Set<Integer> collectSelected(Map<Integer, JCheckBox> map) {
		Set<Integer> set = new HashSet<>();
		for (Map.Entry<Integer, JCheckBox> entry : map.entrySet()) {
			if (entry.getValue().isSelected()) {
				set.add(entry.getKey());
			}
		}
		return set;
	}

	private void onSend() {
		Set<Integer> give = collectSelected(giveBoxes);
		Set<Integer> get = collectSelected(getBoxes);
		if (give.size() != get.size() || give.isEmpty()) {
			return;
		}
		result = new TradeOffer(myUsername, opt.peerUsername, give, get);
		sent = true;
		dispose();
	}

	public boolean isSent() {
		return sent;
	}

	public TradeOffer getOffer() {
		return result;
	}
}
