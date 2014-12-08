package com.josepmtomas.rockgame.programs;

import android.content.Context;

import com.josepmtomas.rockgame.util.ShaderHelper;
import com.josepmtomas.rockgame.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Josep on 17/07/2014.
 */
public class ShaderProgram
{
	// Uniform constants
	protected static final String U_MATRIX = "u_Matrix";
	protected static final String U_MODEL_MATRIX = "u_ModelMatrix";
	protected static final String U_ROTATION_MATRIX = "u_RotationMatrix";
	protected static final String U_MVP_MATRIX = "u_ModelViewProjectionMatrix";

	protected static final String U_EYE_POSITION = "u_EyePosition";

	protected static final String U_COLOR = "u_Color";
	protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
	protected static final String U_TIME = "u_Time";
	protected static final String U_VECTOR_TO_LIGHT = "u_VectorToLight";
	protected static final String U_POINT_LIGHT_POSITIONS = "u_PointLightPositions";
	protected static final String U_POINT_LIGHT_COLORS = "u_PointLightColors";

	// Attribute constants
	protected static final String A_POSITION = "a_Position";
	protected static final String A_TEXCOORD = "a_TexCoord";
	protected static final String A_NORMAL = "a_Normal";
	protected static final String A_TANGENT = "a_Tangent";
	protected static final String A_COLOR = "a_Color";

	protected static final String A_DIRECTION_VECTOR = "a_DirectionVector";
	protected static final String A_PARTICLE_START_TIME = "a_ParticleStartTime";

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
}
