package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 09/10/2014.
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

	/*private final int modelMatricesBlockLocation;
	private final int modelIndicesBlockLocation;
	private final int worldModelMatricesBlockLocation;
	private final int worldMVPMatricesBlockLocation;

	private final int viewProjectionLocation;


	public TreeShadowPassProgram(Context context)
	{
		super(context, "shaders/tree_shadow_pass.vs", "shaders/tree_shadow_pass.fs");

		modelMatricesBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
		modelIndicesBlockLocation = glGetUniformBlockIndex(program, "modelIndices");
		worldModelMatricesBlockLocation = glGetUniformBlockIndex(program, "worldModelMatrices");
		worldMVPMatricesBlockLocation = glGetUniformBlockIndex(program, "worldMVPMatrices");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
	}


	public void setUniforms(float[] viewProjection, int modelMatricesUbo, int modelIndicesUbo, int worldModelMatricesUbo)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		// Model matrices uniform block binding
		glUniformBlockBinding(program, modelMatricesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, modelMatricesUbo);

		// Model matrices indices uniform block binding
		glUniformBlockBinding(program, modelIndicesBlockLocation, 1);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, modelIndicesUbo);

		// World model matrices uniform block binding
		glUniformBlockBinding(program, worldMVPMatricesBlockLocation, 2);
		glBindBufferBase(GL_UNIFORM_BUFFER, 2, worldModelMatricesUbo);
	}*/
}
