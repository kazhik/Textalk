package net.kazhik.android.textalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver implements PeerListListener {
	private WifiP2pManager m_wifiManager;
	private WifiP2pManager.Channel m_wifiChannel;

	public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
		super();
		this.m_wifiManager = manager;
		this.m_wifiChannel = channel;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi P2P is enabled
				Log.d("WifiBroadcastReceiver", "P2P enabled");
			} else {
				Log.d("WifiBroadcastReceiver", "P2P disabled");
				// Wi-Fi P2P is not enabled
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			Log.d("WifiBroadcastReceiver", "peers changed");
		    if (m_wifiManager != null) {
		    	m_wifiManager.requestPeers(m_wifiChannel, this);
		    }

			// Call WifiP2pManager.requestPeers() to get a list of current peers
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			// Respond to new connection or disconnections
			Log.d("WifiBroadcastReceiver", "connection changed");
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			// Respond to this device's wifi state changing
			Log.d("WifiBroadcastReceiver", "this device changed");
		}
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		for (WifiP2pDevice device: peers.getDeviceList()) {
			Log.d("WifiBroadcastReceiver", "device:" + device.deviceName);
		}
		
	}

}
