package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 08/12/2014.
 * @author Josep
 */

public class ProgressBarProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int scaleLocation;
	private final int positionLocation;

	private final int progressSamplerLocation;
	private final int colorLocation;
	private final int opacityLocation;
	private final int percentLocation;


	public ProgressBarProgram(Context context)
	{
		super(context, "shaders/hud_progress_bar.vs", "shaders/hud_progress_bar.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		scaleLocation = glGetUniformLocation(program, "scale");
		positionLocation = glGetUniformLocation(program, "position");

		progressSamplerLocation = glGetUniformLocation(program, "progressSampler");
		colorLocation = glGetUniformLocation(program, "color");
		opacityLocation = glGetUniformLocation(program, "opacity");
		percentLocation = glGetUniformLocation(program, "percent");
	}


	public void setCommonUniforms(float[] viewProjection, int progressSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, progressSampler);
		glUniform1i(progressSamplerLocation, 0);
	}


	public void setSpecificUniforms(float scale, float positionX, float positionY, float colorX, float colorY, float colorZ, float opacity, float percent)
	{
		glUniform4f(scaleLocation, scale, scale, 1f, 1f);
		glUniform4f(positionLocation, positionX, positionY, 0f, 1f);
		glUniform3f(colorLocation, colorX, colorY, colorZ);
		glUniform1f(opacityLocation, opacity);
		glUniform1f(percentLocation, percent);
	}
}