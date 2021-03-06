/**
 * 
 */
package net.kazhik.android.textalk;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

/**
 * @author kazhik
 *
 */
class HistoryDialog extends Dialog
    implements DialogInterface.OnShowListener, OnItemClickListener, View.OnClickListener {
    private HistoryTable m_historyTable;
    private String m_selectedStr = "";

    /**
     * @param context
     */
    HistoryDialog(Context context, HistoryTable historyTable) {
        super(context);
        m_historyTable = historyTable;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

    }

    String getSelectedStr() {
        return m_selectedStr;
    }

    void setSelectedStr(String selectedStr) {
        this.m_selectedStr = selectedStr;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        ListView lvHistory = (ListView) findViewById(R.id.history);
        lvHistory.setOnItemClickListener(this);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        setOnShowListener(this);

    }

    private int loadHistory() {

        ListView lvHistory = (ListView) findViewById(R.id.history);

        if (lvHistory == null) {
            return -1;
        }

        String maxStr =
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString("history_max", "0");
        int max = Integer.parseInt(maxStr);

        ArrayAdapter<String> history =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        List<String> historyFromDB = m_historyTable.getHistory(max);
        for (String phrase : historyFromDB) {
            history.add(phrase);
        }
        lvHistory.setAdapter(history);

        return history.getCount();
    }
    @Override
    public void onShow(DialogInterface dialog) {
        loadHistory();
    }
    @Override
    public void onItemClick(AdapterView<?> items, View view, int position, long id) {
        ListView listView = (ListView) items;
        m_selectedStr = (String) listView.getItemAtPosition(position);
        dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancelButton) {
            dismiss();
        }

    }

}
