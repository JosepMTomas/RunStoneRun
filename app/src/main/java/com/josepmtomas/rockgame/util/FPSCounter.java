package com.josepmtomas.rockgame.util;

import android.util.Log;

/**
 * Created by Josep on 06/08/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class FPSCounter
{
	long startTime = System.nanoTime();
	int lastFrames = 0;
	int frames = 0;
	int seconds = -1;
	long totalFrames = 0;
	float average = 0f;

	public void logFrame()
	{
		frames++;
		if(System.nanoTime() - startTime >= 1000000000)
		{
			Log.i("FPSCounter", "fps: " + frames);
			frames = 0;
			startTime = System.nanoTime();
		}
	}

	public int logFrameWithAverage()
	{
		frames++;
		if(System.nanoTime() - startTime >= 1000000000)
		{
			seconds++;
			totalFrames += frames;
			if(seconds>0) average = (float)totalFrames / (float)seconds;
			Log.i("FPSCounter", "fps: " + frames + " | average: " + average);
			lastFrames = frames + ((int)average * 100);
			frames = 0;
			startTime = System.nanoTime();
		}

		return lastFrames;
	}

	public boolean logFrame(long updateTime, long drawTime)
	{
		frames++;
		if(System.nanoTime() - startTime >= 1000000000)
		{
			//float updateMillis = ((float)updateTime/frames) / 1000000f;
			//float drawMillis = ((float)drawTime/frames) / 1000000f;
			float updateMillis = (float)updateTime;
			float drawMillis = (float)drawTime;

			Log.i("FPSCounter", "fps: " + frames + " | update: " + updateMillis + " | draw: " + drawMillis);
			frames = 0;
			startTime = System.nanoTime();
			return true;
		}
		else
		{
			return false;
		}
	}
}
