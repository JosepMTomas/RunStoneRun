package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 10/12/2014.
 */
public class BrokenTreeProgram extends ShaderProgram
{
	private final int modelLocation;
	private final int modelViewProjectionLocation;

	private final int lightInfoBlockLocation;

	private final int diffuseSamplerLocation;


	public BrokenTreeProgram(Context context)
	{
		super(context, "shaders/broken_tree.vs", "shaders/broken_tree.fs");

		modelLocation = glGetUniformLocation(program, "model");
		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");

		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}


	public void setCommonUniforms(int diffuseSampler)
	{
		//TODO: lightInfo block

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}


	public void setSpecificUniforms(float[] model, float[] modelViewProjection)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
	}
}
