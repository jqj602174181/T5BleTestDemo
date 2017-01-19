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
		
		//找到蓝牙适配器
		btAda = BluetoothAdapter.getDefaultAdapter();
		if (btAda == null) {
			debug("没找到本机蓝牙设配器");
			return ResultCode.BLUETOOTH_ADAPTER_NOT_FIND;
		} else{
			Set<BluetoothDevice> devices = btAda.getBondedDevices();
			if(devices.size()>0){
				for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){
					BluetoothDevice device = (BluetoothDevice)it.next();
					if(device.getName().substring(0, 2).equals("T5")){	// 区别T5与别的厂家设备
						this.mac = device.getAddress();
						Log.i(TAG ,device.getAddress());
						break;
					}
				}
			} else {
				debug("未找到连接的蓝牙设备");
				return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
			}

		}
		
		if(this.mac == null){
			debug("未找到连接的T5设备");
			return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
		}
		
		//开启蓝牙
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
				debug("开启蓝牙失败或蓝牙已经开启");
				return ResultCode.BLUETOOTH_OPEN_FAIL;
			}
		}
		
		//根据指定MAC进行蓝牙配对
		try {
			btDevice = btAda.getRemoteDevice(mac);
			if (btDevice == null) {
				debug("没找到相应蓝牙设备");
				return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
			} else {
				if (btAda.isDiscovering())// 取消扫描，否则匹配会不稳定
				{
					btAda.cancelDiscovery();
				}
				if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {
					debug("蓝牙开始配对...");
					createBond(btDevice);
				}
			}
			
			clientConnectThread = new clientThread();
			clientConnectThread.start();
			
		} catch (IllegalArgumentException e) {
			debug("查找不到对应MAC地址的设备");
			return ResultCode.BLUETOOTH_DEVICE_NOT_FIND;
		}
		
		return 0;
	}
	
	/* 蓝牙配对 */
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

	/* 开启客户端,连接到服务端 */
	private class clientThread extends Thread { 		
		@Override
		public void run() {
			try {
				/* 创建一个Socket连接：只需要服务器在注册时的UUID号
				 * 系统会在远程设备上完成一个SDP查找来匹配UUID。
				 * 如果查找成功并且远程设备接受连接，就共享RFCOMM信道，connect()会返回。
				 * 这也是一个阻塞的调用，不管连接失败还是超时（12秒）都会抛出异常
				 */

				debug("开始连接...");
				btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				
				//连接蓝牙服务端
				//添加循环，尝试五次超时无法连接后返回
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
					debug("蓝牙连接失败");
				} else {
					if (btSocket != null) {
						btIs = btSocket.getInputStream();
						btOs = btSocket.getOutputStream();
						isConnected = true;
						debug("蓝牙连接成功");
					}
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				debug("蓝牙连接失败"+e.getMessage());
				isConnected = false;
				try {
					if (btSocket != null) {
						btSocket.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}finally{					// 反馈连接结果
				Message msg = new Message();
				if(isConnected){
					msg.what = MSG_CONNED;
					msg.obj = "蓝牙连接成功";
				}else{
					msg.what = MSG_CANT;
					msg.obj = "蓝牙连接失败";
				}
				handler.sendMessage(msg);
			}
		}
	};

	@Override
	public int closeDevice() {
		try 
		{
			debug("关闭蓝牙连接");
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
	 * 蓝牙数据传输(发送和接收) - 需判断超时 
	 * 
	 * @param byReq     发送报文内容
	 * @param byReqLen  发送报文长度
	 * @param byRes     响应报文内容
	 * @param timeout   超时时间
	 * @return
	 */
	@Override
	public int transfer(byte[] byReq, int byReqLen, byte[] byRes, int timeout) {
		if (btSocket == null || btIs == null || btOs == null || byReq == null || byRes == null) {
			return ResultCode.BLUETOOTH_TRANSFER_PARAM_ERR;
		}
		
		try {
			
			//发送数据前先清空通信信道,3秒超时
			if(btIs.available()>0){
				debug("btIs has datas");
				clearData(2);
			}

			// 发送数据前判断用户是否取消操作
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}
			
			//发送数据
			btOs.write(byReq, 0, byReqLen);
			debug("BT send: " + byReqLen);
			
			// 接收数据头前判断用户是否取消操作
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}
			
			//接收头数据
			byte [] szHead = new byte[PKG_HEAD_SIZE];
			int nReadLen = readMessage(szHead, PKG_HEAD_SIZE, null, timeout);
			
			// 判断头数据长度
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}else if (nReadLen != PKG_HEAD_SIZE)
			{
				debug("recv msg head length error");
				return nReadLen;
			}
			System.arraycopy(szHead, 0, byRes, 0, PKG_HEAD_SIZE);
			
			//从头部获取数据长度
			int nDataLen = MessageHandler.parseResMessageHead(szHead);
			if(Thread.currentThread().isInterrupted()){
				return ResultCode.DEVICE_CANCELED;
			}else if (nDataLen <= 0) {
				return nReadLen;
			}
			
			//接收数据部分
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
			debug("transfer 出现异常："+e.getMessage());
			isConnected = false;
			return ResultCode.BLUETOOTH_TRANSFER_EXCEPTION;
		}
	}
	
	/**
	* @Title: readMessage
	* @Description: 接收数据
	* @param buf：接收缓冲区
	* @param len：缓冲区大小
	* @param condition：结束条件
	* @param timeout：超时
	* @return >=0:收到的数据长度；<0：错误码
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
					// 用户取消操作
					if(Thread.currentThread().isInterrupted()){
						return ResultCode.DEVICE_CANCELED;
					}
					
					//超时
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
				debug("readMessage出现异常: " + e.getMessage());
				isConnected = false;
				return ResultCode.BLUETOOTH_READ_EXCEPTION;
			}
		} else if (len > PKG_HEAD_SIZE && len < 1024) {
			byBuf = new byte[len];
			try {
				while (nHasRead < len) 
				{
					// 用户取消操作
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
				debug("readMessage出现异常: " + e.getMessage());
				isConnected = false;
				return ResultCode.BLUETOOTH_READ_EXCEPTION;
			}
		} else {
			byBuf = new byte[1024];
			while (nHasRead < len)
			{
				// 用户取消操作
				if(Thread.currentThread().isInterrupted()){
					return ResultCode.DEVICE_CANCELED;
				}
				
				//超时
				if ((System.currentTimeMillis() - startTime) > (timeout + 1) * 1000)
				{
					debug("time out");
					return ResultCode.BLUETOOTH_READ_TIMEOUT;
				}
				
				//读数据
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
					debug("readMessage出现异常: " + e.getMessage());
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
