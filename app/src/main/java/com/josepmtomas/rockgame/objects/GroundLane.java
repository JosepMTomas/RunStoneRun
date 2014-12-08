package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.data.GroundPatchType;
import com.josepmtomas.rockgame.programs.GroundShaderProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

/**
 * Created by Josep on 18/07/2014.
 */
public class GroundLane
{
	private static final String TAG = "GroundLane";

	private GroundPatch[] patches;

	private vec3 currentOffset;

	private int maximumDistance;
	private vec3 initialPosition;

	/*private int grassColorTexture;
	private int grassNormalTexture;
	private int groundColorTexture;
	private int groundAlphaTexture;
	private int groundNormalTexture;*/


	/**
	 *
	 * @param numPatchesZ number of patches in the lane
	 * @param patchSizeX horizontal number of vertices in each patch
	 * @param patchSizeZ vertical number of vertices in each patch
	 * @param patchWidth width of each patch (distance)
	 * @param patchHeight height of each patch (distance)
	 * @param laneOffset initial displacement of the first patch
	 * @param parentGrass grass class
	 * @param program shader program of the patches
	 * @param textures set of textures for use in the shader program
	 */
	public GroundLane(int numPatchesZ, int patchSizeX, int patchSizeZ, float patchWidth, float patchHeight, vec3 laneOffset,
					  Grass parentGrass, GroundShaderProgram program, int[] textures)
	{
		patches = new GroundPatch[numPatchesZ];
		currentOffset = new vec3(laneOffset);

		vec3 patchOffset = new vec3(laneOffset);
		patchOffset.add(0.0f, 0.0f, (((float)numPatchesZ - 1.0f) / 2.0f) * patchHeight);

		patches[0] = new GroundPatch(patchSizeX, patchSizeZ, patchWidth, patchHeight, patchOffset, parentGrass, program, textures);

		for(int i=1; i < numPatchesZ; i++)
		{
			patchOffset.subtract(0.0f, 0.0f, patchHeight);
			patches[i] = new GroundPatch(patchSizeX, patchSizeZ, patchWidth, patchHeight, patchOffset, parentGrass, program, textures);
		}

	}


	/*public GroundLane(Context context, int numPatches)
	{
		// Textures
		grassColorTexture = TextureHelper.loadTexture(context, R.raw.grass_color);
		grassNormalTexture = TextureHelper.loadTexture(context, R.raw.grass_normal);
		groundColorTexture = TextureHelper.loadTexture(context, R.raw.ground_color);
		groundAlphaTexture = TextureHelper.loadTexture(context, R.raw.ground_alpha);
		groundNormalTexture = TextureHelper.loadTexture(context, R.raw.ground_normal);
		Log.d(TAG, "GrassColor ID = " + grassColorTexture);
		Log.d(TAG, "GrassNormal ID = " + grassNormalTexture);
		Log.d(TAG, "GroundColor ID = " + groundColorTexture);
		Log.d(TAG, "GroundAlpha ID = " + groundAlphaTexture);
		Log.d(TAG, "GroundNormal ID = " + groundNormalTexture);

		// Patches
		patches = new GroundPatch[numPatches];

		maximumDistance = 100 * (numPatches - 1);
		initialPosition = new vec3(0f, 0f, (maximumDistance/2f));
		//initialPosition.setValues(0f,0f,0f);

		//patches[0] = new GroundPatch(context, R.raw.ground_patch10x10, initialPosition, new vec3(0f,0f,0f), maximumDistance);
		patches[0] = new GroundPatch(context, 10, 10, 100f, 100f, initialPosition);
		patches[0].setTextures(grassColorTexture, grassNormalTexture, groundColorTexture, groundAlphaTexture, groundNormalTexture);
		patches[0].setOffset(0.0f);
		patches[0].initialize();

		for(int i=1; i < patches.length; i++)
		{
			//patches[i] = new GroundPatch(context, R.raw.ground_patch10x10, initialPosition, new vec3(0f,0f,0f), maximumDistance);
			patches[i] = new GroundPatch(context, 10, 10, 100f, 100f, initialPosition);
			patches[i].setTextures(grassColorTexture, grassNormalTexture, groundColorTexture, groundAlphaTexture, groundNormalTexture);
			patches[i].setOffset(-i*(maximumDistance/(numPatches-1)));
			patches[i].initialize(GroundPatchType.UP,patches[i-1].getUpVertexColors());
		}
	}*/


	public void update(vec3 movement, float[] viewMatrix, float[] projectionMatrix, float time)
	{
		currentOffset.add(movement.x, 0.0f, 0.0f);

		for(int i=0; i < patches.length; i++)
		{
			patches[i].update(movement, viewMatrix, projectionMatrix, time);
		}
	}


	public void draw()
	{
		for(int i=0; i < patches.length; i++)
		{
			//Log.d(TAG, "Binding patch = " + i);
			patches[i].bind();
			patches[i].draw();

			//patches[i].drawGrass();
		}
	}


	public void drawGrass()
	{
		for(int i=0; i < patches.length; i++)
		{
			patches[i].drawGrass();
		}
	}

	public float[] getGroundPatchVertexColors(int index, GroundPatchType type)
	{
		switch(type)
		{
			case GROUND_DOWN:
				return patches[index].getDownVertexColors();

			case GROUND_UP:
				return patches[index].getUpVertexColors();

			case GROUND_LEFT:
				return patches[index].getLeftVertexColors();

			case GROUND_RIGHT:
				return patches[index].getRightVertexColors();

			default:
				return patches[index].getDownVertexColors();
		}
	}


	public void initializePatch(int index)
	{
		patches[index].initialize();
	}

	public void initializePatch(int index, GroundPatchType type, float[] previousColors)
	{
		patches[index].initialize(type, previousColors);
	}

	public void initializePatch(int index, GroundPatchType type, float[] downColors, float[] sideColors)
	{
		patches[index].initialize(type, downColors, sideColors);
	}

	public void reinitializePatch(int index, GroundPatchType type, float[] previousColors)
	{
		patches[index].reinitialize(type, previousColors);
	}

	public void reinitializePatch(int index, GroundPatchType type, float[] downColors, float[] sideColors)
	{
		patches[index].reinitialize(type, downColors, sideColors);
	}

	public vec3 getPatchPosition(int index)
	{
		return patches[index].getPosition();
	}

	public void setPatchPosition(int index, vec3 position)
	{
		//patches[index].setPosition(operations.add(position, offset));
		patches[index].setPosition(position);
	}

	public void setPatchPositionX(int index, float position)
	{
		patches[index].setPositionX(position);
	}

	public void setPatchPositionZ(int index, float position)
	{
		patches[index].setPositionZ(position);
	}

	public GroundPatch getPatch(int index)
	{
		return patches[index];
	}
}
