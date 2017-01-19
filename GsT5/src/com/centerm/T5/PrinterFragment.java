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
	private static final String TextTitle = "甘肃农信";
	//private static final String TextPrinted = "这是遍布城乡的农村信用社，在新农村建设中发挥着举足轻重的作用。以往的统计数字表明，甘肃省九成以上的农业贷款出自农村信用社。";
  private static final String TextPrinted = "|||        甘肃省农村信用社||-------------------------------|           客户凭条||-------------------------------|网　点　号: 06003|设　备　号: A000005E088460|柜　员　号: 060045|日期及时间: 2016/07/05 10:02:17|核心流水号: 01080882|业务类型　: 行内转账|转出卡号　: 621520********1556|转入卡号　: 623065********7790|交易金额　: 100.00|手　续　费: 0.00|成功标志　：交易成功|-------------------------------|本人确认以上交易，同意将其计入本账户！|||#       甘肃省农村信用社||-------------------------------|           银行留存||-------------------------------|网　点　号: 06003|设　备　号: A000005E088460|柜　员　号: 060045|日期及时间: 2016/07/05 10:02:17|核心流水号: 01080882|业务类型　: 行内转账|转出卡号　: 621520100060070155|转入卡号　: 6230650000600117790|交易金额　: 100.00|手　续　费: 0.00|成功标志　：交易成功|-------------------------------|客户签名：||本人确认以上交易，同意将其计入本账户！|||";

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
					
				
				//文本打印
			final	int nRetSetPrinter = cilpInterface.setPrinter(Command.ALIGN, Command.ALIGN_LEFT);
			final	int nRetSetChar = cilpInterface.setCharacterMultiple(0, 0);
			
	
			int ret = cilpInterface.printSpecText(TextPrinted,5);
		
				break;
			}
		}
		
	}

}
