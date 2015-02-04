package com.josepmtomas.runstonerun.algebra;

import android.util.FloatMath;

/**
 * Created by Josep on 22/09/2014.ç
 * @author Josep
 */

@SuppressWarnings("unused")
public class vec2
{
    public float x;
    public float y;


	public vec2()
	{
		x = 0.0f;
		y = 0.0f;
	}

    public vec2(float _x, float _y)
    {
		x = _x;
		y = _y;
    }

	public float length()
	{
		return FloatMath.sqrt(x*x + y*y);
	}

	public boolean isEqual(vec2 b)
	{
		return (x == b.x && y == b.y);
	}

	public void add(vec2 b)
	{
		x += b.x;
		y += b.y;
	}

	public void add(float _x, float _y)
	{
		x += _x;
		y += _y;
	}

	public void add(float n)
	{
		x += n;
		y += n;
	}

	public void subtract(vec2 b)
	{
		x -= b.x;
		y -= b.y;
	}

	public void subtract(float n)
	{
		x -= n;
		y -= n;
	}

	public void multiply(vec2 b)
	{
		x *= b.x;
		y *= b.y;
	}

	public void multiply(float _x, float _y)
	{
		x *= _x;
		y *= _y;
	}

	public void multiply(float n)
	{
		x *= n;
		y *= n;
	}

	public void divide(vec2 b)
	{
		x /= b.x;
		y /= b.y;
	}

	public void divide(float n)
	{
		x /= n;
		y /= n;
	}
}
