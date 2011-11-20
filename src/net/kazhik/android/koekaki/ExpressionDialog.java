/**
 * 
 */
package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.Iterator;

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

/**
 * @author kazhik
 *
 */
public class ExpressionDialog extends Dialog
	implements DialogInterface.OnShowListener, OnItemClickListener, View.OnClickListener {
	/*
	private static final int CONTEXTMENU_DELETEITEM = 2001;
	private static final int CONTEXTMENU_DELETEALL = 2002;
	*/
	private ExpressionTable m_expressionTable;
	private String m_selectedStr = "";

	/**
	 * @param context
	 */
	public ExpressionDialog(Context context, ExpressionTable expressionTable) {
		super(context);
		m_expressionTable = expressionTable;
		requestWindowFeature(Window.FEATURE_NO_TITLE);

	}

	public String getSelectedStr() {
		return m_selectedStr;
	}

	public void setSelectedStr(String selectedStr) {
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
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
		ArrayList<String> expressionsFromDB = m_expressionTable.getExpressions(max);
		for (Iterator<String> it = expressionsFromDB.iterator(); it.hasNext();) {
			expressions.add(it.next());
		}
		lvExpressions.setAdapter(expressions);

		return expressions.getCount();
	}
	public void onShow(DialogInterface dialog) {
		loadExpressions();
	}
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
