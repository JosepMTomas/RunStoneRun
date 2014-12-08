package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;

import static android.opengl.GLES20.*;

/**
 * Created by Josep on 18/07/2014.
 */
public class GroundShaderProgram extends ShaderProgram
{
	private final static String TAG = "GroundShaderProgram";

	private static final String U_GRASS_COLOR_TEXTURE = "u_GrassColorTexture";
	private static final String U_GRASS_NORMAL_TEXTURE = "u_GrassNormalTexture";
	private static final String U_GROUND_COLOR_TEXTURE = "u_GroundColorTexture";
	private static final String U_GROUND_ALPHA_TEXTURE = "u_GroundAlphaTexture";
	private static final String U_GROUND_NORMAL_TEXTURE = "u_GroundNormalTexture";

	// Uniforms locations
	private final int uModelMatrixLocation;
	//private final int uMVPMatrixLocation;
	//private final int uViewProjectionLocation;

	private final int uTimeLocation;

	//  Shadow mapping
	private final int uMVPMatrixLocation;
	private final int uMVMatrixLocation;
	private final int uMMatrixLocation;
	private final int uNMatrixLocation;
	private final int uSMatrixLocation;

	private final int uShadowMapSamplerLocation;

	private final int uGrassColorTextureLocation;
	private final int uGrassNormalTextureLocation;
	private final int uGroundColorTextureLocation;
	private final int uGroundAlphaTextureLocation;
	private final int uGroundNormalTextureLocation;

	public GroundShaderProgram(Context context)
	{
		super(context, R.raw.ground_vertex_shader, R.raw.ground_fragment_shader);

		// Get uniform locations
		uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX);

		uMVPMatrixLocation = glGetUniformLocation(program, "MVP");
		uMVMatrixLocation = glGetUniformLocation(program, "MV");
		uMMatrixLocation = glGetUniformLocation(program, "M");
		uNMatrixLocation = glGetUniformLocation(program, "N");
		uSMatrixLocation = glGetUniformLocation(program, "S");

		uShadowMapSamplerLocation = glGetUniformLocation(program, "shadowMap");

		uTimeLocation = glGetUniformLocation(program, U_TIME);

		uGrassColorTextureLocation = glGetUniformLocation(program, U_GRASS_COLOR_TEXTURE);
		uGrassNormalTextureLocation = glGetUniformLocation(program, U_GRASS_NORMAL_TEXTURE);
		uGroundColorTextureLocation = glGetUniformLocation(program, U_GROUND_COLOR_TEXTURE);
		uGroundAlphaTextureLocation = glGetUniformLocation(program, U_GROUND_ALPHA_TEXTURE);
		uGroundNormalTextureLocation = glGetUniformLocation(program, U_GROUND_NORMAL_TEXTURE);
	}


	public void setUniforms(float time, float[] uMVPMatrix, float[] uMVMatrix, float[] uMMatrix, float[] uNMatrix, float[] uSMatrix,
							int[] textures)
	{
		glUniform1f(uTimeLocation, time);
		//glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		//glUniformMatrix4fv(uViewProjectionLocation, 1, false, uMVPMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);
		glUniformMatrix4fv(uMVMatrixLocation, 1, false, uMVMatrix, 0);
		glUniformMatrix4fv(uMMatrixLocation, 1, false, uMMatrix, 0);
		glUniformMatrix4fv(uNMatrixLocation, 1, false, uNMatrix, 0);
		glUniformMatrix4fv(uSMatrixLocation, 1, false, uSMatrix, 0);

		// Bind the textures to its unit.
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, textures[0]);
		glUniform1i(uGrassColorTextureLocation, 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, textures[1]);
		glUniform1i(uGrassNormalTextureLocation, 2);

		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, textures[2]);
		glUniform1i(uGroundColorTextureLocation, 3);

		glActiveTexture(GL_TEXTURE4);
		glBindTexture(GL_TEXTURE_2D, textures[3]);
		glUniform1i(uGroundAlphaTextureLocation, 4);

		glActiveTexture(GL_TEXTURE5);
		glBindTexture(GL_TEXTURE_2D, textures[4]);
		glUniform1i(uGroundNormalTextureLocation, 5);
	}


	public void setShadowSampler(int sampler)
	{
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, sampler);
		glUniform1i(uShadowMapSamplerLocation, 0);
	}


	public void setModelmatrix(float[] uModelMatrix)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
	}
}
