package com.yongyida.robot.resourcemanager.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.bean.FileBean;

import java.util.List;

public class OtherFileAdapter
        extends BaseAdapter
{

    private List<FileBean> files;
    private LayoutInflater mInflater;

    public static final int TYPE_DOC   = 0;
    public static final int TYPE_XLS   = 1;
    public static final int TYPE_PPT   = 2;
    public static final int TYPE_PDF   = 3;
    public static final int TYPE_APK   = 4;
    public static final int TYPE_TXT   = 5;
    public static final int TYPE_COUNT = 6;


    public OtherFileAdapter(Context context, List<FileBean> files) {
        // TODO Auto-generated constructor stub
        this.files = files;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        FileBean fileBean = files.get(position);
        return fileBean.getType();
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return TYPE_COUNT;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int  type = getItemViewType(position);
        Hold hold = null;
        if (convertView == null) {
            hold = new Hold();
            convertView = mInflater.inflate(R.layout.office_listview_item, parent, false);
            hold.mIvOfficeItemIcon = (ImageView) convertView.findViewById(R.id.iv_office_item_type_icon);
            hold.mTvOfficeItemName = (TextView) convertView.findViewById(R.id.tv_office_item_type_name);
            hold.mTvOfficeItemSize = (TextView) convertView.findViewById(R.id.tv_office_item_type_size);
            hold.mTvOfficeItemTime = (TextView) convertView.findViewById(R.id.tv_office_item_type_time);
            convertView.setTag(hold);
        } else {
            hold = (Hold) convertView.getTag();
        }
        switch (type) {
            case TYPE_DOC:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.doc);
                break;
            case TYPE_XLS:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.xls);
                break;
            case TYPE_PPT:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.ppt);
                break;
            case TYPE_PDF:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.pdf);
                break;
            case TYPE_APK:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.apk);
                break;
            case TYPE_TXT:
                hold.mIvOfficeItemIcon.setImageResource(R.drawable.icon_txt);
                break;
        }
        hold.mTvOfficeItemName.setText(files.get(position)
                                            .getName());
        hold.mTvOfficeItemSize.setText(files.get(position)
                                            .getSize());
        hold.mTvOfficeItemTime.setText(files.get(position)
                                            .getDate());
        return convertView;
    }

    private class Hold {
        ImageView mIvOfficeItemIcon;
        TextView  mTvOfficeItemName, mTvOfficeItemSize, mTvOfficeItemTime;
    }
}
