package net.kazhik.android.textalk;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecognitionResultDialog extends DialogFragment {
	private OnResultListener m_listener;

	public interface OnResultListener {
		void onRetry();
		void onSelect(String text);
	}

	public RecognitionResultDialog() {
		super();
	}
	public static RecognitionResultDialog newInstance(OnResultListener listener,
			ArrayList<String> results) {
		
		RecognitionResultDialog frag = new RecognitionResultDialog();
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("results", results);
		frag.setListener(listener);
		frag.setArguments(bundle);
		return frag;
	}
	private void setListener(OnResultListener listener) {
		this.m_listener = listener;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();

		ArrayList<String> results = this.getArguments().getStringArrayList("results");
		
		// 認識結果を表示するListViewの設定
		ListView resultView = new ListView(activity);
		resultView.setScrollingCacheEnabled(false);

		ArrayAdapter<String> recogResults = new ArrayAdapter<>(activity,
				android.R.layout.simple_list_item_1);
		for (String result: results) {
			recogResults.add(result);
		}
		resultView.setAdapter(recogResults);

		// 再試行ボタン
		DialogInterface.OnClickListener retryListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_listener.onRetry();
			}
		};

		class SelectResultListener implements AdapterView.OnItemClickListener {
			private Dialog dialog;
			private SelectResultListener(Dialog dialog) {
				super();
				this.dialog = dialog;
			}
			@Override
			public void onItemClick(AdapterView<?> items, View view,
					int position, long id) {
				ListView listView = (ListView) items;
				String item = (String) listView.getItemAtPosition(position);
				m_listener.onSelect(item);

				this.dialog.dismiss();
			}
		}
		

		AlertDialog resultDialog = new AlertDialog.Builder(activity)
				.setPositiveButton(R.string.button_retry, retryListener)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
		
		resultView.setOnItemClickListener(new SelectResultListener(resultDialog));
		resultDialog.setView(resultView);

		return resultDialog;
	}	

}
