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
	public static final int VendorID = 0x2B46; //T5 �򿪵���18D1 �رյ���2B46
	public static final int ProductID = 0xBC01;//T5 �򿪵���D003 �رյ���BC01 //CR200��BE01
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
	 * ���豸
	 * 
	 * @return
	 */
	@Override
	public int openDevice() {
		int ret = 0;
		Log.i(TAG, "Begin to open device HID");
		ret = findUsbDevice();
		Log.i(TAG, "1. findUsbDevice �� " + ret);
		if (ret != 0)
		{
			return ret;
		}

		ret = findInterface();
		Log.i(TAG, "2. findInterface �� " + ret);
		if (ret != 0)
		{
			return ret;
		}

		ret = connectDevice();
		Log.i(TAG, "3. connectDevice �� " + ret);
		if (ret != 0)
		{
			return ret;
		}
		
		isConnected = true;
		return ret;
	}
	
	/**
	 * �ر��豸
	 * 
	 * @return
	 */
	@Override
	public int closeDevice()
	{
		Log.i(TAG,"�ر�HID����");
		if (deviceConnection != null)
		{
			deviceConnection.releaseInterface(usbInf);
			deviceConnection.close();
			isConnected = false;
		}
		return 0;
	}
	
	/* ����USB�豸 */
	private int findUsbDevice()
	{
		int ret = 0;

		if (usbManager == null)
		{
			Log.e(TAG, "û���豸������");
			return ResultCode.USBMANAGER_IS_NULL;
		}

		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		if (deviceList.isEmpty())
		{
			Log.e(TAG, "�豸��Ϊ��");
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
					Log.i(TAG, "û�ҵ��豸");
					return ResultCode.USB_FIND_DEVICE_FAIL;
				}
			}
		}

		return ret;
	}

	/* ��ȡ�ӿ� */
	private int findInterface()
	{
		int ret = 0;
		if (usbDevice == null)
		{
			return ResultCode.USB_FIND_DEVICE_FAIL;
		}
		
		for (int i = 0; i < usbDevice.getInterfaceCount(); i++)
		{
			// ��ȡ�豸�ӿڣ�һ�㶼��һ���ӿڣ�����Դ�ӡgetInterfaceCount()�����鿴��
			// �ڵĸ�����������ӿ����������˵㣬OUT �� IN
			Log.i(TAG,"�ӿ�������" + usbDevice.getInterfaceCount());
			UsbInterface intf = usbDevice.getInterface(i);
			if (intf != null){
				Log.i(TAG,"intf.getInterfaceClass():" + intf.getInterfaceClass() + " intf.getInterfaceSubclass():"+intf.getInterfaceSubclass()  + " intf.getInterfaceProtocol():"+ intf.getInterfaceProtocol());
				usbInf = intf;
				return ret;
			}
			if (intf == null)
			{
				Log.i(TAG, "û�ҵ��豸�ӿ�");
				return ResultCode.USB_FIND_INTERFACE_FAIL;
			}
		}
		return ret;
	}

	/* ���Ȩ�ޡ����豸������˵� */
	private int connectDevice()
	{
		int ret = 0;
		UsbDeviceConnection connection = null;
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);
		//����requestPermission()��������ʾ��������豸��Ȩ�޵ĶԻ���
		usbManager.requestPermission(usbDevice, mPermissionIntent);

		// ��ʱ��ûȨ�޾ͷ���
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

		// ���豸����ȡ UsbDeviceConnection���������豸�����ں����ͨѶ
		connection = usbManager.openDevice(usbDevice);
		if (connection == null)
		{
			Log.e(TAG, "�豸����ʧ��");
			return ResultCode.USB_CONNECT_FAIL;
		}

		if (connection.claimInterface(usbInf, true))
		{
			Log.e(TAG, "�����豸�ɹ�");
			deviceConnection = connection;//����android�豸�Ѿ�����HID�豸
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
	 * ��ȡ�˵�
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
			Log.e(TAG, "��ȡ�豸����˵�ʧ��");
			return ResultCode.USB_GET_EPIN_FAIL;
		}

		if (intf.getEndpoint(0) != null) {
			epIn = intf.getEndpoint(0);
		} else {
			Log.e(TAG, "��ȡ�豸����˵�ʧ��");
			return ResultCode.USB_GET_EPOUT_FAIL;
		}
		
		return ret;
	}

	/**
	 * HID���ݴ���(���ͺͽ���) - ���жϳ�ʱ
	 * 
	 * @param byReq     ���ͱ�������
	 * @param byReqLen  ���ͱ��ĳ���
	 * @param byRes     ��Ӧ��������
	 * @param nTimeout  ��ʱʱ��
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
		
		//��������ǰ�����ͨ���ŵ�
		byte[] dataClear = new byte[1024];
		int dataClearLen = 0;
		while(dataClearLen >= 0){
			dataClearLen = deviceConnection.bulkTransfer(epIn, dataClear, dataClear.length, 500);
		}
		
		// ���������ж��Ƿ�ɹ�
		int ret = deviceConnection.bulkTransfer(epOut, byReq, byReq.length, timeout * 1000);
		if (byReq.length != ret)
		{
			Log.e("transfer", "��������ʧ�� :" + ret + " " + byReq.length + " " + (deviceConnection == null));
			return ResultCode.USB_SEND_MESSSAGE_FAIL;
		} else {
			Log.e("transfer", "�������ݳɹ� :" + ret + " " + byReq.length + " " + (deviceConnection == null));
		}
		
		// ��������
		//ret = deviceConnection.bulkTransfer(epIn, byRes, byRes.length, timeout * 1000);
		int readedDataLength = 0;
		while(true){
			byte[] epInByteData = new byte[1024];
			
			ret = deviceConnection.bulkTransfer(epIn, epInByteData, epInByteData.length, (timeout + 5) * 1000);
			Log.i("readHid","�õ����ݳ��ȣ� " + ret + " " + new String(epInByteData).trim() + " length: " + getRealReadedLength(epInByteData));
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
