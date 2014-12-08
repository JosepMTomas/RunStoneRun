package com.josepmtomas.rockgame.objectsES30;

import android.content.Context;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.programs.SkyboxShaderProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.io.BufferedReader;
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
 * Created by Josep on 03/09/2014.
 */
public class Skybox
{
	private float scaleX;
	private float scaleY;
	private float scaleZ;

	private int numVertices;
	private int numElements;

	private float[] positions;
	private short[] elements;

	private FloatBuffer positionsBuffer;
	private ShortBuffer elementsBuffer;

	private int[] vaoHandle = new int[1];

	// Textures
	private int skyboxTextureID;

	// Shader variables
	private float[] model = new float[16];
	private float[] view;
	private float[] projection;
	private float[] modelView = new float[16];
	private float[] modelViewProjection = new float[16];

	// Shader programs
	private SkyboxShaderProgram skyboxShaderProgram;


	/**********************************************************************************************/

	public Skybox(Context context, float scaleX, float scaleY, float scaleZ)
	{
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;

		skyboxShaderProgram = new SkyboxShaderProgram(context);

		String[] cubemapTextures = {
				"textures/Skybox1/left.png",
				"textures/Skybox1/right.png",
				"textures/Skybox1/bottom.png",
				"textures/Skybox1/top.png",
				"textures/Skybox1/front.png",
				"textures/Skybox1/back.png"};

		skyboxTextureID = TextureHelper.loadCubeMap(context, cubemapTextures);

		loadFromFile(context, "models/skybox.vbm");
		initialize();
	}


	private void loadFromFile(Context context, String fileName)
	{
		try
		{
			InputStream inputStream = context.getResources().getAssets().open(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String nextLine;

			int positionsOffset = 0;
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
					positions = new float[3 * numVertices];
				}
				else if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the elements array
					numElements = Integer.parseInt(tokens[1]);
					elements = new short[3 * numElements];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read the vertex positions
					positions[positionsOffset++] = Float.parseFloat(tokens[1]);
					positions[positionsOffset++] = Float.parseFloat(tokens[2]);
					positions[positionsOffset++] = Float.parseFloat(tokens[3]);
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
	}


	private void initialize()
	{
		positionsBuffer = ByteBuffer
				.allocateDirect(positions.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(positions);
		positionsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * Constants.BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		int[] handle = new int[2];
		glGenBuffers(2, handle, 0);

		glBindBuffer(GL_ARRAY_BUFFER, handle[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * Constants.BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * Constants.BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		// Create the VAO
		glGenVertexArrays(1, vaoHandle, 0);
		glBindVertexArray(vaoHandle[0]);

		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, handle[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle[1]);

		glBindVertexArray(0);
	}


	public void setMatrices(float[] viewM, float[] projectionM)
	{
		this.view = viewM;
		this.projection = projectionM;
	}


	public void update()
	{
		setIdentityM(model, 0);
		scaleM(model, 0, scaleX, scaleY, scaleZ);

		multiplyMM(modelView, 0, view, 0, model, 0);
		multiplyMM(modelViewProjection, 0, projection, 0, modelView, 0);
	}


	public void draw()
	{
		skyboxShaderProgram.useProgram();
		skyboxShaderProgram.setUniforms(modelView, modelViewProjection, skyboxTextureID);

		glBindVertexArray(vaoHandle[0]);
		glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_SHORT, 0);
	}
}
