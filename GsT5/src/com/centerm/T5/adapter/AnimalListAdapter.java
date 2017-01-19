package com.centerm.T5.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.T5.R;
import com.centerm.T5.Info.FuncItem;


class ViewHolder
{
	public ImageView title;
	public TextView content;
}

public class AnimalListAdapter extends BaseAdapter
{
	private LayoutInflater mInflater = null;
	private List<FuncItem> lists;

	public AnimalListAdapter(Context context, List<FuncItem> list)
	{
		super();
		lists = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		if (lists != null)
		{
			return lists.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		if (lists != null)
		{
			return lists.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listfragmen_item, null);
			holder.title = (ImageView) convertView.findViewById(R.id.ivTitle);
			holder.content = (TextView) convertView.findViewById(R.id.tvContent);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		FuncItem item = lists.get(position);
		holder.title.setImageResource(item.getImg());
		holder.content.setText(item.getName());

		return convertView;
	}
}