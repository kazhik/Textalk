package net.kazhik.android.textalk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA. User: almondmendoza Date: 07/11/2010 Time: 2:14 AM
 * Link: http://www.tutorialforandroid.com/ Modified by kazhik, 2011.
 */
public class HandwritingActivity extends Activity implements
		View.OnTouchListener, Handler.Callback {

	private HandwritingView m_handwritingView;

	private Button m_undoBtn;
	private Button m_clearBtn;
	private Button m_saveBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing_activity);

		m_handwritingView = (HandwritingView) findViewById(R.id.handwritingView);
		m_handwritingView.setOnTouchListener(this);

		m_undoBtn = (Button) findViewById(R.id.undoBtn);
		m_clearBtn = (Button) findViewById(R.id.clearBtn);
		m_saveBtn = (Button) findViewById(R.id.saveBtn);
		
		this.disableButtons();

	}

	private void enableButtons() {
		this.setButtonEnabled(true);
	}

	private void disableButtons() {
		this.setButtonEnabled(false);
	}

	private void setButtonEnabled(boolean enabled) {
		m_undoBtn.setEnabled(enabled);
		m_clearBtn.setEnabled(enabled);
		m_saveBtn.setEnabled(enabled);
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

			this.enableButtons();
		}
		//disableRotation();
		this.lockOrientation();

		return true;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.undoBtn:
			m_handwritingView.undo();
			if (m_handwritingView.hasStack() == false) {
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
		
		case R.id.saveBtn:
			new ExportBitmap(this, this,
					m_handwritingView.getBitmap()).execute();
			break;
		
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		Toast.makeText(this, R.string.file_saved,
				Toast.LENGTH_LONG).show();
		return false;
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
		boolean ret = true;
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
}