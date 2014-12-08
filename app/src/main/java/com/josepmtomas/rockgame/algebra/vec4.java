package com.josepmtomas.rockgame.algebra;

/**
 * Created by Josep on 04/08/2014.
 */
public class vec4
{
	public float x, y, z, w;

	public vec4(float xyzw)
	{
		x = xyzw;
		y = xyzw;
		z = xyzw;
		w = xyzw;
	}

	public vec4(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public vec4(vec3 vec, float w)
	{
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		this.w = w;
	}

	public vec4(vec4 vec)
	{
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		this.w = vec.w;
	}

	public void add(vec4 b)
	{
		this.x += b.x;
		this.y += b.y;
		this.z += b.z;
		this.w += b.w;
	}

	public void add(float x, float y, float z, float w)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
	}

	public void substract(vec4 b)
	{
		this.x -= b.x;
		this.y -= b.y;
		this.z -= b.z;
		this.w -= b.w;
	}

	public void substract(float x, float y, float z, float w)
	{
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
	}

	public void multiply(float value)
	{
		x = x * value;
		y = y * value;
		z = z * value;
		w = w * value;
	}


	public void multiply(vec4 vec)
	{
		x = x * vec.x;
		y = y * vec.y;
		z = z * vec.z;
		w = w * vec.w;
	}


	public void negate()
	{
		x = -x;
		y = -y;
		z = -z;
		w = -w;
	}

	public void setValues(float xyzw)
	{
		this.x = xyzw;
		this.y = xyzw;
		this.z = xyzw;
		this.w = xyzw;
	}

	public void setValues(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void setValues(vec4 vec)
	{
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		this.w = vec.w;
	}

	public float[] data()
	{
		float[] temp = new float[4];

		temp[0] = x;
		temp[1] = y;
		temp[2] = z;
		temp[3] = w;

		return temp;
	}
}
