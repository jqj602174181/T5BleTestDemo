package com.centerm.util;

public class RedundancyUtil 
{
	/**
	 * 计算冗余校验值
	 * @param data	校验数据
	 * @return	校验值
	 */
	public static byte RedundancyCheck(byte[] data)
	{
		byte checkCode = (byte)0x00;
		for (int i = 0; i < data.length; i++)
		{
			checkCode = (byte)(checkCode ^ data[i]);
		}
		
		return checkCode;
	}
}
