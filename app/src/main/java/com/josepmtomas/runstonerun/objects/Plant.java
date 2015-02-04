package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

import static com.josepmtomas.runstonerun.Constants.*;

/**
 * Created by Josep on 11/10/2014.
 * @author Josep
 */

public class Plant
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

	// Main geometry definition
	private float[] vertices;
	private short[] elements;
	public int[] numElementsToDraw = new int[2];
	private int[] vboHandlesLODA = new int[2];
	private int[] vboHandlesLODB = new int[2];
	public int[] vaoHandles = new int[2];

	// Shadow mesh geometry definition
	private float[] shadowVertices;
	private short[] shadowElements;
	public int numShadowElementsToDraw;
	private int[] shadowVboHandles = new int[2];
	public int[] shadowVaoHandle = new int[1];

	// Reflection mesh geometry definition
	private float[] reflectionVertices;
	private short[] reflectionElements;
	public int numReflectionElementsToDraw;
	private int[] reflectionVboHandles = new int[2];
	public int[] reflectionVaoHandle = new int[1];


	public Plant(Context context, String[] fileNames)
	{
		this.context = context;

		glGenVertexArrays(2, vaoHandles, 0);

		loadLODA(fileNames[LOD_A]);
		loadLODB(fileNames[LOD_B]);
	}


	public void addShadowGeometry(String shadowFileName)
	{
		loadShadow(context, shadowFileName);
	}


	public void addReflectionGeometry(String reflectionFileName)
	{
		loadReflection(context, reflectionFileName);
	}


	private void loadLODA(String fileName)
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
					vertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numElementsToDraw[LOD_A] = numElements * 3;
					elements = new short[numElementsToDraw[LOD_A]];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					vertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					vertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					vertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					vertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[12]);
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

		////////////////////////////////////////////////////////////////////////////////////////////
		// Buffers
		////////////////////////////////////////////////////////////////////////////////////////////

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
		glGenBuffers(2, vboHandlesLODA, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODA[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandlesLODA[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glBindVertexArray(vaoHandles[LOD_A]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODA[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODA[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODA[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODA[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandlesLODA[1]);

		glBindVertexArray(0);
	}


	private void loadLODB(String fileName)
	{
		final int POSITION_OFFSET = 0;
		final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
		final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;

		final int POSITION_BYTE_OFFSET = POSITION_OFFSET * BYTES_PER_FLOAT;
		final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * BYTES_PER_FLOAT;
		final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * BYTES_PER_FLOAT;

		final int BYTE_STRIDE = (POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS) * BYTES_PER_FLOAT;

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
					vertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numElementsToDraw[LOD_B] = numElements * 3;
					elements = new short[numElementsToDraw[LOD_B]];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					vertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					vertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					vertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[8]);
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

		////////////////////////////////////////////////////////////////////////////////////////////
		// Buffers
		////////////////////////////////////////////////////////////////////////////////////////////

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
		glGenBuffers(2, vboHandlesLODB, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODB[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandlesLODB[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glBindVertexArray(vaoHandles[LOD_B]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODB[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODB[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandlesLODB[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandlesLODB[1]);

		glBindVertexArray(0);
	}


	private void loadShadow(Context context, String shadowFileName)
	{
		int numShadowVertices, numShadowElements;
		FloatBuffer shadowVerticesBuffer;
		ShortBuffer shadowElementsBuffer;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Read shadow geometry from file
		////////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			InputStream inputStream = context.getResources().getAssets().open(shadowFileName);
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
					numShadowVertices = Integer.parseInt(tokens[1]);
					shadowVertices = new float[POSITION_COMPONENTS * numShadowVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numShadowElements = Integer.parseInt(tokens[1]);
					numShadowElementsToDraw = numShadowElements * 3;
					shadowElements = new short[numShadowElementsToDraw];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					shadowVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					shadowVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					shadowVertices[verticesOffset++] = Float.parseFloat(tokens[3]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					shadowElements[elementsOffset++] = Short.parseShort(tokens[1]);
					shadowElements[elementsOffset++] = Short.parseShort(tokens[2]);
					shadowElements[elementsOffset++] = Short.parseShort(tokens[3]);
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

		// Build the java native buffers
		shadowVerticesBuffer = ByteBuffer
				.allocateDirect(shadowVertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(shadowVertices);
		shadowVerticesBuffer.position(0);

		shadowElementsBuffer = ByteBuffer
				.allocateDirect(shadowElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(shadowElements);
		shadowElementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, shadowVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, shadowVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  shadowVerticesBuffer.capacity() * BYTES_PER_FLOAT, shadowVerticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, shadowVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, shadowElementsBuffer.capacity() * BYTES_PER_SHORT, shadowElementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, shadowVaoHandle, 0);
		glBindVertexArray(shadowVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, shadowVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, POSITION_COMPONENTS * BYTES_PER_FLOAT, POSITION_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, shadowVboHandles[1]);

		glBindVertexArray(0);
	}


	private void loadReflection(Context context, String reflectionFileName)
	{
		int numVertices, numElements;
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Read geometry from file
		////////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			InputStream inputStream = context.getResources().getAssets().open(reflectionFileName);
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
					reflectionVertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numReflectionElementsToDraw = numElements * 3;
					reflectionElements = new short[numReflectionElementsToDraw];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[5]) * -1.0f;

					// Read the vertex normals
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[12]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					reflectionElements[elementsOffset++] = Short.parseShort(tokens[1]);
					reflectionElements[elementsOffset++] = Short.parseShort(tokens[2]);
					reflectionElements[elementsOffset++] = Short.parseShort(tokens[3]);
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
				.allocateDirect(reflectionVertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(reflectionVertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(reflectionElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(reflectionElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, reflectionVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, reflectionVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, reflectionVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, reflectionVaoHandle, 0);
		glBindVertexArray(reflectionVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, reflectionVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, reflectionVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, reflectionVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, reflectionVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, reflectionVboHandles[1]);

		glBindVertexArray(0);
	}
}
