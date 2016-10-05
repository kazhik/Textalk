package net.kazhik.android.textalk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import net.kazhik.android.textalk.chat.ChatManager;
import net.kazhik.android.textalk.chat.ChatService;

/**
 * Created by kazhik on 16/10/05.
 */

public class ChatActivity extends Activity
        implements ChatManager.ReceiveMessageListener, ServiceConnection {
    private ChatManager chatManager;
    private String myname;
    private static final String TAG = "ChatActivity";

    protected boolean hasChatConnection() {
        return this.chatManager.getConnectionCount() > 0;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        this.myname = intent.getStringExtra("myname");

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (this.chatManager != null) {
            this.chatManager.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (this.chatManager != null) {
            this.chatManager.resume();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "bind service");
        Intent intent = new Intent(this, ChatService.class);

        this.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "unbind service");
        this.unbindService(this);
    }
    @Override
    public void onConnected(String ipaddr, String name) {
        this.keepScreenOn(true);

    }

    @Override
    public void onDisconnected(String ipaddr, String name) {
        if (this.chatManager.getConnectionCount() == 0) {
            this.keepScreenOn(false);
        }

    }

    @Override
    public void onRenamed(String oldname, String newname) {

    }

    @Override
    public void onMessage(String name, String msg) {

    }

    @Override
    public void onBitmap(String name, String filename) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + name.toString());
        ChatService.ChatBinder binder = (ChatService.ChatBinder)iBinder;
        this.chatManager = binder.getChatManager();
        this.chatManager.init(this.myname);
        this.chatManager.addReceiveMessageListener(this);
        if (this.chatManager.getConnectionCount() > 0) {
            this.keepScreenOn(true);
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisConnected: " + name.toString());
        if (this.chatManager.getConnectionCount() == 0) {
            this.keepScreenOn(false);
        }

    }
    private void keepScreenOn(final boolean on) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

    }
}
