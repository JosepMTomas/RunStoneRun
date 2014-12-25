package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

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
	private UIPanelProgram uiPanelProgram;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Geometry
	private int uiPanelVaoHandle;

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

	// Textures
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


	public OptionsMenu(GameActivity parent, UIPanelProgram panelProgram, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.uiPanelProgram = panelProgram;

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
		resolutionPercentageButton25IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_25_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_50_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_75_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100IdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_100_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resolutionPercentageButton25SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_25_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_50_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_75_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/resolution_100_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resetResolutionPercentageCurrentTextures();
	}


	private void resetResolutionPercentageCurrentTextures()
	{
		resolutionPercentageButton25CurrentTexture = resolutionPercentageButton25IdleTexture;
		resolutionPercentageButton50CurrentTexture = resolutionPercentageButton50IdleTexture;
		resolutionPercentageButton75CurrentTexture = resolutionPercentageButton75IdleTexture;
		resolutionPercentageButton100CurrentTexture = resolutionPercentageButton100IdleTexture;
	}


	private void setPositions(float screenWidth, float screenHeight)
	{
		float optionButtonWidth = screenWidth * 0.175f;
		float optionButtonHeight = screenHeight * 0.1f;

		float optionButtonWidthHalf = optionButtonWidth * 0.5f;
		float optionButtonHeightHalf = optionButtonHeight * 0.5f;

		resolutionPercentageButtonScale[0] = optionButtonWidth;
		resolutionPercentageButtonScale[1] = optionButtonHeight;

		resolutionPercentageButton25Position[0] = optionButtonWidth * -1.5f;
		resolutionPercentageButton25Position[1] = 0f;
		resolutionPercentageButton50Position[0] = optionButtonWidth * -0.5f;
		resolutionPercentageButton50Position[1] = 0f;
		resolutionPercentageButton75Position[0] = optionButtonWidth * 0.5f;
		resolutionPercentageButton75Position[1] = 0f;
		resolutionPercentageButton100Position[0] = optionButtonWidth * 1.5f;
		resolutionPercentageButton100Position[1] = 0f;

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
	}


	public void draw()
	{
		uiPanelProgram.useProgram();

		glBindVertexArray(uiPanelVaoHandle);

		// Draw resolution percentage 25% button
		uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton25Position, resolutionPercentageButton25CurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// Draw resolution percentage 50% button
		uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton50Position, resolutionPercentageButton50CurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// Draw resolution percentage 75% button
		uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton75Position, resolutionPercentageButton75CurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// Draw resolution percentage 100% button
		uiPanelProgram.setUniforms(viewProjection, resolutionPercentageButtonScale, resolutionPercentageButton100Position, resolutionPercentageButton100CurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
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
		if(	x >= resolutionPercentageButton50Limits[0] &&
			x <= resolutionPercentageButton50Limits[1] &&
			y >= resolutionPercentageButton50Limits[2] &&
			y <= resolutionPercentageButton50Limits[3])
		{
			touchedResolutionPercentageButton50();
		}

		// 75% button
		if(	x >= resolutionPercentageButton75Limits[0] &&
			x <= resolutionPercentageButton75Limits[1] &&
			y >= resolutionPercentageButton75Limits[2] &&
			y <= resolutionPercentageButton75Limits[3])
		{
			touchedResolutionPercentageButton75();
		}

		// 100% button
		if(	x >= resolutionPercentageButton100Limits[0] &&
			x <= resolutionPercentageButton100Limits[1] &&
			y >= resolutionPercentageButton100Limits[2] &&
			y <= resolutionPercentageButton100Limits[3])
		{
			touchedResolutionPercentageButton100();
		}
	}


	private void touchedResolutionPercentageButton25()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton25CurrentTexture = resolutionPercentageButton25SelectedTexture;
	}


	private void touchedResolutionPercentageButton50()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton50CurrentTexture = resolutionPercentageButton50SelectedTexture;
	}


	private void touchedResolutionPercentageButton75()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton75CurrentTexture = resolutionPercentageButton75SelectedTexture;
	}


	private void touchedResolutionPercentageButton100()
	{
		resetResolutionPercentageCurrentTextures();
		resolutionPercentageButton100CurrentTexture = resolutionPercentageButton100SelectedTexture;
	}

}
