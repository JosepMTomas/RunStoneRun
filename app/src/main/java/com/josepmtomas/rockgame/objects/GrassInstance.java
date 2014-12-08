package com.josepmtomas.rockgame.objects;

import android.util.Log;

import com.josepmtomas.rockgame.algebra.vec3;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Josep on 04/08/2014.
 */
public class GrassInstance
{
	private static final String TAG = "GrassInstance";

	private float[] modelMatrix = new float[16];
	private float[] rotationMatrix = new float[16];

	private float initialRotationX = 75.0f;

	private Grass parent;

	private vec3 position;


	public GrassInstance(Grass grass, vec3 position)
	{
		this.parent = grass;
		this.position = new vec3(position);
	}


	public void update(vec3 movement)
	{
		float[] temp = new float[16];

		position.add(movement);

		setIdentityM(modelMatrix, 0);
		setIdentityM(rotationMatrix, 0);
		rotateM(rotationMatrix, 0, initialRotationX, 1.0f, 0.0f, 0.0f);
		//rotateM(rotationMatrix, 0, FloatMath.sin(time)*22.5f, 1.0f, 0.0f, 0.0f);
		//rotateM(rotationMatrix, 0, 0.0f, 0.0f, 1.0f, 0.0f);
		//translateM(modelMatrix, 0, 0.0f, -10.0f, 0.0f);

		setIdentityM(temp, 0);
		translateM(temp, 0, position.x, position.y, position.z);
		//Log.d(TAG, "Updated Position: X = " + position.x + " Y = " + position.y + " Z = " + position.z);

		multiplyMM(modelMatrix, 0, temp, 0, rotationMatrix, 0);
	}


	public void draw()
	{
		//parent.bind();
		parent.draw(modelMatrix, rotationMatrix);
	}
}
