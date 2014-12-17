package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.FloatMath;

import com.josepmtomas.rockgame.programsForwardPlus.ProgressBarProgram;
import com.josepmtomas.rockgame.programsForwardPlus.ScorePanelProgram;
import com.josepmtomas.rockgame.util.HudHelper;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	// UI States
	private static final int OUT_OF_SCREEN = 0;
	private static final int APPEARING = 1;
	private static final int SHOWING = 2;
	private static final int DISAPPEARING = 3;

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

	// Score panel
	private int[] scoreVboHandles = new int[2];
	private int[] scoreVaoHandle = new int[1];
	private float[] scorePositionOffsetsX = new float[8];//{1700f, 1572f, 1444f, 1316f, 1188f, 1060f, 932f, 804f};
	private float scorePositionOffsetY = 0;
	private float[] scoreTexCoordOffsetsX = {    0f,    0f,     0f, 0f,  0.25f, 0.25f,  0.25f, 0.25f,   0.5f,  0.5f,   0.5f, 0.5f,  0.75f, 0.75f};
	private float[] scoreTexCoordOffsetsY = {-0.75f, -0.5f, -0.25f, 0f, -0.75f, -0.5f, -0.25f,    0f, -0.75f, -0.5f, -0.25f,   0f, -0.75f, -0.5f};
	private float scoreOpacity = 1f;
	private int scoreNumbersTexture;
	private int scoreNumbersNegativeTexture;
	private int[] scoreNumbers = {0, 0, 0, 0, 0, 0, 0, 0};

	// Multiplier panel
	private float[] multiplierNumbersOffsetsX = new float[4];
	private float multiplierNumbersOffsetY;
	private int[] multiplierNumbers = {0, 13, 0, 12};

	// Multiplier progress bar
	private int multiplierProgressVaoHandle;
	private int multiplierProgressTexture;
	private float multiplierProgressValue = 0f;
	private float multiplierProgressOffsetX = 0;
	private float multiplierProgressOffsetY = 0;

	// Recovering progress bar
	private int recoveringProgressBarVaoHandle;
	private int recoveringProgressBarState = OUT_OF_SCREEN;
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
	private int getReadyPanelState = OUT_OF_SCREEN;
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
	private float currentFpsPositionOffsetY = 950;
	private float averageFpsPositionOffsetY = 800;
	private int[] currentFpsNumbers = {0, 0};
	private int[] averageFpsNumbers = {0, 0};

	// Programs
	ScorePanelProgram scorePanelProgram;
	ProgressBarProgram progressBarProgram;

	private static final float NUMBER_HEIGHT_PERCENTAGE = 0.15f;


	public Hud(Context context, float screenWidth, float screenHeight)
	{
		float progressBarHeight = screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.15f;
		float numberHeight =  screenHeight * NUMBER_HEIGHT_PERCENTAGE;
		float numberWidth = numberHeight * 0.7134f;

		// Common matrices
		createMatrices(screenWidth, screenHeight);

		// Score panel
		createScoreGeometry(screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f, screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		scoreNumbersTexture = TextureHelper.loadETC2Texture(context, "textures/hud/numbers_atlas.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		scoreNumbersNegativeTexture = TextureHelper.loadETC2Texture(context, "textures/hud/numbers_atlas_negative_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier panel
		//loadMultiplierGeometry(context, "models/hud_multiplier_base.vbm", screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		//multiplierTexture = TextureHelper.loadETC2Texture(context, "textures/hud/multiplier_9patch_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier progress bar
		multiplierProgressVaoHandle = HudHelper.makeProgressBar((screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f) * 8, progressBarHeight, HUD_BASE_LEFT_CENTER);
		multiplierProgressTexture = TextureHelper.loadETC2Texture(context, "textures/hud/progress_bar_alpha.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Recovering
		recoveringProgressBarVaoHandle = HudHelper.makeProgressBar(800, progressBarHeight, HUD_BASE_CENTER_CENTER);
		getReadyPanelVaoHandle = HudHelper.makePanel(numberWidth * 9f, numberHeight * 0.9f, HUD_BASE_CENTER_CENTER);
		getReadyPanelTexture = TextureHelper.loadETC2Texture(context, "textures/hud/get_ready.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Life bars
		lifeBarVaoHandle = HudHelper.makeProgressBar(200, progressBarHeight, HUD_BASE_CENTER_CENTER);

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

		scorePositionOffsetY = screenHeight - (numberHeight * 0.5f) - (numberWidth/2);
		scorePositionOffsetsX[0] = initialOffsetX;

		for(int i=1; i < 8; i++)
		{
			scorePositionOffsetsX[i] = initialOffsetX - ((float)i * numberWidth);
		}

		/////////// multiplier

		float multiplierBaseOffsetX = screenWidth - (numberWidth / 2f);
		float multiplierBaseOffsetY = screenHeight - numberHeight * 4f;

		multiplierNumbersOffsetY = multiplierBaseOffsetY + numberHeight * 1.25f;
		multiplierNumbersOffsetsX[0] = multiplierBaseOffsetX - (numberWidth * 0.75f);
		multiplierNumbersOffsetsX[1] = multiplierNumbersOffsetsX[0] - numberWidth * 0.75f;
		multiplierNumbersOffsetsX[2] = multiplierNumbersOffsetsX[1] - numberWidth * 0.75f;
		multiplierNumbersOffsetsX[3] = multiplierNumbersOffsetsX[2] - numberWidth;

		multiplierProgressOffsetY = multiplierBaseOffsetY + numberHeight * 2f;
		multiplierProgressOffsetX = scorePositionOffsetsX[7] - numberWidth * 0.5f; //multiplierBaseOffsetX - numberHeight * 4.25f;

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
	}


	public void update(int currentScore, int currentMultiplier, float multiplierPercent, int currentFps, float deltaTime)
	{
		multiplierProgressValue = multiplierPercent;

		int i=0;
		boolean end = false;
		int number;
		int nextScore = currentScore;

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
		if(recoveringProgressBarState == APPEARING)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = Math.min(1f,recoveringProgressBarTimer / recoveringTimers[0]);
			recoveringCurrentPositionY = lerp(recoveringInitialPositionY, recoveringFinalPositionY, recoveringProgressBarOpacity);
			recoveringProgressBarPercent = recoveringTimers[0] / PLAYER_RECOVERING_TIME;
			if(recoveringProgressBarTimer >= recoveringTimers[0])
			{
				recoveringProgressBarState = SHOWING;
				recoveringProgressBarTimer -= recoveringTimers[0];
			}
		}
		else if(recoveringProgressBarState == SHOWING)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = 1f;
			recoveringCurrentPositionY = recoveringFinalPositionY;
			recoveringProgressBarPercent = (recoveringProgressBarTimer + recoveringTimers[0]) / PLAYER_RECOVERING_TIME;
			if(recoveringProgressBarTimer >= recoveringTimers[1])
			{
				recoveringProgressBarState = DISAPPEARING;
				recoveringProgressBarTimer -= recoveringTimers[1];

				if(livesStates[currentLife] == LIFE_LOSING)
				{
					lifeRecoverTimer = 0f;
					livesCounterState = LIVES_TIMER_COUNTING;
				}
			}
		}
		else if(recoveringProgressBarState == DISAPPEARING)
		{
			recoveringProgressBarTimer += deltaTime;
			recoveringProgressBarOpacity = Math.max(0.0f, 1f - (recoveringProgressBarTimer / recoveringTimers[2]));
			recoveringCurrentPositionY = lerp(recoveringInitialPositionY, recoveringFinalPositionY, recoveringProgressBarOpacity);
			recoveringProgressBarPercent = 1f;
			if(recoveringProgressBarTimer >= recoveringTimers[2])
			{
				recoveringProgressBarState = OUT_OF_SCREEN;
				recoveringProgressBarTimer = 0f;
			}
		}

		// Get Ready panel
		if(getReadyPanelState == APPEARING)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = Math.min(1f,getReadyPanelTimer / getReadyPanelTimers[0]);
			getReadyPanelCurrentPositionY = lerp(getReadyPanelInitialPositionY, getReadyPanelFinalPositionY, getReadyPanelOpacity);
			if(getReadyPanelTimer >= getReadyPanelTimers[0])
			{
				getReadyPanelTimer -= getReadyPanelTimers[0];
				getReadyPanelState = SHOWING;
			}
		}
		else if(getReadyPanelState == SHOWING)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = 1f;
			getReadyPanelCurrentPositionY = getReadyPanelFinalPositionY;
			if(getReadyPanelTimer >= getReadyPanelTimers[1])
			{
				getReadyPanelTimer -= getReadyPanelTimers[1];
				getReadyPanelState = DISAPPEARING;
			}
		}
		else if(getReadyPanelState == DISAPPEARING)
		{
			getReadyPanelTimer += deltaTime;
			getReadyPanelOpacity = Math.max(0.0f, 1.0f - (getReadyPanelTimer / getReadyPanelTimers[2]));
			getReadyPanelCurrentPositionY = lerp(getReadyPanelInitialPositionY, getReadyPanelFinalPositionY, getReadyPanelOpacity);
			if(getReadyPanelTimer >= getReadyPanelTimers[2])
			{
				getReadyPanelTimer -= getReadyPanelTimers[2];
				getReadyPanelState = OUT_OF_SCREEN;
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
		recoveringProgressBarState = APPEARING;
		recoveringProgressBarTimer = 0f;

		getReadyPanelState = APPEARING;
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
		// Score panel
		scorePanelProgram.useProgram();
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);

		glBindVertexArray(scoreVaoHandle[0]);

		for(int i=0; i < 8; i++)
		{
			scorePanelProgram.setSpecificUniforms(scorePositionOffsetsX[i], scorePositionOffsetY, scoreTexCoordOffsetsX[scoreNumbers[i]], scoreTexCoordOffsetsY[scoreNumbers[i]],
					scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		//scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersNegativeTexture);
		for(int i=0; i < 4; i++)
		{
			scorePanelProgram.setSpecificUniforms(multiplierNumbersOffsetsX[i], multiplierNumbersOffsetY, scoreTexCoordOffsetsX[multiplierNumbers[i]], scoreTexCoordOffsetsY[multiplierNumbers[i]],
					scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		//scorePanelProgram.setSpecificUniforms(scorePositionOffsetY, scoreTexCoordOffsetsX[scoreNumbers[0]], scoreTexCoordOffsetsY[scoreNumbers[0]]);

		// current FPS
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[0], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[0]], scoreTexCoordOffsetsY[currentFpsNumbers[0]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[1], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[1]], scoreTexCoordOffsetsY[currentFpsNumbers[1]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// average FPS
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[0], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[0]], scoreTexCoordOffsetsY[averageFpsNumbers[0]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[1], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[1]], scoreTexCoordOffsetsY[averageFpsNumbers[1]],
				scoreColor[0], scoreColor[1], scoreColor[2], scoreOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// get ready panel

		if(getReadyPanelState != OUT_OF_SCREEN)
		{
			scorePanelProgram.setCommonUniforms(viewProjection, getReadyPanelTexture);
			scorePanelProgram.setSpecificUniforms(0f, getReadyPanelCurrentPositionY, 0f, 0f, recoveringColor[0], recoveringColor[1], recoveringColor[2], getReadyPanelOpacity);
			glBindVertexArray(getReadyPanelVaoHandle);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		/// Progress bar program

		progressBarProgram.useProgram();
		progressBarProgram.setCommonUniforms(viewProjection, multiplierProgressTexture);
		progressBarProgram.setSpecificUniforms(multiplierProgressOffsetX, multiplierProgressOffsetY, 1f, 1f, 1f, 1f, multiplierProgressValue);

		glBindVertexArray(multiplierProgressVaoHandle);
		glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);

		progressBarProgram.setSpecificUniforms(0f, recoveringCurrentPositionY, recoveringColor[0], recoveringColor[1], recoveringColor[2], recoveringProgressBarOpacity, recoveringProgressBarPercent);

		glBindVertexArray(recoveringProgressBarVaoHandle);
		glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);


		glBindVertexArray(lifeBarVaoHandle);

		for(int i=0; i < 5; i++)
		{
			progressBarProgram.setSpecificUniforms(livesPositionsX[i], livesPositionY, 1f, 1f, 1f, 1f, livesPercents[i]);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}

		if(livesStates[currentLife] == LIFE_LOSING)
		{
			progressBarProgram.setSpecificUniforms(livesPositionsX[currentLife], livesPositionY, 1f, 1f, 0f, 1f, lifeRecoverPercent);
			glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0);
		}
	}
}
