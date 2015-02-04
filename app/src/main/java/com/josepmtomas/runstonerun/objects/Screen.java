package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import com.josepmtomas.runstonerun.Constants;
import com.josepmtomas.runstonerun.programs.PostProcessHighProgram;
import com.josepmtomas.runstonerun.programs.PostProcessLowProgram;
import com.josepmtomas.runstonerun.programs.ScreenProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 09/09/2014.
 * @author Josep
 */

public class Screen
{
	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = 3 * Constants.BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = 5 * Constants.BYTES_PER_FLOAT;

	private float[] vertices = new float[20];
	private short[] elements = new short[6];

	private int[] vboHandles = new int[2];
	private int[] vaoHandle = new int[1];

	// Current level of detail
	private int currentDetail = 0;

	// Shaders
	ScreenProgram screenProgram;
	PostProcessLowProgram postProcessLowProgram;
	PostProcessHighProgram postProcessHighProgram;


	public Screen(Context context)
	{
		screenProgram = new ScreenProgram(context);
		postProcessLowProgram = new PostProcessLowProgram(context);
		postProcessHighProgram = new PostProcessHighProgram(context);

		createScreenPlane();
		initialize();
	}

	private void createScreenPlane()
	{
		float bottom = -1f;
		float left = -1f;
		float width = 2f;
		float height = 2f;

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


	private void initialize()
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


	public void draw(int colorBuffer, float speedFactor)
	{
		if(currentDetail == 0)
		{
			screenProgram.useProgram();
			screenProgram.setUniforms(colorBuffer);
		}
		else if(currentDetail == 1)
		{
			postProcessLowProgram.useProgram();
			postProcessLowProgram.setUniforms(colorBuffer, speedFactor);
		}
		else if(currentDetail == 2)
		{
			postProcessHighProgram.useProgram();
			postProcessHighProgram.setUniforms(colorBuffer, speedFactor);
		}



		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_SHORT, 0);
	}


	public void setNoPostProcess()
	{
		currentDetail = 0;
	}


	public void setLowPostProcess()
	{
		currentDetail = 1;
	}


	public void setHighPostProcess()
	{
		currentDetail = 2;
	}
}
