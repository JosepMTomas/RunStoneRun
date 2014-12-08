package com.josepmtomas.rockgame.objects;

import android.content.Context;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.physics.SpherePhysics;
import com.josepmtomas.rockgame.programs.BallShaderProgram;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 27/07/2014.
 */
public class Ball extends StaticMesh
{
	private float[] modelMatrix = new float[16];
	private float[] viewMatrix;
	private float[] projectionMatrix;
	private float[] modelViewMatrix = new float[16];
	private float[] modelViewProjectionMatrix = new float[16];

	private vec3 position;
	private SpherePhysics spherePhysics;
	private BallShaderProgram ballShaderProgram;


	public Ball(Context context, int resourceId)
	{
		super(context, resourceId);

		position = new vec3(0.0f);
		spherePhysics = new SpherePhysics(position, collisionSpheres.get(0));
		//spherePhysics.enable();
		spherePhysics.setCurrentVelocity(0.0f, 10.0f, 0.0f);
		ballShaderProgram = new BallShaderProgram(context);
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
				ballShaderProgram.getPositionAttributeLocation(),
				POSITION_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TEXCOORD_BYTE_OFFSET,
				ballShaderProgram.getTexCoordAttributeLocation(),
				TEXCOORD_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				NORMAL_BYTE_OFFSET,
				ballShaderProgram.getNormalAttributeLocation(),
				NORMAL_COMPONENTS,
				STRIDE);

		vertexBuffer.setVertexAttribPointer(
				TANGENT_BYTE_OFFSET,
				ballShaderProgram.getTangentAttributeLocation(),
				TANGENT_COMPONENTS,
				STRIDE);
	}


	public void update(float[] viewMatrix, float[] projectionMatrix)
	{
		if(spherePhysics.isEnabled())
			spherePhysics.update();

		this.viewMatrix = viewMatrix;
		this.projectionMatrix = projectionMatrix;

		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, position.x, position.y, position.z);

		// Calculate ModelViewProjection matrix
		multiplyMM(modelViewMatrix, 0, this.viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, this.projectionMatrix, 0, modelViewMatrix, 0);
	}

	@Override
	public void draw() {
		super.draw();

		ballShaderProgram.useProgram();

		ballShaderProgram.setUniforms(modelMatrix, modelViewProjectionMatrix);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_INT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
