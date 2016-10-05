package net.kazhik.android.textalk;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import net.kazhik.android.textalk.chat.ChatManager;
import net.kazhik.android.textalk.chat.ChatService;

import java.io.File;
import java.io.IOException;


public class HandwritingActivity extends ChatActivity implements
		OnTouchListener {

	private HandwritingView m_handwritingView;
	private static final String TAG = "HandwritingActivity";

	private Button m_undoBtn;
	private Button m_clearBtn;
	private Button m_sendBtn;
	private ChatManager chatManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing_activity);

		m_handwritingView = (HandwritingView) findViewById(R.id.handwritingView);
		m_handwritingView.setOnTouchListener(this);

		m_undoBtn = (Button) findViewById(R.id.undoBtn);
		m_clearBtn = (Button) findViewById(R.id.clearBtn);
		m_sendBtn = (Button) findViewById(R.id.sendBtn);

		Bitmap bmp;
		Intent intent = this.getIntent();
//		String sender = intent.getStringExtra("sender");
		String filename = intent.getStringExtra("bitmap");
		if (filename != null) {
			bmp = BitmapFactory.decodeFile(filename);
			new File(filename).delete();
			if (bmp != null) {
				m_handwritingView.setBitmap(bmp);
			}
		}

		m_undoBtn.setEnabled(false);

	}

	private void disableButtons() {
		m_undoBtn.setEnabled(false);
		m_clearBtn.setEnabled(false);
		m_sendBtn.setEnabled(false);
	}

	// http://stackoverflow.com/questions/3611457/android-temporarily-disable-orientation-changes-in-an-activity
	private void lockOrientation() {
		Display display = getWindowManager().getDefaultDisplay();
		int rotation = display.getRotation();
		int height;
		int width;
		Point size = new Point();
		display.getSize(size);
		height = size.y;
		width = size.x;
		switch (rotation) {
		case Surface.ROTATION_90:
			if (width > height)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			break;
		case Surface.ROTATION_180:
			if (height > width)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			break;
		case Surface.ROTATION_270:
			if (width > height)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
        case Surface.ROTATION_0:
		default:
			if (height > width)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		PointF pt = new PointF(motionEvent.getX(), motionEvent.getY());

		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			m_handwritingView.touchDown(pt);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
			m_handwritingView.touchMove(pt);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			m_handwritingView.touchUp(pt);

			m_undoBtn.setEnabled(true);
			m_clearBtn.setEnabled(true);

            if (this.chatManager.getConnectionCount() > 0) {
                m_sendBtn.setEnabled(true);
            }
		}
		//disableRotation();
		this.lockOrientation();

		return true;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.undoBtn:
			m_handwritingView.undo();
			if (!m_handwritingView.hasStack()) {
				this.disableButtons();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			break;

		case R.id.clearBtn:
			m_handwritingView.clear();
			this.disableButtons();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			break;
//		case R.id.closeBtn:
//			m_drawingSurface.stopDraw();
//			finish();
//			break;
		
		case R.id.sendBtn:
			this.broadcastBitmap();
			break;
		
		}
	}
	private void broadcastBitmap() {
		Bitmap bmp = this.m_handwritingView.getBitmap();
		try {
			boolean bSent = this.chatManager.broadcastBitmap(bmp);
			if (bSent) {
				Toast.makeText(this, R.string.bitmap_sent,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.not_connected,
						Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			Log.e(TAG, "Failed to broadcast bitmap", e);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Constants.MENU_SETTING, Menu.NONE,
				R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret;
		Intent intent;
		switch (item.getItemId()) {
		case Constants.MENU_SETTING:
			intent = new Intent(this, Config.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivity(intent);
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

	// ServiceConnection
	@Override
	public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);
        ChatService.ChatBinder binder = (ChatService.ChatBinder)iBinder;
        this.chatManager = binder.getChatManager();
        if (this.hasChatConnection()) {
            m_sendBtn.setEnabled(true);
        } else {
            m_sendBtn.setEnabled(false);

        }
	}
	// ServiceConnection
	@Override
	public void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
        if (!this.hasChatConnection()) {
            m_sendBtn.setEnabled(false);
        }
	}
	private void setSendButtonEnabled(boolean enable) {
		class SetSendButtonEnabled implements Runnable {
			private boolean enable;
			
			private SetSendButtonEnabled(boolean enable) {
				this.enable = enable;
			}
			@Override
			public void run() {
				m_sendBtn.setEnabled(enable);
			}
			
		}

		this.runOnUiThread(new SetSendButtonEnabled(enable));
	}

	// ChatManager.ReceiveMessageListener
	@Override
	public void onConnected(String ipaddr, String name) {
        super.onConnected(ipaddr, name);

		this.toast(this.getResources().getString(R.string.connected, name));
		this.setSendButtonEnabled(true);
	}

    // ChatManager.ReceiveMessageListener
    @Override
    public void onDisconnected(String ipaddr, String name) {
        super.onDisconnected(ipaddr, name);

        this.toast(this.getResources().getString(R.string.disconnected, name));
        if (!hasChatConnection()) {
            this.setSendButtonEnabled(false);
        }
    }

	// ChatManager.ReceiveMessageListener
	@Override
	public void onRenamed(String oldname, String newname) {
		this.toast(this.getResources().getString(R.string.renamed, oldname, newname));
		
	}

	// ChatManager.ReceiveMessageListener
	@Override
	public void onMessage(String sender, String text) {
		Intent intent = new Intent();
		intent.putExtra("sender", sender);
		intent.putExtra("text", text);
		this.setResult(RESULT_OK, intent);
		this.finish();
	}
	// ChatManager.ReceiveMessageListener
	@Override
	public void onBitmap(String name, String filename) {
		this.showBitmap(name, filename);
	}
	private void showBitmap(String sender, String filename) {
		class ShowBitmap implements Runnable {
			private Bitmap bmp;
			
			private ShowBitmap(Bitmap bmp) {
				this.bmp = bmp;
			}

			@Override
			public void run() {
				m_handwritingView.setBitmap(this.bmp);
				m_handwritingView.invalidate();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				
			}
			
		}
		Bitmap bmp = BitmapFactory.decodeFile(filename);
		new File(filename).delete();

		this.runOnUiThread(new ShowBitmap(bmp));
	}
	private void toast(String msg) {
		class ShowToast implements Runnable {
			private String msg;
			private ShowToast(String msg) {
				this.msg = msg;
			}

			@Override
			public void run() {

				Toast.makeText(HandwritingActivity.this,
						this.msg,
						Toast.LENGTH_LONG).show();

			}

		}
		this.runOnUiThread(new ShowToast(msg));

	}


}