package net.kazhik.android.textalk.chat;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ChatService extends Service {

	private ChatManager chatManager;
	private ChatBinder chatBinder = new ChatBinder();
	private static final String TAG = "HandwritingActivity";
	
	public class ChatBinder extends Binder {

		public ChatService getService() {
			return ChatService.this;
		}
		public ChatManager getChatManager() {
			return chatManager;
		}
	}
	@Override
	public void onCreate() {
		this.chatManager = new ChatManager(this);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			this.chatManager.close();
		} catch (IOException e) {
			Log.e(TAG, "Failed to close", e);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return this.chatBinder;
	}

}
