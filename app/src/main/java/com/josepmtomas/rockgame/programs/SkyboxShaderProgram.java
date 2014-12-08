package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 03/09/2014.
 */
public class SkyboxShaderProgram extends ShaderProgram
{
	private final static String TAG = "SkyboxShaderProgram";

	private final int uMVMatrixLocation;
	private final int uMVPMatrixLocation;
	private final int uSkyboxTextureLocation;

	public SkyboxShaderProgram(Context context)
	{
		super(context, "shaders/skybox.vs", "shaders/skybox.fs");

		uMVMatrixLocation = glGetUniformLocation(program, "uModelView");
		uMVPMatrixLocation = glGetUniformLocation(program, "uModelViewProjection");
		uSkyboxTextureLocation = glGetUniformLocation(program, "uSkyboxSampler");
	}


	public void setUniforms(float[] uMV, float[] uMVP, int texture)
	{
		glUniformMatrix4fv(uMVMatrixLocation, 0, false, uMV, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 0, false, uMVP, 0);

		glActiveTexture(GL_TEXTURE5);
		glBindTexture(GL_TEXTURE_CUBE_MAP, texture);
		glUniform1i(uSkyboxTextureLocation, 5);
	}
}
