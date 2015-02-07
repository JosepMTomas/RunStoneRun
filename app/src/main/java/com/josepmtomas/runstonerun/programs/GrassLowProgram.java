package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static com.josepmtomas.runstonerun.Constants.*;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/11/2014.
 * @author Josep
 */

public class GrassLowProgram extends ShaderProgram
{
	private final int grassPropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int viewProjectionLocation;

	private final int diffuseSamplerLocation;


	public GrassLowProgram(Context context)
	{
		super(context, "shaders/grass_new_low.vs", "shaders/grass_new_low.fs");

		grassPropertiesBlockLocation = glGetUniformBlockIndex(program, "grassProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}


	public void setCommonUniforms(float[] viewProjection, int diffuseSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);

		glUniformBlockBinding(program, lightInfoBlockLocation, 3);
	}


	public void setSpecificUniforms(int grassPropertiesUbo)
	{
		glUniformBlockBinding(program, grassPropertiesBlockLocation, 1);
		glBindBufferRange(GL_UNIFORM_BUFFER, 1, grassPropertiesUbo, 0, 4 * BYTES_PER_FLOAT * 1024);
	}
}