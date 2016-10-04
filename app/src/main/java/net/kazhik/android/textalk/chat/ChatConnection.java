package net.kazhik.android.textalk.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ChatConnection implements Runnable {
	public interface MessageListener {
		void onNewMessage(String addr, String msg);
		void onNewBitmap(String addr, Bitmap bmp);
		void onClosed(String addr);
	}

	private MessageListener msgListener;
	private Socket socket;
	private String addr;
	private String name;
	private static final String TAG = "ChatConnection";

	
	public ChatConnection(Socket socket, String addr, String name,
			MessageListener listener) {
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
	@Override
	public void run() {
		if (!this.socket.isConnected()) {
			this.connect();
		}
		try {
			while (this.socket.isConnected()) {
				InputStream is = this.socket.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				char dataType = dis.readChar();
				int dataSize = dis.readInt();
				byte[] recvBuff = new byte[dataSize];
				dis.readFully(recvBuff);
				if (dataType == 'B') {
					Bitmap bmp = BitmapFactory.decodeByteArray(recvBuff, 0, dataSize);
					this.msgListener.onNewBitmap(this.addr, bmp);
				} else if (dataType == 'T') {
					String msg = new String(recvBuff, 0, dataSize, "UTF-8");
					Log.i(TAG, "Received text: " + msg);
					this.msgListener.onNewMessage(this.addr, msg);
				}
			}
			this.socket.close();
			this.msgListener.onClosed(this.addr);
			Log.i(TAG, "ChatConnection end: ");
		} catch (EOFException e) {
			Log.i(TAG, "Disconnected: " + e.getMessage());
			this.msgListener.onClosed(this.addr);
		} catch (SocketException e) {
			Log.i(TAG, "ChatConnection end: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		
	}
	private void sendData(char dataType, byte[] sendData) throws IOException {
		if (this.socket.isClosed() || !this.socket.isConnected()) {
			throw new IOException("Socket is dead");
		}
		OutputStream os = this.socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeChar(dataType);
		dos.writeInt(sendData.length);
		os.write(sendData);
		
	}
	public void sendBitmap(Bitmap bmp) throws IOException {
		Log.d(TAG, "sendBitmap: " + bmp.getByteCount());
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
		this.sendData('B', bos.toByteArray());
	}
	public void sendText(String text) throws IOException {
		Log.d(TAG, "ChatConnection#sendMessage: " + text
				+ " to: " + this.socket.getInetAddress().getHostAddress());
		this.sendData('T', text.getBytes("UTF-8"));
		
	}
	public void close() throws IOException {
		Log.d(TAG, "ChatConnection#close");
		this.socket.close();
	}

	
}