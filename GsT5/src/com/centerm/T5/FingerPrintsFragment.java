package com.centerm.T5;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;

@SuppressLint("NewApi")
public class FingerPrintsFragment extends Fragment implements OnClickListener {

	private Context context;
	private Button bt_fpcode;
	private EditText et_fpcode;
	private Spinner spinner_ic_card;
	private static final String[] n = {"维尔"};
	private ArrayAdapter<String> adapter;
	private static final int CURRENTFINGER = 1;
	private ProgressDialog pd;
	private Handler handler=new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			pd.dismiss();
			switch (msg.what) {
			case CURRENTFINGER:
				if (msg.obj != null) {
					try {
						JSONObject result = (JSONObject) msg.obj;
						if (result.getString("status").equals("1")) {
							et_fpcode.setText(result.getString("fingerCode"));
						} else {
							et_fpcode.setText(result.getString("MSG"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context=inflater.getContext();
		View view = (View) inflater.inflate(R.layout.fingerprints, null);
		bt_fpcode = (Button) view.findViewById(R.id.bt_fpcode);//指纹特征
		et_fpcode =(EditText) view.findViewById(R.id.et_fpcode);
		bt_fpcode.setOnClickListener(this);
		
		spinner_ic_card = (Spinner) view.findViewById(R.id.spinner_ic_card);
		adapter = new ArrayAdapter<String>(context, R.layout.my_spinner_item, n);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_ic_card.setAdapter(adapter);
		spinner_ic_card.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());
		spinner_ic_card.setVisibility(View.VISIBLE);
		
		pd = new ProgressDialog(context);
		pd.setMessage("正在处理，请稍候...");
		pd.setCanceledOnTouchOutside(false);
		
		return view;
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
	}
	 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_fpcode:// 读取指纹特征
			Toast.makeText(context, "请按指纹", Toast.LENGTH_SHORT).show();
			et_fpcode.setText("");
			pd.show();
			myGetFingerData();
			break;
		default:
			break;
		}
	}
	
	private void myGetFingerData() {
		
		CilpInterface cilpInterface = CilpInterface.getCilpInterface(getActivity().getApplication(), new Handler());
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Message msg = new Message();
				msg.obj = successData;
				msg.what = CURRENTFINGER;
				handler.sendMessage(msg);
			}
			
			public void onFailure(JSONObject failureData) {
				Message msg = new Message();
				msg.obj = failureData;
				msg.what = CURRENTFINGER;
				handler.sendMessage(msg);
			}
		};
		
        cilpInterface.getFingerCode(listener);
        //cilpInterface.start(CilpInterface.FINFERGET);				
	}
}
