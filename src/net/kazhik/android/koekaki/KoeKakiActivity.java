package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class KoeKakiActivity extends Activity {
	private static final int REQ_SPEAK = 1001;
	private ListView m_lvHistory;
	private ArrayAdapter<String> m_resultArray;
	private ListView m_lvRecogResults;
	private ArrayAdapter<String> m_recogResults;
	private AlertDialog m_dlgRecogResults = null;  
	private ExpressionDialog m_dlgExpression = null;
	private ExpressionTable m_expressionTable;
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koekaki);

		Button speakButton = (Button) findViewById(R.id.speakButton);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.isEmpty())
		{
			speakButton.setEnabled(false);
			Toast.makeText(KoeKakiActivity.this, "Recognizer not present", Toast.LENGTH_LONG).show();
			return;
		}
		m_expressionTable = new ExpressionTable(this);

		m_resultArray = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		initHistoryView();
		createRecognitionResultDialog();
		createExpressionDialog();


	}
	private void initHistoryView()
	{
		m_lvHistory = (ListView) findViewById(R.id.textList);

		m_lvHistory.setAdapter(m_resultArray);

		/*
		m_lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				String item = (String) listView.getItemAtPosition(position);
				Toast.makeText(KoeKakiActivity.this, item, Toast.LENGTH_LONG).show();
			}
		});
		m_lvHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				String item = (String) listView.getSelectedItem();
				Toast.makeText(KoeKakiActivity.this, item, Toast.LENGTH_LONG).show();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		*/
	}
	private void createRecognitionResultDialog()
	{
		m_recogResults = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		m_lvRecogResults = new ListView(this);
		m_lvRecogResults.setScrollingCacheEnabled(false);
		
		AdapterView.OnItemClickListener selectItemListener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				ListView listView = (ListView) items;
				String item = (String) listView.getItemAtPosition(position);
				m_resultArray.insert(item,  0);
				
				m_resultArray.notifyDataSetChanged();
				m_expressionTable.updateTimesUsed(item);
				m_dlgRecogResults.dismiss();
			}
		};
		m_lvRecogResults.setOnItemClickListener(selectItemListener);

		DialogInterface.OnClickListener retryListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startVoiceRecognitionActivity();
			}
		};
		
		m_dlgRecogResults = new AlertDialog.Builder(this)
		.setPositiveButton(R.string.button_retry, retryListener)
		.setNegativeButton(R.string.button_cancel, null)
		.setView(m_lvRecogResults)
		.create();
		
	}
	private void createExpressionDialog()
	{
		m_dlgExpression = new ExpressionDialog(this, m_expressionTable);
		
		DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (m_dlgExpression.getSelectedStr().length() > 0) {
					m_resultArray.insert(m_dlgExpression.getSelectedStr(), 0);
					m_resultArray.notifyDataSetChanged();
				}
			}
		};
		m_dlgExpression.setOnDismissListener(dismissListener);
	}

	/**
	 * Handle the action of the button being clicked
	 */
	public void speakButtonClicked(View v)
	{
		startVoiceRecognitionActivity();
	}
	/**
	 * Handle the action of the button being clicked
	 */
	public void expressionButtonClicked(View v)
	{
		m_dlgExpression.loadExpressions();
		m_dlgExpression.show();

	}
	/**
	 * Handle the action of the button being clicked
	 */
	public void freqButtonClicked(View v)
	{
	}

	/**
	 * Fire an intent to start the voice recognition activity.
	 */
	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.please_speak);
		startActivityForResult(intent, REQ_SPEAK);
	}

	/**
	 * Handle the results from the voice recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQ_SPEAK && resultCode == RESULT_OK)
		{
			final ArrayList<String> candidates = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			m_recogResults.clear();
			for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
				m_recogResults.add(it.next());
			}

			m_lvRecogResults.setAdapter(m_recogResults);

			m_dlgRecogResults.show();

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}