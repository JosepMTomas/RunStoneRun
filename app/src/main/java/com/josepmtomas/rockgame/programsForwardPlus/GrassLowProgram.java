package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static com.josepmtomas.rockgame.Constants.*;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/11/2014.
 */
public class GrassLowProgram extends ShaderProgram
{
	private final int grassPropertiesBlockLocation;

	private final int viewProjectionLocation;

	private final int diffuseSamplerLocation;


	public GrassLowProgram(Context context)
	{
		super(context, "shaders/grass_new_low.vs", "shaders/grass_new_low.fs");

		grassPropertiesBlockLocation = glGetUniformBlockIndex(program, "grassProperties");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}


	public void setCommonUniforms(float[] viewProjection, int diffuseSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(program, 0);
	}


	public void setSpecificUniforms(int grassPropertiesUbo)
	{
		glUniformBlockBinding(program, grassPropertiesBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, grassPropertiesUbo, 0, 4 * BYTES_PER_FLOAT * 1024);
	}
}
