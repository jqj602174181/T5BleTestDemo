package com.centerm.keypad.common;

import android.util.Log;

public class Algorithm_S 
{
	//rsa
	public static byte[] rsa_GetKey(byte[] e, int nELen, int nBitLen)
	{
		byte[] bKeyData = new byte[1024];
		int nRet = rsaNewKey(e, nELen, nBitLen, bKeyData);
		Log.i("ret", Integer.toString(nRet));
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bKeyData, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	public static byte[] rsa_PubEncrypt(byte[] n, int nNlen, byte[] e, int nElen, byte[] data, int nDatalen)
	{
		byte[] bEncrypt = new byte[1024];
		int nRet = rsaPubEncrypt(n, nNlen, e, nElen, data, nDatalen, bEncrypt);
		Log.i("ret", Integer.toString(nRet));
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bEncrypt, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	public static byte[] rsa_PubDecrypt(byte[] n, int nNlen, byte[] e, int nElen, byte[] data, int nDatalen)
	{
		byte[] bDecrypt = new byte[1024];
		int nRet = rsaPubDecrypt(n, nNlen, e, nElen, data, nDatalen, bDecrypt);
		Log.i("ret", Integer.toString(nRet));
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bDecrypt, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	public static byte[] rsa_PriEncrypt(byte[] n, int nNlen, byte[] d, int nDlen, byte[] e, int nElen, byte[] data, int nDatalen)
	{
		byte[] bEncrypt = new byte[1024];
		int nRet = rsaPriEncrypt(n, nNlen, d, nDlen, e, nElen, data, nDatalen, bEncrypt);
		Log.i("ret", Integer.toString(nRet));
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bEncrypt, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	public static byte[] rsa_PriDecrypt(byte[] n, int nNlen, byte[] d, int nDlen, byte[] e, int nElen, byte[] data, int nDatalen)
	{
		byte[] bDecrypt = new byte[1024];
		int nRet = rsaPriDecrypt(n, nNlen, d, nDlen, e, nElen, data, nDatalen, bDecrypt);
		Log.i("ret", Integer.toString(nRet));
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bDecrypt, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	//sm2
	public static byte[] sm2_GetKey()
	{
		byte[] bKeyData = new byte[128];
		int nRet = sm2GetKey(bKeyData);
		if (nRet > 0)
		{
			byte[] bKey = new byte[nRet];
			System.arraycopy(bKeyData, 0, bKey, 0, nRet);
			return bKey;
		}
		return null;
	}
	public static byte[] sm2_Encode(byte[] x, byte[] y, byte[] data, int dataLen)
	{
		byte[] bEncode = new byte[256];
		int nRet = sm2Encode(x, y, data, dataLen, bEncode);
		if (nRet >= 0)
		{
			byte[] bEText = new byte[nRet];
			System.arraycopy(bEncode, 0, bEText, 0, nRet);
			return bEText;
		}
		return null;
	}
	public static byte[] sm2_Decode(byte[] prikey, byte[] data, int dataLen)
	{
		byte[] bDecode = new byte[128];
		int nRet = sm2Decode(prikey, data, dataLen, bDecode);
		Log.i("result", Integer.toString(nRet));
		if (nRet >= 0)
		{
			byte[] bText = new byte[nRet];
			System.arraycopy(bDecode, 0, bText, 0, nRet);
			return bText;
		}
		return null;
	}
	public static byte[] sm3_Abt(byte[] data, int dataLen)
	{
		byte[] bAbt = new byte[128];
		int nRet = sm3Abt(data, dataLen, bAbt);
		if (nRet >= 0)
		{
			byte[] bText = new byte[nRet];
			System.arraycopy(bAbt, 0, bText, 0, nRet);
			return bText;
		}
		return null;
	}
	public static byte[] sm3_AbtID(byte[] x, byte[] y, byte[] id, int idLen, byte[] data, int dataLen)
	{
		return null;
	}
	public static byte[] sm4_Encode(byte[] key, int keyLen, byte mode, byte[] data, int dataLen,
			byte[] iv, int ivLen)
	{
		byte[] bEncode = new byte[128];
		int nRet = sm4Encode(key, keyLen, mode, data, dataLen, iv, ivLen, bEncode);
		if (nRet >= 0)
		{
			byte[] bText = new byte[nRet];
			System.arraycopy(bEncode, 0, bText, 0, nRet);
			return bText;
		}
		return null;

	}
	public static byte[] sm4_Decode(byte[] key, int keyLen, byte mode, byte[] data, int dataLen,
			byte[] iv, int ivLen)
	{
		byte[] bDecode = new byte[128];
		int nRet = sm4Decode(key, keyLen, mode, data, dataLen, iv, ivLen, bDecode);
		if (nRet >= 0)
		{
			byte[] bText = new byte[nRet];
			System.arraycopy(bDecode, 0, bText, 0, nRet);
			return bText;
		}
		return null;
	}
	//sm2ªÒ»°√‹‘ø
	private static native int sm2GetKey(byte[] key);
	private static native int sm2Encode(byte[] x, byte[] y, byte[] data, int dataLen, byte[] encode);
	private static native int sm2Decode(byte[] prikey, byte[] data, int dataLen, byte[] decode);
	private static native int sm2Sign(byte[] prikey, byte[] e, byte[] sign);
	private static native int sm2SignCheck(byte[] x, byte[] y, byte[] r, byte[] s, byte[] e);
	private static native int sm2Agt(byte[] ownX, byte[] ownY, byte[] ownPrikey, byte[] ownTempX, byte[] ownTempY, byte[] ownTempPrikey, 
			byte[] otherX, byte[] otherY, byte[] otherTempX, byte[] otherTempY, 
			byte[] ownID, int ownIDLen, byte[] otherID, int otherIDLen, byte role, int kLen, byte[] key);
	private static native int sm3Abt(byte[] data, int dataLen, byte[] abt);
	private static native int sm3AbtID(byte[] x, byte[] y, byte[] id, int idLen, byte[] data, int dataLen, byte[] abt);
	private static native int sm4Encode(byte[] key, int keyLen, byte mode, byte[] data, int dataLen,
			byte[] iv, int ivLen, byte[] encode);
	private static native int sm4Decode(byte[] key, int keyLen, byte mode, byte[] data, int dataLen,
			byte[] iv, int ivLen, byte[] decode);
	private static native int rsaNewKey(byte[] e, int elen, int nBitlen, byte[] key);
	private static native int rsaPubEncrypt(byte[] n, int nNlen, byte[] e, int nElen, byte[] data, int nDatalen, byte[] encrypt);
	private static native int rsaPubDecrypt(byte[] n, int nNlen, byte[] e, int nElen, byte[] data, int nDatalen, byte[] decrypt);
	private static native int rsaPriEncrypt(byte[] n, int nNlen, byte[] d, int nDlen, byte[] e, int nElen, byte[] data, int nDatalen, byte[] encrypt);
	private static native int rsaPriDecrypt(byte[] n, int nNlen, byte[] d, int nDlen, byte[] e, int nElen, byte[] data, int nDatalen, byte[] decrypt);	
	
	static
	{
		try
		{
			System.loadLibrary( "encryption_S" );
		}
		catch(Exception exp)
	    {
	    	Log.i("error", exp.toString());
	    }
	}
	
}
