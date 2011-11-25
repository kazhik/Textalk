package net.kazhik.android.textalk;

import net.kazhik.android.textalk.brush.Brush;
import net.kazhik.android.textalk.brush.PenBrush;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:14 AM
 * Link: http://www.tutorialforandroid.com/
 * Modified by kazhik, 2011.
 */
public class HandwritingActivity extends Activity implements View.OnTouchListener{
	private DrawingSurface m_drawingSurface;
	private DrawingPath m_currentDrawingPath;
	private Paint m_currentPaint;

	private boolean m_visible = true;
	
	private Button m_undoBtn;
	private Button m_clearBtn;

	private Brush m_currentBrush;

//	private File APP_FILE_PATH = new File("/sdcard/textalk");


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing_activity);

		setCurrentPaint();
		m_currentBrush = new PenBrush();

		m_drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
		m_drawingSurface.setOnTouchListener(this);
		m_drawingSurface.setPreviewPath(getPreviewPaint());

	    final DrawingPathManager cmdMgr = (DrawingPathManager) getLastNonConfigurationInstance();
	    if (cmdMgr != null) {
	    	m_drawingSurface.setDrawingPathManager(cmdMgr);
	    }
		
		m_undoBtn = (Button) findViewById(R.id.undoBtn);
		m_undoBtn.setEnabled(false);

		m_clearBtn = (Button) findViewById(R.id.clearBtn);
		m_clearBtn.setEnabled(false);

	}
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final DrawingPathManager cmdMgr = m_drawingSurface.getDrawingPathManager();
	    return cmdMgr;
	}
	public void fixOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	private void setCurrentPaint(){
		m_currentPaint = new Paint();
		m_currentPaint.setDither(false);
		setTheme();
		m_currentPaint.setStyle(Paint.Style.STROKE);
		m_currentPaint.setStrokeJoin(Paint.Join.ROUND);
		m_currentPaint.setStrokeCap(Paint.Cap.ROUND);
		m_currentPaint.setStrokeWidth(5);

	}

	private Paint getPreviewPaint(){
		return m_currentPaint;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			
			m_currentDrawingPath = new DrawingPath(m_currentPaint);
			m_currentBrush.mouseDown(m_currentDrawingPath.getPath(),
					motionEvent.getX(), motionEvent.getY());

			m_drawingSurface.mouseDown(m_currentBrush, 
					motionEvent.getX(), motionEvent.getY());

			m_drawingSurface.requestDraw();

			fixOrientation();

		} else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
			m_currentBrush.mouseMove( m_currentDrawingPath.getPath(),
					motionEvent.getX(), motionEvent.getY() );
			m_drawingSurface.mouseMove(m_currentBrush, 
					motionEvent.getX(), motionEvent.getY());
			m_drawingSurface.requestDraw();

		} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
			m_drawingSurface.mouseUp(m_currentBrush, 
					motionEvent.getX(), motionEvent.getY());

			m_currentBrush.mouseUp( m_currentDrawingPath.getPath(),
					motionEvent.getX(), motionEvent.getY() );
			m_drawingSurface.addDrawingPath(m_currentDrawingPath);
			
			m_drawingSurface.requestDraw();

			m_drawingSurface.clearPreviewPath();
			m_undoBtn.setEnabled(true);
			m_clearBtn.setEnabled(true);

			fixOrientation();
		}

		return true;
	}


	public void onClick(View view){
		switch (view.getId()){
		case R.id.undoBtn:
			m_drawingSurface.undo();
			if( m_drawingSurface.hasStack() == false ){
				m_undoBtn.setEnabled( false );
				m_clearBtn.setEnabled(false);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			break;

		case R.id.clearBtn:
			m_drawingSurface.clear();
			m_undoBtn.setEnabled( false );
			m_clearBtn.setEnabled(false);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			break;
		case R.id.closeBtn:
			m_drawingSurface.stopDraw();
			finish();
			break;
		/*
		case R.id.saveBtn:
			final Activity currentActivity  = this;
			Handler saveHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					Toast.makeText(currentActivity, R.string.file_saved, Toast.LENGTH_LONG).show();
				}
			} ;
			new ExportBitmapToFile(this,saveHandler, m_drawingSurface.getBitmap()).execute();
			break;
		*/
		}
	}

	/*

	private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> {
		private Handler mHandler;
		private Bitmap nBitmap;

		public ExportBitmapToFile(Context context,Handler handler,Bitmap bitmap) {
			nBitmap = bitmap;
			mHandler = handler;
		}

		@Override
		protected Boolean doInBackground(Intent... arg0) {
			try {
				if (!APP_FILE_PATH.exists()) {
					APP_FILE_PATH.mkdirs();
				}

				final FileOutputStream out = new FileOutputStream(new File(APP_FILE_PATH + "/myAwesomeDrawing.png"));
				nBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.flush();
				out.close();
				return true;
			}catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}


		@Override
		protected void onPostExecute(Boolean bool) {
			super.onPostExecute(bool);
			if ( bool ){
				mHandler.sendEmptyMessage(1);
			}
		}
	}
	*/
	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, Constants.MENU_SETTING, Menu.NONE, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューが表示される度に呼び出されます
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(Constants.MENU_SETTING).setVisible(m_visible);
		m_visible = !m_visible;
		return super.onPrepareOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		Intent intent;
		switch (item.getItemId()) {
		case Constants.MENU_SETTING:
			intent = new Intent(this, Config.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}
	private void setTheme() {
		String theme =
				PreferenceManager.getDefaultSharedPreferences(this).getString("handwriting_theme", "white");
		
		if (theme.equals("white")) {
			m_currentPaint.setColor(Color.BLACK);
		} else if (theme.equals("green")) {
			m_currentPaint.setColor(Color.rgb(0x8B, 0x00, 0x00));
		} else if (theme.equals("blue")) {
			m_currentPaint.setColor(Color.rgb(0xFA, 0xFA, 0xD2));
		} else {
			m_currentPaint.setColor(Color.BLACK);
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.REQUEST_CODE_SETTINGS) {
			setTheme();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}