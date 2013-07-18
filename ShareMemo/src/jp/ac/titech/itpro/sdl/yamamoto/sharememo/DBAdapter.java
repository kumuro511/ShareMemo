package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
	static final String DATABASE_NAME = "mynote.db";
	static final int DATABASE_VERSION = 1;

	public static final String TABLE_NAME = "notes";
	public static final String COL_ID = "_id";
	public static final String COL_NOTE = "note";
	public static final String COL_USER = "user";
	public static final String COL_LASTUPDATE = "lastupdate";

	protected final Context context;
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;

	public DBAdapter(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
	}

	//
	// SQLiteOpenHelper
	//

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" 
					+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ COL_NOTE + " TEXT NOT NULL," 
					+ COL_USER + " TEXT NOT NULL,"
					+ COL_LASTUPDATE + " TEXT NOT NULL);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

	}

	//
	// Adapter Methods
	//

	public DBAdapter open() {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	//
	// App Methods
	//

	public boolean deleteAllNotes() {
		return db.delete(TABLE_NAME, null, null) > 0;
	}

	public boolean deleteNote(int id) {
		return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
	}
	
	public Cursor getNote(int id) {
		String query = "select * from " + TABLE_NAME + " where " + COL_ID + "=" + id + ";";
		return db.rawQuery(query, null);
	}

	public Cursor getAllNotes() {
		return db.query(TABLE_NAME, null, null, null, null, null, null);
	}

	public int saveNote(String note, String user) {
		ContentValues values = new ContentValues();
		values.put(COL_NOTE, note);
		values.put(COL_USER, user);
		values.put(COL_LASTUPDATE, DateUtils.getDate());
		// idを返す
		return (int) db.insertOrThrow(TABLE_NAME, null, values);
	}
	
	public void updateNote(Note note) {
		ContentValues values = new ContentValues();
		values.put(COL_NOTE, note.getNote());
		values.put(COL_USER, note.getUser());
		values.put(COL_LASTUPDATE, note.getLastupdate());
		String whereClause = COL_ID + "=" + note.getId();
		db.update(TABLE_NAME, values, whereClause, null);
	}
}
