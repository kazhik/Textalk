package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

public class UdpReceiver implements Runnable {
	public interface MessageListener {
		void onReceived(InetAddress sender, String msg);
	}
	private DatagramSocket m_socket;
	private byte[] m_recvBuff;
	private MessageListener m_listener;
	
	private static final String TAG = "UdpReceiver";

	public UdpReceiver(MessageListener listener) {
		m_listener = listener;
	}
	public void init(int port, int buffsize) {
		try {
			m_socket = new DatagramSocket(port);
			m_recvBuff = new byte[buffsize];
		} catch (SocketException e) {
			Log.e(TAG, "init:" + port, e);
		}
	}
	public void close() {
		m_socket.close();
	}
	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(m_recvBuff, m_recvBuff.length);
		try {
			Log.i(TAG, "Receiver start");
			while (!m_socket.isClosed()) {
				m_socket.receive(packet);
				String recvMsg = new String(packet.getData(),
						packet.getOffset(), packet.getLength());
				m_listener.onReceived(packet.getAddress(), recvMsg);
			}
			Log.i(TAG, "Receiver end");
		} catch (IOException e) {
			Log.e(TAG, "run", e);
		}
	}

}
