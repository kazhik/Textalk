/**
 * 
 */
package net.kazhik.android.koekaki;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author kazhik
 *
 */
public class ExpressionTable {
	public static final String DB_NAME = "koekaki.db";
	public static final String TABLE_NAME = "t_expression";
	
	public static final String EXPRESSION = "expression";
	public static final String SORTORDER = "sortorder";
	public static final String TIMESUSED = "timesused";
	
    private DatabaseHelper m_databaseHelper;
	
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, 5);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
            		+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
            		+ EXPRESSION + " TEXT,"
            		+ SORTORDER + " INTEGER,"
            		+ TIMESUSED + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

        }
    }

	public ArrayList<String> initialize() {
		ArrayList<String> expressions = new ArrayList<String>();
		
		SQLiteDatabase db = m_databaseHelper.getWritableDatabase();
		try {
			for (int i = 0; i < expressions.size(); i++ ) {
				ContentValues values = new ContentValues();
				values.put(ExpressionTable.EXPRESSION, expressions.get(i));
				values.put(ExpressionTable.SORTORDER, i + 1);
				values.put(ExpressionTable.TIMESUSED, 0);
				db.insert(TABLE_NAME, null, values);
			}
		} catch (IllegalArgumentException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}

		return new ArrayList<String>(expressions);
	}

	public ExpressionTable(Context context) {
        m_databaseHelper = new DatabaseHelper(context);
	}

	public void updateTimesUsed(String expression) {
		SQLiteDatabase db = m_databaseHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		String[] columns = { ExpressionTable.TIMESUSED };
		String selection = "expression = ?";
		String[] selectionArgs = {expression};

		try {
			Cursor cursor = null;
			cursor = qb.query(db, columns, selection, selectionArgs, null,
					null, null);
			if (cursor != null && cursor.getCount() == 1) {
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(ExpressionTable.TIMESUSED, cursor.getInt(0) + 1);
				db.update(TABLE_NAME, values, "expression = ?", selectionArgs);
			} else {
				ContentValues values = new ContentValues();
				values.put(ExpressionTable.EXPRESSION, expression);
				values.put(ExpressionTable.SORTORDER, 0);
				values.put(ExpressionTable.TIMESUSED, 1);
				db.insert(TABLE_NAME, null, values);
			}
		} catch (SQLiteException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}

	}
    public void clear() {
		SQLiteDatabase db = m_databaseHelper.getWritableDatabase();
		
		db.delete(TABLE_NAME, null, null);

    }
	
	public ArrayList<String> getExpressions(int max) {
		
		SQLiteDatabase db = m_databaseHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
//		String[] columns = { ExpressionTable.EXPRESSION, ExpressionTable.TIMESUSED };
		String[] columns = { ExpressionTable.EXPRESSION };
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = "timesused desc";
		String limit = (max == 0)? null: Integer.toString(max);
		
		ArrayList<String> result = new ArrayList<String>();
		
		Cursor cursor = qb.query(db, columns, selection, selectionArgs, null,
				null, sortOrder, limit);
		
		if (cursor == null) {
			return result;
		}
		
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			result.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		
		return result;
	}


}
