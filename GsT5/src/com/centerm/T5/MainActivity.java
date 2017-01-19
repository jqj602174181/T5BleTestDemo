package com.centerm.T5;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.centerm.T5.utils.CrashHandler;

public class MainActivity extends Activity{

	public static final int REQUEST_ENABLE_BLUETOOTH = 15;//请求打开蓝牙

	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//记录崩溃日志
		//	CrashHandler.getInstance().init(getApplicationContext());
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkBluetoothOpened();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_CANCELED) {
			finish();
		}
	}

	@SuppressLint("NewApi") private void checkBluetoothOpened() {
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("提示");
		builder.setMessage("确认退出？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
			}
		});
		builder.setNeutralButton("取消", null);
		builder.create().show();
	}
}
