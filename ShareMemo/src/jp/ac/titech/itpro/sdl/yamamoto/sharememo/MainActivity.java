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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	static final int MENUITEM_ID_DELETE = 1;
	
	private EditText mNoteEditText;
	private ListView mItemListView;
	private Button mAddButton;
	private Button mDelButton;

	private SelectableNote mEditingNote;
	private boolean mIsNoteEdited;
	private boolean mIsEditWithChangeNote;
	
	private DBAdapter mDbAdapter;
	private NoteAdapter mNoteAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDbAdapter = new DBAdapter(this);
		
		
		initEditText();
		initListView();
		initAddButton();
		initDelButton();
		
		loadNote();
		if (mNoteAdapter.getCount() == 0) {
			// ノートがない時は編集不可
			setNoteEditable(false);
			mEditingNote = null;
		} else {
			// 最初は一番上のノートが選択される
			setNewNote(mNoteAdapter.getItem(0));
		}
		refleshListView();
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
				if (!mIsEditWithChangeNote) {
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
				// クリックされたアイテムを取得します
				ListView listView = (ListView) parent;
                SelectableNote item = (SelectableNote) listView.getItemAtPosition(position);
                // クリックされたノートに切り替えます
                changeSelectedNote(item);
                refleshListView();
			}
		});
	}
	
	private void initAddButton() {
		mAddButton = (Button) findViewById(R.id.add_button);
		mAddButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ノートが少なくとも一つあるので編集可
				setNoteEditable(true);
				// ボタンが押されたら新しいノートを作成する
				SelectableNote newNote = createNewNote();
				changeSelectedNote(newNote);
			}
		});
	}
	
	private void initDelButton() {
		mDelButton = (Button) findViewById(R.id.del_button);
		mDelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ボタンが押されたら現在のノートを削除する
				deleteNote(mEditingNote);
			}
		});
	}
	
	private void changeSelectedNote(SelectableNote newNote) {
		if (mEditingNote != null) {
			// 編集中のNoteの内容を保存します
			savePrevNote();
		}
		// 新しいノートをセットします
		setNewNote(newNote);
        
        refleshListView();
	}
	
	private void savePrevNote() {
		if (mIsNoteEdited) {
			updateNote(mEditingNote);
		}
		mEditingNote.setIsSelected(false);	
	}
	
	private void setNewNote(SelectableNote newNote) {
		mIsEditWithChangeNote = true;
        mEditingNote = newNote;
        mNoteEditText.setText(mEditingNote.getNote());
		mEditingNote.setIsSelected(true);
        mIsNoteEdited = false;
        mIsEditWithChangeNote = false;
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
	}

	private SelectableNote createNewNote() {
		SelectableNote note = null;
		mDbAdapter.open();
		int id = mDbAdapter.saveNote("", "user");
		mDbAdapter.close();
		System.err.println(id);
		mDbAdapter.open();
		Cursor c = mDbAdapter.getNote(id);
		if (c.moveToFirst()) {
			note = getNote(c);
			mNoteAdapter.add(note);
		}
		
		return note;
	}
	
	private void updateNote(Note note) {
		mDbAdapter.open();
		mDbAdapter.updateNote(note);
		mDbAdapter.close();
	}
	
	private void deleteNote(Note note) {
		mDbAdapter.open();
		mDbAdapter.deleteNote(note.getId());
		mDbAdapter.close();
		
		int position = mNoteAdapter.getPosition(mEditingNote);
		mNoteAdapter.remove(mEditingNote);
		mEditingNote = null;
		
		SelectableNote newNote;
		if (mNoteAdapter.getCount() == 0) {
			setNoteEditable(false);
			mIsEditWithChangeNote = true;
			mNoteEditText.setText("");
			mIsEditWithChangeNote = false;
		} else {
			if (position < mNoteAdapter.getCount()) {
				newNote = mNoteAdapter.getItem(position);
			} else {
				newNote = mNoteAdapter.getItem(position - 1);
			}
			setNewNote(newNote);
		}
	}
	
	private void refleshListView() {
		mNoteAdapter.sort(new NoteComparator());
		mNoteAdapter.notifyDataSetChanged();
	}
	
	private void setNoteEditable(boolean isEditable) {
		mNoteEditText.setFocusable(isEditable);
		mNoteEditText.setFocusableInTouchMode(isEditable);
		mNoteEditText.setEnabled(isEditable);
		mNoteEditText.requestFocus();
	}
}
