package com.sajh1s2.espblufi.tcpserver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sajh1s2.espblufi.ui.SajActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.sajh1s2.espblufi.ui.SajActivity.postSajData;

public class ServerService extends Service {
	
	private String TAG = "ServerService";
	private byte[] response_modbus;
	IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ServerService getServerServiceInstance() {
			return ServerService.this;
		}
	}

	public void setResponseModbus(byte[] response_modbus) {
		this.response_modbus = response_modbus;
	}

	public byte[] getResponseModbus() {
		return this.response_modbus;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
		System.out.println("0. pilone: EventBus register Service");
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		EventBus.getDefault().unregister(this);
		System.out.println("0. pilone: EventBus unregister Service");
	}
/*
	public void onEvent(SetSongList event){
		// do something with event
	}*/

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(MessageEvent event) {
		System.out.println("3. pilone: onMessageEvent ThreadMode.MAIN");
		//Toast.makeText(getApplicationContext(), "Nuevo mensaje recibido: " + event.getMessageModBus(), Toast.LENGTH_LONG).show();
		SajActivity.socket = event.getSocket();
		SajActivity.modbus_message = event.getMessageModBus();
		SajActivity.modbus_frame_response = event.getFrameResponseModBus();
		postSajData(event.getMessageModBus());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//MultiThreadedServer server = new MultiThreadedServer();
		MultiThreads server = new MultiThreads();
		new Thread(server).start();
		Log.i(TAG , "sss");
		return START_STICKY;
	}




}
