package com.josepmtomas.runstonerun.objects;

import com.josepmtomas.runstonerun.ForwardPlusRenderer;
import com.josepmtomas.runstonerun.programs.UIPanelProgram;
import com.josepmtomas.runstonerun.util.UIHelper;

import static com.josepmtomas.runstonerun.Constants.*;
import static com.josepmtomas.runstonerun.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 01/01/2015.
 * @author Josep
 */
public class PauseMenu
{
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private MenuTextures textures;

	// State
	private int currentState;

	// Menu common attributes
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// UI Panel
	private int uiPanelVaoHandle;
	private int ui9PatchPanelVaoHandle;

	// Textures
	private int resumeButtonCurrentTexture;
	private int restartButtonCurrentTexture;
	private int endGameButtonCurrentTexture;
	private int optionsButtonCurrentTexture;

	// Background panel
	private float[] background9PatchScale = new float[2];
	private float[] background9PatchPosition = new float[2];
	private float[] background9PatchCurrentScale = new float[2];

	// Pause title
	private float[] pauseTitleScale = new float[2];
	private float[] pauseTitlePosition = new float[2];
	private float[] pauseTitleCurrentScale = new float[2];
	private float[] pauseTitleCurrentPosition = new float[2];

	// Resume button
	private float[] resumeButtonScale = new float[2];
	private float[] resumeButtonPosition = new float[2];
	private float[] resumeButtonCurrentScale = new float[2];
	private float[] resumeButtonCurrentPosition = new float[2];
	private float[] resumeButtonLimits = new float[4];

	// Restart button
	private float[] restartButtonScale = new float[2];
	private float[] restartButtonPosition = new float[2];
	private float[] restartButtonCurrentScale = new float[2];
	private float[] restartButtonCurrentPosition = new float[2];
	private float[] restartButtonLimits = new float[4];

	// End Game button
	private float[] endGameButtonScale = new float[2];
	private float[] endGameButtonPosition = new float[2];
	private float[] endGameButtonCurrentScale = new float[2];
	private float[] endGameButtonCurrentPosition = new float[2];
	private float[] endGameButtonLimits = new float[4];

	// Options button
	private float[] optionsButtonScale = new float[2];
	private float[] optionsButtonPosition = new float[2];
	private float[] optionsButtonCurrentScale = new float[2];
	private float[] optionsButtonCurrentPosition = new float[2];
	private float[] optionsButtonLimits = new float[4];


	public PauseMenu(ForwardPlusRenderer renderer, UIPanelProgram uiPanelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.renderer = renderer;
		this.uiPanelProgram = uiPanelProgram;
		this.textures = textures;

		currentState = UI_STATE_NOT_VISIBLE;

		createMatrices(screenWidth, screenHeight);
		createElements(screenWidth, screenHeight);

		loadTextures();
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


	@SuppressWarnings("unused")
	private void createElements(float screenWidth, float screenHeight)
	{
		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchPanelVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.06f, screenHeight * 0.78f/*0.81f*/, screenHeight * 0.06f, UI_BASE_CENTER_CENTER);

		float titleHeight = screenHeight * 0.1f;
		float titleWidth = titleHeight * 10f;
		float buttonHeight = screenHeight * 0.15f;
		float buttonWidth = buttonHeight * 3.3333f;
		float buttonHeightHalf = buttonHeight * 0.5f;
		float buttonWidthHalf = buttonWidth * 0.5f;

		// Background panel
		background9PatchScale[0] = 1f;
		background9PatchScale[1] = 1f;
		background9PatchPosition[0] = 0f;
		background9PatchPosition[1] = screenHeight * 0.015f;
		background9PatchCurrentScale[0] = background9PatchScale[0];
		background9PatchCurrentScale[1] = background9PatchScale[1];

		// Pause title
		pauseTitleScale[0] = titleWidth;
		pauseTitleScale[1] = titleHeight;
		pauseTitlePosition[0] = 0f;
		pauseTitlePosition[1] = titleHeight * 3.25f;
		pauseTitleCurrentScale[0] = pauseTitleScale[0];
		pauseTitleCurrentScale[1] = pauseTitleScale[1];
		pauseTitleCurrentPosition[0] = pauseTitlePosition[0];
		pauseTitleCurrentPosition[1] = pauseTitlePosition[1];

		// Resume button
		resumeButtonScale[0] = buttonWidth;
		resumeButtonScale[1] = buttonHeight;
		resumeButtonPosition[0] = 0f;
		resumeButtonPosition[1] = buttonHeight * 1.25f;
		resumeButtonCurrentScale[0] = resumeButtonScale[0];
		resumeButtonCurrentScale[1] = resumeButtonScale[1];
		resumeButtonCurrentPosition[0] = resumeButtonPosition[0];
		resumeButtonCurrentPosition[1] = resumeButtonPosition[1];
		resumeButtonLimits[0] = resumeButtonPosition[0] - buttonWidthHalf;
		resumeButtonLimits[1] = resumeButtonPosition[0] + buttonWidthHalf;
		resumeButtonLimits[2] = resumeButtonPosition[1] - buttonHeightHalf;
		resumeButtonLimits[3] = resumeButtonPosition[1] + buttonHeightHalf;

		// Restart button
		restartButtonScale[0] = buttonWidth;
		restartButtonScale[1] = buttonHeight;
		restartButtonPosition[0] = 0f;
		restartButtonPosition[1] = buttonHeight * 0.25f;
		restartButtonCurrentScale[0] = restartButtonScale[0];
		restartButtonCurrentScale[1] = restartButtonScale[1];
		restartButtonCurrentPosition[0] = restartButtonPosition[0];
		restartButtonCurrentPosition[1] = restartButtonPosition[1];
		restartButtonLimits[0] = restartButtonPosition[0] - buttonWidthHalf;
		restartButtonLimits[1] = restartButtonPosition[0] + buttonWidthHalf;
		restartButtonLimits[2] = restartButtonPosition[1] - buttonHeightHalf;
		restartButtonLimits[3] = restartButtonPosition[1] + buttonHeightHalf;

		// End Game button
		endGameButtonScale[0] = buttonWidth;
		endGameButtonScale[1] = buttonHeight;
		endGameButtonPosition[0] = 0f;
		endGameButtonPosition[1] = buttonHeight * -0.75f;
		endGameButtonCurrentScale[0] = endGameButtonScale[0];
		endGameButtonCurrentScale[1] = endGameButtonScale[1];
		endGameButtonCurrentPosition[0] = endGameButtonPosition[0];
		endGameButtonCurrentPosition[1] = endGameButtonPosition[1];
		endGameButtonLimits[0] = endGameButtonPosition[0] - buttonWidthHalf;
		endGameButtonLimits[1] = endGameButtonPosition[0] + buttonWidthHalf;
		endGameButtonLimits[2] = endGameButtonPosition[1] - buttonHeightHalf;
		endGameButtonLimits[3] = endGameButtonPosition[1] + buttonHeightHalf;

		// Options button
		optionsButtonScale[0] = buttonWidth;
		optionsButtonScale[1] = buttonHeight;
		optionsButtonPosition[0] = 0f;
		optionsButtonPosition[1] = buttonHeight * -1.75f;
		optionsButtonCurrentScale[0] = optionsButtonScale[0];
		optionsButtonCurrentScale[1] = optionsButtonScale[1];
		optionsButtonCurrentPosition[0] = optionsButtonPosition[0];
		optionsButtonCurrentPosition[1] = optionsButtonPosition[1];
		optionsButtonLimits[0] = optionsButtonPosition[0] - buttonWidthHalf;
		optionsButtonLimits[1] = optionsButtonPosition[0] + buttonWidthHalf;
		optionsButtonLimits[2] = optionsButtonPosition[1] - buttonHeightHalf;
		optionsButtonLimits[3] = optionsButtonPosition[1] + buttonHeightHalf;
	}


	private void loadTextures()
	{

	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
		menuTimer = 0f;
		releaseTouch();
	}


	public void releaseTouch()
	{
		resumeButtonCurrentTexture = textures.resumeButtonIdleTexture;
		restartButtonCurrentTexture = textures.restartButtonIdleTexture;
		endGameButtonCurrentTexture = textures.endGameButtonIdleTexture;
		optionsButtonCurrentTexture = textures.optionsButtonIdleTexture;
	}


	public void touch(float x, float y)
	{
		if(	x >= resumeButtonLimits[0] &&
			x <= resumeButtonLimits[1] &&
			y >= resumeButtonLimits[2] &&
			y <= resumeButtonLimits[3])
		{
			touchedResumeButton();
		}
		else if(x >= restartButtonLimits[0] &&
				x <= restartButtonLimits[1] &&
				y >= restartButtonLimits[2] &&
				y <= restartButtonLimits[3])
		{
			touchedRestartButton();
		}
		else if(x >= endGameButtonLimits[0] &&
				x <= endGameButtonLimits[1] &&
				y >= endGameButtonLimits[2] &&
				y <= endGameButtonLimits[3])
		{
			touchedEndGameButton();
		}
		else if(x >= optionsButtonLimits[0] &&
				x <= optionsButtonLimits[1] &&
				y >= optionsButtonLimits[2] &&
				y <= optionsButtonLimits[3])
		{
			touchedOptionsButton();
		}
	}


	private void touchedResumeButton()
	{
		resumeButtonCurrentTexture = textures.resumeButtonSelectedTexture;
		renderer.setPause(false);
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchedRestartButton()
	{
		restartButtonCurrentTexture = textures.restartButtonSelectedTexture;
		/*renderer.newGame();
		currentState = UI_STATE_DISAPPEARING;*/
		renderer.changingToRestartMenu();
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchedEndGameButton()
	{
		endGameButtonCurrentTexture = textures.endGameButtonSelectedTexture;
		renderer.changingToEndGameMenu();
		currentState = UI_STATE_DISAPPEARING;
	}


	private void touchedOptionsButton()
	{
		optionsButtonCurrentTexture = textures.optionsButtonSelectedTexture;
		renderer.changingToOptionsMenuFromPauseMenu();
		currentState = UI_STATE_DISAPPEARING;
	}


	private void setCurrentElementsAttributes(float alpha)
	{
		background9PatchCurrentScale[0] = lerp(0f, 1f, alpha);
		background9PatchCurrentScale[1] = lerp(0f, 1f, alpha);

		pauseTitleCurrentScale[0] = lerp(0f, pauseTitleScale[0], alpha);
		pauseTitleCurrentScale[1] = lerp(0f, pauseTitleScale[1], alpha);
		pauseTitleCurrentPosition[0] = lerp(0f, pauseTitlePosition[0], alpha);
		pauseTitleCurrentPosition[1] = lerp(0f, pauseTitlePosition[1], alpha);

		resumeButtonCurrentScale[0] = lerp(0f, resumeButtonScale[0], alpha);
		resumeButtonCurrentScale[1] = lerp(0f, resumeButtonScale[1], alpha);
		resumeButtonCurrentPosition[0] = lerp(0f, resumeButtonPosition[0], alpha);
		resumeButtonCurrentPosition[1] = lerp(0f, resumeButtonPosition[1], alpha);

		restartButtonCurrentScale[0] = lerp(0f, restartButtonScale[0], alpha);
		restartButtonCurrentScale[1] = lerp(0f, restartButtonScale[1], alpha);
		restartButtonCurrentPosition[0] = lerp(0f, restartButtonPosition[0], alpha);
		restartButtonCurrentPosition[1] = lerp(0f, restartButtonPosition[1], alpha);

		endGameButtonCurrentScale[0] = lerp(0f, endGameButtonScale[0], alpha);
		endGameButtonCurrentScale[1] = lerp(0f, endGameButtonScale[1], alpha);
		endGameButtonCurrentPosition[0] = lerp(0f, endGameButtonPosition[0], alpha);
		endGameButtonCurrentPosition[1] = lerp(0f, endGameButtonPosition[1], alpha);

		optionsButtonCurrentScale[0] = lerp(0f, optionsButtonScale[0], alpha);
		optionsButtonCurrentScale[1] = lerp(0f, optionsButtonScale[1], alpha);
		optionsButtonCurrentPosition[0] = lerp(0f, optionsButtonPosition[0], alpha);
		optionsButtonCurrentPosition[1] = lerp(0f, optionsButtonPosition[1], alpha);
	}


	public void update(float deltaTime)
	{
		if(currentState == UI_STATE_APPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = menuTimer / menuAppearTime;

			if(menuTimer >= menuAppearTime)
			{
				menuOpacity = 1f;
				menuTimer = 0f;
				currentState = UI_STATE_VISIBLE;
				renderer.changedToPauseMenu();
			}

			setCurrentElementsAttributes(menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuAppearTime);

			if(menuTimer >= menuDisappearTime)
			{
				menuOpacity = 0f;
				menuTimer = 0f;
				currentState = UI_STATE_NOT_VISIBLE;
			}

			setCurrentElementsAttributes(menuOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			// Background
			glBindVertexArray(ui9PatchPanelVaoHandle);
			uiPanelProgram.setUniforms(viewProjection, background9PatchCurrentScale, background9PatchPosition, textures.background9PatchPanelTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			// Panel
			glBindVertexArray(uiPanelVaoHandle);

			// Pause title
			uiPanelProgram.setUniforms(viewProjection, pauseTitleCurrentScale, pauseTitleCurrentPosition, textures.pauseTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Resume button
			uiPanelProgram.setUniforms(viewProjection, resumeButtonCurrentScale, resumeButtonCurrentPosition, resumeButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Restart button
			uiPanelProgram.setUniforms(viewProjection, restartButtonCurrentScale, restartButtonCurrentPosition, restartButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// End Game button
			uiPanelProgram.setUniforms(viewProjection, endGameButtonCurrentScale, endGameButtonCurrentPosition, endGameButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Options button
			uiPanelProgram.setUniforms(viewProjection, optionsButtonCurrentScale, optionsButtonCurrentPosition, optionsButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}
}
