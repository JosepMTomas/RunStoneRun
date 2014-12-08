package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.programsForwardPlus.TreeLeavesProgram;
import com.josepmtomas.rockgame.programsForwardPlus.TreeTrunkProgram;
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
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 07/09/2014.
 */
public class TestTree
{
	private static final String TAG = "TestTree";

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



	// Tree trunk geometry definition
	private int trunkNumVertices;
	private int trunkNumElements;
	private float[] trunkVertices;
	private short[] trunkElements;
	private FloatBuffer trunkVerticesBuffer;
	private ShortBuffer trunkElementsBuffer;
	private int[] trunkVboHandles;
	private int[] trunkVaoHandle;

	// Tree leaves geometry definition
	private int leavesNumVertices;
	private int leavesNumElements;
	private float[] leavesVertices;
	private short[] leavesElements;
	private FloatBuffer leavesVerticesBuffer;
	private ShortBuffer leavesElementsBuffer;
	private int[] leavesVboHandles;
	private int[] leavesVaoHandle;

	// Matrices
	private float[] model = new float[16];
	private float[] viewProjection;
	private float[] modelViewProjection = new float[16];

	// Shaders
	private TreeTrunkProgram trunkProgram;
	private TreeLeavesProgram leavesProgram;

	// Leaves textures
	private int leavesDiffuseTexture;
	private int leavesNormalTexture;


	public TestTree(Context context)
	{
		trunkVboHandles = new int[2];
		trunkVaoHandle = new int[1];

		leavesVboHandles = new int[2];
		leavesVaoHandle = new int[1];

		trunkProgram = new TreeTrunkProgram(context);
		leavesProgram = new TreeLeavesProgram(context);

		String[] testLeavesDiffuseTexture = {
				"textures/test_leaves/test_leaves_mip_0.jpg",
				"textures/test_leaves/test_leaves_mip_1.jpg",
				"textures/test_leaves/test_leaves_mip_2.mp3",
				"textures/test_leaves/test_leaves_mip_3.mp3",
				"textures/test_leaves/test_leaves_mip_4.mp3",
				"textures/test_leaves/test_leaves_mip_5.mp3",
				"textures/test_leaves/test_leaves_mip_6.mp3",
				"textures/test_leaves/test_leaves_mip_7.mp3"};

		String[] testLeavesNormalTexture =  {
				"textures/test_leaves/test_leaves_normal_mip_0.mp3",
				"textures/test_leaves/test_leaves_normal_mip_1.mp3",
				"textures/test_leaves/test_leaves_normal_mip_2.mp3",
				"textures/test_leaves/test_leaves_normal_mip_3.mp3",
				"textures/test_leaves/test_leaves_normal_mip_4.mp3",
				"textures/test_leaves/test_leaves_normal_mip_5.mp3",
				"textures/test_leaves/test_leaves_normal_mip_6.mp3",
				"textures/test_leaves/test_leaves_normal_mip_7.mp3"};

		Log.e("TestTree", "Loading diffuse texture");
		leavesDiffuseTexture = TextureHelper.loadETC2Texture(context, testLeavesDiffuseTexture, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		Log.e("TestTree", "Loading normal texture");
		leavesNormalTexture = TextureHelper.loadETC2Texture(context, testLeavesNormalTexture, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		loadTrunk(context, "models/test_tree_trunk.vbm");
		loadLeaves(context, "models/test_tree_leaves.vbm");

		initializeTrunk();
		initializeLeaves();
	}


	private void loadTrunk(Context context, String fileName)
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
					trunkNumVertices = Integer.parseInt(tokens[1]);
					trunkVertices = new float[TOTAL_COMPONENTS * trunkNumVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					trunkNumElements = Integer.parseInt(tokens[1]);
					trunkElements = new short[3 * trunkNumElements];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					trunkVertices[verticesOffset++] = Float.parseFloat(tokens[12]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					trunkElements[elementsOffset++] = Short.parseShort(tokens[1]);
					trunkElements[elementsOffset++] = Short.parseShort(tokens[2]);
					trunkElements[elementsOffset++] = Short.parseShort(tokens[3]);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private void loadLeaves(Context context, String fileName)
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
					leavesNumVertices = Integer.parseInt(tokens[1]);
					leavesVertices = new float[TOTAL_COMPONENTS * leavesNumVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					leavesNumElements = Integer.parseInt(tokens[1]);
					leavesElements = new short[3 * leavesNumElements];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Read the vertex texture coordinates
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Read the vertex normals
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// read the vertex tangents
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					leavesVertices[verticesOffset++] = Float.parseFloat(tokens[12]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					leavesElements[elementsOffset++] = Short.parseShort(tokens[1]);
					leavesElements[elementsOffset++] = Short.parseShort(tokens[2]);
					leavesElements[elementsOffset++] = Short.parseShort(tokens[3]);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private void initializeTrunk()
	{
		// Build the arrays
		trunkVerticesBuffer = ByteBuffer
				.allocateDirect(trunkVertices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(trunkVertices);
		trunkVerticesBuffer.position(0);

		trunkElementsBuffer = ByteBuffer
				.allocateDirect(trunkElements.length * Constants.BYTES_PER_INT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(trunkElements);
		trunkElementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, trunkVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, trunkVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, trunkVerticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, trunkVerticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, trunkVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, trunkElementsBuffer.capacity() * Constants.BYTES_PER_SHORT, trunkElementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, trunkVaoHandle, 0);
		glBindVertexArray(trunkVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, trunkVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, trunkVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, trunkVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, trunkVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, trunkVboHandles[1]);

		glBindVertexArray(0);
	}


	private void initializeLeaves()
	{
		// Build the arrays
		leavesVerticesBuffer = ByteBuffer
				.allocateDirect(leavesVertices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(leavesVertices);
		leavesVerticesBuffer.position(0);

		leavesElementsBuffer = ByteBuffer
				.allocateDirect(leavesElements.length * Constants.BYTES_PER_INT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(leavesElements);
		leavesElementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, leavesVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, leavesVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, leavesVerticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, leavesVerticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, leavesVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, leavesElementsBuffer.capacity() * Constants.BYTES_PER_SHORT, leavesElementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, leavesVaoHandle, 0);
		glBindVertexArray(leavesVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, leavesVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, leavesVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTE_STRIDE, TEXCOORD_BYTE_OFFSET);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, leavesVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, BYTE_STRIDE, NORMAL_BYTE_OFFSET);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, leavesVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, BYTE_STRIDE, TANGENT_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, leavesVboHandles[1]);

		glBindVertexArray(0);
	}


	public void update(float[] viewProjectionMatrix)
	{
		this.viewProjection = viewProjectionMatrix;

		setIdentityM(model, 0);
		translateM(model, 0, 10f, 0f, 0f);

		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);
	}


	public void drawTrunk()
	{
		/*trunkProgram.useProgram();
		trunkProgram.setUniforms(modelViewProjection);

		glBindVertexArray(trunkVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, trunkElements.length, GL_UNSIGNED_SHORT, 0);*/
	}


	public void drawLeaves()
	{
		/*leavesProgram.useProgram();
		leavesProgram.setUniforms(modelViewProjection, leavesDiffuseTexture, leavesNormalTexture);

		glBindVertexArray(leavesVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, leavesElements.length, GL_UNSIGNED_SHORT, 0);*/
	}

	public void drawAtPosition(float x, float y, float z)
	{
		setIdentityM(model,0);
		translateM(model, 0, x, y, z);

		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		drawTrunk();
		drawLeaves();
	}

	public void drawReflectionProxy()
	{
		/*float[] modelR = new float[16];
		float[] mvpR = new float[16];

		setIdentityM(modelR, 0);
		translateM(modelR, 0, 10f, 0f, 0f);
		rotateM(modelR, 0, 180f, 1f, 0f, 0f);

		multiplyMM(mvpR, 0, viewProjection, 0, modelR, 0);

		trunkProgram.useProgram();
		trunkProgram.setUniforms(mvpR);

		glBindVertexArray(trunkVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, trunkElements.length, GL_UNSIGNED_SHORT, 0);

		leavesProgram.useProgram();
		leavesProgram.setUniforms(mvpR, leavesDiffuseTexture, leavesNormalTexture);

		glBindVertexArray(leavesVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, leavesElements.length, GL_UNSIGNED_SHORT, 0);*/
	}

	public void drawReflectionProxy(float x, float y, float z)
	{
		/*float[] modelR = new float[16];
		float[] mvpR = new float[16];

		setIdentityM(modelR, 0);
		translateM(modelR, 0, x, y, z);
		rotateM(modelR, 0, 180f, 1f, 0f, 0f);

		multiplyMM(mvpR, 0, viewProjection, 0, modelR, 0);

		trunkProgram.useProgram();
		trunkProgram.setUniforms(mvpR);

		glBindVertexArray(trunkVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, trunkElements.length, GL_UNSIGNED_SHORT, 0);

		leavesProgram.useProgram();
		leavesProgram.setUniforms(mvpR, leavesDiffuseTexture, leavesNormalTexture);

		glBindVertexArray(leavesVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, leavesElements.length, GL_UNSIGNED_SHORT, 0);*/
	}
}
