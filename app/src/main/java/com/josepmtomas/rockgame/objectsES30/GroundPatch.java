package com.josepmtomas.rockgame.objectsES30;

import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.data.GroundPatchType;
import com.josepmtomas.rockgame.util.PerspectiveCamera;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.josepmtomas.rockgame.Constants.*;

/**
 * Created by Josep on 08/08/2014.
 */
public class GroundPatch
{
	public int type;

	// Frustum culling points
	private float[] initialCullingPoints;
	private float[] currentCullingPoints = new float[12];

	// VBO & VAO
	private int[] vboHandles;
	private int[] vboColorHandle = new int[1];
	private int[] vaoHandle = new int[1];

	// Vertex color attribute
	private float[] colors;
	private FloatBuffer colorsBuffer;

	private Float maximumVariance;

	private int numVerticesX;
	private int numVerticesZ;

	private vec3 currentPosition = new vec3(0.0f);

	private float[] currentModelMatrix = new float[16];
	private float[] previousModelMatrix = new float[16];
	private float[] currentModelViewMatrix = new float[16];
	private float[] previousModelViewMatrix = new float[16];



	public GroundPatch(int numVerticesX, int numVerticesZ, Float maximumVariance, int[] vboHandles, float[] initialCullingPoints)
	{
		this.type = GROUND_PATCH_GROUND;

		this.numVerticesX = numVerticesX;
		this.numVerticesZ = numVerticesZ;
		this.maximumVariance = maximumVariance;
		this.vboHandles = vboHandles;
		this.initialCullingPoints = initialCullingPoints;

		setIdentityM(currentModelMatrix, 0);

		colors = new float[numVerticesZ * numVerticesZ * Constants.COLOR_COMPONENTS];
	}


	public void initialize(GroundPatchType type, float[] downColors, float[] sideColors)
	{
		generateVertexColors(type, downColors, sideColors);

		createColorsBuffer();
		createVertexArrayObject();
	}


	public void reinitialize(GroundPatchType type, float[] downColors, float[] sideColors)
	{
		generateVertexColors(type, downColors, sideColors);

		updateColorsBuffer();
		updateVertexArrayObject();
	}


	private void createColorsBuffer()
	{
		colorsBuffer = ByteBuffer
				.allocateDirect(colors.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(colors);
		colorsBuffer.position(0);

		glGenBuffers(1, vboColorHandle, 0);
		glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle[0]);
		glBufferData(GL_ARRAY_BUFFER, colorsBuffer.capacity() * Constants.BYTES_PER_FLOAT, colorsBuffer, GL_DYNAMIC_DRAW);
	}


	private void updateColorsBuffer()
	{
		colorsBuffer.position(0);
		colorsBuffer.put(colors);
		colorsBuffer.position(0);

		glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle[0]);
		glBufferSubData(GL_ARRAY_BUFFER, 0, colorsBuffer.capacity() * Constants.BYTES_PER_FLOAT, colorsBuffer);
	}


	private void createVertexArrayObject()
	{
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[2]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[4]);

		glBindVertexArray(0);
	}


	private void updateVertexArrayObject()
	{
		glBindVertexArray(vaoHandle[0]);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindVertexArray(0);
	}


	private void generateVertexColors()
	{
		int position;
		Random random = new Random();

		for(int i=0; i < numVerticesZ; i++)
		{
			for(int j=0; j < numVerticesX; j++)
			{
				position = (i * numVerticesX + j) * Constants.COLOR_COMPONENTS;

				colors[position] = random.nextFloat();
				colors[position+1] = 0.0f;
				colors[position+2] = 0.0f;
			}
		}
	}


	private void generateVertexColors(GroundPatchType type, float[] downColors, float[] sideColors)
	{
		switch(type)
		{
			case GROUND_ROOT:
				generateVertexColorsRoot();
				break;
			case GROUND_UP:
				generateVertexColorsUp(downColors);
				break;
			case GROUND_LEFT:
				generateVertexColorsLeft(sideColors);
				break;
			case GROUND_RIGHT:
				generateVertexColorsRight(sideColors);
				break;
			case GROUND_UP_LEFT:
				generateVertexColorsUpLeft(downColors, sideColors);
				break;
			case GROUND_UP_RIGHT:
				generateVertexColorsUpRight(downColors, sideColors);
				break;
			default:
				break;
		}
	}


	private void generateVertexColorsRoot()
	{
		Random random = new Random();
		float randomValue;

		int current;
		int previousDown;
		int previousLeft;

		// Paint the first vertex (0,0)
		colors[0] = random.nextFloat();
		colors[1] = 0.0f;
		colors[2] = 0.0f;

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * Constants.COLOR_COMPONENTS;
			previousLeft = (x-1) * Constants.COLOR_COMPONENTS;

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			colors[current] = Math.min(Math.max(colors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			colors[current+1] = 0.0f;
			colors[current+2] = 0.0f;
		}

		// Paint the first column (0,z)
		for(int z=1; z < numVerticesZ; z++)
		{
			current = z * numVerticesX * Constants.COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * Constants.COLOR_COMPONENTS;

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			colors[current] = Math.min(Math.max(colors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			colors[current+1] = 0.0f;
			colors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousLeft]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}


	private void generateVertexColorsUp(float[] downColors)
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
			current = x * Constants.COLOR_COMPONENTS;

			colors[current] = downColors[offset++];
			colors[current+1] = downColors[offset++];
			colors[current+2] = downColors[offset++];
		}

		// Paint the first column (0,z)
		for(int z=1; z < numVerticesZ; z++)
		{
			current = z * numVerticesX * Constants.COLOR_COMPONENTS;
			previousDown = (z-1) * numVerticesX * Constants.COLOR_COMPONENTS;

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			colors[current] = Math.min(Math.max(colors[previousDown] + (randomValue * maximumVariance),0.0f),1.0f);
			colors[current+1] = 0.0f;
			colors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousLeft]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}


	private void generateVertexColorsLeft(float[] leftColors)
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
			current = z * numVerticesX * Constants.COLOR_COMPONENTS;

			colors[current] = leftColors[offset++];
			colors[current+1] = leftColors[offset++];
			colors[current+2] = leftColors[offset++];
		}

		// Paint the first row (x,0)
		for(int x=1; x < numVerticesX; x++)
		{
			current = x * Constants.COLOR_COMPONENTS;
			previousLeft = (x-1) * Constants.COLOR_COMPONENTS;

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			colors[current] = Math.min(Math.max(colors[previousLeft] + (randomValue * maximumVariance),0.0f),1.0f);
			colors[current+1] = 0.0f;
			colors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousLeft]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}

	private void generateVertexColorsRight(float[] rightColors) {
		Random random = new Random();
		float randomValue;

		int offset = 0;
		int current;
		int previousDown;
		int previousRight;

		// Paint the last column
		for (int z = 0; z < numVerticesZ; z++) {
			current = (z * numVerticesX + (numVerticesX-1)) * Constants.COLOR_COMPONENTS;

			colors[current] = rightColors[offset++];
			colors[current + 1] = rightColors[offset++];
			colors[current + 2] = rightColors[offset++];
		}

		// Paint the first row (x,0)
		for (int x = numVerticesX-2; x >= 0; x--) {
			current = x * Constants.COLOR_COMPONENTS;
			previousRight = (x+1) * Constants.COLOR_COMPONENTS;

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			colors[current] = Math.min(Math.max(colors[previousRight] + (randomValue * maximumVariance), 0.0f), 1.0f);
			colors[current+1] = 0.0f;
			colors[current+2] = 0.0f;
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=numVerticesX-2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x+1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousRight]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}


	private void generateVertexColorsUpLeft(float[] downColors, float[] leftColors)
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
			current = x * Constants.COLOR_COMPONENTS;

			colors[current] = downColors[offset++];
			colors[current+1] = downColors[offset++];
			colors[current+2] = downColors[offset++];
		}

		// Paint the first column
		offset = 3;
		for(int z=1; z < numVerticesZ; z++)
		{
			current = (z * numVerticesX) * Constants.COLOR_COMPONENTS;

			colors[current] = leftColors[offset++];
			colors[current+1] = leftColors[offset++];
			colors[current+2] = leftColors[offset++];
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=1; x < numVerticesX; x++)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousLeft = (z * numVerticesX + (x-1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousLeft]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}


	private void generateVertexColorsUpRight(float[] downColors, float[] rightColors)
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
			current = x * Constants.COLOR_COMPONENTS;

			colors[current] = downColors[offset++];
			colors[current+1] = downColors[offset++];
			colors[current+2] = downColors[offset++];
		}

		// Paint the last column
		offset = 3;
		for (int z = 1; z < numVerticesZ; z++) {
			current = (z * numVerticesX + (numVerticesX-1)) * Constants.COLOR_COMPONENTS;

			colors[current] = rightColors[offset++];
			colors[current+1] = rightColors[offset++];
			colors[current+2] = rightColors[offset++];
		}

		// Paint the rest of vertices
		for(int z=1; z < numVerticesZ; z++)
		{
			for(int x=numVerticesX-2; x >= 0; x--)
			{
				current = (z * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousDown = ((z-1) * numVerticesX + x) * Constants.COLOR_COMPONENTS;
				previousRight = (z * numVerticesX + (x+1)) * Constants.COLOR_COMPONENTS;

				// Get a random with a value between [0,1] and transform it into a random with a value
				// between [-1,1]
				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				colors[current] = Math.min(Math.max(((colors[previousRight]+colors[previousDown]) / 2.0f) + (randomValue * maximumVariance),0.0f),1.0f);
				colors[current+1] = 0.0f;
				colors[current+2] = 0.0f;
			}
		}
	}


	public boolean isVisible(PerspectiveCamera camera)
	{
		return camera.anyPointInFrustum(currentCullingPoints, 50f);
	}


	public void update(float[] viewMatrix, float time, vec3 displacement)
	{
		for(int i=0; i<12; i+=3)
		{
			currentCullingPoints[i]   = currentCullingPoints[i] + displacement.x;
			currentCullingPoints[i+1] = currentCullingPoints[i+1] + displacement.y;
			currentCullingPoints[i+2] = currentCullingPoints[i+2] + displacement.z;
		}

		setIdentityM(previousModelMatrix, 0);
		translateM(previousModelMatrix, 0, currentPosition.x, currentPosition.y, currentPosition.z);

		this.currentPosition.add(displacement);

		setIdentityM(currentModelMatrix, 0);
		translateM(currentModelMatrix, 0, currentPosition.x , currentPosition.y, currentPosition.z);

		multiplyMM(currentModelViewMatrix, 0, viewMatrix, 0, currentModelMatrix, 0);
		multiplyMM(previousModelViewMatrix, 0, viewMatrix, 0, previousModelMatrix, 0);
	}


	public float[] getCurrentModelMatrix()
	{
		return currentModelMatrix;
	}


	public float[] getPreviousModelMatrix()
	{
		return previousModelMatrix;
	}


	public float[] getCurrentModelViewMatrix()
	{
		return currentModelViewMatrix;
	}


	public float[] getPreviousModelViewMatrix()
	{
		return previousModelViewMatrix;
	}


	public int getVaoHandle()
	{
		return vaoHandle[0];
	}


	public void setCurrentPosition(float x, float y, float z)
	{
		this.currentPosition.setValues(x,y,z);

		for(int i=0; i<12; i+=3)
		{
			currentCullingPoints[i]   = currentPosition.x + initialCullingPoints[i];
			currentCullingPoints[i+1] = currentPosition.y + initialCullingPoints[i+1];
			currentCullingPoints[i+2] = currentPosition.z + initialCullingPoints[i+2];
		}

		setIdentityM(currentModelMatrix, 0);
		translateM(currentModelMatrix, 0, x, y, z);
	}


	public void setCurrentPosition(vec3 pos)
	{
		this.currentPosition.setValues(pos);

		for(int i=0; i<12; i+=3)
		{
			currentCullingPoints[i]   = currentPosition.x + initialCullingPoints[i];
			currentCullingPoints[i+1] = currentPosition.y + initialCullingPoints[i+1];
			currentCullingPoints[i+2] = currentPosition.z + initialCullingPoints[i+2];
		}
	}


	public vec3 getCurrentPosition()
	{
		return currentPosition;
	}


	public float[] getVertexColors(GroundPatchType type)
	{
		float[] result;
		int position;
		int offset = 0;

		switch(type)
		{
			case GROUND_DOWN:
				result = new float[numVerticesX * 3];

				for(int x=0; x < numVerticesX; x++)
				{
					position = x*3;

					result[offset++] = colors[position];
					result[offset++] = colors[position+1];
					result[offset++] = colors[position+2];
				}

				return result;

			case GROUND_UP:
				result = new float[numVerticesX * 3];

				for(int x=0; x < numVerticesX; x++)
				{
					position = ((numVerticesZ-1)*numVerticesX + x)*3;

					result[offset++] = colors[position];
					result[offset++] = colors[position+1];
					result[offset++] = colors[position+2];
				}

				return result;

			case GROUND_LEFT:
				result = new float[numVerticesZ * 3];

				for(int z=0; z < numVerticesZ; z++)
				{
					position = (z * numVerticesX) * 3;

					result[offset++] = colors[position];
					result[offset++] = colors[position+1];
					result[offset++] = colors[position+2];
				}

				return result;

			case GROUND_RIGHT:
				result = new float[numVerticesZ * 3];

				for(int z=0; z < numVerticesZ; z++)
				{
					position = (z * numVerticesX + (numVerticesX-1)) * 3;

					result[offset++] = colors[position];
					result[offset++] = colors[position+1];
					result[offset++] = colors[position+2];
				}

				return result;

			default:
				return null;
		}
	}
}
