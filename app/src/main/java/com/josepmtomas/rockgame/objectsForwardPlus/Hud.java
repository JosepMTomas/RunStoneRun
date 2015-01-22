package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.programsForwardPlus.ProgressBarProgram;
import com.josepmtomas.rockgame.programsForwardPlus.ScorePanelProgram;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

/**
 * Created by Josep on 29/11/2014.
 * @author Josep
 */
public class Hud
{
	private static String TAG = "HUD";

	private ForwardPlusRenderer renderer;
	private MenuTextures textures;

	// Geometry attributes constants
	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = 3 * BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = 5 * BYTES_PER_FLOAT;

	// Life states
	private static final int LIFE_OK = 0;
	private static final int LIFE_LOSING = 1;
	private static final int LIFE_LOST = 2;

	// Life timer states
	private static final int LIVES_TIMER_IDLE = 0;
	private static final int LIVES_TIMER_COUNTING = 1;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Colors
	private float[] scoreColor = {1f, 1f, 1f};
	private float[] recoveringColor = {0.75f, 1f, 0.75f};

	// Pause button
	private int pauseButtonVaoHandle;
	private int pauseButtonCurrentTexture;
	private float[] pauseButtonScale = new float[2];
	private float[] pauseButtonPosition = new float[2];
	private float[] pauseButtonLimits = new float[4];

	// Pause button state control
	private int pauseButtonState = UI_STATE_NOT_VISIBLE;
	private float[] pauseButtonCurrentScale = new float[2];
	private float[] pauseButtonCurrentPosition = new float[2];
	private float pauseButtonTimer = 0f;
	private float pauseButtonOpacity = 0f;
	private float pauseButtonAppearTime = 0.5f;
	private float pauseButtonDisappearTime = 0.5f;

	// Score panel
	private int[] scoreVboHandles = new int[2];
	private int[] scoreVaoHandle = new int[1];
	private float[] scoreOpacities = new float[8];
	private float[] scorePositionsX = new float[8];//{1700f, 1572f, 1444f, 1316f, 1188f, 1060f, 932f, 804f};
	private float scorePositionY = 0;
	private float[] scoreTexCoordOffsetsX = {    0f,    0f,     0f, 0f,  0.25f, 0.25f,  0.25f, 0.25f,   0.5f,  0.5f,   0.5f, 0.5f,  0.75f, 0.75f};
	private float[] scoreTexCoordOffsetsY = {-0.75f, -0.5f, -0.25f, 0f, -0.75f, -0.5f, -0.25f,    0f, -0.75f, -0.5f, -0.25f,   0f, -0.75f, -0.5f};
	private float scoreOpacity = 0f;
	private int[] scoreNumbers = {0, 0, 0, 0, 0, 0, 0, 0};

	// Score panel state control
	private int scoreCurrentState = UI_STATE_NOT_VISIBLE;
	private float scoreCurrentScale = 1f;
	private float[] scoreCurrentPositionsX = new float[8];
	private float scoreCurrentPositionY = 0f;
	private float scoreTimer = 0f;
	private float scoreAppearTime = 0.5f;
	private float scoreDisappearTime = 0.5f;

	// Multiplier panel
	private float[] multiplierNumbersPositionsX = new float[4];
	private float multiplierNumbersPositionY;
	private int[] multiplierNumbers = {0, 13, 0, 12};

	// Multiplier panel state control
	private int multiplierNumbersCurrentState = UI_STATE_NOT_VISIBLE;
	private float multiplierNumbersCurrentScale = 1f;
	private float[] multiplierNumbersCurrentPositionsX = new float[4];
	private float multiplierNumbersCurrentPositionY = 0f;
	private float multiplierNumbersTimer = 0f;
	private float multiplierNumbersAppearTime = 0.5f;
	private float multiplierNumbersDisappearTime = 0.5f;
	private float multiplierNumbersOpacity = 1f;

	// Multiplier progress bar
	private int multiplierProgressVaoHandle;
	private float multiplierProgressValue = 0f;
	private float multiplierProgressPositionX = 0;
	private float multiplierProgressPositionY = 0;

	// Multiplier progress bar state control
	private int multiplierProgressCurrentState = UI_STATE_NOT_VISIBLE;
	private float multiplierProgressCurrentScale = 1f;
	private float multiplierProgressCurrentPositionX = 0f;
	private float multiplierProgressCurrentPositionY = 0f;
	private float multiplierProgressTimer = 0f;
	private float multiplierProgressAppearTime = 0.5f;
	private float multiplierProgressDisappearTime = 0.5f;
	private float multiplierProgressOpacity = 1f;

	// Recovering progress bar
	private int recoveringProgressBarVaoHandle;
	private int recoveringProgressBarState = UI_STATE_NOT_VISIBLE;
	private float recoveringProgressBarTimer = 0f;
	private float recoveringProgressBarOpacity = 0f;
	private float recoveringProgressBarPercent = 0f;
	private float[] recoveringTimers = {0.5f, PLAYER_RECOVERING_TIME - 0.5f, 0.5f};
	private float recoveringCurrentPositionY = 0f;
	private float recoveringInitialPositionY = -500f;
	private float recoveringFinalPositionY = 0f;

	// "Get Ready" panel
	private int getReadyPanelTexture;
	private int getReadyPanelVaoHandle;
	private int getReadyPanelState = UI_STATE_NOT_VISIBLE;
	private float getReadyPanelTimer = 0f;
	private float getReadyPanelOpacity = 0f;
	private float[] getReadyPanelTimers = {0.5f, PLAYER_RECOVERING_TIME - 0.5f, 0.25f};
	private float getReadyPanelCurrentPositionY = 0f;
	private float getReadyPanelInitialPositionY = -500f;
	private float getReadyPanelFinalPositionY = 0f;

	// "Resuming" panel
	private int resumingPanelTexture;
	private int resumingPanelVaoHandle;
	private int resumingPanelState = UI_STATE_NOT_VISIBLE;
	private float resumingPanelTimer = 0f;
	private float resumingPanelOpacity = 0f;
	private float[] resumingPanelTimes = {0.25f, PLAYER_RESUMING_TIME - 0.25f, 0.25f};
	private float resumingPanelCurrentPositionY = 0f;
	private float resumingPanelInitialPositionY = -500f;
	private float resumingPanelFinalPositionY = 0f;

	// Resuming progress bar
	private int resumingProgressBarVaoHandle;
	private int resumingProgressBarState = UI_STATE_NOT_VISIBLE;
	private float resumingProgressBarTimer = 0f;
	private float resumingProgressBarOpacity = 0f;
	private float resumingProgressBarPercent = 0f;
	private float[] resumingTimers = {0.5f, PLAYER_RESUMING_TIME - 0.5f, 0.5f};
	private float resumingCurrentPositionY = 0f;
	private float resumingInitialPositionY = -500f;
	private float resumingFinalPositionY = 0f;

	// Lives
	private int lifeBarVaoHandle;
	private int currentLife = 2;
	private int livesCounterState = LIVES_TIMER_IDLE;
	private int[] livesStates = {LIFE_OK, LIFE_OK, LIFE_OK, LIFE_LOST, LIFE_LOST};
	private float[] livesPercents = {1f, 1f, 1f, 0f, 0f};
	private float[] livesPositionsX = new float[5];
	private float livesPositionY;
	private float lifeRecoverPercent;
	private float lifeRecoverTimer;
	private float nextLifeCounter = 0f;

	// Lives state control
	private int livesState = UI_STATE_NOT_VISIBLE;
	private float livesTimer = 0f;
	private float livesAppearTime = 0.5f;
	private float livesDisappearTime = 0.5f;
	private float livesOpacity = 0f;
	private float livesCurrentScale = 1f;
	private float livesCurrentPositionY;
	private float[] livesCurrentPositionsX = new float[5];


	// Frames per second panel
	private float[] fpsPositionOffsetsX = {-1572f, -1700f};
	private float currentFpsPositionOffsetY;
	private float averageFpsPositionOffsetY;
	private int[] currentFpsNumbers = {0, 0};
	private int[] averageFpsNumbers = {0, 0};

	// Programs
	UIPanelProgram uiPanelProgram;
	ScorePanelProgram scorePanelProgram;
	ProgressBarProgram progressBarProgram;

	private static final float NUMBER_HEIGHT_PERCENTAGE = 0.15f;


	public Hud(Context context, ForwardPlusRenderer renderer, UIPanelProgram uiPanelProgram, ScorePanelProgram scorePanelProgram, MenuTextures menuTextures, float screenWidth, float screenHeight)
	{
		this.renderer = renderer;
		this.uiPanelProgram = uiPanelProgram;
		this.scorePanelProgram = scorePanelProgram;
		this.textures = menuTextures;

		float progressBarHeight = screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.15f;
		float progressBarWidth = screenHeight * 0.9f;
		float numberHeight =  screenHeight * NUMBER_HEIGHT_PERCENTAGE;
		float numberWidth = numberHeight * 0.7134f;

		// Common matrices
		createMatrices(screenWidth, screenHeight);

		// Pause button
		createPauseButton(screenWidth, screenHeight);

		// Score panel
		createScoreGeometry(screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f, screenHeight * NUMBER_HEIGHT_PERCENTAGE);

		// Multiplier progress bar
		multiplierProgressVaoHandle = UIHelper.makeProgressBar((screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f) * 8, progressBarHeight, UI_BASE_LEFT_CENTER);

		// Recovering
		recoveringProgressBarVaoHandle = UIHelper.makeProgressBar(progressBarWidth, progressBarHeight, UI_BASE_CENTER_CENTER);
		getReadyPanelVaoHandle = UIHelper.makePanel(numberWidth * 9f, numberHeight * 0.9f, UI_BASE_CENTER_CENTER);
		getReadyPanelTexture = TextureHelper.loadETC2Texture(context, "textures/hud/get_ready.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Resuming
		resumingPanelVaoHandle = getReadyPanelVaoHandle;
		resumingPanelTexture = TextureHelper.loadETC2Texture(context, "textures/hud/resuming.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resumingProgressBarVaoHandle = recoveringProgressBarVaoHandle;

		// Life bars
		lifeBarVaoHandle = UIHelper.makeProgressBar(progressBarWidth * 0.2f, progressBarHeight, UI_BASE_CENTER_CENTER);

		// Positions
		setPositions(screenWidth, screenHeight, (screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f) , screenHeight * NUMBER_HEIGHT_PERCENTAGE);

		// Programs
		progressBarProgram = new ProgressBarProgram(context);
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


	private void createPauseButton(float screenWidth, float screenHeight)
	{
		float buttonHeight = screenHeight * 0.1f;
		float buttonWidth = buttonHeight * 3f;
		float buttonHeightHalf = buttonHeight * 0.5f;
		float buttonWidthHalf = buttonWidth * 0.5f;

		pauseButtonScale[0] = buttonWidth;
		pauseButtonScale[1] = buttonHeight;
		pauseButtonCurrentScale[0] = pauseButtonScale[0];
		pauseButtonCurrentScale[1] = pauseButtonScale[1];

		pauseButtonPosition[0] = screenWidth * 0.5f - buttonWidthHalf - (buttonHeightHalf * 0.5f);
		pauseButtonPosition[1] = screenHeight * -0.5f + buttonHeightHalf + (buttonHeightHalf * 0.5f);
		pauseButtonCurrentPosition[0] = pauseButtonPosition[0];
		pauseButtonCurrentPosition[1] = pauseButtonPosition[1];

		// left-right-bottom-top
		pauseButtonLimits[0] = pauseButtonPosition[0] - buttonWidthHalf;
		pauseButtonLimits[1] = pauseButtonPosition[0] + buttonWidthHalf;
		pauseButtonLimits[2] = pauseButtonPosition[1] - buttonHeightHalf;
		pauseButtonLimits[3] = pauseButtonPosition[1] + buttonHeightHalf;

		pauseButtonVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);

		pauseButtonCurrentTexture = textures.pauseButtonIdleTexture;
	}


	private void createScoreGeometry(float numberWidth, float numberHeight)
	{
		float[] vertices = new float[20];
		short[] elements = new short[6];

		float bottom = -numberHeight * 0.5f;
		float left = -numberWidth * 0.5f;

		// D - C
		// | \ |
		// A - B

		int offset = 0;

		for(int y=0; y<2; y++)
		{
			for(int x=0; x<2; x++)
			{
				// Position
				vertices[offset++] = left + ((float)x * numberWidth);
				vertices[offset++] = bottom + ((float)y * numberHeight);
				vertices[offset++] = 0.0f;

				// Texture Coordinates
				vertices[offset++] = (float)x * 0.25f;
				vertices[offset++] = 1.0f - (float)y * 0.25f;
			}
		}

		// Indices
		elements[0] = 0;
		elements[1] = 1;
		elements[2] = 2;

		elements[3] = 1;
		elements[4] = 3;
		elements[5] = 2;

		// Java native buffers
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		verticesBuffer = ByteBuffer
				.allocateDirect(vertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, scoreVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, scoreVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, scoreVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, scoreVaoHandle, 0);
		glBindVertexArray(scoreVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, scoreVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, scoreVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, scoreVboHandles[1]);

		glBindVertexArray(0);
	}


	private void setPositions(float screenWidth, float screenHeight, float numberWidth, float numberHeight)
	{
		float initialOffsetX = screenWidth - (numberWidth * 0.5f) - (numberWidth/2);

		scorePositionY = screenHeight - (numberHeight * 0.5f) - (numberWidth/2);
		scoreCurrentPositionY = scorePositionY;
		scorePositionsX[0] = initialOffsetX;
		scoreCurrentPositionsX[0] = scorePositionsX[0];

		for(int i=1; i < 8; i++)
		{
			scoreOpacities[i] = 0.75f;
			scorePositionsX[i] = initialOffsetX - ((float)i * numberWidth);
			scoreCurrentPositionsX[i] = scorePositionsX[i];
		}

		/////////// multiplier

		float multiplierBaseOffsetX = screenWidth - (numberWidth / 2f);
		float multiplierBaseOffsetY = screenHeight - numberHeight * 4f;

		multiplierNumbersPositionY = multiplierBaseOffsetY + numberHeight * 1.25f;
		multiplierNumbersPositionsX[0] = multiplierBaseOffsetX - (numberWidth * 0.75f);
		multiplierNumbersPositionsX[1] = multiplierNumbersPositionsX[0] - numberWidth * 0.75f;
		multiplierNumbersPositionsX[2] = multiplierNumbersPositionsX[1] - numberWidth * 0.75f;
		multiplierNumbersPositionsX[3] = multiplierNumbersPositionsX[2] - numberWidth;

		multiplierNumbersCurrentPositionY = multiplierNumbersPositionY;
		multiplierNumbersCurrentPositionsX[0] = multiplierNumbersPositionsX[0];
		multiplierNumbersCurrentPositionsX[1] = multiplierNumbersPositionsX[1];
		multiplierNumbersCurrentPositionsX[2] = multiplierNumbersPositionsX[2];
		multiplierNumbersCurrentPositionsX[3] = multiplierNumbersPositionsX[3];

		multiplierProgressPositionY = multiplierBaseOffsetY + numberHeight * 2.25f - (screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.075f);
		multiplierProgressPositionX = scorePositionsX[7] - numberWidth * 0.5f; //multiplierBaseOffsetX - numberHeight * 4.25f;

		multiplierProgressCurrentPositionY = multiplierProgressPositionY;
		multiplierProgressCurrentPositionX = multiplierProgressPositionX;

		// Recovering

		recoveringInitialPositionY = -screenHeight;
		recoveringFinalPositionY = -screenHeight * 0.75f;

		// Get Ready panel

		getReadyPanelInitialPositionY = -screenHeight;
		getReadyPanelFinalPositionY = -screenHeight * 0.65f;

		// Resuming panel

		resumingPanelInitialPositionY = -screenHeight;
		resumingPanelFinalPositionY = -screenHeight * 0.4f;

		resumingInitialPositionY = -screenHeight;
		resumingFinalPositionY = -screenHeight * 0.5f;

		// Lives

		float livesSpacing = screenWidth * 0.125f;
		initialOffsetX = livesSpacing * -2f;

		for(int i=0; i < 5; i++)
		{
			livesPositionsX[i] = initialOffsetX + (i * livesSpacing);
			livesCurrentPositionsX[i] = livesPositionsX[i];
		}
		livesPositionY = screenHeight * -0.85f;
		livesCurrentPositionY = livesPositionY;

		// FPS

		fpsPositionOffsetsX[1] = -screenWidth + numberWidth;
		fpsPositionOffsetsX[0] = fpsPositionOffsetsX[1] + numberWidth;
		currentFpsPositionOffsetY = screenHeight * 0.9f;
		averageFpsPositionOffsetY = screenHeight * 0.75f;
	}


	private void resetPauseButtonTexture()
	{
		pauseButtonCurrentTexture = textures.pauseButtonIdleTexture;
	}


	public void newGame()
	{
		livesPercents[0] = 1f;
		livesPercents[1] = 1f;
		livesPercents[2] = 1f;
		livesPercents[3] = 0f;
		livesPercents[4] = 0f;

		livesStates[0] = LIFE_OK;
		livesStates[1] = LIFE_OK;
		livesStates[2] = LIFE_OK;
		livesStates[3] = LIFE_LOST;
		livesStates[4] = LIFE_LOST;

		currentLife = 2;
		nextLifeCounter = 0;

		livesCounterState = LIVES_TIMER_IDLE;

		recoveringProgressBarTimer = 0f;
		recoveringProgressBarState = UI_STATE_NOT_VISIBLE;
		getReadyPanelState = UI_STATE_NOT_VISIBLE;
	}


	public void resume()
	{
		livesCounterState = LIVES_TIMER_COUNTING;
		resetPauseButtonTexture();
		resumingPanelTimer = 0f;
		resumingPanelState = UI_STATE_APPEARING;
		resumingProgressBarTimer = 0f;
		resumingProgressBarState = UI_STATE_APPEARING;
	}


	public void endGame()
	{
		getReadyPanelTimer = 0f;
		getReadyPanelState = UI_STATE_NOT_VISIBLE;
		livesCounterState = LIVES_TIMER_IDLE;
		lifeRecoverTimer = 0f;
		recoveringProgressBarTimer = 0f;
		recoveringProgressBarState = UI_STATE_NOT_VISIBLE;
	}


	public void setAppearing()
	{
		resetPauseButtonTexture();
		scoreCurrentState = UI_STATE_APPEARING;
		multiplierProgressCurrentState = UI_STATE_NOT_VISIBLE;
		multiplierNumbersCurrentState = UI_STATE_NOT_VISIBLE;
		livesState = UI_STATE_NOT_VISIBLE;
		pauseButtonState = UI_STATE_NOT_VISIBLE;
	}

	public void setDisappearing()
	{
		scoreCurrentState = UI_STATE_DISAPPEARING;
		multiplierProgressCurrentState = UI_STATE_DISAPPEARING;
		multiplierNumbersCurrentState = UI_STATE_DISAPPEARING;
		livesState = UI_STATE_DISAPPEARING;
		pauseButtonState = UI_STATE_DISAPPEARING;
	}


	public boolean touch(float x, float y)
	{
		if(	x >= pauseButtonLimits[0] &&
			x <= pauseButtonLimits[1] &&
			y >= pauseButtonLimits[2] &&
			y <= pauseButtonLimits[3])
		{
			renderer.setPause(true);
			pauseButtonCurrentTexture = textures.pauseButtonSelectedTexture;
			return true;
		}
		return false;
	}


	public void update(int currentScore, float scoreIncrement, int currentMultiplier, float multiplierPercent, int currentFps, float deltaTime)
	{
		multiplierProgressValue = multiplierPercent;

		nextLifeCounter += scoreIncrement;
		if(nextLifeCounter >= 50000f)
		{
			nextLifeCounter -= 50000f;

			if(livesStates[currentLife] != LIFE_OK)
			{
				livesStates[currentLife+1] = livesStates[currentLife];
				livesPercents[currentLife+1] = livesPercents[currentLife];
				livesStates[currentLife] = LIFE_OK;
				livesPercents[currentLife] = 1f;
				currentLife++;
			}
			else
			{
				currentLife++;
				currentLife = Math.min(currentLife, 4);
				livesStates[currentLife] = LIFE_OK;
				livesPercents[currentLife] = 1f;
			}
		}

		int i;
		boolean end = false;
		int number;
		int nextScore = currentScore;

		for(i=0; i<8; i++)
		{
			scoreOpacities[i] = 0.25f;
			scoreNumbers[i] = 0;
		}
		i=0;

		while(i < 8 && !end)
		{
			number = nextScore % 10;
			nextScore = nextScore / 10;

			scoreOpacities[i] = 1f;
			scoreNumbers[i] = number;

			if(nextScore == 0) end = true;

			i++;
		}

		multiplierNumbers[0] = currentMultiplier % 10;
		multiplierNumbers[2] = currentMultiplier / 10;

		currentFpsNumbers[0] = currentFps % 10;
		number = currentFps / 10;
		currentFpsNumbers[1] = number % 10;
		number = number / 10;
		averageFpsNumbers[0] = number % 10;
		number = number / 10;
		averageFpsNumbers[1] = number;

		// Score Panel
		if(scoreCurrentState == UI_STATE_APPEARING)
		{
			scoreTimer += deltaTime;
			scoreOpacity = scoreTimer / scoreAppearTime;

			if(scoreTimer >= scoreAppearTime)
			{
				scoreTimer = 0f;
				scoreOpacity = 1f;
				scoreCurrentState = UI_STATE_VISIBLE;
				multiplierProgressCurrentState = UI_STATE_APPEARING;
			}

			scoreCurrentScale = lerp(2f, 1f, scoreOpacity);
			scoreCurrentPositionY = lerp(scorePositionY * 2f, scorePositionY, scoreOpacity);

			for(i=0; i<8; i++)
			{
				scoreCurrentPositionsX[i] = lerp(scorePositionsX[i] * 2f, scorePositionsX[i], scoreOpacity);
			}
		}
		else if(scoreCurrentState == UI_STATE_DISAPPEARING)
		{
			scoreTimer += deltaTime;
			scoreOpacity = 1f - (scoreTimer / scoreDisappearTime);

			if(scoreTimer >= scoreDisappearTime)
			{
				scoreTimer = 0f;
				scoreOpacity = 0f;
				scoreCurrentState = UI_STATE_NOT_VISIBLE;
			}

			scoreCurrentScale = lerp(1f, 2f, scoreOpacity);
			scoreCurrentPositionY = lerp(scorePositionY * 2f, scorePositionY, scoreOpacity);

			for(i=0; i<8; i++)
			{
				scoreCurrentPositionsX[i] = lerp(scorePositionsX[i] * 2f, scorePositionsX[i], scoreOpacity);
			}
		}

		// Multiplier progress bar
		if(multiplierProgressCurrentState == UI_STATE_APPEARING)
		{
			multiplierProgressTimer += deltaTime;
			multiplierProgressOpacity = multiplierProgressTimer / multiplierProgressAppearTime;

			if(multiplierProgressTimer >= multiplierProgressAppearTime)
			{
				multiplierProgressTimer = 0f;
				multiplierProgressOpacity = 1f;
				multiplierProgressCurrentState = UI_STATE_VISIBLE;
				multiplierNumbersCurrentState = UI_STATE_APPEARING;
			}

			multiplierProgressCurrentScale = lerp(2f, 1f, multiplierProgressOpacity);
			multiplierProgressCurrentPositionX = lerp(multiplierProgressPositionX * 2f, multiplierProgressPositionX, multiplierProgressOpacity);
			multiplierProgressCurrentPositionY = lerp(multiplierProgressPositionY * 2f, multiplierProgressPositionY, multiplierProgressOpacity);
		}
		else if(multiplierProgressCurrentState == UI_STATE_DISAPPEARING)
		{
			multiplierProgressTimer += deltaTime;
			multiplierProgressOpacity = 1f - (multiplierProgressTimer / multiplierProgressDisappearTime);

			if(multiplierProgressTimer >= multiplierProgressDisappearTime)
			{
				multiplierProgressTimer = 0f;
				multiplierProgressOpacity = 0f;
				multiplierProgressCurrentState = UI_STATE_NOT_VISIBLE;
			}

			multiplierProgressCurrentScale = lerp(2f, 1f, multiplierProgressOpacity);
			multiplierProgressCurrentPositionX = lerp(multiplierProgressPositionX * 2f, multiplierProgressPositionX, multiplierProgressOpacity);
			multiplierProgressCurrentPositionY = lerp(multiplierProgressPositionY * 2f, multiplierProgressPositionY, multiplierProgressOpacity);
		}

		// Score multiplier numbers
		if(multiplierNumbersCurrentState == UI_STATE_APPEARING)
		{
			multiplierNumbersTimer += deltaTime;
			multiplierNumbersOpacity = multiplierNumbersTimer / multiplierNumbersAppearTime;

			if(multiplierNumbersTimer >= multiplierNumbersAppearTime)
			{
				multiplierNumbersTimer = 0f;
				multiplierNumbersOpacity = 1f;
				multiplierNumbersCurrentState = UI_STATE_VISIBLE;
				livesState = UI_STATE_APPEARING;
			}

			multiplierNumbersCurrentScale = lerp(2f, 1f, multiplierNumbersOpacity);
			multiplierNumbersCurrentPositionY = lerp(multiplierNumbersPositionY * 2f, multiplierNumbersPositionY, multiplierNumbersOpacity);

			for(i=0; i<4; i++)
			{
				multiplierNumbersCurrentPositionsX[i] = lerp(multiplierNumbersPositionsX[i] * 2f, multiplierNumbersPositionsX[i], multiplierNumbersOpacity);
			}
		}
		else if(multiplierNumbersCurrentState == UI_STATE_DISAPPEARING)
		{
			multiplierNumbersTimer += deltaTime;
			multiplierNumbersOpacity = 1f - (multiplierNumbersTimer / multiplierNumbersDisappearTime);

			if(multiplierNumbersTimer >= multiplierNumbersDisappearTime)
			{
				multiplierNumbersTimer = 0f;
				multiplierNumbersOpacity = 0f;
				multiplierNumbersCurrentState = UI_STATE_NOT_VISIBLE;
			}

			multiplierNumbersCurrentScale = lerp(2f, 1f, multiplierNumbersOpacity);
			multiplierNumbersCurrentPositionY = lerp(multiplierNumbersPositionY * 2f, multiplierNumbersPositionY, multiplierNumbersOpacity);

			for(i=0; i<4; i++)
			{
				multiplierNumbersCurrentPositionsX[i] = lerp(multiplierNumbersPositionsX[i] * 2f, multiplierNumbersPositionsX[i], multiplierNumbersOpacity);
			}
		}

		// Lives (UI state)
		if(livesState == UI_STATE_APPEARING)
		{
			livesTimer += deltaTime;
			livesOpacity = livesTimer / livesAppearTime;

			if(livesTimer >= livesAppearTime)
			{
				livesTimer = 0f;
				livesOpacity = 1f;
				livesState = UI_STATE_VISIBLE;
				pauseButtonState = UI_STATE_APPEARING;
			}

			livesCurrentScale = lerp(2f, 1f, livesOpacity);
			livesCurrentPositionY = lerp(livesPositionY * 2f, livesPositionY, livesOpacity);

			for(i=0; i<5; i++)
			{
				livesCurrentPositionsX[i] = lerp(livesPositionsX[i] * 2f, livesPositionsX[i], livesOpacity);
			}
		}
		else if(livesState == UI_STATE_DISAPPEARING)
		{
			livesTimer += deltaTime;
			livesOpacity = 1f - (livesTimer / livesDisappearTime);

			if(livesTimer >= livesDisappearTime)
			{
				livesTimer = 0f;
				livesOpacity = 0f;
				livesState = UI_STATE_NOT_VISIBLE;
			}

			livesCurrentScale = lerp(2f, 1f, livesOpacity);
			livesCurrentPositionY = lerp(livesPositionY * 2f, livesPositionY, livesOpacity);

			for(i=0; i<5; i++)
			{
				livesCurrentPositionsX[i] = lerp(livesPositionsX[i] * 2f, livesPositionsX[i], livesOpacity);
			}
		}

		// Lives
		if(livesCounterState == LIVES_TIMER_COUNTING)
		{
			lifeRecoverTimer = lifeRecoverTimer + deltaTime;
			lifeRecoverPercent = lifeRecoverTimer / LIFE_RECOVERING_TIME;

			if(lifeRecoverPercent >= 1f)
			{
				livesStates[currentLife] = LIFE_OK;
				livesCounterState = LIVES_TIMER_IDLE;
			}
		}


		// Pause button
		if(pauseButtonState == UI_STATE_APPEARING)
		{
			pauseButtonTimer += deltaTime;
			pauseButtonOpacity = pauseButtonTimer / pauseButtonAppearTime;

			if(pauseButtonTimer >= pauseButtonAppearTime)
			{
				pauseButtonTimer = 0f;
				pauseButtonOpacity = 1f;
				pauseButtonState = UI_STATE_VISIBLE;
			}

			pauseButtonCurrentScale[0] = lerp(pauseButtonScale[0] * 2f, pauseButtonScale[0], pauseButtonOpacity);
			pauseButtonCurrentScale[1] = lerp(pauseButtonScale[1] * 2f, pauseButtonScale[1], pauseButtonOpacity);
			pauseButtonCurrentPosition[0] = lerp(pauseButtonPosition[0] * 2f, pauseButtonPosition[0], pauseButtonOpacity);
			pauseButtonCurrentPosition[1] = lerp(pauseButtonPosition[1] * 2f, pauseButtonPosition[1], pauseButtonOpacity);
		}
		else if(pauseButtonState == UI_STATE_DISAPPEARING)
		{
			pauseButtonTimer += deltaTime;
			pauseButtonOpacity = 1f - (pauseButtonTimer / pauseButtonDisappearTime);

			if(pauseButtonTimer >= pauseButtonDisappearTime)
			{
				pauseButtonTimer = 0f;
				pauseButtonOpacity = 0f;
				pauseButtonState = UI_STATE_NOT_VISIBLE;
			}

			pauseButtonCurrentScale[0] = lerp(pauseButtonScale[0] * 2f, pauseButtonScale[0], pauseButtonOpacity);
			pauseButtonCurrentScale[1] = lerp(pauseButtonScale[1] * 2f, pauseButtonScale[1], pauseButtonOpacity);
			pauseButtonCurrentPosition[0] = lerp(pauseButtonPosition[0] * 2f, pauseButtonPosition[0], pauseButtonOpacity);
			pauseButtonCurrentPosition[1] = lerp(pauseButtonPosition[1] * 2f, pauseButtonPosition[1], pauseButtonOpacity);
		}


		// Recovering progress bar
		if(recoveringProgressBarState == UI_STATE_APPEARING)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = Math.min(1f,recoveringProgressBarTimer / recoveringTimers[0]);
			recoveringCurrentPositionY = lerp(recoveringInitialPositionY, recoveringFinalPositionY, recoveringProgressBarOpacity);
			recoveringProgressBarPercent = recoveringTimers[0] / PLAYER_RECOVERING_TIME;
			if(recoveringProgressBarTimer >= recoveringTimers[0])
			{
				recoveringProgressBarState = UI_STATE_VISIBLE;
				recoveringProgressBarTimer -= recoveringTimers[0];
			}
		}
		else if(recoveringProgressBarState == UI_STATE_VISIBLE)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = 1f;
			recoveringCurrentPositionY = recoveringFinalPositionY;
			recoveringProgressBarPercent = (recoveringProgressBarTimer + recoveringTimers[0]) / PLAYER_RECOVERING_TIME;
			if(recoveringProgressBarTimer >= recoveringTimers[1])
			{
				recoveringProgressBarState = UI_STATE_DISAPPEARING;
				recoveringProgressBarTimer -= recoveringTimers[1];

				if(livesStates[currentLife] == LIFE_LOSING)
				{
					lifeRecoverTimer = 0f;
					livesCounterState = LIVES_TIMER_COUNTING;
				}
			}
		}
		else if(recoveringProgressBarState == UI_STATE_DISAPPEARING)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = Math.max(0.0f, 1f - (recoveringProgressBarTimer / recoveringTimers[2]));
			recoveringCurrentPositionY = lerp(recoveringInitialPositionY, recoveringFinalPositionY, recoveringProgressBarOpacity);
			recoveringProgressBarPercent = 1f;
			if(recoveringProgressBarTimer >= recoveringTimers[2])
			{
				recoveringProgressBarState = UI_STATE_NOT_VISIBLE;
				recoveringProgressBarTimer = 0f;
			}
		}

		// Get Ready panel
		if(getReadyPanelState == UI_STATE_APPEARING)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = Math.min(1f,getReadyPanelTimer / getReadyPanelTimers[0]);
			getReadyPanelCurrentPositionY = lerp(getReadyPanelInitialPositionY, getReadyPanelFinalPositionY, getReadyPanelOpacity);
			if(getReadyPanelTimer >= getReadyPanelTimers[0])
			{
				getReadyPanelTimer -= getReadyPanelTimers[0];
				getReadyPanelState = UI_STATE_VISIBLE;
			}
		}
		else if(getReadyPanelState == UI_STATE_VISIBLE)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = 1f;
			getReadyPanelCurrentPositionY = getReadyPanelFinalPositionY;
			if(getReadyPanelTimer >= getReadyPanelTimers[1])
			{
				getReadyPanelTimer -= getReadyPanelTimers[1];
				getReadyPanelState = UI_STATE_DISAPPEARING;
			}
		}
		else if(getReadyPanelState == UI_STATE_DISAPPEARING)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = Math.max(0.0f, 1.0f - (getReadyPanelTimer / getReadyPanelTimers[2]));
			getReadyPanelCurrentPositionY = lerp(getReadyPanelInitialPositionY, getReadyPanelFinalPositionY, getReadyPanelOpacity);
			if(getReadyPanelTimer >= getReadyPanelTimers[2])
			{
				getReadyPanelTimer -= getReadyPanelTimers[2];
				getReadyPanelState = UI_STATE_NOT_VISIBLE;
			}
		}

		for(int j=0; j < 5; j++)
		{
			if(livesStates[j] == LIFE_OK)
			{
				livesPercents[j] = 1f;
			}
			else
			{
				livesPercents[j] = 0f;
			}
		}
	}


	public void updateOther(float deltaTime)
	{
		// Resuming Panel
		if(resumingPanelState == UI_STATE_APPEARING)
		{
			resumingPanelTimer += deltaTime;
			resumingPanelOpacity = Math.min(1f, resumingPanelTimer / resumingPanelTimes[0]);
			resumingPanelCurrentPositionY = lerp(resumingPanelInitialPositionY, resumingPanelFinalPositionY, resumingPanelOpacity);
			if(resumingPanelTimer >= resumingPanelTimes[0])
			{
				resumingPanelTimer -= resumingPanelTimes[0];
				resumingPanelState = UI_STATE_VISIBLE;
			}
		}
		else if(resumingPanelState == UI_STATE_VISIBLE)
		{
			resumingPanelTimer += deltaTime;
			resumingPanelOpacity = 1.0f;
			resumingPanelCurrentPositionY = resumingPanelFinalPositionY;
			if(resumingPanelTimer >= resumingPanelTimes[1])
			{
				resumingPanelTimer -= resumingPanelTimes[1];
				resumingPanelState = UI_STATE_DISAPPEARING;
			}
		}
		else if(resumingPanelState == UI_STATE_DISAPPEARING)
		{
			resumingPanelTimer += deltaTime;
			resumingPanelOpacity = Math.max(0.0f, 1.0f - (resumingPanelTimer / resumingPanelTimes[2]));
			resumingPanelCurrentPositionY = lerp(resumingPanelInitialPositionY, resumingPanelFinalPositionY, resumingPanelOpacity);
			if(resumingPanelTimer >= resumingPanelTimes[2])
			{
				resumingPanelTimer = 0f;
				resumingPanelState = UI_STATE_NOT_VISIBLE;
			}
		}

		// Resuming progress bar
		if(resumingProgressBarState == UI_STATE_APPEARING)
		{
			resumingProgressBarTimer += deltaTime;
			resumingProgressBarOpacity = Math.min(1f,resumingProgressBarTimer / resumingTimers[0]);
			resumingCurrentPositionY = lerp(resumingInitialPositionY, resumingFinalPositionY, resumingProgressBarOpacity);
			resumingProgressBarPercent = resumingTimers[0] / PLAYER_RESUMING_TIME;
			if(resumingProgressBarTimer >= resumingTimers[0])
			{
				resumingProgressBarState = UI_STATE_VISIBLE;
				resumingProgressBarTimer -= resumingTimers[0];
			}
		}
		else if(resumingProgressBarState == UI_STATE_VISIBLE)
		{
			resumingProgressBarTimer += deltaTime;
			resumingProgressBarOpacity = 1f;
			resumingCurrentPositionY = resumingFinalPositionY;
			resumingProgressBarPercent = (resumingProgressBarTimer + resumingTimers[0]) / PLAYER_RESUMING_TIME;
			if(resumingProgressBarTimer >= resumingTimers[1])
			{
				resumingProgressBarState = UI_STATE_DISAPPEARING;
				resumingProgressBarTimer -= resumingTimers[1];
			}
		}
		else if(resumingProgressBarState == UI_STATE_DISAPPEARING)
		{
			resumingProgressBarTimer += deltaTime;
			resumingProgressBarOpacity = Math.max(0.0f, 1f - (resumingProgressBarTimer / resumingTimers[2]));
			resumingCurrentPositionY = lerp(resumingInitialPositionY, resumingFinalPositionY, resumingProgressBarOpacity);
			resumingProgressBarPercent = 1f;
			if(resumingProgressBarTimer >= resumingTimers[2])
			{
				resumingProgressBarState = UI_STATE_NOT_VISIBLE;
				resumingProgressBarTimer = 0f;
			}
		}
	}


	public boolean hit(int type)
	{
		boolean returnValue = true;

		recoveringProgressBarState = UI_STATE_APPEARING;
		recoveringProgressBarTimer = 0f;

		getReadyPanelState = UI_STATE_APPEARING;
		getReadyPanelTimer = 0f;

		if(currentLife == 0)
		{
			if((type == 0 && livesStates[0] == LIFE_LOSING) || type == 1)
			{
				recoveringProgressBarState = UI_STATE_NOT_VISIBLE;
				getReadyPanelState = UI_STATE_NOT_VISIBLE;
				returnValue = false;
			}
		}

		multiplierProgressValue = 0f;

		if(livesStates[currentLife] == LIFE_OK)
		{
			if(type == 0)
			{
				livesStates[currentLife] = LIFE_LOSING;
				lifeRecoverPercent = 0f;
				lifeRecoverTimer = 0f;
			}
			else
			{
				livesStates[currentLife] = LIFE_LOST;
				currentLife--;
			}
		}
		else if(livesStates[currentLife] == LIFE_LOSING)
		{
			livesCounterState = LIVES_TIMER_IDLE;
			livesStates[currentLife] = LIFE_LOST;
			currentLife--;
		}

		if(currentLife < 0)
		{
			renderer.gameOver();
		}

		//TODO: temp (avoid exception)
		currentLife = Math.max(currentLife, 0);

		return returnValue;
	}


	public void draw()
	{
		// Pause button
		if(pauseButtonState != UI_STATE_NOT_VISIBLE)
		{
			uiPanelProgram.useProgram();
			uiPanelProgram.setUniforms(viewProjection, pauseButtonCurrentScale, pauseButtonCurrentPosition, pauseButtonCurrentTexture, 0.5f * pauseButtonOpacity);
			glBindVertexArray(pauseButtonVaoHandle);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			//Log.w("PauseButton", "Drawing");
		}


		// Score panel
		scorePanelProgram.useProgram();
		scorePanelProgram.setCommonUniforms(viewProjection, textures.numbersAtlasTexture);

		glBindVertexArray(scoreVaoHandle[0]);

		if(scoreCurrentState != UI_STATE_NOT_VISIBLE)
		{
			for(int i=0; i < 8; i++)
			{
				scorePanelProgram.setSpecificUniforms(scoreCurrentScale,
						scoreCurrentPositionsX[i], scoreCurrentPositionY,
						scoreTexCoordOffsetsX[scoreNumbers[i]], scoreTexCoordOffsetsY[scoreNumbers[i]],
						scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacities[i] * scoreOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}
		}

		if(multiplierNumbersCurrentState != UI_STATE_NOT_VISIBLE)
		{
			for(int i=0; i < 4; i++)
			{
				scorePanelProgram.setSpecificUniforms(multiplierNumbersCurrentScale,
						multiplierNumbersCurrentPositionsX[i], multiplierNumbersCurrentPositionY,
						scoreTexCoordOffsetsX[multiplierNumbers[i]], scoreTexCoordOffsetsY[multiplierNumbers[i]],
						scoreColor[0], scoreColor[1], scoreColor[2], multiplierNumbersOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}
		}

		//scorePanelProgram.setSpecificUniforms(scorePositionOffsetY, scoreTexCoordOffsetsX[scoreNumbers[0]], scoreTexCoordOffsetsY[scoreNumbers[0]]);

		// current FPS
		scorePanelProgram.setCommonUniforms(viewProjection, textures.numbersAtlasTexture);
		scorePanelProgram.setSpecificUniforms(1f, fpsPositionOffsetsX[0], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[0]], scoreTexCoordOffsetsY[currentFpsNumbers[0]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(1f, fpsPositionOffsetsX[1], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[1]], scoreTexCoordOffsetsY[currentFpsNumbers[1]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// average FPS
		scorePanelProgram.setSpecificUniforms(1f, fpsPositionOffsetsX[0], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[0]], scoreTexCoordOffsetsY[averageFpsNumbers[0]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(1f, fpsPositionOffsetsX[1], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[1]], scoreTexCoordOffsetsY[averageFpsNumbers[1]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// get ready panel

		if(getReadyPanelState != UI_STATE_NOT_VISIBLE)
		{
			scorePanelProgram.setCommonUniforms(viewProjection, getReadyPanelTexture);
			scorePanelProgram.setSpecificUniforms(1f, 0f, getReadyPanelCurrentPositionY, 0f, 0f, recoveringColor[0], recoveringColor[1], recoveringColor[2], getReadyPanelOpacity);
			glBindVertexArray(getReadyPanelVaoHandle);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		// Resuming panel

		if(resumingPanelState != UI_STATE_NOT_VISIBLE)
		{
			scorePanelProgram.setCommonUniforms(viewProjection, resumingPanelTexture);
			scorePanelProgram.setSpecificUniforms(1f, 0f, resumingPanelCurrentPositionY, 0f, 0f, recoveringColor[0], recoveringColor[1], recoveringColor[2], resumingPanelOpacity);
			glBindVertexArray(resumingPanelVaoHandle);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		/// Progress bar program

		progressBarProgram.useProgram();
		progressBarProgram.setCommonUniforms(viewProjection, textures.progressBarTexture);

		if(multiplierProgressCurrentState != UI_STATE_NOT_VISIBLE)
		{
			progressBarProgram.setSpecificUniforms(multiplierProgressCurrentScale, multiplierProgressCurrentPositionX, multiplierProgressCurrentPositionY,
					1f, 1f, 1f, multiplierProgressOpacity, multiplierProgressValue);
			glBindVertexArray(multiplierProgressVaoHandle);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		if(resumingProgressBarState != UI_STATE_NOT_VISIBLE)
		{
			progressBarProgram.setSpecificUniforms(1f, 0f, resumingCurrentPositionY, recoveringColor[0], recoveringColor[1], recoveringColor[2], resumingProgressBarOpacity, resumingProgressBarPercent);
			glBindVertexArray(resumingProgressBarVaoHandle);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		if(recoveringProgressBarState != UI_STATE_NOT_VISIBLE)
		{
			progressBarProgram.setSpecificUniforms(1f, 0f, recoveringCurrentPositionY, recoveringColor[0], recoveringColor[1], recoveringColor[2], recoveringProgressBarOpacity, recoveringProgressBarPercent);
			glBindVertexArray(recoveringProgressBarVaoHandle);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		if(livesState != UI_STATE_NOT_VISIBLE)
		{
			glBindVertexArray(lifeBarVaoHandle);

			for(int i=0; i < 5; i++)
			{
				progressBarProgram.setSpecificUniforms(livesCurrentScale, livesCurrentPositionsX[i], livesCurrentPositionY, 1f, 1f, 1f, 1f, livesPercents[i] * livesOpacity);
				glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
			}

			if(livesStates[currentLife] == LIFE_LOSING)
			{
				progressBarProgram.setSpecificUniforms(livesCurrentScale, livesCurrentPositionsX[currentLife], livesCurrentPositionY, 1f, 1f, 0f, 1f, lifeRecoverPercent * livesOpacity);
				glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// State save / load
	////////////////////////////////////////////////////////////////////////////////////////////////


	public void saveState(FileOutputStream outputStream) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		builder.append("HUD ");
		builder.append(currentLife);		builder.append(" ");
		builder.append(livesCounterState);	builder.append(" ");
		builder.append(livesStates[0]);		builder.append(" ");
		builder.append(livesStates[1]);		builder.append(" ");
		builder.append(livesStates[2]);		builder.append(" ");
		builder.append(livesStates[3]);		builder.append(" ");
		builder.append(livesStates[4]);		builder.append(" ");
		builder.append(livesPercents[0]);	builder.append(" ");
		builder.append(livesPercents[1]);	builder.append(" ");
		builder.append(livesPercents[2]);	builder.append(" ");
		builder.append(livesPercents[3]);	builder.append(" ");
		builder.append(livesPercents[4]);	builder.append("\n");

		outputStream.write(builder.toString().getBytes());
	}


	public void loadState(String[] tokens)
	{
		currentLife = Integer.parseInt(tokens[1]);
		livesCounterState = Integer.parseInt(tokens[2]);
		livesStates[0] = Integer.parseInt(tokens[3]);
		livesStates[1] = Integer.parseInt(tokens[4]);
		livesStates[2] = Integer.parseInt(tokens[5]);
		livesStates[3] = Integer.parseInt(tokens[6]);
		livesStates[4] = Integer.parseInt(tokens[7]);
		livesPercents[0] = Float.parseFloat(tokens[8]);
		livesPercents[1] = Float.parseFloat(tokens[9]);
		livesPercents[2] = Float.parseFloat(tokens[10]);
		livesPercents[3] = Float.parseFloat(tokens[11]);
		livesPercents[4] = Float.parseFloat(tokens[12]);
	}
}
