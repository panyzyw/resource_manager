package com.yongyida.robot.resourcemanager.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.yongyida.robot.resourcemanager.R;
import com.yongyida.robot.resourcemanager.bean.VideoItem;
import com.yongyida.robot.resourcemanager.utils.ToastUtils;
import com.yongyida.robot.resourcemanager.utils.VideoUtils;

import java.util.ArrayList;

import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

public class VideoPlayerActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {

	private VideoView mVideoView;
	private View mBlackView;
	
	private ArrayList<VideoItem> mList;
	private int currentItem;

	private LinearLayout mLayoutBottomCtrl;
	private LinearLayout mLayoutTopCtrl;

	private TextView mTextViewVideoDuration;
	private TextView mTextViewVideoCurrentTime;
	private TextView mTextViewVideoName;

	private SeekBar mSeekBarVideo;

	private Button mButtonNext;
	private ImageButton mButtonBack;
	private Button mButtonPlay;
	private AudioManager audioManager;

	private View mViewBright;// 调整亮度的View

	// 播放器亮度调节显示相关
	private LinearLayout mLayoutBrightControl;
	private TextView mTvBrightValue;
	
	// 播放器亮度调节显示相关
	private LinearLayout mLayoutSoundControl;
	private TextView mTvSoundValue;

	private GestureDetector mGestureDetector;
	private float mMaxVoiceScreenHeightScale;// 声音最大值相对于屏幕高度的的值
	private float mMaxBrightScreenHeightScale;// 亮度最大值相对于屏幕高度的的值

	private int mMaxVoice;
	private int mCurrentVoice;
	private int mLastVideoTime=-1;
	
	private boolean isPausedByTouch;

	private static final int UPDATE_VIDEO_TIME = 0X110;
	private static final int HIDE_CTRL_LAYOUT = 0X111;// 隐藏控制面板的消息
	private static final int HIDE_BRIGHT_TEXT = 0x112;
	private static final int HIDE_SOUND_TEXT = 0x113;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE_VIDEO_TIME:
				updateCurrentPosition();
				break;
			case HIDE_CTRL_LAYOUT:
				toggleCtrlLayout();
				break;
			case HIDE_BRIGHT_TEXT:
				hideBrightLayout();
				break;
			case HIDE_SOUND_TEXT:
				hideSoundLayout();
				break;
			}
		}
	};
	private BroadcastReceiver receiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
				if(VideoPlayerActivity.this!=null)
					VideoPlayerActivity.this.finish();
			}
		}
		
	};

	private void hideBrightLayout() {
		if (mLayoutBrightControl.getVisibility() == View.GONE) {
			return;
		} else {
			mLayoutBrightControl.setVisibility(View.GONE);
		}
	}
	
	private void hideSoundLayout() {
		if (mLayoutSoundControl.getVisibility() == View.GONE) {
			return;
		} else {
			mLayoutSoundControl.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videoplayer_acitvity_player);
		initView();
		initData();
		initEvent();
	}
	
	private void initView() {
		mVideoView = (VideoView) findViewById(R.id.video_view);
		mBlackView=findViewById(R.id.black_view);
		mLayoutBottomCtrl = (LinearLayout) findViewById(R.id.ll_bottom_ctrl);
		mLayoutTopCtrl = (LinearLayout) findViewById(R.id.ll_top_ctrl);

		mTextViewVideoCurrentTime = (TextView) findViewById(R.id.tv_current_position);
		mTextViewVideoDuration = (TextView) findViewById(R.id.tv_duration);
		mTextViewVideoName = (TextView) findViewById(R.id.tv_current_video_name);

		mSeekBarVideo = (SeekBar) findViewById(R.id.sb_video);

		mButtonNext = (Button) findViewById(R.id.btn_next);
		mButtonPlay = (Button) findViewById(R.id.btn_play);
		mButtonBack = (ImageButton) findViewById(R.id.ib_back);

		mViewBright = findViewById(R.id.view_brightness);
		mViewBright.setVisibility(View.VISIBLE);
		ViewHelper.setAlpha(mViewBright, 0.0f);

		mLayoutBrightControl = (LinearLayout) findViewById(R.id.ll_bright_control);
		mTvBrightValue = (TextView) findViewById(R.id.tv_bright_value);
		
		mLayoutSoundControl = (LinearLayout) findViewById(R.id.ll_sound_control);
		mTvSoundValue = (TextView) findViewById(R.id.tv_sound_value);

		initCtrlLayout();
	}

	private void initData() {
		mList = (ArrayList<VideoItem>) getIntent().getSerializableExtra("list");
		currentItem = getIntent().getIntExtra("position", -1);
		openVideo();
		initVoice();
		mMaxVoiceScreenHeightScale = mMaxVoice * 1.0f / VideoUtils.getScreenHeight(this);

		mMaxBrightScreenHeightScale = 1.0f / VideoUtils.getScreenHeight(this);
	}

	/**
	 * 初始化音量
	 */
	private void initVoice() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVoice = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mCurrentVoice = getCurrentVoice();
	}

	private int getCurrentVoice() {
		return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	private void openVideo() {
		if (mList == null || mList.isEmpty() || currentItem == -1) {
			return;
		}
		String path = mList.get(currentItem).getVid();
		if(path.endsWith("mpg")||path.endsWith("vob")){
			ToastUtils.showToast(VideoPlayerActivity.this,getResources().getString(R.string.video_format_error));
			VideoPlayerActivity.this.finish();
		}
		String name = mList.get(currentItem).getVideoName();
		mTextViewVideoName.setText(name);
		mButtonNext.setEnabled(mList.size() != currentItem - 1);
		mVideoView.setVideoPath(path);
		mButtonPlay.setBackgroundResource(R.drawable.btn_play_press); 
		mVideoView.start();
		isPausedByTouch=false;
		sendHideCtrlMessage();
	}

	private void initEvent() {
		mVideoView.setOnPreparedListener(onPreparedListener);
		mVideoView.setOnInfoListener(onInfoListener);
		mVideoView.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mVideoView.setOnCompletionListener(onCompletionListener);

		mSeekBarVideo.setOnSeekBarChangeListener(this);

		mGestureDetector = new GestureDetector(this, onGestureListener);

		mButtonNext.setOnClickListener(this);
		mButtonBack.setOnClickListener(this);
		mButtonPlay.setOnClickListener(this);
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		registerReceiver(receiver, filter);
	}

	/**
	 * 测量控制面板的宽高
	 */
	private void initCtrlLayout() {
		mLayoutTopCtrl.measure(0, 0);
		mLayoutBottomCtrl.measure(0, 0);// 让系统自动测量控件的宽高
		float topDetalY = mLayoutTopCtrl.getMeasuredHeight();
		float BottomDetalY = mLayoutBottomCtrl.getMeasuredHeight();
		// 控制面板上下平移detalY;
		ViewHelper.setTranslationY(mLayoutBottomCtrl, BottomDetalY);
		ViewHelper.setTranslationY(mLayoutTopCtrl, -topDetalY);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_back:
			mBlackView.setVisibility(View.VISIBLE);
			this.finish();
			break;
		case R.id.btn_next:
			next();
			break;
		case R.id.btn_play:
			play();
			break;
		}
	}

	private void next() {
		if (currentItem != mList.size() - 1) {
			currentItem++;
			openVideo();
		} else {
			currentItem = 0;
			openVideo();
		}
	}

	/**
	 * 暂停或播放
	 */
	private void play() {


		if (mVideoView.isPlaying()) {
			mButtonPlay.setBackgroundResource(R.drawable.btn_play_normal);
			mVideoView.pause();
		} else {

			mButtonPlay.setBackgroundResource(R.drawable.btn_play_press);
			mVideoView.start();
			if(isPausedByTouch){
				sendHideCtrlMessage(); 
				isPausedByTouch=false;
			}
			mButtonPlay.setVisibility(View.GONE);
		}
	}

	private OnPreparedListener onPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(io.vov.vitamio.MediaPlayer mediaPlayer) {
			mediaPlayer.setPlaybackSpeed(1.0f);
			mBlackView.setVisibility(View.GONE);
			int duration = (int) mVideoView.getDuration(); // 获取视频总时长
			mTextViewVideoDuration.setText(VideoUtils.formatTime(duration)); // 格式化视频总时长
			Log.e("time", "duration=" + VideoUtils.formatTime(duration));
			mSeekBarVideo.setMax(duration);
			updateCurrentPosition(); // 更新当播放时间
		}
	};

	private void updateCurrentPosition() {
		int courrentTime = (int) mVideoView.getCurrentPosition();
		mTextViewVideoCurrentTime.setText(VideoUtils.formatTime(courrentTime));
		mSeekBarVideo.setProgress(courrentTime);
		mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_TIME, 1000);
	}

	/**
	 * 缓冲卡顿监听
	 */
	private OnInfoListener onInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(io.vov.vitamio.MediaPlayer mp, int what, int arg2) {
			
			return true;
		}
	};
	/**
	 * 缓冲监听
	 */
	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {


		@Override
		public void onBufferingUpdate(io.vov.vitamio.MediaPlayer arg0, int percent) {
			
		}

	};

	/**
	 * 更新第二进度条
	 */
	private void updateSecondProgress(int percent) {
		float secondPercent = percent / 100f;
		int secondProgress = (int) (mVideoView.getDuration() * secondPercent);
		mSeekBarVideo.setSecondaryProgress(secondProgress);
	}

	/**
	 * 视频播放完成回调监听
	 */
	private OnCompletionListener onCompletionListener = new OnCompletionListener() {


		@Override
		public void onCompletion(io.vov.vitamio.MediaPlayer arg0) {
			next();
		}
	};

	private SimpleOnGestureListener onGestureListener = new SimpleOnGestureListener() {
		boolean isLeftDown;
		boolean isRightDown;

		/**
		 * 单击
		 */
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if(!isPausedByTouch){
				removeHideCtrlMessage();
				toggleCtrlLayout();
			}
			return false;
		};

		/**
		 * 双击
		 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			play();
			return false;
		}

		public boolean onDown(MotionEvent e) {
			isLeftDown = (e.getX() / VideoUtils.getScreenWidth(VideoPlayerActivity.this)) < 0.2f;
			isRightDown = 0.8f < (e.getX() / VideoUtils.getScreenWidth(VideoPlayerActivity.this))
					& (e.getX() / VideoUtils.getScreenWidth(VideoPlayerActivity.this)) < 1.0f;
			Log.e("vioce", isLeftDown + "");
			mCurrentVoice = getCurrentVoice();
			return super.onDoubleTap(e);
		};

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			float detalY = e2.getY() - e1.getY();
			float detalX = e2.getX() - e1.getX();
			if (isLeftDown) {
				// 亮度Seekbar初始化
				initBrightValue();
				changeBrightness(detalY);
			} else if (isRightDown) {
				// 获取当前音量，设置
				initVoiceValue();
				changeVoice(detalY);
			} else {
				if (detalX > 120 || detalX < -120) {
					changeCurrentPosition(detalX);
				}
			}
			return true;
		}

	};

	private float getCurrentBright() {
		return ViewHelper.getAlpha(mViewBright);
	}

	private void initBrightValue() {
		float currentBright = getCurrentBright();
		int initValue = (int) (((0.9f - getCurrentBright() + 0.0000001) / 0.9f) * 100);
		mTvBrightValue.setText(initValue + "%");
	}

	private void initVoiceValue() {
		mTvSoundValue.setText((int) ((getCurrentVoice() + 0.00001) / mMaxVoice) * 100 + "%");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			removeHideCtrlMessage();
			break;
		case MotionEvent.ACTION_UP:
			sendHideCtrlMessage();
			break;
		}
		return super.onTouchEvent(event);
	};

	/**
	 * 控制声音
	 * 
	 * @param detalY
	 */
	private void changeVoice(float detalY) {
		if (Math.abs(detalY) < 20)
			return;
		mLayoutSoundControl.setVisibility(View.VISIBLE);
		int detalVoice = (int) (-detalY * mMaxVoiceScreenHeightScale);
		int result = mCurrentVoice + detalVoice;
		if (result > mMaxVoice) {
			result = mMaxVoice;
		} else if (result < 0) {
			result = 0;
		}
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, result, 0);
		mTvSoundValue.setText(((int) ((result + 0.000001) * 100 / mMaxVoice) + "%"));
		mHandler.sendEmptyMessageDelayed(HIDE_SOUND_TEXT, 2000);
	}

	/**
	 * 控制亮度
	 * 
	 * @param detalY
	 */
	private void changeBrightness(float detalY) {
		float detalBright = detalY / 20 * mMaxBrightScreenHeightScale;
		if (Math.abs(detalY) < 20)
			return;
		mLayoutBrightControl.setVisibility(View.VISIBLE);
		float result = getCurrentBright() + detalBright;
		if (result < 0) {
			result = 0;
		} else if (result > 0.9f) {
			result = 0.9f;
		}
		ViewHelper.setAlpha(mViewBright, result);
		Log.e("bright", result + "");
		int value = (int) (((0.9f - result + 0.0000001) / 0.9f) * 100);
		mTvBrightValue.setText(value + "%");
		mHandler.sendEmptyMessageDelayed(HIDE_BRIGHT_TEXT, 2000);
	};

	/**
	 * 快进或者后退
	 * 
	 * @param detalX
	 */
	private void changeCurrentPosition(float detalX) {
		mVideoView.seekTo((int) (mVideoView.getCurrentPosition() + detalX * 100));
	}

	/**
	 * 显示或隐藏控制面板
	 */
	private void toggleCtrlLayout() {
		float translationY = ViewHelper.getTranslationY(mLayoutBottomCtrl);
		if (translationY == 0) {
			ViewPropertyAnimator.animate(mLayoutBottomCtrl).translationY(mLayoutBottomCtrl.getHeight());
			ViewPropertyAnimator.animate(mLayoutTopCtrl).translationY(-mLayoutTopCtrl.getHeight());
			mButtonPlay.setVisibility(View.INVISIBLE);
		} else {
			ViewPropertyAnimator.animate(mLayoutBottomCtrl).translationY(0f);
			ViewPropertyAnimator.animate(mLayoutTopCtrl).translationY(0f);
			mButtonPlay.setVisibility(View.VISIBLE);
			sendHideCtrlMessage();
		}
	}

	private void removeHideCtrlMessage() {
		mHandler.removeMessages(HIDE_CTRL_LAYOUT);
	}

	private void sendHideCtrlMessage() {
		removeHideCtrlMessage();
		mHandler.sendEmptyMessageDelayed(HIDE_CTRL_LAYOUT, 3000);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			if(!mVideoView.isPlaying()){
				mVideoView.start();
				mButtonPlay.setBackgroundResource(R.drawable.btn_play_press);
			}
			mVideoView.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		removeHideCtrlMessage();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		sendHideCtrlMessage();
	}

	@Override
	protected void onPause() {
		mButtonPlay.setVisibility(View.VISIBLE);
		if(mVideoView!=null){
			mLastVideoTime = (int) mVideoView.getCurrentPosition();
			if(mVideoView.isPlaying()){
				mVideoView.pause();
			}
			mButtonPlay.setBackgroundResource(R.drawable.btn_play_normal);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mBlackView.setVisibility(View.GONE);
		if (mLastVideoTime != -1) {
			isPausedByTouch=true;
			mVideoView.seekTo(mLastVideoTime); 
			float translationY = ViewHelper.getTranslationY(mLayoutBottomCtrl);
			if (translationY != 0) {
				ViewPropertyAnimator.animate(mLayoutBottomCtrl).translationY(0f);
				ViewPropertyAnimator.animate(mLayoutTopCtrl).translationY(0f);
				mButtonPlay.setVisibility(View.VISIBLE);
			}
			if(!mVideoView.isPlaying()){
				mButtonPlay.setBackgroundResource(R.drawable.btn_play_normal);
			}
		}else{
			mButtonPlay.setBackgroundResource(R.drawable.btn_play_press);
			mVideoView.start(); // 开始播放
			mButtonPlay.setVisibility(View.GONE);
			toggleCtrlLayout();
		}
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mHandler!=null){
			mHandler.removeCallbacksAndMessages(null);
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(receiver!=null){
			if(VideoPlayerActivity.this!=null)
				unregisterReceiver(receiver);
		}
		if(mVideoView!=null){
			mVideoView.stopPlayback();
			mVideoView=null;
		}
		if(mList!=null){
			mList=null;
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		mBlackView.setVisibility(View.VISIBLE);
		this.finish();
	}
	
}
