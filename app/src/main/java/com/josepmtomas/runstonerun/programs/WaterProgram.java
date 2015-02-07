package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 28/10/2014.
 * @author Josep
 */

public class WaterProgram extends ShaderProgram
{
	private final int shadowMapSamplerLocation;
	private final int reflectionSamplerLocation;
	private final int waterNormalSamplerLocation;

	private final int modelLocation;
	private final int modelViewProjectionLocation;
	private final int shadowMatrixLocation;

	private final int framebufferDimensionsLocation;


	public WaterProgram(Context context)
	{
		super(context, "shaders/water.vs", "shaders/water.fs");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		reflectionSamplerLocation = glGetUniformLocation(program, "reflectionSampler");
		waterNormalSamplerLocation = glGetUniformLocation(program, "waterNormalSampler");

		modelLocation = glGetUniformLocation(program, "model");
		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");

		framebufferDimensionsLocation = glGetUniformLocation(program, "framebufferDimensions");
	}


	public void setCommonUniforms(float[] shadowMatrix, float[] framebufferDimensions, int shadowMapSampler, int reflectionSampler, int waterNormalSampler)
	{
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glUniform2fv(framebufferDimensionsLocation, 1, framebufferDimensions, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, reflectionSampler);
		glUniform1i(reflectionSamplerLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, waterNormalSampler);
		glUniform1i(waterNormalSamplerLocation, 2);
	}


	public void setSpecificUniforms(float[] model, float[] modelViewProjection)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
	}
}