package com.josepmtomas.rockgame.poissonGeneration;

import android.util.FloatMath;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.algebra.vec2;

import java.util.ArrayList;

/**
 * Created by Josep on 22/09/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class BoundarySampler extends PDSampler
{
	protected ArrayList<Integer> candidates = new ArrayList<Integer>();
	protected RangeList rl = new RangeList(0,0);

	public BoundarySampler(float _radius)
	{
		super(_radius);
	}


	public void complete()
	{
		//RangeList rl = new RangeList(0,0);
		rl.reset(0,0);
		//ArrayList<Integer> candidates = new ArrayList<Integer>();
		candidates.clear();

		addPoint(randomPoint());
		candidates.add(points.size()-1);

		vec2 candidate, pt;

		while(candidates.size() > 0)
		{
			//Log.d("Sampler", "Points = " + points.size());
			int c = random.nextInt(candidates.size());
			int index = candidates.get(c);
			candidate = points.get(index);
			candidates.set(c, candidates.get(candidates.size()-1));
			candidates.remove(candidates.size()-1);

			rl.reset(0, Constants.PI2);
			findNeighborRanges(index, rl);

			while(rl.numRanges > 0)
			{
				RangeEntry re = rl.ranges.get(random.nextInt(rl.numRanges));
				float angle = re.min + (re.max-re.min) * random.nextFloat();
				pt = getTiled(new vec2(candidate.x + FloatMath.cos(angle)*2f*radius,
											candidate.y + FloatMath.sin(angle)*2f*radius));

				addPoint(pt);
				candidates.add(points.size()-1);

				rl.subtract(angle - (float)Math.PI/3f, angle + (float)Math.PI/3f);
			}
		}
	}


	public void completeStrict()
	{
		//RangeList rl = new RangeList(0,0);
		rl.reset(0,0);
		//ArrayList<Integer> candidates = new ArrayList<Integer>();
		candidates.clear();

		addPoint(randomPoint());
		candidates.add(points.size()-1);

		vec2 candidate, pt;

		while(candidates.size() > 0)
		{
			//Log.d("Sampler", "Points = " + points.size());
			int c = random.nextInt(candidates.size());
			int index = candidates.get(c);
			candidate = points.get(index);
			candidates.set(c, candidates.get(candidates.size()-1));
			candidates.remove(candidates.size()-1);

			rl.reset(0, Constants.PI2);
			findNeighborRanges(index, rl);

			while(rl.numRanges > 0)
			{
				RangeEntry re = rl.ranges.get(random.nextInt(rl.numRanges));
				float angle = re.min + (re.max-re.min) * random.nextFloat();
				pt = getTiled(new vec2(candidate.x + FloatMath.cos(angle)*2f*radius,
						candidate.y + FloatMath.sin(angle)*2f*radius));

				addPoint(pt);
				candidates.add(points.size()-1);

				rl.subtract(angle - (float)Math.PI/3f, angle + (float)Math.PI/3f);
			}
		}

		int i=0;
		int j;
		vec2 a,b;

		while(i < points.size()-1)
		{
			a = points.get(i);
			j = i+1;
			while(j < points.size())
			{
				b = points.get(j);
				if(getDistance(a,b) < radius)
				{
					points.remove(j);
				}
				else
				{
					j++;
				}
			}
			i++;
		}
	}


	public void reset()
	{
		for(int y=0; y < mGridSize; y++)
			for(int x=0; x < mGridSize; x++)
				for(int k=0; k < kMaxPointsPerCell; k++)
					mGrid[y * mGridSize + x][k] = -1;

		points.clear();
		mNeighbors.clear();
	}


	public void reset(float _newRadius)
	{
		radius = _newRadius;

		mGridSize = (int) FloatMath.ceil(2f / (4f * radius));
		if(mGridSize < 2)
			mGridSize = 2;

		mGridCellSize = 2f / mGridSize;
		mGrid = new int[mGridSize*mGridSize][kMaxPointsPerCell];

		for(int y=0; y < mGridSize; y++)
			for(int x=0; x < mGridSize; x++)
				for(int k=0; k < kMaxPointsPerCell; k++)
					mGrid[y * mGridSize + x][k] = -1;

		mNeighbors.clear();
		points.clear();
	}


	public void spread(float spreadFactorX, float spreadFactorY)
	{
		for(vec2 point:points)
		{
			point.multiply(spreadFactorX, spreadFactorY);
		}
	}
}
