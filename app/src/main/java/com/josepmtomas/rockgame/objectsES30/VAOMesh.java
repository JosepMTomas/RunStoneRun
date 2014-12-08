package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.collision.CollisionCylinder;
import com.josepmtomas.rockgame.collision.CollisionSphere;
import com.josepmtomas.rockgame.data.IndexBuffer;
import com.josepmtomas.rockgame.data.VertexBuffer;
import com.josepmtomas.rockgame.programs.BasicShaderProgram;
import com.josepmtomas.rockgame.programs.DeferredSceneShaderProgram;
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

//import static android.opengl.GLES20.*;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 06/08/2014.
 */
public class VAOMesh
{
	private static final String TAG = "VAOMesh";

	protected static final int POSITION_COMPONENTS = 3;
	protected static final int TEXCOORD_COMPONENTS = 2;
	protected static final int NORMAL_COMPONENTS = 3;
	protected static final int TANGENT_COMPONENTS = 4;
	protected static final int COLOR_COMPONENTS = 3;
	protected static final int TOTAL_COMPONENTS =
			POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS + COLOR_COMPONENTS;

	protected static final int POSITION_OFFSET = 0;
	protected static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	protected static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	protected static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;
	protected static final int COLOR_OFFSET = TANGENT_OFFSET + TANGENT_COMPONENTS;

	protected static final int POSITION_BYTE_OFFSET = 0;
	protected static final int TEXCOORD_BYTE_OFFSET = POSITION_BYTE_OFFSET + (POSITION_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int NORMAL_BYTE_OFFSET = TEXCOORD_BYTE_OFFSET + (TEXCOORD_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int TANGENT_BYTE_OFFSET = NORMAL_BYTE_OFFSET + (NORMAL_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int COLOR_BYTE_OFFSET = TANGENT_BYTE_OFFSET + (TANGENT_COMPONENTS * Constants.BYTES_PER_FLOAT);

	protected static final int STRIDE = TOTAL_COMPONENTS * Constants.BYTES_PER_FLOAT;

	//protected VertexBuffer vertexBuffer;
	//protected IndexBuffer indexBuffer;

	protected int numVertices;
	protected int numIndices;

	//protected float[] vertices;
	//protected int[] indices;

	protected float[] positions;
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

	protected int[] vaoHandle = new int[1];
	protected int[] shadowVaoHandle = new int[1];

	protected int numInstances;
	protected int matricesBinding = 0;
	protected int[] uniformHandle = new int[1];
	protected int uniformBlockLocation;
	protected float[] uniformMatrices;
	protected FloatBuffer uniformArray;
	protected FloatBuffer viewProjectionMatrixArray;

	// Collision primitives
	protected List<CollisionSphere> collisionSpheres = new ArrayList<CollisionSphere>();
	protected List<CollisionCylinder> collisionCylinders = new ArrayList<CollisionCylinder>();

	// Shading
	float[] modelMatrix = new float[16];
	float[] viewMatrix;
	float[] projectionMatrix;
	float[] viewProjectionMatrix = new float[16];
	float[] modelViewMatrix = new float[16];
	float[] modelViewProjectionMatrix = new float[16];

	float[] shadowViewProjectionMatrix = new float[16];

	BasicShaderProgram basicShaderProgram;

	// Deferred render
	DeferredSceneShaderProgram deferredSceneShaderProgram;
	private ShadowpassShaderProgram shadowpassShaderProgram;


	public VAOMesh(Context context, int resourceId)
	{
		basicShaderProgram = new BasicShaderProgram(context);
		deferredSceneShaderProgram = new DeferredSceneShaderProgram(context);
		shadowpassShaderProgram = new ShadowpassShaderProgram(context);

		numInstances = 64;

		loadFromFile(context, resourceId);
		initialize();
	}


	private void loadFromFile(Context context, int resourceId)
	{
		try
		{
			InputStream inputStream = context.getResources().openRawResource(resourceId);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String nextLine;

			int positionsOffset = 0;
			int texCoordsOffset = 0;
			int normalsOffset = 0;
			int tangentsOffset = 0;
			int colorsOffset = 0;
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

					positions = new float[POSITION_COMPONENTS * numVertices];
					textureCoordinates = new float[TEXCOORD_COMPONENTS * numVertices];
					normals = new float[NORMAL_COMPONENTS * numVertices];
					tangents = new float[TANGENT_COMPONENTS * numVertices];
					colors = new float[COLOR_COMPONENTS * numVertices];
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
					positions[positionsOffset++] = Float.parseFloat(tokens[1]);
					positions[positionsOffset++] = Float.parseFloat(tokens[2]);
					positions[positionsOffset++] = Float.parseFloat(tokens[3]);

					// Vertex texture coordinate (2 components: X Y)
					textureCoordinates[texCoordsOffset++] = Float.parseFloat(tokens[4]);
					textureCoordinates[texCoordsOffset++] = Float.parseFloat(tokens[5]);

					// Vertex normal (3 components: X Y Z)
					normals[normalsOffset++] = Float.parseFloat(tokens[6]);
					normals[normalsOffset++] = Float.parseFloat(tokens[7]);
					normals[normalsOffset++] = Float.parseFloat(tokens[8]);

					// Vertex tangent (4 components: X Y Z W)
					tangents[tangentsOffset++] = Float.parseFloat(tokens[9]);
					tangents[tangentsOffset++] = Float.parseFloat(tokens[10]);
					tangents[tangentsOffset++] = Float.parseFloat(tokens[11]);
					tangents[tangentsOffset++] = Float.parseFloat(tokens[12]);

					// Vertex colors (3 components: R G B)
					colors[colorsOffset++] = Float.parseFloat(tokens[13]);
					colors[colorsOffset++] = Float.parseFloat(tokens[14]);
					colors[colorsOffset++] = Float.parseFloat(tokens[15]);
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
		//vertexBuffer = new VertexBuffer(vertices);
		//indexBuffer = new IndexBuffer(indices);

		// Build the arrays
		positionsArray = ByteBuffer
				.allocateDirect(positions.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(positions);
		positionsArray.position(0);

		texCoordsArray = ByteBuffer
				.allocateDirect(textureCoordinates.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(textureCoordinates);
		texCoordsArray.position(0);

		normalsArray = ByteBuffer
				.allocateDirect(normals.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(normals);
		normalsArray.position(0);

		tangentsArray = ByteBuffer
				.allocateDirect(tangents.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(tangents);
		tangentsArray.position(0);

		colorsArray = ByteBuffer
				.allocateDirect(colors.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(colors);
		colorsArray.position(0);

		elementsArray = ByteBuffer
				.allocateDirect(indices.length * Constants.BYTES_PER_INT)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer()
				.put(indices);
		elementsArray.position(0);

		// Create and populate the buffer objects
		int[] handle = new int[6];
		glGenBuffers(6, handle, 0);

		glBindBuffer(GL_ARRAY_BUFFER, handle[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsArray.capacity() * Constants.BYTES_PER_FLOAT, positionsArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, handle[1]);
		glBufferData(GL_ARRAY_BUFFER, texCoordsArray.capacity() * Constants.BYTES_PER_FLOAT, texCoordsArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, handle[2]);
		glBufferData(GL_ARRAY_BUFFER, normalsArray.capacity() * Constants.BYTES_PER_FLOAT, normalsArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, handle[3]);
		glBufferData(GL_ARRAY_BUFFER, tangentsArray.capacity() * Constants.BYTES_PER_FLOAT, tangentsArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, handle[4]);
		glBufferData(GL_ARRAY_BUFFER, colorsArray.capacity() * Constants.BYTES_PER_FLOAT, colorsArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle[5]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsArray.capacity() * Constants.BYTES_PER_INT, elementsArray, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, handle[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, handle[1]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, handle[2]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, handle[3]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

		// Vertex colors
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, handle[4]);
		glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle[5]);

		glBindVertexArray(0);

		///////////////////////
		// Create the VAO for the shadow mapping pass
		glGenVertexArrays(1, shadowVaoHandle, 0);
		glBindVertexArray(shadowVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, handle[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle[5]);

		glBindVertexArray(0);

		// Uniform matrices

		uniformMatrices = new float[16 * (numInstances)];

		//setIdentityM(uniformMatrices, 0);
		for(int i=0; i < numInstances; i++)
		{
			setIdentityM(uniformMatrices, i * 16);
			translateM(uniformMatrices, i * 16, (float)i*10f, 0.0f, 0.0f);
		}

		uniformArray = ByteBuffer
				.allocateDirect(uniformMatrices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(uniformMatrices);
		uniformArray.position(0);

		setIdentityM(viewProjectionMatrix, 0);
		viewProjectionMatrixArray = ByteBuffer
				.allocateDirect(16 * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(viewProjectionMatrix);
		viewProjectionMatrixArray.position(0);


		// Create uniform buffer
		glGenBuffers(1, uniformHandle, 0);
		glBindBuffer(GL_UNIFORM_BUFFER, uniformHandle[0]);
		glBufferData(GL_UNIFORM_BUFFER, (numInstances)*16*Constants.BYTES_PER_FLOAT, uniformArray, GL_STREAM_DRAW);
		//glBufferData(GL_UNIFORM_BUFFER, (numInstances+1)*16*Constants.BYTES_PER_FLOAT, null, GL_STREAM_DRAW);

		//glBufferSubData(GL_UNIFORM_BUFFER, 16 * Constants.BYTES_PER_FLOAT, (numInstances)*16*Constants.BYTES_PER_FLOAT, uniformArray);

		uniformBlockLocation = glGetUniformBlockIndex(basicShaderProgram.getProgram(), "Matrices");
		glUniformBlockBinding(basicShaderProgram.getProgram(), uniformBlockLocation, matricesBinding);

	}


	public void reinitialize()
	{
		//vertexBuffer.update(vertices);
	}


	public void bind()
	{

	}


	public void setMatrices(float[] viewMatrix, float[] projectionMatrix, float time)
	{
		this.viewMatrix = viewMatrix;
		this.projectionMatrix = projectionMatrix;

		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, 0f, 10f, 0f);
		//rotateM(modelMatrix, 0, time, 1.0f, 0.0f, 0.0f);

		multiplyMM(viewProjectionMatrix, 0, this.projectionMatrix, 0, this.viewMatrix, 0);
		viewProjectionMatrixArray.position(0);
		viewProjectionMatrixArray.put(viewProjectionMatrix);
		viewProjectionMatrixArray.position(0);
		//glBindBuffer(GL_UNIFORM_BUFFER, uniformHandle[0]);
		//glBufferSubData(GL_UNIFORM_BUFFER, 0, viewProjectionMatrixArray.capacity() * Constants.BYTES_PER_FLOAT, viewProjectionMatrixArray);

		multiplyMM(modelViewMatrix, 0, this.viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, this.projectionMatrix, 0, modelViewMatrix, 0);
	}


	public void draw()
	{
		basicShaderProgram.useProgram();

		basicShaderProgram.setUniforms(modelMatrix, viewProjectionMatrix, modelViewProjectionMatrix);

		glBindBufferRange(GL_UNIFORM_BUFFER, matricesBinding, uniformHandle[0], 0, (numInstances)*16*Constants.BYTES_PER_FLOAT);

		glBindVertexArray(vaoHandle[0]);
		//glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
		glDrawElementsInstanced(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0, numInstances);
	}


	public void drawShadowPass(float[] view, float[] projection)
	{
		float[] temp = new float[16];
		float[] MV = new float[16];

		setIdentityM(temp, 0);
		translateM(temp, 0, 0f, 10f, 0f);

		multiplyMM(MV, 0, view, 0, temp, 0);
		multiplyMM(shadowViewProjectionMatrix, 0, projection, 0, MV, 0);

		//multiplyMM(shadowViewProjectionMatrix, 0, projection, 0, view, 0);

		shadowpassShaderProgram.useProgram();
		shadowpassShaderProgram.setUniforms(shadowViewProjectionMatrix);

		glBindVertexArray(shadowVaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void drawDeferred()
	{
		deferredSceneShaderProgram.useProgram();
		deferredSceneShaderProgram.setUniforms(modelMatrix, modelViewMatrix, modelViewProjectionMatrix);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}


	public void delete()
	{

	}
}
