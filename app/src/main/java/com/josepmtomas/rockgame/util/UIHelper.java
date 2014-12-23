package com.josepmtomas.rockgame.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

import static com.josepmtomas.rockgame.Constants.*;

/**
 * Created by Josep on 15/12/2014.
 */
public class UIHelper
{


	public static int makePanel(float width, float height, int baseType)
	{
		int[] vboHandles = new int[2];
		int[] vaoHandle = new int[1];

		// Geometry construction variables
		float initialX, initialY;
		float currentX, currentY;

		// Geometry information
		float[] vertices = new float[48];
		short[] elements = new short[18];
		int verticesOffset = 0;

		// initialize
		switch(baseType)
		{
			case UI_BASE_LEFT_TOP:
				initialX = 0f;
				initialY = 0f;
				break;
			case UI_BASE_LEFT_CENTER:
				initialX = 0f;
				initialY = height * 0.5f;
				break;
			case UI_BASE_LEFT_BOTTOM:
				initialX = 0f;
				initialY = height;
				break;
			case UI_BASE_CENTER_TOP:
				initialX = -width * 0.5f;
				initialY = 0f;
				break;
			case UI_BASE_CENTER_CENTER:
				initialX = -width * 0.5f;
				initialY = height * 0.5f;
				break;
			case UI_BASE_CENTER_BOTTOM:
				initialX = -width * 0.5f;
				initialY = height;
				break;
			case UI_BASE_RIGHT_TOP:
				initialX = -width;
				initialY = 0f;
				break;
			case UI_BASE_RIGHT_CENTER:
				initialX = -width;
				initialY = height * 0.5f;
				break;
			case UI_BASE_RIGHT_BOTTOM:
				initialX = -width;
				initialY = height;
				break;
			default:
				initialX = 0f;
				initialY = 0f;
				break;
		}

		currentY = initialY;
		for(int y=0; y < 2; y++)
		{
			currentX = initialX;
			for(int x=0; x < 2; x++)
			{
				// positions
				vertices[verticesOffset++] = currentX;
				vertices[verticesOffset++] = currentY;
				vertices[verticesOffset++] = 0f;

				// texture coordinates
				vertices[verticesOffset++] = (float)x;
				vertices[verticesOffset++] = (float)y;

				currentX = currentX + width;
			}
			currentY = currentY - height;
		}

		// Elements
		elements[0] = 0;
		elements[1] = 2;
		elements[2] = 1;

		elements[3] = 2;
		elements[4] = 3;
		elements[5] = 1;


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
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);

		return vaoHandle[0];
	}

	/**
	 *
	 * @param width width of the progress bar.
	 * @param height height of the progress bar.
	 * @param baseType specify the base point of the object.
	 * @return vertex array object reference for the created progress bar.
	 */
	public static int makeProgressBar(float width, float height, int baseType)
	{
		// OpenGL buffers
		int[] vboHandles = new int[2];
		int[] vaoHandle = new int[1];

		// Geometry construction variables
		float[] percentagesX = new float[4];
		float[] incrementsX = new float[4];
		float[] uvsX = {0.0f, 0.25f, 0.75f, 1.0f};
		float[] uvsY = {1f, 0f};
		float initialX, initialY;
		float currentX, currentY;
		float currentPercentage;

		// Geometry information
		float[] vertices = new float[48];
		short[] elements = new short[18];
		int verticesOffset = 0;

		// initialize
		switch(baseType)
		{
			case UI_BASE_LEFT_TOP:
				initialX = 0f;
				initialY = 0f;
				break;
			case UI_BASE_LEFT_CENTER:
				initialX = 0f;
				initialY = height * 0.5f;
				break;
			case UI_BASE_LEFT_BOTTOM:
				initialX = 0f;
				initialY = height;
				break;
			case UI_BASE_CENTER_TOP:
				initialX = -width * 0.5f;
				initialY = 0f;
				break;
			case UI_BASE_CENTER_CENTER:
				initialX = -width * 0.5f;
				initialY = height * 0.5f;
				break;
			case UI_BASE_CENTER_BOTTOM:
				initialX = -width * 0.5f;
				initialY = height;
				break;
			case UI_BASE_RIGHT_TOP:
				initialX = -width;
				initialY = 0f;
				break;
			case UI_BASE_RIGHT_CENTER:
				initialX = -width;
				initialY = height * 0.5f;
				break;
			case UI_BASE_RIGHT_BOTTOM:
				initialX = -width;
				initialY = height;
				break;
			default:
				initialX = 0f;
				initialY = 0f;
				break;
		}

		percentagesX[0] = (height / width) * 0.5f;
		percentagesX[1] = 1.0f - (percentagesX[0] * 2f);
		percentagesX[2] = percentagesX[0];
		percentagesX[3] = 0f;

		incrementsX[0] = percentagesX[0] * width;
		incrementsX[1] = percentagesX[1] * width;
		incrementsX[2] = percentagesX[2] * width;
		incrementsX[3] = 0f;

		currentY = initialY;
		for(int y=0; y < 2; y++)
		{
			currentX = initialX;
			currentPercentage = 1.0f;

			for(int x=0; x < 4; x++)
			{
				// Positions
				vertices[verticesOffset++] = currentX;
				vertices[verticesOffset++] = currentY;
				vertices[verticesOffset++] = 0f;

				// Texture coordinates
				vertices[verticesOffset++] = uvsX[x];
				vertices[verticesOffset++] = uvsY[y];

				// Percentage
				vertices[verticesOffset++] = currentPercentage;

				currentX = currentX + incrementsX[x];
				currentPercentage = currentPercentage - percentagesX[x];
			}
			currentY = currentY - height;
		}

		elements[0] = 0;
		elements[1] = 4;
		elements[2] = 1;

		elements[3] = 4;
		elements[4] = 5;
		elements[5] = 1;

		elements[6] = 1;
		elements[7] = 5;
		elements[8] = 2;

		elements[9]  = 5;
		elements[10] = 6;
		elements[11] = 2;

		elements[12] = 3;
		elements[13] = 2;
		elements[14] = 6;

		elements[15] = 6;
		elements[16] = 7;
		elements[17] = 3;

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
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * BYTES_PER_FLOAT, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

		// Vertex percentage
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * BYTES_PER_FLOAT, 5 * BYTES_PER_FLOAT);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);

		// return vertex array object
		return vaoHandle[0];
	}
}
