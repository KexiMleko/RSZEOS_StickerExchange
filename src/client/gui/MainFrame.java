package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.ServerConnection;
import shared.User;

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

	private final HashMap<String, JCheckBox> duplicateBoxes = new HashMap<>();
	private final HashMap<String, JCheckBox> missingBoxes = new HashMap<>();

	public MainFrame(ServerConnection conn, User user) {
		this.conn = conn;
		this.user = user;

		setTitle("Menjaža - " + user.username);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(5, 5));
		setPreferredSize(new Dimension(900, 500));

		JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		topBar.add(possibleTradesButton);
		topBar.add(peersBox);
		add(topBar, BorderLayout.NORTH);

		JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
		center.add(buildSection("Moji duplikati", duplicatesPanel, deleteDuplicatesButton));
		center.add(buildSection("Sličice koje mi trebaju", missingPanel, deleteMissingButton));
		add(center, BorderLayout.CENTER);

		renderCheckboxes(user.getDuplicateCards(), duplicatesPanel, duplicateBoxes);
		renderCheckboxes(user.getMissingCards(), missingPanel, missingBoxes);

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
		for (int i = 0; i < sorted.length; i++) {
			int num = sorted[i];
			JCheckBox cb = new JCheckBox(String.valueOf(num));
			int row = i / COLS;
			int col = i % COLS;
			cb.setBounds(col * CB_WIDTH, row * CB_HEIGHT, CB_WIDTH, CB_HEIGHT);
			boxMap.put("Btn" + num, cb);
			panel.add(cb);
		}
		panel.revalidate();
		panel.repaint();
	}
}
