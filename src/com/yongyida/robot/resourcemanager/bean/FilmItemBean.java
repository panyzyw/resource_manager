package com.yongyida.robot.resourcemanager.bean;

import java.io.Serializable;

import android.graphics.Bitmap;

public class FilmItemBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String videoQuality;
	private String videoName;
	private String videoPublishTime;
	private String vid;
	private String videoDuration;
	private String videoCategory;
	private Bitmap mBitmap;

	public String getVideoCategory() {
		return videoCategory;
	}

	public void setVideoCategory(String videoCategory) {
		this.videoCategory = videoCategory;
	}

	public String getVideoDuration() {
		return videoDuration;
	}

	public void setVideoDuration(String videoDuration) {
		this.videoDuration = videoDuration;
	}

	public String getVid() {
		return vid;
	}

	public Bitmap getmBitmap() {
		return mBitmap;
	}

	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getVideoQuality() {
		return videoQuality;
	}

	public void setVideoQuality(String videoQuality) {
		this.videoQuality = videoQuality;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getVideoPublishTime() {
		return videoPublishTime;
	}

	public void setVideoPublishTime(String videoPublishTime) {
		this.videoPublishTime = videoPublishTime;
	}

	@Override
	public String toString() {

		return "vid=" + vid + "--videoName=" + videoName + "--videoDuration=" + videoDuration + "--videoCategory="
				+ videoCategory;
	}
}
