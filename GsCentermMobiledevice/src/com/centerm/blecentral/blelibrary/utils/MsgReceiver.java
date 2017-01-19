package com.centerm.blecentral.blelibrary.utils;


import java.util.Arrays;

import android.util.Log;

/**
 * Created by jqj on 2016/5/24.
 */
public final class MsgReceiver {
	private static final String TAG = MsgReceiver.class.getSimpleName();

	private boolean sending;
	private int length;
	private int nowLength;
	private byte[] totalBytes;
	private String result;
	private IReceiver receiver;


	public MsgReceiver(IReceiver receiver) {
		init();
		this.receiver = receiver;
	}

	private void init() {
		sending = false;
		length = 0;
		nowLength = 0;
		totalBytes = new byte[0];
		result = null;
	}

	public void outputData(byte[] bytes) {
		if (!sending) {
			length = MsgCommonUtil.goInt(bytes);
			sending = true;
		} else {
			nowLength += bytes.length;
			totalBytes = MsgCommonUtil.merge(totalBytes, bytes);
			Log.e("MsgReceiver:", "nowLength£º" + nowLength);
			if (nowLength >= length) {
				receiver.receiveData(Arrays.copyOf(totalBytes, totalBytes.length));
				Log.e("MsgReceiver:", "³¤¶È£º" + totalBytes.length);
				init();
			}
		}
	}

	public interface IReceiver {
		void receiveData(byte[] data);
	}
}
