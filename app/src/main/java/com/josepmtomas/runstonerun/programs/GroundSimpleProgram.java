package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 29/10/2014.
 * @author Josep
 */

public class GroundSimpleProgram extends ShaderProgram
{
	private final int modelLocation;
	private final int modelViewProjectionLocation;
	private final int shadowMatrixLocation;

	private final int shadowMapSamplerLocation;
	private final int groundBlackSamplerLocation;
	private final int groundWhiteSamplerLocation;


	public GroundSimpleProgram(Context context)
	{
		super(context, "shaders/ground_simple.vs", "shaders/ground_simple.fs");

		modelLocation = glGetUniformLocation(program, "model");
		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		groundBlackSamplerLocation = glGetUniformLocation(program, "groundBlackSampler");
		groundWhiteSamplerLocation = glGetUniformLocation(program, "groundWhiteSampler");
	}


	public void setCommonUniforms(float[] shadowMatrix, int shadowMapSampler, int groundBlackSampler, int groundWhiteSampler)
	{
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, groundBlackSampler);
		glUniform1i(groundBlackSamplerLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, groundWhiteSampler);
		glUniform1i(groundWhiteSamplerLocation, 2);
	}


	public void setSpecificUniforms(float[] model, float[] modelViewProjection)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
	}
}