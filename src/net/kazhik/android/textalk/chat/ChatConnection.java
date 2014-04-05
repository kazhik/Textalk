package net.kazhik.android.textalk.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ChatConnection implements Runnable {
	public interface MessageListener {
		void onNewMessage(String addr, String msg);
		void onClosed(String addr);
	}
	public interface BitmapListener {
		void onNewBitmap(String addr, Bitmap bmp);
	}
	public enum Mode {
		TEXT,
		BINARY
	};
	private MessageListener msgListener;
	private BitmapListener bmpListener;
	private Socket socket;
	private String addr;
	private String name;
	private Mode mode = Mode.TEXT;
	private static final String TAG = "ChatConnection";

	
	public ChatConnection(Socket socket, String addr, String name, MessageListener listener) {
		this.socket = socket;
		this.addr = addr;
		this.name = name;
		this.msgListener = listener;
	}
	private void connect() {
		try {
			this.socket.connect(new InetSocketAddress(this.addr, ChatServer.PORT));
		} catch (IOException e) {
			Log.e(TAG, "Failed to connect", e);
			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException e1) {
					Log.e(TAG, "Failed to close", e1);
				}
			}
			return;
		}		
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isConnected() {
		return this.socket.isConnected();
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	private boolean receiveBinaryData(InputStream is) throws IOException {
		Log.i(TAG, "Waiting data from: " + this.socket.getInetAddress().getHostAddress());
		byte[] recvBuff = new byte[1024*1024];
		int recvSize = is.read(recvBuff);
		if (recvSize == -1) {
			return false;
		}
		
		Bitmap bmp = BitmapFactory.decodeByteArray(recvBuff, 0, recvSize);
		this.bmpListener.onNewBitmap(this.addr, bmp);
		return true;
	}
	private boolean receiveTextMessage(InputStream is) throws IOException {
		Log.i(TAG, "Waiting message from: " + this.socket.getInetAddress().getHostAddress());
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String msg = br.readLine();
		if (msg == null) {
			return false;
		}
		Log.i(TAG, "Received: " + msg);
		this.msgListener.onNewMessage(this.addr, msg);
		return true;
	}
	
	@Override
	public void run() {
		if (!this.socket.isConnected()) {
			this.connect();
		}
		InputStream is;
		try {
			is = this.socket.getInputStream();
			boolean bRet;
			while (this.socket.isConnected()) {
				if (this.mode == Mode.TEXT) {
					bRet = this.receiveTextMessage(is);
				} else {
					bRet = this.receiveBinaryData(is);
				}
				if (!bRet) {
					Log.i(TAG, "Connection closed");
					this.socket.close();
					this.msgListener.onClosed(this.addr);
					break;
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		
	}
	public void sendBitmap(Bitmap bmp) throws IOException {
		if (this.mode != Mode.BINARY) {
			return;
		}
		if (this.socket.isClosed() || !this.socket.isConnected()) {
			throw new IOException("Socket is dead");
		}
		OutputStream os = this.socket.getOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
	}
	public void sendMessage(String text) throws IOException {
		if (this.mode != Mode.TEXT) {
			return;
		}
		if (this.socket.isClosed() || !this.socket.isConnected()) {
			throw new IOException("Socket is dead");
		}
		Log.d(TAG, "ChatConnection#sendMessage: " + text + " to: " + this.socket.getInetAddress().getHostAddress());
		OutputStream os;
		try {
			os = this.socket.getOutputStream();
			PrintWriter w = new PrintWriter(os, true);
			w.println(text);
			Log.i(TAG, "Sent: " + text);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			throw e;
		}
		
	}
	public void close() throws IOException {
		Log.d(TAG, "ChatConnection#close");
		this.socket.close();
	}

	
}