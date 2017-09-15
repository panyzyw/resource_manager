package com.yongyida.robot.resourcemanager.view;

import java.io.IOException;
import java.util.Map;

import com.yongyida.robot.resourcemanager.utils.VideoUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class VideoView extends SurfaceView implements MediaPlayerControl {

	private String TAG = "VideoView";
	private Uri mUri;
	private Map<String, String> mHeaders;

	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mAudioSession;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	private int mSeekWhenPrepared; 
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private Context mContext;
	private int defaultWidth;
	private int defaultHeight;
	private int screenWidth;
	private int screenHeight;

	public VideoView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		if (mVideoWidth > 0 && mVideoHeight > 0) {

			int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
			int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
			int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

			if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
				// the size is fixed
				width = widthSpecSize;
				height = heightSpecSize;

				if (mVideoWidth * height < width * mVideoHeight) {
					width = height * mVideoWidth / mVideoHeight;
				} else if (mVideoWidth * height > width * mVideoHeight) {
					height = width * mVideoHeight / mVideoWidth;
				}
			} else if (widthSpecMode == MeasureSpec.EXACTLY) {
				width = widthSpecSize;
				height = width * mVideoHeight / mVideoWidth;
				if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
					height = heightSpecSize;
				}
			} else if (heightSpecMode == MeasureSpec.EXACTLY) {
				height = heightSpecSize;
				width = height * mVideoWidth / mVideoHeight;
				if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
					width = widthSpecSize;
				}
			} else {
				width = mVideoWidth;
				height = mVideoHeight;
				if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
					height = heightSpecSize;
					width = height * mVideoWidth / mVideoHeight;
				}
				if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
					width = widthSpecSize;
					height = width * mVideoHeight / mVideoWidth;
				}
			}
		} else {
		}
		defaultWidth = width;
		defaultHeight = height;
		setMeasuredDimension(width, height);
	}

	@SuppressLint("NewApi")
	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(VideoView.class.getName());
	}

	@SuppressLint("NewApi")
	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(VideoView.class.getName());
	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		return getDefaultSize(desiredSize, measureSpec);
	}

	private void initVideoView() {
		screenWidth = VideoUtils.getScreenWidth(mContext);
		screenHeight = VideoUtils.getScreenHeight(mContext);
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			return;
		}
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mAudioSession = mMediaPlayer.getAudioSessionId();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mOnInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				requestLayout();
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			mCurrentState = STATE_PREPARED;

			mCanPause = mCanSeekBack = mCanSeekForward = true;

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			int seekToPosition = mSeekWhenPrepared; 
													
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							mMediaController.show(0);
						}
					}
				}
			} else {
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}

			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
					return true;
				}
			}

			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener) {
		this.mBufferingUpdateListener = mBufferingUpdateListener;
	}

	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			mSurfaceHolder = null;
			if (mMediaController != null)
				mMediaController.hide();
			release(true);
		}
	};

	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
				&& keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!mMediaPlayer.isPlaying()) {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				}
				return true;
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	@Override
	public void start() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		release(false);
	}

	public void resume() {
		openVideo();
	}

	@Override
	public int getDuration() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getDuration();
		}

		return -1;
	}

	@Override
	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	@Override
	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE
				&& mCurrentState != STATE_PREPARING);
	}

	@Override
	public boolean canPause() {
		return mCanPause;
	}

	@Override
	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	@Override
	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	@Override
	public int getAudioSessionId() {
		if (mAudioSession == 0) {
			MediaPlayer foo = new MediaPlayer();
			mAudioSession = foo.getAudioSessionId();
			foo.release();
		}
		return mAudioSession;
	}

	private boolean isFullscreen;

	/** 全屏切换 */
	public void toggleFullscreen() {
		LayoutParams params = getLayoutParams();
		if (isFullscreen) {
			// 切换为正常大小
			params.width = defaultWidth;
			params.height = defaultHeight;
		} else {
			// 全屏
			params.width = screenWidth;
			params.height = screenHeight;
		}

		isFullscreen = !isFullscreen;

		requestLayout();
	}

	/** 当前是否是全屏 */
	public boolean isFullscreen() {
		return isFullscreen;
	}

}
