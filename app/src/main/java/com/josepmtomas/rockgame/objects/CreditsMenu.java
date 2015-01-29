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
 * Created by Josep on 27/12/2014.
 * @author Josep
 */
public class CreditsMenu
{
	//
	private GameActivity parent;
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private MenuTextures textures;

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
	private float[] developerTitleLimits = new float[4];

	// Composer title
	private float[] composerTitleScale = new float[2];
	private float[] composerTitlePosition = new float[2];
	private float[] composerTitleCurrentScale = new float[2];
	private float[] composerTitleCurrentPosition = new float[2];
	private float[] composerTitleLimits = new float[4];

	// Sound effects title
	private float[] effectsTitleScale = new float[2];
	private float[] effectsTitlePosition = new float[2];
	private float[] effectsTitleCurrentScale = new float[2];
	private float[] effectsTitleCurrentPosition = new float[2];
	private float[] effectsTitleLimits = new float[4];

	// Font type title
	private float[] fontTitleScale = new float[2];
	private float[] fontTitlePosition = new float[2];
	private float[] fontTitleCurrentScale = new float[2];
	private float[] fontTitleCurrentPosition = new float[2];
	private float[] fontTitleLimits = new float[4];

	// Textures title
	private float[] texturesTitleScale = new float[2];
	private float[] texturesTitlePosition = new float[2];
	private float[] texturesTitleCurrentScale = new float[2];
	private float[] texturesTitleCurrentPosition = new float[2];
	private float[] texturesTitleLimits = new float[4];

	// Original models title
	private float[] originalModelsTitleScale = new float[2];
	private float[] originalModelsTitlePosition = new float[2];
	private float[] originalModelsTitleCurrentScale = new float[2];
	private float[] originalModelsTitleCurrentPosition = new float[2];

	// Hughes Muller title
	private float[] hughesTitleScale = new float[2];
	private float[] hughesTitlePosition = new float[2];
	private float[] hughesTitleCurrentScale = new float[2];
	private float[] hughesTitleCurrentPosition = new float[2];
	private float[] hughesTitleLimits = new float[4];

	// Tomislav Spajic title
	private float[] tomislavTitleScale = new float[2];
	private float[] tomislavTitlePosition = new float[2];
	private float[] tomislavTitleCurrentScale = new float[2];
	private float[] tomislavTitleCurrentPosition = new float[2];
	private float[] tomislavTitleLimits = new float[4];

	// Special thanks title
	private float[] specialThanksTitleScale = new float[2];
	private float[] specialThanksTitlePosition = new float[2];
	private float[] specialThanksTitleCurrentScale = new float[2];
	private float[] specialThanksTitleCurrentPosition = new float[2];

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonCurrentScale = new float[2];
	private float[] backButtonCurrentPosition = new float[2];
	private float[] backButtonLimits = new float[4];

	// Textures


	private int backButtonCurrentTexture;


	public CreditsMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.renderer = renderer;
		this.uiPanelProgram = panelProgram;
		this.textures = textures;

		uiPanelVaoHandle = UIHelper.makePanel(1f,1f,UI_BASE_CENTER_CENTER);
		ui9PatchVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.06f, screenHeight * 0.96f, screenHeight * 0.06f, UI_BASE_CENTER_CENTER);

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

	}


	private void resetCurrentTextures()
	{
		backButtonCurrentTexture = textures.backButtonIdleTexture;
	}


	@SuppressWarnings("unused")
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
		developerTitleLimits[0] = developerTitlePosition[0] - buttonWidthHalf;
		developerTitleLimits[1] = developerTitlePosition[0] + buttonWidthHalf;
		developerTitleLimits[2] = developerTitlePosition[1] - buttonHeightHalf;
		developerTitleLimits[3] = developerTitlePosition[1] + buttonHeightHalf;

		// Composer title
		composerTitleScale[0] = buttonHeight * 3.3333f; //5
		composerTitleScale[1] = buttonHeight * 1.0f;
		composerTitlePosition[0] = buttonWidth * -0.8f;
		composerTitlePosition[1] = buttonHeight * 1.5f;
		composerTitleLimits[0] = composerTitlePosition[0] - buttonWidthHalf;
		composerTitleLimits[1] = composerTitlePosition[0] + buttonWidthHalf;
		composerTitleLimits[2] = composerTitlePosition[1] - buttonHeightHalf;
		composerTitleLimits[3] = composerTitlePosition[1] + buttonHeightHalf;


		// Sound effects title
		effectsTitleScale[0] = buttonHeight * 4.666667f; // 7
		effectsTitleScale[1] = buttonHeight * 1.0f;
		effectsTitlePosition[0] = buttonWidth * 0.75f;
		effectsTitlePosition[1] = buttonHeight * 1.5f; //0.25f
		effectsTitleLimits[0] = effectsTitlePosition[0] - buttonWidthHalf;
		effectsTitleLimits[1] = effectsTitlePosition[0] + buttonWidthHalf;
		effectsTitleLimits[2] = effectsTitlePosition[1] - buttonHeightHalf;
		effectsTitleLimits[3] = effectsTitlePosition[1] + buttonHeightHalf;

		// Font type title
		fontTitleScale[0] = buttonHeight * 4.666667f;
		fontTitleScale[1] = buttonHeight * 1f;
		fontTitlePosition[0] = buttonWidth * -0.75f;
		fontTitlePosition[1] = buttonHeight * 0.25f; //-1.0f
		fontTitleLimits[0] = fontTitlePosition[0] - buttonWidthHalf;
		fontTitleLimits[1] = fontTitlePosition[0] + buttonWidthHalf;
		fontTitleLimits[2] = fontTitlePosition[1] - buttonHeightHalf;
		fontTitleLimits[3] = fontTitlePosition[1] + buttonHeightHalf;

		// Textures title
		texturesTitleScale[0] = buttonHeight * 4.33333f;
		texturesTitleScale[1] = buttonHeight * 1f;
		texturesTitlePosition[0] = buttonWidth * 0.85f;
		texturesTitlePosition[1] = buttonHeight * 0.25f;
		texturesTitleLimits[0] = texturesTitlePosition[0] - buttonWidthHalf;
		texturesTitleLimits[1] = texturesTitlePosition[0] + buttonWidthHalf;
		texturesTitleLimits[2] = texturesTitlePosition[1] - buttonHeightHalf;
		texturesTitleLimits[3] = texturesTitlePosition[1] + buttonHeightHalf;

		// Original models & textures title
		originalModelsTitleScale[0] = buttonHeight * 7.3333333f;
		originalModelsTitleScale[1] = buttonHeight * 0.6666666f;
		originalModelsTitlePosition[0] = 0f;
		originalModelsTitlePosition[1] = buttonHeight * -0.7f;

		// Hughes Muller title
		hughesTitleScale[0] = buttonHeight * 3.466666f;
		hughesTitleScale[1] = buttonHeight * 0.433333f;
		hughesTitlePosition[0] = 0f;
		hughesTitlePosition[1] = buttonHeight * -1.3f;
		hughesTitleLimits[0] = hughesTitlePosition[0] - buttonWidthHalf;
		hughesTitleLimits[1] = hughesTitlePosition[0] + buttonWidthHalf;
		hughesTitleLimits[2] = hughesTitlePosition[1] - (buttonHeightHalf * 0.65f);
		hughesTitleLimits[3] = hughesTitlePosition[1] + (buttonHeightHalf * 0.65f);

		// Tomislav Spajic title
		tomislavTitleScale[0] = buttonHeight * 4.0f;
		tomislavTitleScale[1] = buttonHeight * 0.466666f;
		tomislavTitlePosition[0] = 0f;
		tomislavTitlePosition[1] = buttonHeight * -1.75f;
		tomislavTitleLimits[0] = tomislavTitlePosition[0] - buttonWidthHalf;
		tomislavTitleLimits[1] = tomislavTitlePosition[0] + buttonWidthHalf;
		tomislavTitleLimits[2] = tomislavTitlePosition[1] - (buttonHeightHalf * 0.65f);
		tomislavTitleLimits[3] = tomislavTitlePosition[1] + (buttonHeightHalf * 0.65f);

		// Special thanks title
		specialThanksTitleScale[0] = buttonHeight * 4.0f;
		specialThanksTitleScale[1] = buttonHeight * 1.0f;
		specialThanksTitlePosition[0] = 0f;
		specialThanksTitlePosition[1] = buttonHeight * -2.75f;

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

		// Textures title
		texturesTitleCurrentScale[0] = texturesTitleScale[0];
		texturesTitleCurrentScale[1] = texturesTitleScale[1];
		texturesTitleCurrentPosition[0] = texturesTitlePosition[0];
		texturesTitleCurrentPosition[1] = texturesTitlePosition[1];

		// Original models & textures title
		originalModelsTitleCurrentScale[0] = originalModelsTitleScale[0];
		originalModelsTitleCurrentScale[1] = originalModelsTitleScale[1];
		originalModelsTitleCurrentPosition[0] = originalModelsTitlePosition[0];
		originalModelsTitleCurrentPosition[1] = originalModelsTitlePosition[1];

		// Hughes Muller title
		hughesTitleCurrentScale[0] = hughesTitleScale[0];
		hughesTitleCurrentScale[1] = hughesTitleScale[1];
		hughesTitleCurrentPosition[0] = hughesTitlePosition[0];
		hughesTitleCurrentPosition[1] = hughesTitlePosition[1];

		// Tomislav Spajic title
		tomislavTitleCurrentScale[0] = tomislavTitleScale[0];
		tomislavTitleCurrentScale[1] = tomislavTitleScale[1];
		tomislavTitleCurrentPosition[0] = tomislavTitlePosition[0];
		tomislavTitleCurrentPosition[1] = tomislavTitlePosition[1];

		// Special thanks position
		specialThanksTitleCurrentScale[0] = specialThanksTitleScale[0];
		specialThanksTitleCurrentScale[1] = specialThanksTitleScale[1];
		specialThanksTitleCurrentPosition[0] = specialThanksTitlePosition[0];
		specialThanksTitleCurrentPosition[1] = specialThanksTitlePosition[1];

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

			texturesTitleCurrentScale[0] = lerp(0f, texturesTitleScale[0], menuOpacity);
			texturesTitleCurrentScale[1] = lerp(0f, texturesTitleScale[1], menuOpacity);
			texturesTitleCurrentPosition[0] = lerp(0f, texturesTitlePosition[0], menuOpacity);
			texturesTitleCurrentPosition[1] = lerp(0f, texturesTitlePosition[1], menuOpacity);

			originalModelsTitleCurrentScale[0] = lerp(0f, originalModelsTitleScale[0], menuOpacity);
			originalModelsTitleCurrentScale[1] = lerp(0f, originalModelsTitleScale[1], menuOpacity);
			originalModelsTitleCurrentPosition[0] = lerp(0f, originalModelsTitlePosition[0], menuOpacity);
			originalModelsTitleCurrentPosition[1] = lerp(0f, originalModelsTitlePosition[1], menuOpacity);

			hughesTitleCurrentScale[0] = lerp(0f, hughesTitleScale[0], menuOpacity);
			hughesTitleCurrentScale[1] = lerp(0f, hughesTitleScale[1], menuOpacity);
			hughesTitleCurrentPosition[0] = lerp(0f, hughesTitlePosition[0], menuOpacity);
			hughesTitleCurrentPosition[1] = lerp(0f, hughesTitlePosition[1], menuOpacity);

			tomislavTitleCurrentScale[0] = lerp(0f, tomislavTitleScale[0], menuOpacity);
			tomislavTitleCurrentScale[1] = lerp(0f, tomislavTitleScale[1], menuOpacity);
			tomislavTitleCurrentPosition[0] = lerp(0f, tomislavTitlePosition[0], menuOpacity);
			tomislavTitleCurrentPosition[1] = lerp(0f, tomislavTitlePosition[1], menuOpacity);

			specialThanksTitleCurrentScale[0] = lerp(0f, specialThanksTitleScale[0], menuOpacity);
			specialThanksTitleCurrentScale[1] = lerp(0f, specialThanksTitleScale[1], menuOpacity);
			specialThanksTitleCurrentPosition[0] = lerp(0f, specialThanksTitlePosition[0], menuOpacity);
			specialThanksTitleCurrentPosition[1] = lerp(0f, specialThanksTitlePosition[1], menuOpacity);

			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);
			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1f - (menuTimer / menuDisappearTime);

			if(menuTimer >= menuDisappearTime)
			{
				menuOpacity = 0.0f;
				menuTimer = 0f;
				currentState = UI_STATE_NOT_VISIBLE;
				renderer.changedFromCreditsMenuToMainMenu();
				resetCurrentTextures();
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

			texturesTitleCurrentScale[0] = lerp(0f, texturesTitleScale[0], menuOpacity);
			texturesTitleCurrentScale[1] = lerp(0f, texturesTitleScale[1], menuOpacity);
			texturesTitleCurrentPosition[0] = lerp(0f, texturesTitlePosition[0], menuOpacity);
			texturesTitleCurrentPosition[1] = lerp(0f, texturesTitlePosition[1], menuOpacity);

			originalModelsTitleCurrentScale[0] = lerp(0f, originalModelsTitleScale[0], menuOpacity);
			originalModelsTitleCurrentScale[1] = lerp(0f, originalModelsTitleScale[1], menuOpacity);
			originalModelsTitleCurrentPosition[0] = lerp(0f, originalModelsTitlePosition[0], menuOpacity);
			originalModelsTitleCurrentPosition[1] = lerp(0f, originalModelsTitlePosition[1], menuOpacity);

			hughesTitleCurrentScale[0] = lerp(0f, hughesTitleScale[0], menuOpacity);
			hughesTitleCurrentScale[1] = lerp(0f, hughesTitleScale[1], menuOpacity);
			hughesTitleCurrentPosition[0] = lerp(0f, hughesTitlePosition[0], menuOpacity);
			hughesTitleCurrentPosition[1] = lerp(0f, hughesTitlePosition[1], menuOpacity);

			tomislavTitleCurrentScale[0] = lerp(0f, tomislavTitleScale[0], menuOpacity);
			tomislavTitleCurrentScale[1] = lerp(0f, tomislavTitleScale[1], menuOpacity);
			tomislavTitleCurrentPosition[0] = lerp(0f, tomislavTitlePosition[0], menuOpacity);
			tomislavTitleCurrentPosition[1] = lerp(0f, tomislavTitlePosition[1], menuOpacity);

			specialThanksTitleCurrentScale[0] = lerp(0f, specialThanksTitleScale[0], menuOpacity);
			specialThanksTitleCurrentScale[1] = lerp(0f, specialThanksTitleScale[1], menuOpacity);
			specialThanksTitleCurrentPosition[0] = lerp(0f, specialThanksTitlePosition[0], menuOpacity);
			specialThanksTitleCurrentPosition[1] = lerp(0f, specialThanksTitlePosition[1], menuOpacity);

			backButtonCurrentScale[0] = lerp(0f, backButtonScale[0], menuOpacity);
			backButtonCurrentScale[1] = lerp(0f, backButtonScale[1], menuOpacity);
			backButtonCurrentPosition[0] = lerp(0f, backButtonPosition[0], menuOpacity);
			backButtonCurrentPosition[1] = lerp(0f, backButtonPosition[1], menuOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			glBindVertexArray(ui9PatchVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, backgroundPanelCurrentScale, backgroundPanelPosition, textures.background9PatchPanelTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			glBindVertexArray(uiPanelVaoHandle);

			uiPanelProgram.setUniforms(viewProjection, creditsTitleCurrentScale, creditsTitleCurrentPosition, textures.creditsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, developerTitleCurrentScale, developerTitleCurrentPosition, textures.developerTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, composerTitleCurrentScale, composerTitleCurrentPosition, textures.composerTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, effectsTitleCurrentScale, effectsTitleCurrentPosition, textures.effectsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, fontTitleCurrentScale, fontTitleCurrentPosition, textures.fontTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, texturesTitleCurrentScale, texturesTitleCurrentPosition, textures.texturesTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, originalModelsTitleCurrentScale, originalModelsTitleCurrentPosition, textures.originalModelsTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, hughesTitleCurrentScale, hughesTitleCurrentPosition, textures.hughesTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, tomislavTitleCurrentScale, tomislavTitleCurrentPosition, textures.tomislavTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, specialThanksTitleCurrentScale, specialThanksTitleCurrentPosition, textures.specialThanksTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			uiPanelProgram.setUniforms(viewProjection, backButtonCurrentScale, backButtonCurrentPosition, backButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}


	public void touch(float x, float y)
	{
		if( x >= developerTitleLimits[0] &&
			x <= developerTitleLimits[1] &&
			y >= developerTitleLimits[2] &&
			y <= developerTitleLimits[3] )
		{
			parent.launchDeveloperIntent();
		}
		else if(x >= composerTitleLimits[0] &&
				x <= composerTitleLimits[1] &&
				y >= composerTitleLimits[2] &&
				y <= composerTitleLimits[3] )
		{
			parent.launchComposerIntent();
		}
		else if(x >= effectsTitleLimits[0] &&
				x <= effectsTitleLimits[1] &&
				y >= effectsTitleLimits[2] &&
				y <= effectsTitleLimits[3])
		{
			parent.launchSoundEffectsIntent();
		}
		else if(x >= fontTitleLimits[0] &&
				x <= fontTitleLimits[1] &&
				y >= fontTitleLimits[2] &&
				y <= fontTitleLimits[3])
		{
			parent.touchedFontIntent();
		}
		else if(x >= texturesTitleLimits[0] &&
				x <= texturesTitleLimits[1] &&
				y >= texturesTitleLimits[2] &&
				y <= texturesTitleLimits[3])
		{
			parent.touchedTexturesIntent();
		}
		else if(x >= hughesTitleLimits[0] &&
				x <= hughesTitleLimits[1] &&
				y >= hughesTitleLimits[2] &&
				y <= hughesTitleLimits[3])
		{
			parent.touchedHughesMullerIntent();
		}
		else if(x >= tomislavTitleLimits[0] &&
				x <= tomislavTitleLimits[1] &&
				y >= tomislavTitleLimits[2] &&
				y <= tomislavTitleLimits[3])
		{
			parent.touchedTomislavSpajicIntent();
		}
		else if(x >= backButtonLimits[0] &&
				x <= backButtonLimits[1] &&
				y >= backButtonLimits[2] &&
				y <= backButtonLimits[3])
		{
			touchedBackButton();
		}
	}


	private void touchedBackButton()
	{
		backButtonCurrentTexture = textures.backButtonSelectedTexture;
		renderer.changingFromCreditsMenuToMainMenu();
		currentState = UI_STATE_DISAPPEARING;
	}
}
