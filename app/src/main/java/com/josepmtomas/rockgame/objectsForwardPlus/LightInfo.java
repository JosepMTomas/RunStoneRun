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
	private vec3 vLight;
	private vec4 lightColor;
	private vec4 ambientColor;

	// array
	private float[] lightInfoArray = new float[12];
	private FloatBuffer lightInfoArrayBuffer;

	public int[] ubo = new int[1];

	// view projection matrices
	public float[] view = new float[16];
	public float[] projection = new float[16];
	public float[] viewProjection = new float[16];

	//TODO: temp
	private int increment = -1;

	public LightInfo()
	{
		lightPos = new vec3(100f, 150f, 0f);
		lightColor = new vec4(1f);
		ambientColor = new vec4(0.125f, 0.168f, 0.2f, 1f);//(0.2f, 0.25f, 0.3f, 1f);

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


	public void update()
	{
		/*if(lightPos.x >= 150f) increment = -1;
		if(lightPos.x <= -150f) increment = 1;

		Log.d("LightInfo", "LightPos.x = " + lightPos.x);

		lightPos.add(increment, 0f, 0f);*/

		vLight = normalize(lightPos);

		// Update the light position values
		lightInfoArray[0] = vLight.x;
		lightInfoArray[1] = vLight.y;
		lightInfoArray[2] = vLight.z;

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

		setLookAtM(view, 0, lightPos.x, lightPos.y, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		//setLookAtM(shadowView, 0, 300f, 200f, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		//perspectiveM(shadowProjection, 0, 90f, 1f, 0.1f, 500f);
		orthoM(projection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		//TODO: orthoM(shadowPerspective, 0, );
		multiplyMM(viewProjection, 0, projection, 0, view, 0);
	}
}
