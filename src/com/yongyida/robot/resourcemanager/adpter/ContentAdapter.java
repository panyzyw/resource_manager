package com.yongyida.robot.resourcemanager.adpter;


import java.util.List;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.bean.ItemBean;
import com.yongyida.robot.resourcemanager.utils.Contant;
import com.yongyida.robot.resourcemanager.utils.ImageLoader;
import com.yongyida.robot.resourcemanager.utils.ImageLoader.Type;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentAdapter extends BaseAdapter{
	
	private List<ItemBean> mList;
	private LayoutInflater mInfalter;
	private ImageLoader imageLoader;
	private int type;
	
	
	public ContentAdapter(Context context,List<ItemBean> list,int type){
		this.mInfalter=LayoutInflater.from(context);
		this.mList=list;
		this.type=type;
		imageLoader=ImageLoader.getInstance(2, Type.LIFO);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
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
		final Holder holder;
		if(convertView==null){
			convertView=mInfalter.inflate(R.layout.gridview_item, parent,false);
			holder=new Holder();
			holder.icon=(ImageView) convertView.findViewById(R.id.iv_item_icon);
			holder.name=(TextView) convertView.findViewById(R.id.tv_item_name);
			holder.time=(TextView) convertView.findViewById(R.id.tv_item_time);
			if(type==2){
				holder.path=mList.get(position).getUri();
			}
			convertView.setTag(holder);
		}else{
			holder=(Holder) convertView.getTag();
		}
		
		if(type==2){
			if(holder.path!=mList.get(position).getUri()){
				holder.icon.setImageResource(R.drawable.btn_photo);
			}
			imageLoader.loadImage(mList.get(position).getUri(), holder.icon);
			holder.path=mList.get(position).getUri();
		}else{
			if(mList.get(position).getmIcon()==null){
				holder.icon.setImageResource(R.drawable.music_default_icon);
			}else{
				holder.icon.setImageBitmap(mList.get(position).getmIcon());
			}
		}
		holder.name.setText(mList.get(position).getName());
		holder.time.setText(Contant.formatTime(mList.get(position).getTime()));
		return convertView;
	}
	class Holder{
		ImageView icon;
		TextView name;
		TextView time;
		String path;
	}
}
