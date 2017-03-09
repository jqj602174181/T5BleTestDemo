package com.centerm.intf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.print.sdk.PrinterConstants.Command;
import com.android.print.sdk.PrinterInstance;
import com.centerm.device.ResultCode;
import com.centerm.device.TransControl;
import com.centerm.device.bluetooth.Device_BT;
import com.centerm.message.MessageHandler;
import com.centerm.util.MessageDefUtil;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.TimeOutUtil;

/**
 * 1、所有数据都已json字符串格式返回
   2、返回数据中添加MSG字段 标示成功失败信息
   3、返回数据中添加status字段  标示请求成功失败  0 失败 1成功
   4、所有接口都添加监听事件  数据通过监听返回

 * @author chenling
 */
public class CilpInterface {
	public static final int CRAD_SEARCH_IC = 100;//获取IC卡信息
	public static final int CRAD_SEARCH_MS = 101;//获取磁卡信息
	public static final int IDCARD = 102;	//获取二代证信息
	public static final int FINFERGET = 103;//获取指纹仪特征     
	public static final int PINPAD = 104;   //获取密码键盘
	public static final int DECODEPINPAD = 105;   //获取解密明文密码
	public static final int SM4KEY = 106;   	//灌入密文
	public static final int CRAD_IC_ARQC = 107;	//获取IC卡卡号、二磁道信息和五五域，以及序列号
	public static final int CRAD_IC_SERIALNO = 108; //获取IC卡卡序列号
	public static final int ALL_CARD_INFO = 109; //三卡合一
	public static final int SIGN = 110; //电子签名

	private static final String REQUEST_SUCCESS = "1";
	private static final String REQUEST_ERROR = "0";

	private static final int PRINTER_FOUND = 0;
	private static final int PRINTER_NOT_FOUND = 1;

	private static String GET_ARQC_INPUT_STRING =
			"P012000000000000Q012000000000000R003156S00820110324T00233U006165235W012310280000001";

	private String encryptPassword = "";

	private String account = "";//用户账号

	private String tag ="ICAndMS";
	private int ret = 0;

	private Object lockObje = new Object();


	private static String TAG = CilpInterface.class.getSimpleName();
	private static CilpInterface intf = null;	
	private CilpRetrunListener listener = null;		// 监听是否返回信息
	private boolean isWorking = false;				// 是否在工作，防止一个操作未做完，也未取消就做另一个操作
	private GetInfoThread getInfoThread = null;

	@SuppressWarnings("unused")
	private static Application application = null;		
	private static Handler handler = null;			// 用于处理蓝牙连接状态

	private static PrinterInstance printer = null; 
	private static boolean ICAndMS = false;

	private CilpInterface(){
		//设为私有方法，保证此类不能被实例化
	}

	/**
	 * 单例化
	 * @return
	 */
	public static CilpInterface getCilpInterface(Application app,Handler hand)
	{
		application = app;
		handler = hand;
		if (intf == null) {
			intf = new CilpInterface();
		}
		return intf;
	}

	/**
	 * 连接蓝牙
	 * @param mac	蓝牙地址
	 * @return		是否成功，返回0表示成功，其余为不成功
	 */
	public int connectBluetooth(String mac) {
		if(printer != null) {
			printer.closeConnection();
		}
		Message msg = new Message();

		if((handler == null) || (mac == null)){
			Log.e("CilpInterface", "handler or macAddr is null");
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "参数为null";
			handler.sendMessage(msg);
			return ResultCode.DEVICE_PARAM_ERROR;
		}

		msg.what = Device_BT.MSG_CONNING;
		msg.obj = "连接中";
		handler.sendMessage(msg);

		int ret = TransControl.getInstance().openDevice(TransControl.BT_TYPE, mac, handler);

		if(ret == ResultCode.DEVICE_PARAM_ERROR){
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "参数错误";
			handler.sendMessage(msg);
		}else if((ret == ResultCode.BLUETOOTH_MAC_NULL) || (ret == ResultCode.BLUETOOTH_MAC_ERROR)){
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "蓝牙mac错误";
			handler.sendMessage(msg);
		}else if(ret == ResultCode.DEVICE_OPEN_FAILED){
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "蓝牙设备打开失败";
			handler.sendMessage(msg);
		}else if(ret == ResultCode.BLUETOOTH_ADAPTER_NOT_FIND){
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "没有找到本机蓝牙设备";
			handler.sendMessage(msg);
		}else if(ret == ResultCode.BLUETOOTH_DEVICE_NOT_FIND){
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "没有找到对应的蓝牙设备";
			handler.sendMessage(msg);
		}else if(ret != 0){					// 连接成功反馈在Device_BT中实现
			msg.what = Device_BT.MSG_CANT;
			msg.obj = "蓝牙连接不成功";
			handler.sendMessage(msg);
		}

		return ret;
	}

	//获取交易明细信息
	public void setTxDetail( String data)
	{

		GET_ARQC_INPUT_STRING = new String(data);
		GET_ARQC_INPUT_STRING =  GET_ARQC_INPUT_STRING+"Q012000000000000R003156S00820110324T00233U006165235W012310280000001";

		//Log.e("getTxDetail", "GET_ARQC_INPUT_STRING= " + GET_ARQC_INPUT_STRING);
	}



	/**
	 * 蓝牙是否连接成功
	 * @return true-成功		false-不成功
	 */
	public boolean isBTConnect(){
		return TransControl.getInstance().isDeviceConnect();
	}

	/**
	 * 取消操作
	 */
	public void cancleClip()
	{
		if(getInfoThread == null){
			isWorking = false;
			listener = null;
			return;
		}

		if (getInfoThread.isAlive()) {
			getInfoThread.interrupt();
			getInfoThread = null;
		} else {
			getInfoThread = null;
		}
		isWorking = false;
		listener = null;
	}

	/**
	 * 获取IC卡相关信息
	 * @param icCardListener 监听器
	 */
	public void getICCardInfo(CilpRetrunListener icCardListener) {
		if (icCardListener  == null) {
			Log.e("getICCardInfo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getICCardInfo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = icCardListener;
		start(CRAD_SEARCH_IC);
	}

	public void getAllCardInfo(CilpRetrunListener icCardListener) {  //三卡合一
		if (icCardListener  == null) {
			Log.e("getAllCardInfo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getAllCardInfo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = icCardListener;
		start(ALL_CARD_INFO);
	}

	/**
	 * 磁条卡相关
	 * @param msgCardListener 监听器
	 */
	public void getMSCardInfo(CilpRetrunListener msgCardListener) {
		if (msgCardListener  == null) {
			Log.e("getMSCardInfo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getMSCardInfo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = msgCardListener;
		start(CRAD_SEARCH_MS);
	}


	/**
	 * 获取IC卡卡号、二磁道信息和五五域
	 * @param icCardListener 监听器
	 */
	public void getICCAndMSardInfo(CilpRetrunListener icCardListener) {
		if (icCardListener  == null) {
			Log.e("getICCAndMSardInfo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getICCAndMSardInfo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = icCardListener;
		start(CRAD_IC_ARQC);
	}

	/**
	 * 获取IC卡序列号
	 * @param icCardListener 监听器
	 */
	public void getICCardSerialNo(CilpRetrunListener icCardListener) {
		if (icCardListener  == null) {
			Log.e("getICCardSerialNo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getICCardSerialNo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = icCardListener;
		start(CRAD_IC_SERIALNO);
	}

	/**
	 * 获取身份证相关信息
	 * @param idCardListener 监听器
	 */
	public void getIDcardInfo(CilpRetrunListener idCardListener) {
		if (idCardListener  == null) {
			Log.e("getIDcardInfo", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getIDcardInfo", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = idCardListener;

		start(IDCARD);
	}

	/**
	 * 获取指纹特征码
	 * @param fingerListener 监听器
	 */
	public void getFingerCode(CilpRetrunListener fingerListener) {
		if (fingerListener  == null) {
			Log.e("getFingerCode", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getFingerCode", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = fingerListener;

		start(FINFERGET);
	}

	public String saveSm2_1(String sm2_1){
		int ctype = MessageDefUtil.saveSm2_1;
		//构造请求报文
		Map<String, String> msgMap = new HashMap<String, String>();
		msgMap.put("keys", sm2_1);
		JSONObject object = new JSONObject(msgMap);
		try {
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));
			//发送请求报文
			byte[] byRes = new byte[256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.e("keyFlagPin", "transfer fail ret : " + readLen);
				return null;
			} else {
				//解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					return null;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					return recvObj.getString("saveSm2");
				} else if (retCode[0] == 0x02) {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String saveSm2_2(String sm2_2){
		int ctype = MessageDefUtil.saveSm2_2;
		//构造请求报文
		Map<String, String> msgMap = new HashMap<String, String>();
		msgMap.put("keys", sm2_2);
		JSONObject object = new JSONObject(msgMap);
		try {
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));
			//发送请求报文
			byte[] byRes = new byte[256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.e("keyFlagPin", "transfer fail ret : " + readLen);
				return null;
			} else {
				//解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					return null;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					return recvObj.getString("saveSm2");
				} else if (retCode[0] == 0x02) {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取加密sm4密文的sm2秘钥对
	 * @return	sm2的x+y
	 */
	public String NewKeyPair(){

		int ctype = MessageDefUtil.newKeyPair;

		//构造请求报文
		Map<String, String> msgMap = new HashMap<String, String>();
		msgMap.put("timeout", String.valueOf(TimeOutUtil.getInstance().toString()));
		JSONObject object = new JSONObject(msgMap);
		try {
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));
			//发送请求报文
			byte[] byRes = new byte[256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.e("keyFlagPin", "transfer fail ret : " + readLen);
				return null;
			} else {
				//解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					return null;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					return recvObj.getString("sm2Key");
				} else if (retCode[0] == 0x02) {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 灌入sm4秘钥
	 * @param sm4	加密后的sm4
	 * @return		true-成功   false-失败
	 */
	public boolean UpdateSM4Key(String sm4){
		int ctype = MessageDefUtil.keyAffusePin;
		//构造请求报文
		Map<String, String> msgMap = new HashMap<String, String>();
		msgMap.put("key", sm4);
		try {
			JSONObject object = new JSONObject(msgMap);
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.e("keyAffusePin", "transfer fail ret : " + readLen);
				return false;
			} else {
				//解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					return false;
				}
				if (retCode[0] == 0x01) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;		
	}

	/*/**
	 * 获取RSA加密后的密码或未加密的明文
	 *
	 * @param pinpadListener 监听器
	 */
	/*public void getRSAPwd(CilpRetrunListener pinpadListener) {
		if (pinpadListener  == null) {
			Log.e("getRSAPwd", "wrong param");
			return;
		}

		if (isWorking || this.listener != null) {
			Log.e("getRSAPwd", "isworking:" + String.valueOf(isWorking));
			return;
		}

		this.listener = pinpadListener;
		start(PINPAD);
	}*/



	/**
	 * 获取RSA加密后的密码或未加密的明文
	 *@param accout ----用户账号
	 *@param data -----预留参数
	 * @param pinpadListener 监听器
	 */
	public void getRSAPwd(String account, String data, CilpRetrunListener pinpadListener) {
		this.account = account;
		if (pinpadListener  == null) {
			Log.e("getRSAPwd", "wrong param");
			return;
		}

		if (isWorking || this.listener != null) {
			Log.e("getRSAPwd", "isworking:" + String.valueOf(isWorking));
			return;
		}

		this.listener = pinpadListener;
		start(PINPAD);
	}


	public void getDecodePassword(String encryptPassword, CilpRetrunListener pinpadListener) {
		this.encryptPassword = encryptPassword;
		if (pinpadListener  == null) {
			Log.e("getRSAPwd", "wrong param");
			return;
		}

		if (isWorking || this.listener != null) {
			Log.e("getRSAPwd", "isworking:" + String.valueOf(isWorking));
			return;
		}

		this.listener = pinpadListener;
		start(DECODEPINPAD);
	}

	//初始化打印机
	public int initPrinter(Context context,Handler mHandler) {
		/*if(TransControl.getInstance().isDeviceConnect()) {
			TransControl.getInstance().closeDevice();
			Log.e(TAG,"close devicesconnect");
		}*/

		//	HashMap hashMap = new HashMap<String,PrinterInstance>();
		String TAG = "print";
		//删除之前的对象
		if(printer != null ){
			Log.i(TAG, "initPrinter : printer != null  OK!");
			printer.closeConnection();
			printer = null;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{

		}
		BluetoothAdapter btAda = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> devices = btAda.getBondedDevices();
		Log.i(TAG, "devices.size() = " + String.valueOf(devices.size()));
		if(devices.size()>0){
			for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){
				BluetoothDevice device = (BluetoothDevice)it.next();
				Log.i(TAG, "devices.getName() = " + device.getName());
				if(device.getName().substring(0, 2).equals("MP")){	// 区别T5与别的厂家设备				
					printer =  new PrinterInstance(context, device, mHandler);
					printer.openConnection();
					Log.e(tag,"find");
					//循环连接T7
					int nConnectTimeOut = ((int) TimeOutUtil.mTimeOut) * 1000 ;
					for(int nTime = 0; nTime < nConnectTimeOut; nTime += 1000){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						//判断是否处于连接状态
						if(printer.isConnected()){
							Log.i(TAG, "printer.isConnected()  OK!");
							printer.init();
							return PRINTER_FOUND;
						}

					}
					Log.e(TAG,"printer.isConnected()  fail!");
					return PRINTER_NOT_FOUND;
				}
			}
			return PRINTER_NOT_FOUND;
		}

		return PRINTER_NOT_FOUND;
	}

	//打印普通文本
	public int printText(String textToBePrinted) {
		if( null == printer) {
			return PRINTER_NOT_FOUND;
		} else {
			int iRet = printer.printText(textToBePrinted);
			Log.i(TAG, "printText iRet = " + String.valueOf(iRet));
			return iRet;
		}
	}

	//设置打印位置之类的信息
	public int setPrinter(int command,int value) {
		if( null == printer) {
			return PRINTER_NOT_FOUND;
		} else {
			boolean bRet = printer.setPrinter(command, value);
			Log.i(TAG, "setPrinter iRet = " + String.valueOf(bRet));
			return bRet ? 0 : -1;
		}
	}

	//设置字符大小
	public int setCharacterMultiple(int x,int y) {
		if( null == printer) {
			return PRINTER_NOT_FOUND;
		} else {
			printer.setCharacterMultiple(x, y);
			return 0;
		}
	}


	/*public int printSpecText( final String SpecText )
	{
		ret = 0;
		new Thread(){
			public void run()
			{
				super.run();
				ret = printSpec(SpecText);
				synchronized (lockObje) {
					lockObje.notify();
				}

			}
		}.start();
		synchronized (lockObje) {
			try {
				lockObje.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}*/


	//打印指定信息
	public int printSpecText( String SpecText, int timeout )
	{
		int bRet = 0;
		if( null == printer)
		{
			return PRINTER_NOT_FOUND;

		}

		if(SpecText == null)
		{
			return -1;
		}


		String temp = new String(SpecText.replace("|", "\n"));

		String str1 = new String();
		String str2 = new String();
		int index = 0;
		index = temp.indexOf("#");
		if(index > 0)
		{
			str1 = temp.substring(0, index);
			str2 = temp.substring(index+1, temp.length());

		}

		//	Log.e("printer", "str1="+str1);

		bRet = printer.printText(str1);
		Log.e("printer", "bRet ="+bRet);
		if(bRet < 0)
		{
			return bRet;
		}							

		try {
			Thread.sleep(timeout*1000+3000);
			//	Log.e("printer","sleep");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//	str2 = "\n"+ str2; 
		bRet = printer.printText(str2);
		Log.e("printer", "bRet222 ="+bRet);
		//	Log.e("printer", "str2="+str2);

		return bRet;
	}
	
	public void getSignPhotoData(CilpRetrunListener idCardListener) {
		if (idCardListener  == null) {
			Log.e("getSignPhotoData", "wrong param");
			return;
		}
		if (isWorking || this.listener != null) {
			Log.e("getSignPhotoData", "isworking:" + String.valueOf(isWorking));
			return;
		}
		this.listener = idCardListener;

		start(SIGN);
	}

	/**
	 * 发送请求报文，获取外设返回数据
	 * 
	 * @param type 需要读取的外设类型
	 * @return
	 * true -- 获取成功  false--获取失败
	 */
	private void start(int type){
		//判断监听器是否注册
		if (this.listener == null) {
			Log.e(TAG, "监听器没有注册");
			return;
		}

		Log.i(TAG, "type:"+ String.valueOf(type));

		getInfoThread = new GetInfoThread(type);
		getInfoThread.start();
	}

	/**
	 * 返回错误信息
	 * @param retListener	返回信息的监听器
	 * @param errorMsg		错误信息
	 */
	private void setFailReturn(CilpRetrunListener retListener, String errorMsg)
	{
		try 
		{
			JSONObject failObj = new JSONObject();
			failObj.put("status", REQUEST_ERROR);
			failObj.put("MSG", errorMsg);
			retListener.onFailure(failObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



	/**
	 * 针对IC卡的接收和请求
	 **/
	private int transferForICCard ( byte[] byReq, int byReqLen, byte[] byRes, int timeout)
	{

		int readLen = ResultCode.BLUETOOTH_READ_TIMEOUT;


		long starttime = System.currentTimeMillis();   


		while(true)
		{
			byte[] Res = new byte[1024*256];

			readLen = TransControl.getInstance().transfer(byReq, byReqLen, Res,timeout);
			Log.e("ic", "readLen = " + readLen );

			if( readLen > 0 )
			{
				System.arraycopy(Res, 0, byRes, 0, byRes.length);
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(Res, retCode);
				if( mess != null &&  retCode[0] == 0x01 )//一旦成功则返回
				{

					return readLen;
				}
			}
			else
			{

				return readLen;
			}


			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( (System.currentTimeMillis()- starttime ) >= timeout*1000 )
			{

				readLen = ResultCode.BLUETOOTH_READ_TIMEOUT;
				break;
			}


		}


		return  readLen;

	}

	/**
	 * 请求获取IC卡信息
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetIcCardInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("aryTagList", "A");
			object.put("strAIDList", "");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			JSONObject successObj = new JSONObject();
			byte[] byRes = new byte[1024*256];
			int readLen = 0;
			if(ICAndMS)
			{
				readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			else
			{
				readLen = transferForICCard(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}

			if (readLen <= 0) {
				Log.i("sendAndGetIcCardInfo", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					Log.e(tag, "cardinfo=" + recvObj.toString());
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("cardNo_IC", recvObj.getString("data"));	// 卡号
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
	}

	/**
	 * 请求获取磁卡信息
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetMsgCardInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));
			Log.e("MSg","timeout=" + TimeOutUtil.mTimeOut );
			Log.e("MSg", "byReq=" + byReq.toString());
			Log.e("MSg", "byReq=" + StringUtil.bytesToHexString(byReq));
			//发送请求报文
			byte[] byRes = new byte[1024*256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("rdcard", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				} 
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				Log.e("MSg", "byRes=" + StringUtil.bytesToHexString(byRes));
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					JSONObject successObj = new JSONObject();
					successObj.put("status", REQUEST_SUCCESS);
					String track2 = recvObj.getString("data");
					successObj.put("cardNo_MS", recvObj.getString("track3"));	// 卡号
					successObj.put("track2_MS", track2);						// 二磁道
					successObj.put("track3_MS", recvObj.getString("track3"));	// 三磁道
					if (track2.indexOf(">") == -1 && track2.indexOf("=") == -1) {
						successObj.put("cardNo_MS", "");
					} else {
						int index = track2.indexOf("=") > 0 ? track2.indexOf("=") : track2.indexOf(">");
						successObj.put("cardNo_MS", track2.substring(0, index));
					}
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} else if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isWorking = false;
	}




	/**
	 * 请求获取IC卡卡号、五五域,二磁道数据,IC卡序列号
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetICCAndMSardInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		JSONObject ICSNObj = new JSONObject();//做为获取IC卡序列号的的返回值
		String cardSerialNo = new String();//用于保存IC卡序列号
		String tag = "sendAndGetICCAndMSardInfo";

		ICSNObj = ( JSONObject )IcCardSerialNo();

		try {
			if(ICSNObj.getString("status").equals("1"))
			{
				cardSerialNo = ICSNObj.getString("cardSerialNo_IC");
				Log.e( tag, cardSerialNo );
			}
			else
			{
				retListener.onFailure(ICSNObj);
				isWorking = false;
				Log.e(tag, ICSNObj.toString());
				return;
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("aryTagList", "AE");
			object.put("strInput", GET_ARQC_INPUT_STRING);
			object.put("strAIDList", "");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			JSONObject successObj = new JSONObject();
			byte[] byRes = new byte[1024*256];
			int readLen = 0;
			if(ICAndMS)
			{
				readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			else
			{
				readLen = transferForICCard(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			if (readLen <= 0) {
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					successObj.put("status", REQUEST_SUCCESS);
					// 收到的数据格式为:A019xxxxxxxxxxxxxxxxxxxE025xxxxxxxxxxxxxxxxxxxxxxxxx
					// A表示卡号信息 019表示卡号有19位，B表示二磁道信息 025表示信息长度  xxxx……表示对应的信息
					String[] cardInfo = recvObj.getString("icCardData").split("E");
					successObj.put("cardNo_IC", cardInfo[0].substring(4));	// 卡号
					successObj.put("track2_MS", cardInfo[1].substring(3));				// 二磁道信息
					successObj.put("ICChipData", recvObj.getString("ARQCData"));				// 五五域
					successObj.put("cardSerialNo_IC", cardSerialNo.trim());
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
	}

	/**
	 * 返回错误信息的json
	 * @param errorMsg 错误信息
	 * @return object
	 */

	private Object setFailMsg( String errorMsg )
	{

		JSONObject failObj = new JSONObject();
		try {
			failObj.put("status", REQUEST_ERROR);
			failObj.put("MSG", errorMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return failObj;
	}



	/**
	 * 检测磁卡以及IC卡的状态
	 * */
	private Object ICAndMSGcardStatus()
	{
		//isWorking = true;
		int  ctype = MessageDefUtil.getICAndMSCardStatus;
		JSONObject resultObj = new JSONObject();//做为返回结果
		try {

			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			Log.e(tag, "strTimeout =" +String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文

			byte[] byRes = new byte[1024*256];

			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("ICAndMSGcardStatus", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					resultObj =(JSONObject) setFailMsg( RetUtil.User_Cancel);
				}
				else {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				Log.e(tag, "byRes=" + byRes);
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Recv_Error_Mess_Msg);
					return resultObj;

				} 

				/*当成功时返回信息解析*/
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj.put("status", REQUEST_SUCCESS);
					resultObj.put("type", recvObj.getString("type"));
					//	Log.e(tag, "type="+ recvObj.getString("type") );
					//Log.e(tag, "recvObj="+ recvObj);
					if( resultObj.getString("type").equals("3"))//此时已经获取了磁卡数据
					{

						String[] MSGcard = recvObj.getString("data").split("\\|");

						int index = 0;
						int MSGcardLen = MSGcard.length;
						//	Log.e(tag, "MSGcardLen="+ MSGcardLen );

						//获取卡号
						if(MSGcardLen > 0 )
						{
							index = MSGcard[0].indexOf("=");
						}

						String cardNo = "";
						if( index > 0)
						{
							cardNo = MSGcard[0].substring(0, index);
						}



						//Log.e(tag, "MScard =" + recvObj.getString("data") );
						resultObj.put("cardNo_MS", cardNo);	// 卡号
						if( MSGcardLen > 0)
						{
							resultObj.put("track2_MS", MSGcard[0]);	// 二磁道
							//	Log.e(tag,"MSCard[0]" + MSGcard[0] );

						}

						if(MSGcardLen > 1)
						{
							resultObj.put("track3_MS", MSGcard[1]);	// 三磁道
							//Log.e(tag,"MSCard[1]" + MSGcard[1] );

						}
					}
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj =(JSONObject) setFailMsg(  recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultObj;
		//isWorking = false;
	}


	/**
	 *有IC卡则读IC卡，有磁卡则读磁卡
	 * */

	private void ICCAndMSardInfo(CilpRetrunListener retListener )
	{
		isWorking = true;
		ICAndMS = true;
		int flag = 0;//判断当前在位的卡信息
		JSONObject ICAndMSCardObj = new JSONObject();//磁卡以及IC卡的状态
		Log.e("strIput", "GET_ARQC_INPUT_STRING=" +GET_ARQC_INPUT_STRING); 
		ICAndMSCardObj = (JSONObject) ICAndMSGcardStatus();
		try {
			if(ICAndMSCardObj.getString("status").equals("1"))//获取IC卡或者磁道状态成功
			{


				if(ICAndMSCardObj.getString("type").equals("3"))//成功获取磁卡信息立马返回
				{
					retListener.onSuccess(ICAndMSCardObj );
					retListener = null;
					isWorking = false;
					ICAndMS = false;
					return;
				}


			}
			else
			{
				retListener.onFailure(ICAndMSCardObj);//IC卡与磁卡同时不在位
				isWorking = false;
				ICAndMS = false;
				return;

			}

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		//获取IC卡信息

		JSONObject ICInfoObj = new JSONObject();//IC卡信息

		ICInfoObj = (JSONObject) ICCInfoAndARQC();
		try {
			if(ICInfoObj.getString("status").equals("1"))//获取IC卡信息成功则返回
			{
				retListener.onSuccess(ICInfoObj );
				retListener = null;

			}
			else
			{
				retListener.onFailure(ICInfoObj);//获取IC卡信息失败
			}

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		isWorking = false;
		ICAndMS = false;

	}


	/*private void   ICCAndMSardInfo(CilpRetrunListener retListener )
	{
		isWorking = true;

		JSONObject ICInfoObj = new JSONObject();//IC卡信息

		ICInfoObj = (JSONObject) ICCInfoAndARQC();
		try {
			if(ICInfoObj.getString("status").equals("1"))//获取IC卡信息成功则返回
			{
				retListener.onSuccess(ICInfoObj );
				retListener = null;
				isWorking = false;
				return;
			}

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//当获取IC卡信息失败时，则选择读取磁卡信息


		JSONObject MSardInfoObj = new JSONObject();//IC卡信息
		MSardInfoObj = (JSONObject) MsgCardInfo();

		try {
			if( MSardInfoObj.getString("status").equals("1") )
			{
				retListener.onSuccess(MSardInfoObj);
				retListener = null;


			}
			else
			{
				retListener.onFailure(MSardInfoObj);
			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		isWorking = false;

	}*/

	/**
	 * 获取磁卡信息
	 * @return 磁卡信息
	 *
	 */
	private Object MsgCardInfo()
	{
		JSONObject resultObj = new JSONObject();//做为返回结果
		int ctype = MessageDefUtil.getBookAcct;

		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[1024*256];

			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("rdcard", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Device_Connect_Broken_Msg);
				} 
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					resultObj =(JSONObject) setFailMsg(  RetUtil.User_Cancel);
				}
				else {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Recv_Error_Mess_Msg);

					return resultObj;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));

					resultObj.put("status", REQUEST_SUCCESS);
					String track2 = recvObj.getString("data");
					resultObj.put("cardNo_MS", recvObj.getString("track3"));	// 卡号
					resultObj.put("track2_MS", track2);						// 二磁道
					resultObj.put("track3_MS", recvObj.getString("track3"));	// 三磁道
					if (track2.indexOf(">") == -1 && track2.indexOf("=") == -1) {
						resultObj.put("cardNo_MS", "");
					} else {
						int index = track2.indexOf("=") > 0 ? track2.indexOf("=") : track2.indexOf(">");
						resultObj.put("cardNo_MS", track2.substring(0, index));
					}

				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj =(JSONObject) setFailMsg(  recvObj.getString("errormsg"));
				} else if (retCode[0] == 0xFF) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultObj;
	}



	/**
	 * 请求获取IC卡卡号、五五域,二磁道数据,IC卡序列号
	 * @return IC 信息
	 */
	private Object  ICCInfoAndARQC()
	{
		JSONObject resultObj = new JSONObject();//做为返回结果
		int  ctype = MessageDefUtil.getICInfoAndARQC;



		JSONObject ICSNObj = new JSONObject();//做为获取IC卡序列号的的返回值
		String cardSerialNo = new String();//用于保存IC卡序列号

		String tag = "ICCInfoAndARQC";

		ICSNObj = ( JSONObject )IcCardSerialNo();

		try {
			if(ICSNObj.getString("status").equals("1"))
			{
				cardSerialNo = ICSNObj.getString("cardSerialNo_IC");
				Log.e( tag, cardSerialNo );
			}
			else
			{

				Log.e(tag, ICSNObj.toString());
				return  ICSNObj;//获取IC卡序列号失败
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("aryTagList", "AE");
			object.put("strInput", GET_ARQC_INPUT_STRING);
			object.put("strAIDList", "");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[1024*256];
			int readLen = 0;
			if(ICAndMS)
			{
				readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			else
			{
				readLen = transferForICCard(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			if (readLen <= 0) {
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					resultObj =(JSONObject) setFailMsg( RetUtil.User_Cancel);
				}
				else {
					resultObj =(JSONObject) setFailMsg( RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Recv_Error_Mess_Msg);
					return resultObj;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj.put("status", REQUEST_SUCCESS);
					// 收到的数据格式为:A019xxxxxxxxxxxxxxxxxxxE025xxxxxxxxxxxxxxxxxxxxxxxxx
					// A表示卡号信息 019表示卡号有19位，B表示二磁道信息 025表示信息长度  xxxx……表示对应的信息
					String[] cardInfo = recvObj.getString("icCardData").split("E");
					//	Log.e(tag, "carddata="+ recvObj.toString());
					//	Log.e(tag, "cardInfo= "+ recvObj.getString("icCardData") );

					String cardNo = "";
					int index = cardInfo[1].indexOf('=');

					if(index > 3)
					{
						String temp = cardInfo[1].substring(3, index);
						if(temp  != null)
						{

							index = temp.indexOf('F');
							int length = temp.length();
							if(index == length - 1 )
							{
								cardNo = temp.substring(0, index);

							}
							else
							{
								cardNo = temp;
							}
						}

					}
					resultObj.put("cardNo_IC", cardNo);	// 卡号
					resultObj.put("track2_MS", cardInfo[1].substring(3));
					//Log.e(tag, "track2_Ms"+cardInfo[1].substring(3) );
					// 二磁道信息
					resultObj.put("ICChipData", recvObj.getString("ARQCData"));		
					//Log.e(tag, "ICChipData"+recvObj.getString("ARQCData"));// 五五域
					resultObj.put("cardSerialNo_IC", cardSerialNo.trim());

				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj =(JSONObject) setFailMsg( recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultObj;

	}

	/**
	 * 请求获取IC卡序列号
	 * @return 获取信息
	 */
	private Object IcCardSerialNo()
	{
		//isWorking = true;
		int  ctype = MessageDefUtil.getICCardInfo;
		JSONObject resultObj = new JSONObject();//做为返回结果
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("aryTagList", "J");
			object.put("strAIDList", "");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文

			byte[] byRes = new byte[1024*256];
			int readLen = 0;
			if(ICAndMS)
			{
				readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			else
			{
				readLen = transferForICCard(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}

			if (readLen <= 0) {
				Log.i("sendAndGetIcCardSerialNo", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					resultObj =(JSONObject) setFailMsg( RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					resultObj =(JSONObject) setFailMsg( RetUtil.User_Cancel);
				}
				else {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Recv_Error_Mess_Msg);
					return resultObj;

				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj.put("status", REQUEST_SUCCESS);
					resultObj.put("cardSerialNo_IC", recvObj.getString("data").substring(4));	// 卡号

				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					resultObj =(JSONObject) setFailMsg(  recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					resultObj =(JSONObject) setFailMsg(  RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultObj;
		//isWorking = false;
	}


	/**
	 * 请求获取IC卡序列号
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetIcCardSerialNo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iIcFlag", "3");
			object.put("aryTagList", "J");
			object.put("strAIDList", "");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			JSONObject successObj = new JSONObject();
			byte[] byRes = new byte[1024*256];
			int readLen = 0;
			if(ICAndMS)
			{
				readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			else
			{
				readLen = transferForICCard(byReq, byReq.length, byRes,
						(int) TimeOutUtil.mTimeOut);
			}
			if (readLen <= 0) {
				Log.i("sendAndGetIcCardSerialNo", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("cardSerialNo_IC", recvObj.getString("data"));	// 卡号
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
	}



	//发送请求报文，获取二代证
	private void sendAndGetIdCardInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("timeout", String.valueOf(TimeOutUtil.mTimeOut));
			Log.e("IDcard","timeout="+ String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			Log.e("IDcard", "byReq= " +StringUtil.bytesToHexString(byReq));
			//发送请求报文
			byte[] byRes = new byte[1024 * 256];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("idcard", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				Log.e("IDcard", "byRes= " + StringUtil.bytesToHexString(byReq));
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					JSONObject successObj = new JSONObject();
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("IDcardName", recvObj.getString("name"));
					successObj.put("IDcardNo", recvObj.getString("num"));
					successObj.put("IDsex", recvObj.getString("sex"));
					successObj.put("IDnation", recvObj.getString("nation"));
					successObj.put("IDbirth", recvObj.getString("birthday"));
					successObj.put("IDaddr", recvObj.getString("address"));
					successObj.put("IDissuer", recvObj.getString("issue"));
					successObj.put("IDvalidity", recvObj.getString("validstart") + "-" + recvObj.getString("validend"));

					//					sun.misc.BASE64Encoder  base64Encodeer = new sun.misc.BASE64Encoder();
					//					successObj.put("IDPhoto", base64Encodeer.encode(StringUtil.hexStringToBytes(recvObj.getString("photo"))));//XXX照片base64流
					//					successObj.put("IDPhoto", recvObj.getString("photo"));//XXX照片base64流

					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				}else if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isWorking = false;
	}




	/**
	 * 请求获取指纹
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetFingerInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("strCompanyCode", "维尔");
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[1024*2];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("finger", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					JSONObject successObj = new JSONObject();
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("fingerCode", recvObj.getString("featureCode"));
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} else if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isWorking = false;
	}

	/**
	 * 请求获取密码
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetPinInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;


		if( (this.account).length() < 12  || (this.account== null) )
		{
			setFailReturn(retListener, RetUtil.Param_Err_Msg);
			isWorking = false;
			return;
		}

		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("iEncryType", "0");	// 获取普通密码
			object.put("iLength", "6");		// 最小长度
			object.put("iTimes", "1");		// 输入密码次数
			object.put("timeout", String.valueOf(TimeOutUtil.mTimeOut));				
			object.put("account", this.account);

			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[1024];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				// 解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					JSONObject successObj = new JSONObject();
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("keypad", recvObj.getString("firstPIN"));
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
		return;
	}
	/**
	 * 请求获取密码
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetDecodePin(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;

		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("encryptPassword", this.encryptPassword);//加密

			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			byte[] byRes = new byte[1024];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				// 解析响应报文
				byte[] retCode = new byte[1];
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				}
				if (retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					JSONObject successObj = new JSONObject();
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("password", recvObj.getString("password"));
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
		return;
	}
	
	/**
	 * 三卡合一
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetAllCardInfo(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("strTimeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			JSONObject successObj = new JSONObject();
			byte[] byRes = new byte[1024];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("sendAndGetAllCardInfo", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("type", recvObj.getString("type"));	// 卡类型
					successObj.put("data", recvObj.getString("data"));	// 卡号
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
	}

	/**
	 * 三卡合一
	 * @param retListener
	 * @param ctype
	 */
	private void sendAndGetSignPhotoData(CilpRetrunListener retListener, int ctype)
	{
		isWorking = true;
		try {
			//构造请求报文
			JSONObject object = new JSONObject();
			object.put("timeout", String.valueOf(TimeOutUtil.mTimeOut));
			byte[] byReq = MessageHandler.createReqMessage(ctype, object.toString().getBytes("UTF-8"));

			//发送请求报文
			JSONObject successObj = new JSONObject();
			byte[] byRes = new byte[1024*10];
			int readLen = TransControl.getInstance().transfer(byReq, byReq.length, byRes,
					(int) TimeOutUtil.mTimeOut);
			if (readLen <= 0) {
				Log.i("sendAndGetAllCardInfo", "transfer fail ret : " + readLen);
				if (readLen == ResultCode.BLUETOOTH_TRANSFER_EXCEPTION
						|| readLen == ResultCode.BLUETOOTH_READ_EXCEPTION) {
					setFailReturn(retListener, RetUtil.Device_Connect_Broken_Msg);
				}
				else if (readLen == ResultCode.BLUETOOTH_READ_TIMEOUT) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
				else if(readLen == ResultCode.DEVICE_CANCELED){
					setFailReturn(retListener, RetUtil.User_Cancel);
				}
				else {
					setFailReturn(retListener, RetUtil.Send_Mess_Err_Msg);
				}
			} else {
				//解析响应报文
				byte[] retCode = new byte[1]; 
				byte[] mess = MessageHandler.parseResMessage(byRes, retCode);
				if (mess == null) {
					setFailReturn(retListener, RetUtil.Recv_Error_Mess_Msg);
					isWorking = false;
					return;
				} 
				if(retCode[0] == 0x01) {
					JSONObject recvObj = new JSONObject(new String(mess));
					successObj.put("status", REQUEST_SUCCESS);
					successObj.put("data", recvObj.getString("xmlStr"));
					retListener.onSuccess(successObj);
					retListener = null;
				} else if (retCode[0] == 0x02) {
					JSONObject recvObj = new JSONObject(new String(mess));
					setFailReturn(retListener, recvObj.getString("errormsg"));
				} if (retCode[0] == 0xFF) {
					setFailReturn(retListener, RetUtil.Timeout_Err_Msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isWorking = false;
	}

	/**
	 * 数据返回监听器
	 */
	public interface CilpRetrunListener {
		public void onSuccess(JSONObject successData);
		public void onFailure(JSONObject failureData);
	}

	/**
	 * 自定义类：获取信息线程
	 * @author lianwenjin
	 *
	 */
	private class GetInfoThread extends Thread{

		private int infoType;

		public GetInfoThread(int type){
			this.infoType = type;
		}

		@Override
		public void run() {
			if (TransControl.getInstance().isDeviceConnect()) {
				int ctype = 0;
				switch (infoType) {
				case CRAD_SEARCH_IC:
					ctype = MessageDefUtil.getICCardInfo;
					sendAndGetIcCardInfo(listener, ctype);
					break;
				case CRAD_SEARCH_MS:
					ctype = MessageDefUtil.getBookAcct;
					sendAndGetMsgCardInfo(listener, ctype);
					break;
				case CRAD_IC_ARQC:
					ctype = MessageDefUtil.getICInfoAndARQC;
					ICCAndMSardInfo(listener);
					//sendAndGetICCAndMSardInfo(listener, ctype);
					break;
				case CRAD_IC_SERIALNO:
					ctype = MessageDefUtil.getICCardInfo;
					sendAndGetIcCardSerialNo(listener, ctype);
					break;
				case IDCARD:
					ctype = MessageDefUtil.getIDCardInfo;
					sendAndGetIdCardInfo(listener, ctype);
					break;
				case FINFERGET:
					ctype = MessageDefUtil.readFinger;
					sendAndGetFingerInfo(listener, ctype);
					break;		
				case PINPAD:
					ctype = MessageDefUtil.readPin;
					sendAndGetPinInfo(listener, ctype);
					break;
				case DECODEPINPAD:
					ctype = MessageDefUtil.decodeKey;
					sendAndGetDecodePin(listener, ctype);
					break;
				case ALL_CARD_INFO:
					ctype = MessageDefUtil.getAllCardInfo;
					sendAndGetAllCardInfo(listener, ctype);
					break;
				case SIGN:
					ctype = MessageDefUtil.getSignature;
					sendAndGetSignPhotoData(listener, ctype);
					break;
				default:
					setFailReturn(listener, "请求的接口类型不存在");
					listener = null;
					return;
				}
			} else {
				Log.e(TAG, "通讯连接没有建立");
				setFailReturn(listener, RetUtil.Device_Not_Connect_Msg);
				listener = null;
				return;
			}

			listener = null;
		}
	}
}
