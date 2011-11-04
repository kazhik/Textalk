package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
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
    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;
    private ArrayAdapter<String> resultList;
    private AlertDialog m_Dlg = null;  
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_recog);
 
        Button speakButton = (Button) findViewById(R.id.speakButton);
        
        resultList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
 
        
        wordsList = (ListView) findViewById(R.id.list);
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        wordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(KoeKakiActivity.this, item, Toast.LENGTH_LONG).show();
            }
        });
        // リストビューのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        wordsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // 選択されたアイテムを取得します
                String item = (String) listView.getSelectedItem();
                Toast.makeText(KoeKakiActivity.this, item, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
 
        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
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
    public void patternButtonClicked(View v)
    {
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak slowly and clearly");
        startActivityForResult(intent, REQUEST_CODE);
    }
 
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            final ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            
            ListView lv = new ListView(this);
            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            lv.setScrollingCacheEnabled(false);
            lv.setOnItemClickListener(new OnItemClickListener(){
            	public void onItemClick(AdapterView<?> items, View view, int position, long id) {
            		m_Dlg.dismiss();
            		resultList.add(matches.get(position).toString());
            		wordsList.setAdapter(resultList);
//            		Toast.makeText(KoeKakiActivity.this, matches.get(position).toString(), Toast.LENGTH_LONG).show();
            	}
            });

            // ダイアログを表示
            m_Dlg = new AlertDialog.Builder(this)
            .setTitle("Please choose")
            .setPositiveButton("Cancel", null)
            .setView(lv)
            .create();

            m_Dlg.show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}