package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


import android.util.Log;

class ChatServer implements Runnable {
    interface ConnectionListener {
        void onClientConnected(Socket sock);
    }

    static final int PORT = 5056;
    private ConnectionListener m_listener;
    private ServerSocket m_serverSocket;
    private static final String TAG = "ChatServer";


    ChatServer(ConnectionListener listener) {
        m_listener = listener;
    }

    @Override
    public void run() {
        try {
            m_serverSocket = new ServerSocket(PORT);

            while (m_serverSocket.isBound()) {
                Socket clientSocket = m_serverSocket.accept();

                m_listener.onClientConnected(clientSocket);
            }
            Log.d(TAG, "ChatServer end");
        } catch (SocketException e) {
            Log.d(TAG, "ChatServer end: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
    }
    public void close() throws IOException {
        if (this.m_serverSocket != null) {
            Log.d(TAG, "Closing ChatServer socket");
            m_serverSocket.close();
        }
    }

}