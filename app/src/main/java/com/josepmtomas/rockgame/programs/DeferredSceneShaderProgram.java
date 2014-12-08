package com.josepmtomas.rockgame.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 24/08/2014.
 */
public class DeferredSceneShaderProgram extends ShaderProgram
{
	private final static String TAG = "DeferredSceneShader";

	private final int uModelMatrixLocation;
	private final int uModelViewMatrixLocation;
	private final int uMVPMatrixLocation;


 	public DeferredSceneShaderProgram(Context context)
	{
		super(context, "shaders/test.vs", "shaders/test.fs");

		uModelMatrixLocation = glGetUniformLocation(program, "uModelMatrix");
		uModelViewMatrixLocation = glGetUniformLocation(program, "uModelViewMatrix");
		uMVPMatrixLocation = glGetUniformLocation(program, "uModelViewProjectionMatrix");
	}

	public void setUniforms(float[] uModelMatrix, float[] uModelViewMatrix, float[] uMVPMatrix)
	{
		glUniformMatrix4fv(uModelMatrixLocation, 1, false, uModelMatrix, 0);
		glUniformMatrix4fv(uModelViewMatrixLocation, 1, false, uModelViewMatrix, 0);
		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, uMVPMatrix, 0);
	}
}
