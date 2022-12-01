package com.sajh1s2.espblufi.tcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.util.Log;

public class WorkerRunnable implements Runnable{

    private Socket clientSocket = null;
    private String serverText   = null;
	private String TAG = "WorkerRunnable";

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
        	Log.i(TAG , "bb");
        	BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			Log.i(TAG , "lesson fragment3");
			String answer = input.readLine();
        	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        	out.println("connection ok");
        	out.flush();
        	out.close();
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}
