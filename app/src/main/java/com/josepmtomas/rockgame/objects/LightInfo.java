package com.josepmtomas.rockgame.objects;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.algebra.vec4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;
import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;

/**
 * Created by Josep on 01/12/2014.
 * @author Josep
 */
public class LightInfo
{
	private vec3 lightPos;
	private vec4 lightColor;
	private vec4 ambientColor;

	private float[] lightRotation = new float[16];
	private float[] initialLightVector = {1f, 0f, 0f, 0f};
	private float[] lightVector = {0f, 0f, 0f, 0f};

	// array
	private float[] lightInfoArray = new float[17];
	private FloatBuffer lightInfoArrayBuffer;

	public int[] ubo = new int[1];

	// view projection matrices
	public float[] view = new float[16];
	public float[] projection = new float[16];
	public float[] viewProjection = new float[16];

	// Time
	public float timeOfDay = 12f;
	private static final float TIME_OF_DAY_MULTIPLIER = 0.25f;
	public float timeOfDayAlpha = 0f;

	// Light colors
	private float[] lightColor1 = {1.0f, 0.706f, 0.0f};
	private float[] lightColor2 = {1.0f, 1.0f, 1.0f};
	private float[] lightColor3 = {0.02f, 0.3136f, 0.4704f};//{0.098f, 0.1568f, 0.2352f};

	// Ambient colors
	private float[] ambientColor1 = {0.2f, 0.25f, 0.3f};
	private float[] ambientColor2 = {0.2f, 0.25f, 0.3f};
	private float[] ambientColor3 = {0.1f, 0.1f, 0.1f};

	// Background colors
	private float[] backColor1 = {0.949f, 0.623f, 0.38f};
	private float[] backColor2 = {1f, 1f, 1f};
	private float[] backColor3 = {0.09f, 0.078f, 0.35f};
	public float[] backColor = {0f, 0f, 0f};

	// Shadow factor
	private float shadowFactor = 0f;

	private float currentAngle = 0f;

	public LightInfo()
	{
		lightPos = new vec3(150f, 150f, 0f);
		//lightColor = new vec4(1f);
		lightColor = new vec4(0.988f, 0.89f, 0.655f, 1f);	//light orange
		//lightColor = new vec4(0.655f, 0.945f, 0.988f, 1.0f); // light blue
		//lightColor = new vec4(1.0f, 0.0f, 0.0f, 1.0f);
		ambientColor = new vec4(/*0.125f, 0.168f, 0.2f, 1f);*/0.2f, 0.25f, 0.3f, 1f);

		initialize();
	}


	private void initialize()
	{
		// initialize the array
		for(int i=0; i < 17; i++)
		{
			lightInfoArray[i] = 0f;
		}

		// create the java native buffer and fill it with the initialized array
		lightInfoArrayBuffer = ByteBuffer
				.allocateDirect(lightInfoArray.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(lightInfoArray);
		lightInfoArrayBuffer.position(0);

		// create the uniform buffer object
		glGenBuffers(1, ubo, 0);

		// fill the uniform buffer
		glBindBuffer(GL_UNIFORM_BUFFER, ubo[0]);
		glBufferData(GL_UNIFORM_BUFFER, lightInfoArrayBuffer.capacity() * BYTES_PER_FLOAT, lightInfoArrayBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBufferBase(GL_UNIFORM_BUFFER, 5, ubo[0]);

		setIdentityM(view, 0);
		setIdentityM(projection, 0);
		setIdentityM(viewProjection, 0);
	}


	public void update(float deltaTime)
	{
		timeOfDay += (deltaTime * TIME_OF_DAY_MULTIPLIER);
		timeOfDay = timeOfDay % 24f;

		//Log.d("TimeOfDay", "hour = " + timeOfDay);
		if(timeOfDay >= 0.0f && timeOfDay < 6f)
		{
			timeOfDayAlpha = timeOfDay / 6f;

			currentAngle = timeOfDayAlpha * 45f;

			shadowFactor = 1f - timeOfDayAlpha;

			lightColor.x = lerp(lightColor3[0], lightColor1[0], timeOfDayAlpha);
			lightColor.y = lerp(lightColor3[1], lightColor1[1], timeOfDayAlpha);
			lightColor.z = lerp(lightColor3[2], lightColor1[2], timeOfDayAlpha);

			backColor[0] = lerp(backColor3[0], backColor1[0], timeOfDayAlpha);
			backColor[1] = lerp(backColor3[1], backColor1[1], timeOfDayAlpha);
			backColor[2] = lerp(backColor3[2], backColor1[2], timeOfDayAlpha);

			ambientColor.x = lerp(ambientColor3[0], ambientColor1[0], timeOfDayAlpha);
			ambientColor.y = lerp(ambientColor3[1], ambientColor1[1], timeOfDayAlpha);
			ambientColor.z = lerp(ambientColor3[2], ambientColor1[2], timeOfDayAlpha);
		}
		else if(timeOfDay >= 6f && timeOfDay < 12f)
		{
			timeOfDayAlpha = (timeOfDay - 6f) / 6f;

			currentAngle = timeOfDayAlpha * 45f + 45f;

			shadowFactor = 0f;

			lightColor.x = lerp(lightColor1[0], lightColor2[0], timeOfDayAlpha);
			lightColor.y = lerp(lightColor1[1], lightColor2[1], timeOfDayAlpha);
			lightColor.z = lerp(lightColor1[2], lightColor2[2], timeOfDayAlpha);

			backColor[0] = lerp(backColor1[0], backColor2[0], timeOfDayAlpha);
			backColor[1] = lerp(backColor1[1], backColor2[1], timeOfDayAlpha);
			backColor[2] = lerp(backColor1[2], backColor2[2], timeOfDayAlpha);

			ambientColor.x = lerp(ambientColor1[0], ambientColor2[0], timeOfDayAlpha);
			ambientColor.y = lerp(ambientColor1[1], ambientColor2[1], timeOfDayAlpha);
			ambientColor.z = lerp(ambientColor1[2], ambientColor2[2], timeOfDayAlpha);
		}
		else if(timeOfDay >= 12f && timeOfDay < 18f)
		{
			timeOfDayAlpha = (timeOfDay - 12f) / 6f;

			currentAngle = timeOfDayAlpha * 45f + 90f;

			shadowFactor = 0f;

			lightColor.x = lerp(lightColor2[0], lightColor1[0], timeOfDayAlpha);
			lightColor.y = lerp(lightColor2[1], lightColor1[1], timeOfDayAlpha);
			lightColor.z = lerp(lightColor2[2], lightColor1[2], timeOfDayAlpha);

			backColor[0] = lerp(backColor2[0], backColor1[0], timeOfDayAlpha);
			backColor[1] = lerp(backColor2[1], backColor1[1], timeOfDayAlpha);
			backColor[2] = lerp(backColor2[2], backColor1[2], timeOfDayAlpha);

			ambientColor.x = lerp(ambientColor2[0], ambientColor1[0], timeOfDayAlpha);
			ambientColor.y = lerp(ambientColor2[1], ambientColor1[1], timeOfDayAlpha);
			ambientColor.z = lerp(ambientColor2[2], ambientColor1[2], timeOfDayAlpha);
		}
		else if(timeOfDay >= 18f && timeOfDay < 24f)
		{
			timeOfDayAlpha = (timeOfDay - 18f) / 6f;

			currentAngle = timeOfDayAlpha * 45f + 135f;

			shadowFactor = timeOfDayAlpha;

			lightColor.x = lerp(lightColor1[0], lightColor3[0], timeOfDayAlpha);
			lightColor.y = lerp(lightColor1[1], lightColor3[1], timeOfDayAlpha);
			lightColor.z = lerp(lightColor1[2], lightColor3[2], timeOfDayAlpha);

			backColor[0] = lerp(backColor1[0], backColor3[0], timeOfDayAlpha);
			backColor[1] = lerp(backColor1[1], backColor3[1], timeOfDayAlpha);
			backColor[2] = lerp(backColor1[2], backColor3[2], timeOfDayAlpha);

			ambientColor.x = lerp(ambientColor1[0], ambientColor3[0], timeOfDayAlpha);
			ambientColor.y = lerp(ambientColor1[1], ambientColor3[1], timeOfDayAlpha);
			ambientColor.z = lerp(ambientColor1[2], ambientColor3[2], timeOfDayAlpha);
		}

		setLookAtM(view, 0, lightPos.x, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f);
		translateM(view, 0, 0f, 0f, 0f);
		rotateM(view, 0, -currentAngle, 0f, 0f, 1f);

		// Light vector update
		setIdentityM(lightRotation, 0);
		rotateM(lightRotation, 0, currentAngle, 0f, 0f, 1f);
		multiplyMV(lightVector, 0, lightRotation, 0, initialLightVector, 0);


		orthoM(projection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		multiplyMM(viewProjection, 0, projection, 0, view, 0);

		// Update the light position values
		lightInfoArray[0] = lightVector[0];
		lightInfoArray[1] = lightVector[1];
		lightInfoArray[2] = lightVector[2];

		// Update the light color values
		lightInfoArray[4] = lightColor.x;
		lightInfoArray[5] = lightColor.y;
		lightInfoArray[6] = lightColor.z;
		lightInfoArray[7] = lightColor.w;

		// Update the ambient color values
		lightInfoArray[8]  = ambientColor.x;
		lightInfoArray[9]  = ambientColor.y;
		lightInfoArray[10] = ambientColor.z;
		lightInfoArray[11] = ambientColor.w;

		// Update the back color values
		lightInfoArray[12] = backColor[0];
		lightInfoArray[13] = backColor[1];
		lightInfoArray[14] = backColor[2];
		lightInfoArray[15] = 1f;

		// Update shadow factor
		lightInfoArray[16] = shadowFactor;

		// Update the java native buffer
		lightInfoArrayBuffer.put(lightInfoArray, 0, lightInfoArray.length);
		lightInfoArrayBuffer.position(0);

		// Update the uniform buffer
		glBindBuffer(GL_UNIFORM_BUFFER, ubo[0]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightInfoArrayBuffer.capacity() * BYTES_PER_FLOAT, lightInfoArrayBuffer);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
}
