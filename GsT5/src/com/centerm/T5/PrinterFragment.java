package com.centerm.T5;


import com.android.print.sdk.PrinterConstants.Command;
import com.centerm.intf.CilpInterface;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PrinterFragment extends Fragment implements OnClickListener{

	private Context context;
	private Button Print;
	private EditText TextToBePrinted;
	private static final String TAG = "PrinterFragment";
	private int ret = 0;
	private static final String TextTitle = "����ũ��";
	//private static final String TextPrinted = "���Ǳ鲼�����ũ�������磬����ũ�彨���з����ž������ص����á�������ͳ�����ֱ���������ʡ�ų����ϵ�ũҵ�������ũ�������硣";
  private static final String TextPrinted = "|||        ����ʡũ��������||-------------------------------|           �ͻ�ƾ��||-------------------------------|�����㡡��: 06003|�衡������: A000005E088460|��Ա����: 060045|���ڼ�ʱ��: 2016/07/05 10:02:17|������ˮ��: 01080882|ҵ�����͡�: ����ת��|ת�����š�: 621520********1556|ת�뿨�š�: 623065********7790|���׽�: 100.00|�֡�������: 0.00|�ɹ���־�������׳ɹ�|-------------------------------|����ȷ�����Ͻ��ף�ͬ�⽫����뱾�˻���|||#       ����ʡũ��������||-------------------------------|           ��������||-------------------------------|�����㡡��: 06003|�衡������: A000005E088460|��Ա����: 060045|���ڼ�ʱ��: 2016/07/05 10:02:17|������ˮ��: 01080882|ҵ�����͡�: ����ת��|ת�����š�: 621520100060070155|ת�뿨�š�: 6230650000600117790|���׽�: 100.00|�֡�������: 0.00|�ɹ���־�������׳ɹ�|-------------------------------|�ͻ�ǩ����||����ȷ�����Ͻ��ף�ͬ�⽫����뱾�˻���|||";

	private Object lockObje = new Object();
    private CilpInterface cilpInterface = CilpInterface.getCilpInterface(MyApp.getInstance(), new Handler());
	
	
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		context = inflater.getContext();
		View view = (View) inflater.inflate(R.layout.printer, null);
		Print 			= (Button)view.findViewById(R.id.bt_print);
		TextToBePrinted = (EditText)view.findViewById(R.id.et_textToBePrinted);
		Print.setOnClickListener(this);
		
		TextToBePrinted.setText(TextPrinted);
		
		return view;
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.bt_print:
			{
				
				int nRetInit = cilpInterface.initPrinter(context,handler);
				Log.i(TAG,"initPrinter ret : " + String.valueOf(nRetInit));
					
				
				//�ı���ӡ
			final	int nRetSetPrinter = cilpInterface.setPrinter(Command.ALIGN, Command.ALIGN_LEFT);
			final	int nRetSetChar = cilpInterface.setCharacterMultiple(0, 0);
			
	
			int ret = cilpInterface.printSpecText(TextPrinted,5);
		
				break;
			}
		}
		
	}

}
