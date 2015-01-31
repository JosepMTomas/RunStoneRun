package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 05/09/2014.
 * @author Josep
 */

public class DepthPrePassProgram  extends ShaderProgram
{
	private final int uModelViewProjectionLocation;

	public DepthPrePassProgram(Context context)
	{
		super(context, "shaders/depth_pre_pass.vs", "shaders/depth_pre_pass.fs");

		uModelViewProjectionLocation = glGetUniformLocation(program, "uModelViewProjection");
	}


	public void setUniforms(float[] modelViewProjection)
	{
		glUniformMatrix4fv(uModelViewProjectionLocation, 1, false, modelViewProjection, 0);
	}
}
