package com.centerm.device;

import android.os.Handler;

import com.centerm.blecentral.blelibrary.bluetooth.BLEClient;
import com.centerm.device.bluetooth.Device_BT;

public class TransControl {
	//	public static final int HID_TYPE = 1;
	public static final int BT_TYPE  = 2;
	private DeviceIntf device = null;
	private static TransControl instance;

	/**
	 * ���õ���ģʽ����֤socketͨ������
	 */
	public static TransControl getInstance()
	{
		if (instance == null)
		{
			instance = new TransControl();
		}
		return instance;
	}

	/**
	 * �ж��豸�Ƿ��Ѿ����ӳɹ�
	 */
	public boolean isDeviceConnect()
	{
		if (device == null) {
			return false;
		}
		return device.isDeviceConnect();
	}

	/**
	 * ����T5�豸
	 * 
	 * @param readType
	 *            1��USB HID��ʽ����T5�豸
	 *            2��������ʽ����T5�豸
	 * @param mac 
	 * 			     ����mac��ַ
	 * @param cxt ������
	 * 
	 * @return 0��>Success�� �����룭> Fail
	 */
	public int openDevice(int readType, String mac, Handler handler) {
		if (device != null){
			device.closeDevice();
		}
		switch (readType) {
		//		case HID_TYPE:
		//			if (cxt == null) {
		//				return ResultCode.DEVICE_PARAM_ERROR;
		//			}
		//			device = new Device_HID(cxt);
		//			break;
		case BT_TYPE:
			device = new Device_BT(mac, handler);
			break;
		default:
			return ResultCode.DEVICE_OPEN_FAILED;
		}
		return device.openDevice();
	}

	public void openDevice(String type) {
		if(type.equals("BLE")){
			device = BLEClient.getInstance();
		}
	}

	/**
	 * �ر���T5�豸������
	 * 
	 * @return 0��>Success�� �����룭> Fail
	 */
	public int closeDevice(){
		if (device == null) {
			return ResultCode.DEVICE_SUCCESS;
		}

		int ret = device.closeDevice();
		device = null;
		return ret;
	}

	/**
	 * ���ͺͽ�������
	 * 
	 * @return �����룭> Fail  ������>Success
	 */
	public int transfer(byte[] byReq, int byReqLen, byte[] byRes, int timeout)
	{
		if (device == null) {
			return ResultCode.DEVICE_OPEN_FAILED;
		}

		if (byReq == null || byRes == null) {
			return ResultCode.DEVICE_PARAM_ERROR;
		}

		return device.transfer(byReq, byReqLen, byRes, timeout);
	}

}
