package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 19/08/2014.
 */
public class MirrorShaderProgram extends ShaderProgram
{
	private final static String TAG = "MirrorShaderProgram";

	// Uniform locations
	private final int uModelMatrixLocation;
	private final int uRotationMatrixLocation;
	private final int uMVPMatrixLocation;

	private final int uReflectionTexture;


	public MirrorShaderProgram(Context context)
	{
		super(context, R.raw.mirror_vertex_shader, R.raw.mirror_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, "u_ModelMatrix");
		uRotationMatrixLocation = glGetUniformLocation(program, "u_RotationMatrix");
		uMVPMatrixLocation = glGetUniformLocation(program, "u_ModelViewProjectionMatrix");

		uReflectionTexture = glGetUniformLocation(program, "reflectionTexture");
	}

	public void setUniforms(float[] uModelMatrix, float[] uRotationMatrix, float[] uMVPMatrix, int texture)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uRotationMatrixLocation, 1, false, uRotationMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);

		//glUniform1i(uReflectionTexture, 0);

		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, texture);
		glUniform1i(uReflectionTexture, 3);
	}
}
