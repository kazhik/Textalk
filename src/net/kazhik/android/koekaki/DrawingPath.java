package net.kazhik.android.koekaki;

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
    private Path path;
    private Paint paint;

    public Path getPath() {
    	return path;
    }
    public void initPath() {
    	path = new Path();
    }
    public void setPait(Paint paint) {
    	this.paint = paint;
    }
    public void draw(Canvas canvas) {
        canvas.drawPath( path, paint );
    }

    public void undo() {
        //Todo this would be changed later
    }
}
