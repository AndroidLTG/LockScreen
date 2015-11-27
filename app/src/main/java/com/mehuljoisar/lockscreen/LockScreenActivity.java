package com.mehuljoisar.lockscreen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mehuljoisar.lockscreen.utils.LockscreenService;
import com.mehuljoisar.lockscreen.utils.LockscreenUtils;
import com.mehuljoisar.lockscreenandroid.R;

import java.util.Calendar;

public class LockScreenActivity extends Activity implements
		LockscreenUtils.OnLockStatusChangedListener {

	// User-interface
	//private Button btnUnlock;
	SeekBar sb;

	TextView seekbartest,txttime;
	RelativeLayout page_background;
	// Member variables
	private LockscreenUtils mLockscreenUtils;

	// Set appropriate flags to make the screen appear over the keyguard
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		);

		super.onAttachedToWindow();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lockscreen);

		init();

		// unlock screen in case of app get killed by system
		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {
			enableKeyguard();
			unlockHomeButton();
		} else {

			try {
				// disable keyguard
				disableKeyguard();

				// lock home button
				lockHomeButton();

				// start service for observing intents
				startService(new Intent(this, LockscreenService.class));

				// listen the events get fired during the call
				StateListener phoneStateListener = new StateListener();
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);

			} catch (Exception e) {
			}

		}
	}
	private void loadtime()
	{
		final Calendar c = Calendar.getInstance();
		int a = c.get(Calendar.AM_PM);
		if(a == Calendar.AM)

		{
			if(c.get(Calendar.MINUTE)<10)
			txttime.setText(c.get(Calendar.HOUR)+":0"+c.get(Calendar.MINUTE)+" AM");
			else txttime.setText(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+" AM");
		}
		else {
			if(c.get(Calendar.MINUTE)<10)
				txttime.setText(c.get(Calendar.HOUR)+":0"+c.get(Calendar.MINUTE)+" PM");
			else txttime.setText(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+" PM");
		}
	}
	private void init() {
		mLockscreenUtils = new LockscreenUtils();
		sb = (SeekBar) findViewById(R.id.myseek);
		txttime = (TextView) findViewById(R.id.txttime);
		seekbartest = (TextView) findViewById(R.id.slider_text);
		page_background = (RelativeLayout) findViewById(R.id.full_page_layout);
		seekbartest.setText("Trượt để mở khóa");
		loadtime();

		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress > 95) {
					loadtime();
					seekBar.setThumb(getResources().getDrawable(R.drawable.slider_icon));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				seekbartest.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (seekBar.getProgress() < 80) {
					seekBar.setProgress(0);
					sb.setBackgroundResource(R.drawable.slider);
					seekbartest.setVisibility(View.VISIBLE);
					seekbartest.setText("Trượt để mở khóa");
				loadtime();
				} else {
					seekBar.setProgress(100);
					unlockHomeButton();

				}
			}
		});

//		btnUnlock = (Button) findViewById(R.id.btnUnlock);
//		btnUnlock.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// unlock home button and then screen on button press
//				unlockHomeButton();
//			}
//		});
	}

	// Handle events of calls and unlock screen if necessary
	private class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				unlockHomeButton();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	// Don't finish Activity on Back press
	@Override
	public void onBackPressed() {
		return;
	}

	// Handle button clicks
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (keyCode == KeyEvent.KEYCODE_POWER)
				|| (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
				|| (keyCode == KeyEvent.KEYCODE_CAMERA)) {
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_HOME)) {

			return true;
		}

		return false;

	}

	// handle the key press events here itself
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
				|| (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
			return false;
		}
		if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

			return true;
		}
		return false;
	}

	// Lock home button
	public void lockHomeButton() {
		mLockscreenUtils.lock(LockScreenActivity.this);
	}

	// Unlock home button and wait for its callback
	public void unlockHomeButton() {
		mLockscreenUtils.unlock();
	}

	// Simply unlock device when home button is successfully unlocked
	@Override
	public void onLockStatusChanged(boolean isLocked) {
		if (!isLocked) {
			unlockDevice();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unlockHomeButton();
	}

	@SuppressWarnings("deprecation")
	private void disableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.reenableKeyguard();
	}
	
	//Simply unlock device by finishing the activity
	private void unlockDevice()
	{
		finish();
	}

}