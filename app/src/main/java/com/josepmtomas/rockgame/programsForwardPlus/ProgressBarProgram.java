package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 08/12/2014.
 */
public class ProgressBarProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int positionOffsetLocation;
	private final int texCoordOffsetLocation;

	private final int progressSamplerLocation;
	private final int percentLocation;


	public ProgressBarProgram(Context context)
	{
		super(context, "shaders/hud_progress_bar.vs", "shaders/hud_progress_bar.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		positionOffsetLocation = glGetUniformLocation(program, "positionOffset");
		texCoordOffsetLocation = glGetUniformLocation(program, "texCoordOffset");

		progressSamplerLocation = glGetUniformLocation(program, "progressSampler");
		percentLocation = glGetUniformLocation(program, "percent");
	}


	public void setCommonUniforms(float[] viewProjection, int progressSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, progressSampler);
		glUniform1i(progressSamplerLocation, 0);
	}


	public void setSpecificUniforms(float positionOffsetX, float positionOffsetY, float percent)
	{
		glUniform4f(positionOffsetLocation, positionOffsetX, positionOffsetY, 0f, 1f);
		glUniform1f(percentLocation, percent);
	}
}
