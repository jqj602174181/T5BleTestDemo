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
 * 处理 线程被未捕获的异常终止 的情况, 一旦出现了未捕获异常崩溃, 系统就会回调该类的 
 * @author chenling
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";// 用于打印日志的 TAG 标识符  
    private Thread.UncaughtExceptionHandler mDefaultHandler;//系统默认的UncaughtException处理类
    private static CrashHandler instance = new CrashHandler();//CrashHandler实例
    private Context mContext;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    
    private CrashHandler()
    {
    	
    }
    
    /* 获取CrashHandler实例 ,单例模式 */
    public static CrashHandler getInstance()
    {
    	return instance;
    }
    
    /** 
     * 初始化该类, 向系统中注册 
     * @param context 
     */
    public void init(Context cxt)
    {
    	mContext = cxt;
    	mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    	// 设置该 CrashHandler 为程序的默认处理器
    	Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    /** 
     * 出现未捕获的异常时, 会自动回调该方法 
     */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		Log.i(TAG, thread.getName()+"线程  "+"异常信息："+ex.toString());
		
        if (!handleException(ex) && mDefaultHandler != null) {  
            // 如果自定义的没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            try {  
                Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存 
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);  
        }
	}
	
	/** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
     * @param ex     
     *      异常信息 
     * @return  
     *      true:如果处理了该异常信息;否则返回false. 
     */  
    private boolean handleException(Throwable ex) {  
    	if (ex == null) {
    		return false;
    	}
    	 /* 
         * 使用Toast来显示异常信息,由于在主线程会阻塞,  
         * 不能实时出现 Toast 信息,这里我们在子线程中处理 Toast 信息 
         */ 
    	new Thread() {
    		@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
    	}.start();
        // 保存日志文件  
        saveCrashInfo2File(ex);
    	return true;
    }
    
    /** 
     * 保存错误信息到文件中 
     * @param ex 
     * @return 返回文件名称,便于将文件传送到服务器 
     */
    private void  saveCrashInfo2File(Throwable ex)
    {
    	StringBuffer sb = new StringBuffer();
    	
    	//将 StringBuffer sb 中的字符串写出到文件中
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
			//文件输出路径 
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
