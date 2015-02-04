package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import com.josepmtomas.runstonerun.programs.TestUIProgram;
import com.josepmtomas.runstonerun.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import static com.josepmtomas.runstonerun.Constants.*;

/**
 * Created by Josep on 29/11/2014.
 */
public class TestUI
{
	private static final String TAG = "TestUI";

	// Geometry attributes constants
	private static final int POSITION_BYTE_OFFSET = 0;
	private static final int TEXCOORD_BYTE_OFFSET = 3 * BYTES_PER_FLOAT;
	private static final int BYTE_STRIDE = 5 * BYTES_PER_FLOAT;

	// Geometry buffers handles
	private int[] vboHandles = new int[2];
	public int[] vaoHandle = new int[1];

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Offsets
	private float[] positionOffset1 = {600f, 400f, 0f, 0f};
	private float[] positionOffset2 = {500f, 400f, 0f, 0f};
	private float[] positionOffset3 = {400f, 400f, 0f, 0f};
	private float[] texCoordOffset = {0f, 0f};

	// UI texture
	private int numbersTexture;
	private int numbersBlurTexture;

	// Program
	private TestUIProgram testUIProgram;


	public TestUI(Context context, int screenWidth, int screenHeight)
	{
		createBaseGeometry();
		createMatrices(screenWidth, screenHeight);

		numbersTexture = TextureHelper.loadETC2Texture(context, "textures/test_ui/test_numbers_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		numbersBlurTexture = TextureHelper.loadETC2Texture(context, "textures/test_ui/test_numbers_blur_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		testUIProgram = new TestUIProgram(context);
	}


	private void createBaseGeometry()
	{
		float[] vertices = new float[20];
		short[] elements = new short[6];

		float bottom = -128f;
		float left = -128f;
		float width = 256f;
		float height = 256f;

		float texCoordVstart = 0.1f;
		float texCoordVincrement = -0.1f;

		// D - C
		// | \ |
		// A - B

		int offset = 0;

		for(int y=0; y<2; y++)
		{
			for(int x=0; x<2; x++)
			{
				// Position
				vertices[offset++] = left + ((float)x * width);
				vertices[offset++] = bottom + ((float)y * height);
				vertices[offset++] = 0.0f;

				// Texture Coordinates
				vertices[offset++] = (float)x;
				//vertices[offset++] = (float)y;
				vertices[offset++] = texCoordVstart + ((float)y * texCoordVincrement);
			}
		}

		// Indices
		elements[0] = 0;
		elements[1] = 1;
		elements[2] = 2;

		elements[3] = 1;
		elements[4] = 3;
		elements[5] = 2;

		// Java native buffers
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

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
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

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

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	private void createMatrices(int screenWidth, int screenHeight)
	{
		float right = screenWidth / 2f;
		float left = -right;
		float top = screenHeight / 2f;
		float bottom = -top;
		float near = 0.5f;
		float far = 10f;

		// Initialize view matrix
		setIdentityM(view, 0);
		setLookAtM(view, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);

		// Initialize projection matrix
		setIdentityM(projection, 0);
		orthoM(projection, 0, left, right, bottom, top, near, far);

		// Calculate view x projection matrix
		multiplyMM(viewProjection, 0, projection, 0, view, 0);
	}


	public void update()
	{
		texCoordOffset[1] += 0.1f;
	}


	public void draw()
	{
		testUIProgram.useProgram();
		testUIProgram.setCommonUniforms(viewProjection, numbersTexture);
		testUIProgram.setSpecificUniforms(positionOffset1, texCoordOffset);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		texCoordOffset[1] += 0.1f;

		testUIProgram.setSpecificUniforms(positionOffset2, texCoordOffset);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		texCoordOffset[1] += 0.1f;

		testUIProgram.setSpecificUniforms(positionOffset3, texCoordOffset);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
	}
}
