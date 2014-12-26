package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.TextureHelper;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;

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

	// State
	private int currentState;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Geometry
	private int uiPanelVaoHandle;

	// Menu common parameters
	private float menuAppearTime = 1f;
	private float menuDisappearTime = 1f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Titles
	private float[] screenResolutionTitleScale = new float[2];
	private float[] screenResolutionTitlePosition = new float[2];
	private float[] postProcessDetailTitleScale = new float[2];
	private float[] postProcessDetailTitlePosition = new float[2];

	// Resolution percentage buttons
	private float[] resolutionPercentageButtonScale = new float[2];
	private float[] resolutionPercentageButton25Position = new float[2];
	private float[] resolutionPercentageButton50Position = new float[2];
	private float[] resolutionPercentageButton75Position = new float[2];
	private float[] resolutionPercentageButton100Position = new float[2];
	private float[] resolutionPercentageButton25Limits = new float[4];
	private float[] resolutionPercentageButton50Limits = new float[4];
	private float[] resolutionPercentageButton75Limits = new float[4];
	private float[] resolutionPercentageButton100Limits = new float[4];

	// Post process detail buttons
	private float[] postProcessDetailButtonScale = new float[2];
	private float[] postProcessNoDetailButtonPosition = new float[2];
	private float[] postProcessLowDetailButtonPosition = new float[2];
	private float[] postProcessHighDetailButtonPosition = new float[2];
	private float[] postProcessNoDetailButtonLimits = new float[4];
	private float[] postProcessLowDetailButtonLimits = new float[4];
	private float[] postProcessHighDetailButtonLimits = new float[4];

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonLimits = new float[4];

	// Textures
	private int screenResolutionTitleTexture;
	private int postProcessDetailTitleTexture;

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


	public OptionsMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.uiPanelProgram = panelProgram;
		this.renderer = renderer;
		this.currentState = UI_STATE_NOT_VISIBLE;

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);

		createMatrices(screenWidth, screenHeight);
		loadTextures(context);
		setPositions(screenWidth, screenHeight);
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
		screenResolutionTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/screen_resolution_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessDetailTitleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/post_process_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

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

		resetBackButtonCurrentTexture();
		resetResolutionPercentageCurrentTextures();
		resetPostProcessCurrentTextures();
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


	private void setPositions(float screenWidth, float screenHeight)
	{
		//float optionButtonWidth = screenWidth * 0.175f;
		float optionButtonHeight = screenHeight * 0.1f;
		float optionButtonWidth = optionButtonHeight * 3f;

		float optionButtonWidthHalf = optionButtonWidth * 0.5f;
		float optionButtonHeightHalf = optionButtonHeight * 0.5f;

		// Scales

		backButtonScale[0] = optionButtonWidth;
		backButtonScale[1] = optionButtonHeight;

		screenResolutionTitleScale[0] = optionButtonHeight * 7f;
		screenResolutionTitleScale[1] = optionButtonHeight;

		resolutionPercentageButtonScale[0] = optionButtonWidth;
		resolutionPercentageButtonScale[1] = optionButtonHeight;

		postProcessDetailTitleScale[0] = optionButtonHeight * 8f;
		postProcessDetailTitleScale[1] = optionButtonHeight;

		postProcessDetailButtonScale[0] = optionButtonWidth;
		postProcessDetailButtonScale[1] = optionButtonHeight;

		// Positions

		resolutionPercentageButton25Position[0] = optionButtonWidth * -1.5f;
		resolutionPercentageButton25Position[1] = 0f;
		resolutionPercentageButton50Position[0] = optionButtonWidth * -0.5f;
		resolutionPercentageButton50Position[1] = 0f;
		resolutionPercentageButton75Position[0] = optionButtonWidth * 0.5f;
		resolutionPercentageButton75Position[1] = 0f;
		resolutionPercentageButton100Position[0] = optionButtonWidth * 1.5f;
		resolutionPercentageButton100Position[1] = 0f;

		postProcessNoDetailButtonPosition[0] = -optionButtonWidth;
		postProcessNoDetailButtonPosition[1] = -optionButtonHeight * 2f;
		postProcessLowDetailButtonPosition[0] = 0f;
		postProcessLowDetailButtonPosition[1] = -optionButtonHeight * 2f;
		postProcessHighDetailButtonPosition[0] = optionButtonWidth;
		postProcessHighDetailButtonPosition[1] = -optionButtonHeight * 2f;

		screenResolutionTitlePosition[0] = 0f;
		screenResolutionTitlePosition[1] = optionButtonHeight;

		postProcessDetailTitlePosition[0] = 0f;
		postProcessDetailTitlePosition[1] = -optionButtonHeight * 1f;

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

		backButtonLimits[0] = backButtonPosition[0] - optionButtonWidthHalf;
		backButtonLimits[1] = backButtonPosition[0] + optionButtonWidthHalf;
		backButtonLimits[2] = backButtonPosition[1] - optionButtonHeightHalf;
		backButtonLimits[3] = backButtonPosition[1] + optionButtonHeightHalf;
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
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			glBindVertexArray(uiPanelVaoHandle);

			// Screen Resolution title
			uiPanelProgram.setUniforms(viewProjection, screenResolutionTitleScale, screenResolutionTitlePosition, screenResolutionTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 25% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton25Position, resolutionPercentageButton25CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 50% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton50Position, resolutionPercentageButton50CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 75% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton75Position, resolutionPercentageButton75CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Draw resolution percentage 100% button
			uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton100Position, resolutionPercentageButton100CurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Post-process title
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailTitleScale, postProcessDetailTitlePosition, postProcessDetailTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// No post-process button
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonScale, postProcessNoDetailButtonPosition, postProcessNoDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Low post-process
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonScale, postProcessLowDetailButtonPosition, postProcessLowDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// High post-process button
			uiPanelProgram.setUniforms(viewProjection, postProcessDetailButtonScale, postProcessHighDetailButtonPosition, postProcessHighDetailButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Back button
			uiPanelProgram.setUniforms(viewProjection, backButtonScale, backButtonPosition, backButtonCurrentTexture, menuOpacity);
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
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton25CurrentTexture = resolutionPercentageButton25SelectedTexture;
		renderer.setResolution25();
	}


	private void touchedResolutionPercentageButton50()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton50CurrentTexture = resolutionPercentageButton50SelectedTexture;
		renderer.setResolution50();
	}


	private void touchedResolutionPercentageButton75()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton75CurrentTexture = resolutionPercentageButton75SelectedTexture;
		renderer.setResolution75();
	}


	private void touchedResolutionPercentageButton100()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton100CurrentTexture = resolutionPercentageButton100SelectedTexture;
		renderer.setResolution100();
	}


	private void touchedNoPostProcessDetailButton()
	{
		resetPostProcessCurrentTextures();
		postProcessNoDetailButtonCurrentTexture = postProcessNoDetailButtonSelectedTexture;
		renderer.setNoPostProcessDetail();
	}


	private void touchedLowPostProcessDetailButton()
	{
		resetPostProcessCurrentTextures();
		postProcessLowDetailButtonCurrentTexture = postProcessLowDetailButtonSelectedTexture;
		renderer.setLowPostProcessDetail();
	}


	private void touchedHighPostProcessDetailButton()
	{
		resetPostProcessCurrentTextures();
		postProcessHighDetailButtonCurrentTexture = postProcessHighDetailButtonSelectedTexture;
		renderer.setHighPostProcessDetail();
	}


	private void touchedBackButton()
	{
		backButtonCurrentTexture = backButtonSelectedTexture;
		renderer.changingFromOptionsMenuToMainMenu();
		currentState = UI_STATE_DISAPPEARING;
	}

}
