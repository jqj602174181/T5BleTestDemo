package com.centerm.T5;

import org.json.JSONObject;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;

@SuppressLint("NewApi")
public class ICCardFragment extends Fragment implements OnClickListener {
	private Context context;
	private ProgressDialog pd;
	private Button bt_ic_read;
	private Button bt_icSerialNo_read;
	private EditText et_cardno;
	private boolean bGetICSerialNo = false;
	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());

	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			pd.dismiss();
			if (msg.obj != null) {
				try {
					JSONObject result = ((JSONObject) msg.obj);
					if (result.getString("status").equals("1")) {

						if(bGetICSerialNo)
						{
							et_cardno.setText(result.getString("cardSerialNo_IC"));
						}
						else
						{
							et_cardno.setText(result.getString("cardNo_IC"));
							//et_track2.setText(result.getString("track2_IC"));
						}
					} else {
						et_cardno.setText(result.getString("MSG"));
						//et_track2.setText(result.getString("MSG"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			bGetICSerialNo = false;
		};
	};
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.ic_card, null);

		et_cardno = (EditText)view.findViewById(R.id.et_cardno);
		//et_track2 = (EditText)view.findViewById(R.id.et_track2);
		bt_icSerialNo_read = (Button) view.findViewById(R.id.bt_icSerialNo_read);
		bt_icSerialNo_read.setOnClickListener(this);
		bt_ic_read = (Button) view.findViewById(R.id.bt_ic_read);
		bt_ic_read.setOnClickListener(this);

		pd = new ProgressDialog(context);
		pd.setMessage("正在处理，请稍候...");
		pd.setCanceledOnTouchOutside(false);
		pd.setButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				cilpInterface.cancleClip();
				Log.i("T5", "取消读卡");
				pd.dismiss();
			}
		});

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_ic_read:
			et_cardno.setText("");
			//et_track2.setText("");
			pd.show();
			getICData();
			break;
		case R.id.bt_icSerialNo_read:
			et_cardno.setText("");
			//et_track2.setText("");
			bGetICSerialNo = true;
			pd.show();
			getICCardSerialData();
			break;
		default:
			break;
		}
	}

	private void getICData() {
		//		new Thread() {
		//			public void run() {
		//				cilpInterface = CilpInterface.getInstance();
		//				CilpRetrunListener listener = new CilpRetrunListener() {
		//					public void onSuccess(JSONObject successData) {
		//						Message msg = new Message();
		//						msg.obj = successData;
		//						handler.sendMessage(msg);
		//					}
		//					
		//					public void onFailure(JSONObject failureData) {
		//						Message msg = new Message();
		//						msg.obj = failureData;
		//						handler.sendMessage(msg);
		//					}
		//				};
		//				
		//		        cilpInterface.getICCardInfo(listener);
		//		        cilpInterface.start(CilpInterface.CRAD_SEARCH_IC);
		//			};
		//		}.start();
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Log.i("T5ICCard", "onSuccess");
				Message msg = new Message();
				msg.obj = successData;
				handler.sendMessage(msg);
			}

			public void onFailure(JSONObject failureData) {
				Log.i("T5ICCard", "onFailure");
				Message msg = new Message();
				msg.obj = failureData;
				handler.sendMessage(msg);
			}
		};
		Log.i("T5ICCard", "getICCardInfo start");
		cilpInterface.getICCardInfo(listener);
		//cilpInterface.start(CilpInterface.CRAD_SEARCH_IC);
	};

	private void getICCardSerialData() {
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Log.i("T5ICCard", "onSuccess");
				Message msg = new Message();
				msg.obj = successData;
				handler.sendMessage(msg);
			}

			public void onFailure(JSONObject failureData) {
				Log.i("T5ICCard", "onFailure");
				Message msg = new Message();
				msg.obj = failureData;
				handler.sendMessage(msg);
			}
		};
		Log.i("T5ICCard", "getICCardSerialData start");
		cilpInterface.getICCardSerialNo(listener);
	};
}
