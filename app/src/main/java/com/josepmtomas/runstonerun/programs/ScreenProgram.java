package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 09/09/2014.
 * @author Josep
 */

public class ScreenProgram extends ShaderProgram
{
	private final int colorSamplerLocation;

	public ScreenProgram(Context context)
	{
		super(context, "shaders/screen.vs", "shaders/screen.fs");

		colorSamplerLocation = glGetUniformLocation(program, "colorSampler");
	}


	public void setUniforms(int colorSampler)
	{
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorSampler);
		glUniform1i(colorSamplerLocation, 0);
	}
}
