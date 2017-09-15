package com.yongyida.robot.resourcemanager.animotion;


import com.yongyida.robot.resourcemanager.R;

import android.app.Activity;


public class ActivityAnimator
{
	public void animation(Activity a)
	{
		a.overridePendingTransition(R.anim.acvivity_start_anim, 0);
	}
	public void backAnimation(Activity a)
	{
		a.overridePendingTransition(0, R.anim.acvivity_stop_anim);
	}
}
