package com.josepmtomas.runstonerun.objects;

import android.util.FloatMath;

import com.josepmtomas.runstonerun.algebra.vec2;
import com.josepmtomas.runstonerun.algebra.vec3;
import com.josepmtomas.runstonerun.poissonGeneration.BoundarySampler;
import com.josepmtomas.runstonerun.util.PerspectiveCamera;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import static com.josepmtomas.runstonerun.algebra.operations.*;
import static com.josepmtomas.runstonerun.Constants.*;

/**
 * Created by Josep on 06/09/2014.
 * @author Josep
 */

public class GroundPatch extends BoundarySampler
{
	private static final int COLOR_COMPONENTS = 3;

	public int type;

	// LOD
	public int currentLOD = LOD_A;

	// Frustum culling
	public boolean visible = false;
	private PerspectiveCamera perspectiveCamera;
	private float[] initialCullingPoints;
	private float[] currentCullingPoints = new float[15];

	// Ground VBO & VAO handles
	private final int[] groundVboHandles;
	private final int[] groundColorVboHandle = new int[1];
	public final int[] groundVaoHandle = new int[1];

	// River Entry & River Exit VBO & VAO handles
	private int[] riverEntryVboHandles;
	private int[] riverExitVboHandles;
	private int[] riverExitReflectionVboHandles;
	private int[] riverEntryColorVboHandle = new int[1];
	private int[] riverExitColorVboHandle = new int[1];
	public int[] riverEntryVaoHandle = new int[1];
	public int[] riverExitVaoHandle = new int[1];
	public int[] riverExitReflectionVaoHandle = new int[1];

	private int[] depthPrePassVaoHandle = new int[1];

	// Ground colors
	private float[] groundColors;
	private float[] groundColorsUp;
	private float[] groundColorsDown;
	private float[] groundColorsLeft;
	private float[] groundColorsRight;
	private float[] groundColorGrid;

	// River entry colors
	private float[] riverEntryColors;
	private float[] riverEntryColorsLeft;
	private float[] riverEntryColorsRight;

	// River exit colors
	private float[] riverExitColors;
	private float[] riverExitColorsUp;
	private float[] riverExitColorsLeft;
	private float[] riverExitColorsRight;

	// Colors buffers
	private FloatBuffer groundColorsBuffer;
	private FloatBuffer riverEntryColorsBuffer;
	private FloatBuffer riverExitColorsBuffer;
	private Float maximumVariance;

	private final float spreadFactorX;
	private final float spreadFactorZ;

	private final int numVerticesX;
	private final int numVerticesZ;
	private final int numPolygonsX;
	private final int numPolygonsZ;

	private vec3 currentPosition = new vec3(0.0f);

	// Matrices
	private float[] model = new float[16];
	private float[] modelViewProjection = new float[16];

	// GRASS
	public int indexNum;

	// Grass filtered points
	public ArrayList<vec2> currentPoints = new ArrayList<vec2>();
	private float[] grassPointsScale = new float[64];
	// 64 grass points maximum per LOD level &
	// 4 floats per point: positionXZ(2), scale(1) & distanceNormalized(1)
	public float[] grassPointsLODA = new float[256];
	public float[] grassPointsLODB = new float[256];
	public float[] grassPointsLODC = new float[256];
	public int[] numGrassPointsPerLOD = new int[3];
	//public final int[] grassNumElementsLOD = {18, 12, 6};



	public GroundPatch(
			int indexNum,
			int numVerticesX, int numVerticesZ,
			float patchWidth, float patchHeight,
			Float maximumVariance,
			int[] groundVboHandles,
			float[] initialCullingPoints,
			PerspectiveCamera perspectiveCamera)
	{
		//super(0.17f);
		super(0.195f);

		this.type = GROUND_PATCH_GROUND;
		this.perspectiveCamera = perspectiveCamera;
		this.indexNum = indexNum;

		this.spreadFactorX = patchWidth / 2f;
		this.spreadFactorZ = patchHeight / 2f;
		this.numVerticesX = numVerticesX;
		this.numVerticesZ = numVerticesZ;
		this.maximumVariance = maximumVariance;
		this.groundVboHandles = groundVboHandles;
		this.initialCullingPoints = initialCullingPoints;

		numPolygonsX = numVerticesX - 1;
		numPolygonsZ = numVerticesZ - 1;

		setIdentityM(model, 0);

		groundColors = new float[numVerticesX * numVerticesZ * COLOR_COMPONENTS];

		groundColorsUp = new float[numVerticesX * COLOR_COMPONENTS];
		groundColorsDown = new float[numVerticesX * COLOR_COMPONENTS];
		groundColorsLeft = new float[numVerticesZ * COLOR_COMPONENTS];
		groundColorsRight = new float[numVerticesZ * COLOR_COMPONENTS];
		groundColorGrid = new float[numPolygonsX * numPolygonsZ * COLOR_COMPONENTS];
	}


	public void setRiverVboHandles(int[] riverEntryVboHandles, int[] riverExitVboHandles, int[] riverExitReflectionVboHandles)
	{
		this.riverEntryVboHandles = riverEntryVboHandles;
		this.riverExitVboHandles = riverExitVboHandles;
		this.riverExitReflectionVboHandles = riverExitReflectionVboHandles;
	}


	public void initialize(int patchColorType, float[] downColors, float[] sideColors)
	{
		// Generate the base vertex colors for each type of ground patch
		generateGroundVertexColors(patchColorType, downColors, sideColors);
		initializeRiverVertexColors();

		// Create the buffers for the color vertices (ground, entry & exit)
		createColorsBuffers();

		// Create the VAOs for each type of ground patch
		createGroundVertexArrayObject();
		createRiverEntryVertexArrayObject();
		createRiverExitVertexArrayObject();
		createRiverExitReflectionVertexArrayObject();

		// Boundary sampler
		complete();
		filterGrassPoints();

		// new grass
		initializeGrassPointsArray();
	}


	private void initializeRiverVertexColors()
	{
		int arrayLength = numVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * COLOR_COMPONENTS;
		int position;
		riverEntryColors = new float[arrayLength];
		riverExitColors = new float[arrayLength];

		/*for(int i=0; i < arrayLength; i++)
		{
			riverEntryColors[i] = 0f;
			riverExitColors[i] = 0f;
		}*/

		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=0; x < numVerticesX; x++)
			{
				position = (z * numVerticesX * COLOR_COMPONENTS) + (x * COLOR_COMPONENTS);

				riverEntryColors[position] = 0f;
				riverEntryColors[position+1] = (float)z / (float)GROUND_RIVER_PATCH_POLYGONS_Z;
				riverEntryColors[position+2] = 0f;

				riverExitColors[position] = 0f;
				riverExitColors[position+1] = (float)(GROUND_RIVER_PATCH_POLYGONS_Z - z) / (float)GROUND_RIVER_PATCH_POLYGONS_Z;
				riverExitColors[position+2] = 0f;
			}
		}

		arrayLength = GROUND_RIVER_PATCH_VERTICES_Z * COLOR_COMPONENTS;
		riverEntryColorsLeft = new float[arrayLength];
		riverEntryColorsRight = new float[arrayLength];
		riverExitColorsLeft = new float[arrayLength];
		riverExitColorsRight = new float[arrayLength];

		for(int i=0; i < arrayLength; i++)
		{
			riverEntryColorsLeft[i] = 0f;
			riverEntryColorsRight[i] = 0f;
			riverExitColorsLeft[i] = 0f;
			riverExitColorsRight[i] = 0f;
		}

		arrayLength = numVerticesX * COLOR_COMPONENTS;
		riverExitColorsUp = new float[arrayLength];

		for(int i=0; i < arrayLength; i++)
		{
			riverExitColorsUp[i] = 0f;
		}
	}


	public void reinitialize(int patchColorsType, float[] downColors, float[] sideColors)
	{
		switch(type)
		{
			case GROUND_PATCH_GROUND:
				generateGroundVertexColors(patchColorsType, downColors, sideColors);
				updateGroundColorsBuffer();
				updateGroundVertexArrayObject();
				filterGrassPoints();
				break;

			case GROUND_PATCH_RIVER_ENTRY:
				generateRiverEntryVertexColors(patchColorsType, downColors, sideColors);
				updateRiverEntryColorsBuffer();
				updateRiverEntryVertexArrayObject();
				break;

			case GROUND_PATCH_RIVER_EXIT:
				generateRiverExitVertexColors(patchColorsType, sideColors);
				updateRiverExitColorsBuffer();
				updateRiverExitVertexArrayObject();
				break;
		}
	}


	private void createColorsBuffers()
	{
		groundColorsBuffer = ByteBuffer
				.allocateDirect(groundColors.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(groundColors);
		groundColorsBuffer.position(0);

		riverEntryColorsBuffer = ByteBuffer
				.allocateDirect(riverEntryColors.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(riverEntryColors);
		riverEntryColorsBuffer.position(0);

		riverExitColorsBuffer = ByteBuffer
				.allocateDirect(riverExitColors.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(riverExitColors);
		riverExitColorsBuffer.position(0);

		glGenBuffers(1, groundColorVboHandle, 0);
		glBindBuffer(GL_ARRAY_BUFFER, groundColorVboHandle[0]);
		glBufferData(GL_ARRAY_BUFFER, groundColorsBuffer.capacity() * BYTES_PER_FLOAT, groundColorsBuffer, GL_DYNAMIC_DRAW);

		glGenBuffers(1, riverEntryColorVboHandle, 0);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryColorVboHandle[0]);
		glBufferData(GL_ARRAY_BUFFER, riverEntryColorsBuffer.capacity() * BYTES_PER_FLOAT, riverEntryColorsBuffer, GL_DYNAMIC_DRAW);

		glGenBuffers(1, riverExitColorVboHandle, 0);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitColorVboHandle[0]);
		glBufferData(GL_ARRAY_BUFFER, riverExitColorsBuffer.capacity() * BYTES_PER_FLOAT, riverExitColorsBuffer, GL_DYNAMIC_DRAW);
	}


	private void updateGroundColorsBuffer()
	{
		groundColorsBuffer.position(0);
		groundColorsBuffer.put(groundColors);
		groundColorsBuffer.position(0);

		glBindBuffer(GL_ARRAY_BUFFER, groundColorVboHandle[0]);
		glBufferSubData(GL_ARRAY_BUFFER, 0, groundColorsBuffer.capacity() * BYTES_PER_FLOAT, groundColorsBuffer);
	}


	private void updateRiverEntryColorsBuffer()
	{
		riverEntryColorsBuffer.put(riverEntryColors);
		riverEntryColorsBuffer.position(0);

		glBindBuffer(GL_ARRAY_BUFFER, riverEntryColorVboHandle[0]);
		glBufferSubData(GL_ARRAY_BUFFER, 0, riverEntryColorsBuffer.capacity() * BYTES_PER_FLOAT, riverEntryColorsBuffer);
	}


	private void updateRiverExitColorsBuffer()
	{
		riverExitColorsBuffer.put(riverExitColors);
		riverExitColorsBuffer.position(0);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitColorVboHandle[0]);
		glBufferSubData(GL_ARRAY_BUFFER, 0, riverExitColorsBuffer.capacity() * BYTES_PER_FLOAT, riverExitColorsBuffer);
	}


	private void createGroundVertexArrayObject()
	{
		glGenVertexArrays(1, groundVaoHandle, 0);
		glBindVertexArray(groundVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, groundVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, groundVboHandles[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, groundVboHandles[2]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, groundVboHandles[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, groundColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, groundVboHandles[4]);

		glBindVertexArray(0);

		/******************************************************************************************/

		glGenVertexArrays(1, depthPrePassVaoHandle, 0);
		glBindVertexArray(depthPrePassVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, groundVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, groundVboHandles[4]);

		glBindVertexArray(0);
	}


	private void createRiverEntryVertexArrayObject()
	{
		glGenVertexArrays(1, riverEntryVaoHandle, 0);
		glBindVertexArray(riverEntryVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[2]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverEntryVboHandles[4]);

		glBindVertexArray(0);
	}


	private void createRiverExitVertexArrayObject()
	{
		glGenVertexArrays(1, riverExitVaoHandle, 0);
		glBindVertexArray(riverExitVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[2]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverExitVboHandles[4]);

		glBindVertexArray(0);
	}


	private void createRiverExitReflectionVertexArrayObject()
	{
		glGenVertexArrays(1, riverExitReflectionVaoHandle, 0);
		glBindVertexArray(riverExitReflectionVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitReflectionVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitReflectionVboHandles[1]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverExitVboHandles[4]);

		glBindVertexArray(0);
	}

	private void updateGroundVertexArrayObject()
	{
		glBindVertexArray(groundVaoHandle[0]);
		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, groundColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		glBindVertexArray(0);
	}


	private void updateRiverEntryVertexArrayObject()
	{
		glBindVertexArray(riverEntryVaoHandle[0]);
		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, riverEntryColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		glBindVertexArray(0);
	}


	private void updateRiverExitVertexArrayObject()
	{
		glBindVertexArray(riverExitVaoHandle[0]);
		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, riverExitColorVboHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		glBindVertexArray(0);
	}


	private void generateGroundVertexColors(int patchColors, float[] downColors, float[] sideColors)
	{
		switch(patchColors)
		{
			case GROUND_PATCH_ROOT:
				generateGroundVertexColorsRoot();
				break;
			case GROUND_PATCH_UP:
				generateGroundVertexColorsUp(downColors);
				break;
			case GROUND_PATCH_LEFT:
				generateGroundVertexColorsLeft(sideColors);
				break;
			case GROUND_PATCH_RIGHT:
				generateGroundVertexColorsRight(sideColors);
				break;
			case GROUND_PATCH_UP_LEFT:
				generateGroundVertexColorsUpLeft(downColors, sideColors);
				break;
			case GROUND_PATCH_UP_RIGHT:
				generateGroundVertexColorsUpRight(downColors, sideColors);
				break;
			default:
				break;
		}

		populateGroundColorsLists();
		updateColorGrid();
	}


	private void generateGroundVertexColorsRoot()
	{
		Random random = new Random();
		float randomValue;

		int current;
		int previousDown;
		int previousLeft;

		// Paint the first vertex (0,0)
		groundColors[0] = random.nextFloat();
		groundColors[1] = 0.0f;
		groundColors[2] = 0.0f;

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;
			previousLeft = (x-1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			groundColors[current] = Math.min(Math.max(groundColors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			groundColors[current+1] = 0.0f;
			groundColors[current+2] = 0.0f;
		}

		// Paint the first column (0,z)
		for(int z=1; z < numVerticesZ; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			groundColors[current] = Math.min(Math.max(groundColors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			groundColors[current+1] = 0.0f;
			groundColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousLeft]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}


	private void generateGroundVertexColorsUp(float[] downColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first row
		for(int x=0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			groundColors[current] = downColors[offset++];
			groundColors[current+1] = downColors[offset++];
			groundColors[current+2] = downColors[offset++];
		}

		// Paint the first column (0,z)
		for(int z=1; z < numVerticesZ; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			groundColors[current] = Math.min(Math.max(groundColors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			groundColors[current+1] = 0.0f;
			groundColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousLeft]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}


	private void generateGroundVertexColorsLeft(float[] leftColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first column
		for(int z=0; z < numVerticesZ; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;

			groundColors[current] = leftColors[offset++];
			groundColors[current+1] = leftColors[offset++];
			groundColors[current+2] = leftColors[offset++];
		}

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;
			previousLeft = (x-1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			groundColors[current] = Math.min(Math.max(groundColors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			groundColors[current+1] = 0.0f;
			groundColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousLeft]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}

	private void generateGroundVertexColorsRight(float[] rightColors) {
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousRight;

		// Paint the last column
		for (int z = 0; z < numVerticesZ; z++) {
			current = (z * numVerticesX + (numVerticesX-1)) * COLOR_COMPONENTS;

			groundColors[current] = rightColors[offset++];
			groundColors[current + 1] = rightColors[offset++];
			groundColors[current + 2] = rightColors[offset++];
		}

		// Paint the first row (x,0)
		for (int x = numVerticesX-2; x >= 0; x--) {
			current = x * COLOR_COMPONENTS;
			previousRight = (x+1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			groundColors[current] = Math.min(Math.max(groundColors[previousRight] + (randomValue * maximumVariance), 0.0f), 1.0f);
			groundColors[current+1] = 0.0f;
			groundColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=numVerticesX-2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x+1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousRight]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}


	private void generateGroundVertexColorsUpLeft(float[] downColors, float[] leftColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first row
		for(int x=0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			groundColors[current] = downColors[offset++];
			groundColors[current+1] = downColors[offset++];
			groundColors[current+2] = downColors[offset++];
		}

		// Paint the first column
		offset = 3;
		for(int z=1; z < numVerticesZ; z++)
		{
			current = (z * numVerticesX) * COLOR_COMPONENTS;

			groundColors[current] = leftColors[offset++];
			groundColors[current+1] = leftColors[offset++];
			groundColors[current+2] = leftColors[offset++];
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousLeft]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}


	private void generateGroundVertexColorsUpRight(float[] downColors, float[] rightColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousRight;

		// Paint the first row
		for(int x=0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			groundColors[current] = downColors[offset++];
			groundColors[current+1] = downColors[offset++];
			groundColors[current+2] = downColors[offset++];
		}

		// Paint the last column
		offset = 3;
		for (int z = 1; z < numVerticesZ; z++) {
			current = (z * numVerticesX + (numVerticesX-1)) * COLOR_COMPONENTS;

			groundColors[current] = rightColors[offset++];
			groundColors[current+1] = rightColors[offset++];
			groundColors[current+2] = rightColors[offset++];
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=numVerticesX-2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x+1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				groundColors[current] = Math.min(Math.max(((groundColors[previousRight]+ groundColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				groundColors[current+1] = 0.0f;
				groundColors[current+2] = 0.0f;
			}
		}
	}


	private void populateGroundColorsLists()
	{
		int position;
		int offset;

		// Down Colors
		offset = 0;
		for(int x=0; x < numVerticesX; x++)
		{
			position = x*3;

			groundColorsDown[offset++] = groundColors[position];
			groundColorsDown[offset++] = groundColors[position+1];
			groundColorsDown[offset++] = groundColors[position+2];
		}

		// Up Colors
		offset = 0;
		for(int x=0; x < numVerticesX; x++)
		{
			position = ((numVerticesZ-1)*numVerticesX + x)*3;

			groundColorsUp[offset++] = groundColors[position];
			groundColorsUp[offset++] = groundColors[position+1];
			groundColorsUp[offset++] = groundColors[position+2];
		}

		// Left Colors
		offset = 0;
		for(int z=0; z < numVerticesZ; z++)
		{
			position = (z * numVerticesX) * 3;

			groundColorsLeft[offset++] = groundColors[position];
			groundColorsLeft[offset++] = groundColors[position+1];
			groundColorsLeft[offset++] = groundColors[position+2];
		}

		// Right Colors
		offset = 0;
		for(int z=0; z < numVerticesZ; z++)
		{
			position = (z * numVerticesX + (numVerticesX-1)) * 3;

			groundColorsRight[offset++] = groundColors[position];
			groundColorsRight[offset++] = groundColors[position+1];
			groundColorsRight[offset++] = groundColors[position+2];
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// River Entry Color generation
	////////////////////////////////////////////////////////////////////////////////////////////////


	private void generateRiverEntryVertexColors(int patchColorsType, float[] downColors, float[] sideColors)
	{
		switch(patchColorsType)
		{
			case GROUND_PATCH_UP:
				generateRiverEntryVertexColorsUp(downColors);
				break;
			case GROUND_PATCH_UP_LEFT:
				generateRiverEntryVertexColorsUpLeft(downColors, sideColors);
				break;
			case GROUND_PATCH_UP_RIGHT:
				generateRiverEntryVertexColorsUpRight(downColors, sideColors);
				break;
			default:
				break;
		}

		populateRiverEntryColorsLists();
	}


	private void generateRiverEntryVertexColorsUp(float[] downColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first row
		for(int x=0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			riverEntryColors[current] = downColors[offset++];
			riverEntryColors[current+1] = downColors[offset++];
			riverEntryColors[current+2] = downColors[offset++];
		}

		// Paint the first column (0,z)
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			riverEntryColors[current] = Math.min(Math.max(riverEntryColors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			riverEntryColors[current+1] = 0.0f;
			riverEntryColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverEntryColors[current] = Math.min(Math.max(((riverEntryColors[previousLeft]+ riverEntryColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				riverEntryColors[current+1] = 0.0f;
				riverEntryColors[current+2] = 0.0f;
			}
		}
	}


	private void generateRiverEntryVertexColorsUpLeft(float[] downColors, float[] leftColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first row
		for(int x=0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			riverEntryColors[current] = downColors[offset++];
			riverEntryColors[current+1] = downColors[offset++];
			riverEntryColors[current+2] = downColors[offset++];
		}

		// Paint the first column
		offset = 3;
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			current = (z * numVerticesX) * COLOR_COMPONENTS;

			riverEntryColors[current] = leftColors[offset++];
			riverEntryColors[current+1] = leftColors[offset++];
			riverEntryColors[current+2] = leftColors[offset++];
		}

		// Paint the rest of vertices
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverEntryColors[current] = Math.min(Math.max(((riverEntryColors[previousLeft]+ riverEntryColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				riverEntryColors[current+1] = 0.0f;
				riverEntryColors[current+2] = 0.0f;
			}
		}
	}


	private void generateRiverEntryVertexColorsUpRight(float[] downColors, float[] rightColors) {
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousRight;

		// Paint the first row
		for (int x = 0; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;

			riverEntryColors[current] = downColors[offset++];
			riverEntryColors[current + 1] = downColors[offset++];
			riverEntryColors[current + 2] = downColors[offset++];
		}

		// Paint the last column
		offset = 3;
		for (int z = 1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			current = (z * numVerticesX + (numVerticesX - 1)) * COLOR_COMPONENTS;

			riverEntryColors[current] = rightColors[offset++];
			riverEntryColors[current + 1] = rightColors[offset++];
			riverEntryColors[current + 2] = rightColors[offset++];
		}

		// Paint the rest of vertices
		for (int z = 1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for (int x = numVerticesX - 2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z - 1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x + 1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverEntryColors[current] = Math.min(Math.max(((riverEntryColors[previousRight] + riverEntryColors[previousDown]) / 2.0f) + (randomValue * maximumVariance), 0.0f), 1.0f);
				riverEntryColors[current + 1] = 0.0f;
				riverEntryColors[current + 2] = 0.0f;
			}
		}
	}


	private void populateRiverEntryColorsLists()
	{
		int position;
		int offset;

		// Left Colors
		offset = 0;
		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			position = (z * numVerticesX) * 3;

			riverEntryColorsLeft[offset++] = riverEntryColors[position];
			riverEntryColorsLeft[offset++] = riverEntryColors[position+1];
			riverEntryColorsLeft[offset++] = riverEntryColors[position+2];
		}

		// Right Colors
		offset = 0;
		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			position = (z * numVerticesX + (numVerticesX-1)) * 3;

			riverEntryColorsRight[offset++] = riverEntryColors[position];
			riverEntryColorsRight[offset++] = riverEntryColors[position+1];
			riverEntryColorsRight[offset++] = riverEntryColors[position+2];
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// River Exit Color generation
	////////////////////////////////////////////////////////////////////////////////////////////////


	private void generateRiverExitVertexColors(int patchColorsType, float[] sideColors)
	{
		switch(patchColorsType)
		{
			case GROUND_PATCH_ROOT:
				generateRiverExitVertexColorsRoot();
				break;
			case GROUND_PATCH_LEFT:
				generateRiverExitVertexColorsLeft(sideColors);
				break;
			case GROUND_PATCH_RIGHT:
				generateRiverExitVertexColorsRight(sideColors);
				break;
			default:
				break;
		}

		populateRiverExitColorsLists();
	}


	private void generateRiverExitVertexColorsRoot()
	{
		Random random = new Random();
		float randomValue;

		int current;
		int previousDown;
		int previousLeft;

		// Paint the first vertex (0,0)
		riverExitColors[0] = random.nextFloat();
		//riverExitColors[1] = 0.0f;
		riverExitColors[2] = 0.0f;

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;
			previousLeft = (x-1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			riverExitColors[current] = Math.min(Math.max(riverExitColors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			//riverExitColors[current+1] = 0.0f;
			riverExitColors[current+2] = 0.0f;
		}

		// Paint the first column (0,z)
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			riverExitColors[current] = Math.min(Math.max(riverExitColors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			//riverExitColors[current+1] = 0.0f;
			riverExitColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverExitColors[current] = Math.min(Math.max(((riverExitColors[previousLeft]+ riverExitColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				//riverExitColors[current+1] = 0.0f;
				riverExitColors[current+2] = 0.0f;
			}
		}
	}


	private void generateRiverExitVertexColorsLeft(float[] leftColors)
	{
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousLeft;

		// Paint the first column
		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			current = z * numVerticesX * COLOR_COMPONENTS;

			riverExitColors[current] = leftColors[offset++];
			/**riverExitColors[current+1] = leftColors[offset++];**/offset++;
			riverExitColors[current+2] = leftColors[offset++];
		}

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * COLOR_COMPONENTS;
			previousLeft = (x-1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			riverExitColors[current] = Math.min(Math.max(riverExitColors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			/**riverExitColors[current+1] = 0.0f;**/
			riverExitColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverExitColors[current] = Math.min(Math.max(((riverExitColors[previousLeft]+ riverExitColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				/**riverExitColors[current+1] = 0.0f;**/
				riverExitColors[current+2] = 0.0f;
			}
		}
	}

	private void generateRiverExitVertexColorsRight(float[] rightColors) {
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousRight;

		// Paint the last column
		for (int z = 0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++) {
			current = (z * numVerticesX + (numVerticesX-1)) * COLOR_COMPONENTS;

			riverExitColors[current] = rightColors[offset++];
			//riverExitColors[current + 1] = rightColors[offset++];
			riverExitColors[current + 2] = rightColors[offset++];
		}

		// Paint the first row (x,0)
		for (int x = numVerticesX-2; x >= 0; x--) {
			current = x * COLOR_COMPONENTS;
			previousRight = (x+1) * COLOR_COMPONENTS;

			// Get a random value between [0,1] and transform it into a random value between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			riverExitColors[current] = Math.min(Math.max(riverExitColors[previousRight] + (randomValue * maximumVariance), 0.0f), 1.0f);
			//riverExitColors[current+1] = 0.0f;
			riverExitColors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			for(int x=numVerticesX-2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x+1)) * COLOR_COMPONENTS;

				// Get a random value between [0,1] and transform it into a random value between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				riverExitColors[current] = Math.min(Math.max(((riverExitColors[previousRight]+ riverExitColors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				//riverExitColors[current+1] = 0.0f;
				riverExitColors[current+2] = 0.0f;
			}
		}
	}


	private void populateRiverExitColorsLists()
	{
		int position;
		int offset;

		// Up Colors
		offset = 0;
		for(int x=0; x < numVerticesX; x++)
		{
			position = ((GROUND_RIVER_PATCH_VERTICES_Z-1)*numVerticesX + x)*3;

			riverExitColorsUp[offset++] = riverExitColors[position];
			riverExitColorsUp[offset++] = riverExitColors[position+1];
			riverExitColorsUp[offset++] = riverExitColors[position+2];
		}

		// Left Colors
		offset = 0;
		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			position = (z * numVerticesX) * 3;

			riverExitColorsLeft[offset++] = riverExitColors[position];
			riverExitColorsLeft[offset++] = riverExitColors[position+1];
			riverExitColorsLeft[offset++] = riverExitColors[position+2];
		}

		// Right Colors
		offset = 0;
		for(int z=0; z < GROUND_RIVER_PATCH_VERTICES_Z; z++)
		{
			position = (z * numVerticesX + (numVerticesX-1)) * 3;

			riverExitColorsRight[offset++] = riverExitColors[position];
			riverExitColorsRight[offset++] = riverExitColors[position+1];
			riverExitColorsRight[offset++] = riverExitColors[position+2];
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////


	private void updateColorGrid()
	{
		Random random = new Random();
		int position;

		for(int i=0; i < numPolygonsZ; i++)
		{
			for(int j=0; j < numPolygonsX; j++)
			{
				position = (i * numPolygonsX * COLOR_COMPONENTS) + (j * COLOR_COMPONENTS);

				groundColorGrid[position] = random.nextFloat();
				groundColorGrid[position+1] = random.nextFloat();
				groundColorGrid[position+2] = random.nextFloat();
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	private void filterGrassPoints()
	{
		int numPoints = points.size();
		int i=0;
		int ix, iz;
		int index = 0;
		float x, z;

		/**while(i < numPoints)
		{
			// Points in [0,1] range
			x = (points.get(i).x + 1.0f) * 0.5f;
			z = (points.get(i).y + 1.0f) * 0.5f;

			// Points in [0,X] [0,Z] range
			x *= numPolygonsX;
			z *= numPolygonsZ;

			ix = Math.min((int)FloatMath.floor(x), numPolygonsX - 1);
			iz = Math.min((int)FloatMath.floor(z), numPolygonsZ - 1);

			position = (iz * numPolygonsX * Constants.COLOR_COMPONENTS) + (ix * Constants.COLOR_COMPONENTS);

			if(colorGrid[position] < 0.4)
			{
				points.remove(i);
				numPoints--;
			}
			else
			{
				i++;
			}
		}**/

		float a, b, c, d;
		float aToP, bToP, cToP, dToP;
		float colorValue;

		currentPoints.clear();

		while(i < numPoints)
		{
			// convert point to [0,1] range
			vec2 newPoint = multiply(points.get(i), 1.0f, -1.0f);
			x = (newPoint.x + 1.0f) * 0.5f;
			z = (newPoint.y + 1.0f) * 0.5f;

			x *= (numVerticesX-1);
			z *= (numVerticesZ-1);

			a = FloatMath.floor(x);
			b = FloatMath.ceil(x);
			c = FloatMath.floor(z);
			d = FloatMath.ceil(z);

			aToP = Math.abs(a-x);
			bToP = Math.abs(b-x);
			cToP = Math.abs(c-x);
			dToP = Math.abs(d-x);

			if(aToP < bToP)
			{
				if(cToP < dToP)
				{
					ix = (int)a;
					iz = (int)c;
				}
				else
				{
					ix = (int)a;
					iz = (int)d;
				}
			}
			else
			{
				if(cToP < dToP)
				{
					ix = (int)b;
					iz = (int)c;
				}
				else
				{
					ix = (int)b;
					iz = (int)d;
				}
			}

			colorValue = groundColors[(iz * numVerticesX * COLOR_COMPONENTS)+(ix * COLOR_COMPONENTS)];

			/*if(colorValue < 0.5)
			{
				points.remove(i);
				numPoints--;
			}
			else
			{
				points.get(i).multiply(1,-1);
				i++;
			}*/

			if(colorValue > 0.5 && i < 64)
			{
				//currentPoints.add(new vec2(points.get(i).x, points.get(i).y));
				currentPoints.add(points.get(i));
				grassPointsScale[index++] = random.nextFloat() * 1f + 0.5f;
				////grassYRotations[j++] = random.nextFloat() * 180f;
			}
			i++;
		}
	}


	public void update(float[] viewProjection, vec3 displacement)
	{
		for(int i=0; i<15; i+=3)
		{
			currentCullingPoints[i]   = currentCullingPoints[i] + displacement.x;
			currentCullingPoints[i+1] = currentCullingPoints[i+1] + displacement.y;
			currentCullingPoints[i+2] = currentCullingPoints[i+2] + displacement.z;
		}

		//visible = perspectiveCamera.anyPointInFrustum(currentCullingPoints, 10f);
		visible = perspectiveCamera.anyPointInFrustum(currentCullingPoints);

		this.currentPosition.add(displacement);

		setIdentityM(model, 0);
		translateM(model, 0, currentPosition.x , currentPosition.y, currentPosition.z);

		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);
	}


	public float[] getModelMatrix()
	{
		return model;
	}


	public float[] getModelViewProjectionMatrix()
	{
		return modelViewProjection;
	}


	public int getGroundVaoHandle()
	{
		return groundVaoHandle[0];
	}


	public void setCurrentPosition(float x, float y, float z)
	{
		this.currentPosition.setValues(x,y,z);

		for(int i=0; i<15; i+=3)
		{
			currentCullingPoints[i]   = currentPosition.x + initialCullingPoints[i];
			currentCullingPoints[i+1] = currentPosition.y + initialCullingPoints[i+1];
			currentCullingPoints[i+2] = currentPosition.z + initialCullingPoints[i+2];
		}

		setIdentityM(model, 0);
		translateM(model, 0, x, y, z);
	}


	public void setCurrentPosition(vec3 pos)
	{
		this.currentPosition.setValues(pos);

		for(int i=0; i<15; i+=3)
		{
			currentCullingPoints[i]   = currentPosition.x + initialCullingPoints[i];
			currentCullingPoints[i+1] = currentPosition.y + initialCullingPoints[i+1];
			currentCullingPoints[i+2] = currentPosition.z + initialCullingPoints[i+2];
		}

		newGrassPointsArray();
	}


	public vec3 getCurrentPosition()
	{
		return currentPosition;
	}


	public float[] getVertexColors(int colorType)
	{
		if(type == GROUND_PATCH_GROUND)
		{
			switch (colorType)
			{
				case GROUND_PATCH_DOWN:
					return groundColorsDown;
				case GROUND_PATCH_UP:
					return groundColorsUp;
				case GROUND_PATCH_LEFT:
					return groundColorsLeft;
				case GROUND_PATCH_RIGHT:
					return groundColorsRight;
				default:
					return groundColorsDown;
			}
		}
		else if(type == GROUND_PATCH_RIVER_ENTRY)
		{
			switch(colorType)
			{
				case GROUND_PATCH_LEFT:
					return riverEntryColorsLeft;
				case GROUND_PATCH_RIGHT:
					return riverEntryColorsRight;
				default:
					return riverEntryColorsLeft;
			}
		}
		else if(type == GROUND_PATCH_RIVER_EXIT)
		{
			switch(colorType)
			{
				case GROUND_PATCH_UP:
					return riverExitColorsUp;
				case GROUND_PATCH_LEFT:
					return riverExitColorsLeft;
				case GROUND_PATCH_RIGHT:
					return riverExitColorsRight;
				default:
					return riverExitColorsUp;
			}
		}
		else
		{
			return null;
		}
	}


	private void initializeGrassPointsArray()
	{
		for(int i=0; i<256; i++)
		{
			grassPointsLODA[i] = 1f;
			grassPointsLODB[i] = 1f;
			grassPointsLODC[i] = 1f;
		}

		for(int i=0; i<64; i++)
		{
			grassPointsScale[i] = 1f;
		}
	}


	private void newGrassPointsArray()
	{
		vec2 point;
		float pointX, pointZ;
		int i=0;
		int pointsSize = currentPoints.size();
		int offsetLODA = 0;
		int offsetLODB = 0;
		int offsetLODC = 0;
		float distance;

		numGrassPointsPerLOD[LOD_A] = 0;
		numGrassPointsPerLOD[LOD_B] = 0;
		numGrassPointsPerLOD[LOD_C] = 0;

		while(i < pointsSize)
		{
			point = currentPoints.get(i);
			pointX = point.x;
			pointZ = point.y;
			//point.multiply(spreadFactorX, spreadFactorZ);
			pointX *= spreadFactorX;
			pointZ *= spreadFactorZ;
			//point.add(currentPosition.x, currentPosition.z);
			pointX += currentPosition.x;
			pointZ += currentPosition.y;

			if(perspectiveCamera.pointInFrustum(pointX, 0f, pointZ))
			{
				distance = distanceToOrigin(pointX, pointZ);

				if(distance < GRASS_LOD_A_MAX_DISTANCE)
				{
					grassPointsLODA[offsetLODA++] = pointX;
					grassPointsLODA[offsetLODA++] = pointZ;
					grassPointsLODA[offsetLODA++] = grassPointsScale[i];
					grassPointsLODA[offsetLODA++] = distance / 700f;
					numGrassPointsPerLOD[LOD_A] += 1;
				}
				else if(distance < GRASS_LOD_B_MAX_DISTANCE)
				{
					grassPointsLODB[offsetLODB++] = pointX;
					grassPointsLODB[offsetLODB++] = pointZ;
					grassPointsLODB[offsetLODB++] = grassPointsScale[i];
					grassPointsLODB[offsetLODB++] = distance / 700f;
					numGrassPointsPerLOD[LOD_B] += 1;
				}
				else // LOD_C
				{
					grassPointsLODC[offsetLODC++] = pointX;
					grassPointsLODC[offsetLODC++] = pointZ;
					grassPointsLODC[offsetLODC++] = grassPointsScale[i];
					grassPointsLODC[offsetLODC++] = distance / 700f;
					numGrassPointsPerLOD[LOD_C] += 1;
				}

				//i++;
			}
			/*else
			{
				currentPoints.remove(i);
				pointsSize--;
			}*/
			i++;
		}
	}


	public void updateGrassPointsArray()
	{
		vec2 point;
		float pointX, pointZ;
		int i=0;
		int pointsSize = currentPoints.size();
		int offsetLODA = 0;
		int offsetLODB = 0;
		int offsetLODC = 0;
		float distance;

		numGrassPointsPerLOD[LOD_A] = 0;
		numGrassPointsPerLOD[LOD_B] = 0;
		numGrassPointsPerLOD[LOD_C] = 0;

		while(i < pointsSize)
		{
			//point = currentPoints.get(i);
			point = currentPoints.get(i);
			pointX = point.x;
			pointZ = point.y;
			//point.multiply(spreadFactorX, spreadFactorZ);
			pointX *= spreadFactorX;
			pointZ *= spreadFactorZ;
			//point.add(currentPosition.x, currentPosition.z);
			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			if(perspectiveCamera.sphereInFrustum(pointX, 0f, pointZ, 10f))
			{
				distance = distanceToOrigin(pointX, pointZ);

				if(distance < GRASS_LOD_A_MAX_DISTANCE)
				{
					grassPointsLODA[offsetLODA++] = pointX;
					grassPointsLODA[offsetLODA++] = pointZ;
					grassPointsLODA[offsetLODA++] = grassPointsScale[i];
					grassPointsLODA[offsetLODA++] = distance / 700f;
					numGrassPointsPerLOD[LOD_A] += 1;
				}
				else if(distance < GRASS_LOD_B_MAX_DISTANCE)
				{
					grassPointsLODB[offsetLODB++] = pointX;
					grassPointsLODB[offsetLODB++] = pointZ;
					grassPointsLODB[offsetLODB++] = grassPointsScale[i];
					grassPointsLODB[offsetLODB++] = distance / 700f;
					numGrassPointsPerLOD[LOD_B] += 1;
				}
				else // LOD_C
				{
					grassPointsLODC[offsetLODC++] = pointX;
					grassPointsLODC[offsetLODC++] = pointZ;
					grassPointsLODC[offsetLODC++] = grassPointsScale[i];
					grassPointsLODC[offsetLODC++] = distance / 700f;
					numGrassPointsPerLOD[LOD_C] += 1;
				}

				//i++;
			}
			/*else
			{
				currentPoints.remove(i);
				pointsSize--;
			}*/
			i++;
		}
	}


	public void saveState(FileOutputStream outputStream) throws IOException
	{
		int numPoints;
		vec2 currentPoint;
		StringBuilder builder = new StringBuilder();
		visible = true;

		builder.append("PROPERTIES ");
		builder.append(type);		builder.append(" ");
		builder.append(currentPosition.x);	builder.append(" ");
		builder.append(currentPosition.y);	builder.append(" ");
		builder.append(currentPosition.z);	builder.append(" ");
		builder.append(numGrassPointsPerLOD[LOD_A]);	builder.append(" ");
		builder.append(numGrassPointsPerLOD[LOD_B]);	builder.append(" ");
		builder.append(numGrassPointsPerLOD[LOD_C]);	builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("SAMPLER_POINTS");
		numPoints = points.size();
		builder.append(" ");	builder.append(numPoints);
		for(int i=0; i<numPoints; i++)
		{
			currentPoint = points.get(i);
			builder.append(" ");	builder.append(currentPoint.x);
			builder.append(" ");	builder.append(currentPoint.y);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("GROUND_COLORS ");
		numPoints = groundColors.length;
		builder.append(numPoints);
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(groundColors[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("RIVER_ENTRY_COLORS ");
		numPoints = riverEntryColors.length;
		builder.append(numPoints);
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(riverEntryColors[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("RIVER_EXIT_COLORS ");
		numPoints = riverExitColors.length;
		builder.append(numPoints);
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(riverExitColors[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());
	}


	public void loadState(BufferedReader bufferedReader) throws IOException
	{
		String line;
		String[] tokens;
		int numPoints;
		int offset;
		float x, y, z;

		// Read the properties
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		type = Integer.parseInt(tokens[1]);
		x = Float.parseFloat(tokens[2]);
		y = Float.parseFloat(tokens[3]);
		z = Float.parseFloat(tokens[4]);
		// Set the current position & set the current culling points
		this.setCurrentPosition(x,y,z);
		numGrassPointsPerLOD[LOD_A] = Integer.parseInt(tokens[5]);
		numGrassPointsPerLOD[LOD_B] = Integer.parseInt(tokens[6]);
		numGrassPointsPerLOD[LOD_C] = Integer.parseInt(tokens[7]);

		// Read the sampler points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		points.clear();
		for(int i=0; i<numPoints; i++)
		{
			x = Float.parseFloat(tokens[offset++]);
			y = Float.parseFloat(tokens[offset++]);
			points.add(new vec2(x,y));
		}

		// Read the ground colors
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			groundColors[i] = Float.parseFloat(tokens[offset++]);
		}
		updateGroundColorsBuffer();
		updateGroundVertexArrayObject();

		// Read the river entry colors
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			riverEntryColors[i] = Float.parseFloat(tokens[offset++]);
		}
		updateRiverEntryColorsBuffer();
		updateRiverEntryVertexArrayObject();

		// Read the river exit colors
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			riverExitColors[i] = Float.parseFloat(tokens[offset++]);
		}
		updateRiverExitColorsBuffer();
		updateRiverExitVertexArrayObject();

		// Update the grass buffers
		filterGrassPoints();
	}
}