package net.kazhik.android.textalk;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
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

import net.kazhik.android.textalk.chat.ChatAdapter;
import net.kazhik.android.textalk.chat.ChatManager;
import net.kazhik.android.textalk.chat.ChatMessage;
import net.kazhik.android.textalk.chat.ChatService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextalkActivity extends ChatActivity {
    private static final String KEY_SPEAK_HISTORY = "speak_history";
    private static final int REQ_SPEAK = 1001;
    private static final int REQ_HANDWRITE = 1002;
    private ChatAdapter chatHistory;
    private HistoryTable m_historyTable;
    private static final String TAG = "TextalkActivity";

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
        m_historyTable = new HistoryTable(this);

        this.chatHistory = new ChatAdapter(this);

        if (savedInstanceState != null) {
            ArrayList<ChatMessage> history = savedInstanceState
                    .getParcelableArrayList(KEY_SPEAK_HISTORY);
            this.chatHistory.setMessageList(history);
        }
        ListView historyView = (ListView) findViewById(R.id.textList);

        historyView.setAdapter(this.chatHistory);

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.chatHistory == null) {
            return;
        }
        int count = this.chatHistory.getCount();
        ArrayList<ChatMessage> speakHistoryList = new ArrayList<>();
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
                m_historyTable.clear();
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

    private void showHistoryDialog()
    {
        final HistoryDialog historyDialog = new HistoryDialog(this, m_historyTable);

        DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String selectedStr = historyDialog.getSelectedStr();
                if (!selectedStr.isEmpty()) {
                    showNewText(selectedStr);
                    historyDialog.setSelectedStr("");
                }
            }
        };
        historyDialog.setOnDismissListener(dismissListener);
        historyDialog.show();
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
        startActivityForResult(intent, REQ_HANDWRITE);
    }

    private void openHistoryDialog()
    {
        String maxStr =
                PreferenceManager.getDefaultSharedPreferences(this).getString("history_max", "0");
        int max = Integer.parseInt(maxStr);

        if (m_historyTable == null) {
            return;
        }

        List<String> exList = m_historyTable.getHistory(max);
        if (exList.size() == 0) {
            return;
        }

        if (m_historyTable.getHistory(max).isEmpty()) {
            Toast.makeText(TextalkActivity.this, R.string.no_history, Toast.LENGTH_LONG).show();
            return;
        }
        showHistoryDialog();
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

            private ShowChatMessage(ChatAdapter chatAdapter, ChatMessage msg) {
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
        this.showChatMessage(new ChatMessage(ChatMessage.SENT, this.getDeviceName(), text));

        this.m_historyTable.updateTimesUsed(text);

    }
    @Override
    public void onConnected(String ipaddr, String name) {
        super.onConnected(ipaddr, name);

        String msg = getResources().getString(R.string.connected, name);
        this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, name, msg));
    }
    @Override
    public void onDisconnected(String ipaddr, String name) {
        super.onDisconnected(ipaddr, name);

        String msg = getResources().getString(R.string.disconnected, name);
        this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, name, msg));
    }

    @Override
    public void onRenamed(String oldname, String newname) {
        String msg = getResources().getString(R.string.renamed, oldname, newname);
        this.showChatMessage(new ChatMessage(ChatMessage.SYSTEM, newname, msg));

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
        this.startActivityForResult(intent, REQ_HANDWRITE);

    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);

        ChatService.ChatBinder binder = (ChatService.ChatBinder)iBinder;
        this.chatManager = binder.getChatManager();

        this.chatManager.resume();
    }
}