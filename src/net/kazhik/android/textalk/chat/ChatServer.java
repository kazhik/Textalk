package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


import android.util.Log;

class ChatServer implements Runnable {
	public interface ConnectionListener {
		void onClientConnected(Socket sock);
	}

	public static final int PORT = 5056;
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
			
		} catch (SocketException e) {
			Log.e(TAG, "SocketException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
	}
	public void close() throws IOException {
		m_serverSocket.close();
	}
	
}