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
	private int ui9PatchVaoHandle;

	// Menu common parameters
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuOpacity = 0f;

	// Background panel
	private float[] backgroundPanelScale = new float[2];
	private float[] backgroundPanelPosition = new float[2];
	private float[] backgroundPanelCurrentScale = new float[2];

	// Credits title
	private float[] creditsTitleScale = new float[2];
	private float[] creditsTitlePosition  = new float[2];
	private float[] creditsTitleCurrentScale = new float[2];
	private float[] creditsTitleCurrentPosition = new float[2];

	// Developer title
	private float[] developerTitleScale = new float[2];
	private float[] developerTitlePosition = new float[2];
	private float[] developerTitleCurrentScale = new float[2];
	private float[] developerTitleCurrentPosition = new float[2];

	// Composer title
	private float[] composerTitleScale = new float[2];
	private float[] composerTitlePosition = new float[2];
	private float[] composerTitleCurrentScale = new float[2];
	private float[] composerTitleCurrentPosition = new float[2];

	// Sound effects title
	private float[] effectsTitleScale = new float[2];
	private float[] effectsTitlePosition = new float[2];
	private float[] effectsTitleCurrentScale = new float[2];
	private float[] effectsTitleCurrentPosition = new float[2];

	// Font type title
	private float[] fontTitleScale = new float[2];
	private float[] fontTitlePosition = new float[2];
	private float[] fontTitleCurrentScale = new float[2];
	private float[] fontTitleCurrentPosition = new float[2];

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonCurrentScale = new float[2];
	private float[] backButtonCurrentPosition = new float[2];
	private float[] backButtonLimits = new float[4];

	// Textures
	private int background9PatchTexture;

	private int creditsTitleTexture;

	private int developerTitleTexture;
	private int composerTitleTexture;
	private int effectsTitleTexture;
	private int fontTitleTexture;

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
		ui9PatchVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.1f, screenHeight * 1.0f, screenHeight * 0.1f, UI_BASE_CENTER_CENTER);

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
		background9PatchTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/9patch.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		creditsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/credits_menu/credits_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		developerTitleTexture = TextureHelper.loadETC2Texture(context, "textures/credits_menu/developer_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		composerTitleTexture = TextureHelper.loadETC2Texture(context, "textures/credits_menu/composer_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		effectsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/credits_menu/sound_effects_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		fontTitleTexture = TextureHelper.loadETC2Texture(context, "textures/credits_menu/font_type_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		backButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		backButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/options_menu/back_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}


	private void resetCurrentTextures()
	{
		backButtonCurrentTexture = backButtonIdleTexture;
	}


	private void setPositions(float screenWidth, float screenHeight)
	{
		float buttonHeight = screenHeight * 0.1f;
		float buttonWidth = buttonHeight * 3f;

		float buttonWidthHalf = buttonWidth * 0.5f;
		float buttonHeightHalf = buttonHeight * 0.5f;

		// Background
		backgroundPanelScale[0] = 1f;
		backgroundPanelScale[1] = 1f;
		backgroundPanelPosition[0] = 0f;
		backgroundPanelPosition[1] = 0f;

		// Credits title
		creditsTitleScale[0] = buttonHeight * 10f;
		creditsTitleScale[1] = buttonHeight;
		creditsTitlePosition[0] = 0f;
		creditsTitlePosition[1] = buttonHeight * 4.0f;

		// Developer title
		developerTitleScale[0] = buttonHeight * 4f; //6
		developerTitleScale[1] = buttonHeight * 1.0f;
		developerTitlePosition[0] = 0f;
		developerTitlePosition[1] = buttonHeight * 2.75f;

		// Composer title
		composerTitleScale[0] = buttonHeight * 3.3333f; //5
		composerTitleScale[1] = buttonHeight * 1.0f;
		composerTitlePosition[0] = 0f;
		composerTitlePosition[1] = buttonHeight * 1.5f;

		// Sound effects title
		effectsTitleScale[0] = buttonHeight * 4.666667f; // 7
		effectsTitleScale[1] = buttonHeight * 1.0f;
		effectsTitlePosition[0] = 0f;
		effectsTitlePosition[1] = buttonHeight * 0.25f;

		// Font type title
		fontTitleScale[0] = buttonHeight * 4.666667f;
		fontTitleScale[1] = buttonHeight * 1f;
		fontTitlePosition[0] = 0f;
		fontTitlePosition[1] = buttonHeight * -1.0f;

		// Back button
		backButtonScale[0] = buttonWidth;
		backButtonScale[1] = buttonHeight;
		backButtonPosition[0] = 0f;
		backButtonPosition[1] = buttonHeight * -4f;
		backButtonLimits[0] = backButtonPosition[0] - buttonWidthHalf;
		backButtonLimits[1] = backButtonPosition[0] + buttonWidthHalf;
		backButtonLimits[2] = backButtonPosition[1] - buttonHeightHalf;
		backButtonLimits[3] = backButtonPosition[1] + buttonHeightHalf;
	}


	private void setCurrentPositions()
	{
		// Credits title
		creditsTitleCurrentScale[0] = creditsTitleScale[0];
		creditsTitleCurrentScale[1] = creditsTitleScale[1];
		creditsTitleCurrentPosition[0] = creditsTitlePosition[0];
		creditsTitleCurrentPosition[1] = creditsTitlePosition[1];

		// Developer title
		developerTitleCurrentScale[0] = developerTitleScale[0];
		developerTitleCurrentScale[1] = developerTitleScale[1];
		developerTitleCurrentPosition[0] = developerTitlePosition[0];
		developerTitleCurrentPosition[1] = developerTitlePosition[1];

		// Composer title
		composerTitleCurrentScale[0] = composerTitleScale[0];
		composerTitleCurrentScale[1] = composerTitleScale[1];
		composerTitleCurrentPosition[0] = composerTitleCurrentPosition[0];
		composerTitleCurrentPosition[1] = composerTitleCurrentPosition[1];

		// Sound effects title
		effectsTitleCurrentScale[0] = effectsTitleScale[0];
		effectsTitleCurrentScale[1] = effectsTitleScale[1];
		effectsTitleCurrentPosition[0] = effectsTitlePosition[0];
		effectsTitleCurrentPosition[1] = effectsTitlePosition[1];

		// Font type title
		fontTitleCurrentScale[0] = fontTitleScale[0];
		fontTitleCurrentScale[1] = fontTitleScale[1];
		fontTitleCurrentPosition[0] = fontTitlePosition[0];
		fontTitleCurrentPosition[1] = fontTitlePosition[1];

		// Back button
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

			if(menuTimer >= menuAppearTime)
			{
				menuOpacity = 1.0f;
				menuTimer = 0f;
				currentState = UI_STATE_VISIBLE;
				renderer.changedToCreditsMenu();
				setCurrentPositions();
			}

			backgroundPanelCurrentScale[0] = lerp(0f, backgroundPanelScale[0], menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, backgroundPanelScale[1], menuOpacity);

			creditsTitleCurrentScale[0] = lerp(0f, creditsTitleScale[0], menuOpacity);
			creditsTitleCurrentScale[1] = lerp(0f, creditsTitleScale[1], menuOpacity);
			creditsTitleCurrentPosition[0] = lerp(0f, creditsTitlePosition[0], menuOpacity);
			creditsTitleCurrentPosition[1] = lerp(0f, creditsTitlePosition[1], menuOpacity);

			developerTitleCurrentScale[0] = lerp(0f, developerTitleScale[0], menuOpacity);
			developerTitleCurrentScale[1] = lerp(0f, developerTitleScale[1], menuOpacity);
			developerTitleCurrentPosition[0] = lerp(0f, developerTitlePosition[0], menuOpacity);
			developerTitleCurrentPosition[1] = lerp(0f, developerTitlePosition[1], menuOpacity);

			composerTitleCurrentScale[0] = lerp(0f, composerTitleScale[0], menuOpacity);
			composerTitleCurrentScale[1] = lerp(0f, composerTitleScale[1], menuOpacity);
			composerTitleCurrentPosition[0] = lerp(0f, composerTitlePosition[0], menuOpacity);
			composerTitleCurrentPosition[1] = lerp(0f, composerTitlePosition[1], menuOpacity);

			effectsTitleCurrentScale[0] = lerp(0f, effectsTitleScale[0], menuOpacity);
			effectsTitleCurrentScale[1] = lerp(0f, effectsTitleScale[1], menuOpacity);
			effectsTitleCurrentPosition[0] = lerp(0f, effectsTitlePosition[0], menuOpacity);
			effectsTitleCurrentPosition[1] = lerp(0f, effectsTitlePosition[1], menuOpacity);

			fontTitleCurrentScale[0] = lerp(0f, fontTitleScale[0], menuOpacity);
			fontTitleCurrentScale[1] = lerp(0f, fontTitleScale[1], menuOpacity);
			fontTitleCurrentPosition[0] = lerp(0f, fontTitlePosition[0], menuOpacity);
			fontTitleCurrentPosition[1] = lerp(0f, fontTitlePosition[1], menuOpacity);

			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);
			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			backgroundPanelCurrentScale[0] = lerp(0f, backgroundPanelScale[0], menuOpacity);
			backgroundPanelCurrentScale[1] = lerp(0f, backgroundPanelScale[1], menuOpacity);

			creditsTitleCurrentScale[0] = lerp(0f, creditsTitleScale[0], menuOpacity);
			creditsTitleCurrentScale[1] = lerp(0f, creditsTitleScale[1], menuOpacity);
			creditsTitleCurrentPosition[0] = lerp(0f, creditsTitlePosition[0], menuOpacity);
			creditsTitleCurrentPosition[1] = lerp(0f, creditsTitlePosition[1], menuOpacity);

			developerTitleCurrentScale[0] = lerp(0f, developerTitleScale[0], menuOpacity);
			developerTitleCurrentScale[1] = lerp(0f, developerTitleScale[1], menuOpacity);
			developerTitleCurrentPosition[0] = lerp(0f, developerTitlePosition[0], menuOpacity);
			developerTitleCurrentPosition[1] = lerp(0f, developerTitlePosition[1], menuOpacity);

			composerTitleCurrentScale[0] = lerp(0f, composerTitleScale[0], menuOpacity);
			composerTitleCurrentScale[1] = lerp(0f, composerTitleScale[1], menuOpacity);
			composerTitleCurrentPosition[0] = lerp(0f, composerTitlePosition[0], menuOpacity);
			composerTitleCurrentPosition[1] = lerp(0f, composerTitlePosition[1], menuOpacity);

			effectsTitleCurrentScale[0] = lerp(0f, effectsTitleScale[0], menuOpacity);
			effectsTitleCurrentScale[1] = lerp(0f, effectsTitleScale[1], menuOpacity);
			effectsTitleCurrentPosition[0] = lerp(0f, effectsTitlePosition[0], menuOpacity);
			effectsTitleCurrentPosition[1] = lerp(0f, effectsTitlePosition[1], menuOpacity);

			fontTitleCurrentScale[0] = lerp(0f, fontTitleScale[0], menuOpacity);
			fontTitleCurrentScale[1] = lerp(0f, fontTitleScale[1], menuOpacity);
			fontTitleCurrentPosition[0] = lerp(0f, fontTitlePosition[0], menuOpacity);
			fontTitleCurrentPosition[1] = lerp(0f, fontTitlePosition[1], menuOpacity);

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

			glBindVertexArray(ui9PatchVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, backgroundPanelCurrentScale, backgroundPanelPosition, background9PatchTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			glBindVertexArray(uiPanelVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, creditsTitleCurrentScale, creditsTitleCurrentPosition, creditsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, developerTitleCurrentScale, developerTitleCurrentPosition, developerTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, composerTitleCurrentScale, composerTitleCurrentPosition, composerTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, effectsTitleCurrentScale, effectsTitleCurrentPosition, effectsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, fontTitleCurrentScale, fontTitleCurrentPosition, fontTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

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
