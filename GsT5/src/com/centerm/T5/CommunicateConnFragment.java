package com.centerm.T5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.blecentral.ConnectType;
import com.centerm.blecentral.adapter.ScanListAdapter;
import com.centerm.blecentral.blelibrary.bluetooth.BLEClient;
import com.centerm.blecentral.blelibrary.bluetooth.IBLECallback;
import com.centerm.blecentral.blelibrary.bluetooth.LEScanner;
import com.centerm.blecentral.datas.ScanData;
import com.centerm.device.ResultCode;
import com.centerm.device.TransControl;
import com.centerm.intf.CilpInterface;

@SuppressLint("NewApi")
public class CommunicateConnFragment extends Fragment implements OnClickListener, IBLECallback{
	private static final String TAG = "CommunicateConnFragment";
	private Context context;
	private Button bt_bt,bt_conn_status;
	private EditText et_mac = null;
	private boolean isBlueTooth = true;
	private RadioButton hid_conn, bt_conn, ble_conn;
	private TextView tvUSBStat, tvBTStat;
	public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	public UsbConnReceiver mUsbReceiver;
	public BlutoothConnReceiver mBlutoothReceiver;
	private SharedPreferences sp = null;
	private final int BT = 0, HID = 1;
	public static final int VendorID = 0x2B46; //T5 打开调试18D1 关闭调试2B46
	public static final int ProductID = 0xBC01;//T5 打开调试D003 关闭调试BC01
	private static boolean isBtConnect = false, isHidConnect = false;
	private ProgressDialog pDialog;
	private MyBtReceiver myBtReceiver = null;

	private LinearLayout fristBlock, secondBlock;
	private Button ble_find, ble_lianjie, ble_stop;
	private TextView mac_value;
	private ListView scan_listView;
	public static final int SCAN_DURATION = 10000;//扫描时长

	private Button btnStartScan;
	private Button btnSendMsg;
	private Button btnCheckAdvertise;
	private Button btnStop;
	private EditText etMsg;
	private ListView listChat;

	private LEScanner leScanner;
	private BLEClient bleClient;
	private List<ScanData> scanList;
	private BaseAdapter chatListAdapter;
	private BaseAdapter scanListAdapter;
	private ConnectType connectType;
	private MyHandler nHandler;

	//蓝牙连接状态及标志
	final static int MSG_CONNED = 0;
	final static int MSG_CONNING = 1;
	final static int MSG_NOCONN = 2;
	final static int MSG_CANT = 3;
	private boolean connFlag = false;

	private ConnectThread connectThread;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			pDialog.dismiss();
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_CONNED: {
				tvBTStat.setText("蓝牙已连接");
				connFlag = true; 
				break;
			}
			case MSG_CONNING: {
				tvBTStat.setText("蓝牙断开中");
				break;
			}
			case MSG_NOCONN: {
				tvBTStat.setText("蓝牙断开中");
				break;
			}
			case MSG_CANT: {
				tvBTStat.setText("蓝牙无法连接");
				break;
			}
			default:
				setConnectState(msg.what);
				break;
			}
		}
	};

	private void setConnectState(int ret)
	{

		if (!isBlueTooth) {
			//int hidRet = TransControl.getInstance().openDevice(1, null);
			int hidRet = ret;
			if (hidRet == ResultCode.DEVICE_PARAM_ERROR) {
				//Toast.makeText(context, "USB HID传入参数错误", Toast.LENGTH_LONG).show();
				showTip("USB HID传入参数错误");
			} else if (hidRet == ResultCode.USBMANAGER_IS_NULL) {
				//Toast.makeText(context, "USB HID不支持", Toast.LENGTH_LONG).show();
				showTip("USB HID不支持");
			} else if (hidRet == ResultCode.DEVICE_LIST_NULL) {
				//Toast.makeText(context, "USB HID设备表为空", Toast.LENGTH_LONG).show();
				showTip("USB HID设备表为空");
			} else if (hidRet == ResultCode.USB_FIND_DEVICE_FAIL) {
				//Toast.makeText(context, "USB HID查找设备失败", Toast.LENGTH_LONG).show();
				showTip("USB HID查找设备失败");
			} else if (hidRet == ResultCode.USB_FIND_INTERFACE_FAIL) {
				//	Toast.makeText(context, "USB HID获取设备接口失败",Toast.LENGTH_SHORT).show();
				showTip("USB HID获取设备接口失败");
			} else if (hidRet == ResultCode.USB_NO_PERMISSION) {
				//	Toast.makeText(context, "USB HID没有权限", Toast.LENGTH_LONG).show();
				showTip("USB HID没有权限");
			} else if (hidRet == ResultCode.USB_CONNECT_FAIL) {
				//Toast.makeText(context, "USB HID连接失败", Toast.LENGTH_LONG).show();
				showTip("USB HID连接失败");
			} else if (hidRet == ResultCode.USB_CLAIMED_FAILED) {
				//	Toast.makeText(context, "USB HID Claimed失败",Toast.LENGTH_SHORT).show();
				showTip("USB HID Claimed失败");
			} else {
				//Toast.makeText(context, "USB HID通讯建立成功", Toast.LENGTH_LONG).show();
				showTip( "USB HID通讯建立成功");
				isHidConnect = true;
			}
			if (isBtConnect) {
				setBluetoothState("蓝牙连接断开");
				isBtConnect = false;
			}
		} else {
			String macString = "";
			//			if ("".equals(macString)) {
			//				Toast.makeText(context, "请输入蓝牙mac值", 0).show();
			//				return;
			//			}
			//			int ret = TransControl.getInstance().openDevice(2, context, macString);
			Log.i(TAG,String.valueOf(cilpInterface == null));
			//int ret = cilpInterface.connectBluetooth(macString);
			Log.i(TAG,"Fly" + String.valueOf(ret));
			if (ret == ResultCode.DEVICE_PARAM_ERROR) {
				showTip("传入参数错误");
				//Toast.makeText(context, "传入参数错误", Toast.LENGTH_LONG).show();
				pDialog.dismiss();
			} else if (ret == ResultCode.BLUETOOTH_ADAPTER_NOT_FIND) {
				//	Toast.makeText(context, "没找到本机蓝牙设配器", Toast.LENGTH_LONG).show();
				showTip("没找到本机蓝牙设配器");
				pDialog.dismiss();
			} else if (ret == ResultCode.BLUETOOTH_MAC_ERROR) {
				//	Toast.makeText(context, "蓝牙MAC地址格式错误", Toast.LENGTH_LONG).show();
				showTip("蓝牙MAC地址格式错误");
				pDialog.dismiss();
			} else if (ret == ResultCode.BLUETOOTH_OPEN_FAIL) {
				//Toast.makeText(context, "开启蓝牙失败或蓝牙已经开启", Toast.LENGTH_LONG).show();
				showTip("开启蓝牙失败或蓝牙已经开启");
				pDialog.dismiss();
			} else if (ret == ResultCode.BLUETOOTH_DEVICE_NOT_FIND) {
				//	Toast.makeText(context, "没找到相应蓝牙设备", Toast.LENGTH_LONG).show();
				showTip("没找到相应蓝牙设备");
				pDialog.dismiss();
			} else if (ret != 0) {
				//			Toast.makeText(context, "蓝牙通讯建立失败", Toast.LENGTH_LONG).show();
				showTip("蓝牙通讯建立失败");
				pDialog.dismiss();		
			}
			if (isHidConnect) {
				setUsbState("USB通讯断开");
				isHidConnect = false;
			}
		}
	}

	//获得CilpInterface实例
	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), handler);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//		context = inflater.getContext();
		context = getActivity();
		View view = (View) inflater.inflate(R.layout.communication_conn, null);
		tvBTStat = (TextView) view.findViewById(R.id.tvBTStat);
		tvUSBStat = (TextView) view.findViewById(R.id.tvUSBStat);
		bt_bt = (Button) view.findViewById(R.id.bt_conn_open);
		bt_conn_status = (Button)view.findViewById(R.id.bt_conn_status);
		bt_bt.setOnClickListener(this);
		bt_conn_status.setOnClickListener(this);
		et_mac = (EditText) view.findViewById(R.id.etMac);
		hid_conn = (RadioButton) view.findViewById(R.id.hid_conn);
		bt_conn = (RadioButton) view.findViewById(R.id.bt_conn);
		ble_conn = (RadioButton) view.findViewById(R.id.ble_conn);

		fristBlock = (LinearLayout) view.findViewById(R.id.fristBlock);
		secondBlock = (LinearLayout) view.findViewById(R.id.secondBlock);
		ble_find = (Button) view.findViewById(R.id.ble_find);
		ble_find.setOnClickListener(this);
		ble_lianjie = (Button) view.findViewById(R.id.ble_lianjie);
		ble_lianjie.setOnClickListener(this);
		ble_stop = (Button) view.findViewById(R.id.ble_stop);
		ble_stop.setOnClickListener(this);

		mac_value = (TextView) view.findViewById(R.id.mac_value);
		scan_listView = (ListView) view.findViewById(R.id.list_scan_result);

		sp = context.getSharedPreferences("bt_config", Activity.MODE_PRIVATE);
		if (sp != null) {
			et_mac.setText(sp.getString("mac", ""));
		}

		checkDeviceState();
		initDevice();		
		hid_conn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					isBlueTooth = false;
					et_mac.setFocusable(false);

					fristBlock.setVisibility(View.VISIBLE);
					secondBlock.setVisibility(View.GONE);
				}
			}
		});

		bt_conn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					isBlueTooth = true;
					et_mac.setFocusable(true);
					et_mac.setFocusableInTouchMode(true);
					et_mac.requestFocus();
					et_mac.findFocus();

					fristBlock.setVisibility(View.VISIBLE);
					secondBlock.setVisibility(View.GONE);
				}
			}
		});

		ble_conn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					fristBlock.setVisibility(View.GONE);
					secondBlock.setVisibility(View.VISIBLE);
				}
			}
		});

		pDialog = new ProgressDialog(context);
		pDialog.setMessage("正在连接，请稍候...");
		pDialog.setCanceledOnTouchOutside(false);
		connectThread = new ConnectThread();
		connectThread.start();

		initBle();

		return view;
	}

	private void initBle(){
		scanList = new ArrayList<ScanData>();
		scanListAdapter = new ScanListAdapter(scanList, context);
		MyApp.getInstance().connected = false;
		nHandler = new MyHandler();
		scan_listView.setAdapter(scanListAdapter);
		bleClient = new BLEClient(context, this);
		leScanner = LEScanner.getInstance(context, new LEScanner.IScanResultListener() {
			@Override
			public void onResultReceived(String deviceName, String deviceAddress) {
				for(ScanData data : scanList){
					if(deviceAddress.equals(data.getAddress())){
						return;
					}
				}
				scanList.add(new ScanData(deviceName, deviceAddress));
				nHandler.sendEmptyMessage(MyHandler.REFRESH_SCAN_LIST);
			}

			@Override
			public void onScanFailed(int errorCode) {
				Log.e(TAG, "scan failed");
			}
		});

		scan_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mac_value.setText(scanList.get(position).getAddress());
				//				bleClient.startConnect(scanList.get(position).getAddress());
				leScanner.stopScan();
			}
		});

		nHandler.attach(chatListAdapter, scanListAdapter);
	}

	private void checkDeviceState(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			setBluetoothState("该设备没蓝牙");
		} else {
			if (mBluetoothAdapter.isEnabled()) {
				if (isBtConnect) {
					setBluetoothState("蓝牙已连接");
				} else {
					setBluetoothState("蓝牙已打开");
				}
			} else {
				setBluetoothState("蓝牙已关闭");
			}
		}

		UsbDevice usbDevice = null;
		UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		if (!usbManager.getDeviceList().isEmpty()) {
			HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
			for (UsbDevice device : deviceList.values())
			{
				if (device.getVendorId() == VendorID && device.getProductId() == ProductID)
				{
					usbDevice = device;
				}
			}
			if (usbDevice != null && usbDevice.getInterfaceCount()>0) {
				setUsbState("已接入");
				if (isHidConnect) {
					setUsbState("USB通讯已建立");
				}
			} else {
				setUsbState("未接入");
			}
		} else {
			setUsbState("无设备管理器");
		}
	}

	private void showTip(String tip)
	{
		Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_conn_open:
			Log.e("con"," connect open");
			pDialog.show();
			connectThread.startConnect(et_mac.getText().toString());
			break;
		case R.id.bt_conn_status:
			Toast.makeText(context, "蓝牙连接状态：" + String.valueOf(cilpInterface.isBTConnect()), Toast.LENGTH_LONG).show();
			break;
		case R.id.ble_find: //ble搜索
			startScan();
			break;
		case R.id.ble_lianjie: //ble连接
			String mac = mac_value.getText().toString().trim();
			if(mac.equals("") || mac.equals("55:66:99:88:33:22")){
				Toast.makeText(context, "请选择蓝牙！", Toast.LENGTH_SHORT).show();
				return;
			}

			bleClient.startConnect(mac_value.getText().toString());
			break;
		case R.id.ble_stop: //ble断开
			bleClient.stopConnect();
			break;
		default:
			break;
		}
	}

	private void startScan() {
		scanList.clear();
		mHandler.sendEmptyMessage(MyHandler.REFRESH_SCAN_LIST);
		leScanner.startScan();
		connectType = ConnectType.CENTRAL;
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				leScanner.stopScan();
			}
		}, SCAN_DURATION);
	}

	@SuppressLint("ShowToast") private void initDevice() {
		Log.i(TAG,String.valueOf(cilpInterface == null));

		Log.i(TAG, "initDevice");
		// USB插拔监听处理
		mUsbReceiver = new UsbConnReceiver();
		IntentFilter mUSBFilter;
		mUSBFilter = new IntentFilter(ACTION_USB_PERMISSION);
		mUSBFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		mUSBFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mUSBFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		context.registerReceiver(mUsbReceiver, mUSBFilter);

		// 蓝牙状态监听处理
		mBlutoothReceiver = new BlutoothConnReceiver();
		IntentFilter mBluetoothFilter;
		mBluetoothFilter = new IntentFilter(
				BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mBluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mBluetoothFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		context.registerReceiver(mBlutoothReceiver, mBluetoothFilter);

		//自定义蓝牙连接后的广播
		myBtReceiver = new MyBtReceiver();
		IntentFilter mBtFilter = new IntentFilter("android.ccb.ygqd.centerm.btconnectBroadcast");
		context.registerReceiver(myBtReceiver, mBtFilter);
	}

	class MyBtReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyBtReceiver", "MyBtReceiver");
			if (intent.getStringExtra("msg").equals("1")) {
				setBluetoothState("蓝牙连接成功");
				isBtConnect = true;
			} else {
				setBluetoothState("蓝牙连接失败");
				isBtConnect = false;
			}

			pDialog.dismiss();
		}
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String state = bundle.getString("state");

			switch (msg.what) {
			case BT:
				tvBTStat.setText(state);
				break;
			case HID:
				tvUSBStat.setText(state);
				break;
			default:
				break;
			}
		}
	};

	void setUsbState(final String strUsbStat) {
		if (strUsbStat == null) {
			return;
		}
		Log.i(TAG, "setUsbState:" + strUsbStat);

		Bundle bundle = new Bundle();
		bundle.putString("state", strUsbStat);
		Message msg = new Message();
		msg.what = HID; 
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	void setBluetoothState(final String strStat) {
		if (strStat == null) {
			return;
		}
		Log.i(TAG, "setBluetoothState:" + strStat);

		Bundle bundle = new Bundle();
		bundle.putString("state", strStat);
		Message msg = new Message();
		msg.what = BT; 
		msg.setData(bundle);
		mHandler.sendMessage(msg);	
	}

	/**
	 * usb状态监听器
	 */
	class UsbConnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String strUsbStat = null;
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false))
					strUsbStat = "USB通讯建立成功";
				else
					strUsbStat = "USB通讯建立失败";
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
					|| UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
				Log.i(TAG, "监听USB线拔出");

				UsbAccessory access = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (null == access)
					strUsbStat = "USB设备未连接!";
			} else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.i(TAG, "监听USB线插入");

				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);

				if (device != null) {
					strUsbStat = "USB设备已连接，点击授权";
				}
			} else {
				Log.i(TAG,"未捕获到的USB状态: " + action);
				strUsbStat = "USB处于未知状态 : " + action;
			}
			setUsbState(strUsbStat);
		}
	};

	/**
	 * 蓝牙状态监听器
	 */
	class BlutoothConnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String result = null;
			int state = 0;

			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				Log.i(TAG, "ACTION_STATE_CHANGED");
				state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				result = updateBluetoothStateChange(state);
				setBluetoothState(result);
			} else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				Log.i(TAG, "ACTION_BOND_STATE_CHANGED");
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				state = btDevice.getBondState();
				if (!isBtConnect) {
					result = updateBluetoothBondStateChange(state);
					setBluetoothState(result);
				}
			} else if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
				Log.i(TAG, "ACTION_PAIRING_REQUEST");
			}
		}

		private String updateBluetoothBondStateChange(int state) {
			String result = null;
			switch (state) {
			case BluetoothDevice.BOND_BONDING:
				result = "正在配对...";
				break;
			case BluetoothDevice.BOND_BONDED:
				result = "配对成功";
				break;
			case BluetoothDevice.BOND_NONE:
				result = "取消配对或配对失败";
			default:
				result = "未知异常";
				break;
			}
			return result;
		}

		private String updateBluetoothStateChange(int state) {
			String result = null;

			switch (state) {
			case BluetoothAdapter.STATE_TURNING_ON:
				result = "正在打开...";
				break;
			case BluetoothAdapter.STATE_ON:
				result = "蓝牙已打开";
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				result = "正在关闭...";
				break;
			case BluetoothAdapter.STATE_OFF:
				result = "蓝牙已关闭";
				break;
			default:
				result = "未知异常";
				break;
			}
			return result;
		}
	}

	@Override
	public void onStop() {
		String macString = et_mac.getText().toString().trim();
		Editor editor = sp.edit();
		editor.putString("mac", macString);
		editor.commit();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		context.unregisterReceiver(mBlutoothReceiver);
		context.unregisterReceiver(mUsbReceiver);
		context.unregisterReceiver(myBtReceiver);
		connectThread.quitThread();
	}

	private class ConnectThread extends Thread{
		private Object lockObj;
		private boolean isStop;
		public ConnectThread()
		{
			lockObj = new Object();
			isStop = false;
		}

		public void run()
		{
			super.run();
			while(!isStop){
				synchronized(lockObj){
					try {
						lockObj.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(isStop){
					return;
				}
				int ret = 0;
				Log.e("con","ret is "+ret);
				if(isBlueTooth){
					ret =  cilpInterface.connectBluetooth("mac");
				}else{

				}
				Log.e("con","ret is112233 "+ret);
				handler.sendEmptyMessage(ret);
			}
		}

		public void quitThread()
		{
			isStop = true;
			synchronized(lockObj){
				lockObj.notifyAll();
			}
		}

		public void startConnect(String macAddr)
		{
			synchronized(lockObj){
				lockObj.notifyAll();
			}
		}
	}

	//Handler来刷新UI
	private class MyHandler extends Handler {
		//可以使用runOnUiThread或者持有View使用View.post来简化代码
		public static final int REFRESH_SCAN_LIST = 250;
		public static final int REFRESH_CHAT_LIST = 38;

		private BaseAdapter chatAdapter;
		private BaseAdapter scanAdapter;

		public void attach(BaseAdapter chatAdapter, BaseAdapter scanAdapter) {
			this.chatAdapter = chatAdapter;
			this.scanAdapter = scanAdapter;
		}

		public void detach() {
			chatAdapter = null;
			scanAdapter = null;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == REFRESH_CHAT_LIST) {
				if (chatAdapter != null) {
					chatAdapter.notifyDataSetChanged();
				}
			} else if (msg.what == REFRESH_SCAN_LIST) {
				if (scanAdapter != null) {
					scanAdapter.notifyDataSetChanged();
				}
			} else if(msg.what == 1){
				Toast.makeText(context, "连接成功！", Toast.LENGTH_SHORT).show();
			} else if(msg.what == 2){
				Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onConnected() {
		MyApp.getInstance().connected = true;
		TransControl.getInstance().openDevice("BLE");
		nHandler.sendEmptyMessage(1);
	}

	@Override
	public void onDisconnected() {
		MyApp.getInstance().connected = false;
		nHandler.sendEmptyMessage(2);
	}

	@Override
	public void onDataReceived(byte[] data) {

	}
}
