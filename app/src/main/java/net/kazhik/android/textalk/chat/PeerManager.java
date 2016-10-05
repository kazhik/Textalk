package net.kazhik.android.textalk.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

class PeerManager implements ConnectionListener {

	interface PeerListener {
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
	
	PeerManager(Context context, PeerListener listener) {
		this.context = context;
		this.listener = listener;
	}
	void init(String myname) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this.context);
		this.networkMode = prefs.getString("connection_mode", "udp");
		
		this.myname = myname;

		switch (this.networkMode) {
			case "udp":
				this.udpManager = new UdpManager();
				this.udpManager.start(this.context, this.myname, this);
				break;
			case "wifi_p2p":
				this.wifiBroadcastManager = new WifiBroadcastManager(this.context, this);
				break;
			case "wifi_tethering":
				if (this.isTetheringOn()) {
					this.connectTetheringClients();
				}
				break;
			default:
				Log.e(TAG, "Unknown network mode: " + this.networkMode);
				break;
		}

		
	}
	private void connectTetheringClients() {
		Map<String, String> addrMap = this.getAddressMap();
		for (String addr: addrMap.values()) {
			this.peers.put(addr, addr);
			this.listener.onNewHost(addr, addr);
		}
	}

	private boolean isTetheringOn() {
		WifiManager wifi = (WifiManager) this.context
				.getSystemService(Context.WIFI_SERVICE);
		try {
			Method method = wifi.getClass().getDeclaredMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(wifi);
		} catch (NoSuchMethodException |
				IllegalAccessException |
				InvocationTargetException |
				IllegalArgumentException e) {
			Log.e(TAG, "isTetheringOn", e);
		}
		return false;
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

	void resume() {
        switch (this.networkMode) {
            case "wifi_p2p":
                this.wifiBroadcastManager.resume();
                break;
            case "wifi_tethering":
                this.connectTetheringClients();
                break;
            case "udp":
            default:
                break;
        }
	}
	void pause() {
		if (this.networkMode.equals("wifi_p2p")) {
			this.wifiBroadcastManager.pause();
		}
	}
	// http://stackoverflow.com/questions/9906021/getting-the-ip-address-of-client-or-getting-the-informationssid-of-clients-con
    private Map<String, String> getAddressMap() {
		Map<String, String> addrMap = new HashMap<>();
		BufferedReader br = null;
		try {
            InputStreamReader isr =
                    new InputStreamReader(new FileInputStream("/proc/net/arp"), "UTF-8");
			br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");

				if (splitted.length >= 4) {
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
                if (br != null) {
                    br.close();
                }
            } catch (IOException ignored) {
			}
		}
		return addrMap;
	
	}

}
