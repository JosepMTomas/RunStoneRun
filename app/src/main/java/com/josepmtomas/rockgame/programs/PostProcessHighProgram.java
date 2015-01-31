package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 26/12/2014.
 * @author Josep
 */

public class PostProcessHighProgram extends ShaderProgram
{
	private final int colorSamplerLocation;
	private final int speedFactorLocation;

	public PostProcessHighProgram(Context context)
	{
		super(context, "shaders/post_process_high.vs", "shaders/post_process_high.fs");

		colorSamplerLocation = glGetUniformLocation(program, "colorSampler");
		speedFactorLocation = glGetUniformLocation(program, "speedFactor");
	}


	public void setUniforms(int colorSampler, float speedFactor)
	{
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorSampler);
		glUniform1i(colorSamplerLocation, 0);

		glUniform1f(speedFactorLocation, speedFactor);
	}
}
