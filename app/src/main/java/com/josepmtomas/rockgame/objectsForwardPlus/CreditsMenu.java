package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

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
 * Created by Josep on 27/12/2014.
 */
public class CreditsMenu
{
	//
	private GameActivity parent;
	private Context context;
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;

	// State
	private int currentState = UI_STATE_NOT_VISIBLE;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Panel geometry
	private int uiPanelVaoHandle;

	// Menu common parameters
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonCurrentScale = new float[2];
	private float[] backButtonCurrentPosition = new float[2];
	private float[] backButtonLimits = new float[4];

	// Textures
	private int backButtonIdleTexture;
	private int backButtonSelectedTexture;
	private int backButtonCurrentTexture;


	public CreditsMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.renderer = renderer;
		this.uiPanelProgram = panelProgram;

		uiPanelVaoHandle = UIHelper.makePanel(1f,1f,UI_BASE_CENTER_CENTER);

		createMatrices(screenWidth, screenHeight);

		loadTextures();
		resetCurrentTextures();

		setPositions(screenWidth, screenHeight);
		setCurrentPositions();
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


	private void loadTextures()
	{
		backButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		backButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}


	private void resetCurrentTextures()
	{
		backButtonCurrentTexture = backButtonIdleTexture;
	}


	private void setPositions(float screenWidth, float screenHeight)
	{
		float buttonHeigth = screenHeight * 0.1f;
		float buttonWidth = buttonHeigth * 3f;

		float buttonWidthHalf = buttonWidth * 0.5f;
		float buttonHeightHalf = buttonHeigth * 0.5f;

		backButtonScale[0] = buttonWidth;
		backButtonScale[1] = buttonHeigth;

		backButtonPosition[0] = 0f;
		backButtonPosition[1] = buttonHeigth * -4f;

		backButtonLimits[0] = backButtonPosition[0] - buttonWidthHalf;
		backButtonLimits[1] = backButtonPosition[0] + buttonWidthHalf;
		backButtonLimits[2] = backButtonPosition[1] - buttonHeightHalf;
		backButtonLimits[3] = backButtonPosition[1] + buttonHeightHalf;
	}


	private void setCurrentPositions()
	{
		backButtonCurrentScale[0] = backButtonScale[0];
		backButtonCurrentScale[1] = backButtonScale[1];

		backButtonCurrentPosition[0] = backButtonPosition[0];
		backButtonCurrentPosition[1] = backButtonPosition[1];
	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
	}


	public void update(float deltaTime)
	{
		if(currentState == UI_STATE_APPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = menuTimer / menuAppearTime;

			backButtonCurrentScale[0] = lerp(backButtonScale[0] * 2f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(backButtonScale[1] * 2f, backButtonScale[1], menuOpacity);

			backButtonCurrentPosition[0] = lerp(backButtonPosition[0] * 2f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(backButtonPosition[1] * 2f, backButtonPosition[1], menuOpacity);

			if(menuTimer >= menuAppearTime)
			{
				menuOpacity = 1.0f;
				menuTimer = 0f;
				currentState = UI_STATE_VISIBLE;
				renderer.changedToCreditsMenu();
			}
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);

			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);

			if(menuTimer >= menuDisappearTime)
			{
				menuOpacity = 1.0f;
				menuTimer = 0f;
				currentState = UI_STATE_NOT_VISIBLE;
				renderer.changedFromCreditsMenuToMainMenu();
				resetCurrentTextures();
			}
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();
			glBindVertexArray(uiPanelVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, backButtonCurrentScale, backButtonCurrentPosition, backButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}


	public void touch(float x, float y)
	{
		if(	x >= backButtonLimits[0] &&
			x <= backButtonLimits[1] &&
			y >= backButtonLimits[2] &&
			y <= backButtonLimits[3])
		{
			touchedBackButton();
		}
	}


	private void touchedBackButton()
	{
		backButtonCurrentTexture = backButtonSelectedTexture;
		renderer.changingFromCreditsMenuToMainMenu();
		currentState = UI_STATE_DISAPPEARING;
	}
}
