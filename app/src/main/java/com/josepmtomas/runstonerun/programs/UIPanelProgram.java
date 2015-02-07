package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 22/12/2014.
 * @author Josep
 */

public class UIPanelProgram extends ShaderProgram
{
	private final int viewProjectionLocation;
	private final int positionScaleLocation;
	private final int positionTranslationLocation;
	private final int colorSamplerLocation;
	private final int opacityLocation;

	public UIPanelProgram(Context context)
	{
		super(context, "shaders/ui_panel.vs", "shaders/ui_panel.fs");

		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		positionScaleLocation = glGetUniformLocation(program, "positionScale");
		positionTranslationLocation = glGetUniformLocation(program, "positionTranslation");
		colorSamplerLocation = glGetUniformLocation(program, "colorSampler");
		opacityLocation = glGetUniformLocation(program, "opacity");
	}


	public void setUniforms(float[] viewProjection, float[] scale, float[] translation, int colorSampler, float opacity)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection,0);

		glUniform4f(positionScaleLocation, scale[0], scale[1], 1f, 1f);
		glUniform4f(positionTranslationLocation, translation[0], translation[1], 0f, 0f);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, colorSampler);
		glUniform1i(colorSamplerLocation, 0);

		glUniform1f(opacityLocation, opacity);
	}
}