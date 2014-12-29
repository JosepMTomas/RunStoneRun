package com.josepmtomas.rockgame.programsForwardPlus;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 29/11/2014.
 */
public class ScorePanelProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int scaleLocation;
	private final int positionLocation;
	private final int texCoordOffsetLocation;

	private final int numbersSamplerLocation;
	private final int colorLocation;
	private final int opacityLocation;


	public ScorePanelProgram(Context context)
	{
		super(context, "shaders/hud_score.vs", "shaders/hud_score.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		scaleLocation = glGetUniformLocation(program, "scale");
		positionLocation = glGetUniformLocation(program, "position");
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


	public void setSpecificUniforms(float scale, float positionX, float positionY, float texCoordOffsetX, float texCoordOffsetY, float colorX, float colorY, float colorZ, float opacity)
	{
		glUniform4f(scaleLocation, scale, scale, 1f, 1f);
		glUniform4f(positionLocation, positionX, positionY, 0f, 1f);
		glUniform2f(texCoordOffsetLocation, texCoordOffsetX, texCoordOffsetY);
		glUniform3f(colorLocation, colorX, colorY, colorZ);
		glUniform1f(opacityLocation, opacity);
	}
}
