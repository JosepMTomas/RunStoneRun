package com.josepmtomas.rockgame.algebra;

import android.util.FloatMath;

/**
 * Created by Josep on 16/07/2014.
 */
public class vec3
{
	public float x, y, z;

	public vec3(float xyz)
	{
		x = xyz;
		y = xyz;
		z = xyz;
	}

	public vec3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public vec3(float[] vector)
	{
		this.x = vector[0];
		this.y = vector[1];
		this.z = vector[2];
	}

	public vec3(vec3 vec)
	{
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public void add(vec3 b)
	{
		this.x += b.x;
		this.y += b.y;
		this.z += b.z;
	}

	public void add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void subtract(vec3 b)
	{
		this.x -= b.x;
		this.y -= b.y;
		this.z -= b.z;
	}

	public void subtract(float x, float y, float z)
	{
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}

	public void subtract(float[] vector)
	{
		this.x -= vector[0];
		this.y -= vector[1];
		this.z -= vector[2];
	}

	public float module()
	{
		//return sqrtf((a.x*a.x)+(a.y*a.y)+(a.z*a.z));
		return FloatMath.sqrt((x*x)+(y*y)+(z*z));
	}

	public void multiply(float value)
	{
		x = x * value;
		y = y * value;
		z = z * value;
	}


	public void multiply(vec3 vec)
	{
		x = x * vec.x;
		y = y * vec.y;
		z = z * vec.z;
	}


	public void negate()
	{
		x = -x;
		y = -y;
		z = -z;
	}


	public void normalize()
	{
		multiply(1.0f / this.module());
	}

	public void reflect(vec3 normal)
	{
		//return normalize(normal*2*dot(normal,direction) - direction);
		float dot = operations.dotProduct(normal, this);
		vec3 result = new vec3(normal);

		result.multiply(2.0f);
		result.multiply(dot);
		result.subtract(this);

		this.setValues(result);
	}

	public void setValues(float xyz)
	{
		this.x = xyz;
		this.y = xyz;
		this.z = xyz;
	}

	public void setValues(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setValues(float[] vector)
	{
		this.x = vector[0];
		this.y = vector[1];
		this.z = vector[2];
	}

	public void setValues(vec3 vec)
	{
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public float[] data()
	{
		float[] temp = new float[3];

		temp[0] = x;
		temp[1] = y;
		temp[2] = z;

		return temp;
	}

	public float[] data4()
	{
		float[] temp = new float[4];

		temp[0] = x;
		temp[1] = y;
		temp[2] = z;
		temp[3] = 1.0f;

		return temp;
	}
}
