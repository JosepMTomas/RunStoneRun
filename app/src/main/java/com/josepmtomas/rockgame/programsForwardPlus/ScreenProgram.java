package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 09/09/2014.
 */
public class ScreenProgram extends ShaderProgram
{
	private final int colorSamplerLocation;
	private final int speedFactorLocation;

	public ScreenProgram(Context context)
	{
		super(context, "shaders/screen.vs", "shaders/screen.fs");

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
