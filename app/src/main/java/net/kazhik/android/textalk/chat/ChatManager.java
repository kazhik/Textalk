package net.kazhik.android.textalk.chat;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.kazhik.android.textalk.ExportBitmap;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;



public class ChatManager implements ChatServer.ConnectionListener,
		ChatConnection.MessageListener, PeerManager.PeerListener, Handler.Callback {
	public interface ReceiveMessageListener {
		void onConnected(String ipaddr, String name);
		void onDisconnected(String ipaddr, String name);
		void onRenamed(String oldname, String newname);
		void onMessage(String name, String msg);
		void onBitmap(String name, String filename);
	}
	private ExecutorService m_serverConnect = Executors.newSingleThreadExecutor();
	private ExecutorService m_clientConnect = Executors.newCachedThreadPool();
	private ChatServer m_server;
	private Map<String, ChatConnection> m_clients = new ConcurrentHashMap<String, ChatConnection>();
	private ReceiveMessageListener m_listener;
	private static final String TAG = "ChatManager";
	
	private Context context;
	private PeerManager peerManager = null;

	
	public ChatManager(Context context) {
		this.context = context;
	}
	public void init(String myname) {
		if (this.peerManager != null) {
			return;
		}
		
		this.peerManager = new PeerManager(this.context, this);
		this.peerManager.init(myname);
		
		this.m_server = new ChatServer(this);
		this.m_serverConnect.submit(this.m_server);
		
	}
	public int getConnectionCount() {
		return this.m_clients.size();
	}
	public void addReceiveMessageListener(ReceiveMessageListener listener) {
		this.m_listener = listener;
	}
	
	public boolean connect(String ipaddr, String name) {
		boolean result;
		ChatConnection conn = this.m_clients.get(ipaddr);
		if (conn != null && conn.isConnected()) {
			Log.i(TAG, "Already connected: " + ipaddr);
			if (!conn.getName().equals(name)) {
				String oldname = conn.getName();
				conn.setName(name);
				this.m_listener.onRenamed(oldname, name);
			}
			result = false;
		} else {
			conn = new ChatConnection(new Socket(), ipaddr, name, this);
			this.m_clientConnect.submit(conn);
			Log.d(TAG, "ChatManager#connect: connected with " + ipaddr);
			result = true;
		}
		this.m_clients.put(ipaddr, conn);
		return result;

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
		this.m_listener.onConnected(addr, addr);
		Log.d(TAG, "ChatManager#onClientConnected: " + addr);
	}
	
	public boolean close() throws IOException {
		if (this.peerManager != null) {
			this.peerManager.close();
		}
		if (this.m_server != null) {
			this.m_server.close();
		}
		for (ChatConnection client: this.m_clients.values()) {
			client.close();
		}
		this.m_serverConnect.shutdown();
		this.m_clientConnect.shutdown();
		
		return true;
	}
	public boolean reconnect() throws IOException {
		for (ChatConnection client: this.m_clients.values()) {
			client.close();
			this.m_clientConnect.submit(client);
		}
		return true;
	}
	public boolean broadcastBitmap(Bitmap bmp) throws IOException {
		if (m_clients.isEmpty()) {
			return false;
		}
		for (ChatConnection client: m_clients.values()) {
			client.sendBitmap(bmp);
		}
		return true;
	}

	public boolean broadcastMessage(String text) throws IOException {
		if (m_clients.isEmpty()) {
			return false;
		}
		for (ChatConnection client: m_clients.values()) {
			client.sendText(text);
			Log.d(TAG, "Message was sent to " + client.getName());
		}
		return true;
	}
	public void sendMessage(String hostName, String text) throws IOException {
		ChatConnection client = this.m_clients.get(hostName);
		if (client == null) {
			return;
		}
		client.sendText(text);
		
	}
	@Override
	public void onNewMessage(String addr, String msg) {
		Log.d(TAG, "onNewMessage: " + msg);
		String name = this.m_clients.get(addr).getName();
		this.m_listener.onMessage(name, msg);
		
	}
	@Override
	public void onNewBitmap(String addr, Bitmap bmp) {
		Log.d(TAG, "ChatManager#onNewBitmap: ");

		String sender = this.m_clients.get(addr).getName();
		
		// To avoid "FAILED BINDER TRANSACTION"
		ExportBitmap exportBmp = new ExportBitmap(this.context, this, bmp, sender);
		exportBmp.execute();
		
	}
	@Override
	public boolean handleMessage(Message msg) {
		String filename = msg.getData().getString("filename");
		String sender = msg.getData().getString("sender");
		this.m_listener.onBitmap(sender, filename);
		
		return false;
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
		boolean result;
		try {
			result = this.connect(addr, name);
			if (result) {
				this.m_listener.onConnected(addr, name);
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to connect to: " + addr, e);
		}
	}
	@Override
	public void onClosed(String addr) {
		ChatConnection conn = this.m_clients.remove(addr);
		if (conn != null) {
			this.m_listener.onDisconnected(addr, conn.getName());
		}
	}

}