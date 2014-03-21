package net.kazhik.android.textalk;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
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
	private Stack <DrawingPath> m_pathStack;
	private DrawingPath m_currentDrawingPath = null;
	private Paint m_currentPaint;
	private int m_backgroundColor;

	public DrawingSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().addCallback(this);

		m_pathStack = new Stack<DrawingPath>();

//		m_backgroundColor = R.color.textalk_color;
		m_backgroundColor = Color.WHITE;
	}
	public void mouseDown(float x, float y) {
		m_currentDrawingPath = new DrawingPath(m_currentPaint);
		m_currentDrawingPath.mouseDown(x, y);
		m_drawThread.requestDraw();
	}
	public void mouseMove(float x, float y) {
		m_currentDrawingPath.mouseMove(x, y);
		m_drawThread.requestDraw();
	}
	public void mouseUp(float x, float y) {
		m_currentDrawingPath.mouseUp(x, y);
		m_pathStack.push(m_currentDrawingPath);
		m_drawThread.requestDraw();
	}
	public void stopDraw() {
		m_drawThread.stopDraw();

	}
	public void setPaint(Paint paint) {
		m_currentPaint = paint;
	}


	public void undo(){
		try {
			m_pathStack.pop();
		} catch (EmptyStackException e) {
		}
		m_currentDrawingPath.clearPath();
		m_drawThread.requestDraw();
	}
	public void clear(){
		m_pathStack.clear();
		m_currentDrawingPath.clearPath();
		m_drawThread.requestDraw();
	}

	public boolean hasStack(){
		return m_pathStack.size() > 0;
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

	public Stack<DrawingPath> getDrawingPathStack() {
		return m_pathStack;
	}
	public void setDrawingPathStack(Stack<DrawingPath> pathStack) {
		this.m_pathStack = pathStack;
	}
	class DrawThread extends Thread {
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
			try {
				m_condition.signal();
			} finally {
				m_lock.unlock();
			}
		}

		private synchronized void drawAllPath(Canvas c) {
			if( m_pathStack == null ){
				return;
			}
			synchronized( m_pathStack ) {
				for (Iterator<DrawingPath> i = m_pathStack.iterator(); i.hasNext(); ) {
					i.next().draw(c);
				}
			}

		}
		private void drawAll() {
			Canvas canvas = m_SurfaceHolder.lockCanvas(null);
			if(m_Bitmap == null){
				m_Bitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
			}
			final Canvas c = new Canvas (m_Bitmap);

			c.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(m_backgroundColor);

			drawAllPath(c);
			if (m_currentDrawingPath != null) {
				m_currentDrawingPath.draw(c);
			}

			canvas.drawBitmap (m_Bitmap, 0,  0, null);
			m_SurfaceHolder.unlockCanvasAndPost(canvas);

		}
		@Override
		public void run() {
			drawAll();
			Boolean bRunning = true;
			while (bRunning){
				try{
					m_lock.lock();
					m_condition.await();

					drawAll();
				} catch (InterruptedException e) {
					bRunning = false;
				} finally {
					m_lock.unlock();
				}
			}

		}
	}



}
