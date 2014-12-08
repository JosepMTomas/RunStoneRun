package com.josepmtomas.rockgame.objects;

import android.content.Context;

import com.josepmtomas.rockgame.R;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.data.GroundPatchType;
import com.josepmtomas.rockgame.programs.GroundShaderProgram;
import com.josepmtomas.rockgame.util.TextureHelper;

/**
 * Created by Josep on 30/07/2014.
 */
public class GroundManager
{
	private static final String TAG = "GroundManager";

	private int numPatchesX;
	private int numPatchesZ;

	private int currentPatchIndex;
	private int previousPatchIndex;
	private int currentLaneIndex;
	private int sideLanes;

	private float patchWidth;
	private float patchHeight;
	private float offsetX;
	private float offsetZ;
	private float maximumOffsetZ;
	private float maximumOffsetX;

	private Context context;

	private GroundLane[] groundLanes;

	private GroundShaderProgram groundShaderProgram;
	private int[] groundTextures;

	private Grass parentGrass;

	/*private int grassColorTexture;
	private int grassNormalTexture;
	private int groundColorTexture;
	private int groundAlphaTexture;
	private int groundNormalTexture;*/


	/**
	 *
	 * @param context application context
	 * @param numPatchesX horizontal number of patches
	 * @param numPatchesZ vertical number of patches
	 * @param patchSizeX horizontal number of vertices of each patch
	 * @param patchSizeZ vertical number of vertices of each patch
	 * @param patchWidth width of each patch (distance)
	 * @param patchHeight height of each path (distance)
	 */
	public GroundManager(Context context,
						 int numPatchesX,
						 int numPatchesZ,
						 int patchSizeX,
						 int patchSizeZ,
						 float patchWidth,
						 float patchHeight,
						 Grass parentGrass)
	{
		this.context = context;
		this.numPatchesX = numPatchesX;
		this.numPatchesZ = numPatchesZ;
		this.patchWidth = patchWidth;
		this.patchHeight = patchHeight;

		this.parentGrass = parentGrass;

		offsetX = 0.0f;
		offsetZ = 0.0f;

		maximumOffsetX = (((float)numPatchesX - 1.0f) / 2.0f) * patchWidth;
		maximumOffsetZ = (((float)numPatchesZ - 1.0f) / 2.0f) * patchHeight;

		// Ground shader program
		groundShaderProgram = new GroundShaderProgram(context);

		// Ground textures
		groundTextures = new int[5];
		groundTextures[0] = TextureHelper.loadTexture(context, R.raw.grass_color);
		groundTextures[1] = TextureHelper.loadTexture(context, R.raw.grass_normal);
		groundTextures[2] = TextureHelper.loadTexture(context, R.raw.ground_color_512);
		groundTextures[3] = TextureHelper.loadTexture(context, R.raw.ground_alpha_512);
		groundTextures[4] = TextureHelper.loadTexture(context, R.raw.ground_normal_512);

		groundLanes = new GroundLane[numPatchesX];

		this.sideLanes = (numPatchesX-1)/2;
		this.currentLaneIndex = sideLanes;
		this.currentPatchIndex = 0;
		this.previousPatchIndex = numPatchesZ-1;

		vec3 laneOffset = new vec3(0.0f);

		// Create center lane
		groundLanes[currentLaneIndex] = new GroundLane(numPatchesZ, patchSizeX, patchSizeZ, patchWidth, patchHeight,
				laneOffset, parentGrass, groundShaderProgram, groundTextures);

		// Create left lanes
		for(int i=1; i <= sideLanes; i++)
		{
			laneOffset.subtract(patchWidth, 0.0f, 0.0f);
			groundLanes[currentLaneIndex-i] = new GroundLane(numPatchesZ, patchSizeX, patchSizeZ, patchWidth, patchHeight,
					laneOffset, parentGrass, groundShaderProgram, groundTextures);
		}

		// Create right lanes
		laneOffset.setValues(0.0f, 0.0f, 0.0f);
		for(int i=1; i <= sideLanes; i++)
		{
			laneOffset.add(patchWidth, 0.0f, 0.0f);
			groundLanes[currentLaneIndex+i] = new GroundLane(numPatchesZ, patchSizeX, patchSizeZ, patchWidth, patchHeight,
					laneOffset, parentGrass, groundShaderProgram, groundTextures);
		}
	}


	public void initialize()
	{
		int sideLanes = (numPatchesX-1)/2;

		// Initialize center lane
		groundLanes[currentLaneIndex].initializePatch(0);

		for(int i=1; i < numPatchesZ; i++)
		{
			groundLanes[currentLaneIndex].initializePatch(
					i,
					GroundPatchType.GROUND_UP,
					groundLanes[currentLaneIndex].getGroundPatchVertexColors(i-1, GroundPatchType.GROUND_UP)
			);
		}

		// Initialize left lanes
		for(int i=1; i <= sideLanes; i++)
		{
			//Log.d(TAG, "Initializing Lane = " + (currentLaneIndex-i) + " + Patch = 0");
			groundLanes[currentLaneIndex-i].initializePatch(
					0,
					GroundPatchType.GROUND_LEFT,
					groundLanes[currentLaneIndex-i+1].getGroundPatchVertexColors(0, GroundPatchType.GROUND_LEFT)
			);

			for(int j=1; j < numPatchesZ; j++)
			{
				//Log.d(TAG, "Initializing Lane = " + (currentLaneIndex-i) + " + Patch = " + j);
				groundLanes[currentLaneIndex-i].initializePatch(
						j,
						GroundPatchType.GROUND_UP_LEFT,
						groundLanes[currentLaneIndex-i].getGroundPatchVertexColors(j-1, GroundPatchType.GROUND_UP),
						groundLanes[currentLaneIndex-i+1].getGroundPatchVertexColors(j, GroundPatchType.GROUND_LEFT)
				);
			}
		}

		// Initialize right lanes
		for(int i=1; i <= sideLanes; i++)
		{
			//Log.d(TAG, "Initializing Lane = " + (currentLaneIndex+i) + " + Patch = 0");
			groundLanes[currentLaneIndex+i].initializePatch(
					0,
					GroundPatchType.GROUND_RIGHT,
					groundLanes[currentLaneIndex+i-1].getGroundPatchVertexColors(0, GroundPatchType.GROUND_RIGHT)
			);

			for(int j=1; j < numPatchesZ; j++)
			{
				//Log.d(TAG, "Initializing Lane = " + (currentLaneIndex+i) + " + Patch = " + j);
				groundLanes[currentLaneIndex+i].initializePatch(
						j,
						GroundPatchType.GROUND_UP_RIGHT,
						groundLanes[currentLaneIndex+i].getGroundPatchVertexColors(j-1, GroundPatchType.GROUND_UP),
						groundLanes[currentLaneIndex+i-1].getGroundPatchVertexColors(j, GroundPatchType.GROUND_RIGHT)
				);
			}
		}
	}


	/*public void update()
	{
		// If a lane has been displaced enough to the right
		if(offsetX > patchWidth)
		{
			// Swap lanes
			GroundLane tempLane = groundLanes[numPatchesX-1];

			for(int i=numPatchesX-1; i > 0 ; i++)
			{
				groundLanes[i] = groundLanes[i-1];
			}

			groundLanes[0] = tempLane;
		}
	}*/


	public void update(float[] viewMatrix, float[] projectionMatrix, float time)
	{
		vec3 movement = new vec3(0.0f, 0.0f, 0.0f);

		for(int i=0; i < numPatchesX; i++)
		{
			groundLanes[i].update(movement, viewMatrix, projectionMatrix, time);
		}

		/*vec3 position = new vec3(groundLanes[currentLaneIndex].getPatchPosition(currentPatchIndex));


		if(position.z >= maximumOffsetZ)
		{
			position.z = (position.z % maximumOffsetZ) - maximumOffsetZ;

			for(int i=0; i < numPatchesX; i++)
			{
				groundLanes[i].setPatchPosition(currentPatchIndex, position);
			}

			currentPatchIndex++;
			currentPatchIndex = currentPatchIndex % numPatchesZ;
		}*/


		vec3 position = new vec3(groundLanes[numPatchesX-1].getPatchPosition(currentPatchIndex));
		if(position.x > maximumOffsetX)
		{
			position.x = (position.x % maximumOffsetX) - (maximumOffsetX + patchWidth);
			//position.x = (position.x % 100f) - 100f;

			// Swap lanes
			GroundLane tempLane = groundLanes[numPatchesX-1];

			for(int i=numPatchesX-1; i > 0 ; i--)
			{
				//Log.d(TAG, "Patch " + i + " = " + (i-1));
				groundLanes[i] = groundLanes[i-1];
			}

			//Log.d(TAG, "Patch " + 0 + " = " + (numPatchesX-1));
			groundLanes[0] = tempLane;
			//Log.d(TAG, "SWAP");

			/*for(int i=0; i < numPatchesZ; i++)
			{
				groundLanes[0].setPatchPosition(i, position);
			}*/

			//groundLanes[0].setPatchPosition(currentPatchIndex, position);

			// X
			groundLanes[0].setPatchPositionX(0, position.x);
			groundLanes[0].reinitializePatch(
					0,
					GroundPatchType.GROUND_LEFT,
					groundLanes[1].getGroundPatchVertexColors(0, GroundPatchType.GROUND_LEFT)
			);

			for(int i=1; i < numPatchesZ; i++)
			{
				//groundLanes[0].setPatchPosition(i, position);
				groundLanes[0].setPatchPositionX(i, position.x);
				groundLanes[0].reinitializePatch(
						i,
						GroundPatchType.GROUND_UP_LEFT,
						groundLanes[0].getGroundPatchVertexColors(i-1, GroundPatchType.GROUND_UP),
						groundLanes[1].getGroundPatchVertexColors(i, GroundPatchType.GROUND_LEFT)
				);
			}
		}

		if(position.z > maximumOffsetZ)
		{
			// Correct the position of the current line of patches
			position.z = (position.z % maximumOffsetZ) - (maximumOffsetZ/* + patchHeight*/);

			for(int i=0; i < numPatchesX; i++)
			{
				groundLanes[i].setPatchPositionZ(currentPatchIndex, position.z);
			}

			// Change the middle lane patch vertex colors
			groundLanes[currentLaneIndex].reinitializePatch(
					currentPatchIndex,
					GroundPatchType.GROUND_UP,
					groundLanes[currentLaneIndex].getGroundPatchVertexColors(previousPatchIndex, GroundPatchType.GROUND_UP)
			);

			// Change the left and right lanes patches vertex colors
			for(int i=1; i < sideLanes; i++)
			{
				// Left
				groundLanes[currentLaneIndex-i].reinitializePatch(
						currentPatchIndex,
						GroundPatchType.GROUND_UP_LEFT,
						groundLanes[currentLaneIndex-i].getGroundPatchVertexColors(previousPatchIndex, GroundPatchType.GROUND_UP),
						groundLanes[currentLaneIndex-i+1].getGroundPatchVertexColors(currentPatchIndex, GroundPatchType.GROUND_LEFT)
				);

				// Right
				groundLanes[currentLaneIndex+i].reinitializePatch(
						currentPatchIndex,
						GroundPatchType.GROUND_UP_RIGHT,
						groundLanes[currentLaneIndex+i].getGroundPatchVertexColors(previousPatchIndex, GroundPatchType.GROUND_UP),
						groundLanes[currentLaneIndex+i-1].getGroundPatchVertexColors(currentPatchIndex, GroundPatchType.GROUND_RIGHT)
				);
			}

			// New indices for the current and the previous patch
			currentPatchIndex++;
			currentPatchIndex = currentPatchIndex % (numPatchesZ-1);

			previousPatchIndex = currentPatchIndex - 1;
			if(previousPatchIndex < 0)
				previousPatchIndex = numPatchesZ - 1;
		}
	}


	public void draw()
	{
		drawPatches();
		drawGrass();
	}


	public void drawPatches()
	{
		for(int i=0; i < numPatchesX; i++)
		{
			groundLanes[i].draw();
		}
	}


	public void drawGrass()
	{
		parentGrass.bind();

		for(int i=0; i < numPatchesX; i++)
		{
			groundLanes[i].drawGrass();
		}
	}
}
