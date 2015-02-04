package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 01/12/2014.
 * @author Josep
 */

public class RockLowProgram extends ShaderProgram
{
	private final int rockPropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int viewProjectionLocation;

	private final int diffuseSamplerLocation;


	public RockLowProgram(Context context)
	{
		super(context, "shaders/rock_low.vs", "shaders/rock_low.fs");

		rockPropertiesBlockLocation = glGetUniformBlockIndex(program, "rockProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}


	public void setCommonUniforms(float[] viewProjection, int diffuseSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);
	}


	public void setSpecificUniforms(int rockProperties)
	{
		glUniformBlockBinding(program, rockPropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, rockProperties);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}
}
