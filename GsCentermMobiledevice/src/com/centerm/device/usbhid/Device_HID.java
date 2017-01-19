package com.centerm.device.usbhid;

import java.util.HashMap;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.centerm.device.DeviceIntf;
import com.centerm.device.ResultCode;

public class Device_HID implements DeviceIntf
{
	private String TAG = "Device_HID";
	public static final int VendorID = 0x2B46; //T5 打开调试18D1 关闭调试2B46
	public static final int ProductID = 0xBC01;//T5 打开调试D003 关闭调试BC01 //CR200：BE01
	private static final String ACTION_USB_PERMISSION ="com.android.example.USB_PERMISSION";
	private UsbManager usbManager;
	private UsbDevice usbDevice;
	private UsbInterface usbInf;
	private UsbEndpoint epOut, epIn;
	private UsbDeviceConnection deviceConnection;
	private boolean isConnected = false;
	private Context context;
	
	public Device_HID(Context context) {
		this.context = context;
		usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	   public void onReceive(Context context, Intent intent) {
	       String action = intent.getAction();
	       if (ACTION_USB_PERMISSION.equals(action)) {
	           synchronized (this) {
	        	   UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	               if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	            	   usbDevice = device;
	               } 
	               else {
	                   Log.d(TAG, "permission denied for device " + usbDevice);
	               }
	           }
	       }
	   }
	};

	@Override
	public boolean isDeviceConnect() {
		
		return isConnected;
	}
	
	/**
	 * 打开设备
	 * 
	 * @return
	 */
	@Override
	public int openDevice() {
		int ret = 0;
		Log.i(TAG, "Begin to open device HID");
		ret = findUsbDevice();
		Log.i(TAG, "1. findUsbDevice ： " + ret);
		if (ret != 0)
		{
			return ret;
		}

		ret = findInterface();
		Log.i(TAG, "2. findInterface ： " + ret);
		if (ret != 0)
		{
			return ret;
		}

		ret = connectDevice();
		Log.i(TAG, "3. connectDevice ： " + ret);
		if (ret != 0)
		{
			return ret;
		}
		
		isConnected = true;
		return ret;
	}
	
	/**
	 * 关闭设备
	 * 
	 * @return
	 */
	@Override
	public int closeDevice()
	{
		Log.i(TAG,"关闭HID连接");
		if (deviceConnection != null)
		{
			deviceConnection.releaseInterface(usbInf);
			deviceConnection.close();
			isConnected = false;
		}
		return 0;
	}
	
	/* 查找USB设备 */
	private int findUsbDevice()
	{
		int ret = 0;

		if (usbManager == null)
		{
			Log.e(TAG, "没有设备管理器");
			return ResultCode.USBMANAGER_IS_NULL;
		}

		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		if (deviceList.isEmpty())
		{
			Log.e(TAG, "设备表为空");
			return ResultCode.DEVICE_LIST_NULL;
		}

		usbDevice = null;
		for (UsbDevice device : deviceList.values())
		{
			if (device.getVendorId() == VendorID
					&& device.getProductId() == ProductID)
			{
				usbDevice = device;
				if (usbDevice == null)
				{
					Log.i(TAG, "没找到设备");
					return ResultCode.USB_FIND_DEVICE_FAIL;
				}
			}
		}

		return ret;
	}

	/* 获取接口 */
	private int findInterface()
	{
		int ret = 0;
		if (usbDevice == null)
		{
			return ResultCode.USB_FIND_DEVICE_FAIL;
		}
		
		for (int i = 0; i < usbDevice.getInterfaceCount(); i++)
		{
			// 获取设备接口，一般都是一个接口，你可以打印getInterfaceCount()方法查看接
			// 口的个数，在这个接口上有两个端点，OUT 和 IN
			Log.i(TAG,"接口数量：" + usbDevice.getInterfaceCount());
			UsbInterface intf = usbDevice.getInterface(i);
			if (intf != null){
				Log.i(TAG,"intf.getInterfaceClass():" + intf.getInterfaceClass() + " intf.getInterfaceSubclass():"+intf.getInterfaceSubclass()  + " intf.getInterfaceProtocol():"+ intf.getInterfaceProtocol());
				usbInf = intf;
				return ret;
			}
			if (intf == null)
			{
				Log.i(TAG, "没找到设备接口");
				return ResultCode.USB_FIND_INTERFACE_FAIL;
			}
		}
		return ret;
	}

	/* 检查权限、打开设备、分配端点 */
	private int connectDevice()
	{
		int ret = 0;
		UsbDeviceConnection connection = null;
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);
		//调用requestPermission()方法，显示申请接入设备的权限的对话框：
		usbManager.requestPermission(usbDevice, mPermissionIntent);

		// 超时还没权限就返回
		long startTime = System.currentTimeMillis();
		while(true){
			if ((System.currentTimeMillis() - startTime) > 20 * 1000)
			{
				return ResultCode.USB_NO_PERMISSION;
			}
			if (usbManager.hasPermission(usbDevice)){
				break;
			}
		}

		// 打开设备，获取 UsbDeviceConnection对象，连接设备，用于后面的通讯
		connection = usbManager.openDevice(usbDevice);
		if (connection == null)
		{
			Log.e(TAG, "设备连接失败");
			return ResultCode.USB_CONNECT_FAIL;
		}

		if (connection.claimInterface(usbInf, true))
		{
			Log.e(TAG, "连接设备成功");
			deviceConnection = connection;//到此android设备已经连上HID设备
			ret = getEndpoint(deviceConnection, usbInf);
		}
		else
		{
			connection.close();
			ret = ResultCode.USB_CLAIMED_FAILED;
		}

		return ret;
	}

	/**
	 * 获取端点
	 * 
	 * @param connection
	 * @param intf
	 * @return
	 */
	private int getEndpoint(UsbDeviceConnection connection, UsbInterface intf)
	{
		int ret = 0;

		if (intf.getEndpoint(1) != null) {
			epOut = intf.getEndpoint(1);
		} else {
			Log.e(TAG, "获取设备输出端点失败");
			return ResultCode.USB_GET_EPIN_FAIL;
		}

		if (intf.getEndpoint(0) != null) {
			epIn = intf.getEndpoint(0);
		} else {
			Log.e(TAG, "获取设备输入端点失败");
			return ResultCode.USB_GET_EPOUT_FAIL;
		}
		
		return ret;
	}

	/**
	 * HID数据传输(发送和接收) - 需判断超时
	 * 
	 * @param byReq     发送报文内容
	 * @param byReqLen  发送报文长度
	 * @param byRes     响应报文内容
	 * @param nTimeout  超时时间
	 * @return
	 */
	@Override
	public int transfer(byte[] byReq, int nReqLen, byte[] byRes, int timeout)
	{
		if (epIn == null || epOut == null)
		{
			return ResultCode.USB_EPIN_OR_EPOUT_NULL;
		}
		if (byReq == null || byRes == null) 
		{
			return ResultCode.USB_PRAM_REQ_OR_RES_NULL;
		}
		if (nReqLen < 0)
		{
			return ResultCode.USB_REQ_MESSSAGE_LEN_ERR;
		}
		
		//发送数据前先清空通信信道
		byte[] dataClear = new byte[1024];
		int dataClearLen = 0;
		while(dataClearLen >= 0){
			dataClearLen = deviceConnection.bulkTransfer(epIn, dataClear, dataClear.length, 500);
		}
		
		// 发送数据判断是否成功
		int ret = deviceConnection.bulkTransfer(epOut, byReq, byReq.length, timeout * 1000);
		if (byReq.length != ret)
		{
			Log.e("transfer", "发送数据失败 :" + ret + " " + byReq.length + " " + (deviceConnection == null));
			return ResultCode.USB_SEND_MESSSAGE_FAIL;
		} else {
			Log.e("transfer", "发送数据成功 :" + ret + " " + byReq.length + " " + (deviceConnection == null));
		}
		
		// 接收数据
		//ret = deviceConnection.bulkTransfer(epIn, byRes, byRes.length, timeout * 1000);
		int readedDataLength = 0;
		while(true){
			byte[] epInByteData = new byte[1024];
			
			ret = deviceConnection.bulkTransfer(epIn, epInByteData, epInByteData.length, (timeout + 5) * 1000);
			Log.i("readHid","得到数据长度： " + ret + " " + new String(epInByteData).trim() + " length: " + getRealReadedLength(epInByteData));
			if(ret < 0){
				break;
			} else if(getRealReadedLength(epInByteData) < 1024){
				System.arraycopy(epInByteData, 0, byRes, readedDataLength, 1024);
				readedDataLength += ret;
				break;
			} else {
				System.arraycopy(epInByteData, 0, byRes, readedDataLength, 1024);
				readedDataLength += ret;
			}
			
		}
		return readedDataLength;
	}
	
	private int getRealReadedLength(byte[] byRes){
		int realReadedLength = byRes.length;
		for(int i = byRes.length - 1;i >= 0;i --){
			if(byRes[i] == (byte)0x00){
				realReadedLength --;
			} else {
				return realReadedLength;
			}
		}
		return realReadedLength;
	}

}
