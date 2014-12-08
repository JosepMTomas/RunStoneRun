package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/08/2014.
 */
public class PostProcessShaderProgram extends ShaderProgram
{
	private final int uDisplacementBufferLocation;
	private final int uColorBufferLocation;
	private final int uCurrentVPMatrixLocation;
	private final int uPreviousVPMatrixLocation;

	public PostProcessShaderProgram(Context context)
	{
		super(context, "shaders/post_process.vs", "shaders/post_process.fs");

		uDisplacementBufferLocation = glGetUniformLocation(program, "displacementBuffer");
		uColorBufferLocation = glGetUniformLocation(program, "colorBuffer");
		uCurrentVPMatrixLocation = glGetUniformLocation(program, "currentVPMatrix");
		uPreviousVPMatrixLocation = glGetUniformLocation(program, "previousVPMatrix");
	}


	public void setUniforms(int colorBuffer, int displacementBuffer, float[] currentVPMatrix, float[] previousVPMatrix)
	{
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorBuffer);
		glUniform1i(uColorBufferLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, displacementBuffer);
		glUniform1i(uDisplacementBufferLocation, 1);

		glUniformMatrix4fv(uCurrentVPMatrixLocation, 1, false, currentVPMatrix, 0);
		glUniformMatrix4fv(uPreviousVPMatrixLocation, 1, false, previousVPMatrix, 0);
	}
}
