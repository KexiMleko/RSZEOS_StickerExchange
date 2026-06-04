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

import client.ServerConnection;
import shared.TradeOption;
import shared.User;
import shared.messages.RemoveStickersRequest.ListType;

public class MainFrame extends JFrame {
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
	private final JTextArea tradeMessage = new JTextArea(2, 60);

	private final HashMap<String, JCheckBox> duplicateBoxes = new HashMap<>();
	private final HashMap<String, JCheckBox> missingBoxes = new HashMap<>();

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

	private void renderCheckboxes(Set<Integer> numbers, JPanel panel, HashMap<String, JCheckBox> boxMap) {
		int[] sorted = numbers.stream().mapToInt(Integer::intValue).sorted().toArray();
		for (int num : sorted) {
			JCheckBox cb = new JCheckBox(String.valueOf(num));
			boxMap.put("Btn" + num, cb);
			panel.add(cb);
		}
		relayout(panel, boxMap);
	}

	private void relayout(JPanel panel, HashMap<String, JCheckBox> boxMap) {
		int[] sorted = boxMap.keySet().stream()
				.mapToInt(k -> Integer.parseInt(k.substring(3)))
				.sorted()
				.toArray();
		for (int i = 0; i < sorted.length; i++) {
			JCheckBox cb = boxMap.get("Btn" + sorted[i]);
			int row = i / COLS;
			int col = i % COLS;
			cb.setBounds(col * CB_WIDTH, row * CB_HEIGHT, CB_WIDTH, CB_HEIGHT);
		}
		panel.revalidate();
		panel.repaint();
	}

	private void deleteSelected(ListType list, JPanel panel, HashMap<String, JCheckBox> boxMap, Set<Integer> userSet) {
		List<String> toRemove = new ArrayList<>();
		Set<Integer> removedNumbers = new HashSet<>();
		for (Map.Entry<String, JCheckBox> entry : boxMap.entrySet()) {
			if (entry.getValue().isSelected()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String key : toRemove) {
			JCheckBox cb = boxMap.remove(key);
			panel.remove(cb);
			int num = Integer.parseInt(key.substring(3));
			userSet.remove(num);
			removedNumbers.add(num);
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
		} catch (IOException | ClassNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
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
}
