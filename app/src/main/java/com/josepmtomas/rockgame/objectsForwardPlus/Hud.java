package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.algebra.vec2;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.programsForwardPlus.ProgressBarProgram;
import com.josepmtomas.rockgame.programsForwardPlus.ScorePanelProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import static com.josepmtomas.rockgame.Constants.*;

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

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Score panel
	private int[] scoreVboHandles = new int[2];
	private int[] scoreVaoHandle = new int[1];
	private float[] scorePositionOffsetsX = new float[8];//{1700f, 1572f, 1444f, 1316f, 1188f, 1060f, 932f, 804f};
	private float scorePositionOffsetY = 0;
	private float[] scoreTexCoordOffsetsX = {    0f,    0f,     0f, 0f,  0.25f, 0.25f,  0.25f, 0.25f,   0.5f,  0.5f,   0.5f, 0.5f,  0.75f, 0.75f};
	private float[] scoreTexCoordOffsetsY = {-0.75f, -0.5f, -0.25f, 0f, -0.75f, -0.5f, -0.25f,    0f, -0.75f, -0.5f, -0.25f,   0f, -0.75f, -0.5f};
	private int scoreNumbersTexture;
	private int scoreNumbersNegativeTexture;
	private int[] scoreNumbers = {0, 0, 0, 0, 0, 0, 0, 0};

	// Multiplier panel
	private float[] multiplierVertices;
	private short[] multiplierElements;
	private int numMultiplierElementsToDraw;
	private int[] multiplierVboHandles = new int[2];
	private int[] multiplierVaoHandle = new int[1];
	private int multiplierTexture;
	private float multiplierBaseOffsetX;
	private float multiplierBaseOffsetY;
	private float[] multiplierNumbersOffsetsX = new float[4];
	private float multiplierNumbersOffsetY;
	private int[] multiplierNumbers = {0, 13, 0, 12};

	// Multiplier progress bar
	private int[] multiplierProgressVboHandles = new int[2];
	private int[] multiplierProgressVaoHandle = new int[1];
	private int multiplierProgressTexture;
	private float multiplierProgressValue = 0f;
	private float multiplierProgressOffsetX = 0;
	private float multiplierProgressOffsetY = 0;


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


	public Hud(Context context, float screenWidth, float screenHeight, float numberWidth, float numberHeight)
	{
		// Common matrices
		createMatrices(screenWidth, screenHeight);

		// Score panel
		createScoreGeometry(screenHeight * NUMBER_HEIGHT_PERCENTAGE, screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		scoreNumbersTexture = TextureHelper.loadETC2Texture(context, "textures/hud/numbers_atlas_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		scoreNumbersNegativeTexture = TextureHelper.loadETC2Texture(context, "textures/hud/numbers_atlas_negative_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier panel
		loadMultiplierGeometry(context, "models/hud_multiplier_base.vbm", screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		multiplierTexture = TextureHelper.loadETC2Texture(context, "textures/hud/multiplier_9patch_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Multiplier progress bar
		createMultiplierProgressGeometry(screenHeight * NUMBER_HEIGHT_PERCENTAGE);
		multiplierProgressTexture = TextureHelper.loadETC2Texture(context, "textures/hud/gradient_h_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Positions
		setPositions(screenWidth, screenHeight, screenHeight * NUMBER_HEIGHT_PERCENTAGE, screenHeight * NUMBER_HEIGHT_PERCENTAGE);

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


	private void loadMultiplierGeometry(Context context, String fileName, float numberHeight)
	{
		int numVertices, numElements;
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;


		////////////////////////////////////////////////////////////////////////////////////////////
		// Read geometry from file
		////////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			InputStream inputStream = context.getResources().getAssets().open(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String nextLine;

			int verticesOffset = 0;
			int elementsOffset = 0;

			while((nextLine = bufferedReader.readLine()) != null)
			{
				// Split the line into tokens separated by spaces
				String[] tokens = nextLine.split(" ");

				// Check the first token of the line
				if(tokens[0].equals("VERTICES"))
				{
					// Get the number of vertices and initialize the positions array
					numVertices = Integer.parseInt(tokens[1]);
					multiplierVertices = new float[5 * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numMultiplierElementsToDraw = numElements * 3;
					multiplierElements = new short[numMultiplierElementsToDraw];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					multiplierVertices[verticesOffset++] = Float.parseFloat(tokens[1]) * numberHeight;;
					multiplierVertices[verticesOffset++] = Float.parseFloat(tokens[2]) * numberHeight;;
					multiplierVertices[verticesOffset++] = 0f;

					// Read the vertex texture coordinates
					multiplierVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					multiplierVertices[verticesOffset++] = 1.0f - Float.parseFloat(tokens[5]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					multiplierElements[elementsOffset++] = Short.parseShort(tokens[1]);
					multiplierElements[elementsOffset++] = Short.parseShort(tokens[2]);
					multiplierElements[elementsOffset++] = Short.parseShort(tokens[3]);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// Buffers
		////////////////////////////////////////////////////////////////////////////////////////////

		verticesBuffer = ByteBuffer
				.allocateDirect(multiplierVertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(multiplierVertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(multiplierElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(multiplierElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, multiplierVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, multiplierVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, multiplierVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, multiplierVaoHandle, 0);
		glBindVertexArray(multiplierVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, multiplierVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5*BYTES_PER_FLOAT, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, multiplierVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5*BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, multiplierVboHandles[1]);

		glBindVertexArray(0);
	}


	private void createMultiplierProgressGeometry(float numberSize)
	{
		float[] vertices = new float[20];
		short[] elements = new short[6];

		float bottom = 0f;
		float left = 0f;
		float width = numberSize * 4.0f;
		float height = numberSize * 0.25f;

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
				vertices[offset++] = (float)x;
				vertices[offset++] = 1.0f - (float)y;
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
		glGenBuffers(2, multiplierProgressVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, multiplierProgressVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, multiplierProgressVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, multiplierProgressVaoHandle, 0);
		glBindVertexArray(multiplierProgressVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, multiplierProgressVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, multiplierProgressVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, multiplierProgressVboHandles[1]);

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

		multiplierBaseOffsetX = screenWidth - (numberWidth / 2f);
		multiplierBaseOffsetY = screenHeight - numberHeight * 4f;

		multiplierNumbersOffsetY = multiplierBaseOffsetY + numberHeight * 1.25f;
		multiplierNumbersOffsetsX[0] = multiplierBaseOffsetX - (numberHeight * 0.75f);

		for(int i=1; i < 4; i++)
		{
			multiplierNumbersOffsetsX[i] = multiplierNumbersOffsetsX[0] - ((float)i * numberWidth);
		}

		multiplierProgressOffsetY = multiplierBaseOffsetY + numberHeight * 0.25f;
		multiplierProgressOffsetX = multiplierBaseOffsetX - numberWidth * 4.25f;
	}


	public void update(int currentScore, int currentMultiplier, float multiplierPercent, int currentFps)
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

	}


	public void draw()
	{
		// Score panel
		scorePanelProgram.useProgram();
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);

		glBindVertexArray(scoreVaoHandle[0]);

		for(int i=0; i < 8; i++)
		{
			scorePanelProgram.setSpecificUniforms(scorePositionOffsetsX[i], scorePositionOffsetY, scoreTexCoordOffsetsX[scoreNumbers[i]], scoreTexCoordOffsetsY[scoreNumbers[i]]);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersNegativeTexture);
		for(int i=0; i < 4; i++)
		{
			scorePanelProgram.setSpecificUniforms(multiplierNumbersOffsetsX[i], multiplierNumbersOffsetY, scoreTexCoordOffsetsX[multiplierNumbers[i]], scoreTexCoordOffsetsY[multiplierNumbers[i]]);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}

		//scorePanelProgram.setSpecificUniforms(scorePositionOffsetY, scoreTexCoordOffsetsX[scoreNumbers[0]], scoreTexCoordOffsetsY[scoreNumbers[0]]);

		// current FPS
		scorePanelProgram.setCommonUniforms(viewProjection, scoreNumbersTexture);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[0], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[0]], scoreTexCoordOffsetsY[currentFpsNumbers[0]]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[1], currentFpsPositionOffsetY, scoreTexCoordOffsetsX[currentFpsNumbers[1]], scoreTexCoordOffsetsY[currentFpsNumbers[1]]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// average FPS
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[0], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[0]], scoreTexCoordOffsetsY[averageFpsNumbers[0]]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		scorePanelProgram.setSpecificUniforms(fpsPositionOffsetsX[1], averageFpsPositionOffsetY, scoreTexCoordOffsetsX[averageFpsNumbers[1]], scoreTexCoordOffsetsY[averageFpsNumbers[1]]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);


		///// multiplier

		scorePanelProgram.setCommonUniforms(viewProjection, multiplierTexture);
		scorePanelProgram.setSpecificUniforms(multiplierBaseOffsetX, multiplierBaseOffsetY, 0f, 0f);

		glBindVertexArray(multiplierVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, numMultiplierElementsToDraw, GL_UNSIGNED_SHORT, 0);


		/// Progress bar program

		progressBarProgram.useProgram();
		progressBarProgram.setCommonUniforms(viewProjection, multiplierProgressTexture);
		progressBarProgram.setSpecificUniforms(multiplierProgressOffsetX, multiplierProgressOffsetY, multiplierProgressValue);

		glBindVertexArray(multiplierProgressVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
	}
}
