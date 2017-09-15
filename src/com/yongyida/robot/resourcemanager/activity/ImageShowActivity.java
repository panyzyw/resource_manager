package com.yongyida.robot.resourcemanager.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.view.ZoomImageView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import android.view.ViewGroup;

public class ImageShowActivity extends Activity {
	
	private int mCurrentPosition;
//	private ArrayList<String> mImagePaths;
	
	private ViewPager mVPImageScanner;
	private ImagePagerAdapter mAdapter;
	private  ArrayList<String> mImagePaths=new ArrayList<String>();
	private LruCache<String, Bitmap> mImageCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_show);
		initView();
		initData();
		initEvent();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(otgReceiver!=null){
			unregisterReceiver(otgReceiver);
		}
		if(finishReceiver!=null){
			unregisterReceiver(finishReceiver);
		}
	}

	private void initData() {
		int maxMemory=(int) Runtime.getRuntime().maxMemory();
		int cacheMenmory=maxMemory/8;
		mImageCache=new LruCache<String,Bitmap>(cacheMenmory){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				return value.getRowBytes()*value.getHeight();
			}
		};
				
		mImagePaths=(ArrayList<String>) getIntent().getSerializableExtra("imageList");
		mCurrentPosition=getIntent().getIntExtra("imagePosition", -1);
		if(!new File(mImagePaths.get(mCurrentPosition)).exists()){
			this.finish();
		}
		mAdapter=new ImagePagerAdapter();
		mVPImageScanner.setAdapter(mAdapter);
		mVPImageScanner.setCurrentItem(mCurrentPosition,false);
	}

	private void initView() {
		// TODO Auto-generated method stub
		mVPImageScanner=(ViewPager) findViewById(R.id.vp_image_scanner);
	}
	private void initEvent(){
		IntentFilter filter=new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(otgReceiver, filter);
		IntentFilter filter2=new IntentFilter();
		filter2.addAction("com.intent.action.IMAGEVIEW_CLOSE");
		registerReceiver(finishReceiver, filter2);
		mVPImageScanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("cly", "viewpager.onclick");
				ImageShowActivity.this.finish();
			}
		});
	}
	
	private class ImagePagerAdapter extends PagerAdapter{
		

		public int getItemTypeCount(){
			return 2;
		};
		
		public int getItemViewType(int position){
			if(mImagePaths.get(position).endsWith(".gif")){
				return 0;
			}else{
				return 1;
			}
		}
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mImagePaths.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView((View)object);  	
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			int type = getItemViewType(position);
			if(type==1){
				ZoomImageView mZoomImageView =new ZoomImageView(getApplicationContext());
				if(mImagePaths!=null&mImagePaths.size()>0) {
					Bitmap bitmap=getBitmapFromCache(String.valueOf(position));
					if(bitmap==null){
						bitmap=zoomBitmap(position, mZoomImageView);
						if(bitmap!=null){
							addBitmapToCache(bitmap, String.valueOf(position));
						}
					}
					mZoomImageView.setImageBitmap(bitmap);
				}
				container.addView(mZoomImageView,0);
				return mZoomImageView;
			}else{
				
				GifImageView gifView=new GifImageView(ImageShowActivity.this);
				try {
					GifDrawable gib = new GifDrawable(mImagePaths.get(position));
					gifView.setImageDrawable(gib);
					gifView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ImageShowActivity.this.finish();
						}
					});
				} catch (IOException e) {
				}
				container.addView(gifView,0);
				return gifView;
			}
		}
	}
	
	private Bitmap zoomBitmap(int position,ZoomImageView imageView){
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(mImagePaths.get(position), options);
		
		int sampleSize=caculateSampleSize(options);
		options.inSampleSize=sampleSize;
		options.inJustDecodeBounds=false;
		Bitmap bitmap = BitmapFactory.decodeFile(mImagePaths.get(position), options);
		return bitmap;
	}

	private int caculateSampleSize(BitmapFactory.Options option) {
		int sampleSize=1;
		int viewWidth=getWindowManager().getDefaultDisplay().getWidth();
		int viewHeight=getWindowManager().getDefaultDisplay().getHeight();
		int bitmapWidth=option.outWidth;
		int bitmapHeight=option.outHeight;
		int minSize=Math.min(bitmapWidth/viewWidth,bitmapHeight/viewHeight);
		int maxSize=Math.max(bitmapWidth/viewWidth,bitmapHeight/viewHeight);
		sampleSize=(minSize+maxSize)/2;
		return sampleSize;
	}
	
	private void addBitmapToCache(Bitmap bitmap,String key){
		if(mImageCache!=null){
			if(getBitmapFromCache(key)==null){
				mImageCache.put(key, bitmap);
			}
		}
	}
	
	private Bitmap getBitmapFromCache(String key){
		return mImageCache.get(key);
	}
	
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}
	
	private BroadcastReceiver otgReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				ImageShowActivity.this.finish();
			}
		}
	};
	private BroadcastReceiver finishReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("com.intent.action.IMAGEVIEW_CLOSE")){
				ImageShowActivity.this.finish();
			}
		}
	};
}
