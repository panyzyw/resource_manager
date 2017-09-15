package com.yongyida.robot.resourcemanager.bean;

import com.yongyida.robot.resourcemanager.activity.OtherFileActivity;
import com.yongyida.robot.resourcemanager.adpter.OtherFileAdapter;
import com.yongyida.robot.resourcemanager.utils.Contant;

import java.io.File;
import java.util.Arrays;

public class FileBean {
	
	private String name;
	private String size;
	private String path;
	private String date;
	private int type;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getName(){
		File file =new File(path);
		this.name=file.getName();
		return name;
	}
	public String getDate(){
		File file =new File(path);
		this.date=Contant.formatOfficeTime(file.lastModified()).toString();
		return date;
	}
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	
	public int getType(){
		int lastIndexOf = this.getName().lastIndexOf(".");
		String substring = this.getName().substring(lastIndexOf, this.getName().length());
		if(Arrays.asList(OtherFileActivity.docs).contains(substring)){
			this.type = OtherFileAdapter.TYPE_DOC;
		}
		else if(Arrays.asList(OtherFileActivity.xlss).contains(substring)){
			this.type= 	OtherFileAdapter.TYPE_XLS;
		}
		else if(Arrays.asList(OtherFileActivity.ppts).contains(substring)){
			this.type= 	OtherFileAdapter.TYPE_PPT;
		}else if(Arrays.asList(OtherFileActivity.pdfs).contains(substring)){
			this.type=OtherFileAdapter.TYPE_PDF;
		}else if(Arrays.asList(OtherFileActivity.txts).contains(substring)){
			this.type=OtherFileAdapter.TYPE_TXT;
		}else{
			this.type=OtherFileAdapter.TYPE_APK;
		}
		return this.type;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return path+"--"+getName()+"--"+getDate()+getType();
	}
	
}
