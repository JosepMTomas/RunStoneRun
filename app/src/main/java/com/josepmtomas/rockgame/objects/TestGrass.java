package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.programs.GrassProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

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
 * Created by Josep on 24/09/2014.
 */
public class TestGrass
{
	private static final String TAG = "TestGrass";

	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
	private static final int TANGENT_COMPONENTS = 4;
	private static final int TOTAL_COMPONENTS = POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS;

	private static final int POSITION_OFFSET = 0;
	private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	private static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	private static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;

	private static final int POSITION_BYTE_OFFSET = POSITION_OFFSET * Constants.BYTES_PER_INT;
	private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * Constants.BYTES_PER_INT;
	private static final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * Constants.BYTES_PER_INT;
	private static final int TANGENT_BYTE_OFFSET = TANGENT_OFFSET * Constants.BYTES_PER_INT;

	private static final int BYTE_STRIDE = TOTAL_COMPONENTS * Constants.BYTES_PER_INT;

	// Geometry definition
	private int numVertices;
	private int numElements;
	private float[] vertices;
	private short[] elements;
	private FloatBuffer verticesBuffer;
	private ShortBuffer elementsBuffer;
	private int[] vboHandles;
	private int[] vaoHandle;

	private int numInstances;

	// Matrices
	private float[] viewProjection;
	private int[] modelMatricesUbo;

	// Shaders
	private GrassProgram grassProgram;

	// Textures
	private int grassTextureId;


	public TestGrass(Context context, float[] modelMatrices)
	{
		vboHandles = new int[2];
		vaoHandle = new int[1];
		modelMatricesUbo = new int[1];

		load(context, "models/test_grass.vbm");
		initialize(modelMatrices);

		grassProgram = new GrassProgram(context);

		String[] testGrassTexture = {
				"textures/test_grass/test_grass_mip_0.mp3",
				"textures/test_grass/test_grass_mip_1.mp3",
				"textures/test_grass/test_grass_mip_2.mp3",
				"textures/test_grass/test_grass_mip_3.mp3",
				"textures/test_grass/test_grass_mip_4.mp3",
				"textures/test_grass/test_grass_mip_5.mp3",
				"textures/test_grass/test_grass_mip_6.mp3",
				"textures/test_grass/test_grass_mip_7.mp3"
		};

		Log.e(TAG, "Loading grass texture");
		grassTextureId = TextureHelper.loadETC2Texture(context, testGrassTexture, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}


	private void load(Context context, String fileName)
	{
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
	}


	private void initialize(float[] modelMatrices)
	{
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

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);


		// Model matrices
		numInstances = modelMatrices.length / 16;

		FloatBuffer matricesBuffer = ByteBuffer
				.allocateDirect(modelMatrices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(modelMatrices);
		matricesBuffer.position(0);

		glGenBuffers(1, modelMatricesUbo, 0);
		glBindBuffer(GL_UNIFORM_BUFFER, modelMatricesUbo[0]);
		glBufferData(GL_UNIFORM_BUFFER, matricesBuffer.capacity() * Constants.BYTES_PER_FLOAT, matricesBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	public void update(float[] viewProjectionMatrix)
	{
		this.viewProjection = viewProjectionMatrix;
	}


	public void draw()
	{
		/*grassProgram.useProgram();
		grassProgram.setUniforms(viewProjection, grassTextureId);

		glBindBufferRange(GL_UNIFORM_BUFFER, 0, modelMatricesUbo[0], 0, (numInstances)*16*Constants.BYTES_PER_FLOAT);

		glBindVertexArray(vaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, elements.length, GL_UNSIGNED_SHORT, 0, numInstances);*/
	}


	public int getVaoHandle()
	{
		return vaoHandle[0];
	}
}
