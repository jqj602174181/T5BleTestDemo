package com.centerm.T5.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/** 
 * ���� �̱߳�δ������쳣��ֹ �����, һ��������δ�����쳣����, ϵͳ�ͻ�ص������ 
 * @author chenling
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";// ���ڴ�ӡ��־�� TAG ��ʶ��  
    private Thread.UncaughtExceptionHandler mDefaultHandler;//ϵͳĬ�ϵ�UncaughtException������
    private static CrashHandler instance = new CrashHandler();//CrashHandlerʵ��
    private Context mContext;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    
    private CrashHandler()
    {
    	
    }
    
    /* ��ȡCrashHandlerʵ�� ,����ģʽ */
    public static CrashHandler getInstance()
    {
    	return instance;
    }
    
    /** 
     * ��ʼ������, ��ϵͳ��ע�� 
     * @param context 
     */
    public void init(Context cxt)
    {
    	mContext = cxt;
    	mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    	// ���ø� CrashHandler Ϊ�����Ĭ�ϴ�����
    	Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    /** 
     * ����δ������쳣ʱ, ���Զ��ص��÷��� 
     */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		Log.i(TAG, thread.getName()+"�߳�  "+"�쳣��Ϣ��"+ex.toString());
		
        if (!handleException(ex) && mDefaultHandler != null) {  
            // ����Զ����û�д�������ϵͳĬ�ϵ��쳣������������  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            try {  
                Thread.sleep(3000);// ��������ˣ��ó����������3�����˳�����֤�ļ����� 
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            //�˳�����
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);  
        }
	}
	
	/** 
     * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����. 
     * @param ex     
     *      �쳣��Ϣ 
     * @return  
     *      true:��������˸��쳣��Ϣ;���򷵻�false. 
     */  
    private boolean handleException(Throwable ex) {  
    	if (ex == null) {
    		return false;
    	}
    	 /* 
         * ʹ��Toast����ʾ�쳣��Ϣ,���������̻߳�����,  
         * ����ʵʱ���� Toast ��Ϣ,�������������߳��д��� Toast ��Ϣ 
         */ 
    	new Thread() {
    		@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "�ܱ�Ǹ,��������쳣,�����˳�.", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
    	}.start();
        // ������־�ļ�  
        saveCrashInfo2File(ex);
    	return true;
    }
    
    /** 
     * ���������Ϣ���ļ��� 
     * @param ex 
     * @return �����ļ�����,���ڽ��ļ����͵������� 
     */
    private void  saveCrashInfo2File(Throwable ex)
    {
    	StringBuffer sb = new StringBuffer();
    	
    	//�� StringBuffer sb �е��ַ���д�����ļ���
    	Writer writer = new StringWriter();
    	PrintWriter printWriter = new PrintWriter(writer); 
    	ex.printStackTrace(printWriter);
    	Throwable cause = ex.getCause();
    	while (cause != null) {  
    		cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }
    	printWriter.close();
    	sb.append(writer.toString());
    	
    	try {
			long timestamp = System.currentTimeMillis();
			String time = format.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".txt";
			//�ļ����·�� 
//			String path = "/mnt/sdcard/crashinfo/";
			String path = Environment.getExternalStorageDirectory().getPath() + "/crashinfo/";
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(path + fileName);
			fos.write(sb.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e); 
		}
    }
}
