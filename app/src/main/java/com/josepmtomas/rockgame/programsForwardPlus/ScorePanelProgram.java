package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 29/11/2014.
 */
public class ScorePanelProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int positionOffsetLocation;
	private final int texCoordOffsetLocation;

	private final int numbersSamplerLocation;
	private final int colorLocation;
	private final int opacityLocation;


	public ScorePanelProgram(Context context)
	{
		super(context, "shaders/hud_score.vs", "shaders/hud_score.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		positionOffsetLocation = glGetUniformLocation(program, "positionOffset");
		texCoordOffsetLocation = glGetUniformLocation(program, "texCoordOffset");

		numbersSamplerLocation = glGetUniformLocation(program, "numbersSampler");
		colorLocation = glGetUniformLocation(program, "color");
		opacityLocation = glGetUniformLocation(program, "opacity");
	}


	public void setCommonUniforms(float[] viewProjection, int numbersSampler)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, numbersSampler);
		glUniform1i(numbersSamplerLocation, 0);
	}


	public void setSpecificUniforms(float positionOffsetX, float positionOffsetY, float texCoordOffsetX, float texCoordOffsetY, float colorX, float colorY, float colorZ, float opacity)
	{
		glUniform4f(positionOffsetLocation, positionOffsetX, positionOffsetY, 0f, 1f);
		glUniform2f(texCoordOffsetLocation, texCoordOffsetX, texCoordOffsetY);
		glUniform3f(colorLocation, colorX, colorY, colorZ);
		glUniform1f(opacityLocation, opacity);
	}
}
