package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class KoeKakiActivity extends Activity {
	private static final int DIALOG_EXPRESSIONS = 200;
	private static final int DIALOG_RECOGNITIONRESULT = 201;
	private static final int DIALOG_CLEAR = 202;
	private static final int REQ_SPEAK = 1001;
	private ListView m_lvHistory;
	private ArrayAdapter<String> m_resultArray;
	private ListView m_lvRecogResults;
	private ArrayAdapter<String> m_recogResults;
	private AlertDialog m_dlgRecogResults = null;  
	private ExpressionDialog m_dlgExpression = null;
	private ExpressionTable m_expressionTable;
	private boolean m_visible = true;
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.koekaki);

		// 音声認識機能がない場合は「話す」ボタンを無効化
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

		
		initHistoryView();
		createRecognitionResultDialog();
		createExpressionDialog();
		createClearConfirmDialog();

//		findViewById(R.id.main).setBackgroundColor(0xffffffff);

	}
	private void initHistoryView()
	{
		m_resultArray = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		
		m_lvHistory = (ListView) findViewById(R.id.textList);

		m_lvHistory.setAdapter(m_resultArray);

	}
	private Dialog createClearConfirmDialog()
	{
		DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_expressionTable.clear();
			}
		};

		AlertDialog confirmDialog = new AlertDialog.Builder(this)
		.setMessage(R.string.confirm_clear)
		.setPositiveButton(android.R.string.ok, okListener)
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		
		return confirmDialog;
	}
	private Dialog createRecognitionResultDialog()
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
		.setNegativeButton(android.R.string.cancel, null)
		.setView(m_lvRecogResults)
		.create();
		
		return m_dlgRecogResults;
	}
	private Dialog createExpressionDialog()
	{
		m_dlgExpression = new ExpressionDialog(this, m_expressionTable);
		
		DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				String selectedStr = m_dlgExpression.getSelectedStr();
				if (selectedStr.length() > 0) {
					m_expressionTable.updateTimesUsed(selectedStr);
					m_resultArray.insert(selectedStr, 0);
					m_resultArray.notifyDataSetChanged();
					m_dlgExpression.setSelectedStr("");
				}
			}
		};
		m_dlgExpression.setOnDismissListener(dismissListener);
		
		return m_dlgExpression;
	}
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog d = super.onCreateDialog(id);
	    switch (id) {
	    case DIALOG_EXPRESSIONS:
	        d = createExpressionDialog();
	        break;
	    case DIALOG_RECOGNITIONRESULT:
	        d = createRecognitionResultDialog();
	        break;
	    case DIALOG_CLEAR:
	        d = createClearConfirmDialog();
	        break;
	    }
	    return d;
	}
	/**
	 * Handle the action of the button being clicked
	 */
	public void speakButtonClicked(View v)
	{
		startVoiceRecognitionActivity();
	}
	public void writeButtonClicked(View v)
	{
		
		startActivity(new Intent(KoeKakiActivity.this, HandwritingActivity.class));
		/*
		Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("jp.joker.smile.hitudan");
		startActivity( LaunchIntent );
		*/
	}
	/**
	 * Handle the action of the button being clicked
	 */
	public void expressionButtonClicked(View v)
	{
		String maxStr =
				PreferenceManager.getDefaultSharedPreferences(this).getString("expressions_max", "0");
		int max = Integer.parseInt(maxStr);
		
		if (m_expressionTable.getExpressions(max).size() == 0) {
			Toast.makeText(KoeKakiActivity.this, R.string.no_expressions, Toast.LENGTH_LONG).show();
			return;
		}
		showDialog(DIALOG_EXPRESSIONS);
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
			
			if (candidates.isEmpty()) {
				Toast.makeText(KoeKakiActivity.this, R.string.no_results, Toast.LENGTH_LONG).show();
				return;
			}
			
			m_recogResults.clear();
			for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
				m_recogResults.add(it.next());
			}

			m_lvRecogResults.setAdapter(m_recogResults);

			showDialog(DIALOG_RECOGNITIONRESULT);

		} else if (requestCode == KoeKakiConstants.REQUEST_CODE_SETTINGS) {
			if (resultCode == 200) {
				showDialog(DIALOG_CLEAR);
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, KoeKakiConstants.MENU_SETTING, Menu.NONE, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューが表示される度に呼び出されます
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(KoeKakiConstants.MENU_SETTING).setVisible(m_visible);
		m_visible = !m_visible;
		return super.onPrepareOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		Intent intent;
		switch (item.getItemId()) {
		case KoeKakiConstants.MENU_SETTING:
			intent = new Intent(this, KoeKakiConfig.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivityForResult(intent, KoeKakiConstants.REQUEST_CODE_SETTINGS);
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

}