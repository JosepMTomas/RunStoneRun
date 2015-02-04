package com.josepmtomas.runstonerun.data;

import com.josepmtomas.runstonerun.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

/**
 * Created by Josep on 16/07/2014.
 * @author Josep
 */

public class IndexBuffer
{
	private final int bufferId;
	private final int numElements;


	public IndexBuffer(int[] indexData)
	{
		numElements = indexData.length;

		// Allocate a buffer
		final int buffers[] = new int[1];

		glGenBuffers(buffers.length, buffers, 0);
		if (buffers[0] == 0)
		{
			throw new RuntimeException("Could not create a new vertex buffer object.");
		}
		bufferId = buffers[0];

		// Bind the buffer
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

		// Transfer data to native memory
		IntBuffer indexArray = ByteBuffer
				.allocateDirect(indexData.length * Constants.BYTES_PER_INT)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer()
				.put(indexData);
		indexArray.position(0);

		// Transfer data from native memory to the GPU buffer
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray.capacity() * Constants.BYTES_PER_INT, indexArray, GL_STATIC_DRAW);

		// IMPORTANT: Unbind from the buffer when we're done with it
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	public int getBufferId()
	{
		return bufferId;
	}


	public int getNumElements()
	{
		return numElements;
	}
}
