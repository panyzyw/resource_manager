package com.yongyida.robot.resourcemanager.activity;


import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.utils.Contant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class TranspanrenActivity extends Activity {
	
	private static final String STOP_SCANNER=Intent.ACTION_MEDIA_SCANNER_FINISHED;
	private static final String OTG_REMOVE=Intent.ACTION_MEDIA_BAD_REMOVAL;
	private static final String OTG_INSERT=Intent.ACTION_MEDIA_MOUNTED;
	
	private boolean isOpenByOtg=false;
	
	private ImageView mIvAnimation;
	private AnimationDrawable mAnimationDrawable;
	private BroadcastReceiver otgScannerReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			switch (intent.getAction()) {
			case OTG_INSERT:
			case STOP_SCANNER:
				mHandler.sendEmptyMessage(Contant.OTG_STOP_SCANNER);
				break;
			case OTG_REMOVE:
				mHandler.sendEmptyMessage(Contant.OTG_REMOVE);
				if(otgScannerReceiver!=null){
					if(isOpenByOtg)
						unregisterReceiver(otgScannerReceiver);
				}
				break;
			}
		}
	};
	
	
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Contant.OTG_STOP_SCANNER:
				Intent intent=new Intent(TranspanrenActivity.this,MainActivity.class);
				startActivity(intent);
				TranspanrenActivity.this.finish();
				break;
			case Contant.OTG_REMOVE:
				TranspanrenActivity.this.finish();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String action = getIntent().getAction();
		IntentFilter filter=new IntentFilter();
		filter.addAction(STOP_SCANNER);
		filter.addAction(OTG_REMOVE);
		filter.addAction(OTG_INSERT);
		filter.addDataScheme("file");	
		registerReceiver(otgScannerReceiver, filter);
		if(action.equals("android.intent.yyd.resourcecontrol")){
			setContentView(R.layout.activity_transparent_open_byotg);
			mIvAnimation=(ImageView) findViewById(R.id.iv_otg_scanner);
			mAnimationDrawable=(AnimationDrawable) mIvAnimation.getDrawable();
			mAnimationDrawable.start();
			isOpenByOtg=true;
		}else{
			setContentView(R.layout.activity_transparent);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub 
		super.onResume();
		if(!getIntent().getAction().equals("android.intent.yyd.resourcecontrol")){
			Intent intent=new Intent(this,MainActivity.class);
			startActivity(intent);
			this.overridePendingTransition(R.anim.acvivity_start_anim, 0);
			TranspanrenActivity.this.finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.acvivity_start_anim, 0);
	}
	
}
