package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import com.josepmtomas.runstonerun.Constants;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 06/10/2014.
 * @author Josep
 */

public class ShadowPassInstancedProgram extends ShaderProgram
{
	private final int worldModelLocation;
	private final int viewProjectionLocation;

	private final int uniformBlockLocation;


	public ShadowPassInstancedProgram(Context context)
	{
		super(context, "shaders/shadow_map_pass_instanced.vs", "shaders/shadow_map_pass_instanced.fs");

		worldModelLocation = glGetUniformLocation(program, "worldModel");
		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		uniformBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
	}


	public void setUniforms(float[] worldModel, float[] viewProjection, int matricesUbo)
	{
		glUniformBlockBinding(program, uniformBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, matricesUbo, 0, 16 * Constants.BYTES_PER_FLOAT * 128);

		glUniformMatrix4fv(worldModelLocation, 1, false, worldModel, 0);
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);
	}
}