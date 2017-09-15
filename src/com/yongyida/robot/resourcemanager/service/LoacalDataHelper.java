package com.yongyida.robot.resourcemanager.service;

import java.io.File;

import com.yongyida.robot.resourcemanager.bean.ItemBean;
import com.yongyida.robot.resourcemanager.utils.Contant;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class LoacalDataHelper {
	private Handler mHandler;
	
	private ContentResolver resolver;
	private Uri[] uris={MediaStore.Images.Media.EXTERNAL_CONTENT_URI,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI};
	private String[] imageProjection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
									MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATE_MODIFIED };
	private String[] musicProjection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
										MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DISPLAY_NAME, 
										MediaStore.Audio.Media.DATE_MODIFIED };
	private String selection = null;
	private String[] selectionArgs = null;
	private String[] sortOrder = {MediaStore.Images.Media.DATE_MODIFIED,MediaStore.Audio.Media.DATE_MODIFIED};
	
	public LoacalDataHelper(Context context,Handler handler){
		resolver=context.getContentResolver();
		this.mHandler=handler;
	}
	public void getMusicResourceItem(){
		new Thread(){
			Cursor cursor=null;
			@Override
			public void run() {
				 cursor= resolver.query(uris[1], musicProjection, selection, selectionArgs, sortOrder[1]);
				while (cursor.moveToNext()) {
					ItemBean item=new ItemBean();
					String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
					if(!path.startsWith(Environment.getExternalStorageDirectory()+"/msc/")){
						Log.e("cly", path);
						File file=new File(path);
						if(file.exists()&file.isFile()){
							item.setUri(path);
							item.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
							item.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
							item.setTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)));
							item.setmIcon(creatMusicThumbnail(path));
							if(path.startsWith("/storage/usbotg/")){
								sendLoadMessage(Contant.QUERY_ONE,item,Contant.OTG_DATA);
							}else{
								sendLoadMessage(Contant.QUERY_ONE,item,Contant.INTERNAL_DATA);
							}
						}
					}
				}
				if(cursor!=null){
					cursor.close();
				}
				mHandler.sendEmptyMessage(Contant.QUERY_COMPLETE);
			}
		}.start();
	}
	/**
	 * 获取音频缩略图
	 * @param path
	 * @return
	 */
	public static Bitmap creatMusicThumbnail(String path){
		Bitmap bitmap=null;
		//获取多媒体文件元数据的类
		MediaMetadataRetriever retriever=new MediaMetadataRetriever();
		try {
			//设置数据源
			retriever.setDataSource(path);
			//得到字节型的数据
			byte[] embeddedPicture = retriever.getEmbeddedPicture();
			//字节转换成图片
			bitmap=BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(retriever!=null){
				try {
					retriever.release();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}
	/**
	 * 获取所有的图片
	 * @return
	 */
	public void getImageResourceItem(){
		new Thread(){
			Cursor cursor=null;
			File file=null;
			@Override
			public void run() {
				 cursor= resolver.query(uris[0], imageProjection, selection, selectionArgs, sortOrder[0]);
				while (cursor.moveToNext()) {
					ItemBean item=new ItemBean();
					int id=cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
					String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
					file=new File(path);
					if(file.exists()&file.isFile()){
						if (file.length()>10000) {
							item.setUri(path);
							item.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE)));
							item.setTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)));
							if(!path.contains("storage/emulated/0/PlayCamera")){
								if(path.startsWith("/storage/usbotg/")){
									sendLoadMessage(Contant.QUERY_ONE,item,Contant.OTG_DATA);
								}else{
									sendLoadMessage(Contant.QUERY_ONE,item,Contant.INTERNAL_DATA);
								}
							}
						}
					}
				}
				mHandler.sendEmptyMessage(Contant.QUERY_COMPLETE);
				if(cursor!=null){
					cursor.close();
				}
			}
		}.start();
	}	
	
	private void sendLoadMessage(int what,Object obj,int type){
		Message msg=Message.obtain();
		msg.what=what;
		msg.obj=obj;
		msg.arg1=type;
		mHandler.sendMessage(msg);
	}
}
