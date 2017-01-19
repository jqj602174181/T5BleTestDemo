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
		items.add(new FuncItem("�������", R.drawable.pin_num)); //��Ӧcase 0
		items.add(new FuncItem("IC��", R.drawable.ic_card));
		items.add(new FuncItem("���֤", R.drawable.id_card));
		items.add(new FuncItem("ָ����", R.drawable.finger));
		items.add(new FuncItem("�ſ�", R.drawable.ci_card));
		//		items.add(new FuncItem("IC��/�ſ�", R.drawable.conn));
		//		items.add(new FuncItem("��ʱʱ��", R.drawable.conn));
		//		items.add(new FuncItem("��ӡ��", R.drawable.conn));
		items.add(new FuncItem("����ǩ��", R.drawable.elec_tag));
		items.add(new FuncItem("������һ", R.drawable.ci_card));
		items.add(new FuncItem("ͨ������", R.drawable.conn));

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

			//��ת��ѡ�е�fragment
			changeBusinessFragment(onItemSelected(position));
			curPosition = position;
		}
	}

	//��ת��ѡ��ҳ��
	private Fragment onItemSelected(int id) 
	{
		Fragment fragment = null;

		switch (id) {
		case 0:
			//�������
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
			//ָ����
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
			Log.e("changeBusinessFragment", "���Ϊ��");
			return;
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.content, fragment).commit();
	}
}
