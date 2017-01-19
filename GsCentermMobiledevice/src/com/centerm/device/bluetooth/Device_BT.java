package com.centerm.device.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.device.DeviceIntf;
import com.centerm.device.ResultCode;
import com.centerm.message.MessageHandler;

public class Device_BT implements DeviceIntf
{
	public final static int MSG_CONNED = 0;
	public final static int MSG_CONNING = 1;
	public final static int MSG_NOCONN = 2;
	public final static int MSG_CANT = 3;
	
	private static final int PKG_HEAD_SIZE = 12;
	private String mac = null;
	private Handler handler = null;
	private boolean isDebug = true;
	private String TAG = getClass().getSimpleName();
	private BluetoothSocket btSocket = null;
	private InputStream btIs = null;
	private OutputStream btOs = null;
	private boolean isConnected = false;
	private BluetoothDevice btDevice = null;
	private clientThread clientConnectThread = null;
	
	private BluetoothAdapter btAda;
	
	public Device_BT(String mac, Handler handler) {
//		this.mac = mac;
		this.handler = handler;
	}

	private void debug(String errmsg)
	{
		if (isDebug) {
			Log.i(TAG, errmsg);
		}
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	@Override
	public boolean isDeviceConnect() {
		if(btAda == null){
			isConnected = false;
		}else if(btAda.getState() == BluetoothAdapter.STATE_OFF){
			isConnected = false;
		}else if((btIs == null) || (btOs == null)){
			isConnected = false;
		}
		
		return isConnected;
	}
	
	@Override
	public int openDevice() {
		
		//�ҵ�����������
		btAda = BluetoothAdapter.getDefaultAdapter();
		if (btAda == null) {
			debug("û�ҵ���������������");
			return ResultCode.BLUETOOTH_ADAPTER_NOT_FIND;
		} else{
			Set<BluetoothDevice> devices = btAda.getBondedDevices();
			if(devices.size()>0){
				for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){
					BluetoothDevice device = (BluetoothDevice)it.next();
					if(device.getName().substring(0, 2).equals("T5")){	// ����T5���ĳ����豸
						this.mac = device.getAddress();
						Log.i(TAG ,device.getAddress());
						break;
					}
				}
			} else {
				debug("δ�ҵ����ӵ������豸");
				return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
			}

		}
		
		if(this.mac == null){
			debug("δ�ҵ����ӵ�T5�豸");
			return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
		}
		
		//��������
		if (btAda.isEnabled() == false) {			
			if (btAda.enable()) {
				while (btAda.getState() == BluetoothAdapter.STATE_TURNING_ON
						|| btAda.getState() != BluetoothAdapter.STATE_ON) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				debug("��������ʧ�ܻ������Ѿ�����");
				return ResultCode.BLUETOOTH_OPEN_FAIL;
			}
		}
		
		//����ָ��MAC�����������
		try {
			btDevice = btAda.getRemoteDevice(mac);
			if (btDevice == null) {
				debug("û�ҵ���Ӧ�����豸");
				return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
			} else {
				if (btAda.isDiscovering())// ȡ��ɨ�裬����ƥ��᲻�ȶ�
				{
					btAda.cancelDiscovery();
				}
				if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {
					debug("������ʼ���...");
					createBond(btDevice);
				}
			}
			
			clientConnectThread = new clientThread();
			clientConnectThread.start();
			
		} catch (IllegalArgumentException e) {
			debug("���Ҳ�����ӦMAC��ַ���豸");
			return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
		}
		
		return 0;
	}
	
	/* ������� */
	private boolean createBond(BluetoothDevice dev) {
		try {
			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
			Boolean ret = (Boolean) createBondMethod.invoke(dev);
			return ret.booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/* �����ͻ���,���ӵ������ */
	private class clientThread extends Thread { 		
		@Override
		public void run() {
			try {
				/* ����һ��Socket���ӣ�ֻ��Ҫ��������ע��ʱ��UUID��
				 * ϵͳ����Զ���豸�����һ��SDP������ƥ��UUID��
				 * ������ҳɹ�����Զ���豸�������ӣ��͹���RFCOMM�ŵ���connect()�᷵�ء�
				 * ��Ҳ��һ�������ĵ��ã���������ʧ�ܻ��ǳ�ʱ��12�룩�����׳��쳣
				 */

				debug("��ʼ����...");
				btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				
				//�������������
				//���ѭ����������γ�ʱ�޷����Ӻ󷵻�
				int connectTimes = 0;
				while(connectTimes ++ < 5){
					try {
						btSocket.connect();
						break;
					} catch (Exception e) {
						continue;
					}
				}
				if(connectTimes == 6){
					isConnected = false;
					debug("��������ʧ��");
				} else {
					if (btSocket != null) {
						btIs = btSocket.getInputStream();
						btOs = btSocket.getOutputStream();
						isConnected = true;
						debug("�������ӳɹ�");
					}
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				debug("��������ʧ��"+e.getMessage());
				isConnected = false;
				try {
					if (btSocket != null) {
						btSocket.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}finally{					// �������ӽ��
				Message msg = new Message();
				if(isConnected){
					msg.what = MSG_CONNED;
					msg.obj = "�������ӳɹ�";
				}else{
					msg.what = MSG_CANT;
					msg.obj = "��������ʧ��";
				}
				handler.sendMessage(msg);
			}
		}
	};

	@Override
	public int closeDevice() {
		try 
		{
			debug("�ر���������");
			if (clientConnectThread != null) {
				clientConnectThread.interrupt();
				clientConnectThread = null;
			}
			if (btIs != null) {
				btIs.close();
				btIs = null;
			}
			if (btOs != null) {
				btOs.close();
				btOs = null;
			}
			if (btSocket != null) {
				btSocket.close();
				btSocket = null;
			}
			isConnected = false;
			return 0;
		} catch (IOException e) {
			debug(e.getMessage());
		}
		
		return ResultCode.BLUETOOTH_SOCKET_CLOSE_FAIL;
	}
	
	/**
	 * �������ݴ���(���ͺͽ���) - ���жϳ�ʱ 
	 * 
	 * @param byReq     ���ͱ�������
	 * @param byReqLen  ���ͱ��ĳ���
	 * @param byRes     ��Ӧ��������
	 * @param timeout   ��ʱʱ��
	 * @return
	 */
	@Override
	public int transfer(byte[] byReq, int byReqLen, byte[] byRes, int timeout) {
		if (btSocket == null || btIs == null || btOs == null || byReq == null || byRes == null) {
			return ResultCode.BLUETOOTH_TRANSFER_PARAM_ERR;
		}
		
		try {
			
			//��������ǰ�����ͨ���ŵ�,3�볬ʱ
			if(btIs.available()>0){
				debug("btIs has datas");
				clearData(2);
			}

			// ��������ǰ�ж��û��Ƿ�ȡ������
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}
			
			//��������
			btOs.write(byReq, 0, byReqLen);
			debug("BT send: " + byReqLen);
			
			// ��������ͷǰ�ж��û��Ƿ�ȡ������
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}
			
			//����ͷ����
			byte [] szHead = new byte[PKG_HEAD_SIZE];
			int nReadLen = readMessage(szHead, PKG_HEAD_SIZE, null, timeout);
			
			// �ж�ͷ���ݳ���
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}else if (nReadLen != PKG_HEAD_SIZE)
			{
				debug("recv msg head length error");
				return nReadLen;
			}
			System.arraycopy(szHead, 0, byRes, 0, PKG_HEAD_SIZE);
			
			//��ͷ����ȡ���ݳ���
			int nDataLen = MessageHandler.parseResMessageHead(szHead);
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}else if (nDataLen <= 0) {
				return nReadLen;
			}
			
			//�������ݲ���
			byte[] szRes = new byte[nDataLen];
			nReadLen = readMessage(szRes, nDataLen, null, timeout);
			debug("recv msg data length is:" + nReadLen);
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}else if (nReadLen <= 0)
			{
				return nReadLen;
			}
			
			System.arraycopy(szRes, 0, byRes, PKG_HEAD_SIZE, nReadLen);
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}
			return nReadLen;
		} catch (IOException e) {
			e.printStackTrace();
			debug("transfer �����쳣��"+e.getMessage());
			isConnected = false;
			return ResultCode.BLUETOOTH_TRANSFER_EXCEPTION;
		}
	}
	
	/**
	* @Title: readMessage
	* @Description: ��������
	* @param buf�����ջ�����
	* @param len����������С
	* @param condition����������
	* @param timeout����ʱ
	* @return >=0:�յ������ݳ��ȣ�<0��������
	* @throws
	*/ 
	private int readMessage(byte[] buf, int len, Condition condition, int timeout)
	{
		long startTime = System.currentTimeMillis();
		int nHasRead = 0;
		int nCurRead = 0;
		byte[] byBuf = null;
		
		debug("need read message len: " + len);
		if (len <= PKG_HEAD_SIZE) {
			byBuf = new byte[len];
			try {
				while (nHasRead < len) 
				{
					// �û�ȡ������
					if(Thread.currentThread().isInterrupted()){
						return ResultCode.DEVICE_CANCELED;
					}
					
					//��ʱ
					if ((System.currentTimeMillis() - startTime) > (timeout + 1) * 1000)
					{
						debug("time out");
						return ResultCode.BLUETOOTH_READ_TIMEOUT;
					}
					
					if (btIs.available() > 0){
						nCurRead = btIs.read(byBuf);
						if (nCurRead == -1) {
							continue;
						}
						System.arraycopy(byBuf, 0, buf, nHasRead, nCurRead);
						nHasRead += nCurRead;
						debug("cur read len: " + nCurRead);
						debug("has read total len:" + nHasRead);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();					
				debug("readMessage�����쳣: " + e.getMessage());
				isConnected = false;
				return ResultCode.BLUETOOTH_READ_EXCEPTION;
			}
		} else if (len > PKG_HEAD_SIZE && len < 1024) {
			byBuf = new byte[len];
			try {
				while (nHasRead < len) 
				{
					// �û�ȡ������
					if(Thread.currentThread().isInterrupted()){
						return ResultCode.DEVICE_CANCELED;
					}
					
					if (btIs.available() > 0){
						nCurRead = btIs.read(byBuf);
						if (nCurRead == -1) {
							continue;
						}

						System.arraycopy(byBuf, 0, buf, nHasRead, nCurRead);
						nHasRead += nCurRead;
						debug("cur read len: " + nCurRead);
						debug("has read total len:" + nHasRead);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();					
				debug("readMessage�����쳣: " + e.getMessage());
				isConnected = false;
				return ResultCode.BLUETOOTH_READ_EXCEPTION;
			}
		} else {
			byBuf = new byte[1024];
			while (nHasRead < len)
			{
				// �û�ȡ������
				if(Thread.currentThread().isInterrupted()){
					return ResultCode.DEVICE_CANCELED;
				}
				
				//��ʱ
				if ((System.currentTimeMillis() - startTime) > (timeout + 1) * 1000)
				{
					debug("time out");
					return ResultCode.BLUETOOTH_READ_TIMEOUT;
				}
				
				//������
				try {
					if (btIs.available() > 0){
						nCurRead = btIs.read(byBuf);
						if (nCurRead == -1) {
							continue;
						}
						
						System.arraycopy(byBuf, 0, buf, nHasRead, nCurRead);
						nHasRead += nCurRead;
						debug("cur read len: " + nCurRead);
						debug("has read total len:" + nHasRead);
					}
				} catch (IOException e) {
					e.printStackTrace();
					debug("readMessage�����쳣: " + e.getMessage());
					isConnected = false;
					return ResultCode.BLUETOOTH_READ_EXCEPTION;
				}
			}
		}

		return nHasRead;
	}
	
	private int clearData(int timeout){
		if(btIs == null){
			debug("btIs is null");
			return -1;
		}
		
		long startTime = System.currentTimeMillis();
		byte[] byBuf = new byte[1024];
		int readlen = 0;
		
		try {
			while( readlen >= 0 ){
				if(btIs.available() > 0){
					readlen = btIs.read(byBuf);
				}
				else if ((System.currentTimeMillis() - startTime) > (timeout + 1) * 1000){
					debug("time out");
					return ResultCode.BLUETOOTH_READ_TIMEOUT;
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			debug("IOException: " + e.getMessage());
			return ResultCode.BLUETOOTH_READ_EXCEPTION;
		}
		
		debug("cleared ok");
		return 1;
	}
		
}
