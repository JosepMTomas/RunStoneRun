package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static com.josepmtomas.rockgame.Constants.*;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 08/10/2014.
 */
public class TreeProgram extends ShaderProgram
{
	private final int viewProjectionLocation;

	private final int treePropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int diffuseSamplerLocation;


	public TreeProgram(Context context)
	{
		super(context, "shaders/tree_new.vs", "shaders/tree_new.fs");

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

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}


	public void setSpecificUniforms(int treePropertiesUbo)
	{
		// Model matrices uniform block binding
		glUniformBlockBinding(program, treePropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, treePropertiesUbo);
	}

	/*private final int shadowMatrixLocation;
	private final int viewProjectionLocation;

	private final int treePropertiesBlockLocation;

	private final int modelMatricesBlockLocation;
	private final int modelIndicesBlockLocation;
	private final int worldModelMatricesBlockLocation;
	private final int worldMVPMatricesBlockLocation;

	private final int shadowMapSamplerLocation;
	private final int diffuseSamplerLocation;

	//DEBUG
	private final int lodLocation;


	public TreeProgram(Context context)
	{
		super(context, "shaders/tree.vs", "shaders/tree.fs");

		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");
		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		treePropertiesBlockLocation = glGetUniformBlockIndex(program, "treeProperties");

		//uniformBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
		modelMatricesBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");
		modelIndicesBlockLocation = glGetUniformBlockIndex(program, "modelIndices");
		worldModelMatricesBlockLocation = glGetUniformBlockIndex(program, "worldModelMatrices");
		worldMVPMatricesBlockLocation = glGetUniformBlockIndex(program, "worldMVPMatrices");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");

		//DEBUG
		lodLocation = glGetUniformLocation(program, "lod");
	}*/


	/*public void setUniforms(float[] worldModel, float[] viewProjection, int matricesUbo, int diffuseSampler)
	{
		glUniformMatrix4fv(worldModelLocation, 1, false, worldModel, 0);
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glUniformBlockBinding(program, uniformBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, matricesUbo, 0, 16 * Constants.BYTES_PER_FLOAT * 128);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 0);
	}*/


	/*public void setUniforms(float[] viewProjection, float[] shadowMatrix, int treePropertiesUbo, int worldMVPMatricesUbo, int shadowMapSampler, int diffuseSampler, int lod)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		// Tree properties uniform block binding
		glUniformBlockBinding(program, modelMatricesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, treePropertiesUbo);

		// World MVP
		glUniformBlockBinding(program, worldMVPMatricesBlockLocation, 1);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, worldMVPMatricesUbo);

		// Shadow texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		// Tree texture
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 1);

		// DEBUG LOD
		glUniform1i(lodLocation, lod);
	}


	public void setUniforms(float[] viewProjection, float[] shadowMatrix, int modelMatricesUbo, int modelIndicesUbo, int worldModelMatricesUbo, int shadowMapSampler, int diffuseSampler, int lod)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		// Model matrices uniform block binding
		glUniformBlockBinding(program, modelMatricesBlockLocation, 0);
		//glBindBufferRange(GL_UNIFORM_BUFFER, 0, modelMatricesUbo, 0, 16 * MAX_TREE_INSTANCES_TOTAL * BYTES_PER_FLOAT );
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, modelMatricesUbo);

		// Model matrices indices uniform block binding
		glUniformBlockBinding(program, modelIndicesBlockLocation, 1);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, modelIndicesUbo);
		//glBindBufferRange(GL_UNIFORM_BUFFER, 1, modelIndicesUbo, 0, MAX_TREE_INSTANCES_TOTAL * BYTES_PER_INT);

		// World model matrices uniform block binding
		glUniformBlockBinding(program, worldMVPMatricesBlockLocation, 2);
		glBindBufferBase(GL_UNIFORM_BUFFER, 2, worldModelMatricesUbo);
		//glBindBufferRange(GL_UNIFORM_BUFFER, 2, worldModelMatricesUbo, 0, 16 * 9 * BYTES_PER_FLOAT);

		// Shadow texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		// Tree texture
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 1);

		// DEBUG LOD
		glUniform1i(lodLocation, lod);
	}*/
}
