package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 29/11/2014.
 */
public class TestUIProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int positionOffsetLocation;
	private final int texCoordOffsetLocation;

	private final int numbersSamplerLocation;


	public TestUIProgram(Context context)
	{
		super(context, "shaders/test_ui.vs", "shaders/test_ui.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		positionOffsetLocation = glGetUniformLocation(program, "positionOffset");
		texCoordOffsetLocation = glGetUniformLocation(program, "texCoordOffset");

		numbersSamplerLocation = glGetUniformLocation(program, "numbersSampler");
	}


	public void setCommonUniforms(float[] viewProjection, int numbersSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, numbersSampler);
		glUniform1i(numbersSamplerLocation, 0);
	}


	public void setSpecificUniforms(float[] positionOffset, float[] texCoordOffset)
	{
		glUniform4f(positionOffsetLocation, positionOffset[0], positionOffset[1], positionOffset[2], positionOffset[3]);

		glUniform2f(texCoordOffsetLocation, texCoordOffset[0], texCoordOffset[1]);
	}
}
