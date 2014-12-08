package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.util.TextResourceReader;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 25/09/2014.
 */
public class GrassProgram extends ShaderProgram
{
	private static final String TAG = "GrassProgram";

	private final int shadowMatrixLocation;
	private final int viewProjectionLocation;

	private final int grassPropertiesBlockLocation;
	private final int lightInfoBlockLocation;

	private final int shadowMapSamplerLocation;
	private final int diffuseSamplerLocation;

	//TODO: debug
	private final int lodLocation;

	public GrassProgram(Context context)
	{
		super(context, "shaders/grass_new.vs", "shaders/grass_new.fs");

		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");
		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");

		grassPropertiesBlockLocation = glGetUniformBlockIndex(program, "grassProperties");
		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");

		//TODO: debug
		lodLocation = glGetUniformLocation(program, "lod");
	}

	public void setCommonUniforms(float[] shadowMatrix, float[] viewProjection, int shadowMapSampler, int diffuseSampler)
	{
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 1);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}


	public void setSpecificUniforms(int grassPropertiesUbo, int lod)
	{
		/*glUniformBlockBinding(program, grassPropertiesBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, grassPropertiesUbo, 0, 4 * Constants.BYTES_PER_FLOAT * 1024);*/
		glUniformBlockBinding(program, grassPropertiesBlockLocation, 0);
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, grassPropertiesUbo);

		glUniform1i(lodLocation, lod);
	}

	/*private final int uWorldModelLocation;
	private final int uViewProjectionLocation;
	private final int uShadowMatrixLocation;

	private final int uniformBlockLocation;

	private final int shadowMapSamplerLocation;
	private final int diffuseSamplerLocation;

	//TODO: debug
	private final int lodLocation;

	public GrassProgram(Context context)
	{
		super(context, "shaders/grass.vs", "shaders/grass.fs");

		uWorldModelLocation = glGetUniformLocation(program, "worldModel");
		uViewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		uShadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");

		uniformBlockLocation = glGetUniformBlockIndex(program, "modelMatrices");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");

		//TODO: debug
		lodLocation = glGetUniformLocation(program, "lod");
	}


	public void setUniforms(float[] viewProjection, float[] worldModel, float[] shadowMatrix, int matricesUbo, int shadowMapSampler, int diffuseTexId, int lod)
	{
		glUniformBlockBinding(program, uniformBlockLocation, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, 0, matricesUbo, 0, 16 * Constants.BYTES_PER_FLOAT * 128);

		glUniformMatrix4fv(uWorldModelLocation, 1, false, worldModel, 0);
		glUniformMatrix4fv(uViewProjectionLocation, 1, false, viewProjection, 0);
		glUniformMatrix4fv(uShadowMatrixLocation, 1, false, shadowMatrix, 0);

		//TODO: debug
		glUniform1i(lodLocation, lod);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseTexId);
		glUniform1i(diffuseSamplerLocation, 1);
	}*/
}
