package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.programs.DeferredScreenShaderProgram;
import com.josepmtomas.rockgame.programs.PostProcessShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/08/2014.
 */
public class Screen
{
	private static final String TAG = "Screen";

	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = 3 * Constants.BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = 5 * Constants.BYTES_PER_FLOAT;

	private float[] vertices = new float[20];
	private short[] elements = new short[6];

	private int[] vboHandles = new int[2];
	private int[] vaoHandle = new int[1];

	private DeferredScreenShaderProgram deferredScreenShaderProgram;
	private PostProcessShaderProgram postProcessShaderProgram;
	private int[] buffers;

	public Screen(Context context, float width, float height)
	{
		deferredScreenShaderProgram = new DeferredScreenShaderProgram(context);
		postProcessShaderProgram = new PostProcessShaderProgram(context);

		createScreenPlane(width, height);
		initialize();
	}

	private void createScreenPlane(float planeWidth, float planeHeight)
	{
		/*float bottom = 0.0f;
		float top = planeHeight;
		float right = planeWidth;
		float left = 0.0f;*/

		float bottom = -1f;
		float left = -1f;
		float width = 2f;
		float height = 2f;

		/*float bottom = -0.75f;
		float left = -0.75f;
		float width = 1.5f;
		float height = 1.5f;*/

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
				vertices[offset++] = (float)y;
			}
		}

		// Indices
		elements[0] = 0;
		elements[1] = 1;
		elements[2] = 2;

		elements[3] = 1;
		elements[4] = 3;
		elements[5] = 2;
	}


	public void initialize()
	{
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		// Build the arrays
		verticesBuffer = ByteBuffer
				.allocateDirect(vertices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * Constants.BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * Constants.BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	public void setBuffers(int[] buffers)
	{
		this.buffers = buffers;
	}


	public void draw(float[] inverseViewProjection, float[] inverseProjection, int index)
	{
		deferredScreenShaderProgram.useProgram();
		deferredScreenShaderProgram.setUniforms(inverseViewProjection, inverseProjection, buffers, index);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_SHORT, 0);
	}



	public void drawPostProcess(int colorBuffer, int depthBuffer, float[] VPInverse, float[] previousVP)
	{
		postProcessShaderProgram.useProgram();
		postProcessShaderProgram.setUniforms(colorBuffer, depthBuffer, VPInverse, previousVP);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_SHORT, 0);
	}
}
