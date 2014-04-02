package net.kazhik.android.textalk.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class PeerManager implements WifiBroadcastReceiver.WifiListener,
		UdpManager.HostListener, WifiP2pManager.PeerListListener,
		WifiP2pManager.ConnectionInfoListener {

	public interface PeerListener {
		void onNewHost(String addr, String name);
	}
	private static final String TAG = "PeerManager";
	private Context context;

	private String myname;
	
	private UdpManager udpManager;
	
	// Address -> Name
	private Map<String, String> peers = new ConcurrentHashMap<String, String>();

	private WifiManager wifiManager;
	private WifiP2pManager wifiP2pManager;
	private WifiP2pManager.Channel m_wifiChannel;
	private WifiBroadcastReceiver m_wifiReceiver;
	private IntentFilter m_IntentFilter;
	private String networkMode = "udp";
	private PeerListener listener;
	
	public PeerManager(Context context, PeerListener listener) {
		this.context = context;
		this.listener = listener;
	}
	public void init(String myname) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this.context);
		this.networkMode = prefs.getString("network_mode", "wifiP2P");
		
		this.myname = myname;
		
		if (this.networkMode.equals("udp")) {
			this.wifiManager =
					(WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);
			
			this.udpManager = new UdpManager();
			this.udpManager.start(this.wifiManager, this.myname, this);
		} else if (this.networkMode.equals("wifiP2P")) {
		    m_IntentFilter = new IntentFilter();
		    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		    
		    this.wifiP2pManager =
		    		(WifiP2pManager) this.context.getSystemService(Context.WIFI_P2P_SERVICE);
		    m_wifiChannel =
		    		this.wifiP2pManager.initialize(this.context, this.context.getMainLooper(), null);
		} else {
			Log.e(TAG, "Unknown network mode: " + this.networkMode);
		}

		
	}
	
	@Override
	public void onNewHost(String addr, String name) {
		this.peers.put(addr, name);
		this.listener.onNewHost(addr, name);
	}

	@Override
	public void onHostDead(String addr) {
		this.peers.remove(addr);
	}

	@Override
	public void onWifiP2PEnabled() {
		Log.d(TAG, "Wifi P2P enabled");
	    this.discoverPeers();
		
	}

	@Override
	public void onWifiP2pDisabled() {
		Log.d(TAG, "Wifi P2P disabled");
		
	}

	@Override
	public void onPeersChanged(WifiP2pDeviceList deviceList) {
		if (deviceList == null) {
			this.wifiP2pManager.requestPeers(m_wifiChannel, this);
			return;

		} else {
			for (WifiP2pDevice device: deviceList.getDeviceList()) {
				Log.d("TextalkActivity#onPeersChanged", "device:" + device.deviceName);
			}
		}
		
	}
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		for (WifiP2pDevice peer: peers.getDeviceList()) {
			Log.d(TAG, "DeviceName: " + peer.deviceName + "; status: " + peer.status);
			if (peer.status == WifiP2pDevice.AVAILABLE) {
				this.connect(peer);
			}
		}
		
	}

	private void connect(WifiP2pDevice peer) {

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = peer.deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		this.wifiP2pManager.connect(this.m_wifiChannel, config,
				new ActionListener() {

					@Override
					public void onSuccess() {
					}

					@Override
					public void onFailure(int reason) {
						Log.e(TAG, "Failed to connect: " + reason);
					}
				});
	}

	@Override
	public void onConnectionChanged(WifiP2pInfo p2pInfo,
			NetworkInfo networkInfo, WifiP2pGroup p2pGroup) {

		if (p2pGroup != null) {
			Log.d(TAG, "P2pGroup: " + p2pGroup.toString());
		}
		Log.d(TAG, "WifiP2pInfo: " + p2pInfo.toString());
		if (networkInfo.isConnected()) {
			this.wifiP2pManager.requestConnectionInfo(this.m_wifiChannel, this);
		}

	}
	// WifiP2pManager.ConnectionInfoListener
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Log.d(TAG, "onConnectionInfoAvailable/WifiP2pInfo: " + info.toString());
		this.listener.onNewHost(info.groupOwnerAddress.getHostAddress(), "");
	}

	@Override
	public void onThisDeviceChanged(WifiP2pDevice p2pDevice) {
		Log.d(TAG, "This Device: " + p2pDevice.deviceName);
		
	}
	private void discoverPeers() {
		this.wifiP2pManager.discoverPeers(m_wifiChannel,
				new WifiP2pManager.ActionListener() {

					@Override
					public void onSuccess() {
					}

					@Override
					public void onFailure(int reasonCode) {
						Log.d("TextalkActivity", "discoverPeers failure: "
								+ reasonCode);
					}
				});
	}	
	public void resume() {
		if (this.networkMode.equals("wifiP2P")) {
		    m_wifiReceiver = new WifiBroadcastReceiver(this);
			this.context.registerReceiver(m_wifiReceiver, m_IntentFilter);
		} else if (this.networkMode.equals("udp")) {
		}
	}
	public void pause() {
		if (this.networkMode.equals("wifiP2P")) {
			this.context.unregisterReceiver(m_wifiReceiver);
		} else if (this.networkMode.equals("udp")) {
		}
	}
}
