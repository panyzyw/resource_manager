package com.yongyida.robot.resourcemanager.bean;

import android.graphics.Bitmap;

public class ItemBean {
	private String uri;
	private Bitmap mIcon;
	private String name;
	private long time;
	private int duration;
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Bitmap getmIcon() {
		return mIcon;
	}
	public void setmIcon(Bitmap mIcon) {
		this.mIcon = mIcon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
