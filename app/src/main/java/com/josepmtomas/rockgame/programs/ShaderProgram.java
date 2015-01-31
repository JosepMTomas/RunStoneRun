package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.util.ShaderHelper;
import com.josepmtomas.rockgame.util.TextResourceReader;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 17/07/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class ShaderProgram
{
	// Shader program
	protected final int program;


	protected ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId)
	{
		// Compile the shaders and link the program.
		program = ShaderHelper.buildProgram(
				TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
				TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId)
		);
	}


	protected ShaderProgram(Context context, String vertexShaderFileName, String fragmentShaderFileName)
	{
		// Compile the shaders and link the program
		program = ShaderHelper.buildProgram(
				TextResourceReader.readTextFileFromAsset(context, vertexShaderFileName),
				TextResourceReader.readTextFileFromAsset(context, fragmentShaderFileName)
		);
	}


	public void useProgram()
	{
		// Set the current OpenGL shader program to this program
		glUseProgram(program);
	}

	public int getProgram()
	{
		return program;
	}


	public void deleteProgram()
	{
		glDeleteProgram(program);
	}
}
