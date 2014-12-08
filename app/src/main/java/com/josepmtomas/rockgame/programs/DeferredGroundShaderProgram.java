package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 25/08/2014.
 */
public class DeferredGroundShaderProgram  extends ShaderProgram
{
	private final int uCurrentMMatrixLocation;		// Current Model matrix
	private final int uCurrentMVMatrixLocation;		// Current Model-View matrix
	private final int uPreviousMVMatrixLocation;	// Previous Model-View matrix
	private final int uMVPMatrixLocation;			// Model-View-Projection matrix
	private final int uShadowMatrixLocation;

	private final int uGrassColorTextureLocation;
	private final int uShadowMapLocation;

	public DeferredGroundShaderProgram(Context context)
	{
		super(context, "shaders/ground.vs", "shaders/ground.fs");

		uCurrentMMatrixLocation = glGetUniformLocation(program, "uCurrentModelMatrix");
		uCurrentMVMatrixLocation = glGetUniformLocation(program, "uCurrentModelViewMatrix");
		uPreviousMVMatrixLocation = glGetUniformLocation(program, "uPreviousModelViewMatrix");
		uMVPMatrixLocation = glGetUniformLocation(program, "uModelViewProjectionMatrix");
		uShadowMatrixLocation = glGetUniformLocation(program, "uShadowMatrix");

		uGrassColorTextureLocation = glGetUniformLocation(program, "uGrassColorTexture");

		uShadowMapLocation = glGetUniformLocation(program, "uShadowMap");
	}


	public void setUniforms(float[] uCurrentModelMatrix, float[] uCurrentModelViewMatrix, float[] uPreviousModelViewMatrix,
							float[] uMVPMatrix, float[] uShadowMatrix, int[] textures, int shadowSampler)
	{
		glUniformMatrix4fv(uCurrentMMatrixLocation, 0, false, uCurrentModelMatrix, 0);
		glUniformMatrix4fv(uCurrentMVMatrixLocation, 0, false, uCurrentModelViewMatrix, 0);
		glUniformMatrix4fv(uPreviousMVMatrixLocation, 0, false, uPreviousModelViewMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 0, false, uMVPMatrix, 0);
		glUniformMatrix4fv(uShadowMatrixLocation, 0, false, uShadowMatrix, 0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowSampler);
		glUniform1i(uShadowMapLocation, 0);

		// Bind the textures to its unit.
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, textures[0]);
		glUniform1i(uGrassColorTextureLocation, 1);
	}
}
