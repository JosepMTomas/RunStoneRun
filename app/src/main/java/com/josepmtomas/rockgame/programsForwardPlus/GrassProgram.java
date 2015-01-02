package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 25/09/2014.
 */
public class GrassProgram extends ShaderProgram
{
	private final int shadowMatrixLocation;
	private final int viewProjectionLocation;

	private final int grassPropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int shadowMapSamplerLocation;
	private final int diffuseSamplerLocation;

	public GrassProgram(Context context)
	{
		super(context, "shaders/grass_new.vs", "shaders/grass_new.fs");

		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");
		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		grassPropertiesBlockLocation = glGetUniformBlockIndex(program, "grassProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}

	public void setCommonUniforms(float[] shadowMatrix, float[] viewProjection, int shadowMapSampler, int diffuseSampler)
	{
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 1);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}


	public void setSpecificUniforms(int grassPropertiesUbo)
	{

		glUniformBlockBinding(program, grassPropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, grassPropertiesUbo);
	}
}
