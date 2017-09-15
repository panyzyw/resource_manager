package com.yongyida.robot.resourcemanager.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.adpter.OtherFileAdapter;
import com.yongyida.robot.resourcemanager.bean.FileBean;
import com.yongyida.robot.resourcemanager.utils.Contant;
import com.yongyida.robot.resourcemanager.utils.ToastUtils;
import com.yongyida.robot.resourcemanager.utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OtherFileActivity extends Activity {

	private ListView mLvOfficeContent;
	private View mLoadingLayout;
	private Button mBtnBack;

	private List<FileBean> mOfficeFiles;
	private OtherFileAdapter mAdapter;

	private List<String> paths =new ArrayList<String>();
	public static String[] docs = { ".doc", ".docx", ".dot", ".dotx"};
	public static String[] xlss = { ".xls", ".xlsx", ".csv", ".xlt" ,".xlstx"};
	public static String[] ppts = { ".ppt", ".pptx", ".pps", ".ppsx",".pot",".potx"};
	public static String[] pdfs = {".pdf"};
	public static String[] txts = {".txt"};
	public static String  APK=".apk";

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Contant.QUERY_START:
				mLoadingLayout.setVisibility(View.VISIBLE);
				scannerFile();
				break;

			case Contant.QUERY_ONE:
				File file = (File) msg.obj;
				FileBean bean = null;
				if (file.exists() & file.isFile()) {
					bean = new FileBean();
					bean.setPath(file.getAbsolutePath());
					bean.setSize(Contant.formatFileLength(file.length()));
					mOfficeFiles.add(bean);
				}

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

				break;

			case Contant.QUERY_COMPLETE:
				mLoadingLayout.setVisibility(View.INVISIBLE);
				if(mOfficeFiles.size()<=0){
					ToastUtils.showToast(OtherFileActivity.this, ""+getResources().getString(R.string.no_office_file));
				}
				break;
				
			case Contant.DELETE_ONE:

				break;
			}
		};
	};
	
	
	private BroadcastReceiver otgReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				mHandler.removeCallbacksAndMessages(null);
				mOfficeFiles.clear();
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}
				mHandler.sendEmptyMessage(Contant.QUERY_START);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		mLvOfficeContent = (ListView) findViewById(R.id.lv_office_type);
		mLoadingLayout = findViewById(R.id.file_scanner_loading_layout);
		mBtnBack = (Button) findViewById(R.id.btn_back);
	}

	private void initData() {
		
		paths.add("/storage/sdcard0/");
		String otgRootPath = VideoUtils.getOtgRootPath(OtherFileActivity.this);
		if(otgRootPath!=null){
			paths.add(otgRootPath);
		}
		
		mOfficeFiles = new ArrayList<FileBean>();
		if (mAdapter == null) {
			mAdapter = new OtherFileAdapter(this, mOfficeFiles);
		}
		mLvOfficeContent.setAdapter(mAdapter);

		mHandler.sendEmptyMessage(Contant.QUERY_START);
	}

	private void initEvent() {
		
		IntentFilter filter=new IntentFilter(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(otgReceiver, filter);

		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				OtherFileActivity.this.finish();
			}
		});

		mLvOfficeContent.setSelector(new ColorDrawable(Color.parseColor("#6698D3ED")));
		mLvOfficeContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent =null;
				String path =mOfficeFiles.get(position).getPath();
				File file = new File(path);
				if (file == null || !file.exists()) {
		    		return ;
		    	}
		    	Uri uri = Uri.fromFile(file);
				try {
					intent= new Intent();
					if(mOfficeFiles.get(position).getPath().endsWith(APK)){
				    	intent.setAction(Intent.ACTION_VIEW);
				    	intent.setDataAndType(uri, "application/vnd.android.package-archive");
				    }else{
				    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    	intent.setAction(android.content.Intent.ACTION_VIEW);
				    	intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");
				    	intent.setData(uri);
				    }
					startActivity(intent);
				   } catch (ActivityNotFoundException e) {
					    return ;
				   }
			}
		});
		
		mLvOfficeContent.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
				
				 if(null!=OtherFileActivity.this){
					  new AlertDialog.Builder(OtherFileActivity.this).setTitle(""+getResources().getString(R.string.dialog_delete))
					  			.setMessage(""+getResources().getString(R.string.dialog_delete_sure))
					  			
					  			.setNegativeButton(""+getResources().getString(R.string.dialog_cancel),  new android.content.DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).setPositiveButton(""+getResources().getString(R.string.dialog_confirm),  new android.content.DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										FileBean bean= (FileBean) parent.getItemAtPosition(position);
										deleteOneData(bean);
									}
								}).show();
				  } 
				return true;
			}
		});
	}
	
	
	protected void deleteOneData(FileBean bean) {
		boolean delete=false;
		File file=new File(bean.getPath());
		if(file.exists()&file.isFile()){
			delete=file.delete();
		}
		if(delete){
			
			mOfficeFiles.remove(bean);
			if(mAdapter!=null){
				mAdapter.notifyDataSetChanged();
			}
		}else{
			ToastUtils.showToast(getApplicationContext(), ""+getResources().getString(R.string.not_remove_permission));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mOfficeFiles!=null){
			mOfficeFiles.clear();
		}
		if(otgReceiver!=null){
			unregisterReceiver(otgReceiver);
		}
	}

	public void finish() {
		super.finish();
		this.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}

	private void scannerFile() {
		new Thread() {
			public void run() {
				File parent = null;
				for (int i = 0; i < paths.size(); i++) {
						parent = new File(paths.get(i));
						if (parent.exists()) {
							findAllOfficeFile(parent);
						}
					}
				mHandler.sendEmptyMessage(Contant.QUERY_COMPLETE);
			};
		}.start();
	}

	private void findAllOfficeFile(File parent) {
		if(!parent.exists()){
			return;  
		}
		try{
			File[] files = parent.listFiles();
			if(files!=null&files.length>0){
				for (File file : files) {
					if (file.isDirectory()) {
						findAllOfficeFile(file);
					} else {
						String name = file.getName();
						getDocFile(file, name);
						getXlsFile(file, name);
						getPptFile(file, name);
						getPdfFile(file, name);
						getTxtFile(file, name);
						getApkFile(file, name);
					}
				}
			}
		}catch(Exception e){
			
		}
	}
	private void getTxtFile(File file, String name) {
		Message msg;
		for (int i = 0; i < txts.length; i++) {
			if (name.endsWith(txts[i]) ){
				msg = Message.obtain();
				msg.obj = file.getAbsoluteFile();
				msg.what = Contant.QUERY_ONE;
				mHandler.sendMessage(msg);
			}
		}

	}

	private void getPdfFile(File file, String name) {
		Message msg;
		for (int i = 0; i < pdfs.length; i++) {
			if (name.endsWith(pdfs[i]) ){
				msg = Message.obtain();
				msg.obj = file.getAbsoluteFile();
				msg.what = Contant.QUERY_ONE;
				mHandler.sendMessage(msg);
			}
		}

	}

	private void getDocFile(File file, String name) {
		Message msg;
		for (int i = 0; i < docs.length; i++) {
			if (name.endsWith(docs[i]) ){
				msg = Message.obtain();
				msg.obj = file.getAbsoluteFile();
				msg.what = Contant.QUERY_ONE;
				mHandler.sendMessage(msg);
			}
		}
	}
	private void getPptFile(File file, String name) {
		Message msg;
		for (int i = 0; i < ppts.length; i++) {
			if (name.endsWith(ppts[i]) ){
				msg = Message.obtain();
				msg.obj = file.getAbsoluteFile();
				msg.what = Contant.QUERY_ONE;
				mHandler.sendMessage(msg);
			}
		}
	}
	private void getXlsFile(File file, String name) {
		Message msg;
		for (int i = 0; i < xlss.length; i++) {
			if (name.endsWith(xlss[i]) ){
				msg = Message.obtain();
				msg.obj = file.getAbsoluteFile();
				msg.what = Contant.QUERY_ONE;
				mHandler.sendMessage(msg);
			}
		}
	}
	private void getApkFile(File file, String name) {
		if(name.endsWith(APK)){
			Message msg = Message.obtain();
			msg.obj = file.getAbsoluteFile();
			msg.what = Contant.QUERY_ONE;
			mHandler.sendMessage(msg);
		}
	}
}
