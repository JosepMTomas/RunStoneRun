package com.josepmtomas.rockgame.objects;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programs.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 14/01/2015.
 * @author Josep
 */
public class RestartMenu
{
	private GameActivity parent;
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
	private float[] restartTitleScale = new float[2];
	private float[] restartTitlePosition = new float[2];
	private float[] restartTitleCurrentScale = new float[2];
	private float[] restartTitleCurrentPosition = new float[2];

	// End Game Text
	private float[] restartTextScale = new float[2];
	private float[] restartTextPosition = new float[2];
	private float[] restartTextCurrentScale = new float[2];
	private float[] restartTextCurrentPosition = new float[2];

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


	public RestartMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.parent = parent;
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
		restartTitleScale[0] = titleWidth;
		restartTitleScale[1] = titleHeight;
		restartTitlePosition[0] = 0f;
		restartTitlePosition[1] = titleHeight * 1.5f;
		restartTitleCurrentScale[0] = restartTitleScale[0];
		restartTitleCurrentScale[1] = restartTitleScale[1];
		restartTitleCurrentPosition[0] = restartTitlePosition[0];
		restartTitleCurrentPosition[1] = restartTitlePosition[1];

		// End Game Text
		restartTextScale[0] = titleWidth * 0.8f;
		restartTextScale[1] = titleHeight * 1.5f;
		restartTextPosition[0] = 0f;
		restartTextPosition[1] = 0f;
		restartTextCurrentScale[0] = restartTextScale[0];
		restartTextCurrentScale[1] = restartTextScale[1];
		restartTextCurrentPosition[0] = restartTextPosition[0];
		restartTextCurrentPosition[1] = restartTextPosition[1];

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
		yesButtonCurrentTexture = menuTextures.yesButtonSelectedTexture;
		renderer.newGame();
		currentState = UI_STATE_DISAPPEARING;
		nextMenu = NO_MENU;
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
				renderer.changedToRestartMenu();
			}

			backgroundPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			restartTitleCurrentScale[0] = lerp(0f, restartTitleScale[0], menuOpacity);
			restartTitleCurrentScale[1] = lerp(0f, restartTitleScale[1], menuOpacity);
			restartTitleCurrentPosition[0] = lerp(0f, restartTitlePosition[0], menuOpacity);
			restartTitleCurrentPosition[1] = lerp(0f, restartTitlePosition[1], menuOpacity);

			restartTextCurrentScale[0] = lerp(0f, restartTextScale[0], menuOpacity);
			restartTextCurrentScale[1] = lerp(0f, restartTextScale[1], menuOpacity);
			restartTextCurrentPosition[0] = lerp(0f, restartTextPosition[0], menuOpacity);
			restartTextCurrentPosition[1] = lerp(0f, restartTextPosition[1], menuOpacity);

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

				if(nextMenu == PAUSE_MENU)
				{
					renderer.changedFromEndGameMenuToPauseMenu();
				}
			}

			backgroundPanelCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			restartTitleCurrentScale[0] = lerp(0f, restartTitleScale[0], menuOpacity);
			restartTitleCurrentScale[1] = lerp(0f, restartTitleScale[1], menuOpacity);
			restartTitleCurrentPosition[0] = lerp(0f, restartTitlePosition[0], menuOpacity);
			restartTitleCurrentPosition[1] = lerp(0f, restartTitlePosition[1], menuOpacity);

			restartTextCurrentScale[0] = lerp(0f, restartTextScale[0], menuOpacity);
			restartTextCurrentScale[1] = lerp(0f, restartTextScale[1], menuOpacity);
			restartTextCurrentPosition[0] = lerp(0f, restartTextPosition[0], menuOpacity);
			restartTextCurrentPosition[1] = lerp(0f, restartTextPosition[1], menuOpacity);

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
			uiPanelProgram.setUniforms(viewProjection, restartTitleCurrentScale, restartTitleCurrentPosition, menuTextures.restartTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// End game text
			uiPanelProgram.setUniforms(viewProjection, restartTextCurrentScale, restartTextCurrentPosition, menuTextures.restartTextTexture, menuOpacity);
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
