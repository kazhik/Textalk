/**
 * 
 */
package net.kazhik.android.koekaki;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
public class ExpressionDialog extends Dialog {
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

	public void cancelButtonClicked(View v)
	{
		// TODO: this eventhandler doesn't work
		dismiss();

	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
		setContentView(R.layout.expression);
		} catch (NullPointerException e) {
		}

		ListView lvExpressions = (ListView) findViewById(R.id.expression_list);
		
		AdapterView.OnItemClickListener selectItemListener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				ListView listView = (ListView) items;
				m_selectedStr = (String) listView.getItemAtPosition(position);
				m_expressionTable.updateTimesUsed(m_selectedStr);
				dismiss();
			}
		};
		lvExpressions.setOnItemClickListener(selectItemListener);
		
		/*　コンテキストメニューを作るためにはDialogをActivityに変える必要がある
		View.OnCreateContextMenuListener contextmenuListener = new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				
				
				MenuItem.OnMenuItemClickListener deleteItemListener =
						new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						return false;
					}
				};
				MenuItem.OnMenuItemClickListener deleteAllListener =
						new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						return false;
					}
				};
				
				menu.add(0, CONTEXTMENU_DELETEITEM, 1, R.string.menu_deleteitem);
				menu.add(0, CONTEXTMENU_DELETEALL, 2, R.string.menu_deleteall);
				menu.findItem(CONTEXTMENU_DELETEITEM).setOnMenuItemClickListener(deleteItemListener);
				menu.findItem(CONTEXTMENU_DELETEALL).setOnMenuItemClickListener(deleteAllListener);
				
			}
		};
		lvExpressions.setOnCreateContextMenuListener(contextmenuListener);
		*/
		
		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		};
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(clickListener);
		
		loadExpressions();
	}
	public void loadExpressions() {
		ListView lvExpressions = (ListView) findViewById(R.id.expression_list);
		
		if (lvExpressions == null) {
			return;
		}
		ArrayAdapter<String> expressions;
		
		expressions = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
		ArrayList<String> expressionsFromDB = m_expressionTable.getExpressions();
		/*
		if (expressionsFromDB.isEmpty()) {
			expressionsFromDB = m_expressionTable.initialize();
		}
		*/
		for (Iterator<String> it = expressionsFromDB.iterator(); it.hasNext();) {
			expressions.add(it.next());
		}
		lvExpressions.setAdapter(expressions);
		
	}

}
