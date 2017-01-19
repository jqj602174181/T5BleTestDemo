package com.centerm.T5;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
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
public class MagCardFragment extends Fragment implements OnClickListener 
{
	private EditText et_track2, et_track3, et_cardNo;
	private Button bt_ci;
	private ProgressDialog pd;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			pd.dismiss();
			JSONObject result = (JSONObject) msg.obj;
			try {
				if (result.getString("status").equals("1")) {
					et_track2.setText(result.getString("track2_MS"));
					et_track3.setText(result.getString("track3_MS"));
					et_cardNo.setText(result.getString("cardNo_MS"));
				} else {
					et_track2.setText(result.getString("MSG"));
					et_track3.setText(result.getString("MSG"));
					et_cardNo.setText(result.getString("MSG"));
				}
			} catch (Exception e) {
				Log.e("magcard", e.getMessage());//XXX
				e.printStackTrace();
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.mag_card, null);
		et_track2 = (EditText) view.findViewById(R.id.et_track2);
		et_track3 = (EditText) view.findViewById(R.id.et_track3);
		et_cardNo = (EditText) view.findViewById(R.id.et_cardNo);

		bt_ci = (Button) view.findViewById(R.id.bt_ci);
		bt_ci.setOnClickListener(this);

		pd = new ProgressDialog(inflater.getContext());
		pd.setMessage("正在处理，请稍候...");
		pd.setCanceledOnTouchOutside(false);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_ci:
			et_track2.setText("");
			et_track3.setText("");
			et_cardNo.setText("");
			pd.show();
			getIcData();
			break;
		default:
			break;
		}
	}

	private void getIcData() 
	{

		CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Message msg = new Message();
				msg.obj = successData;
				handler.sendMessage(msg);
			}

			public void onFailure(JSONObject failureData) {
				Message msg = new Message();
				msg.obj = failureData;
				handler.sendMessage(msg);
			}
		};

		cilpInterface.getMSCardInfo(listener);
		//cilpInterface.start(CilpInterface.CRAD_SEARCH_MS);

	}
}
