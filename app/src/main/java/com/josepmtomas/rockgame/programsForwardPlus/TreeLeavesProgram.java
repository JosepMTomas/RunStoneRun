package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.Constants;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 07/09/2014.
 */
public class TreeLeavesProgram extends ShaderProgram
{
	private final int uWorldModelLocation;
	private final int uViewProjectionLocation;
	//private final int diffuseSamplerLocation;
	//private final int normalSamplerLocation;

	private final int uniformBlockLocation;


	public TreeLeavesProgram(Context context)
	{
		super(context, "shaders/tree_leaves.vs", "shaders/tree_leaves.fs");

		uWorldModelLocation = glGetUniformLocation(program, "worldModel");
		uViewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		//diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
		//normalSamplerLocation = glGetUniformLocation(program, "normalSampler");

		uniformBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
	}


	public void setUniforms(float[] viewProjection, float[] worldModel, int matricesUbo)
	{
		glUniformMatrix4fv(uWorldModelLocation, 1, false, worldModel, 0);
		glUniformMatrix4fv(uViewProjectionLocation, 1, false, viewProjection, 0);

		glUniformBlockBinding(program, uniformBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, matricesUbo, 0, 16 * Constants.BYTES_PER_FLOAT * 128);

		/*glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseTexture);
		glUniform1i(diffuseSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, normalTexture);
		glUniform1i(normalSamplerLocation, 1);*/
	}
}
