package com.yongyida.robot.resourcemanager.receiver;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OTGResourceCloseReciver extends BroadcastReceiver {
	
	public static boolean close=false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		switch (intent.getAction()) {
		case "com.yydrobot.resource.close":
			close=true;
			break;
		}
	}
}
