package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;
import android.util.FloatMath;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.data.GroundPatchType;
import com.josepmtomas.rockgame.programs.DeferredGroundShaderProgram;
import com.josepmtomas.rockgame.programs.GroundShaderProgram;
import com.josepmtomas.rockgame.programs.ShadowpassShaderProgram;
import com.josepmtomas.rockgame.util.PerspectiveCamera;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.transposeM;

/**
 * Created by Josep on 08/08/2014.
 */
public class Ground
{
	private final static String TAG = "Ground";

	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
	private static final int TANGENT_COMPONENTS = 4;

	// VBO handle for: positions, texture coordinates, normals, tangents & elements
	private int[] vboHandles = new int[5];

	// VBO & VAO handle for shadow mapping: no patches
	private int[] vboShadowHandles = new int[2];
	private int[] vaoShadowHandle = new int[1];

	private float[] shadowVPMatrix = new float[16];
	private float[] S = new float[16];
	private float[] N = new float[16];
	private float[] M = new float[16];
	private float[] currMV = new float[16];
	private float[] prevMV = new float[16];
	private float[] MV = new float[16];
	private float[] MVP = new float[16];

	// Patch common attributes
	private float[] patchPositions;
	private float[] patchTexCoords;
	private float[] patchNormals;
	private float[] patchTangents;
	private short[] patchElements;

	// Shadow pass attributes
	private float[] shadowPlanePositions;
	private short[] shadowPlaneElements;

	// Visibility pass attributes
	private float[] patchCullingPoints;

	private int numElements;
	private int numPatchesX;
	private int numPatchesZ;
	private int numPolygonsX;
	private int numPolygonsZ;
	private int numVerticesX;
	private int numVerticesZ;
	private float patchWidth;
	private float patchHeight;

	private float minOffsetX;
	private float maxOffsetX;
	//private float minOffsetZ;
	private float maxOffsetZ;

	private int centerLaneIndex;

	private int leftmostIndex;
	private int rightmostIndex;
	private int lowerIndex;
	private int upperIndex;

	// Scene camera
	private PerspectiveCamera camera;

	// Patches
	private GroundPatch[][] groundPatches;
	private int[][] groundVaoHandles;
	private Float maximumVariance;

	// Movement
	private vec3 displacement;

	private GroundShaderProgram groundShaderProgram;
	private int[] groundTextures;

	private DeferredGroundShaderProgram deferredGroundShaderProgram;

	private ShadowpassShaderProgram shadowpassShaderProgram;


	public Ground(Context context, int numPatchesX, int numPatchesZ, int numPolygonsX, int numPolygonsZ, float patchWidth, float patchHeight)
	{
		groundShaderProgram = new GroundShaderProgram(context);
		deferredGroundShaderProgram = new DeferredGroundShaderProgram(context);
		shadowpassShaderProgram = new ShadowpassShaderProgram(context);


		this.numElements = numPolygonsX * numPolygonsZ * 6;
		this.numPatchesX = numPatchesX;
		this.numPatchesZ = numPatchesZ;
		this.numPolygonsX = numPolygonsX;
		this.numPolygonsZ = numPolygonsZ;
		this.numVerticesX = numPolygonsX + 1;
		this.numVerticesZ = numPolygonsZ + 1;
		this.patchWidth = patchWidth;
		this.patchHeight = patchHeight;

		this.maximumVariance = 0.1f;

		centerLaneIndex = (numPatchesX-1)/2;
		//bottomPatchIndex = 0;
		//previousPatchIndex = numPatchesZ-1;

		leftmostIndex = 0;
		rightmostIndex = numPatchesX-1;
		upperIndex = numPatchesZ-1;
		lowerIndex = 0;

		minOffsetX = ((float)centerLaneIndex + 0.5f) * -patchWidth;
		maxOffsetX = ((float)centerLaneIndex + 0.5f) * patchWidth;
		//minOffsetZ = ((float)centerLaneIndex + 0.5f) * -patchHeight;
		maxOffsetZ = ((float)upperIndex/2.0f + 0.5f) * patchHeight;

		String[] grass1Texture = {
				"textures/grass1/grass1_512_mip_0.pkm",
				"textures/grass1/grass1_512_mip_1.pkm",
				"textures/grass1/grass1_512_mip_2.pkm",
				"textures/grass1/grass1_512_mip_3.pkm",
				"textures/grass1/grass1_512_mip_4.pkm",
				"textures/grass1/grass1_512_mip_5.pkm",
				"textures/grass1/grass1_512_mip_6.pkm",
				"textures/grass1/grass1_512_mip_7.pkm",
				"textures/grass1/grass1_512_mip_8.pkm"};

		// Ground textures
		groundTextures = new int[5];
		//groundTextures[0] = TextureHelper.loadTexture(context, R.raw.grass_color);
		//groundTextures[0] = TextureHelper.loadETC2Texture(context,"textures/grass_color_mip_0.pkm", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		groundTextures[0] = TextureHelper.loadETC2Texture(context,grass1Texture, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		groundTextures[1] = TextureHelper.loadTexture(context, R.raw.grass_normal);
		groundTextures[2] = TextureHelper.loadTexture(context, R.raw.ground_color_512);
		groundTextures[3] = TextureHelper.loadTexture(context, R.raw.ground_alpha_512);
		groundTextures[4] = TextureHelper.loadTexture(context, R.raw.ground_normal_512);

		//int compressedTexture = TextureHelper.loadETC2Texture(context,"textures/grass_color_mip_0.pkm", GL_COMPRESSED_RGBA8_ETC2_EAC, false, false);

		// Common attributes arrays (static information)
		patchPositions = new float[numVerticesX * numVerticesZ * POSITION_COMPONENTS];
		patchTexCoords = new float[numVerticesX * numVerticesZ * TEXCOORD_COMPONENTS];
		patchNormals = new float[numVerticesX * numVerticesZ * NORMAL_COMPONENTS];
		patchTangents = new float[numVerticesX * numVerticesZ * TANGENT_COMPONENTS];
		patchElements = new short[numPolygonsX * numPolygonsZ * 6];

		shadowPlanePositions = new float[12];
		shadowPlaneElements = new short[6];

		patchCullingPoints = new float[12];

		// Base patch creation
		this.createBasePatch();
		this.createBaseShadowPlane();
		this.createCullingPoints();

		this.buildBasePatchBuffers();
		this.buildShadowPlaneBuffers();

		this.createGroundPatches();


		//groundPatches[0][0] = new GroundPatch(numVerticesX, numVerticesZ, patchElements.length, vboHandles);
	}


	public void setCamera(PerspectiveCamera camera)
	{
		this.camera = camera;
	}


	private void createBasePatch()
	{
		int index;
		int indexLeft;
		int indexDown;
		int indexLeftDown;

		int positionsOffset = 0;
		int texCoordsOffset = 0;
		int normalsOffset = 0;
		int tangentsOffset = 0;
		int elementsOffset = 0;

		float currentX;
		float currentZ;
		float minimumX = (-patchWidth / 2.0f);
		float minimumZ = (patchHeight / 2.0f);
		float incrementX = patchWidth / (float)numPolygonsX;
		float incrementZ = patchWidth / (float)numPolygonsZ;

		// Create the vertex attributes
		for(int i=0; i < numVerticesZ; i++)
		{
			currentZ = minimumZ - (i * incrementZ);
			for(int j=0; j < numVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				patchPositions[positionsOffset++] = currentX;
				patchPositions[positionsOffset++] = 0.0f;
				patchPositions[positionsOffset++] = currentZ;

				// Vertex texture coordinate (U V)
				// Vertex texture coordinate (U V)
				patchTexCoords[texCoordsOffset++] = (float)j / (float)numPolygonsX;
				patchTexCoords[texCoordsOffset++] = (float)i / (float)numPolygonsZ;

				// Vertex normal (X Y Z)
				patchNormals[normalsOffset++] = 0.0f;
				patchNormals[normalsOffset++] = 1.0f;
				patchNormals[normalsOffset++] = 0.0f;

				// Vertex tangent (X Y Z W)
				patchTangents[tangentsOffset++] = 1.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 1.0f;
			}
		}

		// Create patch elements
		for(int i=1; i < numVerticesZ; i++)
		{
			for (int j=1; j < numVerticesX; j++)
			{
				index = j + (i * numVerticesX);
				indexLeft = (j-1) + (i * numVerticesX);
				indexDown = j + ((i-1) * numVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numVerticesX);

				// First face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeftDown;
				patchElements[elementsOffset++] = (short)indexDown;

				// Second face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeft;
				patchElements[elementsOffset++] = (short)indexLeftDown;
			}
		}
	}


	private void createBaseShadowPlane()
	{
		float planeWidth = numPatchesX * patchWidth;
		float planeHeight = numPatchesZ * patchHeight;

		float bottom = planeHeight / 2.0f;
		float top = -bottom;
		float right = planeWidth / 2.0f;
		float left = -right;
		float height = -0.5f;

		// D - C
		// | \ |
		// A - B

		// A = 0
		shadowPlanePositions[0] = left;
		shadowPlanePositions[1] = height;
		shadowPlanePositions[2] = bottom;

		// B = 1
		shadowPlanePositions[3] = right;
		shadowPlanePositions[4] = height;
		shadowPlanePositions[5] = bottom;

		// C = 2
		shadowPlanePositions[6] = right;
		shadowPlanePositions[7] = height;
		shadowPlanePositions[8] = top;

		// D = 3
		shadowPlanePositions[9] = left;
		shadowPlanePositions[10] = height;
		shadowPlanePositions[11] = top;

		// First polygon
		shadowPlaneElements[0] = 0;
		shadowPlaneElements[1] = 1;
		shadowPlaneElements[2] = 3;

		// Second polygon
		shadowPlaneElements[3] = 1;
		shadowPlaneElements[4] = 2;
		shadowPlaneElements[5] = 3;
	}


	private void createCullingPoints()
	{
		float right = patchWidth / 2.0f;
		float left = -right;
		float bottom = patchHeight / 2.0f;
		float top = -bottom;

		// A
		patchCullingPoints[0] = left;
		patchCullingPoints[1] = 0.0f;
		patchCullingPoints[2] = bottom;

		// B
		patchCullingPoints[3] = right;
		patchCullingPoints[4] = 0.0f;
		patchCullingPoints[5] = bottom;

		// C
		patchCullingPoints[6] = right;
		patchCullingPoints[7] = 0.0f;
		patchCullingPoints[8] = top;

		// D
		patchCullingPoints[9] = left;
		patchCullingPoints[10] = 0.0f;
		patchCullingPoints[11] = top;
	}


	private void buildBasePatchBuffers()
	{
		FloatBuffer positionsBuffer;
		FloatBuffer texCoordsBuffer;
		FloatBuffer normalsBuffer;
		FloatBuffer tangentsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(patchPositions.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchPositions);
		positionsBuffer.position(0);

		texCoordsBuffer = ByteBuffer
				.allocateDirect(patchTexCoords.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTexCoords);
		texCoordsBuffer.position(0);

		normalsBuffer = ByteBuffer
				.allocateDirect(patchNormals.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchNormals);
		normalsBuffer.position(0);

		tangentsBuffer = ByteBuffer
				.allocateDirect(patchTangents.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTangents);
		tangentsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(patchElements.length * Constants.BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(patchElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(5, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * Constants.BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * Constants.BYTES_PER_FLOAT, texCoordsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[2]);
		glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * Constants.BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[3]);
		glBufferData(GL_ARRAY_BUFFER, tangentsBuffer.capacity() * Constants.BYTES_PER_FLOAT, tangentsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[4]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * Constants.BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	private void buildShadowPlaneBuffers()
	{
		FloatBuffer positionsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(shadowPlanePositions.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(shadowPlanePositions);
		positionsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(shadowPlaneElements.length * Constants.BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(shadowPlaneElements);
		elementsBuffer.position(0);

		/******************************************************************************************/

		glGenBuffers(2, vboShadowHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboShadowHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * Constants.BYTES_PER_FLOAT,  positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboShadowHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * Constants.BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		/******************************************************************************************/

		glGenVertexArrays(1, vaoShadowHandle, 0);
		glBindVertexArray(vaoShadowHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboShadowHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboShadowHandles[1]);

		glBindVertexArray(0);
	}


	private void createGroundPatches()
	{
		float minimumX = -(float)((numPatchesX-1)/2) * patchWidth;
		float minimumZ = (float)((numPatchesZ-1)/2) * patchHeight;
		//float minimumZ = ((float)numPatchesZ-1.0f) * patchHeight;

		// Patches
		groundPatches = new GroundPatch[numPatchesX][numPatchesZ];
		groundVaoHandles = new int[numPatchesX][numPatchesZ];

		// FIRST LANE
		groundPatches[0][0] = new GroundPatch(numVerticesX, numVerticesZ, maximumVariance, vboHandles, patchCullingPoints);
		groundPatches[0][0].initialize(GroundPatchType.GROUND_ROOT, null, null);
		groundPatches[0][0].setCurrentPosition(minimumX, 0.0f, minimumZ);

		for(int z=1; z < numPatchesZ; z++)
		{
			groundPatches[0][z] = new GroundPatch(numVerticesX, numVerticesZ, maximumVariance, vboHandles, patchCullingPoints);
			groundPatches[0][z].initialize(
					GroundPatchType.GROUND_UP,
					groundPatches[0][z-1].getVertexColors(GroundPatchType.GROUND_UP),
					null);
			groundPatches[0][z].setCurrentPosition(minimumX, 0.0f, minimumZ - (float)z*patchHeight);
		}

		// NEXT LANES
		for(int x=1; x < numPatchesX; x++)
		{
			groundPatches[x][0] = new GroundPatch(numVerticesX, numVerticesZ, maximumVariance, vboHandles, patchCullingPoints);
			groundPatches[x][0].initialize(
					GroundPatchType.GROUND_LEFT,
					null,
					groundPatches[x-1][0].getVertexColors(GroundPatchType.GROUND_RIGHT));
			groundPatches[x][0].setCurrentPosition(minimumX + x*patchWidth, 0.0f, minimumZ);

			for(int z=1; z < numPatchesZ; z++)
			{
				groundPatches[x][z] = new GroundPatch(numVerticesX, numVerticesZ, maximumVariance, vboHandles, patchCullingPoints);
				groundPatches[x][z].initialize(
						GroundPatchType.GROUND_UP_LEFT,
						groundPatches[x][z-1].getVertexColors(GroundPatchType.GROUND_UP),
						groundPatches[x-1][z].getVertexColors(GroundPatchType.GROUND_RIGHT));
				groundPatches[x][z].setCurrentPosition(minimumX + x*patchWidth, 0.0f, minimumZ - z*patchHeight);
			}
		}

		// Get the vertex array objects created by each patch
		for(int x=0; x < numPatchesX; x++)
		{
			for(int z=0; z < numPatchesZ; z++)
			{
				groundVaoHandles[x][z] = groundPatches[x][z].getVaoHandle();
			}
		}
	}


	public void update(float[] viewMatrix, vec3 displacement, float time)
	{
		//float sideDisplacement = FloatMath.sin(time) * 2.0f;
		//vec3 displacement = new vec3(sideDisplacement, 0.0f, 2f);
		//vec3 displacement = new vec3(0.0f);

		this.displacement = displacement;

		for(int x=0; x < numPatchesX; x++)
		{
			for(int z=0; z < numPatchesZ; z++)
			{
				groundPatches[x][z].update(viewMatrix, time, displacement);
			}
		}

		// Check horizontal offset
		if(groundPatches[rightmostIndex][lowerIndex].getCurrentPosition().x > maxOffsetX)
		{
			newLeftPatch();
		}
		else if(groundPatches[leftmostIndex][lowerIndex].getCurrentPosition().x < minOffsetX)
		{
			newRightPatch();
		}

		if(groundPatches[0][lowerIndex].getCurrentPosition().z > maxOffsetZ)
		{
			newUpPatch();
		}
	}


	public void drawShadowMap(float[] view, float[] projection)
	{
		multiplyMM(shadowVPMatrix, 0, projection, 0, view, 0);

		shadowpassShaderProgram.useProgram();
		shadowpassShaderProgram.setUniforms(shadowVPMatrix);

		glBindVertexArray(vaoShadowHandle[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
	}

	public void drawDeferred(float[] viewProjectionMatrix, float[] shadowMatrix, int shadowSampler)
	{
		deferredGroundShaderProgram.useProgram();

		for(int i=0; i < numPatchesX; i++)
		{
			for (int j = 0; j < numPatchesZ; j++)
			{
				M = groundPatches[i][j].getCurrentModelMatrix();
				currMV = groundPatches[i][j].getCurrentModelViewMatrix();
				prevMV = groundPatches[i][j].getPreviousModelViewMatrix();
				multiplyMM(MVP, 0, viewProjectionMatrix, 0, M, 0);
				deferredGroundShaderProgram.setUniforms(M, currMV, prevMV, MVP, shadowMatrix, groundTextures, shadowSampler);

				glBindVertexArray(groundVaoHandles[i][j]);
				glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);
			}
		}
	}

	public void draw(float[] view, float[] projection, float[] shadow, int shadowMapTexID, float time)
	{
		//setIdentityM(M, 0);
		//scaleM(M, 0, 4f, 1f, 4f);
		invertM(N, 0, view, 0);
		//transposeM(N, 0, S, 0);

		S = shadow;

		multiplyMM(MV, 0, view, 0, M, 0);
		multiplyMM(MVP, 0, projection, 0, MV, 0);

		groundShaderProgram.useProgram();
		//groundShaderProgram.setUniforms(time, MVP, MV, M, N, S, groundTextures);
		groundShaderProgram.setShadowSampler(shadowMapTexID);

		/*groundShaderProgram.setModelmatrix(groundPatches[2][2].getModelMatrix());
		glBindVertexArray(groundVaoHandles[2][2]);
		glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);*/

		int drawn = 0;
		int total = 0;

		for(int i=0; i < numPatchesX; i++)
		{
			for (int j = 0; j < numPatchesZ; j++)
			{
				/*M = groundPatches[i][j].getModelMatrix();
				multiplyMM(MV, 0, view, 0, M, 0);
				multiplyMM(MVP, 0, projection, 0, MV, 0);
				groundShaderProgram.setUniforms(time, MVP, MV, M, N, S, groundTextures);

				glBindVertexArray(groundVaoHandles[i][j]);
				glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);*/
				if(groundPatches[i][j].isVisible(camera))
				{
					M = groundPatches[i][j].getCurrentModelMatrix();
					multiplyMM(MV, 0, view, 0, M, 0);
					multiplyMM(MVP, 0, projection, 0, MV, 0);
					groundShaderProgram.setUniforms(time, MVP, MV, M, N, S, groundTextures);

					glBindVertexArray(groundVaoHandles[i][j]);
					glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);

					drawn++;
					total++;
				}
				else
				{
					total++;
				}
			}
		}

		//Log.e(TAG, "Patches drawn " + drawn + " of " + total);

		/**for(int i=0; i < numPatchesX; i++)
		{
			for(int j=0; j < numPatchesZ; j++)
			{
				groundShaderProgram.setModelmatrix(groundPatches[i][j].getModelMatrix());
				glBindVertexArray(groundVaoHandles[i][j]);
				glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);
			}
		}**/
		//glBindVertexArray(groundVaoHandles[0][0]);
		//glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_INT, 0);
	}


	private void newUpPatch()
	{
		Random random = new Random();
		float randomValue = random.nextFloat();
		randomValue = randomValue * 2.0f - 1.0f;

		//maximumVariance = maximumVariance + (randomValue * 0.05f);
		maximumVariance = Math.min(Math.max(maximumVariance + (randomValue * 0.05f),0.0f),0.5f);
		//Log.d(TAG, "MaximumVariance = " + maximumVariance);

		/*********************/

		int current = leftmostIndex;
		int previous = rightmostIndex;

		groundPatches[current][lowerIndex].setCurrentPosition(
				operations.subtract(groundPatches[current][upperIndex].getCurrentPosition(), new vec3(0f, 0f, patchHeight))
		);
		groundPatches[current][lowerIndex].reinitialize(
				GroundPatchType.GROUND_UP,
				groundPatches[current][upperIndex].getVertexColors(GroundPatchType.GROUND_UP),
				null
		);

		for(int x=1; x < numPatchesX; x++)
		{
			current = (current + 1) % numPatchesX;
			previous = (previous + 1) % numPatchesX;

			groundPatches[current][lowerIndex].setCurrentPosition(
					operations.subtract(groundPatches[current][upperIndex].getCurrentPosition(), new vec3(0f,0f,patchHeight))
			);
			groundPatches[current][lowerIndex].reinitialize(
					GroundPatchType.GROUND_UP_LEFT,
					groundPatches[current][upperIndex].getVertexColors(GroundPatchType.GROUND_UP),
					groundPatches[previous][lowerIndex].getVertexColors(GroundPatchType.GROUND_RIGHT)
			);
		}

		lowerIndex++;
		lowerIndex = lowerIndex % numPatchesZ;

		upperIndex++;
		upperIndex = upperIndex % numPatchesZ;
	}


	private void newLeftPatch()
	{
		int current = lowerIndex;
		int previous = upperIndex;

		groundPatches[rightmostIndex][current].setCurrentPosition(
				operations.subtract(groundPatches[leftmostIndex][current].getCurrentPosition(), new vec3(patchWidth, 0f, 0f))
		);
		groundPatches[rightmostIndex][current].reinitialize(
				GroundPatchType.GROUND_RIGHT,
				null,
				groundPatches[leftmostIndex][current].getVertexColors(GroundPatchType.GROUND_LEFT)
		);

		for(int z=1; z < numPatchesZ; z++)
		{
			current = (current + 1) % numPatchesZ;
			previous = (previous + 1) % numPatchesZ;

			groundPatches[rightmostIndex][current].setCurrentPosition(
					operations.subtract(groundPatches[leftmostIndex][current].getCurrentPosition(), new vec3(patchWidth, 0f, 0f))
			);
			groundPatches[rightmostIndex][current].reinitialize(
					GroundPatchType.GROUND_UP_RIGHT,
					groundPatches[rightmostIndex][previous].getVertexColors(GroundPatchType.GROUND_UP),
					groundPatches[leftmostIndex][current].getVertexColors(GroundPatchType.GROUND_LEFT)
			);
		}

		rightmostIndex--;
		if(rightmostIndex < 0) rightmostIndex = numPatchesX-1;

		leftmostIndex--;
		if(leftmostIndex < 0) leftmostIndex = numPatchesX-1;
	}


	private void newRightPatch()
	{
		int current = lowerIndex;
		int previous = upperIndex;

		groundPatches[leftmostIndex][current].setCurrentPosition(
				operations.add(groundPatches[rightmostIndex][current].getCurrentPosition(), new vec3(patchWidth, 0f, 0f))
		);
		groundPatches[leftmostIndex][current].reinitialize(
				GroundPatchType.GROUND_LEFT,
				null,
				groundPatches[rightmostIndex][current].getVertexColors(GroundPatchType.GROUND_RIGHT)
		);

		for(int z=1; z < numPatchesZ; z++)
		{
			current = (current + 1) % numPatchesZ;
			previous = (previous + 1) % numPatchesZ;

			groundPatches[leftmostIndex][current].setCurrentPosition(
					operations.add(groundPatches[rightmostIndex][current].getCurrentPosition(), new vec3(patchWidth, 0f, 0f))
			);
			groundPatches[leftmostIndex][current].reinitialize(
					GroundPatchType.GROUND_UP_LEFT,
					groundPatches[leftmostIndex][previous].getVertexColors(GroundPatchType.GROUND_UP),
					groundPatches[rightmostIndex][current].getVertexColors(GroundPatchType.GROUND_RIGHT)
			);
		}

		rightmostIndex++;
		rightmostIndex = rightmostIndex % (numPatchesX);

		leftmostIndex++;
		leftmostIndex = leftmostIndex % (numPatchesX);
	}
}
