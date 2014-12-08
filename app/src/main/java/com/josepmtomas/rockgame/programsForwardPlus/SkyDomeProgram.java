package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 05/10/2014.
 */
public class SkyDomeProgram extends ShaderProgram
{
	private final int modelViewProjectionLocation;

	public SkyDomeProgram(Context context)
	{
		super(context, "shaders/sky_dome.vs", "shaders/sky_dome.fs");

		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
	}


	public void setUniforms(float[] modelViewProjection)
	{
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
	}
}
