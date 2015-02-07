package com.josepmtomas.runstonerun.algebra;

import android.util.FloatMath;

/**
 * Created by Josep on 16/07/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class operations
{
	//////////////////////////////////////////////////

	public static float lerp(float a, float b, float alpha)
	{
		float oneMinus = 1f - alpha;

		return (a * oneMinus) + (b * alpha);
	}

	//////////////////////////////////////////////////

	public static vec2 add(vec2 a, vec2 b)
	{
		return new vec2(a.x + b.x, a.y + b.y);
	}

	public static vec2 add(vec2 a, float n)
	{
		return new vec2(a.x + n, a.y + n);
	}

	public static vec2 subtract(vec2 a, vec2 b)
	{
		return new vec2(a.x - b.x, a.y - b.y);
	}

	public static vec2 subtract(vec2 a, float n)
	{
		return new vec2(a.x + n, a.y + n);
	}

	public static vec2 multiply(vec2 a, vec2 b)
	{
		return new vec2(a.x * b.x, a.y * b.y);
	}

	public static vec2 multiply(vec2 a, float _x, float _y)
	{
		return new vec2(a.x * _x, a.y * _y);
	}

	public static vec2 multiply(vec2 a, float n)
	{
		return new vec2(a.x * n, a.y * n);
	}

	public static vec2 divide(vec2 a, vec2 b)
	{
		return new vec2(a.x / b.x, a.y / b.y);
	}

	public static vec2 divide(vec2 a, float n)
	{
		return new vec2(a.x / n, a.y / n);
	}

	////////////////////////////////////////////////////

	public static vec3 add(vec3 a, vec3 b)
	{
		return new vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static vec3 multiply(vec3 a, float x)
	{
		return new vec3(a.x * x, a.y * x, a.z * x);
	}

	public static void multiplyVF(float[] result, int resultOffset, float[] vector, int vectorOffset, float value)
	{
		for(int i=0; i < 4; i++)
		{
			result[resultOffset+i] = vector[vectorOffset+i] * value;
		}
	}

	public static void multiplyVV(float[] result, int resultOffset, float[] a, int offsetA, float[] b, int offsetB)
	{
		for(int i=0; i < 4; i++)
		{
			result[resultOffset+i] = a[offsetA+i] * b[offsetB+i];
		}
	}

	public static vec3 negate(vec3 vector)
	{
		return new vec3(-vector.x, -vector.y, -vector.z);
	}

	public static vec3 normalize(vec3 a)
	{
		return multiply(a, 1.0f / a.module());
	}

	public static vec3 reflectV(vec3 vector, vec3 normal)
	{
		//return normalize(normal*2*dot(normal,direction) - direction);
		float dot = operations.dotProduct(normal, vector);
		vec3 result = new vec3(normal);

		result.multiply(2.0f);
		result.multiply(dot);
		result.subtract(vector);

		return result;
	}

	public static vec3 rotate(vec3 vector, vec3 axis, float degrees)
	{
		float angle = (float)Math.toRadians(degrees);
		float sinAngle = FloatMath.sin(angle);
		float cosAngle = FloatMath.cos(angle);
		float x2 = axis.x * axis.x;
		float y2 = axis.y * axis.y;
		float z2 = axis.z * axis.z;
		float xy = axis.x * axis.y;
		float xz = axis.x * axis.z;
		float yz = axis.y * axis.z;
		vec3 result = new vec3(0.0f);
		vec3 column = new vec3(0.0f);

		// vector * first column
		column.x = x2 + ((1f - x2) * cosAngle);
		column.y = xy * (1f - cosAngle) + axis.z * sinAngle;
		column.z = xz * (1f - cosAngle) - axis.y * sinAngle;
		result.x = (vector.x * column.x) + (vector.y * column.y) + (vector.z * column.z);

		// vector * second column
		column.x = xy * (1f - cosAngle) - axis.z * sinAngle;
		column.y = y2 + ((1f - y2) * cosAngle);
		column.z = yz * (1f - cosAngle) + axis.z * sinAngle;
		result.y = (vector.x * column.x) + (vector.y * column.y) + (vector.z * column.z);

		// vector * third column
		column.x = xz * (1f - cosAngle) + axis.y * sinAngle;
		column.y = yz * (1f - cosAngle) - axis.y * sinAngle;
		column.z = z2 + ((1f - z2) * cosAngle);
		result.z = (vector.x * column.x) + (vector.y * column.y) + (vector.z * column.z);

		return result;
	}

	public static vec3 subtract(vec3 a, vec3 b)
	{
		vec3 sum = new vec3(0.0f);

		sum.x = a.x - b.x;
		sum.y = a.y - b.y;
		sum.z = a.z - b.z;

		return sum;
	}

	public static float distanceBetween(vec3 a, vec3 b)
	{
		return FloatMath.sqrt(
				FloatMath.pow(a.x - b.x, 2f) +
				FloatMath.pow(a.y - b.y, 2f) +
				FloatMath.pow(a.z - b.z, 2f));
	}

	public static float distanceToOrigin(vec3 a)
	{
		return FloatMath.sqrt(a.x*a.x + a.y*a.y + a.z*a.z);
	}

	public static float distanceToOrigin(float x, float y, float z)
	{
		return FloatMath.sqrt(x*x + y*y + z*z);
	}

	public static float distanceToOrigin(float x, float y)
	{
		return FloatMath.sqrt(x*x + y*y);
	}

	public static float dotProduct(vec3 a, vec3 b)
	{
		return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
	}

	public static float dot(float ax, float ay, float az, float bx, float by, float bz)
	{
		return (ax * bx) + (ay * by) + (az * bz);
	}

	public static vec3 cross(vec3 a, vec3 b)
	{
		//vec3(a.y*b.z - a.z*b.y, a.z*b.x - a.x*b.z, a.x*b.y - a.y*b.x);
		return new vec3(a.y*b.z - a.z*b.y,
						a.z*b.x - a.x*b.z,
						a.x*b.y - a.y*b.x);
	}
}