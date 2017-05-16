package com.kauron.newssarcher;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

class TCPClient {

    private String SERVER_DOMAIN = "kauron.ddns.net"; //your computer IP address
    private int SERVER_PORT = 2048;
    private OnMessageReceived mMessageListener = null;
    private final String message;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    TCPClient(String message, String endpoint, OnMessageReceived listener) {
        mMessageListener = listener;
        this.message = message;
        if (endpoint.contains(":")) {
            String[] strings = endpoint.split(":");
            SERVER_DOMAIN = strings[0];
            SERVER_PORT = Integer.valueOf(strings[1]);
        }
    }

    void run() {
        try {
            Socket socket = new Socket(SERVER_DOMAIN, SERVER_PORT);
            try {
                //send the message to the server
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                if (!out.checkError()) {
                    out.print(this.message);
                    out.flush();
                }
                //receive the message which the server sends back
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String text = "", line;
                while ((line = in.readLine()) != null) {
                    text += line + '\n';
                }
                text = text.substring(0, text.length() - 1);

                if (mMessageListener != null) {
                    mMessageListener.messageReceived(text);
                }
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    interface OnMessageReceived {
        void messageReceived(String message);
    }
}
