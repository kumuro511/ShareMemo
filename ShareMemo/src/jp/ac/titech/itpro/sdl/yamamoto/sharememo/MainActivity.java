package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	static final int MENUITEM_ID_DELETE = 1;
	
	private EditText mNoteEditText;
	private ListView mItemListView;

	private SelectableNote mEditingNote;
	private int mEditingPosition;
	private boolean mIsNoteEdited;
	private boolean mIsEditWithClick;
	
	private DBAdapter mDbAdapter;
	private NoteAdapter mNoteAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDbAdapter = new DBAdapter(this);
		initEditText();
		initListView();
		loadNote();
		
		if (mNoteAdapter.getCount() == 0) {
			createNewNote();
		}
		// 最初は一番上のノートが選択される
		mEditingPosition = 0;
		mEditingNote = mNoteAdapter.getItem(mEditingPosition);
        setEditText();
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
		if (mIsNoteEdited) {
			updateNote(mEditingNote);
		}
	}
	
	private void initEditText() {
		mNoteEditText = (EditText) findViewById(R.id.note_text);
		mNoteEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (!mIsEditWithClick) {
					// ノートを更新する
					mEditingNote.setNote(mNoteEditText.getText().toString());
					mEditingNote.setLastupdate(DateUtils.getDate());
					mIsNoteEdited = true;
					
					refleshListView();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		});
	}
	
	private void initListView() {
		// ListViewの中身はadapterごしに管理する
		List<SelectableNote> noteList = new ArrayList<SelectableNote>();
		mNoteAdapter = new NoteAdapter(this, 0, noteList);
		mItemListView = (ListView) findViewById(R.id.note_list);
		mItemListView.setAdapter(mNoteAdapter);
	
		mItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				 // Noteの内容を保存します
				if (mIsNoteEdited) {
					updateNote(mEditingNote);
				}
				mEditingNote.setIsSelected(false);
                mIsEditWithClick = true;
				// クリックされたアイテムを取得します
				ListView listView = (ListView) parent;
                mEditingNote = (SelectableNote) listView.getItemAtPosition(position);
                setEditText();
                mIsEditWithClick = false;
                
                refleshListView();
			}
		});
	}
	
	private void setEditText() {
        mNoteEditText.setText(mEditingNote.getNote());
		mEditingNote.setIsSelected(true);
        mIsNoteEdited = false;
	}
	
	private SelectableNote getNote(Cursor c) {
		assert(c != null);
		
		SelectableNote note = new SelectableNote(
				c.getInt(c.getColumnIndex(DBAdapter.COL_ID)), 
				c.getString(c.getColumnIndex(DBAdapter.COL_NOTE)),
				c.getString(c.getColumnIndex(DBAdapter.COL_USER)),
				c.getString(c.getColumnIndex(DBAdapter.COL_LASTUPDATE)));
		return note;
	}

	private void loadNote() {
		mNoteAdapter.clear();

		// Read
		mDbAdapter.open();
		Cursor c = mDbAdapter.getAllNotes();

		if (c.moveToFirst()) {
			do {
				SelectableNote note = getNote(c);
				mNoteAdapter.add(note);
			} while (c.moveToNext());
		}
		mDbAdapter.close();
		
		refleshListView();
	}

	private void createNewNote() {
		SelectableNote note;
		mDbAdapter.open();
		int id = mDbAdapter.saveNote("", "user");
		Cursor c = mDbAdapter.getNote(id);
		note = getNote(c);
		mDbAdapter.close();
		
		mNoteAdapter.add(note);
		refleshListView();
	}
	
	private void updateNote(Note note) {
		mDbAdapter.open();
		mDbAdapter.updateNote(note);
		mDbAdapter.close();
	}
	
	private void refleshListView() {
		mNoteAdapter.notifyDataSetChanged();
		mNoteAdapter.sort(new NoteComparator());
	}
}
