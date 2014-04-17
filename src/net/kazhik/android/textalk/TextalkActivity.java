package net.kazhik.android.textalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.kazhik.android.textalk.chat.ChatAdapter;
import net.kazhik.android.textalk.chat.ChatManager;
import net.kazhik.android.textalk.chat.ChatMessage;
import net.kazhik.android.textalk.chat.ChatService;
import net.kazhik.android.textalk.chat.ChatService.ChatBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TextalkActivity extends Activity implements
		ChatManager.ReceiveMessageListener, ServiceConnection {
	private static final String KEY_SPEAK_HISTORY = "speak_history";
	private static final int REQ_SPEAK = 1001;
	private static final int REQ_HANDWRITE = 1002;
	private ChatAdapter chatHistory;
	private ExpressionTable m_expressionTable;
	private static final String TAG = "TextalkActivity";
	private String myname;

	private ChatManager chatManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.textalk);

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
		
		this.chatHistory = new ChatAdapter(this);
		
		if (savedInstanceState != null) {
			ArrayList<ChatMessage> history = savedInstanceState
					.getParcelableArrayList(KEY_SPEAK_HISTORY);
			this.chatHistory.setMessageList(history);
		}
		ListView historyView = (ListView) findViewById(R.id.textList);

		historyView.setAdapter(this.chatHistory);
		
		this.myname =
				PreferenceManager.getDefaultSharedPreferences(this).getString("myname", "Textalk");

	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
		int count = this.chatHistory.getCount();
		ArrayList<ChatMessage> speakHistoryList = new ArrayList<ChatMessage>();
		for (int i = 0; i < count; i++) {
			speakHistoryList.add((ChatMessage)this.chatHistory.getItem(i));
		}
		outState.putParcelableArrayList(KEY_SPEAK_HISTORY, speakHistoryList);
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
				if (!selectedStr.isEmpty()) {
					showNewText(selectedStr);
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
		Intent intent = new Intent(TextalkActivity.this, HandwritingActivity.class);
		intent.putExtra("myname", this.myname);
		startActivityForResult(intent, REQ_HANDWRITE);
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
		if (requestCode == REQ_SPEAK && resultCode == RESULT_OK) {
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (results.isEmpty()) {
				Toast.makeText(TextalkActivity.this, R.string.no_results,
						Toast.LENGTH_LONG).show();
				return;
			}
			this.showRecognitionResultDialog(results);
		} else if (requestCode == REQ_HANDWRITE && resultCode == RESULT_OK) {
			String text = data.getStringExtra("text");
			String sender = data.getStringExtra("sender");
			
			this.showChatMessage(new ChatMessage(ChatMessage.RECEIVED, sender, text));
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	private void showRecognitionResultDialog(ArrayList<String> results) {
		class SelectTextListener implements
				RecognitionResultDialog.OnResultListener {
			@Override
			public void onRetry() {
				startVoiceRecognitionActivity();
			}

			@Override
			public void onSelect(String text) {
				showNewText(text);
			}

		}
		RecognitionResultDialog resultDialog = RecognitionResultDialog
				.newInstance(new SelectTextListener(), results);
		resultDialog.show(getFragmentManager(), "dialog");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	private void showChatMessage(ChatMessage msg) {
		class ShowChatMessage implements Runnable {
			private ChatAdapter chatAdapter;
			private ChatMessage msg;

			public ShowChatMessage(ChatAdapter chatAdapter, ChatMessage msg) {
				this.chatAdapter = chatAdapter;
				this.msg = msg;
			}

			@Override
			public void run() {
				this.chatAdapter.addMessage(msg);
				ListView historyView = (ListView) findViewById(R.id.textList);
				historyView.setSelection(this.chatAdapter.getCount() - 1);

			}
			
		}
		this.runOnUiThread(new ShowChatMessage(this.chatHistory, msg));

	}
	private void showNewText(String text) {
		try {
			this.chatManager.broadcastMessage(text);
		} catch (IOException e) {
			Log.e(TAG, "broadcast text", e);
		}
		this.showChatMessage(new ChatMessage(ChatMessage.SENT, this.myname, text));

		this.m_expressionTable.updateTimesUsed(text);
		
	}
	@Override
	public void onConnected(String ipaddr, String name) {
		String msg = getResources().getString(R.string.connected, name);
		this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, name, msg));
	}
	@Override
	public void onRenamed(String oldname, String newname) {
		String msg = getResources().getString(R.string.renamed, oldname, newname);
		this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, newname, msg));
		
	}
	@Override
	public void onDisconnected(String ipaddr, String name) {
		String msg = getResources().getString(R.string.disconnected, name);
		this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, name, msg));
		
	}

	@Override
	public void onMessage(String name, String msg) {
		this.showChatMessage(new ChatMessage(ChatMessage.RECEIVED, name, msg));
		
	}

	@Override
	public void onBitmap(String sender, String filename) {
		Intent intent = new Intent(TextalkActivity.this, HandwritingActivity.class);
		intent.putExtra("bitmap", filename);
		intent.putExtra("sender", sender);
		intent.putExtra("myname", this.myname);
		this.startActivityForResult(intent, REQ_HANDWRITE);
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (this.chatManager != null) {
			this.chatManager.pause();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (this.chatManager != null) {
			this.chatManager.resume();
		}

	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "bind service");
		Intent intent = new Intent(this, ChatService.class);

		this.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "unbind service");
		this.unbindService(this);
	}
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: " + name.toString());
		ChatBinder binder = (ChatBinder)service;
		this.chatManager = binder.getChatManager();
		this.chatManager.init(this.myname);
		this.chatManager.addReceiveMessageListener(this);
		
		this.chatManager.resume();
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisConnected: " + name.toString());
		
	}
}