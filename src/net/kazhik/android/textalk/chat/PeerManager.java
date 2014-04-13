package net.kazhik.android.textalk.chat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PeerManager implements ConnectionListener {

	public interface PeerListener {
		void onNewHost(String addr, String name);
	}
	private static final String TAG = "PeerManager";
	private Context context;

	private String myname;
	
	// Address -> Name
	private Map<String, String> peers = new ConcurrentHashMap<String, String>();

	private UdpManager udpManager;
	private WifiBroadcastManager wifiBroadcastManager;

	private String networkMode = "udp";
	private PeerListener listener;
	
	public PeerManager(Context context, PeerListener listener) {
		this.context = context;
		this.listener = listener;
	}
	public void init(String myname) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this.context);
		this.networkMode = prefs.getString("network_mode", "udp");
		
		this.myname = myname;
		
		if (this.networkMode.equals("udp")) {
			this.udpManager = new UdpManager();
			this.udpManager.start(this.context, this.myname, this);
		} else if (this.networkMode.equals("wifi_p2p")) {
		    this.wifiBroadcastManager = new WifiBroadcastManager(this.context, this);
		} else if (this.networkMode.equals("wifi_tethering")) {
		} else {
			Log.e(TAG, "Unknown network mode: " + this.networkMode);
		}

		
	}
	private void connectTetheringClients() {
		Map<String, String> addrMap = this.getAddressMap();
		for (String addr: addrMap.values()) {
			this.peers.put(addr, addr);
			this.listener.onNewHost(addr, addr);
		}
	}
	
	public void close() {
		if (this.networkMode.equals("udp")) {
			this.udpManager.stop();
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

	public void resume() {
		if (this.networkMode.equals("wifi_p2p")) {
			this.wifiBroadcastManager.resume();
		} else if (this.networkMode.equals("wifi_tethering")) {
			this.connectTetheringClients();
		} else if (this.networkMode.equals("udp")) {
		}
	}
	public void pause() {
		if (this.networkMode.equals("wifi_p2p")) {
			this.wifiBroadcastManager.pause();
		} else if (this.networkMode.equals("udp")) {
		}
	}
	// http://stackoverflow.com/questions/9906021/getting-the-ip-address-of-client-or-getting-the-informationssid-of-clients-con
	private Map<String, String> getAddressMap() {
		Map<String, String> addrMap = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");

				if (splitted != null && splitted.length >= 4) {
					String macAddr = splitted[3];

					if (macAddr.matches("..:..:..:..:..:..")) {
						String ipAddr = splitted[0];
						addrMap.put(macAddr, ipAddr);
					}
				}

			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to open /proc/net/arp", e);
		} catch (IOException e) {
			Log.e(TAG, "Failed to read /proc/net/arp", e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		return addrMap;
	
	}

}
