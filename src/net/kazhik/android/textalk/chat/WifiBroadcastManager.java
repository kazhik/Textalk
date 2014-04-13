package net.kazhik.android.textalk.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiBroadcastManager extends BroadcastReceiver implements
		WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

	private ConnectionListener listener;
	private WifiP2pManager wifiP2pManager;
	private WifiP2pManager.Channel wifiChannel;
	private Context context;
	private IntentFilter intentFilter;
	private static final String TAG = "WifiBroadcastManager";
	private boolean receiverRegistered = false;

	public WifiBroadcastManager(Context context,
			ConnectionListener listener) {
		super();
		this.listener = listener;
		this.context = context;

		this.intentFilter = new IntentFilter();
		this.intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		this.intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		this.intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		this.intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		this.wifiP2pManager = (WifiP2pManager) context
				.getSystemService(Context.WIFI_P2P_SERVICE);
		this.wifiChannel = wifiP2pManager.initialize(context,
				context.getMainLooper(), null);

	}

	@SuppressLint("InlinedApi")  // API version check
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		int apiVer = android.os.Build.VERSION.SDK_INT;
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			Log.d("WifiBroadcastReceiver", "state changed: " + state);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// -> WIFI_P2P_PEERS_CHANGED_ACTION
				this.wifiP2pManager.discoverPeers(this.wifiChannel, null);
			} else {
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			if (apiVer >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiP2pDeviceList peers =
						intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
				this.connectPeers(peers);
			} else {
				// -> onPeersAvailable() -> connectPeers()
				this.wifiP2pManager.requestPeers(this.wifiChannel, this);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			Log.d("WifiBroadcastReceiver", "connection changed");
			WifiP2pInfo p2pInfo =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
			NetworkInfo networkInfo =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (apiVer >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				WifiP2pGroup p2pGroup =
						intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
				Log.d(TAG, "P2pGroup: " + p2pGroup.toString());
			}
			Log.d(TAG, "WifiP2pInfo: " + p2pInfo.toString());
			Log.d(TAG, "NetworkInfo: " + networkInfo.toString());
			if (networkInfo.isConnected()) {
				this.wifiP2pManager.requestConnectionInfo(this.wifiChannel, this);
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			Log.d("WifiBroadcastReceiver", "this device changed");
			WifiP2pDevice p2pDevice =
					intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			Log.d(TAG, "This Device: " +
					p2pDevice.deviceName + "; status = " + p2pDevice.status);
		}
	}

	
	private void connect(String deviceAddress) {

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		// -> WIFI_P2P_CONNECTION_CHANGED_ACTION
		this.wifiP2pManager.connect(this.wifiChannel, config, null);
	}
	private void connectPeers(WifiP2pDeviceList peers) {
		for (WifiP2pDevice peer: peers.getDeviceList()) {
			Log.d(TAG, "DeviceName: " + peer.deviceName + "; status: " + peer.status);
			if (peer.status == WifiP2pDevice.AVAILABLE) {
				this.connect(peer.deviceAddress);
			}
		}
		
	}
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		this.connectPeers(peers);
	}
	
	// WifiP2pManager.ConnectionInfoListener
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Log.d(TAG, "onConnectionInfoAvailable/WifiP2pInfo: " + info.toString());
		if (info.groupFormed) {
			String ipaddr = info.groupOwnerAddress.getHostAddress();
			if (info.isGroupOwner) {
				// start server
				Log.d(TAG, "Owner address: " + ipaddr);
			} else {
				// connect to client
				Log.d(TAG, "Other address: " + ipaddr);
				this.listener.onNewHost(ipaddr, ipaddr);
			}
		}
	}
	public void resume() {
		if (this.receiverRegistered == false) {
			this.context.registerReceiver(this, this.intentFilter);
			this.receiverRegistered = true;
		}
	}
	public void pause() {
		if (this.receiverRegistered == true) {
			this.context.unregisterReceiver(this);
			this.receiverRegistered = false;
		}
	}
}
