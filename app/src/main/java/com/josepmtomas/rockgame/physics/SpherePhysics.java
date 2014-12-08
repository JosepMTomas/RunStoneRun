package com.josepmtomas.rockgame.physics;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.collision.CollisionSphere;

/**
 * Created by Josep on 27/07/2014.
 */
public class SpherePhysics
{
	private boolean enabled;

	private vec3 currentVelocity;
	private vec3 position;

	private CollisionSphere collisionSphere;


	public SpherePhysics(vec3 position, CollisionSphere collisionSphere)
	{
		this.collisionSphere = collisionSphere;

		enabled = false;
		currentVelocity = new vec3(0.0f);
		this.position = position;
	}


	public void update()
	{
		if(collisionSphere.center.y <= -0.1f)
		{
			//currentVelocity.multiply(0.95f);
			//currentVelocity.y = -currentVelocity.y;
			currentVelocity.multiply(0.99f);
			currentVelocity.negate();
			currentVelocity.reflect(new vec3(0f,1f,0f));
			//currentVelocity.multiply(0.0f);
		}
		else
		{
			//currentVelocity.multiply(0.97f);
			//currentVelocity.multiply(new vec3(1.0f, 1.0f, 0.95f));
			currentVelocity.subtract(0.0f, 0.2f, 0.0f);
			//currentVelocity.y -= (9.8f * 0.1);
			//currentVelocity.y = currentVelocity.y - 0.25f;
		}

		position.add(currentVelocity);
		collisionSphere.center.add(currentVelocity);
	}


	public void enable()
	{
		enabled = true;
	}

	public void disable()
	{
		enabled = false;
	}


	public boolean isEnabled()
	{
		return enabled;
	}


	public void setCurrentVelocity(float x, float y, float z)
	{
		currentVelocity.setValues(x, y, z);
	}
}
