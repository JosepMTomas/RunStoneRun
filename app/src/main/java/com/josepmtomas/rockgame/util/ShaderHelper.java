package com.josepmtomas.rockgame.util;

import android.util.Log;

import java.util.logging.Logger;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 17/07/2014.
 */
public class ShaderHelper
{
	private static final String TAG = "ShaderHelper";

	public static int compileVertexShader(String shaderCode)
	{
		return compileShader(GL_VERTEX_SHADER, shaderCode);
	}

	public static int compileFragmentShader(String shaderCode)
	{
		return compileShader(GL_FRAGMENT_SHADER, shaderCode);
	}

	private static int compileShader(int type, String shaderCode)
	{
		final int shaderObjectId = glCreateShader(type);
		final int[] compileStatus = new int[1];

		if(shaderObjectId == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not create new shader.");
			}

			return 0;
		}

		glShaderSource(shaderObjectId, shaderCode);
		glCompileShader(shaderObjectId);

		glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

		if(compileStatus[0] == 0)
		{
			//int[] logLength = new int[1];
			//glGetShaderiv(shaderObjectId, GL_INFO_LOG_LENGTH, logLength, 0);
			if(LoggerConfig.ON) Log.e(TAG, glGetShaderInfoLog(shaderObjectId));

			// If it failed, delete the shader object.
			glDeleteShader(shaderObjectId);

			if(LoggerConfig.ON)
			{
				Log.e(TAG,"Compilation of shader failed.");
			}

			return 0;
		}

		if(LoggerConfig.ON)
		{
			Log.d(TAG,"Compilation of shader OK.");
		}

		return shaderObjectId;
	}

	public static int linkProgram(int vertexShaderId, int fragmentShaderId)
	{
		final int programObjectId = glCreateProgram();
		final int[] linkStatus = new int[1];

		if(programObjectId == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not create new program");
			}

			return 0;
		}

		glAttachShader(programObjectId, vertexShaderId);
		glAttachShader(programObjectId, fragmentShaderId);

		glLinkProgram(programObjectId);

		glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

		if(LoggerConfig.ON)
		{
			// Print the program info log to the Android log output.
			Log.v(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId));
		}

		if(linkStatus[0] == 0)
		{
			// If it failed, delete the program object.
			glDeleteProgram(programObjectId);

			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Linking of program failed.");
			}

			return 0;
		}

		return programObjectId;
	}


	public static boolean validateProgram(int programObjectId)
	{
		glValidateProgram(programObjectId);

		final int[] validateStatus = new int[1];

		glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
		Log.v(TAG, "Results of validating program: " + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(programObjectId));

		return validateStatus[0] != 0;
	}


	public static int buildProgram(String vertexShaderSource, String fragmentShaderSource)
	{
		int program;

		// Compile the shaders
		Log.d(TAG, "Compiling vertex shader");
		int vertexShader = compileVertexShader(vertexShaderSource);
		Log.d(TAG, "Compiling fragment shader");
		int fragmentShader = compileFragmentShader(fragmentShaderSource);

		// Link them into a shader program
		Log.d(TAG, "Linking shaders");
		program = linkProgram(vertexShader, fragmentShader);

		if(LoggerConfig.ON)
		{
			validateProgram(program);
		}

		return program;
	}
}
