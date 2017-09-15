package com.yongyida.robot.resourcemanager.adpter;

import java.util.List;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.bean.FilmItemBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocalGridViewAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<FilmItemBean> mList;

	public LocalGridViewAdapter(Context context, List<FilmItemBean> list) {
		this.mList = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.film_fragment_item, parent, false);
			holder.mVideoIcon = (ImageView) convertView.findViewById(R.id.iv_video_icon);
			holder.mVideoTitle = (TextView) convertView.findViewById(R.id.tv_video_name);
			holder.mVideoPublishTime = (TextView) convertView.findViewById(R.id.tv_video_publish_time);
			convertView.setTag(holder);

		} else {
			holder = (Holder) convertView.getTag();
		}
		FilmItemBean filmItemBean = mList.get(position);
		if (filmItemBean.getmBitmap() != null) {
			holder.mVideoIcon.setImageBitmap(filmItemBean.getmBitmap());
		} else {
			holder.mVideoIcon.setImageResource(R.drawable.video_default_icon);
		}
		if (filmItemBean.getVideoName() != null) {
			holder.mVideoTitle.setText(filmItemBean.getVideoName());
		} else {
			holder.mVideoTitle.setText("未知电影");
		}
		if (filmItemBean.getVideoPublishTime() != null) {
			holder.mVideoPublishTime.setText(filmItemBean.getVideoPublishTime());
		} else {
			holder.mVideoPublishTime.setText("未知年份");
		}
		return convertView;
	}

	private class Holder {
		public ImageView mVideoIcon;
		public TextView mVideoTitle;
		public TextView mVideoPublishTime;
	}
}
