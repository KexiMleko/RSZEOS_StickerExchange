package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import client.ServerConnection;
import client.ServerListener;
import shared.TradeOffer;
import shared.TradeOption;
import shared.User;
import shared.messages.RemoveStickersRequest.ListType;

public class MainFrame extends JFrame implements ServerListener {
	private static final long serialVersionUID = 1L;

	private static final int CB_WIDTH = 50;
	private static final int CB_HEIGHT = 25;
	private static final int COLS = 8;

	private final ServerConnection conn;
	private final User user;

	private final JPanel duplicatesPanel = new JPanel(null);
	private final JPanel missingPanel = new JPanel(null);
	private final JButton deleteDuplicatesButton = new JButton("Obriši");
	private final JButton deleteMissingButton = new JButton("Obriši");
	private final JButton possibleTradesButton = new JButton("Moguće razmene");
	private final JComboBox<String> peersBox = new JComboBox<>();
	private final JButton sendOfferButton = new JButton("Razmeni");
	private final JTextArea tradeMessage = new JTextArea(2, 60);

	private final HashMap<Integer, JCheckBox> duplicateBoxes = new HashMap<>();
	private final HashMap<Integer, JCheckBox> missingBoxes = new HashMap<>();

	private List<TradeOption> currentOptions = new ArrayList<>();

	public MainFrame(ServerConnection conn, User user) {
		this.conn = conn;
		this.user = user;

		setTitle("Menjaža - " + user.username);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(5, 5));
		setPreferredSize(new Dimension(900, 500));

		JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		topRow.add(possibleTradesButton);
		topRow.add(peersBox);
		topRow.add(sendOfferButton);

		tradeMessage.setEditable(false);
		tradeMessage.setLineWrap(true);
		tradeMessage.setWrapStyleWord(true);
		tradeMessage.setBackground(getBackground());
		tradeMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

		JPanel top = new JPanel(new BorderLayout());
		top.add(topRow, BorderLayout.NORTH);
		top.add(tradeMessage, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);

		JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
		center.add(buildSection("Moji duplikati", duplicatesPanel, deleteDuplicatesButton));
		center.add(buildSection("Sličice koje mi trebaju", missingPanel, deleteMissingButton));
		add(center, BorderLayout.CENTER);

		renderCheckboxes(user.getDuplicateCards(), duplicatesPanel, duplicateBoxes);
		renderCheckboxes(user.getMissingCards(), missingPanel, missingBoxes);

		deleteDuplicatesButton.addActionListener(e -> deleteSelected(ListType.DUPLICATES, duplicatesPanel, duplicateBoxes, user.getDuplicateCards()));
		deleteMissingButton.addActionListener(e -> deleteSelected(ListType.MISSING, missingPanel, missingBoxes, user.getMissingCards()));
		possibleTradesButton.addActionListener(e -> fetchPossibleTrades());
		peersBox.addActionListener(e -> showSelectedTradeMessage());
		sendOfferButton.addActionListener(e -> sendSelectedOffer());

		conn.setListener(this);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel buildSection(String title, JPanel cbPanel, JButton deleteButton) {
		JPanel section = new JPanel(new BorderLayout(0, 5));
		section.setBorder(BorderFactory.createTitledBorder(title));
		section.add(cbPanel, BorderLayout.CENTER);
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(deleteButton);
		section.add(bottom, BorderLayout.SOUTH);
		return section;
	}

	private void renderCheckboxes(Set<Integer> numbers, JPanel panel, HashMap<Integer, JCheckBox> boxMap) {
		for (int num : numbers.stream().mapToInt(Integer::intValue).sorted().toArray()) {
			JCheckBox cb = new JCheckBox(String.valueOf(num));
			boxMap.put(num, cb);
			panel.add(cb);
		}
		relayout(panel, boxMap);
	}

	private void relayout(JPanel panel, HashMap<Integer, JCheckBox> boxMap) {
		int[] sorted = boxMap.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
		for (int i = 0; i < sorted.length; i++) {
			JCheckBox cb = boxMap.get(sorted[i]);
			int row = i / COLS;
			int col = i % COLS;
			cb.setBounds(col * CB_WIDTH, row * CB_HEIGHT, CB_WIDTH, CB_HEIGHT);
		}
		panel.revalidate();
		panel.repaint();
	}

	private void deleteSelected(ListType list, JPanel panel, HashMap<Integer, JCheckBox> boxMap, Set<Integer> userSet) {
		Set<Integer> removedNumbers = new HashSet<>();
		for (Map.Entry<Integer, JCheckBox> entry : boxMap.entrySet()) {
			if (entry.getValue().isSelected()) {
				removedNumbers.add(entry.getKey());
			}
		}
		for (int num : removedNumbers) {
			JCheckBox cb = boxMap.remove(num);
			panel.remove(cb);
			userSet.remove(num);
		}
		if (!removedNumbers.isEmpty()) {
			try {
				conn.removeStickers(list, removedNumbers);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		relayout(panel, boxMap);
	}

	private void fetchPossibleTrades() {
		try {
			currentOptions = conn.requestPossibleTrades();
		} catch (IOException | InterruptedException ex) {
			JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
			Thread.currentThread().interrupt();
			return;
		}
		peersBox.removeAllItems();
		tradeMessage.setText("");
		if (currentOptions.isEmpty()) {
			tradeMessage.setText("Nema mogućih razmena.");
			return;
		}
		for (TradeOption opt : currentOptions) {
			int count = Math.min(opt.toGive.size(), opt.toGet.size());
			peersBox.addItem(opt.peerUsername + " (" + count + " sličica)");
		}
	}

	private void showSelectedTradeMessage() {
		int idx = peersBox.getSelectedIndex();
		if (idx < 0 || idx >= currentOptions.size()) {
			return;
		}
		TradeOption opt = currentOptions.get(idx);
		tradeMessage.setText(
				"Možeš da menjaš sličice sa korisnikom " + opt.peerUsername + ".\n"
				+ "Ti imaš za njega sličice " + formatSet(opt.toGive)
				+ ", a on za tebe sličice " + formatSet(opt.toGet));
	}

	private String formatSet(Set<Integer> set) {
		return set.stream().sorted().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("");
	}

	private void sendSelectedOffer() {
		int idx = peersBox.getSelectedIndex();
		if (idx < 0 || idx >= currentOptions.size()) {
			return;
		}
		TradeOption opt = currentOptions.get(idx);
		TradeOffer offer = new TradeOffer(user.username, opt.peerUsername, opt.toGive, opt.toGet);
		try {
			conn.sendTradeOffer(offer);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onIncomingOffer(TradeOffer offer) {
		SwingUtilities.invokeLater(() -> {
			String msg = "Korisnik " + offer.initiator + " želi da menja sa tobom.\n"
					+ "On daje: " + formatSet(offer.initiatorGives) + "\n"
					+ "Ti daješ: " + formatSet(offer.recipientGives) + "\n\n"
					+ "Prihvati razmenu?";
			int choice = JOptionPane.showConfirmDialog(this, msg, "Ponuda za razmenu", JOptionPane.YES_NO_OPTION);
			boolean accepted = choice == JOptionPane.YES_OPTION;
			try {
				conn.sendTradeDecision(offer, accepted);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	@Override
	public void onTradeResult(TradeOffer offer, boolean accepted) {
		SwingUtilities.invokeLater(() -> {
			if (!accepted) {
				JOptionPane.showMessageDialog(this, "Razmena nije prihvaćena.", "Razmena", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			boolean iAmInitiator = user.username.equals(offer.initiator);
			Set<Integer> myGave = iAmInitiator ? offer.initiatorGives : offer.recipientGives;
			Set<Integer> myGot = iAmInitiator ? offer.recipientGives : offer.initiatorGives;
			applyLocally(myGave, myGot);
			JOptionPane.showMessageDialog(this, "Razmena uspešna.", "Razmena", JOptionPane.INFORMATION_MESSAGE);
		});
	}

	private void applyLocally(Set<Integer> gave, Set<Integer> got) {
		for (int num : gave) {
			JCheckBox cb = duplicateBoxes.remove(num);
			if (cb != null) {
				duplicatesPanel.remove(cb);
			}
			user.getDuplicateCards().remove(num);
		}
		for (int num : got) {
			JCheckBox cb = missingBoxes.remove(num);
			if (cb != null) {
				missingPanel.remove(cb);
			}
			user.getMissingCards().remove(num);
		}
		relayout(duplicatesPanel, duplicateBoxes);
		relayout(missingPanel, missingBoxes);
	}
}
