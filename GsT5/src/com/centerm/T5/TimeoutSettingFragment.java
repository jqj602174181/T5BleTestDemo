package com.centerm.T5;

import org.json.JSONObject;

import android.R.integer;
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
import android.widget.Toast;

import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;
import com.centerm.util.TimeOutUtil;

@SuppressLint("NewApi")
public class TimeoutSettingFragment extends Fragment implements OnClickListener {
	private Context context;

	private static final String TAG = "TimeoutSettingFragment";

	private Button setTimeout;

	private EditText Timeout;

	private static int timeout = 20;

	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());


	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.timeout_setting, null);

		setTimeout = (Button)view.findViewById(R.id.bt_settimeout);
		Timeout    = (EditText)view.findViewById(R.id.et_id_timeout);

		Timeout.setText(String.valueOf(timeout));
		setTimeout.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_settimeout:

			timeout = Integer.parseInt(Timeout.getText().toString());

			Log.i(TAG,String.valueOf(timeout+1));

			TimeOutUtil.setTimeoutdelay(timeout * 1000);	
			Toast.makeText(context, "…Ë÷√≥…π¶", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

}
