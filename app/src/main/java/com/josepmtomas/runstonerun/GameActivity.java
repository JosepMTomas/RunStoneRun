package com.josepmtomas.runstonerun;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.josepmtomas.runstonerun.objects.SplashScreen;

import java.util.List;

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

	private ForwardRenderer forwardRenderer;

	private MediaPlayer backgroundMusicPlayer;
	private Thread backgroundMusicThread;

	private MediaPlayer impactRockOnTreeSoundEffect;
	private MediaPlayer impactRockOnRockSoundEffect;
	private MediaPlayer treeFallingSoundEffect;

	private SharedPreferences sharedPreferences;

	SplashScreen splash;
	Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		loadShadedPreferences();

		glSurfaceView = new GLSurfaceView(this);
		//glSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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

		//

		boolean isTablet = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		Log.w(TAG, "isTablet = " + isTablet);

		//

		splash = new SplashScreen(getApplicationContext(), (int)width, (int)height);
		//loadingDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen); //Theme.DeviceDefault.NoActionBar.Fullscreen
		loadingDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
		loadingDialog.setContentView(splash);
		loadingDialog.show();

		if(sharedPreferences.getBoolean("SavedGame", false))
		{
			Toast toast = Toast.makeText(this, "Resuming previous game.", Toast.LENGTH_LONG);
			toast.show();
		}


		forwardRenderer = new ForwardRenderer(this, sharedPreferences, width, height);

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

		backgroundMusicPlayer = MediaPlayer.create(this, R.raw.art_of_gardens);
		backgroundMusicPlayer.setLooping(true);

		impactRockOnRockSoundEffect = MediaPlayer.create(this, R.raw.impact_rock_on_rock);
		impactRockOnTreeSoundEffect = MediaPlayer.create(this, R.raw.impact_rock_on_tree);
		treeFallingSoundEffect = MediaPlayer.create(this, R.raw.tree_falling);

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
		glSurfaceView.setRenderer(forwardRenderer);
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

						forwardRenderer.touch(currentX,currentY);

						//deferredRenderer.handleTouch();

						previousX = event.getX();
						previousY = event.getY();
					}
					else if (event.getAction() == MotionEvent.ACTION_UP)
					{
						//Log.d(TAG, "Released");
						//gameRenderer.releaseTouch();
						forwardRenderer.releaseTouch();
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
		//forwardPlusRenderer.setPause(true);

		//forwardPlusRenderer.deleteGL();
	}


	@Override
	protected void onStop() {
		super.onStop();

		forwardRenderer.setPause(true);
		Log.d(TAG, "<<<<< ON STOP >>>>>");
	}


	@Override
	protected void onDestroy()
	{
		forwardRenderer.onDestroy();

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onDestroy();

		backgroundMusicPlayer.release();
		impactRockOnRockSoundEffect.release();
		impactRockOnTreeSoundEffect.release();
		treeFallingSoundEffect.release();

		Log.d(TAG, "<<<<< ON DESTROY >>>>>");
	}


	@Override
	public void onBackPressed()
	{
		/*forwardRenderer.setPause(true);
		//super.onBackPressed();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit game");
		builder.setMessage("Are you sure you want to exit?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				// do something when the OK button is clicked
				GameActivity.super.onBackPressed();
				forwardRenderer.onDestroy();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();*/
		forwardRenderer.onBackPressed();
	}

	public void exit()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit game");
		builder.setMessage("Are you sure you want to exit?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				// do something when the OK button is clicked
				GameActivity.super.onBackPressed();
				forwardRenderer.onDestroy();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void loadShadedPreferences()
	{
		sharedPreferences = getSharedPreferences("RockGamePreferences", Context.MODE_PRIVATE);
	}


	public void launchDeveloperIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Developer");
		builder.setMessage("Send email?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// Build the intent
				Uri email = Uri.parse("mailto:josepmtomas90@gmail.com");
				Intent emailIntent = new Intent(Intent.ACTION_VIEW, email);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(emailIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(emailIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void launchComposerIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit Dan O'Connor's website?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// Build the intent
				Uri webPage = Uri.parse("http://danosongs.com/");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void launchSoundEffectsIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit www.freeSFX.co.uk ?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// Build the intent
				Uri webPage = Uri.parse("http://www.freeSFX.co.uk");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void touchedFontIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit Yamaoka Yasuhiro's website?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// Build the intent
				Uri webPage = Uri.parse("http://yoworks.com/index.html");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void touchedTexturesIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit Giles Hodges' website?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// Build the intent
				Uri webPage = Uri.parse("http://seamless-pixels.blogspot.com");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void touchedHughesMullerIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit Hughes Muller's website?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// Build the intent
				Uri webPage = Uri.parse("http://www.yughues-folio.com");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void touchedTomislavSpajicIntent()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Credits");
		builder.setMessage("Do you want to visit Tomislav Spajic's website?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// Build the intent
				Uri webPage = Uri.parse("http://www.clayman.se/");
				Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(webIntent);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				// do something when the Cancel button is clicked
			}
		});
		//myAlertDialog.show();
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void dismissDialog()
	{
		loadingDialog.dismiss();
	}
}
