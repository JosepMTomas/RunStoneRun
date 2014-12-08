package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.Constants;

import static android.opengl.GLES30.*;

public class TreeTrunkProgram extends ShaderProgram
{
	private final int uWorldModelLocation;
	private final int uViewProjectionLocation;

	private final int uniformBlockLocation;


	public TreeTrunkProgram(Context context)
	{
		super(context, "shaders/tree_trunk.vs", "shaders/tree_trunk.fs");

		uWorldModelLocation = glGetUniformLocation(program, "worldModel");
		uViewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		uniformBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
	}


	//TODO: World model
	public void setUniforms(float[] viewProjection, float[] worldModel, int matricesUbo)
	{
		glUniformBlockBinding(program, uniformBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, matricesUbo, 0, 16 * Constants.BYTES_PER_FLOAT * 128);

		glUniformMatrix4fv(uWorldModelLocation, 1, false, worldModel, 0);
		glUniformMatrix4fv(uViewProjectionLocation, 1, false, viewProjection, 0);
	}
}