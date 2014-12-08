package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;

import static android.opengl.GLES30.glGetAttribLocation;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glUniformMatrix4fv;

/**
 * Created by Josep on 06/08/2014.
 */
public class BasicShaderProgram extends ShaderProgram
{
	private final static String TAG = "BallShaderProgram";

	// Uniform locations
	private final int uModelMatrixLocation;
	private final int uViewProjectionMatrixLocation;
	private final int uMVPMatrixLocation;


	public BasicShaderProgram(Context context)
	{
		super(context, R.raw.basic_vertex_shader, R.raw.basic_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX);
		uViewProjectionMatrixLocation = glGetUniformLocation(program, "u_ViewProjectionMatrix");
		uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);
	}

	public void setUniforms(float[] uModelMatrix, float[] uViewProjectionMatrix, float[] uMVPMatrix)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uViewProjectionMatrixLocation, 1, false, uViewProjectionMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);
	}
}
