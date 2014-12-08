package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.FloatMath;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.algebra.vec4;
import com.josepmtomas.rockgame.programs.GrassShaderProgram;

import java.util.Random;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 04/08/2014.
 */
public class Grass extends StaticMesh
{
	private static final String TAG = "Grass";

	private float[] rotationMatrix = new float[16];
	private float[] modelMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];
	private float[] modelViewProjectionMatrix = new float[16];
	private float[] viewMatrix;
	private float[] projectionMatrix;

	private float initialRotationY;
	private float initialRotationX;

	private vec4 rotationAxisY;
	private vec4 rotationAxisX;
	private vec3 position;
	private GrassShaderProgram grassShaderProgram;

	public Grass(Context context)
	{
		super(context, R.raw.grass);

		grassShaderProgram = new GrassShaderProgram(context);

		position = new vec3(0.0f);
		rotationAxisY = new vec4(0.0f, 1.0f, 0.0f, 1.0f);
		rotationAxisX = new vec4(1.0f, 0.0f, 0.0f, 1.0f);

		initialRotationY = (new Random().nextFloat() * 2.0f) - 1.0f;
		initialRotationX = 75.0f;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	public void update(float[] viewMatrix, float[] projectionMatrix, float time)
	{
		float[] temp = new float[16];

		this.viewMatrix = viewMatrix;
		this.projectionMatrix = projectionMatrix;

		setIdentityM(rotationMatrix, 0);
		rotateM(rotationMatrix, 0, initialRotationX, 1.0f, 0.0f, 0.0f);
		//rotateM(rotationMatrix, 0, FloatMath.sin(time)*22.5f, 1.0f, 0.0f, 0.0f);
		//rotateM(rotationMatrix, 0, 0.0f, 0.0f, 1.0f, 0.0f);
		//translateM(modelMatrix, 0, 0.0f, -10.0f, 0.0f);

		setIdentityM(temp, 0);
		translateM(temp, 0, 0.0f, -10.0f, 0.0f);

		multiplyMM(modelMatrix, 0, rotationMatrix, 0, temp, 0);

		// Calculate ModelViewProjection matrix
		multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
	}

	@Override
	public void bind()
	{
		super.bind();

		vertexBuffer.setVertexAttribPointer(
				POSITION_BYTE_OFFSET,
				grassShaderProgram.getPositionAttributeLocation(),
				POSITION_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TEXCOORD_BYTE_OFFSET,
				grassShaderProgram.getTexCoordAttributeLocation(),
				TEXCOORD_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				NORMAL_BYTE_OFFSET,
				grassShaderProgram.getNormalAttributeLocation(),
				NORMAL_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TANGENT_BYTE_OFFSET,
				grassShaderProgram.getTangentAttributeLocation(),
				TANGENT_COMPONENTS,
				STRIDE);
	}

	@Override
	public void draw()
	{
		super.draw();

		grassShaderProgram.useProgram();

		grassShaderProgram.setUniforms(modelMatrix, rotationMatrix, modelViewProjectionMatrix);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void draw(float[] modelMatrix, float[] rotationMatrix)
	{
		super.draw();

		multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

		grassShaderProgram.useProgram();

		grassShaderProgram.setUniforms(modelMatrix, rotationMatrix, modelViewProjectionMatrix);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
