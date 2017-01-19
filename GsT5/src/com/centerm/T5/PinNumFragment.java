package com.centerm.T5;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.centerm.T5.utils.StringUtil;
import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;
import com.centerm.keypad.common.Algorithm_S;

@SuppressLint("NewApi")
public class PinNumFragment extends Fragment implements OnClickListener {
	private Context context;
	private EditText mSm2EditText;
	private EditText mSm4EditText;
	private EditText mPwEditText;
	private EditText mDecPwEditText;	// 解密后的密码
	private EditText mAccountText; 
	private Button mGetSm2Button, mSendSm4Button, mReadPwButton, mDecPwButton;

	private String EnPassword = "";
	private String Account = "";

	private byte[] sm4InitData =null;
	byte[] sm2X ;
	byte[] sm2Y;
	private String SM4Init = "CF68E5D5A8ADB0991F09A2DB2EE923A25085887603176D57E9C67CF734217953F3BEABCDCBCF01EAFE8410FE44C7D7643103C7B61DABB5C86095D7C109BFCE3532027C66C0EF866B447126DFABD7788A838AF4244F259A4EF987397CED070F2DBBBA06FC084586873BC7CB004038C07C";
	private String SM4Key = "";
	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());

	private String SM2Key = "CF68E5D5A8ADB0991F09A2DB2EE923A25085887603176D57E9C67CF734217953" +
			"CF68E5D5A8ADB0991F09A2DB2EE923A25085887603176D57E9C67CF734217953";

	private int passwordLen = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.pin_num, null);

		sm4InitData = new byte[16];
		for(int i=0;i<sm4InitData.length;i++){
			sm4InitData[i]=0x38;
		}
		SM4Init = StringUtil.bytesToHexString(sm4InitData);
		mSm2EditText = (EditText) view.findViewById(R.id.sm2_key);
		mSm4EditText = (EditText) view.findViewById(R.id.sm4_key);
		mAccountText = (EditText) view.findViewById(R.id.account);

		mSm2EditText.setText(SM2Key);

		mAccountText.setText("1234567890123456");
		mSm4EditText.setText("");
		mPwEditText = (EditText) view.findViewById(R.id.et_pw);
		mDecPwEditText = (EditText) view.findViewById(R.id.et_decPin);

		mGetSm2Button = (Button) view.findViewById(R.id.bt_getsm2);
		mSendSm4Button = (Button) view.findViewById(R.id.bt_sendsm4);
		mReadPwButton = (Button) view.findViewById(R.id.bt_readpin);
		mDecPwButton = (Button) view.findViewById(R.id.bt_decPin);

		mGetSm2Button.setOnClickListener(this);
		mSendSm4Button.setOnClickListener(this);
		mReadPwButton.setOnClickListener(this);
		mDecPwButton.setOnClickListener(this);
		sm2X = new byte[32];
		sm2Y = new byte[32];
		return view;
	}

	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (msg.obj != null) {
					try {
						JSONObject result = (JSONObject)msg.obj;
						if (result.getString("status").equals("1")) {
							//							String sLen = result.getString("len");
							//							passwordLen = Integer.parseInt(sLen);
							EnPassword = result.getString("keypad");
							mPwEditText.setText(EnPassword);
						} else {
							mPwEditText.setText(result.getString("MSG"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			case 2:
				if (msg.obj != null) {
					try {
						JSONObject result = (JSONObject)msg.obj;
						if (result.getString("status").equals("1")) {
							String password = result.getString("password");
							byte[] passwordData = StringUtil.hexStringToBytes(password);
							byte[]data = new byte[passwordLen];
							if(passwordLen> passwordData.length){
								System.arraycopy(passwordData, 0, data, 0, passwordData.length);
							}else{
								System.arraycopy(passwordData, 0, data, 0, passwordLen);
							}


							mDecPwEditText.setText(StringUtil.bytesToHexString(passwordData));
						} else {
							mDecPwEditText.setText(result.getString("MSG"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_getsm2:
			String sm2_1 = SM2Key.substring(0, 64);
			String sm2_2 = SM2Key.substring(64, 128);
			String result1 = cilpInterface.saveSm2_1(sm2_1);
			if(result1 != null && result1.equals("success")){
				String result2 = cilpInterface.saveSm2_2(sm2_2);
				if(result2 != null && result2.equals("success")){
					Toast.makeText(context, "注入sm2成功!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(context, "注入sm2失败!", Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(context, "注入sm2失败!", Toast.LENGTH_SHORT).show();
			}

			//			String sm2 = cilpInterface.NewKeyPair();
			//			mSm2EditText.setText(sm2);
			//			if(sm2!=null){
			//				byte[] sm2Data = StringUtil.hexStringToBytes(sm2); 
			//				System.arraycopy(sm2Data, 0, sm2X, 0, 32);
			//				System.arraycopy(sm2Data, 32, sm2Y, 0, 32);
			//				byte[]SM4Data = StringUtil.hexStringToBytes(SM4Init);
			//				SM4Data = Algorithm_S.sm2_Encode(sm2X,sm2Y, SM4Data, SM4Data.length);
			//				SM4Key = StringUtil.bytesToHexString(SM4Data);
			//				mSm4EditText.setText(SM4Key);
			//			}
			break;
		case R.id.bt_sendsm4:
			boolean ret = cilpInterface.UpdateSM4Key(SM4Key);
			Log.i("PinNum",String.valueOf(ret));
			break;
		case R.id.bt_readpin:
			CilpRetrunListener listener = new CilpRetrunListener() {
				public void onSuccess(JSONObject successData) {
					Log.i("PinNum", "json: success "+successData.toString());
					Message msg = new Message();
					msg.what = 1;
					msg.obj = successData;
					handler.sendMessage(msg);
				}

				public void onFailure(JSONObject failureData) {
					Message msg = new Message();
					msg.what = 1;
					msg.obj = failureData;
					handler.sendMessage(msg);
				}
			};
			Account = mAccountText.getText().toString();
			Log.e("account", Account );
			cilpInterface.getRSAPwd(Account, null, listener);
			break;
		case R.id.bt_decPin:
			CilpRetrunListener lis = new CilpRetrunListener() {
				public void onSuccess(JSONObject successData) {

					Log.i("PinNum", "json："+successData.toString());
					Message msg = new Message();
					msg.what = 2;
					msg.obj = successData;
					handler.sendMessage(msg);
				}

				public void onFailure(JSONObject failureData) {
					Message msg = new Message();
					msg.what = 2;
					msg.obj = failureData;
					handler.sendMessage(msg);
				}
			};
			cilpInterface.getDecodePassword(EnPassword, lis);
			break;
		}
	}

	//	//读取密码，未加密
	//	private void getPin() {
	//		new Thread(new Runnable() {
	//			@Override
	//			public void run() {
	////				CilpInterface cilpInterface = CilpInterface.getInstance();

	//				cilpInterface.getRSAPwd("1", "", listener);
	//				//cilpInterface.start(CilpInterface.PINPAD);
	//			}
	//		}).start();
	//	}
	//	
	//	// 读取加密后的数据
	//	private void getEncryPin(String key) {
	//		final String rsaPublicKey = key; 
	//		
	//		new Thread(new Runnable() {
	//			@Override
	//			public void run() {
	////				CilpInterface cilpInterface = CilpInterface.getInstance();
	//				CilpRetrunListener listener = new CilpRetrunListener() {
	//					public void onSuccess(JSONObject successData) {
	//						Message msg = new Message();
	//						msg.obj = successData;
	//						handler.sendMessage(msg);
	//					}
	//					
	//					public void onFailure(JSONObject failureData) {
	//						Message msg = new Message();
	//						msg.obj = failureData;
	//						handler.sendMessage(msg);
	//					}
	//				};
	//				//先获取密钥标识
	//				String keyFlag = cilpInterface.getCurKeyFlag();
	//				if (keyFlag != null && keyFlag.equals("20150923100100")) {
	//					Log.i("keypad", "不用灌注密钥");
	//					//密钥标识和测试用默认密钥标识一致，不用灌注密钥，直接获取RSA加密后的密码
	//			        cilpInterface.getRSAPwd("0", "1234567890123456", listener);
	//			        cilpInterface.start(CilpInterface.PINPAD);
	//				} else {
	//					//标识不一致，则进行密钥灌注
	//					if (cilpInterface.putRSAKey("20150923100100", rsaPublicKey)) {
	//						//获取RSA加密后的密码
	//				        cilpInterface.getRSAPwd("0", "1234567890123456", listener);
	//				        cilpInterface.start(CilpInterface.PINPAD);
	//					} else {
	//						Message msg = new Message();
	//						JSONObject jsonData = new JSONObject();
	//						try {
	//							jsonData.put("status", "0");
	//							jsonData.put("MSG", "密钥灌注失败");
	//						} catch (Exception e) {
	//							e.printStackTrace();
	//						}
	//						msg.obj = jsonData;
	//						handler.sendMessage(msg);
	//					}
	//				}
	//			}
	//		}).start();
	//	}
	//	
	//    //获取组合后的私钥数据
	//    private PrivateKey getPrivateKey(String modulus,String privateExponent) throws Exception 
	//    {    
	//    	//16进制数据转大数   
	//        BigInteger m = new BigInteger(modulus, 16);    
	//        BigInteger e = new BigInteger(privateExponent, 16);    
	//
	//        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m,e);    
	//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");    
	//        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);    
	//
	//        return privateKey;    
	//    }
	//    
	//    //私钥解密
	//    private byte[] decRSA( PrivateKey privateKey, byte[] enctext )
	//    {      
	//        Cipher cipher;
	//		try 
	//		{
	//			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	//			cipher.init(Cipher.DECRYPT_MODE, privateKey);    
	//		    return cipher.doFinal(enctext);    
	//		} catch (Exception e) {
	//		    e.printStackTrace();
	//		} 
	//      
	//         return null;
	//    }
}
