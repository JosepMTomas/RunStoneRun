package com.josepmtomas.rockgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class GameActivity extends Activity
{
	private static final String TAG = "GameActivity";

	private GLSurfaceView glSurfaceView;

	private boolean soundEffectsEnabled = true;

	private float height;
	private float width;

	private float currentX;
	private float currentY;

	private float previousX;
	private float previousY;

	private DeferredRenderer deferredRenderer;
	private ForwardPlusRenderer forwardPlusRenderer;

	private MediaPlayer backgroundMusicPlayer;
	private Thread backgroundMusicThread;

	private MediaPlayer impactRockOnTreeSoundEffect;
	private MediaPlayer impactRockOnRockSoundEffect;
	private MediaPlayer treeFallingSoundEffect;

	private SharedPreferences sharedPreferences;

	private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		context = this;

		loadShadedPreferences();

		glSurfaceView = new GLSurfaceView(this);
		//glSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

		// Get the width and height of the window
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		this.width = (float)display.getWidth();
		this.height = (float)display.getHeight();

		Point realSizePoint = new Point();
		display.getRealSize(realSizePoint);

		// Check if the system supports OpenGL ES 3.0
		final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		//final boolean supportsES3 = configurationInfo.reqGlEsVersion >= 0x30000;
		//final GameRenderer gameRenderer = new GameRenderer(this, width, height);
		//deferredRenderer = new DeferredRenderer(this, width, height);
		forwardPlusRenderer = new ForwardPlusRenderer(this, sharedPreferences, width, height, 0.75f);

		Log.v("ACTIVITY", "This device supports OpenGL ES up to " + configurationInfo.getGlEsVersion());

		/*** AUDIO TEST **/

		/*public void onClick(View v) {
    new Thread(new Runnable() {
        public void run() {
            Bitmap b = loadImageFromNetwork("http://example.com/image.png");
            mImageView.setImageBitmap(b);
        }
    }).start();
}*/
		/*MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1);
mediaPlayer.start(); // no need to call prepare(); create() does that for you*/

		/*final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.the_metal);
		//mediaPlayer.start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				//AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				//am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				Log.e("Thread", "Ready to start");
				mediaPlayer.start();
			}
		}).start();*/

		backgroundMusicPlayer = MediaPlayer.create(context, R.raw.art_of_gardens);
		backgroundMusicPlayer.setLooping(true);

		impactRockOnRockSoundEffect = MediaPlayer.create(context, R.raw.impact_rock_on_rock);
		impactRockOnTreeSoundEffect = MediaPlayer.create(context, R.raw.impact_rock_on_tree);
		treeFallingSoundEffect = MediaPlayer.create(context, R.raw.tree_falling);

		backgroundMusicThread = new Thread(new Runnable() {
			@Override
			public void run() {
				//AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				//am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				Log.d("Background music thread", "Ready to start");
				backgroundMusicPlayer.start();
			}
		});

		backgroundMusicThread.start();

		// Shared preferences music settings
		enableBackgroundMusic(sharedPreferences.getBoolean("Music", true));
		enableSoundEffects(sharedPreferences.getBoolean("Effects", true));

		/*****************/

		glSurfaceView.setEGLContextClientVersion(3);
		//glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
		glSurfaceView.setRenderer(forwardPlusRenderer);
		//glSurfaceView.setRenderer(deferredRenderer);
		//glSurfaceView.setRenderer(gameRenderer);
		glSurfaceView.setPreserveEGLContextOnPause(true);

		glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event != null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						currentX = event.getX();
						currentY = event.getY();

						//Log.d(TAG, "X = " + currentX);

						/*if (currentX < width / 3.0f)
						{
							//gameRenderer.pressLeft();
							forwardPlusRenderer.pressLeft();
						}
						else if(currentX < (width / 3.0f)*2f )
						{
							//forwardPlusRenderer.scroll();
							forwardPlusRenderer.pressCenter(currentX, currentY);
							forwardPlusRenderer.setPause(false);
						}
						else
						{
							//gameRenderer.pressRight();
							forwardPlusRenderer.pressRight();
						}*/

						forwardPlusRenderer.touch(currentX,currentY);

						//deferredRenderer.handleTouch();

						previousX = event.getX();
						previousY = event.getY();
					}
					else if (event.getAction() == MotionEvent.ACTION_UP)
					{
						//Log.d(TAG, "Released");
						//gameRenderer.releaseTouch();
						forwardPlusRenderer.releaseTouch();
					}
					else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						final float deltaX = event.getX() - previousX;
						final float deltaY = event.getY() - previousY;
						previousX = event.getX();
						previousY = event.getY();

						glSurfaceView.queueEvent(new Runnable() {
							@Override
							public void run() {
								//gameRenderer.handleTouchDrag(deltaX, deltaY);
								//deferredRenderer.handleTouchDrag(deltaX, deltaY);
								//TODO(enable):
								forwardPlusRenderer.handleTouchDrag(deltaX, deltaY);
							}
						});
					}
					else if (event.getAction() == MotionEvent.ACTION_SCROLL)
					{
						forwardPlusRenderer.scroll();
					}
					return true;
				} else {
					return false;
				}
			}
		});

		setContentView(glSurfaceView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /**int id = item.getItemId();
        if (id == R.id.action_normals)
		{
			deferredRenderer.setDebugVisualization(0);
            return true;
        }
		if(id == R.id.action_diffuse)
		{
			deferredRenderer.setDebugVisualization(1);
			return true;
		}
		if(id == R.id.action_specular)
		{
			deferredRenderer.setDebugVisualization(2);
			return true;
		}
		if(id == R.id.action_shadows)
		{
			deferredRenderer.setDebugVisualization(3);
			return true;
		}
		if(id == R.id.action_depth)
		{
			deferredRenderer.setDebugVisualization(4);
			return true;
		}
		if(id == R.id.action_final)
		{
			deferredRenderer.setDebugVisualization(5);
			return true;
		}
		if(id == R.id.action_switch_post_process)
		{
			deferredRenderer.switchPostProcess();
			return true;
		}**/
        return super.onOptionsItemSelected(item);
    }

	public void playImpactRockOnRockSound()
	{
		if(soundEffectsEnabled)
		{
			new Thread(new Runnable() {
				@Override
				public void run() {
					//AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					//am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
					Log.d("SoundFX thread", "Ready to start: impact rock on rock sound effect");
					impactRockOnRockSoundEffect.start();
				}
			}).start();
		}
	}

	public void playImpactRockOnTreeSound()
	{
		if(soundEffectsEnabled)
		{
			new Thread(new Runnable() {
				@Override
				public void run() {
					//AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					//am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
					Log.d("SoundFX thread", "Ready to start: impact rock on tree sound effect");
					impactRockOnTreeSoundEffect.start();
				}
			}).start();
		}
	}

	public void playTreeFallingSound()
	{
		if(soundEffectsEnabled)
		{
			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d("SoundFX thread", "Ready to start: tree falling sound effect");
					treeFallingSoundEffect.start();
				}
			}).start();
		}
	}


	public void enableBackgroundMusic(boolean value)
	{
		if(value)
		{
			backgroundMusicPlayer.setVolume(1f,1f);
		}
		else
		{
			backgroundMusicPlayer.setVolume(0f,0f);
		}
	}


	public void enableSoundEffects(boolean value)
	{
		soundEffectsEnabled = value;
	}


	@Override
	protected void onResume() {
		super.onResume();

		backgroundMusicPlayer.start();
		//forwardPlusRenderer.setPause(false);
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG, "<<<<< ON PAUSE >>>>>");
		backgroundMusicPlayer.pause();
		forwardPlusRenderer.setPause(true);

		forwardPlusRenderer.deleteGL();
	}


	@Override
	protected void onStop() {
		super.onStop();

		Log.d(TAG, "<<<<< ON STOP >>>>>");
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		backgroundMusicPlayer.release();
		impactRockOnRockSoundEffect.release();
		impactRockOnTreeSoundEffect.release();
		treeFallingSoundEffect.release();

		Log.d(TAG, "<<<<< ON DESTROY >>>>>");
	}


	private void loadShadedPreferences()
	{
		sharedPreferences = getSharedPreferences("RockGamePreferences", Context.MODE_PRIVATE);
	}
}
