package com.josepmtomas.rockgame.objectsForwardPlus;

import android.util.Log;

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
	private float[] lightInfoArray = new float[12];
	private FloatBuffer lightInfoArrayBuffer;

	public int[] ubo = new int[1];

	// view projection matrices
	public float[] view = new float[16];
	public float[] projection = new float[16];
	public float[] viewProjection = new float[16];

	// Light colors
	private float[] lightColor1 = {1.0f, 0.706f, 0.0f};
	private float[] lightColor2 = {1.0f, 1.0f, 1.0f};

	// Background colors
	private float[] backColor1 = {0.949f, 0.623f, 0.38f};
	private float[] backColor2 = {1f, 1f, 1f};
	public float[] backColor = {0f, 0f, 0f};

	//TODO: temp
	private float increment = 1f;
	private float currentAngle = 0f;
	public float percent;

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
		for(int i=0; i < 12; i++)
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
		//TODO: rotation test
		currentAngle += increment;
		if(currentAngle > 180f)
		{
			increment = -deltaTime * 5f;
		}
		if(currentAngle < 0)
		{
			increment = deltaTime * 5f;
		}

		float angleAlpha;
		float angleAlphaOM;
		if(currentAngle > 0f && currentAngle < 90f)
		{
			angleAlpha = currentAngle / 90f;
			percent = angleAlpha;
			angleAlphaOM = 1.0f - angleAlpha;

			lightColor.x = lightColor2[0]*angleAlpha + lightColor1[0]*angleAlphaOM;
			lightColor.y = lightColor2[1]*angleAlpha + lightColor1[1]*angleAlphaOM;
			lightColor.z = lightColor2[2]*angleAlpha + lightColor1[2]*angleAlphaOM;

			backColor[0] = backColor2[0]*angleAlpha + backColor1[0]*angleAlphaOM;
			backColor[1] = backColor2[1]*angleAlpha + backColor1[1]*angleAlphaOM;
			backColor[2] = backColor2[2]*angleAlpha + backColor1[2]*angleAlphaOM;
		}
		else
		{
			angleAlpha = (currentAngle - 90f) / 90f;
			angleAlphaOM = 1.0f - angleAlpha;
			percent = angleAlphaOM;

			lightColor.x = lightColor1[0]*angleAlpha + lightColor2[0]*angleAlphaOM;
			lightColor.y = lightColor1[1]*angleAlpha + lightColor2[1]*angleAlphaOM;
			lightColor.z = lightColor1[2]*angleAlpha + lightColor2[2]*angleAlphaOM;

			backColor[0] = backColor1[0]*angleAlpha + backColor2[0]*angleAlphaOM;
			backColor[1] = backColor1[1]*angleAlpha + backColor2[1]*angleAlphaOM;
			backColor[2] = backColor1[2]*angleAlpha + backColor2[2]*angleAlphaOM;
		}

		//currentAngle = 45f;
		//if(lightPos.x >= 150f) increment = -1f;
		//if(lightPos.x <= -150f) increment = 1f;

		//Log.d("LightInfo", "LightPos.x = " + lightPos.x);

		//lightPos.add(increment, 0f, 0f);

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

		// Update the java native buffer
		lightInfoArrayBuffer.put(lightInfoArray, 0, lightInfoArray.length);
		lightInfoArrayBuffer.position(0);

		// Update the uniform buffer
		glBindBuffer(GL_UNIFORM_BUFFER, ubo[0]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightInfoArrayBuffer.capacity() * BYTES_PER_FLOAT, lightInfoArrayBuffer);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);


		// update view matrix
		/*setLookAtM(view, 0, lightPos.x, lightPos.y, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		orthoM(projection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		multiplyMM(viewProjection, 0, projection, 0, view, 0);*/

		setLookAtM(view, 0, lightPos.x, 0f/*lightPos.y*/, 0f, 0f, 0f, 0f, 0f, 1f, 0f);
		//setLookAtM(shadowView, 0, 300f, 200f, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		//perspectiveM(shadowProjection, 0, 90f, 1f, 0.1f, 500f);
		translateM(view, 0, 0f, 0f, 0f);
		rotateM(view, 0, -currentAngle, 0f, 0f, 1f);

		// Light vector update
		setIdentityM(lightRotation, 0);
		rotateM(lightRotation, 0, currentAngle, 0f, 0f, 1f);
		multiplyMV(lightVector, 0, lightRotation, 0, initialLightVector, 0);


		orthoM(projection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		//TODO: orthoM(shadowPerspective, 0, );
		multiplyMM(viewProjection, 0, projection, 0, view, 0);
	}
}
