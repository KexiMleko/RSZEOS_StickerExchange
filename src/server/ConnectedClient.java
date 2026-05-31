package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import shared.User;

public class ConnectedClient implements Runnable{
	private Socket socket; 
	private User user;
	 public ConnectedClient(Socket socket) {
		 this.socket=socket; 
	 }
	 @Override
	 public void run() {
		// TODO Auto-generated method stub
		
	 }
}
