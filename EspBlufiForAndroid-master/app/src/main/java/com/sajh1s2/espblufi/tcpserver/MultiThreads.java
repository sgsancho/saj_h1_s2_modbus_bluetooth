package com.sajh1s2.espblufi.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreads extends Thread {

	int port = 8080;

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		super.run();
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(true){
				Socket socket = serverSocket.accept();
				SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(socket);
				socketServerReplyThread.run();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
