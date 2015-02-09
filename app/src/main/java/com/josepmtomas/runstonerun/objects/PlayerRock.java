package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import static com.josepmtomas.runstonerun.Constants.*;

import com.josepmtomas.runstonerun.GameActivity;
import com.josepmtomas.runstonerun.algebra.operations;
import com.josepmtomas.runstonerun.algebra.vec3;
import com.josepmtomas.runstonerun.programs.PlayerRockProgram;
import com.josepmtomas.runstonerun.programs.ShadowPassProgram;
import com.josepmtomas.runstonerun.util.TextureHelper;

import static com.josepmtomas.runstonerun.algebra.operations.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
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
 * Created by Josep M. Tomas on 05/09/2014.
 * @author Josep
 */

public class PlayerRock
{
	//private static final String TAG = "PlayerRock";

	// Player rock movement states
	private static final int MOVING_FORWARD = 0;
	private static final int MOVING_LEFT = 1;
	private static final int MOVING_RIGHT = 2;
	private static final int TURNING_LEFT = 3;
	private static final int TURNING_RIGHT = 4;
	private static final int RETURNING_CENTER = 5;

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

	private float notVisibleSpeed = 500f;

	// Textures
	private final int diffuseTexture;
	private final int normalTexture;

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

	// Reflection mesh geometry definition
	private float[] reflectionVertices;
	private short[] reflectionElements;
	public int numReflectionElementsToDraw;
	private int[] reflectionVboHandles = new int[2];
	public int[] reflectionVaoHandle = new int[1];

	// Shader program
	ShadowPassProgram shadowPassProgram;
	PlayerRockProgram playerRockProgram;

	// Player rock state
	private float playerRockTimer = 0f;
	private static final float PLAYER_ROCK_APPEAR_TIME = 1f;
	private static final float PLAYER_ROCK_DISAPPEAR_TIME = 1f;
	private float playerRockTimerPercent = 0f;
	private float currentPositionZ = 0f;

	// Position
	public float previousPositionY = 10f;
	public float currentPositionY = 10f;

	// Shadow map generation matrices
	private float[] lightModelViewProjection = new float[16];

	// Normal rendering matrices
	//private float[] rotationVector = new float[4];
	//private float[] vectorX = {1.0f, 0.0f, 0.0f, 1.0f};
	//private float[] vectorY = {0.0f, 1.0f, 0.0f, 1.0f};
	private float[] model = new float[16];
	private float[] rotationMatrix = new float[16];

	private float[] viewProjection;
	private float[] modelViewProjection = new float[16];

	// Reflection matrices
	private float[] proxyModel = new float[16];
	private float[] proxyModelViewProjection = new float[16];

	private float rotationX = 0;
	private float rotationY = 0;

	public float rockRadius = 10.0f;
	private float rockLength = (float)Math.PI * 2.0f * rockRadius;
	private float rockAngleLength = rockLength / 360.0f;
	public float currentSpeed = 0.0f;
	private float[] initialDirection = {0.0f, 0.0f, 1.0f, 1.0f};
	private float[] currentDirection = {0.0f, 0.0f, 1.0f, 1.0f};
	private float[] displacement = {0f, 0f, 0f, 0f};

	// "Physics"
	private float initialForce = 0f;

	// State
	public float scoreMultiplier = 1.0f;
	public int previousGroundPatchState = GROUND_PATCH_GROUND;
	public int state = PLAYER_ROCK_NOT_VISIBLE;
	public int lastObjectTypeHit = 0;
	private float recoverTimer = 0f;

	private int currentState = MOVING_FORWARD;

	private SmokeParticleSystem smokeParticleSystem;
	private WaterParticleSystem waterParticleSystem;

	private LightInfo lightInfo;

	private GameActivity parent;
	private Context context;


	public PlayerRock(GameActivity parent, LightInfo lightInfo)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.lightInfo = lightInfo;

		currentPositionY = 10f;

		shadowPassProgram = new ShadowPassProgram(context);
		playerRockProgram = new PlayerRockProgram(context);

		smokeParticleSystem = new SmokeParticleSystem(context);
		waterParticleSystem = new WaterParticleSystem(context);

		glGenVertexArrays(1, vaoHandle, 0);

		load("models/player_rock.vbm");
		loadShadow("models/player_rock.vbm");
		loadReflection("models/player_rock_reflection.vbm");

		diffuseTexture = TextureHelper.loadETC2Texture(context, "textures/player_rock/diffuse_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		normalTexture = TextureHelper.loadETC2Texture(context, "textures/player_rock/normal_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
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
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]) * -1.0f;

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


	private void loadReflection(String reflectionFileName)
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
					reflectionVertices = new float[STRIDE * numVertices];
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
					reflectionVertices[verticesOffset++] = Float.parseFloat(tokens[5]);

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


	public void update(float[] viewProjectionMatrix, float deltaTime, int groundPatchType)
	{
		smokeParticleSystem.update(viewProjectionMatrix, currentPositionZ, displacement, deltaTime);
		waterParticleSystem.update(viewProjectionMatrix, currentPositionZ, displacement, deltaTime);

		if(previousGroundPatchState == GROUND_PATCH_GROUND && groundPatchType != GROUND_PATCH_GROUND)
		{
			smokeParticleSystem.setEmitterEnabled(false);
			waterParticleSystem.setEmitterEnabled(true);

		}
		else if(previousGroundPatchState != GROUND_PATCH_GROUND && groundPatchType == GROUND_PATCH_GROUND)
		{
			smokeParticleSystem.setEmitterEnabled(true);
			waterParticleSystem.setEmitterEnabled(false);
		}

		previousGroundPatchState = groundPatchType;

		if(state == PLAYER_ROCK_NOT_VISIBLE)
		{
			float speedDifference = (notVisibleSpeed - currentSpeed) * deltaTime;
			currentSpeed += speedDifference;
			currentPositionZ = 50f;
			currentPositionY = 10f;
			initialForce = 0f;
		}
		else if(state == PLAYER_ROCK_APPEARING)
		{
			playerRockTimer += deltaTime;
			playerRockTimerPercent = playerRockTimer / PLAYER_ROCK_APPEAR_TIME;

			if(playerRockTimer >= PLAYER_ROCK_APPEAR_TIME)
			{
				playerRockTimer = 0f;
				playerRockTimerPercent = 1f;
				state = PLAYER_ROCK_MOVING;
			}

			currentPositionZ = lerp(50f, 0f, playerRockTimerPercent);
			currentSpeed = Math.min(MAX_PLAYER_SPEED, currentSpeed + PLAYER_SPEED_INCREMENT * deltaTime);
		}
		else if(state == PLAYER_ROCK_DISAPPEARING)
		{
			playerRockTimer += deltaTime;
			playerRockTimerPercent = playerRockTimer / PLAYER_ROCK_DISAPPEAR_TIME;

			if(playerRockTimer >= PLAYER_ROCK_DISAPPEAR_TIME)
			{
				playerRockTimer = 0f;
				playerRockTimerPercent = 1f;
				state = PLAYER_ROCK_NOT_VISIBLE;
				smokeParticleSystem.setEnabled(false);
			}

			currentPositionY = lerp(currentPositionY, 10f, playerRockTimerPercent);
			currentPositionZ = lerp(0f, 50f, playerRockTimerPercent);
			currentSpeed = lerp(currentSpeed, 500f, playerRockTimerPercent);
		}
		else if(state == PLAYER_ROCK_BOUNCING)
		{
			previousPositionY = currentPositionY;

			initialForce = initialForce - (9.8f * deltaTime);
			currentPositionY  = currentPositionY + initialForce;

			if(currentPositionY < 10f)
			{
				currentPositionY = 10f;
				initialForce = -initialForce * 0.75f;
			}

			if(currentPositionY > 80f)
			{
				currentPositionY = 80f;
				initialForce = -initialForce * 0.5f;
			}

			if(previousPositionY == currentPositionY)
			{
				state = PLAYER_ROCK_RECOVERING;
			}

			recoverTimer += deltaTime;
		}
		else if(state == PLAYER_ROCK_RECOVERING)
		{
			recoverTimer += deltaTime;

			if(recoverTimer >= PLAYER_RECOVERING_TIME)
			{
				state = PLAYER_ROCK_MOVING;
				scoreMultiplier = 1.0f;
			}
		}

		if(state == PLAYER_ROCK_MOVING)
		{
			currentSpeed = Math.min(MAX_PLAYER_SPEED, currentSpeed + PLAYER_SPEED_INCREMENT * deltaTime);
		}
		//currentSpeed = 0f;
		//currentSpeed += 1f;

		this.viewProjection = viewProjectionMatrix;
		//currentSpeed += 0.1;
		//Log.d(TAG, "Speed: " + currentSpeed);

		switch(currentState)
		{
			case TURNING_LEFT:
				rotationY += 90f * deltaTime;
				if(rotationY > 45f)
				{
					rotationY = 45f;
					currentState = MOVING_LEFT;
				}
				break;

			case TURNING_RIGHT:
				rotationY -= 90f * deltaTime;
				if(rotationY < -45f)
				{
					rotationY = -45f;
					currentState = MOVING_RIGHT;
				}
				break;

			case RETURNING_CENTER:
				if(rotationY > 0) rotationY = Math.max(rotationY - (150f * deltaTime), 0f);
				if(rotationY < 0) rotationY = Math.min(rotationY + (150f * deltaTime), 0f);
				if(rotationY == 0) currentState = MOVING_FORWARD;
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

		//rotationX -= (currentSpeed * deltaTime);
		if(state == PLAYER_ROCK_DISAPPEARING)
		{
			rotationX -= lerp(currentSpeed * deltaTime, 0f, playerRockTimerPercent);
		}
		else
		{
			rotationX -= lerp(currentSpeed * 2f * deltaTime, currentSpeed * deltaTime, playerRockTimerPercent);
		}

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
		translateM(model, 0, 0.0f, currentPositionY, currentPositionZ);
		//scaleM(model, 0, 2f, 2f, 2f);
		//rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f);
		//rotateM(modelMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);
		rotateM(model, 0, rotationY, 0f, 1f, 0f);
		rotateM(model, 0, rotationX, 1f, 0f, 0f);

		multiplyMM(modelViewProjection, 0, viewProjectionMatrix, 0, model, 0);

		setIdentityM(proxyModel, 0);
		if(groundPatchType == GROUND_PATCH_RIVER_MIDDLE)
			translateM(proxyModel, 0, 0.0f, -currentPositionY - 10f, currentPositionZ);
		else
			translateM(proxyModel, 0, 0.0f, -currentPositionY, currentPositionZ);
		rotateM(proxyModel, 0, rotationY, 0f, 1f, 0f);
		rotateM(proxyModel, 0, -rotationX, 1f, 0f, 0f); // +180f
	}


	/*public void drawDepthPrePass()
	{
		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		depthPrePassProgram.useProgram();
		depthPrePassProgram.setUniforms(modelViewProjection);

		//glBindVertexArray(positionsVaoHandle[0]);
		//glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}*/


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
		//if(viewProjection == null) Log.w("PlayerRock", "viewProjection is null");
		//if(proxyModel == null) Log.w("PlayerRock", "proxyModel is null");
		multiplyMM(proxyModelViewProjection, 0, viewProjection, 0, proxyModel, 0);

		playerRockProgram.useProgram();
		playerRockProgram.setUniforms(proxyModel, proxyModelViewProjection, shadowMatrix, shadowMapSampler, diffuseTexture, normalTexture);

		glBindVertexArray(reflectionVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0);
	}


	public void draw(float[] shadowMatrix, int shadowMapSampler)
	{
		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		playerRockProgram.useProgram();
		playerRockProgram.setUniforms(model, modelViewProjection, shadowMatrix, shadowMapSampler, diffuseTexture, normalTexture);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, numElementsToDraw, GL_UNSIGNED_SHORT, 0);

		glDepthMask(false);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		smokeParticleSystem.draw();
		waterParticleSystem.draw();

		glDisable(GL_BLEND);
		glDepthMask(true);
	}


	public vec3 getDisplacementVec3()
	{
		return new vec3(displacement);
	}


	public void setAppearing()
	{
		state = PLAYER_ROCK_APPEARING;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// Control
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void setNotVisibleSpeed(boolean isFast)
	{
		if(isFast)
		{
			notVisibleSpeed = MAX_PLAYER_SPEED;
		}
		else
		{
			notVisibleSpeed = 500f;
		}
	}

	public void turnLeft()
	{
		currentState = TURNING_LEFT;
	}


	public void turnRight()
	{
		currentState = TURNING_RIGHT;
	}


	public void releaseTouch()
	{
		currentState = RETURNING_CENTER;
	}

	public void incrementMultiplier()
	{
		scoreMultiplier = Math.min(9.9f, scoreMultiplier + 0.1f);
	}

	public void newGame(int groundPatchType)
	{
		scoreMultiplier = 1.0f;
		currentSpeed = 0f;
		currentPositionY = 10f;
		initialForce = 0f;

		if(groundPatchType == GROUND_PATCH_RIVER_MIDDLE)
		{
			smokeParticleSystem.setEnabled(true);
			smokeParticleSystem.setEmitterEnabled(false);
			waterParticleSystem.setEnabled(true);
			waterParticleSystem.setEmitterEnabled(true);
		}
		else
		{
			smokeParticleSystem.setEnabled(true);
			smokeParticleSystem.setEmitterEnabled(true);
			waterParticleSystem.setEnabled(true);
			waterParticleSystem.setEmitterEnabled(false);
		}
	}

	public void endGame()
	{
		state = PLAYER_ROCK_DISAPPEARING;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Collision
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void hit(int type)
	{
		if(state == PLAYER_ROCK_MOVING)
		{
			recoverTimer = 0f;
			currentSpeed = Math.max(currentSpeed * 0.5f, 500f);
			initialForce = 3f;
			state = PLAYER_ROCK_BOUNCING;
			scoreMultiplier = 0f;
			lastObjectTypeHit = type;

			if(type == 0)
			{
				parent.playImpactRockOnTreeSound();
				parent.playTreeFallingSound();
			}
			else
			{
				parent.playImpactRockOnRockSound();
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// State save / load
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void saveState(FileOutputStream outputStream) throws IOException
	{
		String stateString = "PLAYER_ROCK "
				+ currentSpeed + " "
				+ currentPositionY + " "
				+ rotationX + " "
				+ rotationY + " "
				+ scoreMultiplier + " "
				+ state + " "
				+ currentState + " "
				+ playerRockTimer + " "
				+ initialForce + "\n";
		outputStream.write(stateString.getBytes());

		smokeParticleSystem.saveState(outputStream);
		waterParticleSystem.saveState(outputStream);
	}


	public void loadState(BufferedReader bufferedReader) throws IOException
	{
		String line;
		String[] tokens;

		line = bufferedReader.readLine();
		tokens = line.split(" ");

		// Read properties
		currentSpeed = Float.parseFloat(tokens[1]);
		currentPositionY = Float.parseFloat(tokens[2]);
		rotationX = Float.parseFloat(tokens[3]);
		rotationY = Float.parseFloat(tokens[4]);
		scoreMultiplier = Float.parseFloat(tokens[5]);
		state = Integer.parseInt(tokens[6]);
		currentState = Integer.parseInt(tokens[7]);
		playerRockTimer = Float.parseFloat(tokens[8]);
		initialForce = Float.parseFloat(tokens[9]);

		smokeParticleSystem.loadState(bufferedReader);
		waterParticleSystem.loadState(bufferedReader);
	}
}