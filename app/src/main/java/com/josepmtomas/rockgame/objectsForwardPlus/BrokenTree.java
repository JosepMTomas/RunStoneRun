package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.josepmtomas.rockgame.Constants.*;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 10/12/2014.
 */
public class BrokenTree
{
	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
	private static final int TANGENT_COMPONENTS = 4;
	private static final int TOTAL_COMPONENTS = POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS;

	private static final int POSITION_OFFSET = 0;
	private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	private static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	private static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;

	private static final int POSITION_BYTE_OFFSET = POSITION_OFFSET * BYTES_PER_FLOAT;
	private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * BYTES_PER_FLOAT;
	private static final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * BYTES_PER_FLOAT;
	private static final int TANGENT_BYTE_OFFSET = TANGENT_OFFSET * BYTES_PER_FLOAT;

	private static final int BYTE_STRIDE = TOTAL_COMPONENTS * BYTES_PER_FLOAT;

	private Context context;

	// Root geometry definition
	private float[] rootVertices;
	private short[] rootElements;
	public int numRootElementsToDraw = 0;
	private int[] rootVboHandles = new int[2];
	public int[] rootVaoHandle = new int[1];

	// Top geometry definition
	private float[] topVertices;
	private short[] topElements;
	public int numTopElementsToDraw = 0;
	private int[] topVboHandles = new int[2];
	public int[] topVaoHandle = new int[1];


	public BrokenTree(Context context, String rootFileName, String topFileName)
	{
		glGenVertexArrays(1, rootVaoHandle, 0);
		glGenVertexArrays(1, topVaoHandle, 0);

		this.loadRoot(context, rootFileName);
		this.loadTop(context, topFileName);
	}


	private void loadRoot(Context context, String rootFileName)
	{
		int numVertices, numElements;
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Read geometry from file
		////////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			InputStream inputStream = context.getResources().getAssets().open(rootFileName);
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
					rootVertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numRootElementsToDraw = numElements * 3;
					rootElements = new short[numRootElementsToDraw];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					rootVertices[verticesOffset++] = Float.parseFloat(tokens[12]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					rootElements[elementsOffset++] = Short.parseShort(tokens[1]);
					rootElements[elementsOffset++] = Short.parseShort(tokens[2]);
					rootElements[elementsOffset++] = Short.parseShort(tokens[3]);
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
				.allocateDirect(rootVertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rootVertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(rootElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(rootElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, rootVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, rootVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rootVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glBindVertexArray(rootVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, rootVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, rootVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, rootVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, rootVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rootVboHandles[1]);

		glBindVertexArray(0);
	}


	private void loadTop(Context context, String topFileName)
	{
		int numVertices, numElements;
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Read geometry from file
		////////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			InputStream inputStream = context.getResources().getAssets().open(topFileName);
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
					topVertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numTopElementsToDraw = numElements * 3;
					topElements = new short[numTopElementsToDraw];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					topVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					topVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					topVertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					topVertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					topVertices[verticesOffset++] = Float.parseFloat(tokens[12]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					topElements[elementsOffset++] = Short.parseShort(tokens[1]);
					topElements[elementsOffset++] = Short.parseShort(tokens[2]);
					topElements[elementsOffset++] = Short.parseShort(tokens[3]);
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
				.allocateDirect(topVertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(topVertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(topElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(topElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, topVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, topVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, topVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glBindVertexArray(topVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, topVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, topVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, topVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, topVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, topVboHandles[1]);

		glBindVertexArray(0);
	}
}
