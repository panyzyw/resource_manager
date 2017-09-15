   package com.yongyida.robot.resourcemanager.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.adpter.ContentAdapter;
import com.yongyida.robot.resourcemanager.bean.ItemBean;
import com.yongyida.robot.resourcemanager.service.LoacalDataHelper;
import com.yongyida.robot.resourcemanager.utils.Contant;
import com.yongyida.robot.resourcemanager.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicAndImageActivity extends Activity{
	
	private Button mBtnBack;
	private GridView mGVContent;
	private TextView mTVContentType;
	private View mLoadingLayout;
	private TextView mTvLoading;
	
	private int mResourceType;
	
	private volatile List<ItemBean> mMusics=new ArrayList<ItemBean>();
	private List<ItemBean> mInternalMusics=new ArrayList<ItemBean>();
	
	private ArrayList<String> mMusicPaths=new ArrayList<String>();
	private ArrayList<String> mMusicNames=new ArrayList<String>();
	
	private  ArrayList<String> mImagePaths=new ArrayList<String>();
	private ContentAdapter mAdapter;
	private LoacalDataHelper mGetMusicDataService;
	
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Contant.QUERY_START:
				mTvLoading.setText(""+getResources().getString(R.string.on_scanner));
				mLoadingLayout.setVisibility(View.VISIBLE);
				mGetMusicDataService=new LoacalDataHelper(MusicAndImageActivity.this, mHandler);
				if(mResourceType==1){
					mGetMusicDataService.getMusicResourceItem();
				}else if(mResourceType==2){
					mGetMusicDataService.getImageResourceItem();
				}
				break;
			case Contant.QUERY_ONE:
				ItemBean bean=(ItemBean) msg.obj;
				if(msg.arg1==Contant.INTERNAL_DATA){
					mInternalMusics.add(bean);
				}
				mMusics.add(bean);
				String uri = bean.getUri();
				mMusicPaths.add(uri);
				mImagePaths.add(uri);
				String name = bean.getName();
				mMusicNames.add(name);
				if(mResourceType==1){
					if(mMusics!=null&mMusics.size()>0){
						
						if(mAdapter==null){
							mAdapter=new ContentAdapter(MusicAndImageActivity.this,mMusics,mResourceType);
							mGVContent.setAdapter(mAdapter);
						}else{
							mAdapter.notifyDataSetChanged();
						}
					}
				}
				break;
			case Contant.QUERY_COMPLETE:
				mLoadingLayout.setVisibility(View.INVISIBLE);
				if(mResourceType==2){
					if(mMusics!=null&mMusics.size()>0){
						
						if(mAdapter==null){
							mAdapter=new ContentAdapter(MusicAndImageActivity.this,mMusics,mResourceType);
							mGVContent.setAdapter(mAdapter);
						}else{
							mAdapter.notifyDataSetChanged();
						}
					}
				}
				if(mMusics.size()<=0){
					if(MusicAndImageActivity.this!=null){
						if(mResourceType==1){
							ToastUtils.showToast(MusicAndImageActivity.this, ""+getResources().getString(R.string.detected_audio));
						}else if(mResourceType==2){
							ToastUtils.showToast(MusicAndImageActivity.this, ""+getResources().getString(R.string.detected_picture));
						}
					}
				}
				offlineAutoPlay();
				break;
			case Contant.OTG_REMOVE:
				removeOtgData();
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}
				mHandler.sendEmptyMessage(Contant.QUERY_COMPLETE);
				break;
			}
		};
	};
	
	private BroadcastReceiver otgReceiver=new BroadcastReceiver() {
			
			@Override
		public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				//重新扫描
				mTvLoading.setText(""+getResources().getString(R.string.To_scan));
				mLoadingLayout.setVisibility(View.VISIBLE);
				mHandler.removeCallbacksAndMessages(null);
				mHandler.sendEmptyMessageDelayed(Contant.OTG_REMOVE, 5000);
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resource_list);
		IntentFilter filter=new IntentFilter(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(otgReceiver, filter);
		initView();
		initData();
		initEvent();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		offlineAutoPlay();
	}

	/**
	 * 离线自动播放
	 */
	private void offlineAutoPlay(){
		if(!TextUtils.isEmpty(getIntent().getStringExtra("offline"))){
			if(mMusicPaths.size()>0 && mMusicNames.size()>0){
				Intent intent = new Intent("com.yyd.artmuseum");
				intent.putExtra("path", mMusicPaths);
				intent.putExtra("name", mMusicNames);
				intent.putExtra("position", 0);
				startActivity(intent);

				//发广播出去,告诉其他的服务停止播,比如语音讲故事,当播放视频的时候,停止讲故事
				Intent stopIntent = new Intent("com.yydrobot.STOP");
				stopIntent.putExtra("from", "YYDRobotVideoPlayer");
				sendBroadcast(stopIntent);
			}
		}
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		mBtnBack=(Button) findViewById(R.id.btn_back);
		mTVContentType=(TextView) findViewById(R.id.tv_content_type);
		mGVContent=(GridView) findViewById(R.id.gv_content_detail);
		mLoadingLayout=findViewById(R.id.loading_layout);
		mTvLoading=(TextView) findViewById(R.id.tv_scanner_loading);
	}
	
	private void initData() {
		mResourceType=getIntent().getIntExtra("type", -1);
		if(mMusics!=null&mMusics.size()<=0){
			mHandler.removeMessages(Contant.QUERY_START);
			mHandler.sendEmptyMessage(Contant.QUERY_START);
		}
		if(mResourceType==1){
			mTVContentType.setText(""+getResources().getString(R.string.title_music));
		}else if(mResourceType==2){
			mTVContentType.setText(""+getResources().getString(R.string.title_picture));
		}
		offlineAutoPlay();
	}

	private void initEvent() {
		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicAndImageActivity.this.finish();
			}
		});
		
		mGVContent.setSelector(new ColorDrawable(Color.parseColor("#6698D3ED")));
		mGVContent.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(mMusicPaths.size()>0&mMusicNames.size()>0){
				if(mResourceType==1){
						try{
							Intent intent =new Intent("com.yyd.artmuseum");
							intent.putExtra("path", mMusicPaths);
							intent.putExtra("name", mMusicNames);
							intent.putExtra("position", position);
							startActivity(intent);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				if(mResourceType==2){
					try{
						Intent intent =new Intent(MusicAndImageActivity.this,ImageShowActivity.class);
						intent.putExtra("imagePosition", position);
						intent.putExtra("imageList", mImagePaths);
						startActivity(intent);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			}
		});
		mGVContent.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
				// TODO Auto-generated method stub
				
				  if(null!=MusicAndImageActivity.this){
					  new AlertDialog.Builder(MusicAndImageActivity.this).setTitle(""+getResources().getString(R.string.dialog_delete))
					  			.setMessage(""+getResources().getString(R.string.dialog_delete_sure))
					  			
					  			.setNegativeButton(""+getResources().getString(R.string.dialog_cancel),  new android.content.DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
									}
								}).setPositiveButton(""+getResources().getString(R.string.dialog_confirm),  new android.content.DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										ItemBean bean= (ItemBean) parent.getItemAtPosition(position);
										deleteOneData(bean);
									}
								}).show();
				  } 
				return true;
			}
		});
	}
	
	protected void deleteOneData(ItemBean bean) {
		// TODO Auto-generated method stub
		boolean delete=false;
		File file=new File(bean.getUri());
		if(file.exists()&file.isFile()){
			delete=file.delete();
		}
		if(delete){
			
			mMusics.remove(bean);
			if(mInternalMusics.contains(bean)){
				mInternalMusics.remove(bean);
			}
			if(mResourceType==1){
				mMusicNames.remove(bean.getName());
				mMusicPaths.remove(bean.getUri());
			}else{
				mImagePaths.remove(bean.getUri());
			}
			if(mAdapter!=null){
				mAdapter.notifyDataSetChanged();
			}
		}else{
			ToastUtils.showToast(getApplicationContext(), ""+getResources().getString(R.string.not_remove_permission));
		}
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		mImagePaths.clear();
		this.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}
	
	private void removeOtgData() {
		mMusics.clear();
		mMusicPaths.clear();
		mMusicNames.clear();
		mImagePaths.clear();
		if(mInternalMusics.size()>0){
			for(ItemBean bean:mInternalMusics){
				mMusics.add(bean);
				String uri = bean.getUri();
				mMusicPaths.add(uri);
				mImagePaths.add(uri);
				String name = bean.getName();
				mMusicNames.add(name);
			}
		}
	}
}
