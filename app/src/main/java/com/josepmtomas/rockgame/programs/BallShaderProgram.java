package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;

import static android.opengl.GLES20.*;

/**
 * Created by Josep on 27/07/2014.
 */
public class BallShaderProgram extends ShaderProgram
{
	private final static String TAG = "BallShaderProgram";

	// Uniform locations
	private final int uModelMatrixLocation;
	private final int uMVPMatrixLocation;

	// Attribute locations
	private final int aPositionLocation;
	private final int aTexCoordLocation;
	private final int aNormalLocation;
	private final int aTangentLocation;


	public BallShaderProgram(Context context)
	{
		super(context, R.raw.ball_vertex_shader, R.raw.ball_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX);
		uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);

		// Get attribute locations
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aTexCoordLocation = glGetAttribLocation(program, A_TEXCOORD);
		aNormalLocation = glGetAttribLocation(program, A_NORMAL);
		aTangentLocation = glGetAttribLocation(program, A_TANGENT);
	}

	public void setUniforms(float[] uModelMatrix, float[] uMVPMatrix)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);
	}

	public int getPositionAttributeLocation()
	{
		return aPositionLocation;
	}


	public int getTexCoordAttributeLocation()
	{
		return aTexCoordLocation;
	}


	public int getNormalAttributeLocation()
	{
		return aNormalLocation;
	}


	public int getTangentAttributeLocation()
	{
		return aTangentLocation;
	}
}
