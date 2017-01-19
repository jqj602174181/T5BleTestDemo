package com.centerm.T5.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.centerm.T5.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class OpenFileDialog {
	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/"; 
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "此目录没有访问权限";
	
	public static Dialog createDialog(Context context, String title, CallbackBundle callback, Map<String, Integer> images){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, callback, images));
		Dialog dialog = builder.create();
		dialog.setTitle(title);
		return dialog;
	}
	
	static class FileSelectView extends ListView implements OnItemClickListener,OnItemLongClickListener{
		private CallbackBundle callback = null;
		private String path = sRoot;
		private List<Map<String, Object>> list = null;
		
		private Map<String, Integer> imagemap = null;
		
		public FileSelectView(Context context, CallbackBundle callback, Map<String, Integer> images) {
			super(context);
			this.imagemap = images;
			this.callback = callback;
			this.setOnItemClickListener(this);
			this.setOnItemLongClickListener(this);
			refreshFileList();
		}
		
		private int getImageId(String s){
			if(imagemap == null){
				return 0;
			}
			else if(imagemap.containsKey(s)){
				return imagemap.get(s);
			}
			else if(imagemap.containsKey(sEmpty)){
				return imagemap.get(sEmpty);
			}
			else {
				return 0;
			}
		}
		
		private int refreshFileList()
		{
			// 刷新文件列表
			File[] files = null;
			try{
				files = new File(path).listFiles();
			}
			catch(Exception e){
				files = null;
			}
			if(files==null){
				// 访问出错
				Toast.makeText(getContext(), sOnErrorMsg,Toast.LENGTH_SHORT).show();
				return -1;
			}
			if(list != null){
				list.clear();
			}
			else{
				list = new ArrayList<Map<String, Object>>(files.length);
			}
			
			// 用来先保存文件夹和文件夹的两个列表
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
			
			if(!this.path.equals(sRoot)){
				// 添加根目录 和 上一层目录
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", sRoot);
				map.put("path", sRoot);
				map.put("img", getImageId(sRoot));
				list.add(map);
				
				map = new HashMap<String, Object>();
				map.put("name", sParent);
				map.put("path", path);
				map.put("img", getImageId(sParent));
				list.add(map);
			}
			
			for(File file: files)
			{
				if(file.isDirectory()){
					// 添加文件夹
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(sFolder));
					lfolders.add(map);
				}
			}
			
			list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
			
			SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
			this.setAdapter(adapter);
			return files.length;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			// 条目选择
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			if(fn.equals(sRoot) || fn.equals(sParent)){
				// 如果是更目录或者上一层
				File fl = new File(pt);
				String ppt = fl.getParent();
				if(ppt != null){
					// 返回上一层
					path = ppt;
				}
				else{
					// 返回根目录
					path = sRoot;
				}
			}
			else{
				File fl = new File(pt);
				if(fl.isDirectory()){
					// 如果是文件夹 那么进入选中的文件夹
					path = pt;
				}
			}
			this.refreshFileList();
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			// 条目选择
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			if(fn.equals(sRoot) || fn.equals(sParent)){
				// 如果是更目录或者上一层
				File fl = new File(pt);
				String ppt = fl.getParent();
				if(ppt != null){
					// 返回上一层
					path = ppt;
				}
				else{
					// 返回根目录
					path = sRoot;
				}
			}
			else{
				File fl = new File(pt);
				if(fl.isDirectory()){
					// 设置回调的返回值
					Bundle bundle = new Bundle();
					bundle.putString("path", pt);
					// 调用事先设置的回调函数
					this.callback.callback(bundle);
					return true;
				}
			}
			return false;
		}
	}
}
