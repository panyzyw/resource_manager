package com.yongyida.robot.resourcemanager.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.utils.ToastUtils;

public class MainActivity extends Activity implements OnClickListener{
	private Button mBtnBack;
	
	private ImageButton mBtnVideo;
	private ImageButton mBtnMusic;
	private ImageButton mBtnPicture;
	private ImageButton mBtnUnidentifyFile;
	
	private BroadcastReceiver otgReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				ToastUtils.showToast(MainActivity.this, ""+getResources().getString(R.string.u_disk_remove));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter filter=new IntentFilter(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(otgReceiver, filter);
		initView();
		initEvent();
	}

	private void initView() {
		mBtnBack=(Button) findViewById(R.id.btn_back);
		mBtnVideo=(ImageButton) findViewById(R.id.ib_video);
		mBtnMusic=(ImageButton) findViewById(R.id.ib_music);
		mBtnPicture=(ImageButton) findViewById(R.id.ib_picture);
		mBtnUnidentifyFile=(ImageButton) findViewById(R.id.ib_unidentify_file);


	}
	
	private void initEvent(){
		mBtnBack.setOnClickListener(this);
		mBtnVideo.setOnClickListener(this);
		mBtnMusic.setOnClickListener(this);
		mBtnPicture.setOnClickListener(this);
		mBtnUnidentifyFile.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		Intent intent=null;
		int position=-1;
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			return;
		case R.id.ib_video:
			intent =new Intent(this,VideoActivity.class);
			position=0;
			break;
		case R.id.ib_music:	
			intent =new Intent(this,MusicAndImageActivity.class);
			position=1;
			break;
		case R.id.ib_picture:
			intent =new Intent(this,MusicAndImageActivity.class);
			position=2;
			break;
		case R.id.ib_unidentify_file:
			intent =new Intent(this,OtherFileActivity.class);
			position=3;
			break;
		}
		if(intent!=null)
			try {
				intent.putExtra("type", position);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(otgReceiver!=null){
			unregisterReceiver(otgReceiver);
		}
	}
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}
}
