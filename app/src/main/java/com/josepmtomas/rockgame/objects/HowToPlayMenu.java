package com.josepmtomas.rockgame.objects;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.programs.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 03/02/2015.
 * @author Josep
 */

public class HowToPlayMenu
{
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private MenuTextures textures;

	// Strife
	private int currentState = UI_STATE_NOT_VISIBLE;

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

	// Panels
	private int currentPanel = 0;
	private int fromPanel = 0;
	private int toPanel = 0;
	private static final float PANEL_TRANSITION_TIME = 0.25f;
	private float panelLeftOffset;
	private float panelRightOffset;

	// Background
	private float[] background9PatchScale = {1f,1f};
	private float[] background9PatchPosition = {0f,0f};
	private float[] background9PatchCurrentScale = new float[2];

	// How to play title
	private float[] howToPlayTitleScale = new float[2];
	private float[] howToPlayTitlePosition = new float[2];
	private float[] howToPlayTitleCurrentScale = new float[2];
	private float[] howToPlayTitleCurrentPosition = new float[2];

	// Back button
	private float[] backButtonScale = new float[2];
	private float[] backButtonCurrentScale = new float[2];
	private float[] backButtonPosition = new float[2];
	private float[] backButtonCurrentPosition = new float[2];
	private float[] backButtonLimits = new float[4];
	private int backButtonCurrentTexture;

	// Objectives panel
	private float[] objectivesScale = new float[2];
	private float[] objectivesPosition = new float[2];
	private float[] objectivesCurrentScale = new float[2];
	private float[] objectivesCurrentPosition = new float[2];
	private float objectivesPanelTimer = 0f;
	private float objectivesCurrentOpacity;
	private int objectivesCurrentState;
	private boolean objectivesDirection;

	// Controls panel
	private float[] controlsScale = new float[2];
	private float[] controlsPosition = new float[2];
	private float[] controlsCurrentScale = new float[2];
	private float[] controlsCurrentPosition = new float[2];
	private float controlsPanelTimer = 0f;
	private float controlsCurrentOpacity;
	private int controlsCurrentState;
	private boolean controlsDirection;

	// Score panel
	private float[] scoreScale = new float[2];
	private float[] scorePosition = new float[2];
	private float[] scoreCurrentScale = new float[2];
	private float[] scoreCurrentPosition = new float[2];
	private float scorePanelTimer = 0f;
	private float scoreCurrentOpacity;
	private int scoreCurrentState;
	private boolean scoreDirection;

	// Lives panel
	private float[] livesScale = new float[2];
	private float[] livesPosition = new float[2];
	private float[] livesCurrentScale = new float[2];
	private float[] livesCurrentPosition = new float[2];
	private float livesPanelTimer = 0f;
	private float livesCurrentOpacity;
	private int livesCurrentState;
	private boolean livesDirection;

	// Pause panel
	private float[] pauseScale = new float[2];
	private float[] pausePosition = new float[2];
	private float[] pauseCurrentScale = new float[2];
	private float[] pauseCurrentPosition = new float[2];
	private float pausePanelTimer = 0f;
	private float pauseCurrentOpacity;
	private int pauseCurrentState;
	private boolean pauseDirection;

	// Left button
	private float[] leftButtonScale = new float[2];
	private float[] leftButtonPosition = new float[2];
	private float[] leftButtonCurrentScale = new float[2];
	private float[] leftButtonCurrentPosition = new float[2];
	private float[] leftButtonLimits = new float[4];
	private float leftButtonCurrentOpacity = 0.5f;
	private int leftButtonCurrentTexture;
	private boolean leftButtonEnabled = false;

	// Right button
	private float[] rightButtonScale = new float[2];
	private float[] rightButtonPosition = new float[2];
	private float[] rightButtonCurrentScale = new float[2];
	private float[] rightButtonCurrentPosition = new float[2];
	private float[] rightButtonLimits = new float[4];
	private float rightButtonCurrentOpacity = 1f;
	private int rightButtonCurrentTexture;
	private boolean rightButtonEnabled = true;



	public HowToPlayMenu(ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.uiPanelProgram = panelProgram;
		this.textures = textures;
		this.renderer = renderer;
		this.currentState = UI_STATE_NOT_VISIBLE;

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.26f, screenHeight * 0.96f, screenHeight * 0.06f, UI_BASE_CENTER_CENTER);

		panelLeftOffset = screenWidth * -0.1f;
		panelRightOffset = screenWidth * 0.1f;

		createMatrices(screenWidth, screenHeight);
		setPositions(screenWidth, screenHeight);
		//setCurrentPositions();
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


	private void setPositions(float screenWidth, float screenHeight)
	{
		float maxPanelSizeX = screenHeight * 0.9f;
		float maxPanelSizeY = screenHeight * 0.9f;

		// Background
		background9PatchScale[0] = 1f;
		background9PatchScale[1] = 1f;
		background9PatchPosition[0] = 0f;
		background9PatchPosition[1] = 0f;
		background9PatchCurrentScale[0] = background9PatchScale[0];
		background9PatchCurrentScale[1] = background9PatchScale[1];

		// How to play title
		howToPlayTitleScale[0] = screenHeight;
		howToPlayTitleScale[1] = screenHeight * 0.1f;
		howToPlayTitlePosition[0] = 0f;
		howToPlayTitlePosition[1] = screenHeight * 0.4f;
		howToPlayTitleCurrentScale[0] = howToPlayTitleScale[0];
		howToPlayTitleCurrentScale[1] = howToPlayTitleScale[1];
		howToPlayTitleCurrentPosition[0] = howToPlayTitlePosition[0];
		howToPlayTitleCurrentPosition[1] = howToPlayTitlePosition[1];

		// Back button
		backButtonScale[0] = screenHeight * 0.3f;
		backButtonScale[1] = screenHeight * 0.1f;
		backButtonPosition[0] = 0f;
		backButtonPosition[1] = screenHeight * -0.4f;
		backButtonCurrentScale[0] = backButtonScale[0];
		backButtonCurrentScale[1] = backButtonScale[1];
		backButtonCurrentPosition[0] = backButtonPosition[0];
		backButtonCurrentPosition[1] = backButtonPosition[1];
		backButtonLimits[0] = backButtonPosition[0] - (screenHeight * 0.15f);
		backButtonLimits[1] = backButtonPosition[0] + (screenHeight * 0.15f);
		backButtonLimits[2] = backButtonPosition[1] - (screenHeight * 0.05f);
		backButtonLimits[3] = backButtonPosition[1] + (screenHeight * 0.05f);
		backButtonCurrentTexture = textures.backButtonIdleTexture;

		// Objectives panel
		objectivesScale[0] = maxPanelSizeX * 0.9f;
		objectivesScale[1] = maxPanelSizeY * 0.22f;
		objectivesPosition[0] = 0f;
		objectivesPosition[1] = 0f;
		objectivesCurrentScale[0] = objectivesScale[0];
		objectivesCurrentScale[1] = objectivesScale[1];
		objectivesCurrentPosition[0] = objectivesPosition[0];
		objectivesCurrentPosition[1] = objectivesPosition[1];
		objectivesCurrentOpacity = 1.0f;
		objectivesCurrentState = UI_STATE_VISIBLE;

		// Controls panel
		controlsScale[0] = maxPanelSizeX * 0.8f;
		controlsScale[1] = maxPanelSizeY * 0.61f;
		controlsPosition[0] = 0f;
		controlsPosition[1] = 0f;
		controlsCurrentScale[0] = controlsScale[0];
		controlsCurrentScale[1] = controlsScale[1];
		controlsCurrentPosition[0] = controlsPosition[0];
		controlsCurrentPosition[1] = controlsPosition[1];
		controlsCurrentOpacity = 0.0f;
		controlsCurrentState = UI_STATE_NOT_VISIBLE;

		// Score panel
		scoreScale[0] = maxPanelSizeX * 0.98f;
		scoreScale[1] = maxPanelSizeY * 0.64f;
		scorePosition[0] = 0f;
		scorePosition[1] = 0f;
		scoreCurrentScale[0] = scoreScale[0];
		scoreCurrentScale[1] = scoreScale[1];
		scoreCurrentPosition[0] = scorePosition[0];
		scoreCurrentPosition[1] = scorePosition[1];
		scoreCurrentOpacity = 0.0f;
		scoreCurrentState = UI_STATE_NOT_VISIBLE;

		// Lives panel
		livesScale[0] = maxPanelSizeX * 0.95f;
		livesScale[1] = maxPanelSizeY * 0.725f;
		livesPosition[0] = 0f;
		livesPosition[1] = 0f;
		livesCurrentScale[0] = livesScale[0];
		livesCurrentScale[1] = livesScale[1];
		livesCurrentPosition[0] = livesPosition[0];
		livesCurrentPosition[1] = livesPosition[1];
		livesCurrentOpacity = 0.0f;
		livesCurrentState = UI_STATE_NOT_VISIBLE;

		// Pause panel
		pauseScale[0] = maxPanelSizeX * 0.9f;
		pauseScale[1] = maxPanelSizeY * 0.33f;
		pausePosition[0] = 0f;
		pausePosition[1] = 0f;
		pauseCurrentScale[0] = pauseScale[0];
		pauseCurrentScale[1] = pauseScale[1];
		pauseCurrentPosition[0] = pausePosition[0];
		pauseCurrentPosition[1] = pausePosition[1];
		pauseCurrentOpacity = 0.0f;
		pauseCurrentState = UI_STATE_NOT_VISIBLE;

		// Left button
		leftButtonScale[0] = screenHeight * 0.15f;
		leftButtonScale[1] = screenHeight * 0.15f;
		leftButtonPosition[0] = screenWidth * -0.5f + (screenHeight * 0.125f);
		leftButtonPosition[1] = 0f;
		leftButtonCurrentScale[0] = leftButtonScale[0];
		leftButtonCurrentScale[1] = leftButtonScale[1];
		leftButtonCurrentPosition[0] = leftButtonPosition[0];
		leftButtonCurrentPosition[1] = leftButtonPosition[1];
		leftButtonLimits[0] = leftButtonPosition[0] - (screenHeight * 0.075f);
		leftButtonLimits[1] = leftButtonPosition[0] + (screenHeight * 0.075f);
		leftButtonLimits[2] = leftButtonPosition[1] - (screenHeight * 0.075f);
		leftButtonLimits[3] = leftButtonPosition[1] + (screenHeight * 0.075f);
		leftButtonCurrentTexture = textures.leftArrowButtonIdle;

		// Right button
		rightButtonScale[0] = screenHeight * 0.15f;
		rightButtonScale[1] = screenHeight * 0.15f;
		rightButtonPosition[0] = screenWidth * 0.5f - (screenHeight * 0.125f);
		rightButtonPosition[1] = 0f;
		rightButtonCurrentScale[0] = rightButtonScale[0];
		rightButtonCurrentScale[1] = rightButtonScale[1];
		rightButtonCurrentPosition[0] = rightButtonPosition[0];
		rightButtonCurrentPosition[1] = rightButtonPosition[1];
		rightButtonLimits[0] = rightButtonPosition[0] - (screenHeight * 0.075f);
		rightButtonLimits[1] = rightButtonPosition[0] + (screenHeight * 0.075f);
		rightButtonLimits[2] = rightButtonPosition[1] - (screenHeight * 0.075f);
		rightButtonLimits[3] = rightButtonPosition[1] + (screenHeight * 0.075f);
		rightButtonCurrentTexture = textures.rightArrowButtonIdle;
	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
		menuTimer = 0f;

		leftButtonCurrentOpacity = 0.25f;
		leftButtonEnabled = false;
		rightButtonCurrentOpacity = 1f;
		rightButtonEnabled = true;
		currentPanel = 0;
		objectivesCurrentOpacity = 1f;
		objectivesCurrentState = UI_STATE_VISIBLE;
		controlsCurrentOpacity = 0f;
		controlsCurrentState = UI_STATE_NOT_VISIBLE;
		scoreCurrentOpacity = 0f;
		scoreCurrentState = UI_STATE_NOT_VISIBLE;
		livesCurrentOpacity = 0f;
		livesCurrentState = UI_STATE_NOT_VISIBLE;
		pauseCurrentOpacity = 0f;
		pauseCurrentState = UI_STATE_NOT_VISIBLE;
	}


	private void setCurrentProperties(float alpha)
	{
		background9PatchCurrentScale[0] = lerp(0, background9PatchScale[0], alpha);
		background9PatchCurrentScale[1] = lerp(0, background9PatchScale[1], alpha);

		howToPlayTitleCurrentScale[0] = lerp(0, howToPlayTitleScale[0], alpha);
		howToPlayTitleCurrentScale[1] = lerp(0, howToPlayTitleScale[1], alpha);
		howToPlayTitleCurrentPosition[0] = lerp(0, howToPlayTitlePosition[0], alpha);
		howToPlayTitleCurrentPosition[1] = lerp(0, howToPlayTitlePosition[1], alpha);

		objectivesCurrentScale[0] = lerp(0, objectivesScale[0], alpha);
		objectivesCurrentScale[1] = lerp(0, objectivesScale[1], alpha);
		objectivesCurrentPosition[0] = lerp(0, objectivesPosition[0], alpha);
		objectivesCurrentPosition[1] = lerp(0, objectivesPosition[1], alpha);

		controlsCurrentScale[0] = lerp(0, controlsScale[0], alpha);
		controlsCurrentScale[1] = lerp(0, controlsScale[1], alpha);
		controlsCurrentPosition[0] = lerp(0, controlsPosition[0], alpha);
		controlsCurrentPosition[1] = lerp(0, controlsPosition[1], alpha);

		scoreCurrentScale[0] = lerp(0, scoreScale[0], alpha);
		scoreCurrentScale[1] = lerp(0, scoreScale[1], alpha);
		scoreCurrentPosition[0] = lerp(0, scorePosition[0], alpha);
		scoreCurrentPosition[1] = lerp(0, scorePosition[1], alpha);

		livesCurrentScale[0] = lerp(0, livesScale[0], alpha);
		livesCurrentScale[1] = lerp(0, livesScale[1], alpha);
		livesCurrentPosition[0] = lerp(0, livesPosition[0], alpha);
		livesCurrentPosition[1] = lerp(0, livesPosition[1], alpha);

		pauseCurrentScale[0] = lerp(0, pauseScale[0], alpha);
		pauseCurrentScale[1] = lerp(0, pauseScale[1], alpha);
		pauseCurrentPosition[0] = lerp(0, pausePosition[0], alpha);
		pauseCurrentPosition[1] = lerp(0, pausePosition[1], alpha);

		backButtonCurrentScale[0] = lerp(0, backButtonScale[0], alpha);
		backButtonCurrentScale[1] = lerp(0, backButtonScale[1], alpha);
		backButtonCurrentPosition[0] = lerp(0, backButtonPosition[0], alpha);
		backButtonCurrentPosition[1] = lerp(0, backButtonPosition[1], alpha);

		leftButtonCurrentScale[0] = lerp(0, leftButtonScale[0], alpha);
		leftButtonCurrentScale[1] = lerp(0, leftButtonScale[1], alpha);
		leftButtonCurrentPosition[0] = lerp(0, leftButtonPosition[0], alpha);
		leftButtonCurrentPosition[1] = lerp(0, leftButtonPosition[1], alpha);

		rightButtonCurrentScale[0] = lerp(0, rightButtonScale[0], alpha);
		rightButtonCurrentScale[1] = lerp(0, rightButtonScale[1], alpha);
		rightButtonCurrentPosition[0] = lerp(0, rightButtonPosition[0], alpha);
		rightButtonCurrentPosition[1] = lerp(0, rightButtonPosition[1], alpha);
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
				renderer.changedToHowToPlayMenu();
			}

			setCurrentProperties(menuOpacity);
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
				backButtonCurrentTexture = textures.backButtonIdleTexture;
			}

			setCurrentProperties(menuOpacity);
		}

		// Objectives transition
		if(objectivesCurrentState == UI_STATE_APPEARING)
		{
			objectivesPanelTimer += deltaTime;
			objectivesCurrentOpacity = objectivesPanelTimer / PANEL_TRANSITION_TIME;

			if(objectivesPanelTimer >= PANEL_TRANSITION_TIME)
			{
				objectivesPanelTimer = 0f;
				objectivesCurrentOpacity = 1f;
				objectivesCurrentState = UI_STATE_VISIBLE;
			}

			if(objectivesDirection)
				objectivesCurrentPosition[0] = lerp(panelLeftOffset, objectivesPosition[0], objectivesCurrentOpacity);
			else
				objectivesCurrentPosition[0] = lerp(objectivesPosition[0], panelRightOffset, objectivesCurrentOpacity);
		}
		else if(objectivesCurrentState ==UI_STATE_DISAPPEARING)
		{
			objectivesPanelTimer += deltaTime;
			objectivesCurrentOpacity = 1.0f - (objectivesPanelTimer / PANEL_TRANSITION_TIME);

			if(objectivesPanelTimer >= PANEL_TRANSITION_TIME)
			{
				objectivesPanelTimer = 0f;
				objectivesCurrentOpacity = 0f;
				objectivesCurrentState = UI_STATE_NOT_VISIBLE;
			}

			if(objectivesDirection)
				objectivesCurrentPosition[0] = lerp(objectivesPosition[0], panelRightOffset, objectivesCurrentOpacity);
			else
				objectivesCurrentPosition[0] = lerp(panelLeftOffset, objectivesPosition[0], objectivesCurrentOpacity);

		}

		// Controls transition
		if(controlsCurrentState == UI_STATE_APPEARING)
		{
			controlsPanelTimer += deltaTime;
			controlsCurrentOpacity = controlsPanelTimer / PANEL_TRANSITION_TIME;

			if(controlsPanelTimer >= PANEL_TRANSITION_TIME)
			{
				controlsPanelTimer = 0f;
				controlsCurrentOpacity = 1f;
				controlsCurrentState = UI_STATE_VISIBLE;
			}

			if(controlsDirection)
				controlsCurrentPosition[0] = lerp(panelLeftOffset, controlsPosition[0], controlsCurrentOpacity);
			else
				controlsCurrentPosition[0] = lerp(panelRightOffset, controlsPosition[0], controlsCurrentOpacity);
		}
		else if(controlsCurrentState == UI_STATE_DISAPPEARING)
		{
			controlsPanelTimer += deltaTime;
			controlsCurrentOpacity = 1.0f - (controlsPanelTimer / PANEL_TRANSITION_TIME);

			if(controlsPanelTimer >= PANEL_TRANSITION_TIME)
			{
				controlsPanelTimer = 0f;
				controlsCurrentOpacity = 0f;
				controlsCurrentState = UI_STATE_NOT_VISIBLE;
			}

			if(controlsDirection)
				controlsCurrentPosition[0] = lerp(panelRightOffset, controlsPosition[0], controlsCurrentOpacity);
			else
				controlsCurrentPosition[0] = lerp(panelLeftOffset, controlsPosition[0], controlsCurrentOpacity);

		}

		// Score transition
		if(scoreCurrentState == UI_STATE_APPEARING)
		{
			scorePanelTimer += deltaTime;
			scoreCurrentOpacity = scorePanelTimer / PANEL_TRANSITION_TIME;

			if(scorePanelTimer >= PANEL_TRANSITION_TIME)
			{
				scorePanelTimer = 0f;
				scoreCurrentOpacity = 1f;
				scoreCurrentState = UI_STATE_VISIBLE;
			}

			if(scoreDirection)
				scoreCurrentPosition[0] = lerp(panelLeftOffset, scorePosition[0], scoreCurrentOpacity);
			else
				scoreCurrentPosition[0] = lerp(panelRightOffset, scorePosition[0], scoreCurrentOpacity);
		}
		else if(scoreCurrentState == UI_STATE_DISAPPEARING)
		{
			scorePanelTimer += deltaTime;
			scoreCurrentOpacity = 1.0f - (scorePanelTimer / PANEL_TRANSITION_TIME);

			if(scorePanelTimer >= PANEL_TRANSITION_TIME)
			{
				scorePanelTimer = 0f;
				scoreCurrentOpacity = 0f;
				scoreCurrentState = UI_STATE_NOT_VISIBLE;
			}

			if(scoreDirection)
				scoreCurrentPosition[0] = lerp(panelRightOffset, scorePosition[0], scoreCurrentOpacity);
			else
				scoreCurrentPosition[0] = lerp(panelLeftOffset, scorePosition[0], scoreCurrentOpacity);
		}

		// Lives transition
		if(livesCurrentState == UI_STATE_APPEARING)
		{
			livesPanelTimer += deltaTime;
			livesCurrentOpacity = livesPanelTimer / PANEL_TRANSITION_TIME;

			if(livesPanelTimer >= PANEL_TRANSITION_TIME)
			{
				livesPanelTimer = 0f;
				livesCurrentOpacity = 1f;
				livesCurrentState = UI_STATE_VISIBLE;
			}

			if(livesDirection)
				livesCurrentPosition[0] = lerp(panelLeftOffset, livesPosition[0], livesCurrentOpacity);
			else
				livesCurrentPosition[0] = lerp(panelRightOffset, livesPosition[0], livesCurrentOpacity);
		}
		else if(livesCurrentState == UI_STATE_DISAPPEARING)
		{
			livesPanelTimer += deltaTime;
			livesCurrentOpacity = 1.0f - (livesPanelTimer / PANEL_TRANSITION_TIME);

			if(livesPanelTimer >= PANEL_TRANSITION_TIME)
			{
				livesPanelTimer = 0f;
				livesCurrentOpacity = 0f;
				livesCurrentState = UI_STATE_NOT_VISIBLE;
			}

			if(livesDirection)
				livesCurrentPosition[0] = lerp(panelRightOffset, livesPosition[0], livesCurrentOpacity);
			else
				livesCurrentPosition[0] = lerp(panelLeftOffset, livesPosition[0], livesCurrentOpacity);
		}

		// Pause transition
		if(pauseCurrentState == UI_STATE_APPEARING)
		{
			pausePanelTimer += deltaTime;
			pauseCurrentOpacity = pausePanelTimer / PANEL_TRANSITION_TIME;

			if(pausePanelTimer >= PANEL_TRANSITION_TIME)
			{
				pausePanelTimer = 0f;
				pauseCurrentOpacity = 1f;
				pauseCurrentState = UI_STATE_VISIBLE;
			}

			if(pauseDirection)
				pauseCurrentPosition[0] = lerp(panelLeftOffset, pausePosition[0], pauseCurrentOpacity);
			else
				pauseCurrentPosition[0] = lerp(panelRightOffset, pausePosition[0], pauseCurrentOpacity);
		}
		else if(pauseCurrentState == UI_STATE_DISAPPEARING)
		{
			pausePanelTimer += deltaTime;
			pauseCurrentOpacity = 1.0f - (pausePanelTimer / PANEL_TRANSITION_TIME);

			if(pausePanelTimer >= PANEL_TRANSITION_TIME)
			{
				pausePanelTimer = 0f;
				pauseCurrentOpacity = 0f;
				pauseCurrentState = UI_STATE_NOT_VISIBLE;
			}

			if(pauseDirection)
				pauseCurrentPosition[0] = lerp(panelRightOffset, pausePosition[0], pauseCurrentOpacity);
			else
				pauseCurrentPosition[0] = lerp(panelLeftOffset, pausePosition[0], pauseCurrentOpacity);
		}
	}


	public void draw()
	{
		if(currentState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();

			// Background
			glBindVertexArray(ui9PatchVaoHandle);
			uiPanelProgram.setUniforms(viewProjection, background9PatchCurrentScale, background9PatchPosition, textures.background9PatchPanelTexture, menuOpacity * 0.75f);
			glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

			glBindVertexArray(uiPanelVaoHandle);

			// Options title
			uiPanelProgram.setUniforms(viewProjection, howToPlayTitleCurrentScale, howToPlayTitleCurrentPosition, textures.howToPlayTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Back button
			uiPanelProgram.setUniforms(viewProjection, backButtonCurrentScale, backButtonCurrentPosition, backButtonCurrentTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Objectives panel
			if(objectivesCurrentState != UI_STATE_NOT_VISIBLE)
			{
				uiPanelProgram.setUniforms(viewProjection, objectivesCurrentScale, objectivesCurrentPosition, textures.howToPlayObjectivesPanelTexture, menuOpacity * objectivesCurrentOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Controls panel
			if(controlsCurrentState != UI_STATE_NOT_VISIBLE)
			{
				uiPanelProgram.setUniforms(viewProjection, controlsCurrentScale, controlsCurrentPosition, textures.howToPlayControlsPanelTexture, menuOpacity * controlsCurrentOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Score panel
			if(scoreCurrentState != UI_STATE_NOT_VISIBLE)
			{
				uiPanelProgram.setUniforms(viewProjection, scoreCurrentScale, scoreCurrentPosition, textures.howToPlayScorePanelTexture, menuOpacity * scoreCurrentOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Lives panel
			if(livesCurrentState != UI_STATE_NOT_VISIBLE)
			{
				uiPanelProgram.setUniforms(viewProjection, livesCurrentScale, livesCurrentPosition, textures.howToPlayLivesPanelTexture, menuOpacity * livesCurrentOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Pause panel
			if(pauseCurrentState != UI_STATE_NOT_VISIBLE)
			{
				uiPanelProgram.setUniforms(viewProjection, pauseCurrentScale, pauseCurrentPosition, textures.howToPlayPausePanelTexture, menuOpacity * pauseCurrentOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Left button
			uiPanelProgram.setUniforms(viewProjection, leftButtonCurrentScale, leftButtonCurrentPosition, leftButtonCurrentTexture, menuOpacity * leftButtonCurrentOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			// Right button
			uiPanelProgram.setUniforms(viewProjection, rightButtonCurrentScale, rightButtonCurrentPosition, rightButtonCurrentTexture, menuOpacity * rightButtonCurrentOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}


	public void touch(float x, float y)
	{
		if(	x >= leftButtonLimits[0] &&
			x <= leftButtonLimits[1] &&
			y >= leftButtonLimits[2] &&
			y <= leftButtonLimits[3] &&
			leftButtonEnabled)
		{
			touchedLeftButton();
		}
		else if(x >= rightButtonLimits[0] &&
				x <= rightButtonLimits[1] &&
				y >= rightButtonLimits[2] &&
				y <= rightButtonLimits[3] &&
				rightButtonEnabled)
		{
			touchedRightButton();
		}
		else if(x >= backButtonLimits[0] &&
				x <= backButtonLimits[1] &&
				y >= backButtonLimits[2] &&
				y <= backButtonLimits[3])
		{
			touchedBackButton();
		}
	}


	private void touchedLeftButton()
	{
		leftButtonCurrentTexture = textures.leftArrowButtonSelected;

		toPanel = currentPanel - 1;
		fromPanel = currentPanel;
		currentPanel = toPanel;

		if(toPanel == 0)
		{
			leftButtonCurrentOpacity = 0.25f;
			leftButtonEnabled = false;
		}

		if(fromPanel == 4)
		{
			rightButtonCurrentOpacity = 1f;
			rightButtonEnabled = true;
		}

		switch(toPanel)
		{
			case 0:
				objectivesCurrentState = UI_STATE_APPEARING;
				objectivesDirection = true;
				break;
			case 1:
				controlsCurrentState = UI_STATE_APPEARING;
				controlsDirection = true;
				break;
			case 2:
				scoreCurrentState = UI_STATE_APPEARING;
				scoreDirection = true;
				break;
			case 3:
				livesCurrentState = UI_STATE_APPEARING;
				livesDirection = true;
				break;
		}

		switch(fromPanel)
		{
			case 1:
				controlsCurrentState = UI_STATE_DISAPPEARING;
				controlsDirection = true;
				break;
			case 2:
				scoreCurrentState = UI_STATE_DISAPPEARING;
				scoreDirection = true;
				break;
			case 3:
				livesCurrentState = UI_STATE_DISAPPEARING;
				livesDirection = true;
				break;
			case 4:
				pauseCurrentState = UI_STATE_DISAPPEARING;
				pauseDirection = true;
				break;
		}
	}


	private void touchedRightButton()
	{
		rightButtonCurrentTexture = textures.rightArrowButtonSelected;

		toPanel = currentPanel + 1;
		fromPanel = currentPanel;
		currentPanel = toPanel;

		if(toPanel == 4)
		{
			rightButtonCurrentOpacity = 0.25f;
			rightButtonEnabled = false;
		}

		if(fromPanel == 0)
		{
			leftButtonCurrentOpacity = 1f;
			leftButtonEnabled = true;
		}

		switch(toPanel)
		{
			case 1:
				controlsCurrentState = UI_STATE_APPEARING;
				controlsDirection = false;
				break;
			case 2:
				scoreCurrentState = UI_STATE_APPEARING;
				scoreDirection = false;
				break;
			case 3:
				livesCurrentState = UI_STATE_APPEARING;
				livesDirection = false;
				break;
			case 4:
				pauseCurrentState = UI_STATE_APPEARING;
				pauseDirection = false;
				break;
		}

		switch(fromPanel)
		{
			case 0:
				objectivesCurrentState = UI_STATE_DISAPPEARING;
				objectivesDirection = false;
				break;
			case 1:
				controlsCurrentState = UI_STATE_DISAPPEARING;
				controlsDirection = false;
				break;
			case 2:
				scoreCurrentState = UI_STATE_DISAPPEARING;
				scoreDirection = false;
				break;
			case 3:
				livesCurrentState = UI_STATE_DISAPPEARING;
				livesDirection = false;
				break;
		}
	}


	private void touchedBackButton()
	{
		backButtonCurrentTexture = textures.backButtonSelectedTexture;
		currentState = UI_STATE_DISAPPEARING;
		renderer.changingFromHowToPlayMenuToMainMenu();
	}


	public void releaseTouch()
	{
		leftButtonCurrentTexture = textures.leftArrowButtonIdle;
		rightButtonCurrentTexture = textures.rightArrowButtonIdle;
	}
}
