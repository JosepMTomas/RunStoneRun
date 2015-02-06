package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/11/2014.
 * @author Josep
 */

public class TreeReflectionProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int treePropertiesBlockLocation;
	private final int diffuseSamplerLocation;

	private final int lightInfoBlockLocation;


	public TreeReflectionProgram(Context context)
	{
		super(context, "shaders/tree_reflection.vs", "shaders/tree_reflection.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		treePropertiesBlockLocation = glGetUniformBlockIndex(program, "treeProperties");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");

		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");
	}


	public void setCommonUniforms(float[] viewProjection, int diffuseSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);

		glUniformBlockBinding(program, lightInfoBlockLocation, 3);
	}


	public void setSpecificUniforms(int treePropertiesUbo)
	{
		glUniformBlockBinding(program, treePropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, treePropertiesUbo);
	}
}
