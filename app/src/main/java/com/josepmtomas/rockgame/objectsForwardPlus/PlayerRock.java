package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import static com.josepmtomas.rockgame.Constants.*;
import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.collision.CollisionCylinder;
import com.josepmtomas.rockgame.collision.CollisionSphere;
import com.josepmtomas.rockgame.objectsES30.PlayeRockState;
import com.josepmtomas.rockgame.programsForwardPlus.DepthPrePassProgram;
import com.josepmtomas.rockgame.programsForwardPlus.PlayerRockProgram;
import com.josepmtomas.rockgame.programsForwardPlus.ShadowPassProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 05/09/2014.
 */
public class PlayerRock
{
	private static final String TAG = "PlayerRock";

	// Mesh constants
	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
	private static final int TANGENT_COMPONENTS = 4;

	private static final int STRIDE = POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS;
	private static final int BYTE_STRIDE = STRIDE * BYTES_PER_FLOAT;

	private static final int POSITION_OFFSET = 0;
	private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	private static final int NORMAL_OFFSET = TEXCOORD_OFFSET +TEXCOORD_COMPONENTS;
	private static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;

	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * BYTES_PER_FLOAT;
	private static final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * BYTES_PER_FLOAT;
	private static final int TANGENT_BYTE_OFFSET = TANGENT_OFFSET * BYTES_PER_FLOAT;

	// Main geometry definition
	private float[] vertices;
	private short[] elements;
	public int numElementsToDraw;
	private int[] vboHandles = new int[2];
	public int[] vaoHandle = new int[1];

	// Shadow mesh geometry definition
	private float[] shadowVertices;
	private short[] shadowElements;
	public int numShadowElementsToDraw;
	private int[] shadowVboHandles = new int[2];
	public int[] shadowVaoHandle = new int[1];

	// Shader program
	DepthPrePassProgram depthPrePassProgram;
	ShadowPassProgram shadowPassProgram;
	PlayerRockProgram playerRockProgram;

	// Position
	public float previousPositionY = 10f;
	public float currentPositionY = 10f;

	// Shadow map generation matrices
	private float[] lightViewProjection;
	private float[] lightModelViewProjection = new float[16];

	// Normal rendering matrices
	private float[] rotationVector = new float[4];
	private float[] vectorX = {1.0f, 0.0f, 0.0f, 1.0f};
	private float[] vectorY = {0.0f, 1.0f, 0.0f, 1.0f};
	private float[] model = new float[16];
	private float[] rotationMatrix = new float[16];

	private float[] modelView = new float[16];
	private float[] viewProjection;
	private float[] modelViewProjection = new float[16];

	// Reflection matrices
	private float[] proxyModel = new float[16];
	private float[] proxyModelViewProjection = new float[16];

	private float rotationX = 0;
	private float rotationY = 0;
	private float rotationYtemp = 0;

	public float rockRadius = 10.0f;
	private float rockLength = (float)Math.PI * 2.0f * rockRadius;
	private float rockAngleLength = rockLength / 360.0f;
	//private float displacement = 0.0f;
	public float currentSpeed = 0.0f;
	private float[] initialDirection = {0.0f, 0.0f, 1.0f, 1.0f};
	private float[] currentDirection = {0.0f, 0.0f, 1.0f, 1.0f};
	private float[] displacement = {0f, 0f, 0f, 0f};

	// "Physics"
	private float initialForce = 0f;
	private float currentDisplacement = 0f;

	// State
	public float scoreMultiplier = 1.0f;
	public int state = PLAYER_ROCK_MOVING;

	private PlayeRockState currentState = PlayeRockState.MOVING_FORWARD;

	private LightInfo lightInfo;

	private Context context;

	/**
	 * Creates a new player rock
	 * @param context current application context
	 */
	public PlayerRock(Context context, LightInfo lightInfo)
	{
		this.context = context;
		this.lightInfo = lightInfo;

		currentPositionY = 10f;

		depthPrePassProgram = new DepthPrePassProgram(context);
		shadowPassProgram = new ShadowPassProgram(context);
		playerRockProgram = new PlayerRockProgram(context);

		//load(context, R.raw.spherified_cube2);

		glGenVertexArrays(1, vaoHandle, 0);

		load("models/player_rock.vbm");
		loadShadow("models/player_rock.vbm");
	}


	private void load(String fileName)
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
					vertices = new float[STRIDE * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					numElementsToDraw = numElements * 3;
					elements = new short[numElementsToDraw];
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

		Log.e(TAG, "numVertices = " + vertices.length + ", numElements = " + elements.length);

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
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER,  verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
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
	}


	private void loadShadow(String shadowFileName)
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


	public void updateLightMatrices(float[] lightViewProjection)
	{
		this.lightViewProjection = lightViewProjection;
	}


	public void update(float[] viewProjectionMatrix, float deltaTime)
	{
		if(state == PLAYER_ROCK_RECOVERING)
		{
			previousPositionY = currentPositionY;

			initialForce = initialForce - (9.8f * deltaTime);
			currentPositionY  = currentPositionY + initialForce;

			if(currentPositionY < 10f)
			{
				currentPositionY = 10f;
				initialForce = -initialForce * 0.9f;
			}

			if(previousPositionY == currentPositionY)
			{
				state = PLAYER_ROCK_MOVING;
				scoreMultiplier = 1.0f;
			}
		}

		currentSpeed = Math.min(MAX_PLAYER_SPEED, currentSpeed + 10f);
		//currentSpeed = 0f;
		//currentSpeed += 1f;

		this.viewProjection = viewProjectionMatrix;
		//currentSpeed += 0.1;
		//Log.d(TAG, "Speed: " + currentSpeed);

		switch(currentState)
		{
			case TURNING_LEFT:
				rotationY += 2.5f;
				if(rotationY > 75f)
				{
					rotationY = 75f;
					currentState = PlayeRockState.MOVING_LEFT;
				}
				break;

			case TURNING_RIGHT:
				rotationY -= 2.5f;
				if(rotationY < -75f)
				{
					rotationY = -75f;
					currentState = PlayeRockState.MOVING_RIGHT;
				}
				break;

			case RETURNING_CENTER:
				if(rotationY > 0) rotationY -= 2.5f;
				if(rotationY < 0) rotationY += 2.5f;
				if(rotationY == 0) currentState = PlayeRockState.MOVING_FORWARD;
				break;

			default:
				break;
		}

		setIdentityM(rotationMatrix, 0);
		/**rotateM(rotationMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f);
		 multiplyMV(rotationVector, 0, rotationMatrix, 0, vectorX, 0);
		 multiplyMV(currentDirection, 0, rotationMatrix, 0, initialDirection, 0);
		 rotateM(rotationMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);**/
		rotateM(rotationMatrix, 0, rotationY, 0f, 1f, 0f);
		multiplyMV(currentDirection, 0, rotationMatrix, 0, initialDirection, 0);
		rotateM(rotationMatrix, 0, rotationX, 1f, 0f, 0f);


		//Log.e(TAG, "RotationVector (" + rotationVector[0] + ", " + rotationVector[1] + ", " + rotationVector[2] + ")");

		rotationX -= (currentSpeed * deltaTime);

		//displacement = rockAngleLength * currentSpeed;
		//Log.d("RockAngleLength", "= " + rockAngleLength);
		//Log.d("Displacement quantity", "= " + (rockAngleLength * currentSpeed * deltaTime));
		operations.multiplyVF(displacement, 0, currentDirection, 0, (rockAngleLength * currentSpeed * deltaTime));
		//Log.d(TAG, "CurrentDirection = (" + currentDirection[0] + ", " + currentDirection[1] + ", " + currentDirection[2] + ")");
		//Log.d(TAG, "Displacement = (" + displacement[0] + ", " + displacement[1] + ", " + displacement[2] + ")");

		setIdentityM(model, 0);
		//setIdentityM(rotationMatrix, 0);

		//rotateM(rotationMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);

		//rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f);
		translateM(model, 0, 0.0f, currentPositionY, 0.0f);
		//scaleM(model, 0, 2f, 2f, 2f);
		//rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f);
		//rotateM(modelMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);
		rotateM(model, 0, rotationY, 0f, 1f, 0f);
		rotateM(model, 0, rotationX, 1f, 0f, 0f);

		multiplyMM(modelViewProjection, 0, viewProjectionMatrix, 0, model, 0);

		// TODO: mirrored proxy
		setIdentityM(proxyModel, 0);
		translateM(proxyModel, 0, 0.0f, -10.0f, 0.0f);
		rotateM(proxyModel, 0, rotationY, 0f, 1f, 0f);
		rotateM(proxyModel, 0, -rotationX + 180f, 1f, 0f, 0f);
	}


	public void drawDepthPrePass()
	{
		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		depthPrePassProgram.useProgram();
		depthPrePassProgram.setUniforms(modelViewProjection);

		//glBindVertexArray(positionsVaoHandle[0]);
		//glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void drawShadowMap()
	{
		multiplyMM(lightModelViewProjection, 0, lightInfo.viewProjection, 0, model, 0);

		shadowPassProgram.useProgram();
		shadowPassProgram.setUniforms(lightModelViewProjection);

		glBindVertexArray(shadowVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0);
	}


	public void drawReflectionProxy(float[] shadowMatrix, int shadowMapSampler)
	{
		multiplyMM(proxyModelViewProjection, 0, viewProjection, 0, proxyModel, 0);

		// TODO:
		playerRockProgram.useProgram();
		playerRockProgram.setUniforms(proxyModel, proxyModelViewProjection, shadowMatrix, shadowMapSampler);

		//glBindVertexArray(vaoHandle[0]);
		//glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void draw(float[] shadowMatrix, int shadowMapSampler)
	{
		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		playerRockProgram.useProgram();
		playerRockProgram.setUniforms(model, modelViewProjection, shadowMatrix, shadowMapSampler);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, numElementsToDraw, GL_UNSIGNED_SHORT, 0);
	}


	public float[] getDisplacement()
	{
		return displacement;
	}


	public vec3 getDisplacementVec3()
	{
		return new vec3(displacement);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// Control
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void turnLeft()
	{
		currentState = PlayeRockState.TURNING_LEFT;
	}


	public void turnRight()
	{
		currentState = PlayeRockState.TURNING_RIGHT;
	}


	public void releaseTouch()
	{
		currentState = PlayeRockState.RETURNING_CENTER;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// Collision
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void hit()
	{
		if(state == PLAYER_ROCK_MOVING)
		{
			currentSpeed = currentSpeed * 0.5f;
			initialForce = 3f;
			state = PLAYER_ROCK_RECOVERING;
			scoreMultiplier = 0f;
		}
		else if(state == PLAYER_ROCK_RECOVERING)
		{
			// nothing at this point
		}

		//currentSpeed = Math.max(0f, currentSpeed - 10f);
	}

	public void deleteGL()
	{
		playerRockProgram.deleteProgram();
		depthPrePassProgram.deleteProgram();
		shadowPassProgram.deleteProgram();
	}
}
