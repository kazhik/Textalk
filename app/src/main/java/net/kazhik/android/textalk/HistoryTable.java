/**
 * 
 */
package net.kazhik.android.textalk;

import java.util.ArrayList;
import java.util.List;

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
class HistoryTable {
	private static final String DB_NAME = "textalk.db";
	private static final String TABLE_NAME = "t_history";
	
	private static final String PHRASE = "phrase";
	private static final String SORTORDER = "sortorder";
	private static final String TIMESUSED = "timesused";
	
    private DatabaseHelper m_databaseHelper;
	
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, 6);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
            		+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
            		+ PHRASE + " TEXT,"
            		+ SORTORDER + " INTEGER,"
            		+ TIMESUSED + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

        }
    }

	HistoryTable(Context context) {
        m_databaseHelper = new DatabaseHelper(context);
	}

	void updateTimesUsed(String phrase) {
		SQLiteDatabase db = m_databaseHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		String[] columns = { HistoryTable.TIMESUSED };
		String selection = PHRASE + " = ?";
		String[] selectionArgs = {phrase};

		try {
			Cursor cursor;
			cursor = qb.query(db, columns, selection, selectionArgs, null,
					null, null);
			if (cursor != null && cursor.getCount() == 1) {
				cursor.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(HistoryTable.TIMESUSED, cursor.getInt(0) + 1);
				db.update(TABLE_NAME, values, PHRASE + " = ?", selectionArgs);
			} else {
				ContentValues values = new ContentValues();
				values.put(HistoryTable.PHRASE, phrase);
				values.put(HistoryTable.SORTORDER, 0);
				values.put(HistoryTable.TIMESUSED, 1);
				db.insert(TABLE_NAME, null, values);
			}
		} catch (SQLiteException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}

	}
    void clear() {
		SQLiteDatabase db = m_databaseHelper.getWritableDatabase();
		
		db.delete(TABLE_NAME, null, null);

    }
	
	List<String> getHistory(int max) {
		
		SQLiteDatabase db = m_databaseHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		String[] columns = { HistoryTable.PHRASE};
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = "timesused desc";
		String limit = (max == 0)? null: Integer.toString(max);
		
		ArrayList<String> result = new ArrayList<>();
		
		Cursor cursor = qb.query(db, columns, selection, selectionArgs, null,
				null, sortOrder, limit);
		
		if (cursor.getCount() == 0) {
			return result;
		}
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		
		return result;
	}


}
