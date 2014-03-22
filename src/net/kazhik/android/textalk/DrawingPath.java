package net.kazhik.android.textalk;

import android.graphics.Path;
import android.graphics.PointF;

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
	private PointF m_lastPt;

	public DrawingPath() {
		m_path = new Path();
	}
	public DrawingPath(DrawingPath dpath) {
		m_path = dpath.m_path;
	}
	public Path getPath() {
		return m_path;
	}
	public void clearPath() {
		m_path.reset();
	}

	public void setStartPoint(PointF pt) {
		m_path.moveTo( pt.x, pt.y );
		m_lastPt = pt;
	}

	public boolean addLine(PointF pt) {
		if (!pt.equals(m_lastPt)) {
			m_path.lineTo( pt.x, pt.y );
			m_lastPt = pt;
			return true;
		}
		return false;
	}


}
