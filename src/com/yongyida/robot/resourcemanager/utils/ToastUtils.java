package com.yongyida.robot.resourcemanager.utils;


import com.yongyida.robot.resourcemanager.R;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {
	private static Toast mToast;

	private static Handler mHandler = new Handler();
	private static Runnable runnable = new Runnable() {

		@Override
		public void run() {
			mToast.cancel();
			mToast = null;
		}
	};

	public static void showToast(Context context, String message) {
		View view = null;
		if (context != null) {
			view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
			TextView textView = (TextView) view.findViewById(R.id.tv_toast_message);
			textView.setText(message);
			mHandler.removeCallbacks(runnable);
			if (mToast == null) {
				mToast = new Toast(context);
				mToast.setDuration(Toast.LENGTH_SHORT);
				mToast.setView(view);
			}
			mHandler.postDelayed(runnable, 1500);
			mToast.show();
		}
	}
}
