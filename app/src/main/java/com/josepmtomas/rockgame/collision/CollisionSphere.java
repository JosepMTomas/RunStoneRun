package com.josepmtomas.rockgame.collision;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.algebra.operations;

/**
 * Created by Josep on 23/07/2014.
 */
public class CollisionSphere
{
	public vec3 center;
	public float radius;


	public CollisionSphere()
	{
		this.center = new vec3(0f);
		this.radius = 0f;
	}


	public CollisionSphere(vec3 center, float radius)
	{
		this.center = new vec3(center);
		this.radius = radius;
	}


	public boolean intersects(CollisionSphere other)
	{
		// If the distance between the two centers is bigger than the sum of its radius, then exists
		// an intersection.
		if(operations.distanceBetween(this.center, other.center) <= this.radius + other.radius)
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public vec3 intersectionPoint(CollisionSphere other)
	{
		// Vector from the center of this sphere to the center of the other sphere
		vec3 vector = operations.subtract(other.center, this.center);

		// Distance of intersection from this center
		float distance = operations.distanceBetween(this.center, other.center);
		distance = this.radius + other.radius - distance;
		distance = this.radius - (distance / 2.0f);

		// Increment vector
		vector = operations.multiply(vector, distance);

		// Intersection point = this center * increment vector
		return operations.add(this.center, vector);
	}
}
