package net.kazhik.android.koekaki;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
	private Boolean m_run = false;
	protected DrawThread m_drawThread;
	private Bitmap mBitmap;
	private boolean m_isDrawing = true;
	private DrawingPath m_previewPath = null;

	private CommandManager m_commandManager = null;

	public DrawingSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().addCallback(this);

		m_commandManager = new CommandManager();
	}
	public void setIsDrawing(boolean isDrawing) {
		m_isDrawing = isDrawing;
	}
	public DrawingPath getPreviewPath() {
		return m_previewPath;
	}
	public void setPreviewPath(Paint paint) {
		if (m_previewPath == null) {
			m_previewPath = new DrawingPath();
			m_previewPath.initPath();
		}
		m_previewPath.setPait(paint);
		
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
			m_run = run;
		}
		public boolean isRunning() {
			return m_run;
		}


		@Override
		public void run() {
			Canvas canvas = null;
			String theme = PreferenceManager.getDefaultSharedPreferences(getContext())
					.getString("handwriting_theme", "white");
			while (m_run){
				if(m_isDrawing == true){
					try{
						canvas = mSurfaceHolder.lockCanvas(null);
						if(mBitmap == null){
							mBitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
						}
						final Canvas c = new Canvas (mBitmap);

						c.drawColor(0, PorterDuff.Mode.CLEAR);
						canvas.drawColor(0, PorterDuff.Mode.CLEAR);
						if (theme.equals("white")) {
							canvas.drawColor(Color.WHITE);
						} else if (theme.equals("green")) {
							canvas.drawColor(Color.rgb(0x98, 0xFB, 0x98));
						} else if (theme.equals("blue")) {
							canvas.drawColor(Color.rgb(0x4B, 0x00, 0x82));
						} else {
							canvas.drawColor(Color.WHITE);
						}

						m_commandManager.executeAll(c, previewDoneHandler);
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

	public boolean hasStack(){
		return m_commandManager.hasStack();
	}

	public Bitmap getBitmap(){
		return mBitmap;
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
		mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
	}


	public void surfaceCreated(SurfaceHolder holder) {
		if (m_drawThread == null || m_drawThread.getState() == Thread.State.TERMINATED) {
			m_drawThread = new DrawThread(getHolder());
		}
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


	public CommandManager getCommandManager() {
		return m_commandManager;
	}


	public void setCommandManager(CommandManager commandManager) {
		this.m_commandManager = commandManager;
	}

}
