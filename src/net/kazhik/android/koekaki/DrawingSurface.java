package net.kazhik.android.koekaki;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 07/11/2010
 * Time: 2:15 AM
 * Link: http://www.tutorialforandroid.com/
 */
public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
	private Boolean mRun;
	protected DrawThread m_drawThread;
	private Bitmap mBitmap;
	public boolean m_isDrawing = true;
	public DrawingPath m_previewPath;

	private CommandManager m_commandManager;

	public DrawingSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().addCallback(this);


		m_commandManager = new CommandManager();
		m_drawThread = new DrawThread(getHolder());
	}

	private Handler previewDoneHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			m_isDrawing = false;
		}
	};

	class DrawThread extends  Thread{
		private SurfaceHolder mSurfaceHolder;


		public DrawThread(SurfaceHolder surfaceHolder){
			mSurfaceHolder = surfaceHolder;

		}

		public void setRunning(boolean run) {
			mRun = run;
		}


		@Override
		public void run() {
			Canvas canvas = null;
			while (mRun){
				if(m_isDrawing == true){
					try{
						canvas = mSurfaceHolder.lockCanvas(null);
						if(mBitmap == null){
							mBitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
						}
						final Canvas c = new Canvas (mBitmap);

						c.drawColor(0, PorterDuff.Mode.CLEAR);
						canvas.drawColor(0, PorterDuff.Mode.CLEAR);


						m_commandManager.executeAll(c,previewDoneHandler);
						m_previewPath.draw(c);

						canvas.drawBitmap (mBitmap, 0,  0,null);
					} finally {
						mSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}

			}

		}
	}


	public void addDrawingPath (DrawingPath drawingPath){
		m_commandManager.addCommand(drawingPath);
	}


	public void undo(){
		m_isDrawing = true;
		m_commandManager.undo();
	}
	public void clear(){
		m_isDrawing = true;
		m_commandManager.clear();
	}

	public boolean hasMoreUndo(){
		return m_commandManager.hasMoreUndo();
	}

	public Bitmap getBitmap(){
		return mBitmap;
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
		mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
	}


	public void surfaceCreated(SurfaceHolder holder) {
		m_drawThread.setRunning(true);
		m_drawThread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		m_drawThread.setRunning(false);
		while (retry) {
			try {
				m_drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

}
