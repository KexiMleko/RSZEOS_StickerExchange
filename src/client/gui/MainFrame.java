package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.ServerConnection;
import shared.User;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final ServerConnection conn;
	private final User user;

	private final JPanel duplicatesPanel = new JPanel(null);
	private final JPanel missingPanel = new JPanel(null);
	private final JButton deleteDuplicatesButton = new JButton("Obriši");
	private final JButton deleteMissingButton = new JButton("Obriši");
	private final JButton possibleTradesButton = new JButton("Moguće razmene");
	private final JComboBox<String> peersBox = new JComboBox<>();

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
		center.add(buildSection("Sličice koje imam", duplicatesPanel, deleteDuplicatesButton));
		center.add(buildSection("Sličice koje mi trebaju", missingPanel, deleteMissingButton));
		add(center, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel buildSection(String title, JPanel cbPanel, JButton deleteButton) {
		JPanel section = new JPanel(new BorderLayout(0, 5));
		section.setBorder(BorderFactory.createTitledBorder(title));
		section.add(new JLabel(title), BorderLayout.NORTH);
		section.add(cbPanel, BorderLayout.CENTER);
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(deleteButton);
		section.add(bottom, BorderLayout.SOUTH);
		return section;
	}
}
