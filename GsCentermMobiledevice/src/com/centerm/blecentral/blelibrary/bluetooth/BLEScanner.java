package com.centerm.blecentral.blelibrary.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jqj on 2016/5/24.
 * 鏉╂瑤閲滅猾璇差嚠BluetoothLeScanner鏉╂稖顢戞禍鍡楃殱鐟侊拷
 */
@SuppressLint("NewApi")
public final class BLEScanner {

	private static final String TAG = BLEScanner.class.getSimpleName();

	private WeakReference<Context> contextWeakReference;

	private IScanResultListener scanResultListener;
	private BluetoothLeScanner scanner;
	private ScanCallback scanCallback;
	private ScanSettings scanSettings;
	private List<ScanFilter> filters;

	private static BLEScanner instance;

	/**
	 * 閸楁洑绶ュΟ鈥崇础
	 *
	 * @param context  娣囨繂鐡╟ontext閻ㄥ嫬绱╅悽锟�
	 * @param listener 閹殿偅寮跨紒鎾寸亯閻ㄥ埐istener
	 * @return BLEScanner閻ㄥ嫬鐤勬笟锟�
	 */
	public static BLEScanner getInstance(Context context, IScanResultListener listener) {
		if (instance == null) {
			instance = new BLEScanner(context);
		} else {
			instance.contextWeakReference = new WeakReference<Context>(context);
		}
		instance.scanResultListener = listener;
		return instance;
	}

	/**
	 * 瀵拷婵澹傞幓蹇撴噯閸ュ娈戠拋鎯ь槵
	 *
	 * @return 瀵拷婵澹傞幓蹇斿灇閸旂喕绻戦崶鐎焤ue, 閸氾箑鍨潻鏂挎礀false
	 */
	public boolean startScan() {
		Context context = contextWeakReference.get();
		if (context == null) {
			return false;
		}

		BluetoothAdapter bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "bluetoothAdapter is null");
			return false;
		}
		scanner = bluetoothAdapter.getBluetoothLeScanner();
		if (scanner == null) {
			Log.e(TAG, "bluetoothLeScanner is null");
			return false;
		}
//		scanner.startScan(filters, scanSettings, scanCallback);
		scanner.startScan(scanCallback);
		Log.i(TAG, "Start scan success");
		return true;
	}

	public void stopScan() {
		if (scanner == null || scanCallback == null) {
			return;
		}
		scanner.stopScan(scanCallback);
	}

	private BLEScanner(Context context) {
		contextWeakReference = new WeakReference<>(context);
		initScanData();
	}


	private void initScanData() {
		scanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				super.onScanResult(callbackType, result);
				Log.i(TAG, "onScanResult" + result);
				String address = result.getDevice().getAddress();
				String name;
				ScanRecord scanRecord = result.getScanRecord();
				name = scanRecord == null ? "unknown" : scanRecord.getDeviceName();
				scanResultListener.onResultReceived(name, address);
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				super.onBatchScanResults(results);
				Log.e(TAG, "onBatchScanResults");
			}

			@Override
			public void onScanFailed(int errorCode) {
				super.onScanFailed(errorCode);
				Log.e(TAG, "onScanFailed");
				scanResultListener.onScanFailed(errorCode);
			}
		};
		filters = new ArrayList<>();
		filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BLEProfile.UUID_SERVICE)).build());
		scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
	}

	public interface IScanResultListener {
		/**
		 * 鏉╂瑤閲滈弬瑙勭《娴兼艾婀幋鎰閹恒儲鏁归崚鐗堝閹诲繒绮ㄩ弸婊勬鐠嬪啰鏁�
		 *
		 * @param deviceName    鐠佹儳顦崥宥囆�
		 * @param deviceAddress 鐠佹儳顦崷鏉挎絻
		 */
		void onResultReceived(String deviceName, String deviceAddress);

		/**
		 * 鏉╂瑤閲滈弬瑙勭《娴兼艾婀幍顐ｅ伎婢惰精瑙﹂弮鎯扮殶閻㈩煉绱�
		 *
		 * @param errorCode 鐠囬攱鐓￠梼鍖癱anCallback缁崵娈慉PI
		 */
		void onScanFailed(int errorCode);
	}

}
