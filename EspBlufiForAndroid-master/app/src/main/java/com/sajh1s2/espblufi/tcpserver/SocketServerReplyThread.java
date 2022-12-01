package com.sajh1s2.espblufi.tcpserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class SocketServerReplyThread extends Thread{
	
	private Socket hostThreadSocket;
	private String TAG = "SocketServerReplyThread";
	private String response = "";
	public SocketServerReplyThread(Socket socket) {
		hostThreadSocket = socket;
	}

	private String modbus_transaction_identifier = "";
	private String modbus_protocol_identifier = "";
	private String modbus_message = "";
	private String modbus_unit_id = "";
	private String modbus_function_code = "";
	private String modbus_address = "";
	private String modbus_number_registers = "";
	@Override
	public void run() {
		// TODO Auto-generated method stub
//		super.run();
//		OutputStream outputStream;
		try {
			Log.i(TAG , "yeeeehaaah");
			DataInputStream dataInputStream = new DataInputStream(hostThreadSocket.getInputStream());

			byte[] resultBuff = new byte[0];
			byte[] buff = new byte[128];
			if(dataInputStream!=null) {
				int k = hostThreadSocket.getInputStream().read(buff, 0, buff.length);
				if (k > -1) {
					byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
					System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
					System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
					resultBuff = tbuff; // call the temp buffer as your result buff
					String trama = gettStringTrama(resultBuff);

					System.out.println("0. pilone: resultBuff length -> " + resultBuff.length + " : trama -> " + trama );

					if(resultBuff.length!=12 && resultBuff.length!=8) {
						System.out.println("1. pilone: error en la trama recibida" );
					}
					else {

						if(resultBuff.length==8) {
							//Ejemplo desde sensores de HA: 1, 3, 64, -95, 0, 1, -64, 40
							if(resultBuff[0]==1 && resultBuff[1]==3) {
								modbus_transaction_identifier = "0001";
								modbus_protocol_identifier = "0000";
								modbus_message = "0006";
								modbus_unit_id = String.format("%02X", (byte) resultBuff[0]);
								modbus_function_code = String.format("%02X", (byte) resultBuff[1]);
								modbus_address = String.format("%02X%02X", (byte) resultBuff[2], (byte) resultBuff[3]);
								modbus_number_registers = String.format("%02X%02X", (byte) resultBuff[4], (byte) resultBuff[5]);
							}
							else {
								System.out.println("1. pilone: error mensaje sensores -> " + resultBuff.length + " : " + trama );
							}
						}
						else {
							//Ejemplo desde NodeRed de HA: 0, 1, 0, 0, 0, 6, 1, 3, 64, -94, 0, 1
							modbus_transaction_identifier = String.format("%02X%02X", (byte) resultBuff[0], (byte) resultBuff[1]);
							modbus_protocol_identifier = String.format("%02X%02X", (byte) resultBuff[2], (byte) resultBuff[3]);
							modbus_message = String.format("%02X%02X", (byte) resultBuff[4], (byte) resultBuff[5]);
							modbus_unit_id = String.format("%02X", (byte) resultBuff[6]);
							modbus_function_code = String.format("%02X", (byte) resultBuff[7]);
							modbus_address = String.format("%02X%02X", (byte) resultBuff[8], (byte) resultBuff[9]);
							modbus_number_registers = String.format("%02X%02X", (byte) resultBuff[10], (byte) resultBuff[11]);
						}

						System.out.println("modbus_transaction_identifier: " + modbus_transaction_identifier);
						System.out.println("modbus_protocol_identifier: " + modbus_protocol_identifier);
						System.out.println("modbus_message: " + modbus_message);
						System.out.println("modbus_unit_id: " + modbus_unit_id);
						System.out.println("modbus_function_code: " + modbus_function_code);
						System.out.println("modbus_address: " + modbus_address);
						System.out.println("modbus_number_registers: " + modbus_number_registers);

						int total_registros = (Integer.parseInt(modbus_number_registers, 16) * 2);
						byte[] total_registros_hex = intToByteArray(total_registros + 3);

						String modbus_message_final = (modbus_unit_id + modbus_function_code + modbus_address + modbus_number_registers).toUpperCase();
						System.out.println("1. pilone: modbus_message_final -> " + modbus_message_final);

						byte[] modbus_frame_response = {resultBuff[0], resultBuff[1], resultBuff[2], resultBuff[3], total_registros_hex[1], total_registros_hex[0], resultBuff[6], resultBuff[7]};
						System.out.println("2. pilone: modbus_frame_response -> " + gettStringTrama(modbus_frame_response));
						/*
						0001: Transaction identifier
						0000: Protocol identifier
						0006: Message length (6 bytes to follow)
						15: The unit identifier (17 = 0x15)
						03: The function code (read analog output holding registers)
						006B: The data address of the first register requested (40108 - 40001 offset = 107 = 0x6B).
						0003: The total number of registers requested. (read 3 registers 40108 to 40110)
						*/


						EventBus.getDefault().postSticky(new MessageEvent(modbus_message_final, modbus_frame_response, hostThreadSocket));


						/*byte[] slice = {20, 72, 49, 83, 50, 54, 48, 50, 74, 50, 49, 53, 49, 69, 48, 57, 51, 48, 53, 0, 0};
						byte[] combined = new byte[modbus_frame_response.length + slice.length];

						for (int i = 0; i < combined.length; ++i)
						{
							combined[i] = i < modbus_frame_response.length ? modbus_frame_response[i] : slice[i - modbus_frame_response.length];
						}
						dataoutputStream.flush();
						dataoutputStream.write(combined); //sending response for client socket request
						dataoutputStream.flush();
						*/

					}
				}
			}
			//dataInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        
	}

	private String gettStringTrama(byte[] resultBuff) {
		String trama = "";
		for (int i=0; i<resultBuff.length; i++)
		{
			if (i==0) {
				trama += resultBuff[i];
			}
			else {
				trama += ", " + resultBuff[i];
			}
		}
		return trama;
	}

	public static byte[] intToByteArray(int a)
	{
		byte[] ret = new byte[2];
		ret[0] = (byte) (a & 0xFF);
		ret[1] = (byte) ((a >> 8) & 0xFF);
		return ret;
	}
	/*@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessageResponseEvent(MessageResponseEvent event) throws IOException {
		byte[] response = {72, 49, 83, 50, 54, 48, 50, 74, 50, 49, 53, 49, 69, 48, 57, 51, 48, 53, 0, 0};

		//EventBus.getDefault().postSticky(new MessageResponseEvent(response));
	}*/
}



