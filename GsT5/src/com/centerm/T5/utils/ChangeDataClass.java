package com.centerm.T5.utils;


/**
 * @class: DataHandlerUnit
 * @function: 拆分与合并数据功能模块
 * @author: zxx
 * 
 * @data:2013-02-19
 */
public final class ChangeDataClass 
{
	/**
	 * @name: bytesToHexString
	 * @function: 将一个byte数据拆分转换成字符串,如byte[]{0x2B, 0×44, 0xEF}–> "2B44EF"
	 * 
	 * @param byte[]
	 *            bData ---待处理数据
	 * @return byte[] 成功返回拆分后的数据，失败返回""
	 */

	public static byte[] bytesToHexString(byte[] bData) 
	{
		if (bData == null)
		{
			return null;
		}

        byte[] dest = new byte[2*bData.length];
        int temp = 0;
		for (int i = 0; i < bData.length; i++) 
		{
			temp = (bData[i] & 0xF0) >> 4;
	        if ( temp <= 9 )
	        {
	        	dest[2*i] = (byte)(0x30 + temp);
	        }
	        else
	        {
	        	dest[2*i] = (byte)(0x41 + temp -10);
	        }
	        
	        temp = (bData[i] & 0x0F);
	        if ( temp <= 9 )
	        {
	        	dest[2*i+1] = (byte)(0x30 + temp);
	        }
	        else
	        {
	        	dest[2*i+1] = (byte)(0x41 + temp -10);
	        }
		}
		return dest;
	}

	/**
	 * @name: hexStringToBytes
	 * @function: 将指定byte数据，以每两个byte合并转换为16进制形式 如："2B44EF" –> byte[]{0x2B, 0×44,
	 *            0xEF}
	 * 
	 * @param byte[]
	 *            bData ---待分割的字符串
	 * 
	 * @return byte[] 合并后的数据
	 */
	public static byte[] hexStringToBytes(byte[] bData) 
	{
		if (bData == null || bData.length%2 != 0)
		{
			return null;
		}
		
		int nLen = bData.length;
		int temp = 0;
		int i = 0;
		byte[] bRet = new byte[ nLen / 2];
		byte dest;
		for (i = 0; i < bData.length / 2; i++) 
		{
			temp = (bData[i * 2] & 0xFF);
	        if ( (temp >= 0x30) && (temp <= 0x39) )
	        {
	        	dest = (byte)((bData[i * 2] << 4) & 0xF0);
	        }
	        else if ( (temp >= 0x61) && (temp <= 0x66) )
	        {
	        	dest = (byte)(( bData[i * 2] - 'a' + 10 ) << 4);
	        }
	        else if ( (temp >= 0x41) && (temp <= 0x46) )
	        {
	        	dest = (byte)(( bData[i * 2] - 'A' + 10 ) << 4);
	        }
	        else
	        {
	            break;
	        }
	        
	        temp = (bData[2*i+1] & 0xFF);
	        if ( (temp >= 0x30) && (temp <= 0x39) )
	        {
	        	dest |= (temp & 0x0F);
	        }
	        else if ( (temp >= 0x61) && (temp <= 0x66) )
	        {
	        	dest |= ( temp - 'a' + 10 );
	        }
	        else if ( (temp >= 0x41) && (temp <= 0x46) )
	        {
	        	dest |= ( temp - 'A' + 10 );
	        }
	        else
	        {
	            break;
	        }

			bRet[i] = dest;
		}
		
		if (i != bData.length / 2)
		{
			return null;
		}
		return bRet;
	}
	
	/**
	 * @name: starCombination
	 * @function: 将指定字符串src，以每两个字符合并转换为16进制形式 如：byte[] {0x35, 0x3B, 0x34, 0x35 } 
	 * –> byte[]{0x5B, 0×45}
	 *    	
	 * 
	 * @param byte
	 *            bData ---待合并的byte[]数据
	 * 
	 * @return byte[] 合并后的数据
	 */
	public static byte[] starCombination(byte[] bData) 
	{
		if (bData == null || bData.length%2 != 0)
		{
			return null;
		}
		
		int nLen = bData.length;
		int i = 0;
		int j = 0;
		byte[] bReturnData = new byte[nLen / 2];
		
		for (i = 0; i < nLen; i = i + 2) 
		{
			j = i / 2;
			bReturnData[j] = (byte) (((bData[i] << 4) & 0xF0) | (bData[i + 1] & 0x0F));
		}
		
		return bReturnData;
	}

	/**
	 * @name: starDivide
	 * @function: 将一个byte拆分成两个 如：byte[]{0x2B,0x45} –> byte[]{0x32. 0x3B, 0x34,
	 *            0x45}
	 * 
	 * @param byte[]
	 *            bData ---待分割的字符串数据
	 * 
	 * @return byte[] 拆分后的数据
	 */
	public static byte[] starDivide(byte[] bData) 
	{
		if (bData == null)
		{
			return null;
		}
		
		int nLen = bData.length;
		byte[] bReturnData = new byte[2 * nLen];

		for (int i = 0; i < nLen; i++) 
		{
			bReturnData[2 * i] = (byte) (((bData[i] & 0xF0) >> 4) + 0x30);
			bReturnData[2 * i + 1] = (byte) ((bData[i] & 0x0F) + 0x30);
		}
		
		return bReturnData;
	}
}
