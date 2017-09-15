package com.yongyida.robot.resourcemanager.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.adpter.LocalGridViewAdapter;
import com.yongyida.robot.resourcemanager.bean.FilmItemBean;
import com.yongyida.robot.resourcemanager.bean.VideoItem;
import com.yongyida.robot.resourcemanager.utils.ToastUtils;
import com.yongyida.robot.resourcemanager.utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;

public class VideoActivity extends Activity{

	private Button mBtnBack;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
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
			Intent intent = new Intent(this, VideoPlayerActivity.class);
			if (mVidList != null ) {
				if( mVidList.size() > 0){
					intent.putExtra("list", mVidList);
					intent.putExtra("position", 0);
					startActivity(intent);

					//发广播出去,告诉其他的服务停止播,比如语音讲故事,当播放视频的时候,停止讲故事
					Intent stopIntent = new Intent("com.yydrobot.STOP");
					stopIntent.putExtra("from", "YYDRobotVideoPlayer");
					sendBroadcast(stopIntent);

				}
			}
		}
	}
	
	private GridView mGridViewVideoList;
	private LocalGridViewAdapter mAdapter;
	public ArrayList<FilmItemBean> mInternalList = new ArrayList<FilmItemBean>();
	public ArrayList<FilmItemBean> mList = new ArrayList<FilmItemBean>();
	private ArrayList<VideoItem> mVidList=new ArrayList<VideoItem>();

	private View mLoadingLayout;
	private LoadDataFromContent mProvider;
	private TextView mTextViewLoading;

	private static final int QUERY_DATA = 0x120;
	private static final int QUERY_ONE = 0x121;
	private static final int QUERY_COMPLETE=0x122;
	private static final int ITEM_DELETE=0x123;
	private static final int OTG_REMOVE=0x124;
	private static final int INTERNAL_STORAGE=0;
	private static final int OTG_STORAGE=1;
	

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case QUERY_DATA:
				mLoadingLayout.setVisibility(View.VISIBLE);
				mTextViewLoading.setText(""+getResources().getString(R.string.scanning_video));
				mProvider = new LoadDataFromContent(VideoActivity.this);
				mProvider.getFilms();
				break;
			case QUERY_ONE:
				FilmItemBean bean=(FilmItemBean)msg.obj;
				if(msg.arg1==INTERNAL_STORAGE){
					mInternalList.add(bean);
				}
				mList.add(bean);
				VideoItem videoItem=new VideoItem();
				videoItem.setVid(bean.getVid());
				videoItem.setVideoName(bean.getVideoName());
				mVidList.add(videoItem);
				if (mList != null & mList.size() > 0) {
					setGridView();
					
						if(mAdapter==null){
							mAdapter = new LocalGridViewAdapter(VideoActivity.this, mList);
							mGridViewVideoList.setAdapter(mAdapter);
						}else {
							mAdapter.notifyDataSetChanged();
						}
				} 
				break;
			case QUERY_COMPLETE:
				mLoadingLayout.setVisibility(View.GONE);
				if(mList.size()<=0){
						ToastUtils.showToast(VideoActivity.this, ""+getResources().getString(R.string.Not_detect_video));
				}
				offlineAutoPlay();
				break;
			case ITEM_DELETE:
				int position=(Integer) msg.obj;
				removeOtgData(position);
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
					setGridView();
				}
				break;
				
			case OTG_REMOVE:
				removeOtgData();
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
					setGridView();
				}
				mHandler.sendEmptyMessage(QUERY_COMPLETE);
				break;
			}
		}
	};
	
	private BroadcastReceiver receiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				mLoadingLayout.setVisibility(View.VISIBLE);
				mTextViewLoading.setText(""+getResources().getString(R.string.To_scan));
				if(mHandler!=null){
					mHandler.removeCallbacksAndMessages(null);
					mHandler.sendEmptyMessageDelayed(OTG_REMOVE, 2000);
				}
			}
		}
	};
	
	private void removeOtgData() {
		// TODO Auto-generated method stub
		mList.clear();
		mVidList.clear();
		if(mInternalList.size()>0){
			for(FilmItemBean bean:mInternalList){
				mList.add(bean);
				VideoItem videoItem=new VideoItem();
				videoItem.setVid(bean.getVid());
				videoItem.setVideoName(bean.getVideoName());
				mVidList.add(videoItem);
			}
		}
	}
	
	private void removeOtgData(int position) {
		boolean delete=false;
		String path = mList.get(position).getVid();
		File file=new File(path);
		if(file.exists()&file.isFile()){
			VideoUtils.runLinuxCommand("chmod 777 "+path);
				delete= file.delete();
		}
		
		if(delete){
			if(mList!=null){
				mList.remove(position);
			}
			if(mVidList!=null){
				mVidList.remove(position);
			}
		}else{
			ToastUtils.showToast(getApplicationContext(), ""+getResources().getString(R.string.not_remove_permission));
		}
	}	

	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}

	private void initView() {
		mGridViewVideoList = (GridView) findViewById(R.id.gv_film_fragment);
		mLoadingLayout = findViewById(R.id.video_loading_layout);
		mTextViewLoading = (TextView) findViewById(R.id.tv_scanner_loading);
		mBtnBack=(Button) findViewById(R.id.btn_back);
	}

	private void initData() {
		if (mList != null & mList.size() <= 0) {
			mHandler.sendEmptyMessage(QUERY_DATA);
		}
		offlineAutoPlay();
	}

	private void initEvent() {
		IntentFilter filter=new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(receiver, filter);
		
		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				VideoActivity.this.finish();
			}
			
			
		});
		
//		mGridViewVideoList.setSelector(new ColorDrawable(Color.TRANSPARENT)); 
		mGridViewVideoList.setSelector(new ColorDrawable(Color.parseColor("#6698D3ED")));
		mGridViewVideoList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(VideoActivity.this, VideoPlayerActivity.class);
				if (mVidList != null ) {
					if( mVidList.size() > 0){
						intent.putExtra("list", mVidList);
						intent.putExtra("position", position);
						startActivity(intent);

							//发广播出去,告诉其他的服务停止播,比如语音讲故事,当播放视频的时候,停止讲故事
							Intent stopIntent = new Intent("com.yydrobot.STOP");
							stopIntent.putExtra("from", "YYDRobotVideoPlayer");
							sendBroadcast(stopIntent);
						
					}
				}
			}
		});
	
		  //长按删除
		mGridViewVideoList.setOnItemLongClickListener(new OnItemLongClickListener() {
		  @Override 
		  public boolean onItemLongClick(AdapterView<?> parent, View
		   view, final int position, long id) { 
			  // TODO Auto-generated method stub
			  if(null!=VideoActivity.this){
				  new AlertDialog.Builder(VideoActivity.this).setTitle(""+getResources().getString(R.string.dialog_delete))
				  			.setMessage(""+getResources().getString(R.string.dialog_delete_sure))
				  			
				  			.setNegativeButton(""+getResources().getString(R.string.dialog_cancel),  new android.content.DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									mHandler.removeCallbacksAndMessages(null);
								}
							}).setPositiveButton(""+getResources().getString(R.string.dialog_confirm),  new android.content.DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Message msg=Message.obtain();
									msg.obj=position;
									msg.what=ITEM_DELETE;
									mHandler.sendMessage(msg);
								}
							}).show();
			  } 
			  return true; 
		  } 
		});
		
	}

	private void setGridView() {
		int length = 126;
		DisplayMetrics dm = new DisplayMetrics();
		if (VideoActivity.this != null) {
			VideoActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		}
		float density = dm.density;
		int height=(int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210, dm)) ;
		if(density>=1.5){
			length=142;
			height= (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 243, dm));
		}
		int gridviewWidth = (int) (mList.size() * length * density + (mList.size() - 1) * 10 * density);
		int itemWidth = (int) (length * density);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(gridviewWidth, height);
		mGridViewVideoList.setLayoutParams(params);
		mGridViewVideoList.setColumnWidth(itemWidth);
		mGridViewVideoList.setHorizontalSpacing((int) (10 * density));
		mGridViewVideoList.setStretchMode(GridView.NO_STRETCH);
		mGridViewVideoList.setNumColumns(mList.size());
	}

	class LoadDataFromContent {

		ContentResolver mResolver;
		private Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		private String[] projection = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_MODIFIED };
		private String selection = null;
		private String[] selectionArgs = null;
		private String sortOrder = MediaStore.Video.Media._ID;

		public LoadDataFromContent(Context context) {
			mResolver = context.getContentResolver();
		}

		public void getFilms() {
			new Thread() {
				Cursor cursor = null;
				
				@Override
				public void run() {
					cursor = mResolver.query(uri, projection, selection, selectionArgs, sortOrder);
					File file=null;
					while (cursor.moveToNext()) {
						FilmItemBean bean = new FilmItemBean();
						String vid = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
						file =new File(vid);
						if(file.exists()&file.isFile()){
							bean.setVid(vid);
							Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(vid, Images.Thumbnails.MINI_KIND);
							bean.setmBitmap(bitmap);
							bean.setVideoName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
							bean.setVideoDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
							long dataAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
							bean.setVideoPublishTime(VideoUtils.formatVideoAddedTime(dataAdded).toString());
							Message msg=Message.obtain();
							msg.obj=bean;
							if(vid.startsWith("/storage/usbotg/")){
								msg.arg1=OTG_STORAGE;
							}else{
								msg.arg1=INTERNAL_STORAGE;
							}
							msg.what=QUERY_ONE;
							mHandler.sendMessage(msg);
						}
					}
					if (cursor != null) {
						cursor.close();
					}
					mHandler.sendEmptyMessage(QUERY_COMPLETE);
				}
			}.start();
		}
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}
}
