package com.centerm.blecentral.adapter;

import android.R;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.centerm.blecentral.datas.ScanData;

/**
 * Created by jqj on 2016/5/24.
 *
 */
public final class ScanListAdapter extends BaseAdapter{
	private List<ScanData> list;
	private Context mContext;

	public ScanListAdapter(List<ScanData> list,Context context) {
		this.list = list;
		this.mContext=context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView=new TextView(mContext);
		textView.setText(list.get(position).getDeviceName());
		textView.setTextColor(Color.parseColor("#ff000000")); 
		return textView;
	}
}
