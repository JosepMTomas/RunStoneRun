package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;
import android.content.res.Resources;
import android.util.FloatMath;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.collision.CollisionCylinder;
import com.josepmtomas.rockgame.collision.CollisionSphere;
import com.josepmtomas.rockgame.programs.PlayerRockShaderProgram;
import com.josepmtomas.rockgame.programs.ShadowpassShaderProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 12/08/2014.
 */
public class PlayerRock
{
	private static final String TAG = "PlayerRock";

		// Mesh constants
		private static final int POSITION_COMPONENTS = 3;
		private static final int TEXCOORD_COMPONENTS = 2;
		private static final int NORMAL_COMPONENTS = 3;
		private static final int TANGENT_COMPONENTS = 4;
		private static final int COLOR_COMPONENTS = 3;

		private static final int STRIDE = POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS + COLOR_COMPONENTS;
		private static final int BYTE_STRIDE = STRIDE * Constants.BYTES_PER_FLOAT;

		private static final int POSITION_OFFSET = 0;
		private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
		private static final int NORMAL_OFFSET = TEXCOORD_OFFSET +TEXCOORD_COMPONENTS;
		private static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;
		private static final int COLOR_OFFSET = TANGENT_OFFSET + TANGENT_COMPONENTS;

		private static final int POSITION_BYTE_OFFSET = 0;
		private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * Constants.BYTES_PER_FLOAT;
		private static final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * Constants.BYTES_PER_FLOAT;
		private static final int TANGENT_BYTE_OFFSET = TANGENT_OFFSET * Constants.BYTES_PER_FLOAT;
		private static final int COLOR_BYTE_OFFSET = COLOR_OFFSET * Constants.BYTES_PER_FLOAT;

		// Mesh definition
		protected int numVertices;
		protected int numIndices;

		private float[] vertices;
		private int[] indices;

		private FloatBuffer verticesBuffer;
		private IntBuffer elementsBuffer;

		private int[] vboHandles = new int[2];
		private int[] vaoHandle = new int[1];
		private int[] shadowVaoHandle = new int[1];

	/*protected float[] positions;
	protected float[] textureCoordinates;
	protected float[] normals;
	protected float[] tangents;
	protected float[] colors;

	protected int[] indices;

	protected FloatBuffer positionsArray;
	protected FloatBuffer texCoordsArray;
	protected FloatBuffer normalsArray;
	protected FloatBuffer tangentsArray;
	protected FloatBuffer colorsArray;
	protected IntBuffer elementsArray;

	protected int[] vboHandles = new int[6];
	protected int[] vaoHandle = new int[1];*/

		// Mesh collision
		List<CollisionSphere> collisionSpheres = new ArrayList<CollisionSphere>();
		List<CollisionCylinder> collisionCylinders = new ArrayList<CollisionCylinder>();

		// Shader program
		PlayerRockShaderProgram playerRockShaderProgram;
		ShadowpassShaderProgram shadowpassShaderProgram;

		private float[] rotationVector = new float[4];
		private float[] vectorX = {1.0f, 0.0f, 0.0f, 1.0f};
		private float[] vectorY = {0.0f, 1.0f, 0.0f, 1.0f};
		private float[] modelMatrix = new float[16];
		private float[] rotationMatrix = new float[16];

		private float[] modelViewMatrix = new float[16];
		private float[] viewProjectionMatrix;
		private float[] modelViewProjectionMatrix = new float[16];
		private float[] shadowMVPMatrix = new float[16];

		private float rotationX = 0;
		private float rotationY = 0;
		private float rotationYtemp = 0;

		private float rockRadius = 10.0f;
		private float rockLength = (float)Math.PI * 2.0f * rockRadius;
		private float rockAngleLength = rockLength / 360.0f;
		//private float displacement = 0.0f;
		private float currentSpeed = 10.0f;
		private float[] initialDirection = {0.0f, 0.0f, 1.0f, 1.0f};
		private float[] currentDirection = {0.0f, 0.0f, 1.0f, 1.0f};
		private float[] displacement = new float[4];

		private PlayeRockState currentState = PlayeRockState.MOVING_FORWARD;


		public PlayerRock(Context context)
		{
			playerRockShaderProgram = new PlayerRockShaderProgram(context);
			shadowpassShaderProgram = new ShadowpassShaderProgram(context);

			load(context, R.raw.spherified_cube2);
		}


	private void load(Context context, int resourceId)
	{
		try
		{
			InputStream inputStream = context.getResources().openRawResource(resourceId);
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
					// Get the number of vertices and initialize its array
					numVertices = Integer.parseInt(tokens[1]);

					vertices = new float[STRIDE * numVertices];
				}
				if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the indices array
					numIndices = Integer.parseInt(tokens[1]);
					indices = new int[3 * numIndices];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read a vertex: <position(3)> <uvs(2)> <normal(3)> <tangent(4)>

					// Vertex position (3 components: X Y Z)
					vertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Vertex texture coordinate (2 components: X Y)
					vertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Vertex normal (3 components: X Y Z)
					vertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// Vertex tangent (4 components: X Y Z W)
					vertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[12]);

					// Vertex colors (3 components: R G B)
					vertices[verticesOffset++] = Float.parseFloat(tokens[13]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[14]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[15]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					indices[elementsOffset++] = Integer.parseInt(tokens[1]);
					indices[elementsOffset++] = Integer.parseInt(tokens[2]);
					indices[elementsOffset++] = Integer.parseInt(tokens[3]);
				}
				else if(tokens[0].equals("COLLISION_SPHERE"))
				{
					// Read a collision sphere (center(X Y Z) & radius)
					CollisionSphere collisionSphere = new CollisionSphere(
							new vec3(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3])),
							Float.parseFloat(tokens[4]));

					collisionSpheres.add(collisionSphere);
				}
				else if(tokens[0].equals("COLLISION_CYLINDER"))
				{
					// Read a collision cylinder ( bottom(X Y Z) top(X Y Z) & radius)
					CollisionCylinder collisionCylinder = new CollisionCylinder(
							new vec3(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3])),
							new vec3(
									Float.parseFloat(tokens[4]),
									Float.parseFloat(tokens[5]),
									Float.parseFloat(tokens[6])),
							Float.parseFloat(tokens[7]));

					collisionCylinders.add(collisionCylinder);
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not open resource: " + resourceId, e);
		}
		catch (Resources.NotFoundException nfe)
		{
			throw new RuntimeException("Resource not found: " + resourceId, nfe);
		}
	}


	public void initialize()
	{
		// Build the arrays
		verticesBuffer = ByteBuffer
				.allocateDirect(vertices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(indices.length * Constants.BYTES_PER_INT)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer()
				.put(indices);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * Constants.BYTES_PER_INT, elementsBuffer, GL_STATIC_DRAW);

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

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, BYTE_STRIDE, COLOR_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);

		/******************************************************************************************/

		// Create the VAO for the shadow mapping pass
		glGenVertexArrays(1, shadowVaoHandle, 0);
		glBindVertexArray(shadowVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	public void update(float[] viewProjectionMatrix)
	{
		this.viewProjectionMatrix = viewProjectionMatrix;
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

		rotationX -= currentSpeed;

		//displacement = rockAngleLength * currentSpeed;
		operations.multiplyVF(displacement, 0, currentDirection, 0, (rockAngleLength * currentSpeed));
		//Log.d(TAG, "CurrentDirection = (" + currentDirection[0] + ", " + currentDirection[1] + ", " + currentDirection[2] + ")");
		//Log.d(TAG, "Displacement = (" + displacement[0] + ", " + displacement[1] + ", " + displacement[2] + ")");

		setIdentityM(modelMatrix, 0);
		//setIdentityM(rotationMatrix, 0);

		//rotateM(rotationMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);

		//rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f);
		translateM(modelMatrix, 0, 0.0f, 10.0f, 0.0f);
		//rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f);
		//rotateM(modelMatrix, 0, rotationX, rotationVector[0], rotationVector[1], rotationVector[2]);
		rotateM(modelMatrix, 0, rotationY, 0f, 1f, 0f);
		rotateM(modelMatrix, 0, rotationX, 1f, 0f, 0f);

		multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
	}


	public void drawShadowMap(float[] view, float[] projection)
	{
		multiplyMM(modelViewMatrix, 0, view, 0, modelMatrix, 0);
		multiplyMM(shadowMVPMatrix, 0, projection, 0, modelViewMatrix, 0);

		shadowpassShaderProgram.useProgram();
		shadowpassShaderProgram.setUniforms(shadowMVPMatrix);

		glBindVertexArray(shadowVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void draw(float[] view, float[] projection)
	{
		float[] tempMV = new float[16];
		float[] tempMVP = new float[16];

		multiplyMM(tempMV, 0, view, 0, modelMatrix, 0);
		multiplyMM(tempMVP, 0, projection, 0, tempMV, 0);

		playerRockShaderProgram.useProgram();
		playerRockShaderProgram.setUniforms(rotationMatrix, tempMVP, 0, new vec3(0.0f));

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void draw(int textureID, vec3 eyePosition)
	{
		playerRockShaderProgram.useProgram();
		playerRockShaderProgram.setUniforms(rotationMatrix, modelViewProjectionMatrix, textureID, eyePosition);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public float[] getDisplacement()
	{
		return displacement;
	}


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
}
