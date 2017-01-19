package com.centerm.blecentral.blelibrary.bluetooth;

import java.lang.ref.WeakReference;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

public class LEScanner {
	private static final String TAG = LEScanner.class.getSimpleName();
	private static LEScanner instance;

	private WeakReference<Context> contextWeakReference;
	private IScanResultListener scanResultListener;
	private BluetoothAdapter bluetoothAdapter;

	/**
	 * 单例模式
	 *
	 * @param context  保存context的引用
	 * @param listener 扫描结果的listener
	 * @return BLEScanner的实例
	 */
	public static LEScanner getInstance(Context context, IScanResultListener listener) {
		if (instance == null) {
			instance = new LEScanner(context);
		} else {
			instance.contextWeakReference = new WeakReference<Context>(context);
		}
		instance.scanResultListener = listener;
		return instance;
	}

	public void stopScan() {
		if (bluetoothAdapter == null) {
			return;
		}
		bluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	private LEScanner(Context context) {
		contextWeakReference = new WeakReference<>(context);
	}

	/**
	 * 开始扫描周围的设备
	 *
	 * @return 开始扫描成功返回true, 否则返回false
	 */
	public boolean startScan() {
		Context context = contextWeakReference.get();
		if (context == null) {
			return false;
		}

		bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "bluetoothAdapter is null");
			return false;
		}

		bluetoothAdapter.startLeScan(mLeScanCallback);
		return true;
	}

	/**
	 * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
	 * 
	 * **/
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord)
		{
			String name = device.getName();
			String address = device.getAddress();
			scanResultListener.onResultReceived(name, address);
		}
	};

	public interface IScanResultListener {
		/**
		 * 这个方法会在成功接收到扫描结果时调用
		 *
		 * @param deviceName    设备名称
		 * @param deviceAddress 设备地址
		 */
		void onResultReceived(String deviceName, String deviceAddress);

		/**
		 * 这个方法会在扫描失败时调用，
		 *
		 * @param errorCode 请查阅ScanCallback类的API
		 */
		void onScanFailed(int errorCode);
	}
}
