package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 30/11/2014.
 * @author Josep
 */

public class RockProgram extends ShaderProgram
{
	private final int rockPropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int viewProjectionLocation;

	private final int diffuseSamplerLocation;
	private final int normalSamplerLocation;


	public RockProgram(Context context)
	{
		super(context, "shaders/rock.vs", "shaders/rock.fs");

		rockPropertiesBlockLocation = glGetUniformBlockIndex(program, "rockProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
		normalSamplerLocation = glGetUniformLocation(program, "normalSampler");
	}


	public void setCommonUniforms(float[] viewProjection, int diffuseSampler, int normalSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, normalSampler);
		glUniform1i(normalSamplerLocation, 1);
	}


	public void setSpecificUniforms(int rockProperties)
	{
		glUniformBlockBinding(program, rockPropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, rockProperties);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}
}
