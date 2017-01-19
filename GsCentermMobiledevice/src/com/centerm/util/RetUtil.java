package com.centerm.util;

/**
 * 外设驱动接口错误信息规范
 * 
 * 出现错误时，外设驱动出现应返回明确的错误提示。
 * 
 * 错误提示=错误码+中文错误描述
 * @author chenling
 * 
 */
public class RetUtil {
	/*
	 * 各类驱动都可能出现的错误：DRV+7位数字
	 * 错误编号	           错误描述
	 * DRV0000001	未知错误
	 * DRV0000002	超时错误
	 * DRV0000003	打开串口失败
	 * DRV0000004	向串口发送数据失败
	 * DRV0000005	接口参数错误
	 * DRV0000006	找不到动态链接库
	 * DRV0000007	动态链接库加载错误
	 * DRV0000008	通讯连接没有建立
	 */
	public final static String Unknown_Err = "DRV0000001";
	public final static String Timeout_Err = "DRV0000002";
	public final static String Open_Serial_Err = "DRV0000003";
	public final static String Send_Mess_Err = "DRV0000004";
	public final static String Param_Err = "DRV0000005";
	public final static String Not_Find_So_Err = "DRV0000006";
	public final static String Load_So_Err = "DRV0000007";
	public final static String Device_Not_Connect = "DRV0000008";
	public final static String Recv_Error_Mess = "DRV0000009";
	public final static String Device_Connect_Broken = "DRV0000010";
	
	public final static String Unknown_Err_Msg = "未知错误";
	public final static String Timeout_Err_Msg = "超时错误";
	public final static String Open_Serial_Err_Msg = "打开串口失败";
	public final static String Send_Mess_Err_Msg = "发送或接收报文失败";
	public final static String Param_Err_Msg = "接口参数错误";
	public final static String Not_Find_So_Err_Msg = "找不到动态链接库";
	public final static String Load_So_Err_Msg = "动态链接库加载错误";
	public final static String Device_Not_Connect_Msg = "通讯连接没有建立";
	public final static String Recv_Error_Mess_Msg = "接收的报文格式错误";
	public final static String Device_Connect_Broken_Msg = "通讯连接破坏";
	public final static String User_Cancel = "用户取消操作";
	
	/*
	 * 某类驱动出现的错误：3位设备类型代码+7位数字
	 * 设备类型代码	设备描述
	 * PIN	                         密码键盘
     * ICC	        IC卡键盘
     * IDC	                        二代证（ID卡）
     * FIG			指纹仪
	 * RDC			刷卡器
	 * SIN          电子签名
	 */
	
}
