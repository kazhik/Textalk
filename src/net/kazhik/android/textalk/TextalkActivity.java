package net.kazhik.android.textalk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TextalkActivity extends Activity {
	private static final int DIALOG_EXPRESSIONS = 200;
	private static final int DIALOG_ABOUT = 201;
	private static final int DIALOG_CLEAR = 202;
	private static final int REQ_SPEAK = 1001;
	private ArrayAdapter<String> m_speakHistory;
	private ExpressionTable m_expressionTable;
	
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.textalk);

		// 音声認識機能がない場合は「話す」ボタンを無効化
		Button speakButton = (Button) findViewById(R.id.speakButton);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.isEmpty())
		{
			speakButton.setEnabled(false);
			Toast.makeText(TextalkActivity.this, "Recognizer not present", Toast.LENGTH_LONG).show();
			return;
		}
		m_expressionTable = new ExpressionTable(this);

		
		initHistoryView();
//		createExpressionDialog();
//		createClearConfirmDialog();

		// 回転時に履歴が消えないようにする
	    @SuppressWarnings("unchecked")
		final ArrayAdapter<String> history = (ArrayAdapter<String>) getLastNonConfigurationInstance();
	    if (history != null) {
	    	m_speakHistory = history;
	    }

	}
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayAdapter<String> history = m_speakHistory;
	    return history;
	}
	private void initHistoryView()
	{
		m_speakHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		
		ListView historyView = (ListView) findViewById(R.id.textList);

		historyView.setAdapter(m_speakHistory);

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
	private void showRecognitionResultDialog(ArrayList<String> results)
	{
		// 再試行ボタン
		DialogInterface.OnClickListener retryListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startVoiceRecognitionActivity();
			}
		};

		final AlertDialog resultDialog = new AlertDialog.Builder(this)
		.setPositiveButton(R.string.button_retry, retryListener)
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		
		// 認識結果を表示するListViewの設定
		ListView resultView = new ListView(this);
		resultView.setScrollingCacheEnabled(false);

		ArrayAdapter<String> recogResults
			= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (Iterator<String> it = results.iterator(); it.hasNext();) {
			recogResults.add(it.next());
		}
		resultView.setAdapter(recogResults);
		
		AdapterView.OnItemClickListener selectItemListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				ListView listView = (ListView) items;
				String item = (String) listView.getItemAtPosition(position);
				m_speakHistory.insert(item,  0);
				
				m_speakHistory.notifyDataSetChanged();
				m_expressionTable.updateTimesUsed(item);
				
				resultDialog.dismiss();
			}
		};
		resultView.setOnItemClickListener(selectItemListener);

		resultDialog.setView(resultView);

		resultDialog.show();
		
	}
	private Dialog createAboutDialog()
	{
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		Resources res = getResources();
		StringBuffer aboutText = new StringBuffer();
		aboutText.append(res.getString(R.string.app_name));
		aboutText.append("\n\n");
		aboutText.append("Version: " + pkgInfo.versionName);
		aboutText.append("\n");
		aboutText.append("Website: github.com/kazhik/Textalk");
		final SpannableString sstr = new SpannableString(aboutText.toString());
		Linkify.addLinks(sstr, Linkify.ALL);

		final AlertDialog d = new AlertDialog.Builder(this)
		.setPositiveButton(android.R.string.ok, null)
		.setMessage(sstr)
		.create();

		d.show();

		// Make the textview clickable. Must be called after show()
		((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		
		return d;
	}
	
	private Dialog createExpressionDialog()
	{
		final ExpressionDialog expDialog = new ExpressionDialog(this, m_expressionTable);
		
		DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				String selectedStr = expDialog.getSelectedStr();
				if (selectedStr.length() > 0) {
					m_expressionTable.updateTimesUsed(selectedStr);
					m_speakHistory.insert(selectedStr, 0);
					m_speakHistory.notifyDataSetChanged();
					expDialog.setSelectedStr("");
				}
			}
		};
		expDialog.setOnDismissListener(dismissListener);
		return expDialog;
	}
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog d = super.onCreateDialog(id);
	    switch (id) {
	    case DIALOG_EXPRESSIONS:
	        d = createExpressionDialog();
	        break;
	    case DIALOG_CLEAR:
	        d = createClearConfirmDialog();
	        break;
	    case DIALOG_ABOUT:
	        d = createAboutDialog();
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
		
		startActivity(new Intent(TextalkActivity.this, HandwritingActivity.class));
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
			Toast.makeText(TextalkActivity.this, R.string.no_expressions, Toast.LENGTH_LONG).show();
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
		String lang =
				PreferenceManager.getDefaultSharedPreferences(this).getString("recognition_language", "default");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		if (lang.equals("default")) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
		} else {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
		}
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
			ArrayList<String> results = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			
			if (results.isEmpty()) {
				Toast.makeText(TextalkActivity.this, R.string.no_results, Toast.LENGTH_LONG).show();
				return;
			}
			showRecognitionResultDialog(results);
			

		} else if (requestCode == Constants.REQUEST_CODE_SETTINGS) {
			if (resultCode == Constants.RESULT_CODE_CLEAR) {
				showDialog(DIALOG_CLEAR);
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, Constants.MENU_SETTING, Menu.NONE, R.string.menu_settings)
		.setIcon(android.R.drawable.ic_menu_preferences);
		Resources res = getResources();
		menu.add(Menu.NONE, Constants.MENU_ABOUT, Menu.NONE,
				res.getString(R.string.menu_about, 
						res.getString(R.string.app_name)))
						.setIcon(android.R.drawable.ic_dialog_info);
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューが表示される度に呼び出されます
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		Intent intent;
		switch (item.getItemId()) {
		case Constants.MENU_SETTING:
			intent = new Intent(this, Config.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
			ret = true;
			break;
		case Constants.MENU_ABOUT:
			showDialog(DIALOG_ABOUT);
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

}