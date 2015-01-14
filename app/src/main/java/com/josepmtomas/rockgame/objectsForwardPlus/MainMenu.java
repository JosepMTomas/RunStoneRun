package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 22/12/2014.
 */
public class MainMenu
{
	private GameActivity parent;
	private ForwardPlusRenderer renderer;
	private Context context;
	private MenuTextures textures;

	// State
	private int currentState;

	// Menu common attributes
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 1f;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// UI Panel
	private int uiPanelVaoHandle;
	private int ui9PatchPanelVaoHandle;

	// Buttons back panel
	private float[] buttonsBackPanelPosition = new float[2];
	private float[] buttonsBackPanelCurrentScale = new float[2];
	private float[] buttonsBackPanelCurrentPosition = new float[2];

	// New game button
	private int newGameButtonCurrentTexture;
	private float[] newGameButtonPosition = new float[2];
	private float[] newGameButtonScale = new float[2];
	private float[] newGameButtonLimits = new float[4];
	private float[] newGameButtonCurrentPosition = new float[2];
	private float[] newGameButtonCurrentScale = new float[2];

	// Options button
	private int optionsButtonCurrentTexture;
	private float[] optionsButtonPosition = new float[2];
	private float[] optionsButtonScale = new float[2];
	private float[] optionsButtonLimits = new float[4];
	private float[] optionsButtonCurrentPosition = new float[2];
	private float[] optionsButtonCurrentScale = new float[2];

	// Credits button
	private int creditsButtonCurrentTexture;
	private float[] creditsButtonPosition = new float[2];
	private float[] creditsButtonScale = new float[2];
	private float[] creditsButtonLimits = new float[4];
	private float[] creditsButtonCurrentPosition = new float[2];
	private float[] creditsButtonCurrentScale = new float[2];

	// renderer ui programs
	UIPanelProgram uiPanelProgram;


	public MainMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.renderer = renderer;
		this.context = parent.getApplicationContext();
		this.textures = textures;
		this.uiPanelProgram = panelProgram;
		this.currentState = UI_STATE_VISIBLE;

		createMatrices(screenWidth, screenHeight);
		loadTextures();

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);

		createButtons(screenWidth, screenHeight);
		createButtonsBackPanel(screenWidth, screenHeight);
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
		newGameButtonCurrentTexture = textures.newGameButtonIdleTexture;
		optionsButtonCurrentTexture = textures.optionsButtonIdleTexture;
		creditsButtonCurrentTexture = textures.creditsButtonIdleTexture;
	}


	private void createButtons(float screenWidth, float screenHeight)
	{
		float width = screenWidth * 0.3f;
		float height = screenHeight * 0.16f;

		createNewGameButton(width, height);
		createOptionsButton(width, height);
		createCreditsButton(width, height);
		setCurrentPositions();
	}


	public void createButtonsBackPanel(float screenWidth, float screenHeight)
	{
		float borderSize = screenHeight * 0.025f;
		//float cornerSize = screenHeight * 0.08f + borderSize;
		float cornerSize = screenHeight * 0.048f + borderSize;
		float width = (screenWidth * 0.3f) + (borderSize * 2f);
		float height = (screenHeight * 0.16f * 3f) + (borderSize * 2f);

		ui9PatchPanelVaoHandle = UIHelper.make9PatchPanel(width, height, cornerSize, UI_BASE_CENTER_CENTER);

		buttonsBackPanelCurrentScale[0] = 1f;
		buttonsBackPanelCurrentScale[1] = 1f;

		buttonsBackPanelPosition[0] = 0f;
		buttonsBackPanelPosition[1] = optionsButtonPosition[1];
		buttonsBackPanelCurrentPosition[0] = buttonsBackPanelPosition[0];
		buttonsBackPanelCurrentPosition[1] = buttonsBackPanelPosition[1];
	}


	private void createNewGameButton(float width, float height)
	{
		newGameButtonPosition[0] = 0f;
		newGameButtonPosition[1] = height * 0.5f;

		newGameButtonScale[0] = width;
		newGameButtonScale[1] = height;

		// left-right-bottom-top
		newGameButtonLimits[0] = newGameButtonPosition[0] - (width * 0.5f);
		newGameButtonLimits[1] = newGameButtonPosition[0] + (width * 0.5f);
		newGameButtonLimits[2] = newGameButtonPosition[1] - (height * 0.5f);
		newGameButtonLimits[3] = newGameButtonPosition[1] + (height * 0.5f);
	}


	private void createOptionsButton(float width, float height)
	{
		optionsButtonPosition[0] = newGameButtonPosition[0];
		optionsButtonPosition[1] = newGameButtonPosition[1] - height;

		optionsButtonScale[0] = width;
		optionsButtonScale[1] = height;

		optionsButtonLimits[0] = optionsButtonPosition[0] - (width * 0.5f);
		optionsButtonLimits[1] = optionsButtonPosition[0] + (width * 0.5f);
		optionsButtonLimits[2] = optionsButtonPosition[1] - (height * 0.5f);
		optionsButtonLimits[3] = optionsButtonPosition[1] + (height * 0.5f);
	}


	private void createCreditsButton(float width, float height)
	{
		creditsButtonPosition[0] = optionsButtonPosition[0];
		creditsButtonPosition[1] = optionsButtonPosition[1] - height;

		creditsButtonScale[0] = width;
		creditsButtonScale[1] = height;

		creditsButtonLimits[0] = creditsButtonPosition[0] - (width * 0.5f);
		creditsButtonLimits[1] = creditsButtonPosition[0] + (width * 0.5f);
		creditsButtonLimits[2] = creditsButtonPosition[1] - (height * 0.5f);
		creditsButtonLimits[3] = creditsButtonPosition[1] + (height * 0.5f);
	}


	private void setCurrentPositions()
	{
		newGameButtonCurrentPosition[0] = newGameButtonPosition[0];
		newGameButtonCurrentPosition[1] = newGameButtonPosition[1];
		newGameButtonCurrentScale[0] = newGameButtonScale[0];
		newGameButtonCurrentScale[1] = newGameButtonScale[1];

		optionsButtonCurrentPosition[0] = optionsButtonPosition[0];
		optionsButtonCurrentPosition[1] = optionsButtonPosition[1];
		optionsButtonCurrentScale[0] = optionsButtonScale[0];
		optionsButtonCurrentScale[1] = optionsButtonScale[1];

		creditsButtonCurrentPosition[0] = creditsButtonPosition[0];
		creditsButtonCurrentPosition[1] = creditsButtonPosition[1];
		creditsButtonCurrentScale[0] = creditsButtonScale[0];
		creditsButtonCurrentScale[1] = creditsButtonScale[1];
	}


	public void touch(float x, float y)
	{
		//Log.w("MainMenu", "Touch : x = " + x + " : y = " + y);
		if(	x >= newGameButtonLimits[0] &&
			x <= newGameButtonLimits[1] &&
			y >= newGameButtonLimits[2] &&
			y <= newGameButtonLimits[3])
		{
			touchNewGameButton();
		}

		else if(x >= optionsButtonLimits[0] &&
				x <= optionsButtonLimits[1] &&
				y >= optionsButtonLimits[2] &&
				y <= optionsButtonLimits[3])
		{
			touchOptionsButton();
		}

		else if(x >= optionsButtonLimits[0] &&
				x <= optionsButtonLimits[1] &&
				y >= creditsButtonLimits[2] &&
				y <= creditsButtonLimits[3])
		{
			touchCreditsButton();
		}
	}


	public void releaseTouch()
	{
		newGameButtonCurrentTexture = textures.newGameButtonIdleTexture;
		optionsButtonCurrentTexture = textures.optionsButtonIdleTexture;
		creditsButtonCurrentTexture = textures.creditsButtonIdleTexture;
	}


	private void touchNewGameButton()
	{
		newGameButtonCurrentTexture = textures.newGameButtonSelectedTexture;
		renderer.newGame();
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchOptionsButton()
	{
		optionsButtonCurrentTexture = textures.optionsButtonSelectedTexture;
		renderer.changingToOptionsMenuFromMainMenu();
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchCreditsButton()
	{
		creditsButtonCurrentTexture = textures.creditsButtonSelectedTexture;
		renderer.changingToCreditsMenu();
		currentState = UI_STATE_DISAPPEARING;
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

			if(menuTimer >= menuAppearTime)
			{
				currentState = UI_STATE_VISIBLE;
				menuOpacity = 1f;
				menuTimer = 0f;
				renderer.changedToMainMenu();
			}

			buttonsBackPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			buttonsBackPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);
			buttonsBackPanelCurrentPosition[0] = lerp(0f, buttonsBackPanelPosition[0], menuOpacity);
			buttonsBackPanelCurrentPosition[1] = lerp(0f, buttonsBackPanelPosition[1], menuOpacity);

			newGameButtonCurrentScale[0] = lerp(0f, newGameButtonScale[0], menuOpacity);
			newGameButtonCurrentScale[1] = lerp(0f, newGameButtonScale[1], menuOpacity);
			optionsButtonCurrentScale[0] = lerp(0f, optionsButtonScale[0], menuOpacity);
			optionsButtonCurrentScale[1] = lerp(0f, optionsButtonScale[1], menuOpacity);
			creditsButtonCurrentScale[0] = lerp(0f, creditsButtonScale[0], menuOpacity);
			creditsButtonCurrentScale[1] = lerp(0f, creditsButtonScale[1], menuOpacity);

			newGameButtonCurrentPosition[0] = lerp(0f, newGameButtonPosition[0], menuOpacity);
			newGameButtonCurrentPosition[1] = lerp(0f, newGameButtonPosition[1], menuOpacity);
			optionsButtonCurrentPosition[0] = lerp(0f, optionsButtonPosition[0], menuOpacity);
			optionsButtonCurrentPosition[1] = lerp(0f, optionsButtonPosition[1], menuOpacity);
			creditsButtonCurrentPosition[0] = lerp(0f, creditsButtonPosition[0], menuOpacity);
			creditsButtonCurrentPosition[1] = lerp(0f, creditsButtonPosition[1], menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			if(menuTimer >= menuDisappearTime)
			{
				currentState = UI_STATE_NOT_VISIBLE;
				menuOpacity = 0f;
				menuTimer = 0f;
			}

			buttonsBackPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			buttonsBackPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);
			buttonsBackPanelCurrentPosition[0] = lerp(0f, buttonsBackPanelPosition[0], menuOpacity);
			buttonsBackPanelCurrentPosition[1] = lerp(0f, buttonsBackPanelPosition[1], menuOpacity);

			newGameButtonCurrentScale[0] = lerp(0f, newGameButtonScale[0], menuOpacity);
			newGameButtonCurrentScale[1] = lerp(0f, newGameButtonScale[1], menuOpacity);
			optionsButtonCurrentScale[0] = lerp(0f, optionsButtonScale[0], menuOpacity);
			optionsButtonCurrentScale[1] = lerp(0f, optionsButtonScale[1], menuOpacity);
			creditsButtonCurrentScale[0] = lerp(0f, creditsButtonScale[0], menuOpacity);
			creditsButtonCurrentScale[1] = lerp(0f, creditsButtonScale[1], menuOpacity);

			newGameButtonCurrentPosition[0] = lerp(0f, newGameButtonPosition[0], menuOpacity);
			newGameButtonCurrentPosition[1] = lerp(0f, newGameButtonPosition[1], menuOpacity);
			optionsButtonCurrentPosition[0] = lerp(0f, optionsButtonPosition[0], menuOpacity);
			optionsButtonCurrentPosition[1] = lerp(0f, optionsButtonPosition[1], menuOpacity);
			creditsButtonCurrentPosition[0] = lerp(0f, creditsButtonPosition[0], menuOpacity);
			creditsButtonCurrentPosition[1] = lerp(0f, creditsButtonPosition[1], menuOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			glBindVertexArray(ui9PatchPanelVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, buttonsBackPanelCurrentScale, buttonsBackPanelCurrentPosition, textures.background9PatchPanelTexture, 0.75f * menuOpacity);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			glBindVertexArray(uiPanelVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, newGameButtonCurrentScale, newGameButtonCurrentPosition, newGameButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, optionsButtonCurrentScale, optionsButtonCurrentPosition, optionsButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, creditsButtonCurrentScale, creditsButtonCurrentPosition, creditsButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}
}
