package com.centerm.message;

import android.util.Log;

import com.centerm.util.Formater;
import com.centerm.util.MessageDefUtil;
import com.centerm.util.RedundancyUtil;

public class MessageHandler {
	private static final int MSG_HEAD_LEN = 8;//报文头
	private static final int MSG_TYPE_LEN = 2;//报文类型
	private static final int MSG_SIZE_LEN = 4;//报文长
	private static final int MSG_CHECK_LEN = 1;//报文校验位

	/**
	 * 构造请求报文
	 * 报文格式：
	 *   报文头（8字节：CT，不足在末尾补0x20）
	 * + 报文长度（4字节）
	 * + 报文类型（2字节）
	 * + 报文体
	 * + 校验位（1字节-MAC校验值）
	 */
	public static byte[] createReqMessage(int ctype, byte[] message)
	{
		if (message == null) {
			return null;
		}

		//报文头
		byte[] head = new byte[MSG_HEAD_LEN];
		for (int i = 0; i < MSG_HEAD_LEN; i++) {
			head[i] = 0x20;
		}
		head[0] = 'C';
		head[1] = 'T';

		//报文类型
		byte[] type = getType(ctype);
		if (type == null) {
			return null;
		}

		//报文长度
		byte[] size = Formater.IntegerToByteArray(message.length + MSG_TYPE_LEN + MSG_CHECK_LEN);

		//报文体
		byte[] tmp = new byte[MSG_HEAD_LEN + MSG_SIZE_LEN+ MSG_TYPE_LEN + message.length];

		try 
		{
			System.arraycopy(head, 0, tmp, 0, head.length);
			System.arraycopy(size, 0, tmp, head.length, size.length);
			System.arraycopy(type, 0, tmp, head.length+size.length, type.length);
			System.arraycopy(message, 0, tmp, head.length+size.length+type.length, message.length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		byte[] resPKG = new byte[MSG_HEAD_LEN + MSG_SIZE_LEN+ MSG_TYPE_LEN + message.length + MSG_CHECK_LEN];
		//校验码
		resPKG[resPKG.length - 1] = RedundancyUtil.RedundancyCheck(tmp);
		System.arraycopy(tmp, 0, resPKG, 0, tmp.length);

		Log.i("createReqMessage", "req message len:"+ resPKG.length);
		return resPKG;
	}

	/**
	 * 解析响应报文：报文头+ 报文长度（4字节）
	 * 报文格式：
	 *   报文头（8字节：CT，不足在末尾补0x20）
	 * + 报文长度（4字节）
	 * + 报文类型（2字节）
	 * + 报文体
	 * + 校验位（1字节-MAC校验值）
	 */
	public static int parseResMessageHead(byte[] head)
	{
		if (head == null || head.length <= 0) {
			return -1;
		}

		//判断报文头部是否合法
		if (head[0] != 'C' || head[1] != 'T') {
			return -1;
		}

		//获取报文长度，判断长度是否合法
		byte[] messSize = new byte[4];
		System.arraycopy(head, MSG_HEAD_LEN, messSize, 0, 4);
		int len = Formater.byteArrayToInteger(messSize, 0, 4);
		Log.i("parseResMessageHead ", "res mess len:" + len);
		if (len <= 3) {
			return -1;
		}

		return len;
	}


	/**
	 * 解析响应报文
	 * 报文格式：
	 *   报文头（8字节：CT，不足在末尾补0x20）
	 * + 报文长度（4字节）
	 * + 报文类型（2字节）
	 * + 报文体
	 * + 校验位（1字节-MAC校验值）
	 */
	public static byte[] parseResMessage(byte[] byRes, byte[] retCode)
	{
		if (byRes == null || byRes.length <= 0) {
			return null;
		}

		//判断报文头部是否合法
		if (byRes[0] != 'C' || byRes[1] != 'T') {
			return null;
		}

		//获取报文长度，判断长度是否合法
		byte[] messSize = new byte[4];
		System.arraycopy(byRes, MSG_HEAD_LEN, messSize, 0, 4);
		int len = Formater.byteArrayToInteger(messSize, 0, 4);
		Log.e("parseResMessage", "total res mess len:" + len);//XXX
		if (len <= 3) {
			return null;
		}

		//判断报文类型是否正确
		byte[] messType = new byte[2];
		System.arraycopy(byRes, MSG_HEAD_LEN + MSG_SIZE_LEN, messType, 0, 2);
		//XXX  判断报文类型是否正确
		retCode[0] = messType[1];

		//获取报文体
		byte[] mess = new byte[len - MSG_TYPE_LEN - MSG_CHECK_LEN];
		System.arraycopy(byRes, MSG_HEAD_LEN + MSG_SIZE_LEN + MSG_TYPE_LEN, 
				mess, 0, mess.length);

		return mess;
	}

	/* 根据外设类型获取发送报文类型 */
	private static byte[] getType(int deviceType)
	{
		if (deviceType < 0) {
			return null;
		}

		byte[] resMesType = new byte[2];

		switch (deviceType) {
		case MessageDefUtil.readPin:
			resMesType[0] = 0x10;
			resMesType[1] = 0x01;
			break;
		case MessageDefUtil.keyAffusePin:
			resMesType[0] = 0x10;
			resMesType[1] = 0x02;
			break;
		case MessageDefUtil.keyFlagPin:
			resMesType[0] = 0x10;
			resMesType[1] = 0x03;
			break;	
		case MessageDefUtil.newKeyPair:
			resMesType[0] = 0x10;
			resMesType[1] = 0x04;
			break;	
		case MessageDefUtil.decodeKey:
			resMesType[0] = 0x10;
			resMesType[1] = 0x05;
			break;	
		case MessageDefUtil.registerFinger:
			resMesType[0] = 0x20;
			resMesType[1] = 0x01;
			break;
		case MessageDefUtil.readFinger:
			resMesType[0] = 0x20;
			resMesType[1] = 0x02;
			break;

		case MessageDefUtil.getIDCardInfo:
			resMesType[0] = 0x30;
			resMesType[1] = 0x01;
			break;
		case MessageDefUtil.getIDFullInfo:
			resMesType[0] = 0x30;
			resMesType[1] = 0x02;
			break;

		case MessageDefUtil.getBookAcct:
			resMesType[0] = 0x40;
			resMesType[1] = 0x01;
			break;	

		case MessageDefUtil.getSignature:
			resMesType[0] = 0x50;
			resMesType[1] = 0x01;
			break;

		case MessageDefUtil.keyAffuseSign:
			resMesType[0] = 0x50;
			resMesType[1] = 0x02;
			break;	

		case MessageDefUtil.getEncrySignature:
			resMesType[0] = 0x50;
			resMesType[1] = 0x03;
			break;	

		case MessageDefUtil.getSignPhotoData:
			resMesType[0] = 0x50;
			resMesType[1] = 0x04;
			break;		

		case MessageDefUtil.getICCardInfo:
			resMesType[0] = 0x60;
			resMesType[1] = 0x01;
			break;
		case MessageDefUtil.genARQC:
			resMesType[0] = 0x60;
			resMesType[1] = 0x02;
			break;
		case MessageDefUtil.ARPC_ExeICScript:
			resMesType[0] = 0x60;
			resMesType[1] = 0x03;
			break;
		case MessageDefUtil.getTxDetail:
			resMesType[0] = 0x60;
			resMesType[1] = 0x04;
			break;
		case MessageDefUtil.getCardRdWrtCap:
			resMesType[0] = 0x60;
			resMesType[1] = 0x05;
			break;	
		case MessageDefUtil.getICInfoAndARQC:
			resMesType[0] = 0x60;
			resMesType[1] = 0x06;
			break;

		case MessageDefUtil.getICAndMSCardStatus:
			resMesType[0] = 0x60;
			resMesType[1] = 0x07;
			break;
		case MessageDefUtil.getAllCardInfo:
			resMesType[0] = 0x60;
			resMesType[1] = 0x08;
			break;
		case MessageDefUtil.saveSm2_1:
			resMesType[0] = 0x10;
			resMesType[1] = 0x11;
			break;
		case MessageDefUtil.saveSm2_2:
			resMesType[0] = 0x10;
			resMesType[1] = 0x12;
			break;
		default:
			return null;
		}

		return resMesType;
	}

}
