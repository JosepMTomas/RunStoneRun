package com.josepmtomas.rockgame.objects;

import android.util.FloatMath;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programs.ScorePanelProgram;
import com.josepmtomas.rockgame.programs.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 10/01/2015.
 * @author Josep
 */
public class GameOverMenu
{
	private static final String TAG = "GameOverMenu";

	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private ScorePanelProgram scorePanelProgram;
	private MenuTextures textures;

	// Geometry attributes constants
	private static final float NUMBER_HEIGHT_PERCENTAGE = 0.15f;
	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = 3 * BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = 5 * BYTES_PER_FLOAT;

	// State
	private int currentState = UI_STATE_NOT_VISIBLE;
	private boolean isNewRecord = false;

	// Menu common attributes
	private static final float menuAppearTime = 0.5f;
	private static final float menuDisappearTime = 0.5f;
	private float menuTimer = 0f;
	private float menuGlobalTimer = 0f;
	private float menuOpacity = 0f;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// UI Panel
	private int uiPanelVaoHandle;
	private int ui9PatchPanelVaoHandle;

	// Background panel
	private float[] background9PatchPosition = new float[2];
	private float[] background9PatchCurrentScale = new float[2];

	// Game Over Title
	private float[] gameOverTitleScale = new float[2];
	private float[] gameOverTitlePosition = new float[2];
	private float[] gameOverTitleCurrentScale = new float[2];
	private float[] gameOverTitleCurrentPosition = new float[2];

	// Final score title
	private float[] finalScoreTitleScale = new float[2];
	private float[] finalScoreTitlePosition = new float[2];
	private float[] finalScoreTitleCurrentScale = new float[2];
	private float[] finalScoreTitleCurrentPosition = new float[2];

	// Touch to continue title
	private float[] touchToContinueTitleScale = new float[2];
	private float[] touchToContinueTitlePosition = new float[2];
	private float[] touchToContinueTitleCurrentScale = new float[2];
	private float[] touchToContinueTitleCurrentPosition = new float[2];

	// New record title
	private float[] newRecordTitleScale = new float[2];
	private float[] newRecordTitlePosition = new float[2];
	private float[] newRecordTitleCurrentScale = new float[2];
	private float[] newRecordTitleCurrentPosition = new float[2];

	// Score panel
	private int[] scoreVboHandles = new int[2];
	private int[] scoreVaoHandle = new int[1];
	private float[] scorePositionsX = new float[8];
	private float scorePositionY = 0;
	private float[] scoreTexCoordOffsetsX = {    0f,    0f,     0f, 0f,  0.25f, 0.25f,  0.25f, 0.25f,   0.5f,  0.5f,   0.5f, 0.5f,  0.75f, 0.75f};
	private float[] scoreTexCoordOffsetsY = {-0.75f, -0.5f, -0.25f, 0f, -0.75f, -0.5f, -0.25f,    0f, -0.75f, -0.5f, -0.25f,   0f, -0.75f, -0.5f};
	private int scoreNumberOfDigits = 0;
	private float scoreNumberWidth;
	private int[] scoreNumbers = {0, 0, 0, 0, 0, 0, 0, 0};

	private float scoreCurrentScale = 1f;
	private float[] scoreCurrentPositionsX = new float[8];
	private float scoreCurrentPositionY = 0f;


	public GameOverMenu(ForwardPlusRenderer renderer, UIPanelProgram uiPanelProgram, ScorePanelProgram scorePanelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.renderer = renderer;
		this.uiPanelProgram = uiPanelProgram;
		this.scorePanelProgram = scorePanelProgram;
		this.textures = textures;

		currentState = UI_STATE_NOT_VISIBLE;

		createMatrices(screenWidth, screenHeight);
		createElements(screenWidth, screenHeight);
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


	@SuppressWarnings("unused")
	private void createElements(float screenWidth, float screenHeight)
	{
		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchPanelVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.06f, screenHeight * 0.56f, screenHeight * 0.06f, UI_BASE_CENTER_CENTER);

		float numberWidth = screenHeight * NUMBER_HEIGHT_PERCENTAGE * 0.7134f * 1.5f;
		float numberHeight = screenHeight * NUMBER_HEIGHT_PERCENTAGE * 1.5f;
		scoreNumberWidth = numberWidth;

		createScoreGeometry(numberWidth, numberHeight);

		float titleHeight = screenHeight * 0.1f;
		float titleWidth = titleHeight * 10f;

		background9PatchPosition[0] = 0f;
		background9PatchPosition[1] = 0f;
		background9PatchCurrentScale[0] = 1f;
		background9PatchCurrentScale[1] = 1f;

		gameOverTitleScale[0] = titleWidth;
		gameOverTitleScale[1] = titleHeight;
		gameOverTitlePosition[0] = 0f;
		gameOverTitlePosition[1] = titleHeight * 2f;
		gameOverTitleCurrentScale[0] = gameOverTitleScale[0];
		gameOverTitleCurrentScale[1] = gameOverTitleScale[1];
		gameOverTitleCurrentPosition[0] = gameOverTitlePosition[0];
		gameOverTitleCurrentPosition[1] = gameOverTitlePosition[1];

		finalScoreTitleScale[0] = titleHeight * 4.5f;
		finalScoreTitleScale[1] = titleHeight;
		finalScoreTitlePosition[0] = 0f;
		finalScoreTitlePosition[1] = titleHeight;
		finalScoreTitleCurrentScale[0] = finalScoreTitleScale[0];
		finalScoreTitleCurrentScale[1] = finalScoreTitleScale[1];
		finalScoreTitleCurrentPosition[0] = finalScoreTitlePosition[0];
		finalScoreTitleCurrentPosition[1] = finalScoreTitlePosition[1];

		touchToContinueTitleScale[0] = titleHeight * 6f;
		touchToContinueTitleScale[1] = titleHeight * 1.5f;
		touchToContinueTitlePosition[0] = 0f;
		touchToContinueTitlePosition[1] = titleHeight * -1.75f;
		touchToContinueTitleCurrentScale[0] = touchToContinueTitleScale[0];
		touchToContinueTitleCurrentScale[1] = touchToContinueTitleScale[1];
		touchToContinueTitleCurrentPosition[0] = touchToContinueTitlePosition[0];
		touchToContinueTitleCurrentPosition[1] = touchToContinueTitlePosition[1];

		newRecordTitleScale[0] = titleWidth;
		newRecordTitleScale[1] = titleHeight;
		newRecordTitlePosition[0] = 0f;
		newRecordTitlePosition[1] = finalScoreTitlePosition[1];
		newRecordTitleCurrentScale[0] = newRecordTitleScale[0];
		newRecordTitleCurrentScale[1] = newRecordTitleScale[1];
		newRecordTitleCurrentPosition[0] = newRecordTitlePosition[0];
		newRecordTitleCurrentPosition[1] = newRecordTitlePosition[1];

		scorePositionY = (screenHeight * -0.05f);
		scoreCurrentPositionY = scorePositionY;

		for(int i=0; i<8; i++)
		{
			scorePositionsX[i] = 0f;
			scoreCurrentPositionsX[i] = 0f;
		}
	}


	public void setAppearing(int currentScore, boolean isNewRecord)
	{
		currentState = UI_STATE_APPEARING;
		this.isNewRecord = isNewRecord;

		int i;
		boolean end = false;
		int number;
		int nextScore = currentScore;

		for(i=0; i<8; i++)
		{
			scoreNumbers[i] = 0;
		}

		i=0;
		scoreNumberOfDigits = 0;

		while(i < 8 && !end)
		{
			number = nextScore % 10;
			nextScore = nextScore / 10;

			scoreNumbers[i] = number;
			scoreNumberOfDigits++;

			if(nextScore == 0) end = true;

			i++;
		}

		float initialX;

		if(scoreNumberOfDigits % 2 == 0)
		{
			initialX = FloatMath.floor((float)scoreNumberOfDigits / 2f) * scoreNumberWidth - (scoreNumberWidth * 0.5f);
		}
		else
		{
			initialX = FloatMath.floor((float)scoreNumberOfDigits / 2f) * scoreNumberWidth;
		}

		for(i=0; i<scoreNumberOfDigits; i++)
		{
			scorePositionsX[i] = initialX - (i*scoreNumberWidth);
			scoreCurrentPositionsX[i] = scorePositionsX[i];
		}
	}


	public void touch()
	{
		currentState = UI_STATE_DISAPPEARING;
		renderer.changingFromGameOverMenuToMainMenu();
	}


	public void update(float deltaTime)
	{
		menuGlobalTimer += deltaTime;

		if(currentState == UI_STATE_APPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = menuTimer / menuAppearTime;

			if(menuTimer >= menuAppearTime)
			{
				menuTimer = 0f;
				menuOpacity = 1f;
				currentState = UI_STATE_VISIBLE;
				renderer.changedToGameOverMenu();
			}

			background9PatchCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			background9PatchCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			gameOverTitleCurrentScale[0] = lerp(0f, gameOverTitleScale[0], menuOpacity);
			gameOverTitleCurrentScale[1] = lerp(0f, gameOverTitleScale[1], menuOpacity);
			gameOverTitleCurrentPosition[0] = lerp(0f, gameOverTitlePosition[0], menuOpacity);
			gameOverTitleCurrentPosition[1] = lerp(0f, gameOverTitlePosition[1], menuOpacity);

			finalScoreTitleCurrentScale[0] = lerp(0f, finalScoreTitleScale[0], menuOpacity);
			finalScoreTitleCurrentScale[1] = lerp(0f, finalScoreTitleScale[1], menuOpacity);
			finalScoreTitleCurrentPosition[0] = lerp(0f, finalScoreTitlePosition[0], menuOpacity);
			finalScoreTitleCurrentPosition[1] = lerp(0f, finalScoreTitlePosition[1], menuOpacity);

			touchToContinueTitleCurrentScale[0] = lerp(0f, touchToContinueTitleScale[0], menuOpacity);
			touchToContinueTitleCurrentScale[1] = lerp(0f, touchToContinueTitleScale[1], menuOpacity);
			touchToContinueTitleCurrentPosition[0] = lerp(0f, touchToContinueTitlePosition[0], menuOpacity);
			touchToContinueTitleCurrentPosition[1] = lerp(0f, touchToContinueTitlePosition[1], menuOpacity);

			newRecordTitleCurrentScale[0] = lerp(0f, newRecordTitleScale[0], menuOpacity);
			newRecordTitleCurrentScale[1] = lerp(0f, newRecordTitleScale[1], menuOpacity);
			newRecordTitleCurrentPosition[0] = lerp(0f, newRecordTitlePosition[0], menuOpacity);
			newRecordTitleCurrentPosition[1] = lerp(0f, newRecordTitlePosition[1], menuOpacity);

			scoreCurrentScale = menuOpacity;
			scoreCurrentPositionY = lerp(0f, scorePositionY, menuOpacity);
			for(int i=0; i<scoreNumberOfDigits; i++)
			{
				scoreCurrentPositionsX[i] = lerp(0f, scorePositionsX[i], menuOpacity);
			}
		}
		else if(currentState == UI_STATE_DISAPPEARING)
		{
			menuTimer += deltaTime;
			menuOpacity = 1.0f - (menuTimer / menuDisappearTime);

			if(menuTimer >= menuDisappearTime)
			{
				menuTimer = 0f;
				menuOpacity = 0f;
				currentState = UI_STATE_NOT_VISIBLE;
			}

			background9PatchCurrentScale[0] = lerp(0f, 1f, menuOpacity);
			background9PatchCurrentScale[1] = lerp(0f, 1f, menuOpacity);

			gameOverTitleCurrentScale[0] = lerp(0f, gameOverTitleScale[0], menuOpacity);
			gameOverTitleCurrentScale[1] = lerp(0f, gameOverTitleScale[1], menuOpacity);
			gameOverTitleCurrentPosition[0] = lerp(0f, gameOverTitlePosition[0], menuOpacity);
			gameOverTitleCurrentPosition[1] = lerp(0f, gameOverTitlePosition[1], menuOpacity);

			finalScoreTitleCurrentScale[0] = lerp(0f, finalScoreTitleScale[0], menuOpacity);
			finalScoreTitleCurrentScale[1] = lerp(0f, finalScoreTitleScale[1], menuOpacity);
			finalScoreTitleCurrentPosition[0] = lerp(0f, finalScoreTitlePosition[0], menuOpacity);
			finalScoreTitleCurrentPosition[1] = lerp(0f, finalScoreTitlePosition[1], menuOpacity);

			touchToContinueTitleCurrentScale[0] = lerp(0f, touchToContinueTitleScale[0], menuOpacity);
			touchToContinueTitleCurrentScale[1] = lerp(0f, touchToContinueTitleScale[1], menuOpacity);
			touchToContinueTitleCurrentPosition[0] = lerp(0f, touchToContinueTitlePosition[0], menuOpacity);
			touchToContinueTitleCurrentPosition[1] = lerp(0f, touchToContinueTitlePosition[1], menuOpacity);

			newRecordTitleCurrentScale[0] = lerp(0f, newRecordTitleScale[0], menuOpacity);
			newRecordTitleCurrentScale[1] = lerp(0f, newRecordTitleScale[1], menuOpacity);
			newRecordTitleCurrentPosition[0] = lerp(0f, newRecordTitlePosition[0], menuOpacity);
			newRecordTitleCurrentPosition[1] = lerp(0f, newRecordTitlePosition[1], menuOpacity);

			scoreCurrentScale = menuOpacity;
			scoreCurrentPositionY = lerp(0f, scorePositionY, menuOpacity);
			for(int i=0; i<scoreNumberOfDigits; i++)
			{
				scoreCurrentPositionsX[i] = lerp(0f, scorePositionsX[i], menuOpacity);
			}
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

			// Game Over title
			uiPanelProgram.setUniforms(viewProjection, gameOverTitleCurrentScale, gameOverTitleCurrentPosition, textures.gameOverTitleTexture, menuOpacity);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

			if(isNewRecord)
			{
				// New Record title
				uiPanelProgram.setUniforms(viewProjection, newRecordTitleCurrentScale, newRecordTitleCurrentPosition, textures.newRecordTitleTexture,
						Math.abs(menuOpacity * FloatMath.cos(menuGlobalTimer * 1.75f)));
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}
			else
			{
				// Final score title
				uiPanelProgram.setUniforms(viewProjection, finalScoreTitleCurrentScale, finalScoreTitleCurrentPosition, textures.finalScoreTitleTexture, menuOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}

			// Touch to continue title
			uiPanelProgram.setUniforms(viewProjection, touchToContinueTitleCurrentScale, touchToContinueTitleCurrentPosition, textures.touchToContinueTitleTexture,
					Math.abs(menuOpacity * FloatMath.sin(menuGlobalTimer)));
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


			// Score
			scorePanelProgram.useProgram();
			scorePanelProgram.setCommonUniforms(viewProjection, textures.numbersAtlasTexture);
			glBindVertexArray(scoreVaoHandle[0]);

			for(int i=0; i < scoreNumberOfDigits; i++)
			{
				scorePanelProgram.setSpecificUniforms(scoreCurrentScale,
						scoreCurrentPositionsX[i], scoreCurrentPositionY,
						scoreTexCoordOffsetsX[scoreNumbers[i]], scoreTexCoordOffsetsY[scoreNumbers[i]],
						1f, 1f, 1f, menuOpacity);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
			}
		}
	}
}
