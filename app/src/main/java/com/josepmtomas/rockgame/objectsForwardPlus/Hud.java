package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.programsForwardPlus.ProgressBarProgram;
import com.josepmtomas.rockgame.programsForwardPlus.ScorePanelProgram;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;
import com.josepmtomas.rockgame.util.TextureHelper;

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
 */
public class Hud
{
	private static String TAG = "HUD";

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
	private int pauseButtonIdleTexture;
	private int pauseButtonSelectedTexture;
	private int pauseButtonCurrentTexture;
	private float[] pauseButtonPosition = new float[2];
	private float[] pauseButtonScale = new float[2];
	private float[] pauseButtonLimits = new float[4];
	private float[] pauseButtonCurrentPosition = new float[2];
	private float[] pauseButtonCurrentScale = new float[2];

	// Score panel
	private int[] scoreVboHandles = new int[2];
	private int[] scoreVaoHandle = new int[1];
	private float[] scorePositionsX = new float[8];//{1700f, 1572f, 1444f, 1316f, 1188f, 1060f, 932f, 804f};
	private float scorePositionY = 0;
	private float[] scoreTexCoordOffsetsX = {    0f,    0f,     0f, 0f,  0.25f, 0.25f,  0.25f, 0.25f,   0.5f,  0.5f,   0.5f, 0.5f,  0.75f, 0.75f};
	private float[] scoreTexCoordOffsetsY = {-0.75f, -0.5f, -0.25f, 0f, -0.75f, -0.5f, -0.25f,    0f, -0.75f, -0.5f, -0.25f,   0f, -0.75f, -0.5f};
	private float scoreOpacity = 1f;
	private int scoreNumbersTexture;
	private int[] scoreNumbers = {0, 0, 0, 0, 0, 0, 0, 0};

	// Score panel state control
	private int scoreCurrentState = UI_STATE_NOT_VISIBLE;
	private float scoreCurrentScale = 1f;
	private float[] scoreCurrentPositionsX = new float[8];
	private float scoreCurrentPositionY = 0f;
	private float scoreTimer = 0f;
	private float scoreAppearTime = 0.5f;

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
	private float multiplierNumbersOpacity = 1f;

	// Multiplier progress bar
	private int multiplierProgressVaoHandle;
	private int multiplierProgressTexture;
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
	private float[] getReadyPanelTimers = {0.5f, PLAYER_RECOVERING_TIME - 0.5f, 0.5f};
	private float getReadyPanelCurrentPositionY = 0f;
	private float getReadyPanelInitialPositionY = -500f;
	private float getReadyPanelFinalPositionY = 0f;

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


	public Hud(Context context, UIPanelProgram uiPanelProgram, float screenWidth, float screenHeight)
	{
		this.uiPanelProgram = uiPanelProgram;

		float progressBarHeight = screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.15f;
		float numberHeight =  screenHeight * NUMBER_HEIGHT_PERCENTAGE;
		float numberWidth = numberHeight * 0.7134f;

		// Common matrices
		createMatrices(screenWidth, screenHeight);

		// Pause button
		createPauseButton(context, screenWidth, screenHeight);

		// Score panel
		createScoreGeometry(screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f, screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		scoreNumbersTexture = TextureHelper.loadETC2Texture(context, "textures/hud/numbers_atlas.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier panel
		//loadMultiplierGeometry(context, "models/hud_multiplier_base.vbm", screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		//multiplierTexture = TextureHelper.loadETC2Texture(context, "textures/hud/multiplier_9patch_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier progress bar
		multiplierProgressVaoHandle = UIHelper.makeProgressBar((screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f) * 8, progressBarHeight, UI_BASE_LEFT_CENTER);
		multiplierProgressTexture = TextureHelper.loadETC2Texture(context, "textures/hud/progress_bar_alpha.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Recovering
		recoveringProgressBarVaoHandle = UIHelper.makeProgressBar(800, progressBarHeight, UI_BASE_CENTER_CENTER);
		getReadyPanelVaoHandle = UIHelper.makePanel(numberWidth * 9f, numberHeight * 0.9f, UI_BASE_CENTER_CENTER);
		getReadyPanelTexture = TextureHelper.loadETC2Texture(context, "textures/hud/get_ready.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Life bars
		lifeBarVaoHandle = UIHelper.makeProgressBar(200, progressBarHeight, UI_BASE_CENTER_CENTER);

		// Positions
		setPositions(screenWidth, screenHeight, (screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f) , screenHeight * NUMBER_HEIGHT_PERCENTAGE);

		// Programs
		scorePanelProgram = new ScorePanelProgram(context);
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


	private void createPauseButton(Context context, float screenWidth, float screenHeight)
	{
		float buttonHeight = screenHeight * 0.1f;
		float buttonWidth = buttonHeight * 3f;
		float buttonHeightHalf = buttonHeight * 0.5f;
		float buttonWidthHalf = buttonWidth * 0.5f;

		pauseButtonScale[0] = buttonWidth;
		pauseButtonScale[1] = buttonHeight;
		pauseButtonCurrentScale[0] = pauseButtonScale[0];
		pauseButtonCurrentScale[1] = pauseButtonScale[1];

		pauseButtonPosition[0] = screenWidth * 0.5f - buttonWidthHalf;
		pauseButtonPosition[1] = screenHeight * -0.5f + buttonHeightHalf;
		pauseButtonCurrentPosition[0] = pauseButtonPosition[0];
		pauseButtonCurrentPosition[1] = pauseButtonPosition[1];

		// left-right-bottom-top
		pauseButtonLimits[0] = pauseButtonPosition[0] - buttonWidthHalf;
		pauseButtonLimits[1] = pauseButtonPosition[1] + buttonWidthHalf;
		pauseButtonLimits[0] = pauseButtonPosition[0] - buttonHeightHalf;
		pauseButtonLimits[1] = pauseButtonPosition[1] + buttonHeightHalf;

		pauseButtonVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);

		pauseButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/hud/pause_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		pauseButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/hud/pause_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		pauseButtonCurrentTexture = pauseButtonIdleTexture;
	}


	private void createScoreGeometry(float numberWidth, float numberHeight)
	{
		float[] vertices = new float[20];
		short[] elements = new short[6];

		float bottom = -numberHeight * 0.5f;
		float left = -numberWidth * 0.5f;
		float width = numberWidth;
		float height = numberHeight;

		float texCoordVstart = 0.1f;
		float texCoordVincrement = -0.1f;

		// D - C
		// | \ |
		// A - B

		int offset = 0;

		for(int y=0; y<2; y++)
		{
			for(int x=0; x<2; x++)
			{
				// Position
				vertices[offset++] = left + ((float)x * width);
				vertices[offset++] = bottom + ((float)y * height);
				vertices[offset++] = 0.0f;

				// Texture Coordinates
				vertices[offset++] = (float)x * 0.25f;
				vertices[offset++] = 1.0f - (float)y * 0.25f;
				//vertices[offset++] = texCoordVstart + ((float)y * texCoordVincrement);
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

		multiplierProgressPositionY = multiplierBaseOffsetY + numberHeight * 2.25f;
		multiplierProgressPositionX = scorePositionsX[7] - numberWidth * 0.5f; //multiplierBaseOffsetX - numberHeight * 4.25f;

		multiplierProgressCurrentPositionY = multiplierProgressPositionY;
		multiplierProgressCurrentPositionX = multiplierProgressPositionX;

		// Recovering

		recoveringInitialPositionY = -screenHeight;
		recoveringFinalPositionY = -screenHeight * 0.5f;

		// Get Ready panel

		getReadyPanelInitialPositionY = -screenHeight;
		getReadyPanelFinalPositionY = -screenHeight * 0.4f;

		// Lives

		float livesSpacing = screenWidth * 0.125f;
		initialOffsetX = livesSpacing * -2f;

		for(int i=0; i < 5; i++)
		{
			livesPositionsX[i] = initialOffsetX + (i * livesSpacing);
		}
		livesPositionY = screenHeight * -0.85f;

		// FPS

		fpsPositionOffsetsX[1] = -screenWidth + numberWidth;
		fpsPositionOffsetsX[0] = fpsPositionOffsetsX[1] + numberWidth;
		currentFpsPositionOffsetY = screenHeight * 0.9f;
		averageFpsPositionOffsetY = screenHeight * 0.75f;
	}


	public void setAppearing()
	{
		scoreCurrentState = UI_STATE_APPEARING;
		multiplierProgressCurrentState = UI_STATE_NOT_VISIBLE;
		multiplierNumbersCurrentState = UI_STATE_NOT_VISIBLE;
	}


	public void update(int currentScore, int currentMultiplier, float multiplierPercent, int currentFps, float deltaTime)
	{
		multiplierProgressValue = multiplierPercent;

		int i;
		boolean end = false;
		int number;
		int nextScore = currentScore;

		for(i=0; i<8; i++)
		{
			scoreNumbers[i] = 0;
		}
		i=0;

		while(i < 8 && !end)
		{
			number = nextScore % 10;
			nextScore = nextScore / 10;

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
			}

			multiplierNumbersCurrentScale = lerp(2f, 1f, multiplierNumbersOpacity);
			multiplierNumbersCurrentPositionY = lerp(multiplierNumbersPositionY * 2f, multiplierNumbersPositionY, multiplierNumbersOpacity);

			for(i=0; i<4; i++)
			{
				multiplierNumbersCurrentPositionsX[i] = lerp(multiplierNumbersPositionsX[i] * 2f, multiplierNumbersPositionsX[i], multiplierNumbersOpacity);
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


	public void hit(int type)
	{
		recoveringProgressBarState = UI_STATE_APPEARING;
		recoveringProgressBarTimer = 0f;

		getReadyPanelState = UI_STATE_APPEARING;
		getReadyPanelTimer = 0f;

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

		//TODO: temp (avoid exception)
		currentLife = Math.max(currentLife, 0);
	}


	public void draw()
	{
		// Pause button
		uiPanelProgram.useProgram();
		uiPanelProgram.setUniforms(viewProjection, pauseButtonCurrentScale, pauseButtonCurrentPosition, pauseButtonCurrentTexture, 0.5f);
		glBindVertexArray(pauseButtonVaoHandle);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// Score panel
		scorePanelProgram.useProgram();
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);

		glBindVertexArray(scoreVaoHandle[0]);

		if(scoreCurrentState != UI_STATE_NOT_VISIBLE)
		{
			for(int i=0; i < 8; i++)
			{
				scorePanelProgram.setSpecificUniforms(scoreCurrentScale,
						scoreCurrentPositionsX[i], scoreCurrentPositionY,
						scoreTexCoordOffsetsX[scoreNumbers[i]], scoreTexCoordOffsetsY[scoreNumbers[i]],
						scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
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
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);
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

		/// Progress bar program

		progressBarProgram.useProgram();
		progressBarProgram.setCommonUniforms(viewProjection, multiplierProgressTexture);

		if(multiplierProgressCurrentState != UI_STATE_NOT_VISIBLE)
		{
			progressBarProgram.setSpecificUniforms(multiplierProgressCurrentScale, multiplierProgressCurrentPositionX, multiplierProgressCurrentPositionY,
					1f, 1f, 1f, multiplierProgressOpacity, multiplierProgressValue);
			glBindVertexArray(multiplierProgressVaoHandle);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		progressBarProgram.setSpecificUniforms(1f, 0f, recoveringCurrentPositionY, recoveringColor[0], recoveringColor[1], recoveringColor[2], recoveringProgressBarOpacity, recoveringProgressBarPercent);

		glBindVertexArray(recoveringProgressBarVaoHandle);
		glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);


		glBindVertexArray(lifeBarVaoHandle);

		for(int i=0; i < 5; i++)
		{
			progressBarProgram.setSpecificUniforms(1f, livesPositionsX[i], livesPositionY, 1f, 1f, 1f, 1f, livesPercents[i]);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		if(livesStates[currentLife] == LIFE_LOSING)
		{
			progressBarProgram.setSpecificUniforms(1f, livesPositionsX[currentLife], livesPositionY, 1f, 1f, 0f, 1f, lifeRecoverPercent);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}
	}
}
