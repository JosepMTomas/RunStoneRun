package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.programs.MirrorShaderProgram;
import com.josepmtomas.rockgame.programs.ShadowpassShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 17/08/2014.
 */
public class Mirror
{
	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;

	private static final int POSITION_OFFSET = 0;
	private static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	private static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	private static final int STRIDE = POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS;

	private static final int POSITION_BYTE_OFFSET = POSITION_OFFSET * Constants.BYTES_PER_FLOAT;
	private static final int TEXCOORD_BYTE_OFFSET = TEXCOORD_OFFSET * Constants.BYTES_PER_FLOAT;
	private static final int NORMAL_BYTE_OFFSET = NORMAL_OFFSET * Constants.BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = STRIDE * Constants.BYTES_PER_FLOAT;

	// Mesh definition arrays
	private float[] normal = new float[4];
	private float[] baseNormal = {0.0f, 0.0f, 1.0f, 1.0f};
	private float[] position = new float[4];
	private float[] basePosition = {0.0f, 0.0f, 0.0f, 1.0f};
	private float[] vertices;
	private short[] indices;

	// Mesh definition buffers
	private FloatBuffer verticesBuffer;
	private ShortBuffer elementsBuffer;

	// Transform
	private float[] translation = {0.0f, 0.0f, 0.0f};
	private float[] rotation = {0.0f, 0.0f, 0.0f};
	private float[] scale = {1.0f, 1.0f, 1.0f};
	private float[] rotationMatrix = new float[16];
	private float[] modelMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];
	private float[] modelViewProjectionMatrix = new float[16];

	// Shadow matrices
	private float[] shadowMVMatrix = new float[16];
	private float[] shadowMVPMatrix = new float[16];

	// VBO & VAO handles
	private int[] vboHandles = new int[2];
	private int[] vaoHandle = new int[1];
	private int[] vaoShadowHandle = new int[1];

	// Mirror programs
	MirrorShaderProgram mirrorShaderProgram;
	ShadowpassShaderProgram shadowpassShaderProgram;
	private int reflectionTexture;

	/**********************************************************************************************/

	public Mirror(Context context, float width, float height)
	{
		mirrorShaderProgram = new MirrorShaderProgram(context);
		shadowpassShaderProgram  = new ShadowpassShaderProgram(context);

		createMirror(width, height);
	}


	private void createMirror(float width, float height)
	{
		float initialX = -(width/2.0f);
		float initialY = -(height/2.0f);
		int offset = 0;

		vertices = new float[STRIDE * 4];
		indices = new short[6];

		for(int y=0; y<2; y++)
		{
			for(int x=0; x<2; x++)
			{
				// Position
				vertices[offset++] = initialX + ((float)x * width);
				vertices[offset++] = initialY + ((float)y * height);
				vertices[offset++] = 0.0f;

				// Texture Coordinates
				vertices[offset++] = (float)x;
				vertices[offset++] = (float)y;

				//  Normal
				vertices[offset++] = 0.0f;
				vertices[offset++] = 0.0f;
				vertices[offset++] = 1.0f;
			}
		}

		// Indices
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;

		indices[3] = 1;
		indices[4] = 3;
		indices[5] = 2;
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
				.asShortBuffer()
				.put(indices);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * Constants.BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

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

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);

		/********* SHADOW PASS VAO *********/

		glGenVertexArrays(1, vaoShadowHandle, 0);
		glBindVertexArray(vaoShadowHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTE_STRIDE, POSITION_BYTE_OFFSET);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);

		/*************************************/

		buildMatrices();
	}


	private void buildMatrices()
	{
		setIdentityM(rotationMatrix, 0);
		setIdentityM(modelMatrix, 0);

		// Rotation matrix
		rotateM(rotationMatrix, 0, rotation[0], 1f, 0f, 0f);
		rotateM(rotationMatrix, 0, rotation[1], 0f, 1f, 0f);
		rotateM(rotationMatrix, 0, rotation[2], 0f, 0f, 1f);

		// Adjust normal of the mirror
		multiplyMV(normal, 0, rotationMatrix, 0, baseNormal, 0);

		// ModelMatrix
		translateM(modelMatrix, 0, translation[0], translation[1],translation[2]);
		rotateM(modelMatrix, 0, rotation[0], 1f, 0f, 0f);
		rotateM(modelMatrix, 0, rotation[1], 0f, 1f, 0f);
		rotateM(modelMatrix, 0, rotation[2], 0f, 0f, 1f);
		scaleM(modelMatrix, 0, scale[0], scale[1], scale[2]);

		// Adjust center position
		multiplyMV(position, 0, modelMatrix, 0, basePosition, 0);
	}


	public void update(float[] view, float[] projection)
	{
		multiplyMM(modelViewMatrix, 0, view, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projection, 0, modelViewMatrix, 0);
	}


	public void drawShadowPass(float[] view, float[] projection)
	{
		multiplyMM(shadowMVMatrix, 0, view, 0, modelMatrix, 0);
		multiplyMM(shadowMVPMatrix, 0, projection, 0, shadowMVMatrix, 0);

		shadowpassShaderProgram.useProgram();
		shadowpassShaderProgram.setUniforms(shadowMVPMatrix);

		glBindVertexArray(vaoShadowHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, 0);
	}


	public void draw(int reflectionTexture)
	{
		mirrorShaderProgram.useProgram();
		mirrorShaderProgram.setUniforms(modelMatrix, rotationMatrix, modelViewProjectionMatrix, reflectionTexture);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, 0);
	}


	public float[] getNormal()
	{
		return normal;
	}


	public vec3 getNormalVec3()
	{
		return new vec3(normal[0], normal[1], normal[2]);
	}


	public vec3 getPositionVec3()
	{
		return new vec3(position[0], position[1], position[2]);
	}


	public void setTranslation(float x, float y, float z)
	{
		translation[0] = x;
		translation[1] = y;
		translation[2] = z;

		buildMatrices();
	}


	public void setRotation(float degreesX, float degreesY, float degreesZ)
	{
		rotation[0] = degreesX;
		rotation[1] = degreesY;
		rotation[2] = degreesZ;

		buildMatrices();
	}


	public void setScale(float x, float y, float z)
	{
		scale[0] = x;
		scale[1] = y;
		scale[2] = z;

		buildMatrices();
	}

}
