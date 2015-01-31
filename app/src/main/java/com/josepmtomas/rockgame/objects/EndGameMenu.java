package com.josepmtomas.rockgame.objects;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.programs.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 01/01/2015.
 * @author Josep
 */
public class EndGameMenu
{
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private MenuTextures menuTextures;

	// State
	private int currentState = UI_STATE_NOT_VISIBLE;
	private int nextMenu = MAIN_MENU;

	// Menu common attributes
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Panels
	private final int uiPanelVaoHandle;
	private final int ui9PatchPanelVaoHandle;

	// Background panel
	private float[] backgroundPanelCurrentScale = new float[2];
	private float[] backgroundPanelCurrentPosition = new float[2];

	// End Game Title
	private float[] endGameTitleScale = new float[2];
	private float[] endGameTitlePosition = new float[2];
	private float[] endGameTitleCurrentScale = new float[2];
	private float[] endGameTitleCurrentPosition = new float[2];

	// End Game Text
	private float[] endGameTextScale = new float[2];
	private float[] endGameTextPosition = new float[2];
	private float[] endGameTextCurrentScale = new float[2];
	private float[] endGameTextCurrentPosition = new float[2];

	// Yes button
	private int yesButtonCurrentTexture;
	private float[] yesButtonScale = new float[2];
	private float[] yesButtonPosition = new float[2];
	private float[] yesButtonCurrentScale = new float[2];
	private float[] yesButtonCurrentPosition = new float[2];
	private float[] yesButtonLimits = new float[4];

	// No button
	private int noButtonCurrentTexture;
	private float[] noButtonScale = new float[2];
	private float[] noButtonPosition = new float[2];
	private float[] noButtonCurrentScale = new float[2];
	private float[] noButtonCurrentPosition = new float[2];
	private float[] noButtonLimits = new float[4];


	public EndGameMenu(ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.renderer = renderer;
		this.uiPanelProgram = panelProgram;
		this.menuTextures = textures;

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchPanelVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.06f, screenHeight * 0.46f, screenHeight * 0.06f, UI_BASE_CENTER_CENTER);

		createMatrices(screenWidth, screenHeight);
		setPositions(screenWidth, screenHeight);
		resetButtonTextures();
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


	private void resetButtonTextures()
	{
		yesButtonCurrentTexture = menuTextures.yesButtonIdleTexture;
		noButtonCurrentTexture = menuTextures.noButtonIdleTexture;
	}


	@SuppressWarnings("unused")
	private void setPositions(float screenWidth, float screenHeight)
	{
		float titleHeight = screenHeight * 0.1f;
		float titleWidth = titleHeight * 10f;

		float buttonHeight = screenHeight * 0.1f;
		float buttonWidth = buttonHeight * 3f;
		float buttonHeightHalf = buttonHeight * 0.5f;
		float buttonWidthHalf = buttonWidth * 0.5f;

		// Background panel
		backgroundPanelCurrentScale[0] = 1f;
		backgroundPanelCurrentScale[1] = 1f;
		backgroundPanelCurrentPosition[0] = 0f;
		backgroundPanelCurrentPosition[1] = 0f;

		// End Game Title
		endGameTitleScale[0] = titleWidth;
		endGameTitleScale[1] = titleHeight;
		endGameTitlePosition[0] = 0f;
		endGameTitlePosition[1] = titleHeight * 1.5f;
		endGameTitleCurrentScale[0] = endGameTitleScale[0];
		endGameTitleCurrentScale[1] = endGameTitleScale[1];
		endGameTitleCurrentPosition[0] = endGameTitlePosition[0];
		endGameTitleCurrentPosition[1] = endGameTitlePosition[1];

		// End Game Text
		endGameTextScale[0] = titleWidth;
		endGameTextScale[1] = titleHeight * 1.5f;
		endGameTextPosition[0] = 0f;
		endGameTextPosition[1] = 0f;
		endGameTextCurrentScale[0] = endGameTextScale[0];
		endGameTextCurrentScale[1] = endGameTextScale[1];
		endGameTextCurrentPosition[0] = endGameTextPosition[0];
		endGameTextCurrentPosition[1] = endGameTextPosition[1];

		// Yes button
		yesButtonScale[0] = buttonWidth;
		yesButtonScale[1] = buttonHeight;
		yesButtonPosition[0] = -buttonWidthHalf;
		yesButtonPosition[1] = -buttonHeight * 1.5f;
		yesButtonCurrentScale[0] = yesButtonScale[0];
		yesButtonCurrentScale[1] = yesButtonScale[1];
		yesButtonCurrentPosition[0] = yesButtonPosition[0];
		yesButtonCurrentPosition[1] = yesButtonPosition[1];
		yesButtonLimits[0] = yesButtonPosition[0] - buttonWidthHalf;
		yesButtonLimits[1] = yesButtonPosition[0] + buttonWidthHalf;
		yesButtonLimits[2] = yesButtonPosition[1] - buttonHeightHalf;
		yesButtonLimits[3] = yesButtonPosition[1] + buttonHeightHalf;

		// No button
		noButtonScale[0] = buttonWidth;
		noButtonScale[1] = buttonHeight;
		noButtonPosition[0] = buttonWidthHalf;
		noButtonPosition[1] = -buttonHeight * 1.5f;
		noButtonCurrentScale[0] = noButtonScale[0];
		noButtonCurrentScale[1] = noButtonScale[1];
		noButtonCurrentPosition[0] = noButtonPosition[0];
		noButtonCurrentPosition[1] = noButtonPosition[1];
		noButtonLimits[0] = noButtonPosition[0] - buttonWidthHalf;
		noButtonLimits[1] = noButtonPosition[0] + buttonWidthHalf;
		noButtonLimits[2] = noButtonPosition[1] - buttonHeightHalf;
		noButtonLimits[3] = noButtonPosition[1] + buttonHeightHalf;
	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
		resetButtonTextures();
	}


	public void touch(float x, float y)
	{
		if(	x >= yesButtonLimits[0] &&
			x <= yesButtonLimits[1] &&
			y >= yesButtonLimits[2] &&
			y <= yesButtonLimits[3])
		{
			touchedYesButton();
		}
		else if(x >= noButtonLimits[0] &&
				x <= noButtonLimits[1] &&
				y >= noButtonLimits[2] &&
				y <= noButtonLimits[3])
		{
			touchedNoButton();
		}
	}


	private void touchedYesButton()
	{
		currentState = UI_STATE_DISAPPEARING;
		yesButtonCurrentTexture = menuTextures.yesButtonSelectedTexture;
		renderer.changingFromEndGameMenuToMainMenu();
		renderer.endGame();
		nextMenu = MAIN_MENU;
	}


	private void touchedNoButton()
	{
		currentState = UI_STATE_DISAPPEARING;
		noButtonCurrentTexture = menuTextures.noButtonSelectedTexture;
		renderer.changingFromEndGameMenuToPauseMenu();
		nextMenu = PAUSE_MENU;
	}


	public void update(float deltaTime)
	{
		if(currentState == UI_STATE_APPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = menuTimer / menuAppearTime;

			if(menuTimer >= menuAppearTime)
			{
				menuTimer = 0f;
				menuOpacity = 1f;
				currentState = UI_STATE_VISIBLE;
				renderer.changedToEndGameMenu();
			}

			backgroundPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			endGameTitleCurrentScale[0] = lerp(0f, endGameTitleScale[0], menuOpacity);
			endGameTitleCurrentScale[1] = lerp(0f, endGameTitleScale[1], menuOpacity);
			endGameTitleCurrentPosition[0] = lerp(0f, endGameTitlePosition[0], menuOpacity);
			endGameTitleCurrentPosition[1] = lerp(0f, endGameTitlePosition[1], menuOpacity);

			endGameTextCurrentScale[0] = lerp(0f, endGameTextScale[0], menuOpacity);
			endGameTextCurrentScale[1] = lerp(0f, endGameTextScale[1], menuOpacity);
			endGameTextCurrentPosition[0] = lerp(0f, endGameTextPosition[0], menuOpacity);
			endGameTextCurrentPosition[1] = lerp(0f, endGameTextPosition[1], menuOpacity);

			yesButtonCurrentScale[0] = lerp(0f, yesButtonScale[0], menuOpacity);
			yesButtonCurrentScale[1] = lerp(0f, yesButtonScale[1], menuOpacity);
			yesButtonCurrentPosition[0] = lerp(0f, yesButtonPosition[0], menuOpacity);
			yesButtonCurrentPosition[1] = lerp(0f, yesButtonPosition[1], menuOpacity);

			noButtonCurrentScale[0] = lerp(0f, noButtonScale[0], menuOpacity);
			noButtonCurrentScale[1] = lerp(0f, noButtonScale[1], menuOpacity);
			noButtonCurrentPosition[0] = lerp(0f, noButtonPosition[0], menuOpacity);
			noButtonCurrentPosition[1] = lerp(0f, noButtonPosition[1], menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			if(menuTimer >= menuDisappearTime)
			{
				menuTimer = 0f;
				menuOpacity = 0f;
				currentState = UI_STATE_NOT_VISIBLE;

				if(nextMenu == MAIN_MENU)
				{
					renderer.changedFromEndGameMenuToMainMenu();
				}
				else if(nextMenu == PAUSE_MENU)
				{
					renderer.changedFromEndGameMenuToPauseMenu();
				}
			}

			backgroundPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			endGameTitleCurrentScale[0] = lerp(0f, endGameTitleScale[0], menuOpacity);
			endGameTitleCurrentScale[1] = lerp(0f, endGameTitleScale[1], menuOpacity);
			endGameTitleCurrentPosition[0] = lerp(0f, endGameTitlePosition[0], menuOpacity);
			endGameTitleCurrentPosition[1] = lerp(0f, endGameTitlePosition[1], menuOpacity);

			endGameTextCurrentScale[0] = lerp(0f, endGameTextScale[0], menuOpacity);
			endGameTextCurrentScale[1] = lerp(0f, endGameTextScale[1], menuOpacity);
			endGameTextCurrentPosition[0] = lerp(0f, endGameTextPosition[0], menuOpacity);
			endGameTextCurrentPosition[1] = lerp(0f, endGameTextPosition[1], menuOpacity);

			yesButtonCurrentScale[0] = lerp(0f, yesButtonScale[0], menuOpacity);
			yesButtonCurrentScale[1] = lerp(0f, yesButtonScale[1], menuOpacity);
			yesButtonCurrentPosition[0] = lerp(0f, yesButtonPosition[0], menuOpacity);
			yesButtonCurrentPosition[1] = lerp(0f, yesButtonPosition[1], menuOpacity);

			noButtonCurrentScale[0] = lerp(0f, noButtonScale[0], menuOpacity);
			noButtonCurrentScale[1] = lerp(0f, noButtonScale[1], menuOpacity);
			noButtonCurrentPosition[0] = lerp(0f, noButtonPosition[0], menuOpacity);
			noButtonCurrentPosition[1] = lerp(0f, noButtonPosition[1], menuOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			// Background panel
			glBindVertexArray(ui9PatchPanelVaoHandle);
			uiPanelProgram.setUniforms(viewProjection, backgroundPanelCurrentScale, backgroundPanelCurrentPosition, menuTextures.background9PatchPanelTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			// End game title
			glBindVertexArray(uiPanelVaoHandle);
			uiPanelProgram.setUniforms(viewProjection, endGameTitleCurrentScale, endGameTitleCurrentPosition, menuTextures.endGameTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// End game text
			uiPanelProgram.setUniforms(viewProjection, endGameTextCurrentScale, endGameTextCurrentPosition, menuTextures.endGameTextTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Yes button
			uiPanelProgram.setUniforms(viewProjection, yesButtonCurrentScale, yesButtonCurrentPosition, yesButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// No button
			uiPanelProgram.setUniforms(viewProjection, noButtonCurrentScale, noButtonCurrentPosition, noButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}
}