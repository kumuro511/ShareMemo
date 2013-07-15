package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NoteAdapter extends ArrayAdapter<SelectableNote> {
	private static final int STR_LENGTH = 10;
	
	private LayoutInflater layoutInflater;
	
	
	public NoteAdapter(Context context, int textViewResourceId, List<SelectableNote> objects) {
		super(context, textViewResourceId, objects);
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		 int length = Math.min(STR_LENGTH, item.getNote().length());
		 textView1.setText(item.getNote().substring(0, length));
		 
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
	
}
