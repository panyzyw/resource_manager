package com.yongyida.robot.resourcemanager.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.content.Context;
import android.os.storage.StorageManager;
import android.text.format.DateFormat;
import android.view.WindowManager;

/**
 * 屏幕宽高和时间格式化
 * 
 * @author clyrp
 *
 */
public class VideoUtils {

	private static final int SECONDS_HOUR = 1000 * 60 * 60;

	public static int getScreenWidth(Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return manager.getDefaultDisplay().getWidth();
	}

	public static int getScreenHeight(Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return manager.getDefaultDisplay().getHeight();
	}

	/**
	 * 格式化视频的播放时间，假如时间大于一小时，则格式化为01:30:49，否则格式化为30:49
	 * 
	 * @param duration
	 * @return
	 */
	public static CharSequence formatTime(long duration) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.add(Calendar.MILLISECOND, (int) duration);
		CharSequence inFormat = duration / SECONDS_HOUR > 0 ? "kk:mm:ss" : "mm:ss";
		return DateFormat.format(inFormat, calendar);
	}

	/**
	 * 格式化本地视频的添加年份
	 */
	public static CharSequence formatVideoAddedTime(long dataAdded) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.add(Calendar.SECOND, (int) dataAdded);
		CharSequence inFormat = "yyyy-MM-dd";
		return DateFormat.format(inFormat, calendar);
	}
	
	public static boolean runLinuxCommand(String command){
		Process process=null;
		try {
			process= Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (Exception e) {
		}finally {
			if(process!=null){
				try {
					process.destroy();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		return true;
	}
	
	/**
	 * 判断是否有OTG的盘符
	 * @param context
	 * @return
	 */
	public static boolean readSdcardCount(Context context){
		String[] result = null;
		boolean isOtgInsert=false;
		StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);  
		try {  
		    Method method = StorageManager.class.getMethod("getVolumePaths");  
		    method.setAccessible(true);  
		    try {  
		        result =(String[])method.invoke(storageManager); 
		    } catch (InvocationTargetException e) {  
		    }  
		   if(result.length>2){
			   isOtgInsert=true;
		   }else{
			   isOtgInsert=false;
		   }
		} catch (Exception e) {  
		    e.printStackTrace();  
		}
		return isOtgInsert;
	}
	
	/**
	 * 获取OTG盘符的根目录
	 * @param context
	 * @return
	 */
	public static String getOtgRootPath(Context context){
		String[] result = null;
		String otgPath=null;
		StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);  
		try {  
		    Method method = StorageManager.class.getMethod("getVolumePaths");  
		    method.setAccessible(true);  
		    try {  
		        result =(String[])method.invoke(storageManager); 
		        
		    } catch (InvocationTargetException e) {  
		    }  
		   if(result.length>0){
			   for(int i=0;i<result.length;i++){
				  if(result[i].contains("/storage/usbotg/")){
					  otgPath=result[i];
				  }
			   }
		   }
		} catch (Exception e) {  
		}
		return otgPath;
	}
}
