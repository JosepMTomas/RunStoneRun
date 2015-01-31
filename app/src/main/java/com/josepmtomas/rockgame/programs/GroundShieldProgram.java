package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 16/12/2014.
 * @author Josep
 */

public class GroundShieldProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int colorSamplerLocation;
	private final int colorLocation;
	private final int timeLocation;


	public GroundShieldProgram(Context context)
	{
		super(context, "shaders/ground_shield.vs", "shaders/ground_shield.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		colorSamplerLocation = glGetUniformLocation(program, "colorSampler");
		colorLocation = glGetUniformLocation(program, "color");
		timeLocation = glGetUniformLocation(program, "time");
	}


	public void setUniforms(float[] viewProjection, int colorSampler, float colorX, float colorY, float colorZ, float time)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorSampler);
		glUniform1i(colorSamplerLocation, 0);

		glUniform3f(colorLocation, colorX, colorY, colorZ);
		glUniform1f(timeLocation, time);
	}
}
