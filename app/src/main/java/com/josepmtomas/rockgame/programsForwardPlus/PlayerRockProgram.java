package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 05/09/2014.
 */
public class PlayerRockProgram extends ShaderProgram
{
	private final int lightInfoBlockLocation;

	private final int modelLocation;
	private final int modelViewProjectionLocation;
	private final int shadowMatrixLocation;

	private final int shadowMapSamplerLocation;
	private final int diffuseSamplerLocation;
	private final int normalSamplerLocation;

	public PlayerRockProgram(Context context)
	{
		super(context, "shaders/player_rock.vs", "shaders/player_rock.fs");

		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		modelLocation = glGetUniformLocation(program, "model");
		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
		diffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
		normalSamplerLocation = glGetUniformLocation(program, "normalSampler");
	}


	public void setUniforms(float[] model, float[] modelViewProjection, float[] shadowMatrix, int shadowMapSampler, int diffuseSampler, int normalSampler)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, diffuseSampler);
		glUniform1i(diffuseSamplerLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, normalSampler);
		glUniform1i(normalSamplerLocation, 2);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}
}
