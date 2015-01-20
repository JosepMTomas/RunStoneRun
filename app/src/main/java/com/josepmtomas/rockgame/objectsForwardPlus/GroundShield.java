package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.programsForwardPlus.GroundShieldProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

import static com.josepmtomas.rockgame.Constants.*;

/**
 * Created by Josep on 16/12/2014.
 */
public class GroundShield
{
	/*private static final int INVISIBLE = 0;
	private static final int APPEARING = 1;
	private static final int SHOWING = 2;
	private static final int DISAPPEARING = 3;*/

	private int[] vaoHandle = new int[1];

	private int texture;

	private GroundShieldProgram groundShieldProgram;

	// State
	public boolean isVisible = false;
	private int state = UI_STATE_NOT_VISIBLE;
	private float[] stateTimers = {0f, 0.5f, 2.0f, 0.5f};
	private float timer = 0f;
	private float totalTimer = 0f;


	public GroundShield(Context context)
	{
		createGeometry();

		groundShieldProgram = new GroundShieldProgram(context);

		texture = TextureHelper.loadETC2Texture(context, "textures/player_rock/ground_shield.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}


	public void createGeometry()
	{
		int[] vboHandles = new int[2];
		float[] vertices = new float[20];

		float sizeHalf = 15f;
		float left = -sizeHalf;
		float right = sizeHalf;
		float up = -sizeHalf;
		float down = sizeHalf;
		float height = 0.1f;

		// A
		vertices[0] = left;
		vertices[1] = height;
		vertices[2] = down;
		vertices[3] = 0f;
		vertices[4] = 0f;

		// B
		vertices[5] = right;
		vertices[6] = height;
		vertices[7] = down;
		vertices[8] = 1f;
		vertices[9] = 0f;

		// C
		vertices[10] = right;
		vertices[11] = height;
		vertices[12] = up;
		vertices[13] = 1f;
		vertices[14] = 1f;

		// D
		vertices[15] = left;
		vertices[16] = height;
		vertices[17] = up;
		vertices[18] = 0f;
		vertices[19] = 1f;

		short[] elements = new short[6];

		// A - B - C
		elements[0] = 0;
		elements[1] = 1;
		elements[2] = 2;

		// A - C - D
		elements[3] = 0;
		elements[4] = 2;
		elements[5] = 3;

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
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	public void update(float deltaTime)
	{
		if(state == UI_STATE_APPEARING)
		{
			timer += deltaTime;
			totalTimer += deltaTime;

			if(timer >= stateTimers[UI_STATE_APPEARING])
			{
				timer -= stateTimers[UI_STATE_APPEARING];
				state = UI_STATE_VISIBLE;
			}
		}
		else if(state == UI_STATE_VISIBLE)
		{
			timer += deltaTime;
			totalTimer += deltaTime;

			if(timer >= stateTimers[UI_STATE_VISIBLE])
			{
				timer -= stateTimers[UI_STATE_VISIBLE];
				state = UI_STATE_DISAPPEARING;
			}
		}
		else if(state == UI_STATE_DISAPPEARING)
		{
			timer += deltaTime;
			totalTimer += deltaTime;

			if(timer >= stateTimers[UI_STATE_DISAPPEARING])
			{
				state = UI_STATE_NOT_VISIBLE;
				isVisible = false;
			}
		}
	}


	public void hit()
	{
		state = UI_STATE_APPEARING;
		isVisible = true;

		timer = 0f;
		totalTimer = 0f;
	}


	public void newGame()
	{
		state = UI_STATE_NOT_VISIBLE;
	}


	public void endGame()
	{
		state = UI_STATE_NOT_VISIBLE;
	}


	public void draw(float[] viewProjection)
	{
		if(state != UI_STATE_NOT_VISIBLE)
		{
			groundShieldProgram.useProgram();
			groundShieldProgram.setUniforms(viewProjection, texture, 0.25f, 1f, 0f, totalTimer * 5f);

			glBindVertexArray(vaoHandle[0]);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
		}
	}
}
