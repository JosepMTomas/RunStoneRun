package com.josepmtomas.runstonerun.poissonGeneration;

import android.util.FloatMath;

import com.josepmtomas.runstonerun.Constants;
import com.josepmtomas.runstonerun.algebra.vec2;
import static com.josepmtomas.runstonerun.algebra.operations.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Josep on 22/09/2014.
 * @author Josep
 */

@SuppressWarnings("all")
public class PDSampler
{
	protected final int kMaxPointsPerCell = 9;
	protected Random random;
	protected ArrayList<Integer> mNeighbors;
	protected int[][] mGrid;
	protected int mGridSize;
	protected float mGridCellSize;

	public ArrayList<vec2> points;
	public float radius;


	public PDSampler(float _radius)
	{
		random = new Random();

		radius = _radius;

		mGridSize = (int) FloatMath.ceil(2f / (4f * radius));
		if(mGridSize < 2)
			mGridSize = 2;

		mGridCellSize = 2f / mGridSize;
		mGrid = new int[mGridSize*mGridSize][kMaxPointsPerCell];

		for(int y=0; y < mGridSize; y++)
			for(int x=0; x < mGridSize; x++)
				for(int k=0; k < kMaxPointsPerCell; k++)
					mGrid[y * mGridSize + x][k] = -1;

		mNeighbors = new ArrayList<Integer>();
		points = new ArrayList<vec2>();
	}


	public boolean pointInDomain(vec2 a)
	{
		return (-1 <= a.x) && (-1 <= a.y) && (1 >= a.x) && (1 >= a.y);
	}


	/** Return the shortest distance between 'a' and 'b' (accounting for tiling)**/
	public float getDistanceSquared(vec2 a, vec2 b)
	{
		vec2 v = getTiled(subtract(a,b));
		return v.x*v.x + v.y*v.y;
	}

	public float getDistance(vec2 a, vec2 b)
	{
		return FloatMath.sqrt(getDistanceSquared(a,b));
	}


	/** Generate a random point in square **/
	public vec2 randomPoint()
	{
		float x = 2f * random.nextFloat() - 1f;
		float y = 2f * random.nextFloat() - 1f;
		return new vec2(x,y);
	}


	/** Return tiled coordinates of 'v' **/
	public vec2 getTiled(vec2 v)
	{
		float x = v.x;
		float y = v.y;

		if(x < -1) x += 2f;
		else if(x > 1) x -= 2f;

		if(y < -1) y += 2f;
		else if(y > 1) y -= 2f;

		return new vec2(x,y);
	}


	/** Return grid (x,y) for point **/
	public void getGridXY(vec2 v, int[] gOut)
	{
		gOut[0] = (int) FloatMath.floor(0.5f * (v.x + 1f) * mGridSize);
		gOut[1] = (int) FloatMath.floor(0.5f * (v.y + 1f) * mGridSize);
	}


	/** Add 'pt' to point list and grid **/
	public void addPoint(vec2 pt)
	{
		int i;
		int[] g = new int[2];
		int[] cell;

		points.add(pt);

		getGridXY(pt, g);
		cell = mGrid[g[1]*mGridSize + g[0]];

		for(i=0; i<kMaxPointsPerCell; i++)
		{
			if(cell[i] == -1)
			{
				cell[i] = points.size()-1;
				break;
			}
		}

		if(i == kMaxPointsPerCell)
		{
			// exit
		}
	}


	/** Populate mNeighbors with list of all points within 'radius' of 'pt'
		and return number of such points */
	public int findNeighbors(vec2 pt, float distance)
	{
		float distanceSqrd = distance * distance;
		int i, j, k;
		int N = (int)FloatMath.ceil(distance / mGridCellSize);
		int[] g = new int[2];

		if(N > (mGridSize>>1)) N = mGridSize>>1;

		mNeighbors.clear();
		getGridXY(pt, g);

		for(j=-N; j <= N; j++)
		{
			for(i=-N; i <= N; i++)
			{
				int cx = (g[0]+i+mGridSize) % mGridSize;
				int cy = (g[1]+i+mGridSize) % mGridSize;
				int[] cell = mGrid[cy * mGridSize + cx];

				for(k=0; k < kMaxPointsPerCell; k++)
				{
					if(cell[k] == -1)
					{
						break;
					}
					else
					{
						if(getDistanceSquared(pt, points.get(cell[k])) < distanceSqrd)
						{
							mNeighbors.add(cell[k]);
						}
					}
				}
			}
		}

		return mNeighbors.size();
	}


	/** Return distance to the closest neighbor within 'radius' **/
	public float findClosestNeighbor(vec2 pt, float distance)
	{
		float closestSqrd = distance * distance;
		int i, j, k;
		int N = (int)FloatMath.ceil(distance / mGridCellSize);
		int[] g = new int[2];

		getGridXY(pt, g);

		for(j=-N; j<=N; j++)
		{
			for(i=-N; i<=N; i++)
			{
				int cx = (g[0]+i+mGridSize) % mGridSize;
				int cy = (g[1]+i+mGridSize) % mGridSize;
				int[] cell = mGrid[cy * mGridSize + cx];

				for(k=0; k<kMaxPointsPerCell; k++)
				{
					if(cell[k] == -1)
					{
						break;
					}
					else
					{
						float d = getDistanceSquared(pt, points.get(cell[k]));
						if(d < closestSqrd)
							closestSqrd = d;
					}
				}
			}
		}

		return FloatMath.sqrt(closestSqrd);
	}


	/** Find available angle ranges on boundary for candidate
		by subtracting occluded neighbor ranges from 'r1' **/
	public void findNeighborRanges(int index, RangeList rl)
	{
		vec2 candidate = points.get(index);
		float rangeSqrd = 16f*radius*radius;
		int i, j, k;
		int N = (int)FloatMath.ceil(4*radius / mGridCellSize);
		int[] g = new int[2];

		if(N > (mGridSize>>1)) N = mGridSize>>1;

		getGridXY(candidate, g);

		int xSide = (candidate.x - (-1 + g[0]*mGridCellSize))>mGridCellSize*0.5f ? 1 : 0;
		int ySide = (candidate.y - (-1 + g[1]*mGridCellSize))>mGridCellSize*0.5f ? 1 : 0;
		int iy = 1;

		for(j=-N; j <= N; j++)
		{
			int ix = 1;

			if(j==0) iy = ySide;
			else if(j==1) iy = 0;

			for(i=-N; i <= N; i++)
			{
				if(i==0) ix = xSide;
				else if(i==1) ix = 0;

				// Offset to closest cell point
				float dx = candidate.x - (-1 + (g[0]+i+ix)*mGridCellSize);
				float dy = candidate.y - (-1 + (g[1]+j+iy)*mGridCellSize);

				if(dx*dx+dy*dy < rangeSqrd)
				{
					int cx = (g[0]+i+mGridSize) % mGridSize;
					int cy = (g[1]+j+mGridSize) % mGridSize;
					int[] cell = mGrid[cy * mGridSize + cx];

					for(k=0; k<kMaxPointsPerCell; k++)
					{
						if(cell[k] == -1)
						{
							break;
						}
						else if(cell[k] != index)
						{
							vec2 pt = points.get(cell[k]);
							vec2 v = getTiled(subtract(pt, candidate));
							float distSqrd = v.x*v.x + v.y*v.y;

							if(distSqrd < rangeSqrd)
							{
								float dist = FloatMath.sqrt(distSqrd);
								float angle = (float)Math.atan2(v.y, v.x);
								float theta = (float)Math.acos(0.25*dist/radius);

								rl.subtract(angle-theta, angle+theta);
							}
						}
					}
				}
			}
		}
	}


	/** Extends point set by boundary sampling until domain is full **/
	public void maximize()
	{
		RangeList rl = new RangeList(0,0);
		int i;
		int N = points.size();

		for(i=0; i<N; i++)
		{
			vec2 candidate = points.get(i);

			rl.reset(0, Constants.PI2);
			findNeighborRanges(i, rl);

			while(rl.numRanges > 0)
			{
				RangeEntry re = rl.ranges.get(random.nextInt(rl.numRanges));
				float angle = re.min + (re.max-re.min) * random.nextFloat();
				vec2 pt = getTiled(new vec2(candidate.x + FloatMath.cos(angle)*2f*radius,
											candidate.y + FloatMath.sin(angle)*2f*radius));

				addPoint(pt);
				rl.subtract(angle - (float)Math.PI/3f, angle + (float)Math.PI/3f);
			}
		}
	}
}