package net.kazhik.android.textalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class WriteMessageDialog extends DialogFragment implements OnClickListener {
	private EditText m_input;
	private OnInputListener m_listener;

	public interface OnInputListener {
		public void onText(String text);
	}

	public static WriteMessageDialog newInstance(OnInputListener listener) {
		
		WriteMessageDialog frag = new WriteMessageDialog();
		frag.setListener(listener);
		return frag;
	}
	
	private void setListener(OnInputListener listener) {
		this.m_listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		
		LayoutInflater li = LayoutInflater.from(activity);
		View writeMsgView = li.inflate(R.layout.writemessage, null);
		m_input = (EditText) writeMsgView.findViewById(R.id.messageText);

		Dialog dlg = new AlertDialog.Builder(activity)
			.setView(writeMsgView)
			.setTitle(R.string.write_message)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create();

		class TextFocusListener implements View.OnFocusChangeListener {
			private Dialog dialog;

			public TextFocusListener(Dialog dialog) {
				super();
				this.dialog = dialog;
			}

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					this.dialog.getWindow().setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
				
			}
		}
		m_input.setOnFocusChangeListener(new TextFocusListener(dlg));
		
		return dlg;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == AlertDialog.BUTTON_POSITIVE) {
			m_listener.onText(m_input.getText().toString());
		}
//		dialog.cancel();
		
	}

}
