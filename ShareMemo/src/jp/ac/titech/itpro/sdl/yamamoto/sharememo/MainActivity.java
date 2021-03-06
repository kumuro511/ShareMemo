package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends Activity implements CreateNdefMessageCallback,
	OnNdefPushCompleteCallback, FilterListener {
	static final int MENUITEM_ID_DELETE = 1;
	
	private EditText mNoteEditText;
	private ListView mItemListView;
	private Button mAddButton;
	private Button mDelButton;
	private SearchView mSearchView;

	private SelectableNote mEditingNote;
	private boolean mIsNoteEdited;
	private boolean mIsEditWithChangeNote;
	
	private DBAdapter mDbAdapter;
	private NoteAdapter mNoteAdapter;

	private NfcAdapter mNfcAdapter;
	private Drawable mUnEditableIcon;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDbAdapter = new DBAdapter(this);
		
		initEditText();
		initListView();
		initAddButton();
		initDelButton();
		initSearch();
		
		loadNote();
		if (mNoteAdapter.getCount() == 0) {
			mEditingNote = null;
			deselectNote();
		} else {
			// 最初は一番上のノートが選択される
			selectNote(mNoteAdapter.getItem(0));
		}
		refleshListView();
		
		// android beam
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
		
		// 
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
	
	@Override
	public void onResume() {
		super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }
	// android beam を受け取った時の処理
	private void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        
        byte[] data = msg.getRecords()[0].getPayload();

        try {
	        ByteArrayInputStream bis = new ByteArrayInputStream(data);
	        ObjectInputStream ois = new ObjectInputStream(bis);
	
	        // データを取り出して保存、そのノートに切り替えます。
	        SelectableNote newNote = (SelectableNote) ois.readObject();
	        newNote = saveNote(newNote.getNote(), newNote.getUser());
	        selectNote(newNote);
	        
        } catch (Exception e) {
        	Log.d("DEBUG", "deserialize fail");
        }        
	}
	
	private void initEditText() {
		mNoteEditText = (EditText) findViewById(R.id.note_text);
		mNoteEditText.requestFocus();
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
		
		// 編集不可のときのiconを設定する
		mUnEditableIcon = getResources().getDrawable(android.R.drawable.ic_delete);
		mUnEditableIcon.setBounds(0, 0, 
				mUnEditableIcon.getIntrinsicWidth(), mUnEditableIcon.getIntrinsicHeight());
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
				// クリックされたアイテムを取得し、そのノートを選択します
				ListView listView = (ListView) parent;
                SelectableNote item = (SelectableNote) listView.getItemAtPosition(position);
                selectNote(item);
			}
		});
	}
	
	private void initAddButton() {
		mAddButton = (Button) findViewById(R.id.add_button);
		mAddButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ボタンが押されたら新しいノートを作成し、選択する
				SelectableNote newNote = createNewNote();
				selectNote(newNote);
				mNoteEditText.requestFocus();
				// 検索フィルタはリセットする
				mSearchView.setQuery("", false);
				
			}
		});
	}
	
	private void initDelButton() {
		mDelButton = (Button) findViewById(R.id.del_button);
		mDelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mEditingNote != null) {
					// ボタンが押されたら現在のノートを削除する
					deleteNote(mEditingNote);
				}
			}
		});
	}
	
	private void initSearch() {
		// search window
		mSearchView = (SearchView) findViewById(R.id.search_window);
		mSearchView.setIconified(false);	
		mSearchView.setSubmitButtonEnabled(false);
		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d("DEBUG", "test");
				return false;
			}
			@Override
			public boolean onQueryTextChange(String queryText) {
				if (TextUtils.isEmpty(queryText)) {
					mNoteAdapter.getFilter().filter("", MainActivity.this);
				} else {
					mNoteAdapter.getFilter().filter(queryText, MainActivity.this);
				}
				return true;
			}
		});
	}
	
	private void savePrevNote() {
		if (mEditingNote != null) {
			if (mIsNoteEdited) {
				updateNote(mEditingNote);
			}
			mEditingNote.setIsSelected(false);	
		}
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

	private SelectableNote saveNote(String notetext, String user) {
		SelectableNote note = null;
		mDbAdapter.open();
		int id = mDbAdapter.saveNote(notetext, user);
		Cursor c = mDbAdapter.getNote(id);
		if (c.moveToFirst()) {
			note = getNote(c);
			mNoteAdapter.add(note);
		}
		mDbAdapter.close();
		return note;
	}
	private SelectableNote createNewNote() {
		Account[] accounts = AccountManager.get(this).getAccounts();
		String user = "noname";
		if (accounts != null && accounts.length > 0) {
			Account account = accounts[0];
			if (account != null) {
				user = account.name;
			}
		}
		return saveNote("", user);
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
			deselectNote();
		} else {
			// 一つ上もしくは一つ下のノートを新しく選択中にする
			if (position < mNoteAdapter.getCount()) {
				newNote = mNoteAdapter.getItem(position);
			} else {
				newNote = mNoteAdapter.getItem(position - 1);
			}
			selectNote(newNote);
		}
	}
	
	// プログラム側からのEditTextの書き換えで、Noteのlastupdateが更新されないようにする
	// そのためのフラグ設定
	private void setEditTextsText(CharSequence str) {
		mIsEditWithChangeNote = true;
		mNoteEditText.setText(str);
		mIsEditWithChangeNote = false;
	}
	
	private void refleshListView() {
		mNoteAdapter.sort(new NoteComparator());
		mNoteAdapter.notifyDataSetChanged();
	}
	
	private void setNoteEditable(boolean isEditable) {
		mNoteEditText.setFocusable(isEditable);
		mNoteEditText.setFocusableInTouchMode(isEditable);
		mNoteEditText.setEnabled(isEditable);
	}
	
	private void selectNote(SelectableNote note) {
		// 編集中のNoteの内容を保存します
		savePrevNote();
		// 新しいノートをEditTextなどにセットします
        mEditingNote = note;
		setEditTextsText(mEditingNote.getNote());
		mEditingNote.setIsSelected(true);
        mIsNoteEdited = false;
        
		mNoteEditText.setCompoundDrawables(null, null, null, null);
        setNoteEditable(true);
        refleshListView();
	}
	
	private void deselectNote() {
		// 編集中のNoteの内容を保存します
		savePrevNote();
		// EditTextなどをからにします
		mEditingNote = null;
		setEditTextsText("");
		mIsNoteEdited = false;
		
		mNoteEditText.setCompoundDrawables(mUnEditableIcon, null, null, null);
		setNoteEditable(false);
        refleshListView();
	}
	

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		Log.d("DEBUG", "onNdefComplete");
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = null;
		if (mEditingNote != null) {
			byte[] data;
			try { 
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutputStream oos = new ObjectOutputStream(bos);
			    oos.writeObject(mEditingNote);
			    oos.flush();
			    data = bos.toByteArray();
			    
				msg = new NdefMessage(NdefRecord.createMime(
						"application/jp.ac.titech.itpro.sdl.yamamoto.sharememo", data));
			} catch (Exception e) {
				Log.d("DEBUG", "serialize fail");
			}
		}
		return msg;
	}
	
	@Override
	public void onFilterComplete(int count) {
		if (mNoteAdapter.getPosition(mEditingNote) < 0) {
			// 選択中のアイテムが検索によってListViewからいなくなったら
			deselectNote();
		} 
	}
}
