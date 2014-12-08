package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.FloatMath;
import android.util.Log;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.data.GroundPatchType;
import com.josepmtomas.rockgame.programs.GroundShaderProgram;

import java.util.Random;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 18/07/2014.
 */
public class GroundPatch extends StaticMesh
{
	private final static String TAG = "GroundPatch";

	private float[] modelMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];
	private float[] modelViewProjectionMatrix = new float[16];

	private int sizeX;
	private int sizeZ;

	private vec3 initialPosition;
	private vec3 currentPosition;
	private vec3 center;
	private float currentDistance;
	private float maximumDistance;

	private float maximumVariation = 0.25f;

	private GroundShaderProgram groundShaderProgram;

	private GrassInstance[] grassInstances;

	/*private int grassColorTexture;
	private int grassNormalTexture;
	private int groundColorTexture;
	private int groundAlphaTexture;
	private int groundNormalTexture;*/

	private int[] textures;

	private float time;


	/**
	 *
	 * @param sizeX horizontal number of vertices in the patch
	 * @param sizeZ vertical number of vertices in the patch
	 * @param width width of the patch (distance)
	 * @param height height of the patch (distance)
	 * @param initialOffset initial displacement of this patch
	 * @param groundShaderProgram shader program
	 * @param textures set of textures for the shader program
	 */
	public GroundPatch(int sizeX, int sizeZ, float width, float height, vec3 initialOffset, Grass grassParent, GroundShaderProgram groundShaderProgram, int[] textures)
	{
		this.sizeX = sizeX;
		this.sizeZ = sizeZ;

		this.groundShaderProgram = groundShaderProgram;
		this.textures = textures;
		this.currentPosition = new vec3(initialOffset);

		createPatch(sizeX, sizeZ, width, height);
		createGrass(sizeX, sizeZ, width, height, grassParent);
	}


	public GroundPatch(Context context, int sizeX, int sizeZ, float width, float height, vec3 initialPosition)
	{
		this.sizeX = sizeX;
		this.sizeZ = sizeZ;

		createPatch(sizeX, sizeZ, width, height);

		this.initialPosition = new vec3(initialPosition);
		this.center = new vec3(0.0f);
		this.maximumDistance = 1000.0f;
		this.currentPosition = new vec3(initialPosition);
		this.currentDistance = 0.0f;

		groundShaderProgram = new GroundShaderProgram(context);
	}

	public GroundPatch(Context context, int resourceId, vec3 initialPosition, vec3 center, float maximumDistance) {
		super(context, resourceId);

		this.initialPosition = initialPosition;
		this.center = center;
		this.maximumDistance = maximumDistance;

		currentPosition = new vec3(initialPosition);
		currentDistance = 0.0f;

		groundShaderProgram = new GroundShaderProgram(context);
	}

	/*public void setTextures(int grassColor, int grassNormal, int groundColor, int groundAlpha, int groundNormal)
	{
		grassColorTexture = grassColor;
		grassNormalTexture = grassNormal;
		groundColorTexture = groundColor;
		groundAlphaTexture = groundAlpha;
		groundNormalTexture = groundNormal;
	}*/

	public void setOffset(float offset)
	{
		currentPosition.add(0f,0f,offset);
	}

	@Override
	public void initialize()
	{
		generateVertexColors();

		super.initialize();
	}


	public void initialize(GroundPatchType type, float[] previousColors)
	{
		if(type == GroundPatchType.GROUND_UP)
			generateVertexColorsWithDownColors(previousColors);
		else if(type == GroundPatchType.GROUND_LEFT)
			generateVertexColorsWithRightColors(previousColors);
		else if(type == GroundPatchType.GROUND_RIGHT)
			generateVertexColorsWithLeftColors(previousColors);

		super.initialize();
	}


	public void initialize(GroundPatchType type, float[] downColors, float[] sideColors)
	{
		if(type == GroundPatchType.GROUND_UP_LEFT)
			generateVertexColorsWithDownAndRightColors(downColors, sideColors);
		else if(type == GroundPatchType.GROUND_UP_RIGHT)
			generateVertexColorsWithDownAndLeftColors(downColors, sideColors);

		super.initialize();
	}


	public void reinitialize(GroundPatchType type, float[] previousColors)
	{
		if(type == GroundPatchType.GROUND_LEFT)
			generateVertexColorsWithRightColors(previousColors);
		else if(type == GroundPatchType.GROUND_RIGHT)
			generateVertexColorsWithLeftColors(previousColors);

		super.reinitialize();
	}


	public void reinitialize(GroundPatchType type, float[] downColors, float[] sideColors)
	{
		if(type == GroundPatchType.GROUND_UP_LEFT)
			generateVertexColorsWithDownAndRightColors(downColors, sideColors);
		else if(type == GroundPatchType.GROUND_UP_RIGHT)
			generateVertexColorsWithDownAndLeftColors(downColors, sideColors);

		super.reinitialize();
	}


	@Override
	public void bind() {
		super.bind();

		/*vertexBuffer.setVertexAttribPointer(
				0,
				groundShaderProgram.getPositionAttributeLocation(),
				POSITION_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TEXCOORD_BYTE_OFFSET,
				groundShaderProgram.getTexCoordAttributeLocation(),
				TEXCOORD_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				NORMAL_BYTE_OFFSET,
				groundShaderProgram.getNormalAttributeLocation(),
				NORMAL_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TANGENT_BYTE_OFFSET,
				groundShaderProgram.getTangentAttributeLocation(),
				TANGENT_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				COLOR_BYTE_OFFSET,
				groundShaderProgram.getColorAttributeLocation(),
				COLOR_COMPONENTS,
				STRIDE);*/
	}


	public void update(vec3 movement, float[] viewMatrix, float[] projectionMatrix, float time)
	{
		this.time = time;
		this.viewMatrix = viewMatrix;
		this.projectionMatrix = projectionMatrix;

		this.currentPosition.add(movement);

		// Update position
		//currentPosition.add(0f, 0f, 0.75f);
		/*currentDistance = operations.distanceBetween(initialPosition, currentPosition);

		if(currentDistance > maximumDistance)
		{
			currentDistance = currentDistance % maximumDistance;
			currentPosition.setValues(initialPosition);
			currentPosition.add(0f, 0f, currentDistance);

			initialize();
		}*/

		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, currentPosition.x, currentPosition.y - 10.0f, currentPosition.z);

		// Calculate ModelViewProjection matrix
		multiplyMM(modelViewMatrix, 0, this.viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, this.projectionMatrix, 0, modelViewMatrix, 0);

		for(int i=0; i < grassInstances.length; i++)
		{
			grassInstances[i].update(movement);
		}
	}


	@Override
	public void draw()
	{
		super.draw();

		groundShaderProgram.useProgram();

		//groundShaderProgram.setUniforms(time, modelViewProjectionMatrix, textures);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	public void drawGrass()
	{
		for(int i=0; i < grassInstances.length; i++)
		{
			grassInstances[i].draw();
		}
	}


	private void createPatch(int sizeX, int sizeZ, float width, float height)
	{
		int numVerticesX = sizeX+1;
		int numVerticesZ = sizeZ+1;
		int index;
		int indexLeft;
		int indexDown;
		int indexLeftDown;
		int offset = 0;

		float currentX;
		float currentZ;
		float minimumX = -(width / 2.0f);
		float minimumZ = (height / 2.0f);
		float incrementX = width / (float)sizeX;
		float incrementZ = height / (float)sizeZ;

		// Generate vertices
		numVertices = numVerticesX * numVerticesZ;
		vertices = new float[numVertices * TOTAL_COMPONENTS];

		for(int i=0; i < numVerticesZ; i++)
		{
			currentZ = minimumZ - (i * incrementZ);
			for(int j=0; j < numVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				vertices[offset++] = currentX;
				vertices[offset++] = 0.0f;
				vertices[offset++] = currentZ;

				// Vertex texture coordinate (U V)
				vertices[offset++] = (float)j / (float)sizeX;
				vertices[offset++] = (float)i / (float)sizeZ;

				// Vertex normal (X Y Z)
				vertices[offset++] = 0.0f;
				vertices[offset++] = 1.0f;
				vertices[offset++] = 0.0f;

				// Vertex tangent (X Y Z W)
				vertices[offset++] = 1.0f;
				vertices[offset++] = 0.0f;
				vertices[offset++] = 0.0f;
				vertices[offset++] = 1.0f;

				// Vertex color (R G B)
				vertices[offset++] = 0.0f;
				vertices[offset++] = 0.0f;
				vertices[offset++] = 0.0f;
			}
		}

		// Generate faces
		numIndices = sizeX * sizeZ * 2;
		indices = new int[3 * numIndices];
		offset = 0;

		for(int i=1; i < numVerticesZ; i++)
		{
			for (int j=1; j < numVerticesX; j++)
			{
				index = j + (i * numVerticesX);
				indexLeft = (j-1) + (i * numVerticesX);
				indexDown = j + ((i-1) * numVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numVerticesX);

				// First face
				indices[offset++] = index;
				indices[offset++] = indexLeftDown;
				indices[offset++] = indexDown;

				// Second face
				indices[offset++] = index;
				indices[offset++] = indexLeft;
				indices[offset++] = indexLeftDown;
			}
		}
	}


	public void createGrass(int sizeX, int sizeZ, float width, float height, Grass grassParent)
	{
		grassInstances = new GrassInstance[sizeX * sizeZ];

		float initialX = (((float)sizeX/2.0f) - 1.0f)* width + (width / 2.0f);
		initialX = -initialX;
		float initialZ = (((float)sizeZ/2.0f) - 1.0f)* height + (height / 2.0f);

		for(int i=0; i < sizeZ; i++)
		{
			for(int j=0; j < sizeX; j++)
			{
				vec3 pos = new vec3(initialX + (float)j*width, -10.0f, initialZ - (float)i*height);
				//pos.add(currentPosition);

				grassInstances[i*sizeX + j] = new GrassInstance(grassParent, pos);
			}
		}
	}


	// Initial generation of vertex colors
	private void generateVertexColors()
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousRightValue;
		float previousDownValue;

		int columns = sizeX + 1;
		int rows = sizeZ + 1;

		int current;
		int previousRight;
		int previousDown;

		// Paint the first vertex
		current = COLOR_OFFSET;
		vertices[current++] = random.nextFloat();
		vertices[current++] = 0.0f;
		vertices[current] = 0.0f;

		// Paint the first row
		for(int col = 1; col < columns; col++)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);
			previousRight = COLOR_OFFSET + ((col-1) * TOTAL_COMPONENTS);

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			vertices[current++] = Math.min(Math.max(vertices[previousRight] + randomValue * maximumVariation, 0.0f), 1.0f);
			vertices[current++] = 0.0f;
			vertices[current] = 0.0f;
		}

		// Paint the first column
		for(int row = 1; row < rows; row++)
		{
			current = COLOR_OFFSET + ((row * columns) * TOTAL_COMPONENTS);
			previousDown = COLOR_OFFSET + (((row-1) * columns) * TOTAL_COMPONENTS);

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			vertices[current++] = Math.min(Math.max(vertices[previousDown] + randomValue * maximumVariation, 0.0f), 1.0f);
			vertices[current++] = 0.0f;
			vertices[current] = 0.0f;
		}

		// Paint the rest of the mesh
		for(int row = 1; row < rows; row++)
		{
			for(int col = 1; col < columns; col++)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousRight = COLOR_OFFSET + ((row * columns + (col-1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousRightValue = vertices[previousRight];
				currentValue = (previousDownValue + previousRightValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	private void generateVertexColorsWithDownColors(float[] previousUpColors)
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousLeftValue;
		float previousDownValue;

		int columns = sizeX+1;
		int rows = sizeZ+1;

		int current;
		int previousDown;
		int previousLeft;
		int offset = 0;

		// Paint the lower vertices
		for(int col=0; col < columns; col++)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);

			vertices[current] = previousUpColors[offset++];
			vertices[current+1] = previousUpColors[offset++];
			vertices[current+2] = previousUpColors[offset++];
		}

		// Paint the first column
		for(int row = 1; row < rows; row++)
		{
			current = COLOR_OFFSET + ((row * columns) * TOTAL_COMPONENTS);
			previousDown = COLOR_OFFSET + (((row-1) * columns) * TOTAL_COMPONENTS);

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			vertices[current++] = Math.min(Math.max(vertices[previousDown] + randomValue * maximumVariation, 0.0f), 1.0f);
			vertices[current++] = 0.0f;
			vertices[current] = 0.0f;
		}

		// Paint the rest of the mesh
		for(int row = 1; row < rows; row++)
		{
			for(int col = 1; col < columns; col++)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousLeft = COLOR_OFFSET + ((row * columns + (col-1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousLeftValue = vertices[previousLeft];
				currentValue = (previousDownValue + previousLeftValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	private void generateVertexColorsWithRightColors(float[] previousRightColors)
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousRightValue;
		float previousDownValue;

		int columns = sizeX+1;
		int rows = sizeZ+1;

		int current;
		int previousDown;
		int previousRight;
		int offset = 0;

		// Paint the last column
		for(int row=0; row < rows; row++)
		{
			current = COLOR_OFFSET + (row * columns * TOTAL_COMPONENTS) + ((columns-1) * TOTAL_COMPONENTS);

			vertices[current] = previousRightColors[offset++];
			vertices[current+1] = previousRightColors[offset++];
			vertices[current+2] = previousRightColors[offset++];
		}

		// Paint the first row
		for(int col=columns-2; col >= 0; col--)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);
			previousRight = COLOR_OFFSET + ((col+1) * TOTAL_COMPONENTS);

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			vertices[current++] = Math.min(Math.max(vertices[previousRight] + randomValue * maximumVariation, 0.0f), 1.0f);
			vertices[current++] = 0.0f;
			vertices[current] = 0.0f;
		}

		// Paint the rest
		for(int row = 1; row < rows; row++)
		{
			for(int col = columns-2; col >= 0; col--)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousRight = COLOR_OFFSET + ((row * columns + (col+1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousRightValue = vertices[previousRight];
				currentValue = (previousDownValue + previousRightValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	private void generateVertexColorsWithLeftColors(float[] previousRightColors)
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousLeftValue;
		float previousDownValue;

		int columns = sizeX+1;
		int rows = sizeZ+1;

		int current;
		int previousDown;
		int previousLeft;
		int offset = 0;

		// Paint the first column
		for(int row=0; row < rows; row++)
		{
			current = COLOR_OFFSET + (row * columns * TOTAL_COMPONENTS);

			vertices[current] = previousRightColors[offset++];
			vertices[current+1] = previousRightColors[offset++];
			vertices[current+2] = previousRightColors[offset++];
		}

		// Paint the first row
		for(int col=1; col < columns; col++)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);
			previousLeft = COLOR_OFFSET + ((col-1) * TOTAL_COMPONENTS);

			// Get a random with a value between [0,1] and transform it into a random with a value
			// between [-1,1]
			randomValue = random.nextFloat();
			randomValue = randomValue * 2.0f - 1.0f;

			vertices[current++] = Math.min(Math.max(vertices[previousLeft] + randomValue * maximumVariation, 0.0f), 1.0f);
			vertices[current++] = 0.0f;
			vertices[current] = 0.0f;
		}

		// Paint the rest
		for(int row = 1; row < rows; row++)
		{
			for(int col = 1; col < columns; col++)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousLeft = COLOR_OFFSET + ((row * columns + (col-1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousLeftValue = vertices[previousLeft];
				currentValue = (previousDownValue + previousLeftValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	private void generateVertexColorsWithDownAndLeftColors(float[] previousDownColors, float[] previousLeftColors)
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousLeftValue;
		float previousDownValue;

		int columns = sizeX+1;
		int rows = sizeZ+1;

		int current;
		int previousDown;
		int previousLeft;
		int offset;

		// Paint the first row
		offset = 0;
		for(int col=0; col < columns; col++)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);

			vertices[current] = previousDownColors[offset++];
			vertices[current+1] = previousDownColors[offset++];
			vertices[current+2] = previousDownColors[offset++];
		}

		// Paint the first column
		offset = 3;
		for(int row=1; row < rows; row++)
		{
			current = COLOR_OFFSET + (row * columns * TOTAL_COMPONENTS);

			vertices[current] = previousLeftColors[offset++];
			vertices[current+1] = previousLeftColors[offset++];
			vertices[current+2] = previousLeftColors[offset++];
		}

		// Paint the rest
		for(int row = 1; row < rows; row++)
		{
			for(int col = 1; col < columns; col++)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousLeft = COLOR_OFFSET + ((row * columns + (col-1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousLeftValue = vertices[previousLeft];
				currentValue = (previousDownValue + previousLeftValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	private void generateVertexColorsWithDownAndRightColors(float[] previousDownColors, float[] previousRightColors)
	{
		Random random = new Random();
		float randomValue;
		float currentValue;
		float previousRightValue;
		float previousDownValue;

		int columns = sizeX+1;
		int rows = sizeZ+1;

		int current;
		int previousDown;
		int previousRight;
		int offset;

		// Paint the first row
		offset = 0;
		for(int col=0; col < columns; col++)
		{
			current = COLOR_OFFSET + (col * TOTAL_COMPONENTS);

			vertices[current] = previousDownColors[offset++];
			vertices[current+1] = previousDownColors[offset++];
			vertices[current+2] = previousDownColors[offset++];
		}

		// Paint the last column
		offset = 3;
		for(int row=1; row < rows; row++)
		{
			//current = COLOR_OFFSET + (row * columns * TOTAL_COMPONENTS) + ((columns-1) * TOTAL_COMPONENTS);
			// col = columns-1
			current = COLOR_OFFSET + (((row * columns)+ columns-1)* TOTAL_COMPONENTS);

			vertices[current] = previousRightColors[offset++];
			vertices[current+1] = previousRightColors[offset++];
			vertices[current+2] = previousRightColors[offset++];
		}

		// Paint the rest
		for(int row = 1; row < rows; row++)
		{
			for(int col = columns-2; col >= 0; col--)
			{
				current = COLOR_OFFSET + ((row * columns + col) * TOTAL_COMPONENTS);
				previousDown = COLOR_OFFSET + (((row-1) * columns + col) * TOTAL_COMPONENTS);
				previousRight = COLOR_OFFSET + ((row * columns + (col+1)) * TOTAL_COMPONENTS);

				randomValue = random.nextFloat();
				randomValue = randomValue * 2.0f - 1.0f;

				previousDownValue = vertices[previousDown];
				previousRightValue = vertices[previousRight];
				currentValue = (previousDownValue + previousRightValue)/2.0f;
				currentValue = Math.min(Math.max(currentValue + randomValue * maximumVariation, 0.0f), 1.0f);

				vertices[current++] = currentValue;
				vertices[current++] = 0.0f;
				vertices[current] = 0.0f;
			}
		}
	}


	public float[] getDownVertexColors()
	{
		float[] colors = new float[(sizeX+1) * COLOR_COMPONENTS];
		int offset = 0;
		int position;

		for(int i=0; i < sizeX+1; i++)
		{
			position = COLOR_OFFSET + (i * TOTAL_COMPONENTS);

			colors[offset++] = vertices[position];
			colors[offset++] = vertices[position+1];
			colors[offset++] = vertices[position+2];
		}

		return colors;
	}


	public float[] getLeftVertexColors()
	{
		float[] colors = new float[(sizeZ+1) * COLOR_COMPONENTS];
		int offset = 0;
		int position;

		for(int i=0; i < sizeZ+1; i++)
		{
			position = COLOR_OFFSET + (i * TOTAL_COMPONENTS * (sizeX+1));

			colors[offset++] = vertices[position];
			colors[offset++] = vertices[position+1];
			colors[offset++] = vertices[position+2];
		}

		return colors;
	}


	public float[] getRightVertexColors()
	{
		float[] colors = new float[(sizeZ+1) * COLOR_COMPONENTS];
		int offset = 0;
		int position;

		for(int i=0; i < sizeZ+1; i++)
		{
			position = COLOR_OFFSET + (TOTAL_COMPONENTS * (sizeX)) + (i * TOTAL_COMPONENTS * (sizeX+1));

			colors[offset++] = vertices[position];
			colors[offset++] = vertices[position+1];
			colors[offset++] = vertices[position+2];
		}

		return colors;
	}


	public float[] getUpVertexColors()
	{
		float[] colors = new float[(sizeX+1) * COLOR_COMPONENTS];
		int offset = 0;
		int position;

		//for(int i=110; i < 121; i++)
		for(int i=0; i < sizeX+1; i++)
		{
			//position = COLOR_OFFSET + (i * TOTAL_COMPONENTS);
			position = COLOR_OFFSET + (((sizeX+1)*sizeZ + i)*TOTAL_COMPONENTS);

			colors[offset++] = vertices[position];
			colors[offset++] = vertices[position+1];
			colors[offset++] = vertices[position+2];
		}

		return colors;
	}


	public vec3 getPosition()
	{
		return currentPosition;
	}


	public void setPosition(vec3 position)
	{
		this.currentPosition.setValues(position);
	}


	public void setPositionX(float position)
	{
		currentPosition.x = position;
	}


	public void setPositionZ(float position)
	{
		currentPosition.z = position;
	}
}
