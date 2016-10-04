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

import java.util.ArrayList;

/**
 * @author kazhik
 *
 */
class ExpressionDialog extends Dialog
	implements DialogInterface.OnShowListener, OnItemClickListener, View.OnClickListener {
	private ExpressionTable m_expressionTable;
	private String m_selectedStr = "";

	/**
	 * @param context
	 */
	ExpressionDialog(Context context, ExpressionTable expressionTable) {
		super(context);
		m_expressionTable = expressionTable;
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
		setContentView(R.layout.expression);

		ListView lvExpressions = (ListView) findViewById(R.id.expression_list);
		lvExpressions.setOnItemClickListener(this);
		
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);
		
		setOnShowListener(this);
		
	}

	private int loadExpressions() {
		
		ListView lvExpressions = (ListView) findViewById(R.id.expression_list);
		
		if (lvExpressions == null) {
			return -1;
		}

		String maxStr =
				PreferenceManager.getDefaultSharedPreferences(getContext()).getString("expressions_max", "0");
		int max = Integer.parseInt(maxStr);
		
		ArrayAdapter<String> expressions =
				new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
		ArrayList<String> expressionsFromDB = m_expressionTable.getExpressions(max);
		for (String anExpressionsFromDB : expressionsFromDB) {
			expressions.add(anExpressionsFromDB);
		}
		lvExpressions.setAdapter(expressions);

		return expressions.getCount();
	}
	@Override
	public void onShow(DialogInterface dialog) {
		loadExpressions();
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
