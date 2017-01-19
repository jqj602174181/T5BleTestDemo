package com.centerm.T5;

import org.json.JSONObject;

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

import com.centerm.T5.utils.StringUtil;
import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;
import com.centerm.util.TimeOutUtil;

@SuppressLint("NewApi")
public class ICAndMsgFragment extends Fragment implements OnClickListener {
	private Context context;
	
	private static final String TAG = "ICAndMsgFragment";
	
	private Button readinfo, setTexDail;
	private ProgressDialog pd;
	
	private boolean flag = true;//AB标签状态标识
	private static String  Label = "A";
	
	private EditText ICcardNo,Domains,Track2,Timeout,LabelNum, ICcardSerialNo, MsgcardNo ,Track3,TxDetail;

	private static String P0120 = "P012000000000000";

	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());
	
	
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			pd.dismiss();
			if (msg.obj != null) {
				try {
					JSONObject result = ((JSONObject) msg.obj);
					if (result.getString("status").equals("1")) {
						
						if(!result.isNull("cardNo_IC"))
						{
							ICcardNo.setText(result.getString("cardNo_IC"));
						}
						
						if(!result.isNull("ICChipData"))
						{
						    Log.e("55", "ICChipData"+result.getString("ICChipData"));
							Domains.setText(result.getString("ICChipData"));
						}
				
						if(!result.isNull("track2_MS"))
						{
						  Track2.setText(result.getString("track2_MS"));
						
						}
						
						if(!result.isNull("cardSerialNo_IC"))
						{
							ICcardSerialNo.setText( result.getString("cardSerialNo_IC") );
						}
						
						if(!result.isNull("cardNo_MS"))
						{
							MsgcardNo.setText(result.getString("cardNo_MS"));
						}
						
						if(!result.isNull("track3_MS"))
						{
							Track3.setText( result.getString("track3_MS" ));
						}
						
					} else {
						//ICcardNo.setText(result.getString("MSG"));
						//Domains.setText(result.getString("MSG"));
					    Track2.setText(result.getString("MSG"));
						//ICcardSerialNo.setText(result.getString("MSG"));
						MsgcardNo.setText(result.getString("MSG"));
						Track3.setText( result.getString("MSG" ));

					}
				} catch (Exception e) {
					e.printStackTrace();
					//XXX
				}
			} 
		};
	};
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.icandmsg, null);
		
		//初始化Button和EditText
		setTexDail = (Button)view.findViewById(R.id.btn_TxDetail);
		readinfo = (Button)view.findViewById(R.id.bt_readinfo);
		ICcardNo = (EditText)view.findViewById(R.id.et_ICcardNo);
		Domains  = (EditText)view.findViewById(R.id.et_domains);
		MsgcardNo= (EditText)view.findViewById(R.id.et_MsgcardNo);
		Track2   = (EditText)view.findViewById(R.id.et_id_track2);
		ICcardSerialNo = (EditText)view.findViewById(R.id.et_id_icSerialNo);
		Track3   = (EditText)view.findViewById(R.id.et_id_track3);
		TxDetail  = (EditText)view.findViewById(R.id.et_TxDetail);
		LabelNum = (EditText)view.findViewById(R.id.et_labelNum);
		LabelNum.setText(Label);
		LabelNum.setOnClickListener(this);
		//Timeout.setText(String.valueOf(TimeOutUtil.mTimeOut));
		readinfo.setOnClickListener(this);
		setTexDail.setOnClickListener(this);
		TxDetail.setText(P0120);
		
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
		case R.id.bt_readinfo:
			
			{
				Log.i(TAG,"onClick");		
			ICcardNo.setText("");
			Domains.setText("");
			Track2.setText("");
			ICcardSerialNo.setText("");
			MsgcardNo.setText("");
			Track3.setText("");
			pd.show();
			getICData();
			
			break;
			}
		case R.id.et_labelNum:
			if(flag)
			{
				LabelNum.setText("B");
				flag = false;
			}
			else
			{
				LabelNum.setText("A");
				flag = true;
			}
			Label = LabelNum.getText().toString();
			break;
		case R.id.btn_TxDetail:
		  {
			 String data = TxDetail.getText().toString();
			 
			 cilpInterface.setTxDetail(data);
			break;
		  }
		default:
			break;
		}
	}

	private void getICData() {

		
		Log.i(TAG,"getICData");
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
		
        cilpInterface.getICCAndMSardInfo(listener);
	}

}
