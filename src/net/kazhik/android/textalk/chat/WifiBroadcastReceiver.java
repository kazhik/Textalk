package net.kazhik.android.textalk.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver {
	private WifiListener m_listener;

	public interface WifiListener {
		void onWifiP2PEnabled();
		void onWifiP2pDisabled();
		void onPeersChanged(WifiP2pDeviceList deviceList);
		void onConnectionChanged(WifiP2pInfo p2pInfo, NetworkInfo networkInfo, WifiP2pGroup p2pGroup);
		void onThisDeviceChanged(WifiP2pDevice p2pDevice);
	}

	public WifiBroadcastReceiver(WifiListener listener) {
		super();
		this.m_listener = listener;
	}

	@SuppressLint("InlinedApi")  // API version check
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		int apiVer = android.os.Build.VERSION.SDK_INT;
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			Log.d("WifiBroadcastReceiver", "state changed");
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				m_listener.onWifiP2PEnabled();
			} else {
				m_listener.onWifiP2pDisabled();
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			Log.d("WifiBroadcastReceiver", "peers changed");
			if (apiVer >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiP2pDeviceList deviceList =
						intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
				m_listener.onPeersChanged(deviceList);
			} else {
				m_listener.onPeersChanged(null);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			Log.d("WifiBroadcastReceiver", "connection changed");
			WifiP2pInfo p2pInfo =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
			NetworkInfo networkInfo =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (apiVer >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiP2pGroup p2pGroup =
						intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
				m_listener.onConnectionChanged(p2pInfo, networkInfo, p2pGroup);
			} else {
				m_listener.onConnectionChanged(p2pInfo, networkInfo, null);
			}

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			Log.d("WifiBroadcastReceiver", "this device changed");
			WifiP2pDevice p2pDevice =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			m_listener.onThisDeviceChanged(p2pDevice);
		}
	}

}
