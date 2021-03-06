package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import android.util.Log;

class UdpBroadcast implements Runnable {
	private static final String TAG = "UdpBroadcast";

	private DatagramSocket m_socket;
	private DatagramPacket m_packet;

	boolean init(InetAddress localAddr, int targetPort, String sendData) {

		try {
			m_socket = new DatagramSocket();
			m_socket.setBroadcast(true);
			
			InetAddress adr = this.getBroadcastAddr(localAddr);
			if (adr == null) {
				Log.e(TAG, "Failed to get broadcast address");
				return false;
			}
			byte[] data = sendData.getBytes("UTF-8");
			m_packet = new DatagramPacket(data, data.length, adr, targetPort);
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return true;

	}
	private InetAddress getBroadcastAddr(InetAddress inetAddr) throws SocketException {

		List<InterfaceAddress> addresses = NetworkInterface
				.getByInetAddress(inetAddr).getInterfaceAddresses();

		for (InterfaceAddress adr: addresses) {
			if (adr.getBroadcast() != null) {
				Log.i(TAG, "broadcast: " + adr.getBroadcast().getHostAddress());
				return adr.getBroadcast();
			}
		}
		return null;
	}
	@Override
	public void run() {
		try {
			m_socket.send(m_packet);
		} catch (IOException e) {
			Log.e(TAG, "BroadcastLocalAddress", e);
		}		
		
	}
	

}
