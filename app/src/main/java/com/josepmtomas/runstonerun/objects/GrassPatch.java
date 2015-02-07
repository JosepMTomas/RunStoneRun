package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import com.josepmtomas.runstonerun.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 30/09/2014.
 * @author Josep
 */

public class GrassPatch
{
	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int TOTAL_COMPONENTS = POSITION_COMPONENTS + TEXCOORD_COMPONENTS;

	private static final int POSITION_OFFSET = 0;
	private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;

	private static final int POSITION_BYTE_OFFSET = POSITION_OFFSET * Constants.BYTES_PER_FLOAT;
	private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * Constants.BYTES_PER_FLOAT;

	private static final int BYTE_STRIDE = TOTAL_COMPONENTS * Constants.BYTES_PER_FLOAT;

	// Geometry definition
	private float[] vertices;
	private short[] elements;
	private int[] vboHandles;
	private int[] vaoHandle;


	public GrassPatch(Context context)
	{
		vboHandles = new int[2];
		vaoHandle = new int[1];

		load(context, "models/test_grass.vbm");
		initialize();
	}


	private void load(Context context, String fileName)
	{
		int numVertices;
		int numElements;

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
					vertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					elements = new short[3 * numElements];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					vertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					vertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]) * -1.0f;
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					elements[elementsOffset++] = Short.parseShort(tokens[1]);
					elements[elementsOffset++] = Short.parseShort(tokens[2]);
					elements[elementsOffset++] = Short.parseShort(tokens[3]);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private void initialize()
	{
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		// Build the java native buffers
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
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

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

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	public int getVaoHandle()
	{
		return vaoHandle[0];
	}
}