package net.kazhik.android.textalk.chat;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class UdpManager implements UdpReceiver.MessageListener {

	private ScheduledExecutorService m_sender =
			Executors.newScheduledThreadPool(1);
	private ExecutorService m_receiver =
			Executors.newSingleThreadExecutor();

	public static final int PORT = 5057;
	private InetAddress m_localAddr;
	private Map<String, Long> m_remoteAddrs =
			new ConcurrentHashMap<String, Long>();
	private UdpReceiver m_receiveTask;
	private static final int BUFFSIZE = 128;
	private static final String TAG = "UdpManager";
	private ConnectionListener m_listener;
	
	public void start(Context context, String sendData, ConnectionListener listener) {
		m_listener = listener;
		
		WifiManager wifiManager =
				(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		
		m_localAddr = this.convertIpAddr(wifiManager.getConnectionInfo().getIpAddress());
		if (m_localAddr == null) {
			Log.e(TAG, "Failed to get My Address");
			return;
		}
		Log.i(TAG, "My Address is: " + m_localAddr.getHostAddress());
		this.startReceive();
		this.startRefresh();
		this.startBroadcasting(sendData);
	}
	public void stop() {
		m_sender.shutdown();
		m_receiveTask.close();
		m_receiver.shutdown();
	}
	// http://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
	private InetAddress convertIpAddr(int ipAddress) {
		// Convert little-endian to big-endianif needed
		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ipAddress = Integer.reverseBytes(ipAddress);
		}

		byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

		InetAddress addr = null;
		try {
			addr = InetAddress.getByAddress(ipByteArray);
		} catch (UnknownHostException ex) {
			Log.e(TAG, "getLocalWifiAddress", ex);
		}

		return addr;
	}
	private void refreshRemoteAddrs() {
		long currTime = System.currentTimeMillis();
		for (String addr: m_remoteAddrs.keySet()) {
			long lastUpdateTime = m_remoteAddrs.get(addr);
			if (currTime - lastUpdateTime > 20 * 1000) {
				Log.i(TAG, addr + " is dead");
				m_listener.onHostDead(addr);
				m_remoteAddrs.remove(addr);
			}
		}
	}
	private void startRefresh() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				refreshRemoteAddrs();
			}
			
		};
		this.m_sender.scheduleAtFixedRate(r, 10, 10, TimeUnit.SECONDS);
	}


	private void startReceive() {

		m_receiveTask = new UdpReceiver(this);
		m_receiveTask.init(PORT, BUFFSIZE);
		
		m_receiver.submit(m_receiveTask);
		
	}
	private void startBroadcasting(String sendData) {
		int sendLen = sendData.getBytes().length;
		if (sendLen > BUFFSIZE) {
			Log.e(TAG, "senddata too big: " + sendLen);
			return;
		}
		UdpBroadcast broadcast = new UdpBroadcast();
		boolean ret = broadcast.init(m_localAddr, PORT, sendData);
		if (!ret) {
			return;
		}
		
		// broadcasts in every 10 seconds
		m_sender.scheduleAtFixedRate(broadcast, 0, 10, TimeUnit.SECONDS);

	}
	
	@Override
	public void onReceived(InetAddress sender, String msg) {
		if (sender.equals(m_localAddr)) {
			return;
		}

		String addr = sender.getHostAddress();
		if (!m_remoteAddrs.containsKey(addr)) {
			m_listener.onNewHost(addr, msg);
		}
		this.m_remoteAddrs.put(addr, System.currentTimeMillis());
		Log.i(TAG, "Received from " + addr + ": " + msg);
	}

}
