package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 05/10/2014.
 * @author Josep
 */

public class SkyDomeProgram extends ShaderProgram
{
	private final int modelViewProjectionLocation;

	private final int gradient1SamplerLocation;
	private final int gradient2SamplerLocation;

	private final int percentLocation;


	public SkyDomeProgram(Context context)
	{
		super(context, "shaders/sky_dome.vs", "shaders/sky_dome.fs");

		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");

		gradient1SamplerLocation = glGetUniformLocation(program, "gradient1Sampler");
		gradient2SamplerLocation = glGetUniformLocation(program, "gradient2Sampler");

		percentLocation = glGetUniformLocation(program, "percent");
	}


	public void setUniforms(float[] modelViewProjection, int gradient1Sampler, int gradient2Sampler, float percent)
	{
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);

		glUniform1f(percentLocation, percent);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, gradient1Sampler);
		glUniform1i(gradient1SamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, gradient2Sampler);
		glUniform1i(gradient2SamplerLocation, 1);
	}
}
