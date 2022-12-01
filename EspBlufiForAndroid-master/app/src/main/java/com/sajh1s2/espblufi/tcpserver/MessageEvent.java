package com.sajh1s2.espblufi.tcpserver;

import java.net.Socket;

public class MessageEvent {


    private  String message_modbus;
    private  byte[] modbus_frame_response;
    private  Socket socket;


    public  MessageEvent(String message_modbus, byte[] modbus_frame_response, Socket socket){

        this.message_modbus = message_modbus;
        this.modbus_frame_response = modbus_frame_response;
        this.socket = socket;
    }


    public String getMessageModBus(){

        return this.message_modbus;

    }

    public byte[] getFrameResponseModBus(){

        return this.modbus_frame_response;

    }

    public Socket getSocket(){

        return this.socket;

    }

}