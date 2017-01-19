package com.centerm.T5;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import com.centerm.T5.utils.StringUtil;
import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;

@SuppressLint("NewApi")
public class SignFragment extends Fragment implements OnClickListener {
	private Context context;
	private Button bt_elec_tag;
	private ImageView imageView;
	private ProgressDialog pd;
	private int iEncryType = 1;
	private Spinner spinner_encrpt;
	private EditText passKeys, timeout, encrySign;
	private TableRow passKeysRow;
	private static final String[] encryType = { "不加密", "DES", "3DES" };

	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());

	private static final int GETIDFULLINFO = 1;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			pd.dismiss();
			if (msg.obj != null) {
				try {
					JSONObject result = ((JSONObject) msg.obj);
					if (result.getString("status").equals("1")) {
						encrySign.setText(result.getString("data"));
					} else {
						encrySign.setText(result.getString("MSG"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			//			String[] result = (String[]) msg.obj;
			//			if (result[0].equals("0")) {
			//				if (iEncryType == 1) {
			//					byte[] photoData = StringUtil.hexStringToBytes(result[1]);
			//					Bitmap bm = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
			//					imageView.setImageBitmap(bm);
			//					imageView.setVisibility(View.VISIBLE);
			//				} else {
			//					encrySign.setText(result[1]);
			//					encrySign.setVisibility(View.VISIBLE);
			//				}
			//			} else {
			//				if (!result[1].isEmpty()) {
			//					Toast.makeText(context, result[1], Toast.LENGTH_LONG).show();
			//				} else {
			//					Toast.makeText(context, "获取失败", Toast.LENGTH_LONG).show();
			//				}
			//
			//				return;
			//			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.electronic_tag, null);
		imageView = (ImageView) view.findViewById(R.id.iv_img);
		bt_elec_tag = (Button) view.findViewById(R.id.bt_elec_tag);
		bt_elec_tag.setOnClickListener(this);
		context = inflater.getContext();
		spinner_encrpt = (Spinner) view.findViewById(R.id.spinner_encrpt);
		ArrayAdapter< String> adapter = new ArrayAdapter<String>(context, R.layout.my_spinner_item, encryType);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_encrpt.setAdapter(adapter);
		spinner_encrpt.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());
		passKeysRow = (TableRow) view.findViewById(R.id.passKeysRow);
		passKeys = (EditText) view.findViewById(R.id.passKeys);
		timeout = (EditText) view.findViewById(R.id.timeout);
		encrySign = (EditText) view.findViewById(R.id.encrySign);
		pd = new ProgressDialog(context);
		pd.setMessage("正在读取，请稍候...");
		pd.setCanceledOnTouchOutside(false);
		return view;
	}

	class SpinnerSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			String type = arg0.getItemAtPosition(position).toString();
			if (type == "DES") {
				passKeysRow.setVisibility(View.VISIBLE);
				passKeys.setText("1112131415161718");
				iEncryType = 2;
			} else if (type == "3DES") {
				passKeysRow.setVisibility(View.VISIBLE);
				passKeys.setText("11121314151617182122232425262728");
				iEncryType = 3;
			} else if (type == "不加密") {
				passKeysRow.setVisibility(View.INVISIBLE);
				iEncryType = 1;
			}
		}
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	@Override
	public void onClick(View v) {
		//		imageView.setVisibility(View.GONE);
		//		encrySign.setVisibility(View.GONE);

		String itimeout = "30";
		String timeoutStr = timeout.getText().toString();
		if (!timeoutStr.isEmpty()) {
			itimeout = timeoutStr;
		}

		if (iEncryType == 1) {
			pd.show();
			getSignData(itimeout);
		} else if (iEncryType == 2) {
			String passKey = passKeys.getText().toString();
			if (passKey.isEmpty()) {
				Toast.makeText(context, "请输入密钥", Toast.LENGTH_SHORT).show();
				return;
			}
			//DES加密密钥字节长度必须为8
			if (passKey.length() % 16 != 0) {
				Toast.makeText(context, "DES密钥长度不合法", Toast.LENGTH_SHORT).show();
				return;
			}
			String[] keys = new String[passKey.length() / 16];
			for (int i = 0; i < passKey.length() / 16; i++) {
				keys[i] = passKey.substring(i*16, i*16+16);
			}
			Log.e("sign", keys[0]);
			//				String[] keys = {"1112131415161718"};
			pd.show();
			getEncryData(itimeout, keys);
		} else {
			String passKey = passKeys.getText().toString();
			if (passKey.isEmpty()) {
				Toast.makeText(context, "请输入密钥", Toast.LENGTH_SHORT).show();
				return;
			}
			//3DES加密密钥字节长度必须为16或者24
			String[] keys = null;
			if (passKey.length() % 32 != 0 && passKey.length() % 48 != 0) {
				Toast.makeText(context, "3DES密钥长度不合法", Toast.LENGTH_SHORT).show();
				return;
			} else if (passKey.length() % 32 == 0) {
				keys = new String[passKey.length() / 32];
				for (int i = 0; i < passKey.length() / 32; i++) {
					keys[i] = passKey.substring(i*32, i*32+32);
				}
			} else if (passKey.length() % 48 == 0) {
				keys = new String[passKey.length() / 48];
				for (int i = 0; i < passKey.length() / 48; i++) {
					keys[i] = passKey.substring(i*48, i*48+48);
				}
			}
			pd.show();
			getEncryData(itimeout, keys);
		}
	}

	private void getSignData(final String strTimeout)
	{
		//				String[] aryRet = new Sign().getSignPhotoData(strTimeout);
		//				Message msg = new Message();
		//				msg.obj = aryRet;
		//				handler.sendMessage(msg);
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Message msg = new Message();
				Log.i("T5", "信息如："+successData.toString());
				msg.obj = successData;
				handler.sendMessage(msg);
			}

			public void onFailure(JSONObject failureData) {
				Log.i("T5", "错误信息如："+failureData.toString());
				Message msg = new Message();
				msg.obj = failureData;
				handler.sendMessage(msg);
			}
		};

		cilpInterface.getSignPhotoData(listener);
	}

	private void getEncryData(final String strTimeout, final String[] keys)
	{
		new Thread(){
			public void run() {
				//				String[] ret = new Sign().keyAffuse(keys);
				//				if (ret[0].equals("0")) {
				//					String[] aryRet = new Sign().getSignPhotoData(strTimeout);
				//					Message msg = new Message();
				//					msg.obj = aryRet;
				//					handler.sendMessage(msg);
				//				} else {
				//					Message msg = new Message();
				//					msg.obj = ret;
				//					handler.sendMessage(msg);
				//				}
			};
		}.start();
	}
}
