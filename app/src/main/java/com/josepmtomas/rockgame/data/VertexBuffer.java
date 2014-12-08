package com.josepmtomas.rockgame.data;

import com.josepmtomas.rockgame.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

/**
 * Created by Josep on 16/07/2014.
 */
public class VertexBuffer
{
	private final int bufferId;
	private final int size;


	public VertexBuffer(float[] vertexData)
	{
		size = vertexData.length;

		// Allocate a buffer
		final int buffers[] = new int[1];

		glGenBuffers(buffers.length, buffers, 0);
		if(buffers[0] == 0)
		{
			throw new RuntimeException("Could not create a new vertex buffer object.");
		}
		bufferId = buffers[0];

		// Bind the buffer
		glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);

		// Transfer data to native memory
		FloatBuffer vertexArray = ByteBuffer
				.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertexData);
		vertexArray.position(0);

		// Transfer data from native memory to the GPU buffer
		glBufferData(GL_ARRAY_BUFFER, vertexArray.capacity() * Constants.BYTES_PER_FLOAT, vertexArray, GL_STATIC_DRAW);

		// IMPORTANT: Unbind from the buffer when we're done with it
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}


	public void update(float[] vertexData)
	{
		// Bind the buffer
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);

		// Transfer data to native memory
		FloatBuffer vertexArray = ByteBuffer
				.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertexData);
		vertexArray.position(0);

		// Transfer data from native memory to the GPU buffer
		glBufferData(GL_ARRAY_BUFFER, vertexArray.capacity() * Constants.BYTES_PER_FLOAT, vertexArray, GL_STATIC_DRAW);

		// IMPORTANT: Unbind from the buffer when we're done with it
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}


	public int getBufferId()
	{
		return bufferId;
	}

	public int size()
	{
		return size;
	}


	public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride)
	{
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);
		glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, dataOffset);
		glEnableVertexAttribArray(attributeLocation);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}
