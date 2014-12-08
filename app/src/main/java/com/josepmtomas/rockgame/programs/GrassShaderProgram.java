package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Josep on 04/08/2014.
 */
public class GrassShaderProgram extends ShaderProgram
{
	private static final String TAG = "GrassShaderProgram";

	// Uniform locations
	private final int uModelMatrixLocation;
	private final int uRotationMatrixLocation;
	private final int uMVPMatrixLocation;

	// Attribute locations
	private final int aPositionLocation;
	private final int aTexCoordLocation;
	private final int aNormalLocation;
	private final int aTangentLocation;


	public GrassShaderProgram(Context context)
	{
		super(context, R.raw.grass_vertex_shader, R.raw.grass_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX);
		uRotationMatrixLocation = glGetUniformLocation(program, U_ROTATION_MATRIX);
		uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);

		// Get attribute locations
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aTexCoordLocation = glGetAttribLocation(program, A_TEXCOORD);
		aNormalLocation = glGetAttribLocation(program, A_NORMAL);
		aTangentLocation = glGetAttribLocation(program, A_TANGENT);
	}

	public void setUniforms(float[] uModelMatrix, float[] uRotationMatrix, float[] uMVPMatrix)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uRotationMatrixLocation, 1, false, uRotationMatrix, 0);
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
