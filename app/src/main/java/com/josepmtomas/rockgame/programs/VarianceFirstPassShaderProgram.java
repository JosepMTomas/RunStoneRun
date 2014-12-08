package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;
/**
 * Created by Josep on 28/08/2014.
 */
public class VarianceFirstPassShaderProgram extends ShaderProgram
{
	private final int uMVPMatrixLocation;


	public VarianceFirstPassShaderProgram(Context context)
	{
		super(context, "shaders/variance_first_pass.vs", "shaders/variance_second_pass.fs");

		uMVPMatrixLocation = glGetUniformLocation(program, "ModelViewProjection");
	}


	public void setUniforms(float[] uMVPMatrix)
	{
		glUniformMatrix4fv(uMVPMatrixLocation, 0, false, uMVPMatrix, 0);
	}
}
