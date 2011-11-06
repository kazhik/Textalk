/**
 * 
 */
package net.kazhik.android.koekaki;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

/**
 * @author kazhik
 *
 */
public class RecognitionResult extends AlertDialog {

	/**
	 * @param context
	 */
	public RecognitionResult(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param theme
	 */
	public RecognitionResult(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public RecognitionResult(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recognition_result);
	}

}
