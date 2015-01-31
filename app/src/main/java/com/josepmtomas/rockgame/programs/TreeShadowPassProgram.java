package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 09/10/2014.
 * @author Josep
 */
public class TreeShadowPassProgram extends ShaderProgram
{
	private final int viewProjectionLocation;

	private final int treePropertiesBlockLocation;


	public TreeShadowPassProgram(Context context)
	{
		super(context, "shaders/tree_shadow_pass_new.vs", "shaders/tree_shadow_pass_new.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		treePropertiesBlockLocation = glGetUniformBlockIndex(program, "treeProperties");
	}


	public void setUniforms(float[] viewProjection, int treePropertiesUbo)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glUniformBlockBinding(program, treePropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, treePropertiesUbo);
	}
}
