package net.kazhik.android.koekaki;

import net.kazhik.android.koekaki.brush.Brush;
import net.kazhik.android.koekaki.brush.PenBrush;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
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
 */
public class HandwritingActivity extends Activity implements View.OnTouchListener{
	private DrawingSurface m_drawingSurface;
	private DrawingPath m_currentDrawingPath;
	private Paint m_currentPaint;

	private boolean m_visible = true;
	
	private Button m_undoBtn;
	private Button m_clearBtn;

	private Brush m_currentBrush;

//	private File APP_FILE_PATH = new File("/sdcard/KoeKaki");


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing_activity);

		setCurrentPaint();
		m_currentBrush = new PenBrush();

		m_drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
		m_drawingSurface.setOnTouchListener(this);
		m_drawingSurface.m_previewPath = new DrawingPath();
		m_drawingSurface.m_previewPath.path = new Path();
		m_drawingSurface.m_previewPath.paint = getPreviewPaint();

		m_undoBtn = (Button) findViewById(R.id.undoBtn);
		m_undoBtn.setEnabled(false);

		m_clearBtn = (Button) findViewById(R.id.clearBtn);
		m_clearBtn.setEnabled(false);

	}

	private void setCurrentPaint(){
		m_currentPaint = new Paint();
		m_currentPaint.setDither(false);
		m_currentPaint.setColor(0xFFFFFFFF);
		m_currentPaint.setStyle(Paint.Style.STROKE);
		m_currentPaint.setStrokeJoin(Paint.Join.ROUND);
		m_currentPaint.setStrokeCap(Paint.Cap.ROUND);
		m_currentPaint.setStrokeWidth(3);

	}

	private Paint getPreviewPaint(){
		final Paint previewPaint = new Paint();
		previewPaint.setColor(0xFFC1C1C1);
		previewPaint.setStyle(Paint.Style.STROKE);
		previewPaint.setStrokeJoin(Paint.Join.ROUND);
		previewPaint.setStrokeCap(Paint.Cap.ROUND);
		previewPaint.setStrokeWidth(3);
		return previewPaint;
	}




	public boolean onTouch(View view, MotionEvent motionEvent) {
		if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
			m_drawingSurface.m_isDrawing = true;

			m_currentDrawingPath = new DrawingPath();
			m_currentDrawingPath.paint = m_currentPaint;
			m_currentDrawingPath.path = new Path();
			m_currentBrush.mouseDown(m_currentDrawingPath.path, motionEvent.getX(), motionEvent.getY());
			m_currentBrush.mouseDown(m_drawingSurface.m_previewPath.path, motionEvent.getX(), motionEvent.getY());


		}else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
			m_drawingSurface.m_isDrawing = true;
			m_currentBrush.mouseMove( m_currentDrawingPath.path, motionEvent.getX(), motionEvent.getY() );
			m_currentBrush.mouseMove(m_drawingSurface.m_previewPath.path, motionEvent.getX(), motionEvent.getY());


		}else if(motionEvent.getAction() == MotionEvent.ACTION_UP){


			m_currentBrush.mouseUp(m_drawingSurface.m_previewPath.path, motionEvent.getX(), motionEvent.getY());
			m_drawingSurface.m_previewPath.path = new Path();
			m_drawingSurface.addDrawingPath(m_currentDrawingPath);

			m_currentBrush.mouseUp( m_currentDrawingPath.path, motionEvent.getX(), motionEvent.getY() );

			m_undoBtn.setEnabled(true);
			m_clearBtn.setEnabled(true);

		}

		return true;
	}


	public void onClick(View view){
		switch (view.getId()){
		case R.id.undoBtn:
			m_drawingSurface.undo();
			if( m_drawingSurface.hasMoreUndo() == false ){
				m_undoBtn.setEnabled( false );
			}
			break;

		case R.id.clearBtn:
			m_drawingSurface.clear();
			m_undoBtn.setEnabled( false );
			m_clearBtn.setEnabled(false);
			break;
		case R.id.closeBtn:
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
		menu.add(Menu.NONE, KoeKakiConstants.MENU_SETTING, Menu.NONE, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューが表示される度に呼び出されます
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(KoeKakiConstants.MENU_SETTING).setVisible(m_visible);
		m_visible = !m_visible;
		return super.onPrepareOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		Intent intent;
		switch (item.getItemId()) {
		case KoeKakiConstants.MENU_SETTING:
			intent = new Intent(this, KoeKakiConfig.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivityForResult(intent, KoeKakiConstants.REQUEST_CODE_SETTINGS);
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}
}