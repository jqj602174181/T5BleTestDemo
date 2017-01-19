package com.centerm.device;

public interface DeviceIntf {

	/**
	 * ���豸,�������豸������
	 * @return 0��>Success�� ״̬�룭> Fail
	 */
	public int openDevice();
	
	/**
	 * �ر����豸������
	 * 
	 * @return 0��>Success�� ״̬�룭> Fail
	 */
	public int closeDevice();
	
	
	/**
	 * ��ȡ���豸������״̬
	 * 
	 * @return true��>���ӳɹ��� false��> ����ʧ��
	 */
	public boolean isDeviceConnect();

	/**
	 * ���ݴ���(���ͺͽ���) - ���жϳ�ʱ
	 * 
	 * @param byReq     ���ͱ�������
	 * @param byReqLen  ���ͱ��ĳ���
	 * @param byRes     ��Ӧ��������
	 * @param timeout   ��ʱʱ��
	 * @return
	 */
	public int transfer(byte[] byReq, int byReqLen, byte[] byRes, int timeout);
	
}
