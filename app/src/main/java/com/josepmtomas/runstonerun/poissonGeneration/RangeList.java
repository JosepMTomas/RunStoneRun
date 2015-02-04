package com.josepmtomas.runstonerun.poissonGeneration;

import com.josepmtomas.runstonerun.Constants;

import java.util.ArrayList;

/**
 * Created by Josep on 22/09/2014.
 */
public class RangeList
{
	private static final float kSmallestRange = 0.000001f;

	public ArrayList<RangeEntry> ranges;
	public int numRanges, rangesSize;

	public RangeList(float min, float max)
	{
		numRanges = 0;
		rangesSize = 8;
		ranges = new ArrayList<RangeEntry>();
		reset(min, max);
	}

	public void reset(float min, float max)
	{
		numRanges = 1;

		ranges.clear();
		ranges.add(new RangeEntry(min, max));
		ranges.add(new RangeEntry(min, max));
		ranges.add(new RangeEntry(min, max));
		//ranges.get(0).min = min;
		//ranges.get(0).max = max;
	}

	public void subtract(float a, float b)
	{
		float twoPi = Constants.PI2;

		if (a > twoPi)
		{
			subtract(a - twoPi, b - twoPi);
		}
		else if (b < 0)
		{
			subtract(a + twoPi, b + twoPi);
		}
		else if (a < 0)
		{
			subtract(0, b);
			subtract(a + twoPi, twoPi);
		}
		else if (b > twoPi)
		{
			subtract(a, twoPi);
			subtract(0, b - twoPi);
		}
		else if (numRanges==0)
		{
		//	;
		}
		else
		{
			int pos;

			if(a < ranges.get(0).min) {
				pos = -1;
			}
			else
			{
				int lo = 0;
				int mid = 0;
				int hi = numRanges;

				while(lo < hi-1)
				{
					mid = (lo+hi)>>1;
					if(ranges.get(mid).min < a)
					{
						lo = mid;
					}
					else
					{
						hi = mid;
					}
				}
				pos = lo;
			}

			if(pos == -1)
			{
				pos = 0;
			}
			else if(a < ranges.get(pos).max)
			{
				float c = ranges.get(pos).min;
				float d = ranges.get(pos).max;

				if((a-c) < kSmallestRange)
				{
					if(b < d)
					{
						ranges.get(pos).min = b;
					}
					else
					{
						deleteRange(pos);
					}
				}
				else
				{
					ranges.get(pos).max = a;
					if(b < d)
					{
						insertRange(pos+1, b, d);
					}
					pos++;
				}
			}
			else
			{
				if(pos < (numRanges-1) && b > ranges.get(pos+1).min)
				{
					pos++;
				}
				else
				{
					return;
				}
			}

			while(pos < numRanges && b > ranges.get(pos).min)
			//while(pos < numRanges)
			{
				//if(b > ranges.get(pos).min)
				{
					if (ranges.get(pos).max - b < kSmallestRange) {
						deleteRange(pos);
					} else {
						ranges.get(pos).min = b;
					}
				}
			}
		}
	}

	private void deleteRange(int pos)
	{
		if(pos < numRanges-1)
		{
			ranges.remove(pos);
		}
		numRanges--;
	}

	private void insertRange(int pos, float min, float max)
	{
		if(pos < numRanges-1)
		{
			ranges.add(pos, new RangeEntry(min, max));
		}
		else if(pos == numRanges)
		{
			ranges.add(new RangeEntry(min, max));
		}

		numRanges++;
	}
}
