package net.kazhik.android.textalk;

import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HandwritingView extends View {

	private Stack <Path> m_pathStack = new Stack<>();
	private PointF m_lastPt;
	private Path m_currentPath = new Path();
	private Paint m_currentPaint;
	private int m_backgroundColor;
	private Bitmap canvasBitmap;
	private Bitmap receivedBitmap;
	private Canvas  m_canvas;
	private static final String TAG = "HandwritingView";
	
	
	public HandwritingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		m_backgroundColor = Color.WHITE;
		m_currentPaint = new Paint();
		m_currentPaint.setStyle(Paint.Style.STROKE);
		m_currentPaint.setStrokeJoin(Paint.Join.ROUND);
		m_currentPaint.setStrokeCap(Paint.Cap.ROUND);
		m_currentPaint.setStrokeWidth(5);
		m_currentPaint.setColor(Color.DKGRAY);
		m_currentPaint.setDither(true);
		m_currentPaint.setFilterBitmap(true);
		m_currentPaint.setAntiAlias(true);

	}
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		this.canvasBitmap.eraseColor(this.m_backgroundColor);
		m_canvas = new Canvas(this.canvasBitmap);

		if (this.receivedBitmap != null) {
			this.receivedBitmap = Bitmap.createScaledBitmap(
					this.receivedBitmap,
					m_canvas.getWidth(),
					m_canvas.getHeight(),
					true);
			
		}
		Log.d(TAG, "onSizeChanged");
		
    
    }
	
	public void touchDown(PointF pt) {
		if (this.receivedBitmap != null) {
			this.receivedBitmap = null;
			this.canvasBitmap.eraseColor(this.m_backgroundColor);
		}
		m_currentPath.moveTo( pt.x, pt.y );
		m_lastPt = pt;
		invalidate();
	}
	public void touchMove(PointF pt) {
		if (!pt.equals(m_lastPt)) {
			m_currentPath.lineTo( pt.x, pt.y );
			m_lastPt = pt;
			invalidate();
		}
	}
	public void touchUp(PointF pt) {
		m_canvas.drawPath(m_currentPath, m_currentPaint);
		m_pathStack.push(new Path(m_currentPath));
	}

	public void undo(){
		if (!m_pathStack.isEmpty()) {
			m_pathStack.pop();
		}
		m_currentPath.reset();
		this.receivedBitmap = null;
		
		this.canvasBitmap.eraseColor(this.m_backgroundColor);
		for (Path path : m_pathStack) {
			this.m_canvas.drawPath(path, m_currentPaint);
		}

		invalidate();
	}
	public void clear(){
		m_pathStack.clear();
		m_currentPath.reset();
		this.receivedBitmap = null;
		this.canvasBitmap.eraseColor(this.m_backgroundColor);

		invalidate();
	}

	public boolean hasStack(){
		return m_pathStack.size() > 0;
	}

	public Bitmap getBitmap() {
		return this.canvasBitmap;
	}
	public void setBitmap(Bitmap bmp) {
		if (this.m_canvas != null) {
/*
			Matrix matrix = new Matrix();
			// Received: Landscape, Canvas: Portrait
			if (bmp.getHeight() < bmp.getWidth() &&
					m_canvas.getHeight() > m_canvas.getWidth()) {
				matrix.postRotate(90);
			} else if (bmp.getHeight() > bmp.getWidth() &&
					m_canvas.getHeight() < m_canvas.getWidth()) {
				matrix.postRotate(90);
			}
			
			this.receivedBitmap = Bitmap.createBitmap(bmp, 0, 0, m_canvas.getWidth(),
					m_canvas.getHeight(), matrix, true);
*/
			this.receivedBitmap = Bitmap.createScaledBitmap(
					bmp,
					m_canvas.getWidth(),
					m_canvas.getHeight(),
					true);
		} else {
			this.receivedBitmap = bmp;
		}
		m_pathStack.clear();
		m_currentPath.reset();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (this.receivedBitmap != null) {
			canvas.drawBitmap(this.receivedBitmap, 0, 0, null);
		} else {
			canvas.drawColor(m_backgroundColor);
			if (!m_pathStack.isEmpty()) {
				for (Path path : m_pathStack) {
					canvas.drawPath(path, m_currentPaint);
				}
			}
			if (!m_currentPath.isEmpty()) {
				canvas.drawPath(m_currentPath, m_currentPaint);
			}
		}

	}
	public Stack<Path> getDrawingPathStack() {
		return m_pathStack;
	}
	public void setDrawingPathStack(Stack<Path> pathStack) {
		this.m_pathStack = pathStack;
	}

}
