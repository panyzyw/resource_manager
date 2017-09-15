package com.yongyida.robot.resourcemanager.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.text.format.DateFormat;
import android.util.Log;

public class Contant {
	public static final int QUERY_COMPLETE=0x110;
	public static final int QUERY_START=0x111;
	public static final int QUERY_ONE = 0x115;
	
	public static final int OTG_START_SCANNER=0x116;
	public static final int OTG_STOP_SCANNER=0x113;
	public static final int OTG_REMOVE=0x114;
	public static final int INTERNAL_DATA=0x117;
	public static final int OTG_DATA=0x118;
	public static final int DELETE_ONE=0x119;
	
	//----------------------
	private static final int GB=1024*1024*1024;
	private static final int MB=1024*1024;
	private static final int KB=1024;

	/** true允许程序错误时自动重启，false时方便调试运行停止的情况 */
	public static final boolean allowUnCatchHandler = true;
	
	public static CharSequence formatOfficeTime(long dataAdded) {
		Date date=new Date(dataAdded);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		return df.format(date);
	}
	
	public static CharSequence formatTime(long dataAdded) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.add(Calendar.SECOND, (int) dataAdded);
		CharSequence inFormat = "yyyy-MM-dd";
		return DateFormat.format(inFormat, calendar);
	}

	public static String formatFileLength(long length){
		DecimalFormat format=new DecimalFormat("#.00");
		String fileSize=null;
		if(length<KB){
			fileSize=format.format((double)length)+"B";
		}else if(length<MB){
			fileSize=format.format((double)length/KB)+"K";
		}else if(length<GB){
			fileSize=format.format((double)length/MB)+"M";
		}else{
			fileSize=format.format((double)length/GB)+"G";
		}
		return fileSize;
	}
}
