package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.programs.RockShaderProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_INT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;

/**
 * Created by Josep on 17/07/2014.
 */
public class Rock extends StaticMesh
{
	private final static String TAG = "Rock";

	private float[] modelViewProjectionMatrix;
	private float[] modelMatrix;

	private vec3 eyeVector;

	private RockShaderProgram rockShaderProgram;
	private int normalTexture;


	public Rock(Context context, int resourceId) {
		super(context, resourceId);

		rockShaderProgram = new RockShaderProgram(context);

		normalTexture = TextureHelper.loadTexture(context, R.raw.stonework);
	}


	@Override
	public void initialize() {
		super.initialize();
	}


	@Override
	public void bind() {
		super.bind();

		vertexBuffer.setVertexAttribPointer(
				POSITION_BYTE_OFFSET,
				rockShaderProgram.getPositionAttributeLocation(),
				POSITION_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TEXCOORD_BYTE_OFFSET,
				rockShaderProgram.getTexCoordAttributeLocation(),
				TEXCOORD_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				NORMAL_BYTE_OFFSET,
				rockShaderProgram.getNormalAttributeLocation(),
				NORMAL_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TANGENT_BYTE_OFFSET,
				rockShaderProgram.getTangentAttributeLocation(),
				TANGENT_COMPONENTS,
				STRIDE);
	}


	public void update(float[] modelMatrix, float[] modelViewProjectionMatrix, vec3 eyeVector)
	{
		this.modelMatrix = modelMatrix;
		this.modelViewProjectionMatrix = modelViewProjectionMatrix;
		this.eyeVector = eyeVector;
	}


	@Override
	public void draw() {
		super.draw();

		rockShaderProgram.useProgram();

		rockShaderProgram.setUniforms(modelMatrix, modelViewProjectionMatrix, eyeVector, normalTexture);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		//Log.d(TAG,"NumElements = " + indexBuffer.getNumElements());
		glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_INT, 0);
		//glDrawElements(GL_TRIANGLES,36, GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
