package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.TextureHelper;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 24/12/2014.
 */
public class OptionsMenu
{
	//
	private GameActivity parent;
	private Context context;
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;

	// Preferences
	private int resolution;
	private int postProcessQuality;
	private boolean musicEnabled;
	private boolean effectsEnabled;

	// State
	private int currentState;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Geometry
	private int uiPanelVaoHandle;
	private int ui9PatchVaoHandle;

	// Menu common parameters
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Background
	private float[] background9PatchScale = {1f,1f};
	private float[] background9PatchPosition = {0f,0f};
	private float[] background9PatchCurrentScale = new float[2];

	// Titles
	private float[] optionsTitleScale = new float[2];
	private float[] optionsTitleCurrentScale = new float[2];
	private float[] optionsTitlePosition = new float[2];
	private float[] optionsTitleCurrentPosition = new float[2];
	private float[] screenResolutionTitleScale = new float[2];
	private float[] screenResolutionTitleCurrentScale = new float[2];
	private float[] screenResolutionTitlePosition = new float[2];
	private float[] screenResolutionTitleCurrentPosition = new float[2];
	private float[] postProcessDetailTitleScale = new float[2];
	private float[] postProcessDetailTitleCurrentScale = new float[2];
	private float[] postProcessDetailTitlePosition = new float[2];
	private float[] postProcessDetailTitleCurrentPosition = new float[2];
	private float[] musicTitleScale = new float[2];
	private float[] musicTitleCurrentScale = new float[2];
	private float[] musicTitlePosition = new float[2];
	private float[] musicTitleCurrentPosition = new float[2];
	private float[] effectsTitleScale = new float[2];
	private float[] effectsTitleCurrentScale = new float[2];
	private float[] effectsTitlePosition = new float[2];
	private float[] effectsTitleCurrentPosition = new float[2];

	// Resolution percentage buttons
	private float[] resolutionPercentageButtonScale = new float[2];
	private float[] resolutionPercentageButtonCurrentScale = new float[2];
	private float[] resolutionPercentageButton25Position = new float[2];
	private float[] resolutionPercentageButton50Position = new float[2];
	private float[] resolutionPercentageButton75Position = new float[2];
	private float[] resolutionPercentageButton100Position = new float[2];
	private float[] resolutionPercentageButton25CurrentPosition = new float[2];
	private float[] resolutionPercentageButton50CurrentPosition = new float[2];
	private float[] resolutionPercentageButton75CurrentPosition = new float[2];
	private float[] resolutionPercentageButton100CurrentPosition = new float[2];
	private float[] resolutionPercentageButton25Limits = new float[4];
	private float[] resolutionPercentageButton50Limits = new float[4];
	private float[] resolutionPercentageButton75Limits = new float[4];
	private float[] resolutionPercentageButton100Limits = new float[4];

	// Post process detail buttons
	private float[] postProcessDetailButtonScale = new float[2];
	private float[] postProcessDetailButtonCurrentScale = new float[2];
	private float[] postProcessNoDetailButtonPosition = new float[2];
	private float[] postProcessLowDetailButtonPosition = new float[2];
	private float[] postProcessHighDetailButtonPosition = new float[2];
	private float[] postProcessNoDetailButtonCurrentPosition = new float[2];
	private float[] postProcessLowDetailButtonCurrentPosition = new float[2];
	private float[] postProcessHighDetailButtonCurrentPosition = new float[2];
	private float[] postProcessNoDetailButtonLimits = new float[4];
	private float[] postProcessLowDetailButtonLimits = new float[4];
	private float[] postProcessHighDetailButtonLimits = new float[4];

	// Music buttons
	private float[] musicButtonScale = new float[2];
	private float[] musicButtonCurrentScale = new float[2];
	private float[] musicEnableButtonPosition = new float[2];
	private float[] musicDisableButtonPosition = new float[2];
	private float[] musicEnableButtonCurrentPosition = new float[2];
	private float[] musicDisableButtonCurrentPosition = new float[2];
	private float[] musicEnableButtonLimits = new float[4];
	private float[] musicDisableButtonLimits = new float[4];

	// Sound effects buttons
	private float[] effectsButtonScale = new float[2];
	private float[] effectsButtonCurrentScale = new float[2];
	private float[] effectsEnableButtonPosition = new float[2];
	private float[] effectsDisableButtonPosition = new float[2];
	private float[] effectsEnableButtonCurrentPosition = new float[2];
	private float[] effectsDisableButtonCurrentPosition = new float[2];
	private float[] effectsEnableButtonLimits = new float[4];
	private float[] effectsDisableButtonLimits = new float[4];

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonCurrentScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonCurrentPosition = new float[2];
	private float[] backButtonLimits = new float[4];

	// Textures
	private int background9PatchTexture;

	private int optionsTitleTexture;
	private int screenResolutionTitleTexture;
	private int postProcessDetailTitleTexture;
	private int musicTitleTexture;
	private int effectsTitleTexture;

	private int backButtonCurrentTexture;
	private int backButtonIdleTexture;
	private int backButtonSelectedTexture;

	private int resolutionPercentageButton25CurrentTexture;
	private int resolutionPercentageButton50CurrentTexture;
	private int resolutionPercentageButton75CurrentTexture;
	private int resolutionPercentageButton100CurrentTexture;
	private int resolutionPercentageButton25IdleTexture;
	private int resolutionPercentageButton50IdleTexture;
	private int resolutionPercentageButton75IdleTexture;
	private int resolutionPercentageButton100IdleTexture;
	private int resolutionPercentageButton25SelectedTexture;
	private int resolutionPercentageButton50SelectedTexture;
	private int resolutionPercentageButton75SelectedTexture;
	private int resolutionPercentageButton100SelectedTexture;

	private int postProcessNoDetailButtonCurrentTexture;
	private int postProcessNoDetailButtonIdleTexture;
	private int postProcessNoDetailButtonSelectedTexture;

	private int postProcessLowDetailButtonCurrentTexture;
	private int postProcessLowDetailButtonIdleTexture;
	private int postProcessLowDetailButtonSelectedTexture;

	private int postProcessHighDetailButtonCurrentTexture;
	private int postProcessHighDetailButtonIdleTexture;
	private int postProcessHighDetailButtonSelectedTexture;

	private int soundEnableButtonIdleTexture;
	private int soundEnableButtonSelectedTexture;
	private int soundDisableButtonIdleTexture;
	private int soundDisableButtonSelectedTexture;
	private int musicEnableButtonCurrentTexture;
	private int musicDisableButtonCurrentTexture;
	private int effectsEnableButtonCurrentTexture;
	private int effectsDisableButtonCurrentTexture;

	// Shared preferences
	private SharedPreferences sharedPreferences;


	public OptionsMenu(GameActivity parent, ForwardPlusRenderer renderer, SharedPreferences sharedPreferences, UIPanelProgram panelProgram, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.sharedPreferences = sharedPreferences;
		this.uiPanelProgram = panelProgram;
		this.renderer = renderer;
		this.currentState = UI_STATE_NOT_VISIBLE;

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchVaoHandle = UIHelper.make9PatchPanel(screenWidth, screenHeight, screenHeight * 0.1f, UI_BASE_CENTER_CENTER);

		createMatrices(screenWidth, screenHeight);
		loadTextures(context);
		setPositions(screenWidth, screenHeight);
		setCurrentPositions();
		loadPreferences();
	}


	private void createMatrices(float screenWidth, float screenHeight)
	{
		float right = screenWidth * 0.5f;
		float left = -right;
		float top = screenHeight * 0.5f;
		float bottom = -top;
		float near = 0.5f;
		float far = 10f;

		// Initialize view matrix
		setIdentityM(view, 0);
		setLookAtM(view, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);

		// Initialize projection matrix
		setIdentityM(projection, 0);
		orthoM(projection, 0, left, right, bottom, top, near, far);

		// Calculate view x projection matrix
		multiplyMM(viewProjection, 0, projection, 0, view, 0);
	}


	private void loadTextures(Context context)
	{
		background9PatchTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/9patch.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		optionsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/options_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		screenResolutionTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/screen_resolution_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessDetailTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/post_process_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		musicTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/music_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		effectsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/effects_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		backButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		backButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resolutionPercentageButton25IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_25_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_50_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_75_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_100_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resolutionPercentageButton25SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_25_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_50_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_75_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_100_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		postProcessNoDetailButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/no_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessLowDetailButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/low_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessHighDetailButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/high_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		postProcessNoDetailButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/no_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessLowDetailButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/low_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessHighDetailButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/high_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		soundEnableButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/sound_enabled_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		soundEnableButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/sound_enabled_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		soundDisableButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/sound_disabled_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		soundDisableButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/sound_disabled_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resetBackButtonCurrentTexture();
		resetResolutionPercentageCurrentTextures();
		resetPostProcessCurrentTextures();
		resetMusicCurrentTextures();
		resetEffectsCurrentTextures();
	}


	private void loadPreferences()
	{
		resolution = sharedPreferences.getInt("ScreenResolution", 0);
		postProcessQuality = sharedPreferences.getInt("PostProcessQuality", 0);
		musicEnabled = sharedPreferences.getBoolean("Music", true);
		effectsEnabled = sharedPreferences.getBoolean("Effects", true);

		switch(resolution)
		{
			case 0:
				touchedResolutionPercentageButton100();
				break;
			case 1:
				touchedResolutionPercentageButton75();
				break;
			case 2:
				touchedResolutionPercentageButton50();
				break;
			default:
				touchedResolutionPercentageButton25();
				break;
		}

		switch(postProcessQuality)
		{
			case 0:
				touchedNoPostProcessDetailButton();
				break;
			case 1:
				touchedLowPostProcessDetailButton();
				break;
			default:
				touchedHighPostProcessDetailButton();
				break;
		}

		if(musicEnabled)
		{
			touchedMusicEnableButton();
		}
		else
		{
			touchedMusicDisableButton();
		}

		if(effectsEnabled)
		{
			touchedEffectsEnableButton();
		}
		else
		{
			touchedEffectsDisableButton();
		}
	}


	private void savePreferences()
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt("ScreenResolution", resolution);
		editor.putInt("PostProcessQuality", postProcessQuality);
		editor.putBoolean("Music", musicEnabled);
		editor.putBoolean("Effects", effectsEnabled);
		editor.apply();
	}


	private void resetBackButtonCurrentTexture()
	{
		backButtonCurrentTexture = backButtonIdleTexture;
	}


	private void resetResolutionPercentageCurrentTextures()
	{
		resolutionPercentageButton25CurrentTexture = resolutionPercentageButton25IdleTexture;
		resolutionPercentageButton50CurrentTexture = resolutionPercentageButton50IdleTexture;
		resolutionPercentageButton75CurrentTexture = resolutionPercentageButton75IdleTexture;
		resolutionPercentageButton100CurrentTexture = resolutionPercentageButton100IdleTexture;
	}


	private void resetPostProcessCurrentTextures()
	{
		postProcessNoDetailButtonCurrentTexture = postProcessNoDetailButtonIdleTexture;
		postProcessLowDetailButtonCurrentTexture = postProcessLowDetailButtonIdleTexture;
		postProcessHighDetailButtonCurrentTexture = postProcessHighDetailButtonIdleTexture;
	}


	private void resetMusicCurrentTextures()
	{
		musicEnableButtonCurrentTexture = soundEnableButtonIdleTexture;
		musicDisableButtonCurrentTexture = soundDisableButtonIdleTexture;
	}


	private void resetEffectsCurrentTextures()
	{
		effectsEnableButtonCurrentTexture = soundEnableButtonIdleTexture;
		effectsDisableButtonCurrentTexture = soundDisableButtonIdleTexture;
	}


	private void setPositions(float screenWidth, float screenHeight)
	{
		//float optionButtonWidth = screenWidth * 0.175f;
		float optionButtonHeight = screenHeight * 0.1f;
		float optionButtonWidth = optionButtonHeight * 3f;

		float optionButtonWidthHalf = optionButtonWidth * 0.5f;
		float optionButtonHeightHalf = optionButtonHeight * 0.5f;

		float soundButtonSize = optionButtonHeight * 1.5f;
		float soundButtonSizeHalf = soundButtonSize * 0.5f;

		// Scales

		optionsTitleScale[0] = optionButtonHeight * 10f;
		optionsTitleScale[1] = optionButtonHeight * 1f;

		screenResolutionTitleScale[0] = optionButtonHeight * 7f;
		screenResolutionTitleScale[1] = optionButtonHeight;

		resolutionPercentageButtonScale[0] = optionButtonWidth;
		resolutionPercentageButtonScale[1] = optionButtonHeight;

		postProcessDetailTitleScale[0] = optionButtonHeight * 8f;
		postProcessDetailTitleScale[1] = optionButtonHeight;

		postProcessDetailButtonScale[0] = optionButtonWidth;
		postProcessDetailButtonScale[1] = optionButtonHeight;

		musicTitleScale[0] = optionButtonHeight * 2f;
		musicTitleScale[1] = optionButtonHeight;

		musicButtonScale[0] = optionButtonHeight * 1.5f;
		musicButtonScale[1] = optionButtonHeight * 1.5f;

		effectsTitleScale[0] = optionButtonHeight * 3f;
		effectsTitleScale[1] = optionButtonHeight;
		effectsButtonScale[0] = optionButtonHeight * 1.5f;
		effectsButtonScale[1] = optionButtonHeight * 1.5f;

		backButtonScale[0] = optionButtonWidth;
		backButtonScale[1] = optionButtonHeight;

		// Positions

		optionsTitlePosition[0] = 0f;
		optionsTitlePosition[1] = optionButtonHeight * 4f;

		screenResolutionTitlePosition[0] = 0f;
		screenResolutionTitlePosition[1] = optionButtonHeight * 3f;

		resolutionPercentageButton25Position[0] = optionButtonWidth * -1.5f;
		resolutionPercentageButton25Position[1] = optionButtonHeight * 2.25f;
		resolutionPercentageButton50Position[0] = optionButtonWidth * -0.5f;
		resolutionPercentageButton50Position[1] = optionButtonHeight * 2.25f;
		resolutionPercentageButton75Position[0] = optionButtonWidth * 0.5f;
		resolutionPercentageButton75Position[1] = optionButtonHeight * 2.25f;
		resolutionPercentageButton100Position[0] = optionButtonWidth * 1.5f;
		resolutionPercentageButton100Position[1] = optionButtonHeight * 2.25f;


		postProcessDetailTitlePosition[0] = 0f;
		postProcessDetailTitlePosition[1] = optionButtonHeight * 1.25f;

		postProcessNoDetailButtonPosition[0] = -optionButtonWidth;
		postProcessNoDetailButtonPosition[1] = optionButtonHeight * 0.5f;
		postProcessLowDetailButtonPosition[0] = 0f;
		postProcessLowDetailButtonPosition[1] = optionButtonHeight * 0.5f;
		postProcessHighDetailButtonPosition[0] = optionButtonWidth;
		postProcessHighDetailButtonPosition[1] = optionButtonHeight * 0.5f;



		musicTitlePosition[0] = -optionButtonWidth;
		musicTitlePosition[1] = optionButtonHeight * -1.0f;

		musicEnableButtonPosition[0] = 0f;
		musicEnableButtonPosition[1] = optionButtonHeight * -1.0f;

		musicDisableButtonPosition[0] = optionButtonHeight * 2.0f;
		musicDisableButtonPosition[1] = optionButtonHeight * -1.0f;


		effectsTitlePosition[0] = -optionButtonWidth;
		effectsTitlePosition[1] = optionButtonHeight * -2.5f;

		effectsEnableButtonPosition[0] = 0f;
		effectsEnableButtonPosition[1] = optionButtonHeight * -2.5f;

		effectsDisableButtonPosition[0] = optionButtonHeight * 2.0f;
		effectsDisableButtonPosition[1] = optionButtonHeight * -2.5f;

		backButtonPosition[0] = 0f;
		backButtonPosition[1] = -optionButtonHeight * 4f;

		// Limits (left-right-bottom-top)
		resolutionPercentageButton25Limits[0] = resolutionPercentageButton25Position[0] - optionButtonWidthHalf;
		resolutionPercentageButton25Limits[1] = resolutionPercentageButton25Position[0] + optionButtonWidthHalf;
		resolutionPercentageButton25Limits[2] = resolutionPercentageButton25Position[1] - optionButtonHeightHalf;
		resolutionPercentageButton25Limits[3] = resolutionPercentageButton25Position[1] + optionButtonHeightHalf;

		resolutionPercentageButton50Limits[0] = resolutionPercentageButton50Position[0] - optionButtonWidthHalf;
		resolutionPercentageButton50Limits[1] = resolutionPercentageButton50Position[0] + optionButtonWidthHalf;
		resolutionPercentageButton50Limits[2] = resolutionPercentageButton50Position[1] - optionButtonHeightHalf;
		resolutionPercentageButton50Limits[3] = resolutionPercentageButton50Position[1] + optionButtonHeightHalf;

		resolutionPercentageButton75Limits[0] = resolutionPercentageButton75Position[0] - optionButtonWidthHalf;
		resolutionPercentageButton75Limits[1] = resolutionPercentageButton75Position[0] + optionButtonWidthHalf;
		resolutionPercentageButton75Limits[2] = resolutionPercentageButton75Position[1] - optionButtonHeightHalf;
		resolutionPercentageButton75Limits[3] = resolutionPercentageButton75Position[1] + optionButtonHeightHalf;

		resolutionPercentageButton100Limits[0] = resolutionPercentageButton100Position[0] - optionButtonWidthHalf;
		resolutionPercentageButton100Limits[1] = resolutionPercentageButton100Position[0] + optionButtonWidthHalf;
		resolutionPercentageButton100Limits[2] = resolutionPercentageButton100Position[1] - optionButtonHeightHalf;
		resolutionPercentageButton100Limits[3] = resolutionPercentageButton100Position[1] + optionButtonHeightHalf;

		postProcessNoDetailButtonLimits[0] = postProcessNoDetailButtonPosition[0] - optionButtonWidthHalf;
		postProcessNoDetailButtonLimits[1] = postProcessNoDetailButtonPosition[0] + optionButtonWidthHalf;
		postProcessNoDetailButtonLimits[2] = postProcessNoDetailButtonPosition[1] - optionButtonHeightHalf;
		postProcessNoDetailButtonLimits[3] = postProcessNoDetailButtonPosition[1] + optionButtonHeightHalf;

		postProcessLowDetailButtonLimits[0] = postProcessLowDetailButtonPosition[0] - optionButtonWidthHalf;
		postProcessLowDetailButtonLimits[1] = postProcessLowDetailButtonPosition[0] + optionButtonWidthHalf;
		postProcessLowDetailButtonLimits[2] = postProcessLowDetailButtonPosition[1] - optionButtonHeightHalf;
		postProcessLowDetailButtonLimits[3] = postProcessLowDetailButtonPosition[1] + optionButtonHeightHalf;

		postProcessHighDetailButtonLimits[0] = postProcessHighDetailButtonPosition[0] - optionButtonWidthHalf;
		postProcessHighDetailButtonLimits[1] = postProcessHighDetailButtonPosition[0] + optionButtonWidthHalf;
		postProcessHighDetailButtonLimits[2] = postProcessHighDetailButtonPosition[1] - optionButtonHeightHalf;
		postProcessHighDetailButtonLimits[3] = postProcessHighDetailButtonPosition[1] + optionButtonHeightHalf;

		musicEnableButtonLimits[0] = musicEnableButtonPosition[0] - soundButtonSizeHalf;
		musicEnableButtonLimits[1] = musicEnableButtonPosition[0] + soundButtonSizeHalf;
		musicEnableButtonLimits[2] = musicEnableButtonPosition[1] - soundButtonSizeHalf;
		musicEnableButtonLimits[3] = musicEnableButtonPosition[1] + soundButtonSizeHalf;

		musicDisableButtonLimits[0] = musicDisableButtonPosition[0] - soundButtonSizeHalf;
		musicDisableButtonLimits[1] = musicDisableButtonPosition[0] + soundButtonSizeHalf;
		musicDisableButtonLimits[2] = musicDisableButtonPosition[1] - soundButtonSizeHalf;
		musicDisableButtonLimits[3] = musicDisableButtonPosition[1] + soundButtonSizeHalf;

		effectsEnableButtonLimits[0] = effectsEnableButtonPosition[0] - soundButtonSizeHalf;
		effectsEnableButtonLimits[1] = effectsEnableButtonPosition[0] + soundButtonSizeHalf;
		effectsEnableButtonLimits[2] = effectsEnableButtonPosition[1] - soundButtonSizeHalf;
		effectsEnableButtonLimits[3] = effectsEnableButtonPosition[1] + soundButtonSizeHalf;

		effectsDisableButtonLimits[0] = effectsDisableButtonPosition[0] - soundButtonSizeHalf;
		effectsDisableButtonLimits[1] = effectsDisableButtonPosition[0] + soundButtonSizeHalf;
		effectsDisableButtonLimits[2] = effectsDisableButtonPosition[1] - soundButtonSizeHalf;
		effectsDisableButtonLimits[3] = effectsDisableButtonPosition[1] + soundButtonSizeHalf;

		backButtonLimits[0] = backButtonPosition[0] - optionButtonWidthHalf;
		backButtonLimits[1] = backButtonPosition[0] + optionButtonWidthHalf;
		backButtonLimits[2] = backButtonPosition[1] - optionButtonHeightHalf;
		backButtonLimits[3] = backButtonPosition[1] + optionButtonHeightHalf;
	}


	private void setCurrentPositions()
	{
		background9PatchCurrentScale[0] = 1f;
		background9PatchCurrentScale[1] = 1f;

		optionsTitleCurrentScale[0] = optionsTitleScale[0];
		optionsTitleCurrentScale[1] = optionsTitleScale[1];

		screenResolutionTitleCurrentScale[0] = screenResolutionTitleScale[0];
		screenResolutionTitleCurrentScale[1] = screenResolutionTitleScale[1];

		resolutionPercentageButtonCurrentScale[0] = resolutionPercentageButtonScale[0];
		resolutionPercentageButtonCurrentScale[1] = resolutionPercentageButtonScale[1];

		postProcessDetailTitleCurrentScale[0] = postProcessDetailTitleScale[0];
		postProcessDetailTitleCurrentScale[1] = postProcessDetailTitleScale[1];

		postProcessDetailButtonCurrentScale[0] = postProcessDetailButtonScale[0];
		postProcessDetailButtonCurrentScale[1] = postProcessDetailButtonScale[1];

		musicTitleCurrentScale[0] = musicTitleScale[0];
		musicTitleCurrentScale[1] = musicTitleScale[1];

		musicButtonCurrentScale[0] = musicButtonScale[0];
		musicButtonCurrentScale[1] = musicButtonScale[1];

		effectsTitleCurrentScale[0] = effectsTitleScale[0];
		effectsTitleCurrentScale[1] = effectsTitleScale[1];

		effectsButtonCurrentScale[0] = effectsButtonScale[0];
		effectsButtonCurrentScale[1] = effectsButtonScale[1];

		backButtonCurrentScale[0] = backButtonScale[0];
		backButtonCurrentScale[1] = backButtonScale[1];

		// Positions
		optionsTitleCurrentPosition[0] = optionsTitlePosition[0];
		optionsTitleCurrentPosition[1] = optionsTitlePosition[1];

		screenResolutionTitleCurrentPosition[0] = screenResolutionTitlePosition[0];
		screenResolutionTitleCurrentPosition[1] = screenResolutionTitlePosition[1];

		resolutionPercentageButton25CurrentPosition[0] = resolutionPercentageButton25Position[0];
		resolutionPercentageButton25CurrentPosition[1] = resolutionPercentageButton25Position[1];
		resolutionPercentageButton50CurrentPosition[0] = resolutionPercentageButton50Position[0];
		resolutionPercentageButton50CurrentPosition[1] = resolutionPercentageButton50Position[1];
		resolutionPercentageButton75CurrentPosition[0] = resolutionPercentageButton75Position[0];
		resolutionPercentageButton75CurrentPosition[1] = resolutionPercentageButton75Position[1];
		resolutionPercentageButton100CurrentPosition[0] = resolutionPercentageButton100Position[0];
		resolutionPercentageButton100CurrentPosition[1] = resolutionPercentageButton100Position[1];

		postProcessDetailTitleCurrentPosition[0] = postProcessDetailTitlePosition[0];
		postProcessDetailTitleCurrentPosition[1] = postProcessDetailTitlePosition[1];

		postProcessNoDetailButtonCurrentPosition[0] = postProcessNoDetailButtonPosition[0];
		postProcessNoDetailButtonCurrentPosition[1] = postProcessNoDetailButtonPosition[1];
		postProcessLowDetailButtonCurrentPosition[0] = postProcessLowDetailButtonPosition[0];
		postProcessLowDetailButtonCurrentPosition[1] = postProcessLowDetailButtonPosition[1];
		postProcessHighDetailButtonCurrentPosition[0] = postProcessHighDetailButtonPosition[0];
		postProcessHighDetailButtonCurrentPosition[1] = postProcessHighDetailButtonPosition[1];

		musicTitleCurrentPosition[0] = musicTitlePosition[0];
		musicTitleCurrentPosition[1] = musicTitlePosition[1];

		musicEnableButtonCurrentPosition[0] = musicEnableButtonPosition[0];
		musicEnableButtonCurrentPosition[1] = musicEnableButtonPosition[1];

		musicDisableButtonCurrentPosition[0] = musicDisableButtonPosition[0];
		musicDisableButtonCurrentPosition[1] = musicDisableButtonPosition[1];

		effectsTitleCurrentPosition[0] = effectsTitlePosition[0];
		effectsTitleCurrentPosition[1] = effectsTitlePosition[1];

		effectsEnableButtonCurrentPosition[0] = effectsEnableButtonPosition[0];
		effectsEnableButtonCurrentPosition[1] = effectsEnableButtonPosition[1];

		effectsDisableButtonCurrentPosition[0] = effectsDisableButtonPosition[0];
		effectsDisableButtonCurrentPosition[1] = effectsDisableButtonPosition[1];

		backButtonCurrentPosition[0] = backButtonPosition[0];
		backButtonCurrentPosition[1] = backButtonPosition[1];
	}


	public void update(float deltaTime)
	{
		if(currentState == UI_STATE_APPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = menuTimer / menuAppearTime;

			if(menuTimer >= menuAppearTime)
			{
				currentState = UI_STATE_VISIBLE;
				menuOpacity = 1f;
				menuTimer = 0f;
				renderer.changedToOptionMenu();
			}

			background9PatchCurrentScale[0] = lerp(0f, background9PatchScale[0], menuOpacity);
			background9PatchCurrentScale[1] = lerp(0f, background9PatchScale[1], menuOpacity);
			optionsTitleCurrentScale[0] = lerp(0f, optionsTitleScale[0], menuOpacity);
			optionsTitleCurrentScale[1] = lerp(0f, optionsTitleScale[1], menuOpacity);
			screenResolutionTitleCurrentScale[0] = lerp(0f, screenResolutionTitleScale[0], menuOpacity);
			screenResolutionTitleCurrentScale[1] = lerp(0f, screenResolutionTitleScale[1], menuOpacity);
			resolutionPercentageButtonCurrentScale[0] = lerp(0f, resolutionPercentageButtonScale[0], menuOpacity);
			resolutionPercentageButtonCurrentScale[1] = lerp(0f, resolutionPercentageButtonScale[1], menuOpacity);
			postProcessDetailTitleCurrentScale[0] = lerp(0f, postProcessDetailTitleScale[0], menuOpacity);
			postProcessDetailTitleCurrentScale[1] = lerp(0f, postProcessDetailTitleScale[1], menuOpacity);
			postProcessDetailButtonCurrentScale[0] = lerp(0f, postProcessDetailButtonScale[0], menuOpacity);
			postProcessDetailButtonCurrentScale[1] = lerp(0f, postProcessDetailButtonScale[1], menuOpacity);
			musicTitleCurrentScale[0] = lerp(0f, musicTitleScale[0], menuOpacity);
			musicTitleCurrentScale[1] = lerp(0f, musicTitleScale[1], menuOpacity);
			musicButtonCurrentScale[0] = lerp(0f, musicButtonScale[0], menuOpacity);
			musicButtonCurrentScale[1] = lerp(0f, musicButtonScale[1], menuOpacity);
			effectsTitleCurrentScale[0] = lerp(0f, effectsTitleScale[0], menuOpacity);
			effectsTitleCurrentScale[1] = lerp(0f, effectsTitleScale[1], menuOpacity);
			effectsButtonCurrentScale[0] = lerp(0f, effectsButtonScale[0], menuOpacity);
			effectsButtonCurrentScale[1] = lerp(0f, effectsButtonScale[1], menuOpacity);
			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);

			optionsTitleCurrentPosition[0] = lerp(0f, optionsTitlePosition[0], menuOpacity);
			optionsTitleCurrentPosition[1] = lerp(0f, optionsTitlePosition[1], menuOpacity);
			screenResolutionTitleCurrentPosition[0] = lerp(0f, screenResolutionTitlePosition[0], menuOpacity);
			screenResolutionTitleCurrentPosition[1] = lerp(0f, screenResolutionTitlePosition[1], menuOpacity);
			resolutionPercentageButton25CurrentPosition[0] = lerp(0f, resolutionPercentageButton25Position[0], menuOpacity);
			resolutionPercentageButton25CurrentPosition[1] = lerp(0f, resolutionPercentageButton25Position[1], menuOpacity);
			resolutionPercentageButton50CurrentPosition[0] = lerp(0f, resolutionPercentageButton50Position[0], menuOpacity);
			resolutionPercentageButton50CurrentPosition[1] = lerp(0f, resolutionPercentageButton50Position[1], menuOpacity);
			resolutionPercentageButton75CurrentPosition[0] = lerp(0f, resolutionPercentageButton75Position[0], menuOpacity);
			resolutionPercentageButton75CurrentPosition[1] = lerp(0f, resolutionPercentageButton75Position[1], menuOpacity);
			resolutionPercentageButton100CurrentPosition[0] = lerp(0f, resolutionPercentageButton100Position[0], menuOpacity);
			resolutionPercentageButton100CurrentPosition[1] = lerp(0f, resolutionPercentageButton100Position[1], menuOpacity);
			postProcessDetailTitleCurrentPosition[0] = lerp(0f, postProcessDetailTitlePosition[0], menuOpacity);
			postProcessDetailTitleCurrentPosition[1] = lerp(0f, postProcessDetailTitlePosition[1], menuOpacity);
			postProcessNoDetailButtonCurrentPosition[0] = lerp(0f, postProcessNoDetailButtonPosition[0], menuOpacity);
			postProcessNoDetailButtonCurrentPosition[1] = lerp(0f, postProcessNoDetailButtonPosition[1], menuOpacity);
			postProcessLowDetailButtonCurrentPosition[0] = lerp(0f, postProcessLowDetailButtonPosition[0], menuOpacity);
			postProcessLowDetailButtonCurrentPosition[1] = lerp(0f, postProcessLowDetailButtonPosition[1], menuOpacity);
			postProcessHighDetailButtonCurrentPosition[0] = lerp(0f, postProcessHighDetailButtonPosition[0], menuOpacity);
			postProcessHighDetailButtonCurrentPosition[1] = lerp(0f, postProcessHighDetailButtonPosition[1], menuOpacity);
			musicTitleCurrentPosition[0] = lerp(0f, musicTitlePosition[0], menuOpacity);
			musicTitleCurrentPosition[1] = lerp(0f, musicTitlePosition[1], menuOpacity);
			musicEnableButtonCurrentPosition[0] = lerp(0f, musicEnableButtonPosition[0], menuOpacity);
			musicEnableButtonCurrentPosition[1] = lerp(0f, musicEnableButtonPosition[1], menuOpacity);
			musicDisableButtonCurrentPosition[0] = lerp(0f, musicDisableButtonPosition[0], menuOpacity);
			musicDisableButtonCurrentPosition[1] = lerp(0f, musicDisableButtonPosition[1], menuOpacity);
			effectsTitleCurrentPosition[0] = lerp(0f, effectsTitlePosition[0], menuOpacity);
			effectsTitleCurrentPosition[1] = lerp(0f, effectsTitlePosition[1], menuOpacity);
			effectsEnableButtonCurrentPosition[0] = lerp(0f, effectsEnableButtonPosition[0], menuOpacity);
			effectsEnableButtonCurrentPosition[1] = lerp(0f, effectsEnableButtonPosition[1], menuOpacity);
			effectsDisableButtonCurrentPosition[0] = lerp(0f, effectsDisableButtonPosition[0], menuOpacity);
			effectsDisableButtonCurrentPosition[1] = lerp(0f, effectsDisableButtonPosition[1], menuOpacity);
			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			if(menuTimer >= menuDisappearTime)
			{
				currentState = UI_STATE_NOT_VISIBLE;
				menuOpacity = 0f;
				menuTimer = 0;
				renderer.changedFromOptionsMenuToMainMenu();
				resetBackButtonCurrentTexture();
			}

			background9PatchCurrentScale[0] = lerp(0f, background9PatchScale[0], menuOpacity);
			background9PatchCurrentScale[1] = lerp(0f, background9PatchScale[1], menuOpacity);
			optionsTitleCurrentScale[0] = lerp(0f, optionsTitleScale[0], menuOpacity);
			optionsTitleCurrentScale[1] = lerp(0f, optionsTitleScale[1], menuOpacity);
			screenResolutionTitleCurrentScale[0] = lerp(0f, screenResolutionTitleScale[0], menuOpacity);
			screenResolutionTitleCurrentScale[1] = lerp(0f, screenResolutionTitleScale[1], menuOpacity);
			resolutionPercentageButtonCurrentScale[0] = lerp(0f, resolutionPercentageButtonScale[0], menuOpacity);
			resolutionPercentageButtonCurrentScale[1] = lerp(0f, resolutionPercentageButtonScale[1], menuOpacity);
			postProcessDetailTitleCurrentScale[0] = lerp(0f, postProcessDetailTitleScale[0], menuOpacity);
			postProcessDetailTitleCurrentScale[1] = lerp(0f, postProcessDetailTitleScale[1], menuOpacity);
			postProcessDetailButtonCurrentScale[0] = lerp(0f, postProcessDetailButtonScale[0], menuOpacity);
			postProcessDetailButtonCurrentScale[1] = lerp(0f, postProcessDetailButtonScale[1], menuOpacity);
			musicTitleCurrentScale[0] = lerp(0f, musicTitleScale[0], menuOpacity);
			musicTitleCurrentScale[1] = lerp(0f, musicTitleScale[1], menuOpacity);
			musicButtonCurrentScale[0] = lerp(0f, musicButtonScale[0], menuOpacity);
			musicButtonCurrentScale[1] = lerp(0f, musicButtonScale[1], menuOpacity);
			effectsTitleCurrentScale[0] = lerp(0f, effectsTitleScale[0], menuOpacity);
			effectsTitleCurrentScale[1] = lerp(0f, effectsTitleScale[1], menuOpacity);
			effectsButtonCurrentScale[0] = lerp(0f, effectsButtonScale[0], menuOpacity);
			effectsButtonCurrentScale[1] = lerp(0f, effectsButtonScale[1], menuOpacity);
			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);

			optionsTitleCurrentPosition[0] = lerp(0f, optionsTitlePosition[0], menuOpacity);
			optionsTitleCurrentPosition[1] = lerp(0f, optionsTitlePosition[1], menuOpacity);
			screenResolutionTitleCurrentPosition[0] = lerp(0f, screenResolutionTitlePosition[0], menuOpacity);
			screenResolutionTitleCurrentPosition[1] = lerp(0f, screenResolutionTitlePosition[1], menuOpacity);
			resolutionPercentageButton25CurrentPosition[0] = lerp(0f, resolutionPercentageButton25Position[0], menuOpacity);
			resolutionPercentageButton25CurrentPosition[1] = lerp(0f, resolutionPercentageButton25Position[1], menuOpacity);
			resolutionPercentageButton50CurrentPosition[0] = lerp(0f, resolutionPercentageButton50Position[0], menuOpacity);
			resolutionPercentageButton50CurrentPosition[1] = lerp(0f, resolutionPercentageButton50Position[1], menuOpacity);
			resolutionPercentageButton75CurrentPosition[0] = lerp(0f, resolutionPercentageButton75Position[0], menuOpacity);
			resolutionPercentageButton75CurrentPosition[1] = lerp(0f, resolutionPercentageButton75Position[1], menuOpacity);
			resolutionPercentageButton100CurrentPosition[0] = lerp(0f, resolutionPercentageButton100Position[0], menuOpacity);
			resolutionPercentageButton100CurrentPosition[1] = lerp(0f, resolutionPercentageButton100Position[1], menuOpacity);
			postProcessDetailTitleCurrentPosition[0] = lerp(0f, postProcessDetailTitlePosition[0], menuOpacity);
			postProcessDetailTitleCurrentPosition[1] = lerp(0f, postProcessDetailTitlePosition[1], menuOpacity);
			postProcessNoDetailButtonCurrentPosition[0] = lerp(0f, postProcessNoDetailButtonPosition[0], menuOpacity);
			postProcessNoDetailButtonCurrentPosition[1] = lerp(0f, postProcessNoDetailButtonPosition[1], menuOpacity);
			postProcessLowDetailButtonCurrentPosition[0] = lerp(0f, postProcessLowDetailButtonPosition[0], menuOpacity);
			postProcessLowDetailButtonCurrentPosition[1] = lerp(0f, postProcessLowDetailButtonPosition[1], menuOpacity);
			postProcessHighDetailButtonCurrentPosition[0] = lerp(0f, postProcessHighDetailButtonPosition[0], menuOpacity);
			postProcessHighDetailButtonCurrentPosition[1] = lerp(0f, postProcessHighDetailButtonPosition[1], menuOpacity);
			musicTitleCurrentPosition[0] = lerp(0f, musicTitlePosition[0], menuOpacity);
			musicTitleCurrentPosition[1] = lerp(0f, musicTitlePosition[1], menuOpacity);
			musicEnableButtonCurrentPosition[0] = lerp(0f, musicEnableButtonPosition[0], menuOpacity);
			musicEnableButtonCurrentPosition[1] = lerp(0f, musicEnableButtonPosition[1], menuOpacity);
			musicDisableButtonCurrentPosition[0] = lerp(0f, musicDisableButtonPosition[0], menuOpacity);
			musicDisableButtonCurrentPosition[1] = lerp(0f, musicDisableButtonPosition[1], menuOpacity);
			effectsTitleCurrentPosition[0] = lerp(0f, effectsTitlePosition[0], menuOpacity);
			effectsTitleCurrentPosition[1] = lerp(0f, effectsTitlePosition[1], menuOpacity);
			effectsEnableButtonCurrentPosition[0] = lerp(0f, effectsEnableButtonPosition[0], menuOpacity);
			effectsEnableButtonCurrentPosition[1] = lerp(0f, effectsEnableButtonPosition[1], menuOpacity);
			effectsDisableButtonCurrentPosition[0] = lerp(0f, effectsDisableButtonPosition[0], menuOpacity);
			effectsDisableButtonCurrentPosition[1] = lerp(0f, effectsDisableButtonPosition[1], menuOpacity);
			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			// Background
			glBindVertexArray(ui9PatchVaoHandle);
			uiPanelProgram.setUniforms(viewProjection, background9PatchCurrentScale, background9PatchPosition, background9PatchTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			glBindVertexArray(uiPanelVaoHandle);

			// Options title
			uiPanelProgram.setUniforms(viewProjection, optionsTitleCurrentScale, optionsTitleCurrentPosition, optionsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Screen Resolution title
			uiPanelProgram.setUniforms(viewProjection, screenResolutionTitleCurrentScale, screenResolutionTitleCurrentPosition, screenResolutionTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 25% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonCurrentScale, resolutionPercentageButton25CurrentPosition, resolutionPercentageButton25CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 50% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonCurrentScale, resolutionPercentageButton50CurrentPosition, resolutionPercentageButton50CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 75% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonCurrentScale, resolutionPercentageButton75CurrentPosition, resolutionPercentageButton75CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 100% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonCurrentScale, resolutionPercentageButton100CurrentPosition, resolutionPercentageButton100CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Post-process title
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailTitleCurrentScale, postProcessDetailTitleCurrentPosition, postProcessDetailTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// No post-process button
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonCurrentScale, postProcessNoDetailButtonCurrentPosition, postProcessNoDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Low post-process
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonCurrentScale, postProcessLowDetailButtonCurrentPosition, postProcessLowDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// High post-process button
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonCurrentScale, postProcessHighDetailButtonCurrentPosition, postProcessHighDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Music title
			uiPanelProgram.setUniforms(viewProjection, musicTitleCurrentScale, musicTitleCurrentPosition, musicTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Music enable button
			uiPanelProgram.setUniforms(viewProjection, musicButtonCurrentScale, musicEnableButtonCurrentPosition, musicEnableButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Music enable button
			uiPanelProgram.setUniforms(viewProjection, musicButtonCurrentScale, musicDisableButtonCurrentPosition, musicDisableButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Effects title
			uiPanelProgram.setUniforms(viewProjection, effectsTitleCurrentScale, effectsTitleCurrentPosition, effectsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Effects enable button
			uiPanelProgram.setUniforms(viewProjection, effectsButtonCurrentScale, effectsEnableButtonCurrentPosition, effectsEnableButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Effects enable button
			uiPanelProgram.setUniforms(viewProjection, effectsButtonCurrentScale, effectsDisableButtonCurrentPosition, effectsDisableButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Back button
			uiPanelProgram.setUniforms(viewProjection, backButtonCurrentScale, backButtonCurrentPosition, backButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}


	public void touch(float x, float y)
	{
		// 25% button
		if(	x >= resolutionPercentageButton25Limits[0] &&
			x <= resolutionPercentageButton25Limits[1] &&
			y >= resolutionPercentageButton25Limits[2] &&
			y <= resolutionPercentageButton25Limits[3])
		{
			touchedResolutionPercentageButton25();
		}

		// 50% button
		else if(x >= resolutionPercentageButton50Limits[0] &&
				x <= resolutionPercentageButton50Limits[1] &&
				y >= resolutionPercentageButton50Limits[2] &&
				y <= resolutionPercentageButton50Limits[3])
		{
			touchedResolutionPercentageButton50();
		}

		// 75% button
		else if(x >= resolutionPercentageButton75Limits[0] &&
				x <= resolutionPercentageButton75Limits[1] &&
				y >= resolutionPercentageButton75Limits[2] &&
				y <= resolutionPercentageButton75Limits[3])
		{
			touchedResolutionPercentageButton75();
		}

		// 100% button
		else if(x >= resolutionPercentageButton100Limits[0] &&
				x <= resolutionPercentageButton100Limits[1] &&
				y >= resolutionPercentageButton100Limits[2] &&
				y <= resolutionPercentageButton100Limits[3])
		{
			touchedResolutionPercentageButton100();
		}

		// No post-process button
		else if(x >= postProcessNoDetailButtonLimits[0] &&
				x <= postProcessNoDetailButtonLimits[1] &&
				y >= postProcessNoDetailButtonLimits[2] &&
				y <= postProcessNoDetailButtonLimits[3])
		{
			touchedNoPostProcessDetailButton();
		}

		// Low post-process button
		else if(x >= postProcessLowDetailButtonLimits[0] &&
				x <= postProcessLowDetailButtonLimits[1] &&
				y >= postProcessLowDetailButtonLimits[2] &&
				y <= postProcessLowDetailButtonLimits[3])
		{
			touchedLowPostProcessDetailButton();
		}

		// High post-process button
		else if(x >= postProcessHighDetailButtonLimits[0] &&
				x <= postProcessHighDetailButtonLimits[1] &&
				y >= postProcessHighDetailButtonLimits[2] &&
				y <= postProcessHighDetailButtonLimits[3])
		{
			touchedHighPostProcessDetailButton();
		}

		// Enable music button
		else if(x >= musicEnableButtonLimits[0] &&
				x <= musicEnableButtonLimits[1] &&
				y >= musicEnableButtonLimits[2] &&
				y <= musicEnableButtonLimits[3])
		{
			touchedMusicEnableButton();
		}

		// Disable music button
		else if(x >= musicDisableButtonLimits[0] &&
				x <= musicDisableButtonLimits[1] &&
				y >= musicDisableButtonLimits[2] &&
				y <= musicDisableButtonLimits[3])
		{
			touchedMusicDisableButton();
		}

		// Enable effects button
		else if(x >= effectsEnableButtonLimits[0] &&
				x <= effectsEnableButtonLimits[1] &&
				y >= effectsEnableButtonLimits[2] &&
				y <= effectsEnableButtonLimits[3])
		{
			touchedEffectsEnableButton();
		}

		// Disable effects button
		else if(x >= effectsDisableButtonLimits[0] &&
				x <= effectsDisableButtonLimits[1] &&
				y >= effectsDisableButtonLimits[2] &&
				y <= effectsDisableButtonLimits[3])
		{
			touchedEffectsDisableButton();
		}

		// Back button
		else if(x >= backButtonLimits[0] &&
				x <= backButtonLimits[1] &&
				y >= backButtonLimits[2] &&
				y <= backButtonLimits[3])
		{
			touchedBackButton();
		}
	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
	}


	public void setDisappearing()
	{
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchedResolutionPercentageButton25()
	{
		resolution = 3;
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton25CurrentTexture = resolutionPercentageButton25SelectedTexture;
		renderer.setResolution25();
		savePreferences();
	}


	private void touchedResolutionPercentageButton50()
	{
		resolution = 2;
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton50CurrentTexture = resolutionPercentageButton50SelectedTexture;
		renderer.setResolution50();
		savePreferences();
	}


	private void touchedResolutionPercentageButton75()
	{
		resolution = 1;
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton75CurrentTexture = resolutionPercentageButton75SelectedTexture;
		renderer.setResolution75();
		savePreferences();
	}


	private void touchedResolutionPercentageButton100()
	{
		resolution = 0;
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton100CurrentTexture = resolutionPercentageButton100SelectedTexture;
		renderer.setResolution100();
		savePreferences();
	}


	private void touchedNoPostProcessDetailButton()
	{
		postProcessQuality = 0;
		resetPostProcessCurrentTextures();
		postProcessNoDetailButtonCurrentTexture = postProcessNoDetailButtonSelectedTexture;
		renderer.setNoPostProcessDetail();
		savePreferences();
	}


	private void touchedLowPostProcessDetailButton()
	{
		postProcessQuality = 1;
		resetPostProcessCurrentTextures();
		postProcessLowDetailButtonCurrentTexture = postProcessLowDetailButtonSelectedTexture;
		renderer.setLowPostProcessDetail();
		savePreferences();
	}


	private void touchedHighPostProcessDetailButton()
	{
		postProcessQuality = 2;
		resetPostProcessCurrentTextures();
		postProcessHighDetailButtonCurrentTexture = postProcessHighDetailButtonSelectedTexture;
		renderer.setHighPostProcessDetail();
		savePreferences();
	}


	private void touchedMusicEnableButton()
	{
		musicEnabled = true;
		resetMusicCurrentTextures();
		musicEnableButtonCurrentTexture = soundEnableButtonSelectedTexture;
		parent.enableBackgroundMusic(true);
		savePreferences();
	}


	private void touchedMusicDisableButton()
	{
		musicEnabled = false;
		resetMusicCurrentTextures();
		musicDisableButtonCurrentTexture = soundDisableButtonSelectedTexture;
		parent.enableBackgroundMusic(false);
		savePreferences();
	}


	private void touchedEffectsEnableButton()
	{
		effectsEnabled = true;
		resetEffectsCurrentTextures();
		effectsEnableButtonCurrentTexture = soundEnableButtonSelectedTexture;
		parent.enableSoundEffects(true);
		savePreferences();
	}


	private void touchedEffectsDisableButton()
	{
		effectsEnabled = false;
		resetEffectsCurrentTextures();
		effectsDisableButtonCurrentTexture = soundDisableButtonSelectedTexture;
		parent.enableSoundEffects(false);
		savePreferences();
	}


	private void touchedBackButton()
	{
		backButtonCurrentTexture = backButtonSelectedTexture;
		renderer.changingFromOptionsMenuToMainMenu();
		currentState = UI_STATE_DISAPPEARING;
	}

}
