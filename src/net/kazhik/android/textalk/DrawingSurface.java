package net.kazhik.android.textalk;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.kazhik.android.textalk.brush.Brush;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:15 AM
 * Link: http://www.tutorialforandroid.com/
 * Modified by kazhik, 2011.
 */
public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
	protected DrawThread m_drawThread;
	private DrawingPathManager m_drawingPathManager = null;
	private DrawingPath m_previewPath = null;

	public DrawingSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().addCallback(this);

		m_drawingPathManager = new DrawingPathManager();
	}
	public void mouseUp(Brush brush, float x, float y) {
		brush.mouseUp(m_previewPath.getPath(), x, y);
	}
	public void mouseMove(Brush brush, float x, float y) {
		brush.mouseMove(m_previewPath.getPath(), x, y);
	}
	public void mouseDown(Brush brush, float x, float y) {
		brush.mouseDown(m_previewPath.getPath(), x, y);
	}
	public void requestDraw() {
		m_drawThread.requestDraw();
	}
	public void stopDraw() {
		m_drawThread.stopDraw();
		
	}
	public void setPreviewPath(Paint paint) {
		if (m_previewPath == null) {
			m_previewPath = new DrawingPath(paint);
		}
	}
	public void clearPreviewPath() {
		m_previewPath.clearPath();
	}

	public void addDrawingPath (DrawingPath drawingPath){
		m_drawingPathManager.addPath(drawingPath);
	}


	public void undo(){
		m_drawingPathManager.undo();
		m_drawThread.requestDraw();
	}
	public void clear(){
		m_drawingPathManager.clear();
		m_drawThread.requestDraw();
	}

	public boolean hasStack(){
		return m_drawingPathManager.hasStack();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
		m_drawThread.createBitmap(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (m_drawThread == null || m_drawThread.getState() == Thread.State.TERMINATED) {
			m_drawThread = new DrawThread(getHolder());
		}
		m_drawThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;

		Log.d("DrawingSurface", "surfaceDestroyed");
		m_drawThread.stopDraw();
		while (retry) {
			try {
				m_drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	public DrawingPathManager getDrawingPathManager() {
		return m_drawingPathManager;
	}

	public void setDrawingPathManager(DrawingPathManager commandManager) {
		this.m_drawingPathManager = commandManager;
	}
	class DrawThread extends  Thread{
		private SurfaceHolder m_SurfaceHolder;
		private Bitmap m_Bitmap;
		private Lock m_lock = new ReentrantLock();
		private Condition m_condition = m_lock.newCondition();

		public DrawThread(SurfaceHolder surfaceHolder){
			m_SurfaceHolder = surfaceHolder;

		}
		public void createBitmap(int width, int height) {
			m_Bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
			
		}

		public void stopDraw() {
			interrupt();
			
		}

		public void requestDraw() {
			m_lock.lock();
			m_condition.signal();
			m_lock.unlock();
		}

		private void drawAll(int bgColor) {
			Canvas canvas = m_SurfaceHolder.lockCanvas(null);
			if(m_Bitmap == null){
				m_Bitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
			}
			final Canvas c = new Canvas (m_Bitmap);

			c.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(bgColor);

			m_drawingPathManager.drawAllPath(c);
			m_previewPath.draw(c);

			canvas.drawBitmap (m_Bitmap, 0,  0, null);
			m_SurfaceHolder.unlockCanvasAndPost(canvas);
	
		}
		@Override
		public void run() {
			String theme = PreferenceManager.getDefaultSharedPreferences(getContext())
					.getString("handwriting_theme", "white");
			int bgColor = Color.WHITE;
			if (theme.equals("white")) {
				bgColor = Color.WHITE;
			} else if (theme.equals("green")) {
				bgColor = Color.rgb(0x98, 0xFB, 0x98);
			} else if (theme.equals("blue")) {
				bgColor = Color.rgb(0x4B, 0x00, 0x82);
			}
			drawAll(bgColor);
			Boolean bRunning = true;
			while (bRunning){
				try{
					m_lock.lock();
					m_condition.await();
					
					drawAll(bgColor);
				} catch (InterruptedException e) {
					Log.e("DrawThread", "interrupted");
					bRunning = false;
				} finally {
					m_lock.unlock();
				}
			}

		}
	}



}
