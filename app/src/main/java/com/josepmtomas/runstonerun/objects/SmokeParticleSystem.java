package com.josepmtomas.runstonerun.objects;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

import com.josepmtomas.runstonerun.programs.ParticleProgram;
import com.josepmtomas.runstonerun.util.TextureHelper;

import static com.josepmtomas.runstonerun.Constants.*;

/**
 * Created by Josep on 16/01/2015.
 * @author Josep
 */
public class SmokeParticleSystem
{
	//private static final String TAG = "SmokePS";

	// Geometry
	private int[] vaoHandle = new int[1];

	// View Projection matrix
	private float[] viewProjection;

	// Particles info
	private static final float PARTICLE_GENERATOR_PERIOD = 0.05f;
	private static final float PARTICLE_MAXIMUM_LIFE = 1.5f;
	private static final int MAX_NUMBER_OF_PARTICLES = 32;
	//private float[] rotationMatrix = new float[16];
	//private float[] directions;
	private float[] positions;
	private float[] rotations;
	private float[] angles;
	private float[] scales;
	private float[] timers;
	private float globalTimer = 0f;
	private int currentIndex = 0;
	private int numberOfParticles = 0;

	// Matrices
	private float[] particlesMatricesArray;
	private FloatBuffer particlesMatricesBuffer;

	// Particles buffer
	private int[] particlesMatricesUbo = new int[1];

	// State
	private boolean isEnabled = false;
	private boolean isEmitterEnabled = false;

	// Random numbers generation
	private Random random;

	// Program
	ParticleProgram particleProgram;

	// Texture
	private final int smokeTexture;


	public SmokeParticleSystem(Context context)
	{
		random = new Random();

		particleProgram = new ParticleProgram(context);

		smokeTexture = TextureHelper.loadETC2Texture(context, "textures/particles/smoke.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		createGeometry();
		createParticlesBuffer();
		initialize();
	}


	@SuppressWarnings("all")
	private void createGeometry()
	{
		int[] vboHandles = new int[2];
		float[] vertices = new float[20];

		float sizeHalf = 10f;
		float left = -sizeHalf;
		float right = sizeHalf;
		float up = -sizeHalf;
		float down = sizeHalf;

		// A
		vertices[0] = left;
		vertices[1] = down;
		vertices[2] = 0f;
		vertices[3] = 0f;
		vertices[4] = 0f;

		// B
		vertices[5] = right;
		vertices[6] = down;
		vertices[7] = 0f;
		vertices[8] = 1f;
		vertices[9] = 0f;

		// C
		vertices[10] = right;
		vertices[11] = up;
		vertices[12] = 0f;
		vertices[13] = 1f;
		vertices[14] = 1f;

		// D
		vertices[15] = left;
		vertices[16] = up;
		vertices[17] = 0f;
		vertices[18] = 0f;
		vertices[19] = 1f;

		short[] elements = new short[6];

		// A - B - C
		elements[0] = 0;
		elements[1] = 2;
		elements[2] = 1;

		// A - C - D
		elements[3] = 0;
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
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 0);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

		// Elements
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[1]);

		glBindVertexArray(0);
	}


	private void createParticlesBuffer()
	{
		particlesMatricesArray = new float[MAX_NUMBER_OF_PARTICLES * 16];

		for(int i=0; i<MAX_NUMBER_OF_PARTICLES*16; i++)
		{
			particlesMatricesArray[i] = 0f;
		}

		// Create the native buffer
		particlesMatricesBuffer = ByteBuffer
				.allocateDirect(particlesMatricesArray.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(particlesMatricesArray);
		particlesMatricesBuffer.position(0);

		// Generate the OpenGL buffer
		glGenBuffers(1, particlesMatricesUbo, 0);

		// Create the OpenGL buffer
		glBindBuffer(GL_UNIFORM_BUFFER, particlesMatricesUbo[0]);
		glBufferData(GL_UNIFORM_BUFFER, particlesMatricesBuffer.capacity() * BYTES_PER_FLOAT, particlesMatricesBuffer, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	private void initialize()
	{
		//directions = new float[MAX_NUMBER_OF_PARTICLES * 4];
		positions = new float[MAX_NUMBER_OF_PARTICLES * 4];
		rotations = new float[MAX_NUMBER_OF_PARTICLES];
		angles = new float[MAX_NUMBER_OF_PARTICLES];
		scales = new float[MAX_NUMBER_OF_PARTICLES];
		timers = new float[MAX_NUMBER_OF_PARTICLES];

		for(int i=0; i<MAX_NUMBER_OF_PARTICLES*4; i++)
		{
			//directions[i] = 0f;
			positions[i] = 0f;
		}

		for(int i=0; i<MAX_NUMBER_OF_PARTICLES; i++)
		{
			scales[i] = 1f;
			timers[i] = 0f;
		}
	}


	private void emitParticle(float currentPositionZ)
	{
		float angle;
		int currentOffset;

		if(isEmitterEnabled && numberOfParticles < MAX_NUMBER_OF_PARTICLES)
		{
			// Increment the index
			currentIndex++;
			currentIndex = currentIndex % MAX_NUMBER_OF_PARTICLES;
			currentOffset = currentIndex * 4;

			// Obtain new angle
			angle = random.nextFloat() * 180f - 90f;

			// Obtain new direction
			////setIdentityM(rotationMatrix, 0);
			////rotateM(rotationMatrix, 0, angle, 0f, 1f, 0f);
			////multiplyMV(directions, currentOffset, rotationMatrix, 0, currentDirection, 0);

			// Calculate position
			positions[currentOffset] = random.nextFloat() * 10f - 5f;
			positions[currentOffset + 1] = 0f;
			positions[currentOffset + 2] = currentPositionZ - 5f;
			positions[currentOffset + 3] = 1f;

			// New scale
			scales[currentIndex] = random.nextFloat() * 0.75f + 1.0f;

			// New rotation
			if(angle < 0)
				rotations[currentIndex] = random.nextFloat() * 30f;
			else
				rotations[currentIndex] = random.nextFloat() * -30f;

			angles[currentIndex] = random.nextFloat() * 360f;

			// Reset timer
			timers[currentIndex] = 0f;

			numberOfParticles++;
		}
	}


	public void update(float[] viewProjection, float currentPositionZ, float[] displacement, float deltaTime)
	{
		this.viewProjection = viewProjection;

		int currentOffset;
		int index;

		if(isEnabled)
		{
			globalTimer += deltaTime;

			if(globalTimer >= PARTICLE_GENERATOR_PERIOD)
			{
				globalTimer -= PARTICLE_GENERATOR_PERIOD;
				emitParticle(currentPositionZ);
			}

			index = currentIndex;
			int i=0;
			while(i < numberOfParticles)
			{
				currentOffset = index * 4;

				positions[currentOffset]   += displacement[0] * 0.25f;
				positions[currentOffset+1] += 0f;
				positions[currentOffset+2] += displacement[2] * 0.25f;

				timers[index] += deltaTime;

				angles[index] = angles[index] + (rotations[index] * deltaTime);

				if(timers[index] > PARTICLE_MAXIMUM_LIFE)
				{
					timers[index] = 0f;
					numberOfParticles--;
				}

				index--;
				if(index < 0) index = MAX_NUMBER_OF_PARTICLES - 1;

				i++;
			}

			// Update matrices
			index = currentIndex;
			float scale;
			for(i=0; i <numberOfParticles; i++)
			{
				currentOffset = i *16;

				scale = Math.min(scales[index] * (timers[index] * 4f + 0.25f), scales[index]);

				setIdentityM(particlesMatricesArray, currentOffset);
				translateM(particlesMatricesArray, currentOffset, positions[index*4], positions[index*4 + 1], positions[index*4 + 2]);
				rotateM(particlesMatricesArray, currentOffset, angles[index], 0f, 0f, 1f);
				scaleM(particlesMatricesArray, currentOffset, scale, scale, scale);

				index--;
				if(index < 0) index = MAX_NUMBER_OF_PARTICLES - 1;
			}

			// Update native buffer
			particlesMatricesBuffer.position(0);
			particlesMatricesBuffer.put(particlesMatricesArray);
			particlesMatricesBuffer.position(0);

			// Update OpenGL buffer
			glBindBuffer(GL_UNIFORM_BUFFER, particlesMatricesUbo[0]);
			glBufferSubData(GL_UNIFORM_BUFFER, 0, particlesMatricesBuffer.capacity() * BYTES_PER_FLOAT, particlesMatricesBuffer);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}
	}


	public void draw()
	{
		//Log.w(TAG, "Number of particles: " + numberOfParticles);

		if(numberOfParticles > 0)
		{
			particleProgram.useProgram();
			particleProgram.setUniforms(viewProjection, particlesMatricesUbo[0], smokeTexture);

			glBindVertexArray(vaoHandle[0]);
			glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0, numberOfParticles);
		}
	}

	public void setEnabled(boolean value)
	{
		isEnabled = value;

		if(isEnabled)
		{
			globalTimer = 0f;
			numberOfParticles = 0;
			currentIndex = 0;

			for(int i=0; i<MAX_NUMBER_OF_PARTICLES; i++)
			{
				timers[i] = 0f;
			}
		}
		else
		{
			numberOfParticles = 0;
		}
	}

	public void setEmitterEnabled(boolean value)
	{
		isEmitterEnabled = value;
	}


	public void saveState(FileOutputStream outputStream) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		String stateString = "SMOKE_PARTICLES "
				+ isEnabled + " "
				+ isEmitterEnabled + " "
				+ globalTimer + " "
				+ currentIndex + " "
				+ numberOfParticles + "\n";
		outputStream.write(stateString.getBytes());

		builder.append("POSITIONS ");
		builder.append(positions.length);
		for(float position : positions)
		{
			builder.append(" ");	builder.append(position);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("ROTATIONS ");
		builder.append(rotations.length);
		for(float rotation : rotations)
		{
			builder.append(" ");	builder.append(rotation);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("ANGLES ");
		builder.append(angles.length);
		for(float angle : angles)
		{
			builder.append(" ");	builder.append(angle);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("SCALES ");
		builder.append(scales.length);
		for(float scale : scales)
		{
			builder.append(" ");	builder.append(scale);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("TIMERS ");
		builder.append(timers.length);
		for(float timer : timers)
		{
			builder.append(" ");	builder.append(timer);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());
	}


	public void loadState(BufferedReader bufferedReader) throws IOException
	{
		String line;
		String[] tokens;
		int numPoints, offset;

		line = bufferedReader.readLine();
		tokens = line.split(" ");

		isEnabled = Boolean.parseBoolean(tokens[1]);
		isEmitterEnabled = Boolean.parseBoolean(tokens[2]);
		globalTimer = Float.parseFloat(tokens[3]);
		currentIndex = Integer.parseInt(tokens[4]);
		numberOfParticles = Integer.parseInt(tokens[5]);

		// Read positions
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			positions[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read rotations
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			rotations[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read angles
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			angles[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read scales
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			scales[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read timers
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i<numPoints; i++)
		{
			timers[i] = Float.parseFloat(tokens[offset++]);
		}
	}
}
