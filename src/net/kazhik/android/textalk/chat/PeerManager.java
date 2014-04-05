package net.kazhik.android.textalk.chat;

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

	public void resume() {
		if (this.networkMode.equals("wifi_p2p")) {
			this.wifiBroadcastManager.resume();
		} else if (this.networkMode.equals("udp")) {
		}
	}
	public void pause() {
		if (this.networkMode.equals("wifi_p2p")) {
			this.wifiBroadcastManager.pause();
		} else if (this.networkMode.equals("udp")) {
		}
	}
}
