package com.josepmtomas.runstonerun.programs;

import android.content.Context;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 01/11/2014.
 * @author Josep
 */

public class ObjectsPatchDebugProgram extends ShaderProgram
{
	private final int modelLocation;
	private final int viewProjectionLocation;
	private final int lodLocation;
	private final int distLocation;

	public ObjectsPatchDebugProgram(Context context)
	{
		super(context, "shaders/objects_patch_debug.vs", "shaders/objects_patch_debug.fs");

		modelLocation = glGetUniformLocation(program, "model");
		viewProjectionLocation = glGetUniformLocation(program, "viewProjection");
		lodLocation = glGetUniformLocation(program, "lod");
		distLocation = glGetUniformLocation(program, "dist");
	}


	public void setCommonUniforms(float[] viewProjection)
	{
		glUniformMatrix4fv(viewProjectionLocation, 1, false, viewProjection, 0);
	}


	public void setSpecificUniforms(float[] model, int lod, float dist)
	{
		glUniformMatrix4fv(modelLocation, 1, false, model, 0);
		glUniform1i(lodLocation, lod);
		glUniform1f(distLocation, dist);
	}
}
