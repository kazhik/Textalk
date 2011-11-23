package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.graphics.Canvas;
import android.os.Handler;

/**
 * Created by IntelliJ IDEA.
 * User: almondmendoza
 * Date: 15/11/2010
 * Time: 12:23 AM
 * To change this template use File | Settings | File Templates.
 * Modified by kazhik, 2011.
 */
public class CommandManager {
	private List<DrawingPath> currentStack;

	public  CommandManager(){
		currentStack = Collections.synchronizedList(new ArrayList<DrawingPath>());
	}

	public void addCommand(DrawingPath command){
		currentStack.add(command);
	}

	public void undo (){
		final int length = currentStackLength();

		if ( length > 0) {
			final DrawingPath undoCommand = currentStack.get(  length - 1  );
			currentStack.remove( length - 1 );
			undoCommand.undo();
		}
	}
	public void clear (){
		currentStack.clear();
	}

	public int currentStackLength(){
		final int length = currentStack.toArray().length;
		return length;
	}


	public void executeAll( Canvas canvas, Handler doneHandler){
		if( currentStack != null ){
			synchronized( currentStack ) {
				final Iterator<DrawingPath> i = currentStack.iterator();

				while ( i.hasNext() ){
					final DrawingPath drawingPath = (DrawingPath) i.next();
					drawingPath.draw( canvas );
					//doneHandler.sendEmptyMessage(1);
				}
			}
		}
	}



	public boolean hasStack(){
		return  currentStack.toArray().length > 0;
	}

}
