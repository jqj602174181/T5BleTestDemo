package com.centerm.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class StringUtil {
	public StringUtil(){
		
	}
	
	/**
	 * 十六进制形式输出byte[]
	 * 1->2
	 * @param b
	 */
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null)
		{
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			String hex = Integer.toHexString(bytes[i] & 0xff);
			if (hex.length() == 1)
			{
				hex = '0' + hex;
			}
			builder.append(hex.toUpperCase());
		}
		return builder.toString();
	}
	
	/**
	 * 转变十六进制形式的byte[]
	 * 2->1
	 * @param orign
	 * @return
	 */
	public static byte[] hexStringToBytes(String orign){
		int length = orign.length()/2;
		byte[] result = new byte[length];
		for(int i=0; i<length; i++){
			result[i] = (byte) Integer.parseInt(orign.substring(i*2, i*2+2),16);
		}
		return result;
	}
	
	public static String ByteToString(byte[] byBuf,int start,int length)
	{
		String strBuf = new String(byBuf, start, length); 
		return strBuf;
	}
	
	public static String ByteToString(byte[] byBuf)
	{
		int len = GetVailArrayLen(byBuf);
		String strBuf = new String(byBuf, 0, len); 
		return strBuf;
	}
	
	public static String ByteToString(byte[] byBuf, String charset)
	{
		int len = GetVailArrayLen(byBuf);
		String strBuf = null;
		try {
			strBuf = new String(byBuf, 0, len, charset);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return strBuf;
	}
	
	public static int GetVailArrayLen(byte[] byBuf)
	{
		for(int i=0; i<byBuf.length; i++)
		{
			if(byBuf[i] == 0)
				return i;
		}
		return byBuf.length;
	}
	
	// 1->2
	public static String HexToStringA(byte[] s)
	{
		String str="";
		int len = GetVailArrayLen(s);
		for (int i=0;i<len;i++)
		{
			int ch = (int)s[i];
			String s4 = Integer.toHexString(ch);
			if(s4.length() == 1)
			{
				s4 = "0" + s4;
			}
			else if(s4.length() > 2)
			{
				s4 = s4.substring(s4.length()-2, s4.length());
			}
			
			str = str + s4;
		}
		
		return str;
	} 
	
	// 2->1
	public static byte[] StringToHexA(String s)
	{
		byte[] baKeyword = new byte[s.length()/2];
		for(int i = 0; i < baKeyword.length; i++)
		{
			try
			{
				baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			s = new String(baKeyword, "utf-8");//UTF-16le:Not
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		//return s;
		return baKeyword;
	} 
	
   /* byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
	* @param src byte数组  
	* @param offset 从数组的第offset位开始  
	* @return int数值  
	*/    
	public static int bytesToInt(byte[] src, int offset) {  
		int value;    
		value = (int) ((src[offset] & 0xFF)   
			| ((src[offset+1] & 0xFF)<<8)   
			| ((src[offset+2] & 0xFF)<<16)   
			| ((src[offset+3] & 0xFF)<<24));  
		return value;  
	}
	
	/* byte数组中取int数值，本方法适用于(高位在前，低位在后)的顺序
	* @param src byte数组  
	* @param offset 从数组的第offset位开始  
	* @return int数值  
	*/    
	public static byte[] intToBytes(int number) {  
		return ByteBuffer.allocate(4).putInt(number).array();
	}
	/**
	 * 将byte数组转换为int
	 * @param byteArray	byte数组
	 * @return	int数值
	 */
	public static int byteArrayToLength(byte[] byteArray)
	{
		int result = 0;
		for ( int i = 0; i < byteArray.length; i++ )
		{
			result = (byteArray[i] & 0x000000FF) ^ result;
			if ( i < byteArray.length - 1 )
			{
				result <<= 8;
			}
		}
		
		return result;
	}
	
	public static boolean isInt(String str)
	{
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 将日期YYYYMMDD转成YYYY-MM-DD格式
	 * @param date	YYYYMMDD格式日期
	 * @return
	 */
	public static String formatDate1(String date){
		String d = "";
		int len = date.length();
		if(len != 8){
			d = "长期";
		}else{
			d = date.substring(0, 4)
					+ "-" + date.substring(4, 6)
					+ "-" + date.substring(6);
		}
		
		return d;
	}
	
	/**
	 * 将日期YYYYMMDD转成YYYY年MM月DD日格式
	 * @param date	YYYYMMDD格式日期
	 * @return
	 */
	public static String formatDate2(String date){
		String d = "";
		int len = date.length();
		if(len != 8){
			d = "长期";
		}else{
			d = date.substring(0, 4) + "年"
					+ date.substring(4, 6) + "月"
					+ date.substring(6) + "日";
		}
		
		return d;
	}
}
