package net.kazhik.android.textalk;

import java.util.EmptyStackException;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class HandwritingView extends View {

	private Stack <Path> m_pathStack = new Stack<Path>();
	private PointF m_lastPt;
	private Path m_currentPath = new Path();
	private Paint m_currentPaint;
	private int m_backgroundColor;
	private Bitmap m_bitmap;
	private Canvas  m_canvas;
	
	public HandwritingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		m_backgroundColor = Color.WHITE;
		m_currentPaint = new Paint();
		m_currentPaint.setDither(false);
		m_currentPaint.setStyle(Paint.Style.STROKE);
		m_currentPaint.setStrokeJoin(Paint.Join.ROUND);
		m_currentPaint.setStrokeCap(Paint.Cap.ROUND);
		m_currentPaint.setStrokeWidth(5);
		m_currentPaint.setColor(Color.DKGRAY);

	}
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		m_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		m_canvas = new Canvas(m_bitmap);
    }
	
	public void touchDown(PointF pt) {
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
		try {
			m_pathStack.pop();
		} catch (EmptyStackException e) {
		}
		m_currentPath.reset();
		invalidate();
	}
	public void clear(){
		m_pathStack.clear();
		m_currentPath.reset();
		invalidate();
	}

	public boolean hasStack(){
		return m_pathStack.size() > 0;
	}

	public Bitmap getBitmap() {
		return m_bitmap;
	}
	public void setBitmap(Bitmap bmp) {
		this.m_bitmap = bmp;
	}

	@Override
	protected void onDraw(Canvas canvas) {
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

	public Stack<Path> getDrawingPathStack() {
		return m_pathStack;
	}
	public void setDrawingPathStack(Stack<Path> pathStack) {
		this.m_pathStack = pathStack;
	}

}
