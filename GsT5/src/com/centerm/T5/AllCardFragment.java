package com.centerm.T5;

import org.json.JSONObject;

import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AllCardFragment extends Fragment implements OnClickListener {

	private Context context;
	private Button bt_read;
	private ProgressDialog pd;
	private EditText et_cardno;
	private EditText et_cardtype;

	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());

	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			pd.dismiss();
			if (msg.obj != null) {
				try {
					JSONObject result = ((JSONObject) msg.obj);
					if (result.getString("status").equals("1")) {
						et_cardno.setText(result.getString("data"));
						String type = result.getString("type");
						if(type.equals("3")){ //磁卡
							et_cardtype.setText("磁卡");
						}else if(type.equals("2")){
							et_cardtype.setText("IC卡非接触");
						}else if(type.equals("1")){
							et_cardtype.setText("IC卡接触");
						}
					} else {
						et_cardno.setText(result.getString("MSG"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.allcardfragment, null);
		et_cardno = (EditText)view.findViewById(R.id.et_cardno);
		et_cardtype = (EditText)view.findViewById(R.id.et_cardtype);
		bt_read = (Button) view.findViewById(R.id.bt_read);
		bt_read.setOnClickListener(this);

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
		case R.id.bt_read:
			et_cardno.setText("");
			et_cardtype.setText("");
			pd.show();
			getAllCardData();
			break;
		default:
			break;
		}
	}

	private void getAllCardData() {
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
		cilpInterface.getAllCardInfo(listener);
	};
}
