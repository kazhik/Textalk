package net.kazhik.android.textalk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

	private DrawingSurface m_drawingSurface;
	private Paint m_currentPaint;

	private Button m_undoBtn;
	private Button m_clearBtn;
	private Button m_saveBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing_activity);

		setCurrentPaint();

		m_drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
		m_drawingSurface.setOnTouchListener(this);
		m_drawingSurface.setPaint(m_currentPaint);

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

	private void disableRotation() {
		final int orientation = getResources().getConfiguration().orientation;
		final int rotation = getWindowManager().getDefaultDisplay()
				.getRotation();

		// Copied from Android docs, since we don't have these values in Froyo
		// 2.2
		int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
		int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
			SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}

		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		} else if (rotation == Surface.ROTATION_180
				|| rotation == Surface.ROTATION_270) {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			}
		}
	}

	private void setCurrentPaint() {
		m_currentPaint = new Paint();
		m_currentPaint.setDither(false);
		m_currentPaint.setStyle(Paint.Style.STROKE);
		m_currentPaint.setStrokeJoin(Paint.Join.ROUND);
		m_currentPaint.setStrokeCap(Paint.Cap.ROUND);
		m_currentPaint.setStrokeWidth(5);
		m_currentPaint.setColor(Color.DKGRAY);

	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		PointF pt = new PointF(motionEvent.getX(), motionEvent.getY());

		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			m_drawingSurface.mouseDown(pt);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
			m_drawingSurface.mouseMove(pt);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			m_drawingSurface.mouseUp(pt);

			this.enableButtons();
		}
		disableRotation();

		return true;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.undoBtn:
			m_drawingSurface.undo();
			if (m_drawingSurface.hasStack() == false) {
				this.disableButtons();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			break;

		case R.id.clearBtn:
			m_drawingSurface.clear();
			this.disableButtons();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			break;
//		case R.id.closeBtn:
//			m_drawingSurface.stopDraw();
//			finish();
//			break;
		
		case R.id.saveBtn:
			new ExportBitmap(this, this,
					m_drawingSurface.getBitmap()).execute();
			break;
		
		}
	}
	@Override
	protected void onDestroy() {
		m_drawingSurface.stopDraw();
		super.onDestroy();
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