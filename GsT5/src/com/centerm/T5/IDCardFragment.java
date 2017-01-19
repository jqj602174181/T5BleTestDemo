package com.centerm.T5;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.centerm.T5.utils.StringUtil;
import com.centerm.intf.CilpInterface;
import com.centerm.intf.CilpInterface.CilpRetrunListener;

@SuppressLint("NewApi")
public class IDCardFragment extends Fragment {
	private Button readBtn = null;
	private Context context;
	private EditText et_id_address, et_id_birthday, et_id_effectiveday,
	et_id_government, et_id_name, et_id_nation, et_id_num, et_id_sex, et_photo_path;
	private ImageView iv_id_person;
	private static final int GETIDFULLINFO = 1;
	private ProgressDialog pd;
	private Dialog dialog = null;

	private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());	
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GETIDFULLINFO:
				pd.dismiss();
				if (msg.obj != null) {
					try {
						JSONObject result = (JSONObject) msg.obj;
						if (result.getString("status").equals("1")) {
							et_id_name.setText(result.getString("IDcardName"));
							et_id_sex.setText(result.getString("IDsex"));
							et_id_nation.setText(result.getString("IDnation"));
							et_id_birthday.setText(result.getString("IDbirth"));
							et_id_address.setText(result.getString("IDaddr"));
							et_id_num.setText(result.getString("IDcardNo"));
							et_id_government.setText(result.getString("IDissuer"));
							et_id_effectiveday.setText(result.getString("IDvalidity"));
							//							String photo = result.getString("IDPhoto");
							//
							//							Log.i("photo",photo);
							//
							//							//��Ƭbase64������
							//							sun.misc.BASE64Decoder  base64Decodeer = new sun.misc.BASE64Decoder();
							//							String photo1 = StringUtil.bytesToHexString(base64Decodeer.decodeBuffer(photo));
							//
							//							byte[] photoData = StringUtil.hexStringToBytes(photo1);
							//							Bitmap bm = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
							//							iv_id_person.setImageBitmap(bm);
						} else {
							Toast.makeText(context, result.getString("MSG"),
									Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		};
	};

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.id_card, null);
		readBtn = (Button) view.findViewById(R.id.readBtn);
		iv_id_person=(ImageView) view.findViewById(R.id.iv_id_img);
		et_id_address=(EditText) view.findViewById(R.id.et_id_address);
		et_id_birthday=(EditText) view.findViewById(R.id.et_id_birthday);
		et_id_effectiveday=(EditText) view.findViewById(R.id.et_id_effectiveday);
		et_id_government=(EditText) view.findViewById(R.id.et_id_government);
		et_id_name=(EditText) view.findViewById(R.id.et_id_name);
		et_id_nation=(EditText) view.findViewById(R.id.et_id_nation);
		et_id_num=(EditText) view.findViewById(R.id.et_id_num);
		et_id_sex=(EditText) view.findViewById(R.id.et_id_sex);

		//		et_photo_path = (EditText) view.findViewById(R.id.et_photo_path);
		//		et_photo_path.setOnClickListener(new OnClickListener() {
		//			@Override
		//			public void onClick(View arg0) {
		//				Map<String, Integer> images = new HashMap<String, Integer>();
		//				// ���漸�����ø��ļ����͵�ͼ�꣬ ��Ҫ���Ȱ�ͼ����ӵ���Դ�ļ���
		//				images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// ��Ŀ¼ͼ��
		//				images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//������һ���ͼ��
		//				images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//�ļ���ͼ��
		//				images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
		//				dialog = OpenFileDialog.createDialog(context, "ѡ��ͼ��洢·��(*����ȷ��ѡ��)", new CallbackBundle() {
		//					@Override
		//					public void callback(Bundle bundle) {
		//						String filepath = bundle.getString("path");
		//						et_photo_path.setText(filepath);
		//						dialog.dismiss();
		//					}
		//				}, 
		//				images);
		//				dialog.show();
		//			}
		//		});

		readBtn.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				iv_id_person.setBackground(null);
				et_id_address.setText("");
				et_id_birthday.setText("");
				et_id_effectiveday.setText("");
				et_id_government.setText("");
				et_id_name.setText("");
				et_id_nation.setText("");
				et_id_num.setText("");
				et_id_sex.setText("");
				pd.show();
				getIDInfo("5");
			}
		});
		pd = new ProgressDialog(context);
		pd.setMessage("���ڴ������Ժ�...");
		pd.setCanceledOnTouchOutside(false);
		pd.setButton("ȡ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				cilpInterface.cancleClip();
			}
		});

		return view;
	}

	private void getIDInfo(final String strTimeout) {
		//		new Thread() {
		//			public void run() {
		//				CilpInterface cilpInterface = CilpInterface.getInstance();
		//				CilpRetrunListener listener = new CilpRetrunListener() {
		//					public void onSuccess(JSONObject successData) {
		//						Message msg = new Message();
		//						Log.i("T5", "��Ϣ�磺"+successData.toString());
		//						msg.obj = successData;
		//						msg.what = GETIDFULLINFO;
		//						handler.sendMessage(msg);
		//					}
		//					
		//					public void onFailure(JSONObject failureData) {
		//						Log.i("T5", "������Ϣ�磺"+failureData.toString());
		//						Message msg = new Message();
		//						msg.obj = failureData;
		//						msg.what = GETIDFULLINFO;
		//						handler.sendMessage(msg);
		//					}
		//				};
		//				
		//		        cilpInterface.getIDcardInfo(listener);
		//		        cilpInterface.start(CilpInterface.IDCARD);	
		//			};
		//		}.start();

		//		CilpInterface cilpInterface = CilpInterface.getInstance();
		CilpRetrunListener listener = new CilpRetrunListener() {
			public void onSuccess(JSONObject successData) {
				Message msg = new Message();
				Log.i("T5", "��Ϣ�磺"+successData.toString());
				msg.obj = successData;
				msg.what = GETIDFULLINFO;
				handler.sendMessage(msg);
			}

			public void onFailure(JSONObject failureData) {
				Log.i("T5", "������Ϣ�磺"+failureData.toString());
				Message msg = new Message();
				msg.obj = failureData;
				msg.what = GETIDFULLINFO;
				handler.sendMessage(msg);
			}
		};

		cilpInterface.getIDcardInfo(listener);
		//cilpInterface.start(CilpInterface.IDCARD);	

	}
}
