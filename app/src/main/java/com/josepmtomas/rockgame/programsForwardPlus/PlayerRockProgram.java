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

	public PlayerRockProgram(Context context)
	{
		super(context, "shaders/player_rock.vs", "shaders/player_rock.fs");

		lightInfoBlockLocation = glGetUniformBlockIndex(program, "lightInfo");

		modelLocation = glGetUniformLocation(program, "model");
		modelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
		shadowMatrixLocation = glGetUniformLocation(program, "shadowMatrix");

		shadowMapSamplerLocation = glGetUniformLocation(program, "shadowMapSampler");
	}


	public void setUniforms(float[] model, float[] modelViewProjection, float[] shadowMatrix, int shadowMapSampler)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniformMatrix4fv(modelViewProjectionLocation, 1, false, modelViewProjection, 0);
		glUniformMatrix4fv(shadowMatrixLocation, 1, false, shadowMatrix, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapSampler);
		glUniform1i(shadowMapSamplerLocation, 0);

		glUniformBlockBinding(program, lightInfoBlockLocation, 5);
	}
}
