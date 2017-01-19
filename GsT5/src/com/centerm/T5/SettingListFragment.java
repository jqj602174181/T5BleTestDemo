package com.centerm.T5;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.centerm.T5.Info.FuncItem;
import com.centerm.T5.adapter.AnimalListAdapter;

@SuppressLint("NewApi")
public class SettingListFragment extends ListFragment {
	private int curPosition = 0;
	private AnimalListAdapter adapter = null;
	List<FuncItem> items = new ArrayList<FuncItem>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		items.add(new FuncItem("密码键盘", R.drawable.pin_num)); //对应case 0
		items.add(new FuncItem("IC卡", R.drawable.ic_card));
		items.add(new FuncItem("身份证", R.drawable.id_card));
		items.add(new FuncItem("指纹仪", R.drawable.finger));
		items.add(new FuncItem("磁卡", R.drawable.ci_card));
		//		items.add(new FuncItem("IC卡/磁卡", R.drawable.conn));
		//		items.add(new FuncItem("超时时间", R.drawable.conn));
		//		items.add(new FuncItem("打印机", R.drawable.conn));
		items.add(new FuncItem("电子签名", R.drawable.elec_tag));
		items.add(new FuncItem("三卡合一", R.drawable.ci_card));
		items.add(new FuncItem("通信连接", R.drawable.conn));

		adapter = new AnimalListAdapter(getActivity(), items);
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		changeBusinessFragment(onItemSelected(0));
		return inflater.inflate(R.layout.fragment_list, null);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) 
	{
		super.onListItemClick(listView, view, position, id);
		if (position != curPosition)
		{
			view.setBackgroundColor(Color.WHITE);
			View v = 	listView.getChildAt(curPosition);
			if(v!=null){
				v.setBackground(null);
			}

			//跳转到选中的fragment
			changeBusinessFragment(onItemSelected(position));
			curPosition = position;
		}
	}

	//跳转到选中页面
	private Fragment onItemSelected(int id) 
	{
		Fragment fragment = null;

		switch (id) {
		case 0:
			//密码键盘
			fragment= new PinNumFragment();
			//fragment = new ICCardFragment();
			break;
		case 1:
			fragment = new ICCardFragment();
			break;
		case 2:
			fragment = new IDCardFragment();
			break;
		case 3:
			//指纹仪
			fragment = new FingerPrintsFragment();
			break;
		case 4:
			fragment = new MagCardFragment();
			break;
		case 5:
			fragment = new SignFragment();
			break;
			//		case 5:
			//			fragment = new ICAndMsgFragment();
			//			break;
			//		case 6:
			//			fragment = new TimeoutSettingFragment();
			//			break;
			//		case 7:
			//			fragment = new PrinterFragment();
			//			break;
		case 6:
			fragment = new AllCardFragment();
			break;
		case 7:
			fragment = new CommunicateConnFragment();
			break;
		default:
			break;
		}
		return fragment;
	}


	private void changeBusinessFragment(Fragment fragment) {
		if (fragment == null) {
			Log.e("changeBusinessFragment", "入参为空");
			return;
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.content, fragment).commit();
	}
}
