package net.kazhik.android.textalk;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

import android.graphics.Canvas;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 15/11/2010
 * Time: 12:23 AM
 * To change this template use File | Settings | File Templates.
 * Modified by kazhik, 2011.
 */
public class DrawingPathManager {
	private Stack <DrawingPath> m_pathStack = null;

	public DrawingPathManager(){
		m_pathStack = new Stack<DrawingPath>();
	}

	public void addPath(DrawingPath path){
		m_pathStack.push(path);
	}

	public void undo (){
		try {
			m_pathStack.pop();
		} catch (EmptyStackException e) {
		}
	}
	public void clear (){
		m_pathStack.clear();
	}

	public void drawAllPath(Canvas canvas){
		if( m_pathStack == null ){
			return;
		}
		synchronized( m_pathStack ) {
			for (Iterator<DrawingPath> i = m_pathStack.iterator(); i.hasNext(); ) {
				i.next().draw(canvas);
			}
		}
	}
	public boolean hasStack(){
		return  m_pathStack.size() > 0;
	}

}
