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
	 * 采用单例模式，保证socket通信正常
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
	 * 判断设备是否已经连接成功
	 */
	public boolean isDeviceConnect()
	{
		if (device == null) {
			return false;
		}
		return device.isDeviceConnect();
	}

	/**
	 * 连接T5设备
	 * 
	 * @param readType
	 *            1：USB HID方式连接T5设备
	 *            2：蓝牙方式连接T5设备
	 * @param mac 
	 * 			     蓝牙mac地址
	 * @param cxt 上下文
	 * 
	 * @return 0－>Success； 错误码－> Fail
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
	 * 关闭与T5设备的连接
	 * 
	 * @return 0－>Success； 错误码－> Fail
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
	 * 发送和接收数据
	 * 
	 * @return 错误码－> Fail  正数－>Success
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
