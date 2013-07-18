package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class NoteAdapter extends ArrayAdapter<SelectableNote> {
	private static final int STR_LENGTH = 10;
	private LayoutInflater layoutInflater;
	private Filter filter = null;
	// 全てのnote
    List<SelectableNote> list = null;
	
	public NoteAdapter(Context context, int textViewResourceId, List<SelectableNote> objects) {
		super(context, textViewResourceId, objects);
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		this.list = new ArrayList<SelectableNote>();
		for (SelectableNote item : objects) {
			this.list.add(item);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 特定の行(position)のデータを得る
		SelectableNote item = (SelectableNote) getItem(position);
		 
		// convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
		if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.list_item, null);
		}
		// CustomDataのデータをViewの各Widgetにセットする
		 
		 TextView textView1;
		 textView1 = (TextView) convertView.findViewById(R.id.text_str);
		 // LINE_LENGTH分の長さ、改行は含まない文字列を表示
		 int length = Math.min(STR_LENGTH, item.getNote().length());
		 String sub = item.getNote().substring(0, length);
		 sub = sub.split(System.getProperty("line.separator"))[0];
		 textView1.setText(sub);
		 
		 TextView textView2;
		 textView2 = (TextView) convertView.findViewById(R.id.text_date);
		 textView2.setText(item.getUser() + ", " + item.getLastupdate());

		 if (item.isSelected()) {
			 convertView.setBackgroundColor(convertView.getResources().getColor(R.color.green));
		 } else {
			 convertView.setBackgroundColor(convertView.getResources().getColor(R.color.white));
		 }
		 return convertView;
	}
	
    /*************************************************
    * アダプタに値を追加する。
    * 表示用のリストに値を追加するのと同時に、オリジナルのリストにも追加している。
    * @param object 追加する値。
    *************************************************/
    @Override
    public void add(SelectableNote object) {
        super.add(object);
        list.add(object);
    }
    /*************************************************
    * アダプタから値を削除する。
    * 表示用のリストから値を削除するのと同時に、オリジナルのリストからも削除している。
    * @param object 削除する値。
    *************************************************/
    @Override
    public void remove (SelectableNote object) {
        super.remove(object);
        list.remove(object);
    }
    
    /*************************************************
    * フィルタを返す
    * @return フィルタオブジェクト
    *************************************************/
    @Override
    public Filter getFilter() {
        return new Filter() {
            /******************************************************************
            * フィルタに表示する値リストを作成するメソッド
            * @param constraint 検索値
            * @return フィルタに表示する値リスト
            ******************************************************************/
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<SelectableNote> filterItems = new ArrayList<SelectableNote>();
                if(constraint == null || constraint.length() == 0) {
                    for(final SelectableNote item : list) {
                        filterItems.add(item);
                    }
                }
                else {
                    for(final SelectableNote item : list) {
                        if(item.getNote().contains(constraint)) {
                            filterItems.add(item);
                        }
                    }
                }
                results.values = filterItems;
                results.count = filterItems.size();
                
                return results;
            }
            /******************************************************************
            * フィルタに値を設定するメソッド
            * @param constraint 検索値
            * @param results performFiltering の戻り値
            ******************************************************************/
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null) {
                    clear();
                    @SuppressWarnings("unchecked")
                    List<SelectableNote> resultlist = (List<SelectableNote>) results.values;
                    for(final SelectableNote item : resultlist) {
                        NoteAdapter.super.add(item);
                    }
                    notifyDataSetChanged();
                }
                
            }
        };
    }	
    
    @Override
    public void sort(Comparator<? super SelectableNote> comparator) {
    	super.sort(comparator);
    	Collections.sort(this.list, comparator);
    }
}
