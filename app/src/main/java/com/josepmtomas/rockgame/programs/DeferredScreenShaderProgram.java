package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/08/2014.
 */
public class DeferredScreenShaderProgram extends ShaderProgram
{
	private static final String TAG = "DeferredScreenShader";

	private final int uInverseVPMatrixLocation;
	private final int uInversePMatrixLocation;

	private final int uDepthSamplerLocation;
	private final int uNormalSamplerLocation;
	private final int uDiffuseSamplerLocation;
	private final int uEyeSamplerLocation;

	private final int uIndexLocation;

	public DeferredScreenShaderProgram(Context context)
	{
		super(context, "shaders/screen.vs", "shaders/screen.fs");

		uInverseVPMatrixLocation = glGetUniformLocation(program, "inverseViewProjection");
		uInversePMatrixLocation = glGetUniformLocation(program, "inverseProjection");

		uDepthSamplerLocation = glGetUniformLocation(program, "depthSampler");
		uNormalSamplerLocation = glGetUniformLocation(program, "normalSampler");
		uDiffuseSamplerLocation = glGetUniformLocation(program, "diffuseSampler");
		uEyeSamplerLocation = glGetUniformLocation(program, "eyeSampler");

		uIndexLocation = glGetUniformLocation(program, "index");
	}


	public void setUniforms(float[] inverseViewProjection, float[] inverseProjection, int[] buffers, int index)
	{
		glUniformMatrix4fv(uInverseVPMatrixLocation, 0, false, inverseViewProjection, 0);
		glUniformMatrix4fv(uInversePMatrixLocation, 0, false, inverseProjection, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, buffers[0]);
		glUniform1i(uDepthSamplerLocation, 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, buffers[1]);
		glUniform1i(uNormalSamplerLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, buffers[2]);
		glUniform1i(uDiffuseSamplerLocation, 2);

		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, buffers[3]);
		glUniform1i(uEyeSamplerLocation, 3);

		/*glActiveTexture(GL_TEXTURE4);
		glBindTexture(GL_TEXTURE_2D, buffers[4]);
		glUniform1i(uDepthBufferLocation, 4);*/

		glUniform1i(uIndexLocation, index);
	}
}
