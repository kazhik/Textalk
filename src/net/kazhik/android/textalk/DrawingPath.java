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
public class DrawingPath implements ICanvasCommand{
	private Path m_path;
	private Paint m_paint;

	public DrawingPath(Paint paint) {
		m_paint = paint;
		m_path = new Path();
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
	@Override
	public void draw(Canvas canvas) {
		if (m_path.isEmpty()) {
			return;
		}
		canvas.drawPath( m_path, m_paint );
	}

	@Override
	public void undo() {
		//Todo this would be changed later
	}
}
