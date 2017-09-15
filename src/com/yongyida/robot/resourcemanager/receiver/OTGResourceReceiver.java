package com.yongyida.robot.resourcemanager.receiver;


import com.yongyida.robot.resourcemanager.utils.VideoUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OTGResourceReceiver extends BroadcastReceiver {
	
//	private static final String OTG_REMOVE=Intent.ACTION_MEDIA_REMOVED;
	private static final String OTG_INSERT=Intent.ACTION_MEDIA_MOUNTED;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(OTGResourceCloseReciver.close){
			OTGResourceCloseReciver.close=false;
			return;
		}
		switch (intent.getAction()) {
		case OTG_INSERT:
			boolean otgExists = VideoUtils.readSdcardCount(context);
			if(otgExists){
				Intent i=new Intent("android.intent.yyd.resourcecontrol");
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
			break;
		}
	}
}
