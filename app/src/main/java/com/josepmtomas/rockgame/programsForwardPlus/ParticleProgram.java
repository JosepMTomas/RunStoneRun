package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 16/01/2015.
 * @author Josep
 */

public class ParticleProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int propertiesBlockIndex;
	private final int diffuseSamplerLocation;


	public ParticleProgram(Context context)
	{
		super(context, "shaders/particle.vs", "shaders/particle.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		propertiesBlockIndex = glGetUniformBlockIndex(program, "particleProperties");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
	}


	public void setUniforms(float[] viewProjection, int propertiesUbo, int diffuseSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, propertiesUbo);
		glUniformBlockBinding(program, propertiesBlockIndex, 0);
	}
}
