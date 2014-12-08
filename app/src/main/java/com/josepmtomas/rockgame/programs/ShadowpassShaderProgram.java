package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 15/08/2014.
 */
public class ShadowpassShaderProgram extends ShaderProgram
{
	private static final String TAG = "ShadowpassShaderProgram";

	private int uMVPMatrixLocation;


	public ShadowpassShaderProgram(Context context)
	{
		super(context, R.raw.shadowpass_vertex_shader, R.raw.shadowpass_fragment_shader);

		uMVPMatrixLocation = glGetUniformLocation(program, "MVP");
	}


	public void setUniforms(float[] modelViewProjectionMatrix)
	{
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
	}
}
