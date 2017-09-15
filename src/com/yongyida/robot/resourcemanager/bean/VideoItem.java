package com.yongyida.robot.resourcemanager.bean;

import java.io.Serializable;

public class VideoItem implements Serializable {
	private String videoName;
	private String vid;

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}
}
