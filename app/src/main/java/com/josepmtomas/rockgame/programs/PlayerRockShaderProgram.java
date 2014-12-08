package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;

import static android.opengl.GLES30.*;


/**
 * Created by Josep on 12/08/2014.
 */
public class PlayerRockShaderProgram extends ShaderProgram
{
	private final static String TAG = "PlayerRockShaderProgram";

	private int uCubeMapLocation;
	private int uEyePositionLocation;

	private int uModelMatrixLocation;
	private int uMVPMatrixLocation;

	public PlayerRockShaderProgram(Context context)
	{
		super(context, R.raw.player_rock_vertex_shader, R.raw.player_rock_fragment_shader);

		uCubeMapLocation = glGetUniformLocation(program, "uCubeMap");
		uEyePositionLocation = glGetUniformLocation(program, "uEyePosition");

		uModelMatrixLocation = glGetUniformLocation(program, "u_ModelMatrix");
		uMVPMatrixLocation = glGetUniformLocation(program, "u_ModelViewProjectionMatrix");
	}


	public void setUniforms(float[] modelMatrix, float[] modelViewProjectionMatrix, int cubeMapTexId, vec3 eyePosition)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, modelViewProjectionMatrix, 0);

		// Bind the textures to its unit.
		glActiveTexture(GL_TEXTURE10);
		glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTexId);
		glUniform1i(uCubeMapLocation, 10);

		glUniform3f(uEyePositionLocation, eyePosition.x, eyePosition.y, eyePosition.z);
	}
}
