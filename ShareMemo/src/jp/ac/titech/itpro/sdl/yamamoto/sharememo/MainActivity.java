package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.view.Menu;
import android.widget.EditText;

public class MainActivity extends Activity {
	static final int MENUITEM_ID_DELETE = 1;
	
	EditText noteEditText;

	
	static DBAdapter dbAdapter;
	static List<Note> noteList = new ArrayList<Note>();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dbAdapter = new DBAdapter(this);
		noteEditText = (EditText) findViewById(R.id.note_text);
		loadNote();
		
		if (noteList.size() > 0) {
			Note note = noteList.get(0);
			noteEditText.setText(note.getNote());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveItem();
	}

	protected void loadNote() {
		noteList.clear();

		// Read
		dbAdapter.open();
		Cursor c = dbAdapter.getAllNotes();

		if (c.moveToFirst()) {
			do {
				Note note = new Note(c.getInt(c. getColumnIndex(DBAdapter.COL_ID)), 
						c.getString(c.getColumnIndex(DBAdapter.COL_NOTE)),
						c.getString(c.getColumnIndex(DBAdapter.COL_USER)),
						c.getString(c.getColumnIndex(DBAdapter.COL_LASTUPDATE)));
				noteList.add(note);
			} while (c.moveToNext());
		}

		dbAdapter.close();

	}

	protected void saveItem() {
		dbAdapter.open();
		dbAdapter.saveNote(noteEditText.getText().toString(), "user");
		dbAdapter.close();
		loadNote();
	}
}
