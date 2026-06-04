package client;

import javax.swing.SwingUtilities;

import client.gui.LoginFrame;

public class Client {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}
}
