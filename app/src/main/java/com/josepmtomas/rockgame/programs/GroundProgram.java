package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 06/09/2014.
 * @author Josep
 */

public class GroundProgram  extends ShaderProgram
{
	private final int lightInfoBlockLocation;

	private final int modelLocation;
	private final int modelViewProjectionLocation;
	private final int shadowMatrixLocation;

	private final int framebufferDimensionsLocation;

	private final int shadowMapSamplerLocation;
	private final int reflectionSamplerLocation;
	private final int waterNormalSamplerLocation;

	private final int groundBlackSamplerLocation;
	private final int groundWhiteSamplerLocation;

	private final int groundBlackNormalSamplerLocation;

	//DEBUG
	private final int lodLocation;

	public GroundProgram(Context context)
	{
		super(context, "shaders/ground.vs", "shaders/ground.fs");

		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		modelLocation = glGetUniformLocation(program, "uModel");
		modelViewProjectionLocation = glGetUniformLocation(program, "uModelViewProjection");
		shadowMatrixLocation = glGetUniformLocation(program, "uShadowMatrix");

		framebufferDimensionsLocation = glGetUniformLocation(program, "uFramebufferDimensions");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		reflectionSamplerLocation = glGetUniformLocation(program, "reflectionSampler");
		waterNormalSamplerLocation = glGetUniformLocation(program, "waterNormalSampler");

		groundBlackSamplerLocation = glGetUniformLocation(program, "groundBlackSampler");
		groundWhiteSamplerLocation = glGetUniformLocation(program, "groundWhiteSampler");

		groundBlackNormalSamplerLocation = glGetUniformLocation(program, "groundBlackNormalSampler");

		//DEBUG
		lodLocation = glGetUniformLocation(program, "lod");
	}


	public void setCommonUniforms(int[] groundSamplers, int normalSampler, int shadowMapSampler, int reflectionSampler, int waterNormalSampler, float[] shadowMatrix, float[] dimensions)
	{
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glUniform2fv(framebufferDimensionsLocation, 1, dimensions, 0);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, reflectionSampler);
		glUniform1i(reflectionSamplerLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, waterNormalSampler);
		glUniform1i(waterNormalSamplerLocation, 2);

		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, groundSamplers[0]);
		glUniform1i(groundBlackSamplerLocation, 3);

		glActiveTexture(GL_TEXTURE4);
		glBindTexture(GL_TEXTURE_2D, groundSamplers[1]);
		glUniform1i(groundWhiteSamplerLocation, 4);

		glActiveTexture(GL_TEXTURE5);
		glBindTexture(GL_TEXTURE_2D, normalSampler);
		glUniform1i(groundBlackNormalSamplerLocation, 5);
	}


	public void setPatchUniforms(float[] model, float[] modelViewProjection, int lod)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);

		glUniform1i(lodLocation, lod);
	}


	public void setUniforms(
			float[] model,
			float[] modelViewProjection,
			float[] shadowMatrix,
			int shadowMapSampler,
			int reflectionSampler,
			int waterNormalSampler,
			float[] dimensions)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glUniform2fv(framebufferDimensionsLocation, 1, dimensions, 0);

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
}
