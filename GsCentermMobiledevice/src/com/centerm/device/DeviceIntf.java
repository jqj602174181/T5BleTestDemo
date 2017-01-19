package com.centerm.device;

public interface DeviceIntf {

	/**
	 * 打开设备,建立与设备的连接
	 * @return 0－>Success； 状态码－> Fail
	 */
	public int openDevice();
	
	/**
	 * 关闭与设备的连接
	 * 
	 * @return 0－>Success； 状态码－> Fail
	 */
	public int closeDevice();
	
	
	/**
	 * 获取与设备的连接状态
	 * 
	 * @return true－>连接成功； false－> 连接失败
	 */
	public boolean isDeviceConnect();

	/**
	 * 数据传输(发送和接收) - 需判断超时
	 * 
	 * @param byReq     发送报文内容
	 * @param byReqLen  发送报文长度
	 * @param byRes     响应报文内容
	 * @param timeout   超时时间
	 * @return
	 */
	public int transfer(byte[] byReq, int byReqLen, byte[] byRes, int timeout);
	
}
