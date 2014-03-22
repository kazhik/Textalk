package net.kazhik.android.textalk;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 10/11/2010
 * Time: 12:44 AM
 * Link: http://www.tutorialforandroid.com/
 * Modified by kazhik, 2011.
 */
public class DrawingPath {
	private Path m_path;
	private Paint m_paint;
	private float m_lastX = 0;
	private float m_lastY = 0;

	public DrawingPath(Paint paint) {
		m_paint = paint;
		m_path = new Path();
	}
	public DrawingPath(DrawingPath dpath) {
		m_paint = dpath.m_paint;
		m_path = dpath.m_path;
	}
	public Path getPath() {
		return m_path;
	}
	public void clearPath() {
		m_path.reset();
	}
	public void setPait(Paint paint) {
		this.m_paint = paint;
	}
	public void draw(Canvas canvas) {
		if (m_path.isEmpty()) {
			return;
		}
		canvas.drawPath( m_path, m_paint );
	}
	public void setStartPoint(float x, float y) {
		m_path.moveTo( x, y );
		m_lastX = x;
		m_lastY = y;
	}

	public boolean addLine(float x, float y) {
		if (x != m_lastX || y != m_lastY) {
			m_path.lineTo( x, y );
			return true;
		}
		return false;
	}


}
