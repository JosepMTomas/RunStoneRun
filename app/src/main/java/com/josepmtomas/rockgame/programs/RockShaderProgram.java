package com.josepmtomas.rockgame.programs;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;

import static android.opengl.GLES20.*;

/**
 * Created by Josep on 17/07/2014.
 */
public class RockShaderProgram extends ShaderProgram
{
	private final static String TAG = "RockShaderProgram";

	private static final String U_NORMAL_TEXTURE = "u_NormalTexture";

	// Uniforms locations
	private final int uModelMatrixLocation;
	private final int uMVPMatrixLocation;

	private final int uNormalTextureLocation;

	private final int uEyePosition;

	// Attribute locations
	private final int aPositionLocation;
	private final int aTexCoordLocation;
	private final int aNormalLocation;
	private final int aTangentLocation;


	public RockShaderProgram(Context context)
	{
		super(context, R.raw.rock_vertex_shader, R.raw.rock_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX);
		uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);

		uNormalTextureLocation = glGetUniformLocation(program, U_NORMAL_TEXTURE);

		uEyePosition = glGetUniformLocation(program, U_EYE_POSITION);

		// Get attribute locations
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aTexCoordLocation = glGetAttribLocation(program, A_TEXCOORD);
		aNormalLocation = glGetAttribLocation(program, A_NORMAL);
		aTangentLocation = glGetAttribLocation(program, A_TANGENT);
	}


	public void setUniforms(float[] uModelMatrix, float[] uMVPMatrix, vec3 eyePosition, int normalTexture)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);

		glUniform3fv(uEyePosition, 1, eyePosition.data(), 0);

		// Bind the texture to this unit.
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, normalTexture);
		glUniform1i(uNormalTextureLocation, 0);
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
