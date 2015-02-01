package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;

import com.josepmtomas.rockgame.R;

/**
 * Created by Josep on 01/02/2015.
 * @author Josep
 */

@SuppressWarnings("all")
public class SplashScreen extends View
{
	Bitmap loader;
	Bitmap loaderScaled;
	int width;
	int height;

	public SplashScreen(Context context, int width, int height)
	{
		super(context);

		this.width = width;
		this.height = height;

		// Initial bitmap
		loader = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
		// Create the scaled bitmap
		loaderScaled = Bitmap.createScaledBitmap(loader, width, height, true);
		// Recycle the original bitmap as we no longer need it
		loader.recycle();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawBitmap(loaderScaled, 0, 0, null);
	}
}
