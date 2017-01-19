package com.centerm.device;

public class ResultCode {
	// 通信错误码
	public static final int DEVICE_SUCCESS = 0; // 成功
	public static final int DEVICE_OPEN_FAILED = -1; // 打开设备失败
	public static final int DEVICE_WRITEDEVICE_FAILED = -2; // 发送指令失败
	public static final int DEVICE_READDEVICE_FAILED = -3; // 接收数据失败
	public static final int DEVICE_PACKAGE_ERROR = -4; // 接收到的报文格式错误
	public static final int DEVICE_TIMEOUT = -5; // 超时
	public static final int DEVICE_CANCELED = -6; // 用户取消
	public static final int DEVICE_DATA_ERROR = -8; // 数据不正确
	public static final int DEVICE_PARAM_ERROR = -9; // 参数不正确
	public static final int DEVICE_COMMUNICATE = -10; // 通信失败
	public static final int DEVICE_OPERATE_FAILED = -11; // 操作失败
	public static final int DEVICE_OUTOF_BUFFER = -12; // 数据越界

	// USB错误码
	public static final int USB_NO_PERMISSION = -90;
	public static final int USBMANAGER_IS_NULL = -91;
	public static final int DEVICE_LIST_NULL = -92;
	public static final int USB_FIND_DEVICE_FAIL = -93;
	public static final int USB_FIND_INTERFACE_FAIL = -94;
	public static final int USB_CONNECT_FAIL = -95;
	public static final int USB_GET_EPIN_FAIL = -96;
	public static final int USB_GET_EPOUT_FAIL = -97;
	public static final int USB_CLAIMED_FAILED = -98;
	public static final int USB_EPIN_OR_EPOUT_NULL = -99;
	public static final int USB_REQ_MESSSAGE_LEN_ERR = -100;
	public static final int USB_SEND_MESSSAGE_FAIL = -101;
	public static final int USB_READ_TIMEOUT = -102;
	public static final int USB_PRAM_REQ_OR_RES_NULL = -103;
	
	// 蓝牙错误码
	public static final int BLUETOOTH_ADAPTER_NOT_FIND = -200;
	public static final int BLUETOOTH_MAC_NULL = -201;
	public static final int BLUETOOTH_MAC_ERROR = -202;
	public static final int BLUETOOTH_OPEN_FAIL = -203;
	public static final int BLUETOOTH_DEVICE_NOT_FIND = -204;
	public static final int BLUETOOTH_SOCKET_GET_FAIL = -205;
	public static final int BLUETOOTH_SOCKET_CLOSE_FAIL = -206;
	public static final int BLUETOOTH_TRANSFER_PARAM_ERR = -207;
	public static final int BLUETOOTH_READ_TIMEOUT = -208;
	public static final int BLUETOOTH_READ_EXCEPTION = -209;
	public static final int BLUETOOTH_READ_MESS_HEAD_FAIL = -210;
	public static final int BLUETOOTH_READ_MESS_LEN_FAIL = -211;
	public static final int BLUETOOTH_READ_MESS_DATA_FAIL = -212;
	public static final int BLUETOOTH_TRANSFER_EXCEPTION = -213;
}
