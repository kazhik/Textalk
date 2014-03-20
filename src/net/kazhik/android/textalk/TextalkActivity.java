package net.kazhik.android.textalk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TextalkActivity extends Activity {
	private static final String KEY_SPEAK_HISTORY = "speak_history";
	private static final int REQ_SPEAK = 1001;
	private ArrayAdapter<String> m_speakHistory;
	private ExpressionTable m_expressionTable;

	private WifiP2pManager m_wifiManager;
	private WifiP2pManager.Channel m_wifiChannel;
	private WifiBroadcastReceiver m_wifiReceiver;
	private IntentFilter m_IntentFilter;

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

		if (savedInstanceState != null) {
			ArrayList<String> speakHistory = savedInstanceState
					.getStringArrayList(KEY_SPEAK_HISTORY);
			for (String history : speakHistory) {
				m_speakHistory.add(history);
			}
		}

	    m_IntentFilter = new IntentFilter();
	    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    m_IntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	    
	    m_wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    m_wifiChannel = m_wifiManager.initialize(this, getMainLooper(), null);

		boolean chatWifi = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("chat_wifi", false);
		if (chatWifi) {
		    this.discoverPeers();
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
		int count = m_speakHistory.getCount();
		ArrayList<String> speakHistoryList = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			speakHistoryList.add(m_speakHistory.getItem(i));
		}
	    outState.putStringArrayList(KEY_SPEAK_HISTORY, speakHistoryList);
	}

	private void initHistoryView()
	{
		m_speakHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		
		ListView historyView = (ListView) findViewById(R.id.textList);

		historyView.setAdapter(m_speakHistory);

	}
	private void showClearConfirmDialog()
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

		confirmDialog.show();
	}

	private void showRecognitionResultDialog(ArrayList<String> results)
	{
		class SelectTextListener implements RecognitionResultDialog.OnResultListener {
			@Override
			public void onRetry() {
				startVoiceRecognitionActivity();
			}
			@Override
			public void onSelect(String text) {
				showNewText(text);
			}
			
		}
		RecognitionResultDialog resultDialog =
				RecognitionResultDialog.newInstance(new SelectTextListener(), results);
		resultDialog.show(getFragmentManager(), "dialog");
	}
	private void showAboutDialog() 
	{
		AboutDialog aboutDialog = AboutDialog.newInstance();
		aboutDialog.show(getFragmentManager(), "dialog");
		
	}
	
	private void showExpressionDialog()
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
		expDialog.show();
	}
	public void onClickSpeakButton(View v)
	{
		startVoiceRecognitionActivity();
	}
	public void onClickWriteButton(View v)
	{
		class InputMsgListener implements WriteMessageDialog.OnInputListener {

			@Override
			public void onText(String text) {
				showNewText(text);
			}
			
		}
		WriteMessageDialog msgDialog = WriteMessageDialog.newInstance(new InputMsgListener());
		msgDialog.show(getFragmentManager(), "dialog");
		
	}
	public void onClickHandwriteButton(View v)
	{
		
		startActivity(new Intent(TextalkActivity.this, HandwritingActivity.class));
	}

	private void openHistoryDialog()
	{
		String maxStr =
				PreferenceManager.getDefaultSharedPreferences(this).getString("expressions_max", "0");
		int max = Integer.parseInt(maxStr);
		
		if (m_expressionTable == null) {
			return;
		}
		
		ArrayList<String> exList = m_expressionTable.getExpressions(max);
		if (exList.size() == 0) {
			return;
		}
		
		if (m_expressionTable.getExpressions(max).isEmpty()) {
			Toast.makeText(TextalkActivity.this, R.string.no_expressions, Toast.LENGTH_LONG).show();
			return;
		}
		showExpressionDialog();
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
			
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, Constants.MENU_SETTING, Menu.NONE,
				R.string.menu_settings)
				.setIcon(android.R.drawable.ic_menu_preferences);

		menu.add(Menu.NONE, Constants.MENU_PICK_HISTORY, Menu.NONE,
				R.string.menu_pick_history);
		
		menu.add(Menu.NONE, Constants.MENU_CLEAR_HISTORY, Menu.NONE,
				R.string.menu_clear_history)
				.setIcon(android.R.drawable.ic_menu_delete);

		Resources res = getResources();
		menu.add(
				Menu.NONE,
				Constants.MENU_ABOUT,
				Menu.NONE,
				res.getString(R.string.menu_about, res.getString(R.string.app_name))
			)
			.setIcon(android.R.drawable.ic_menu_info_details);
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
			startActivity(intent);
			ret = true;
			break;
		case Constants.MENU_PICK_HISTORY:
			openHistoryDialog();
			break;
		case Constants.MENU_CLEAR_HISTORY:
			showClearConfirmDialog();
			break;
		case Constants.MENU_ABOUT:
			showAboutDialog();
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(m_wifiReceiver);

	}
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.d("TextalkActivity", "registerReceiver");
	    m_wifiReceiver = new WifiBroadcastReceiver(m_wifiManager, m_wifiChannel);
		registerReceiver(m_wifiReceiver, m_IntentFilter);

	}
	
	private void discoverPeers() {
		m_wifiManager.discoverPeers(m_wifiChannel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				Log.d("TextalkActivity", "discoverPeers success");
			}

			@Override
			public void onFailure(int reasonCode) {
				Log.d("TextalkActivity", "discoverPeers failure: " + reasonCode);
			}
		});
	}
	private void showNewText(String text) {
		String insertPos = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("history_insert_position", "top");
		if (insertPos.equals("top")) {
			m_speakHistory.insert(text, 0);
		} else {
			m_speakHistory.add(text);
		}

		m_speakHistory.notifyDataSetChanged();
		m_expressionTable.updateTimesUsed(text);
	}

	
}