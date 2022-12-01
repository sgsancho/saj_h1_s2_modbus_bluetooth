package com.sajh1s2.espblufi.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


import android.util.Log;

public class MultiThreadedServer implements Runnable{

	protected int	serverPort = 8080;
	protected ServerSocket	serverSocket = null;
    protected boolean	isStopped = false;
    protected Thread	runningThread = null;
	private String TAG = "MultiThreadServer";

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
            	Log.i(TAG , "thread1");
                clientSocket = this.serverSocket.accept();
                Log.i(TAG , "thread2222");
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            Log.i(TAG , "thread1");
            new Thread(new WorkerRunnable(clientSocket, "Multithreaded Server")).start();
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

}
