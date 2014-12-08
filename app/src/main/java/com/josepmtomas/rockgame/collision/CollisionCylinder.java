package com.josepmtomas.rockgame.collision;

import com.josepmtomas.rockgame.algebra.vec3;

/**
 * Created by Josep on 25/07/2014.
 */
public class CollisionCylinder
{
	public vec3 bottom;
	public vec3 top;
	public float radius;


	public CollisionCylinder()
	{
		this.bottom = new vec3(0.0f);
		this.top = new vec3(0.0f);
		this.radius = 0.0f;
	}


	public CollisionCylinder(vec3 bottom, vec3 top, float radius)
	{
		this.bottom = new vec3(bottom);
		this.top = new vec3(top);
		this.radius = radius;
	}
}
