package com.sajh1s2.espblufi.tcpserver;

import java.net.Socket;

public class MessageDebugEvent {


    private  String message;


    public MessageDebugEvent(String message){

        this.message = message;
    }


    public String getMessageDebug(){

        return this.message;

    }

}