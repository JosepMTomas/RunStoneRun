package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 08/10/2014.
 * @author Josep
 */

public class TreeProgram extends ShaderProgram
{
	private final int viewProjectionLocation;

	private final int treePropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int diffuseSamplerLocation;


	public TreeProgram(Context context)
	{
		super(context, "shaders/tree.vs", "shaders/tree.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		treePropertiesBlockLocation = glGetUniformBlockIndex(program, "treeProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
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
		// Model matrices uniform block binding
		glUniformBlockBinding(program, treePropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, treePropertiesUbo);
	}
}