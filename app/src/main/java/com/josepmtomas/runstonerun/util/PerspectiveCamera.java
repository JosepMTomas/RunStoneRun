package com.josepmtomas.runstonerun.util;

import com.josepmtomas.runstonerun.algebra.operations;
import com.josepmtomas.runstonerun.algebra.vec3;

import static com.josepmtomas.runstonerun.algebra.operations.*;

/**
 * Created by Josep on 16/08/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class PerspectiveCamera
{
	// Camera parameters
	private vec3 look;
	private vec3 poi;
	private vec3 pov;

	private vec3 u;
	private vec3 v;

	private float aspectRatio;
	private float fov;
	private float zNear;
	private float zFar;

	// Frustum planes
	private vec3[] frustumNearPoints = new vec3[4];
	private vec3[] frustumFarPoints = new vec3[4];
	private vec3[] frustumPlanesN = new vec3[6];
	private float[] frustumPlanesD = new float[6];

	/**********************************************************************************************/

	public PerspectiveCamera(
			float povX, float povY, float povZ,
			float poiX, float poiY, float poiZ,
			float near, float far,
			float aspectRatio, float fov
	)
	{
		this.pov = new vec3(povX, povY, povZ);
		this.poi = new vec3(poiX, poiY, poiZ);
		this.look = subtract(this.poi, this.pov);
		this.look.normalize();

		calculateUVW();

		this.zNear = near;
		this.zFar = far;
		this.aspectRatio = aspectRatio;
		this.fov = fov;

		buildFrustumPlanes();
	}


	private void calculateUVW()
	{
		vec3 w = operations.subtract(poi, pov);
		w.negate();
		w.normalize();

		u = operations.cross(new vec3(0,1,0), w);
		u.normalize();

		v = operations.cross(w,u);
	}


	private void buildFrustumPlanes()
	{
		vec3 cN = operations.add(pov, operations.multiply(look, zNear));
		vec3 cF = operations.add(pov, operations.multiply(look, zFar));

		float fov2 = (float)Math.toRadians(fov);

		float hNear = 2.0f * (float)Math.tan(fov2/ 2.0f) *zNear;
		float wNear = hNear * aspectRatio;
		float hFar = 2.0f * (float)Math.tan(fov2/ 2.0f) *zFar;
		float wFar = hFar * aspectRatio;
		float hHNear = hNear / 2.0f;
		float hWNear = wNear / 2.0f;
		float hHFar = hFar / 2.0f;
		float hWFar = wFar / 2.0f;

		frustumFarPoints[0] = subtract(cF, add(multiply(v, hHFar), multiply(u, hWFar)));
		frustumFarPoints[1] = subtract(cF, subtract(multiply(v, hHFar), multiply(u, hWFar)));
		frustumFarPoints[2] = add(cF, add(multiply(v, hHFar), multiply(u, hWFar)));
		frustumFarPoints[3] = add(cF, subtract( multiply(v, hHFar), multiply(u, hWFar)));

		frustumNearPoints[0] = subtract(cN, add( multiply(v, hHNear), multiply(u, hWNear)));
		frustumNearPoints[1] = subtract(cN, subtract( multiply(v, hHNear), multiply(u, hWNear)));
		frustumNearPoints[2] = add(cN, add( multiply(v, hHNear), multiply(u, hWNear)));
		frustumNearPoints[3] = add(cN, subtract( multiply(v, hHNear), multiply(u, hWNear)));

		/*Log.w("FrustumFar","["+frustumFarPoints[0].x+", "+frustumFarPoints[0].y+", "+frustumFarPoints[0].z+"]\n["
							+frustumFarPoints[1].x+", "+frustumFarPoints[1].y+", "+frustumFarPoints[1].z+"]\n["
							+frustumFarPoints[2].x+", "+frustumFarPoints[2].y+", "+frustumFarPoints[2].z+"]\n["
							+frustumFarPoints[3].x+", "+frustumFarPoints[3].y+", "+frustumFarPoints[3].z+"]");*/
		//Log.w("FrustumNear","["+frustumNearPoints[0]+"]["+frustumNearPoints[1]+"]["+frustumNearPoints[2]+"]["+frustumNearPoints[3]+"]");

		buildFrustumPlane(0, frustumNearPoints[0], frustumNearPoints[1], frustumFarPoints[0]);	// bottom plane
		buildFrustumPlane(1, frustumNearPoints[1], frustumNearPoints[2], frustumFarPoints[1]);	// right plane
		buildFrustumPlane(2, frustumNearPoints[2], frustumNearPoints[3], frustumFarPoints[2]);	// top plane
		buildFrustumPlane(3, frustumNearPoints[3], frustumNearPoints[0], frustumFarPoints[3]);	// left plane
		buildFrustumPlane(4, frustumNearPoints[0], frustumNearPoints[3], frustumNearPoints[2]);	// near plane
		buildFrustumPlane(5, frustumFarPoints[3], frustumFarPoints[0], frustumFarPoints[1]);	// far plane
	}


	private void buildFrustumPlane(int index, vec3 v1, vec3 v2, vec3 v3)
	{
		vec3 e1 = subtract(v2, v1);
		vec3 e2 = subtract(v3, v1);
		frustumPlanesN[index] = normalize(cross(e1, e2));
		frustumPlanesD[index] = dotProduct(frustumPlanesN[index], v1);
	}


	public boolean pointInFrustum(float x, float y, float z)
	{
		for(int i=0; i < 6; i++)
		{
			//Log.d(TAG, "DOT[" + i + "] = " + dot(frustumPlanesN[i].x, frustumPlanesN[i].y, frustumPlanesN[i].z, x, y, z));
			if((dot(frustumPlanesN[i].x, frustumPlanesN[i].y, frustumPlanesN[i].z, x, y, z) - frustumPlanesD[i]) < 0.0f)
			{
				return false;
			}
		}
		return true;
	}


	public boolean sphereInFrustum(float x, float y, float z, float r)
	{
		float d;

		for(int i=0; i<6; i++)
		{
			d = dot(frustumPlanesN[i].x, frustumPlanesN[i].y, frustumPlanesN[i].z, x, y, z) - frustumPlanesD[i];
			if(d < -r)
				return false;
		}
		return true;
	}


	public boolean anyPointInFrustum(float[] points)
	{
		for(int i=0; i < points.length; i+=3)
		{
			//result = result || pointInFrustum(points[i], points[i+1], points[i+2]);
			if(pointInFrustum(points[i], points[i+1], points[i+2])) return true;
		}

		return false;
	}


	public boolean anyPointInFrustum(float[] basePoints, float offsetX, float offsetY, float offsetZ)
	{
		for(int i=0; i < basePoints.length; i+=3)
		{
			if(pointInFrustum(basePoints[i]+offsetX, basePoints[i+1]+offsetY, basePoints[i+2]+offsetZ)) return true;
		}

		return false;
	}


	public boolean anyPointInFrustum(float[] points, float radius)
	{
		for(int i=0; i < points.length; i+=3)
		{
			if(sphereInFrustum(points[i], points[i+1], points[i+2], radius)) return true;
		}

		return false;
	}


	public boolean treePointVisible(float x, float y, float z)
	{
		if((dot(frustumPlanesN[0].x, frustumPlanesN[0].y, frustumPlanesN[0].z, x, y, z) - frustumPlanesD[1]) < 0.0f)
		{
			return false;
		}
		else if((dot(frustumPlanesN[0].x, frustumPlanesN[0].y, frustumPlanesN[0].z, x, y, z) - frustumPlanesD[3]) < 0.0f)
		{
			return false;
		}
		else if((dot(frustumPlanesN[0].x, frustumPlanesN[0].y, frustumPlanesN[0].z, x, y, z) - frustumPlanesD[4]) < 0.0f)
		{
			return false;
		}
		return true;
	}


	// check planes 1,3,4
	public boolean cullTreePoints(float[] treeCullingPoints, int offset, int count)
	{
		//int length = treeCullingPoints.length;
		int length = offset + (count*3);

		for(int i=offset; i<length; i+=3)
		{
			if(treePointVisible(treeCullingPoints[i], treeCullingPoints[i+1], treeCullingPoints[i+2]))
			{
				return true;
			}
		}

		return false;
	}
}