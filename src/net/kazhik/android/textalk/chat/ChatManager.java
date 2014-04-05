package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.util.Log;



public class ChatManager implements ChatServer.ConnectionListener,
		ChatConnection.MessageListener, PeerManager.PeerListener {
	public interface ReceiveMessageListener {
		void onConnected(String ipaddr, String name);
		void onMessage(String name, String msg);
	}
	private ExecutorService m_serverConnect = Executors.newSingleThreadExecutor();
	private ExecutorService m_clientConnect = Executors.newCachedThreadPool();
	private ChatServer m_server = new ChatServer(this);
	private Map<String, ChatConnection> m_clients = new ConcurrentHashMap<String, ChatConnection>();
	private ReceiveMessageListener m_listener;
	private static final String TAG = "ChatManager";
	
	private Context context;
	private PeerManager peerManager;

	
	public ChatManager(Context context, ReceiveMessageListener listener) {
		this.context = context;
		this.m_listener = listener;
	}
	public void init(String myname) {
		
		this.peerManager = new PeerManager(this.context, this);
		this.peerManager.init(myname);
		
		this.m_serverConnect.submit(new ChatServer(this));
		
	}
	
	public void connect(String ipaddr, String name) {
		ChatConnection conn = this.m_clients.get(ipaddr);
		if (conn != null && conn.isConnected()) {
			Log.i(TAG, "Already connected: " + ipaddr);
			conn.setName(name);
		} else {
			conn = new ChatConnection(new Socket(), ipaddr, name, this);
			this.m_clientConnect.submit(conn);
			Log.d(TAG, "ChatManager#connect: connected with " + ipaddr);
		}
		this.m_clients.put(ipaddr, conn);

	}
	@Override
	public void onClientConnected(Socket clientSocket) {
		String addr = clientSocket.getInetAddress().getHostAddress();
		if (this.m_clients.get(addr) != null) {
			Log.i(TAG, "Already connected: " + addr);
			return;
		}
		ChatConnection client = new ChatConnection(clientSocket, addr, addr, this);

		this.m_clientConnect.submit(client);
		this.m_clients.put(addr, client);
		Log.d(TAG, "ChatManager#onClientConnected: " + addr);
		
	}
	
	public boolean close() throws IOException {
		this.m_server.close();
		for (ChatConnection client: this.m_clients.values()) {
			client.close();
		}
		this.m_serverConnect.shutdown();
		this.m_clientConnect.shutdown();
		
		return true;
	}
	public boolean reconnect(ChatConnection.Mode mode) throws IOException {
		for (ChatConnection client: this.m_clients.values()) {
			client.close();
			client.setMode(mode);
			this.m_clientConnect.submit(client);
		}
		return true;
	}
	public void broadcastMessage(String text) throws IOException {
		for (ChatConnection client: m_clients.values()) {
			client.sendMessage(text);
		}
		
	}
	public void sendMessage(String hostName, String text) throws IOException {
		ChatConnection client = this.m_clients.get(hostName);
		if (client == null) {
			return;
		}
		client.sendMessage(text);
		
	}
	@Override
	public void onNewMessage(String addr, String msg) {
		Log.d(TAG, "onNewMessage: " + msg);
		String name = this.m_clients.get(addr).getName();
		this.m_listener.onMessage(name, msg);
		
	}

	public void pause() {
		this.peerManager.pause();
	}
	public void resume() {
		this.peerManager.resume();
	}
	@Override
	public void onNewHost(String addr, String name) {
		Log.d(TAG, "ChatManager#onNewHost: " + addr + "; name: " + name);
		this.connect(addr, name);
		this.m_listener.onConnected(addr, name);
	}
	@Override
	public void onClosed(String addr) {
		this.m_clients.remove(addr);
	}
}
