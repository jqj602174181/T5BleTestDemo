package com.centerm.util;

public class MessageDefUtil {

	/* ������������ӿ� */
	public final static int readPin = 1001;
	public final static int keyAffusePin  = 1002;
	public final static int keyFlagPin  = 1003;

	public final static int newKeyPair = 1004;		// ��ȡ����SM4��SM2��Կ
	public final static int decodeKey = 1005;		// ��ȡ����SM4��SM2��Կ

	public final static int saveSm2_1 = 1011;
	public final static int saveSm2_2 = 1012;

	/* ָ���������ӿ� */
	public final static int registerFinger = 2001;
	public final static int readFinger  = 2002;

	/* ����֤�����ӿ� */
	public final static int getIDCardInfo = 3001;
	public final static int getIDFullInfo  = 3002;

	/* �ſ� */
	public final static int getBookAcct = 4001;

	/* ����ǩ�� */
	public final static int getSignature = 5001;
	public final static int keyAffuseSign = 5002;
	public final static int getEncrySignature = 5003;
	public final static int getSignPhotoData = 5004;

	/* IC�� */
	public final static int getICCardInfo = 6001;
	public final static int genARQC  = 6002;
	public final static int ARPC_ExeICScript = 6003;
	public final static int getTxDetail  = 6004;
	public final static int getCardRdWrtCap = 6005;
	public final static int getICInfoAndARQC = 6006;
	public final static int getICAndMSCardStatus = 6007;

	public final static int getAllCardInfo = 6008;
}
