package com.josepmtomas.rockgame.programs;

import android.content.Context;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 05/09/2014.
 * @author Josep
 */

public class ShadowPassProgram extends ShaderProgram
{
	private final int uModelViewProjectionLocation;


	public ShadowPassProgram(Context context)
	{
		super(context, "shaders/shadow_map_pass.vs", "shaders/shadow_map_pass.fs");

		uModelViewProjectionLocation = glGetUniformLocation(program, "modelViewProjection");
	}


	public void setUniforms(float[] modelViewProjection)
	{
		glUniformMatrix4fv(uModelViewProjectionLocation, 0, false, modelViewProjection, 0);
	}
}
