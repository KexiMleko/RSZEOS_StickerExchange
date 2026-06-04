package client.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import client.ServerConnection;
import shared.User;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JTextField usernameField = new JTextField(15);
	private final JButton loginButton = new JButton("Prijavi se");

	public LoginFrame() {
		setTitle("Menjaža - Prijava");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(10, 10));

		JPanel form = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row.add(new JLabel("Korisničko ime:"));
		row.add(usernameField);
		form.add(row);

		JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonRow.add(loginButton);
		form.add(buttonRow);

		add(form, BorderLayout.CENTER);

		loginButton.addActionListener(e -> doLogin());

		pack();
		setLocationRelativeTo(null);
	}

	private void doLogin() {
		String username = usernameField.getText().trim();
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Unesite korisničko ime.", "Greška", JOptionPane.WARNING_MESSAGE);
			return;
		}
		ServerConnection conn = new ServerConnection();
		try {
			User user = conn.connect("localhost", 9000, username);
			dispose();
			MainFrame main = new MainFrame(conn, user);
			main.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Neuspešna prijava: " + ex.getMessage(), "Greška",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}
}
