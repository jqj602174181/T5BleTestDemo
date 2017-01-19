package com.centerm.util;


public class TimeOutUtil {
	
	private static TimeOutUtil timeOutUtil = null;
	public static long mTimeOut = 20;
	
	private TimeOutUtil(){
		// 保证单例
	}
	
	/**
	 * 单例化
	 * @return
	 */
	public static TimeOutUtil getInstance()
	{
		if (timeOutUtil == null) {
			timeOutUtil = new TimeOutUtil();
		}
		return timeOutUtil;
	}

	public static void setTimeoutdelay(long timeout){
		if (timeOutUtil == null) {
			timeOutUtil = new TimeOutUtil();
		}
		mTimeOut = timeout / 1000;
	}
	
}
