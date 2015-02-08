package com.josepmtomas.runstonerun.objects;

import static com.josepmtomas.runstonerun.Constants.*;
import static com.josepmtomas.runstonerun.algebra.operations.*;
import com.josepmtomas.runstonerun.algebra.vec2;
import com.josepmtomas.runstonerun.algebra.vec3;
import com.josepmtomas.runstonerun.poissonGeneration.BoundarySampler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static android.opengl.Matrix.*;

/**
 * Created by Josep on 04/10/2014.
 * @author Josep
 */

public class ObjectsPatch extends BoundarySampler
{
	public int index;

	// Patch properties
	private final float spreadFactorX;
	private final float spreadFactorZ;

	private Random random;

	public vec3 currentPosition = new vec3(0.0f);

	public float[] pineTreePoints;
	public int pineTreeNumPoints;
	public float[] pineTreePointsLODA;
	public float[] pineTreePointsLODB;

	public float[] hugeTreePoints;
	public int hugeTreeNumPoints;
	public float[] hugeTreePointsLODA;
	public float[] hugeTreePointsLODB;

	public float[] palmTreePoints;
	public int palmTreeNumPoints;
	public float[] palmTreePointsLODA;
	public float[] palmTreePointsLODB;

	public float[] birchTreePoints;
	public int birchTreeNumPoints;
	public float[] birchTreePointsLODA;
	public float[] birchTreePointsLODB;

	public float[] fernPlantPoints;
	public int fernPlantNumPoints;
	public float[] fernPlantPointsLODA;
	public float[] fernPlantPointsLODB;

	public float[] weedPlantPoints;
	public int weedPlantNumPoints;
	public float[] weedPlantPointsLODA;
	public float[] weedPlantPointsLODB;

	public float[] bushPlantPoints;
	public int bushPlantNumPoints;
	public float[] bushPlantPointsLODA;
	public float[] bushPlantPointsLODB;

	public float[] palmPlantPoints;
	public int palmPlantNumPoints;
	public float[] palmPlantPointsLODA;
	public float[] palmPlantPointsLODB;

	public float[] rockAPoints;
	public int rockANumPoints;
	public float[] rockAPointsLODA;
	public float[] rockAPointsLODB;

	public float[] rockBPoints;
	public int rockBNumPoints;
	public float[] rockBPointsLODA;
	public float[] rockBPointsLODB;

	public float[] rockCPoints;
	public int rockCNumPoints;
	public float[] rockCPointsLODA;
	public float[] rockCPointsLODB;


	public int[] pineTreeNumInstances = {0,0};
	public int[] hugeTreeNumInstances = {0,0};
	public int[] palmTreeNumInstances = {0,0};
	public int[] birchTreeNumInstances = {0,0};
	public int[] fernPlantNumInstances = {0,0};
	public int[] weedPlantNumInstances = {0,0};
	public int[] bushPlantNumInstances = {0,0};
	public int[] palmPlantNumInstances = {0,0};
	public int[] rockANumInstances = {0,0};
	public int[] rockBNumInstances = {0,0};
	public int[] rockCNumInstances = {0,0};

	// Matrices
	public float[] model = new float[16];
	public float[] modelViewProjection = new float[16];
	public float[] lightModelViewProjection = new float[16];
	public float[] viewProjection;

	// Collisions
	public float[] collisionCylinders = new float[MAX_TREE_INSTANCES_TOTAL * 4];
	public float[] collisionSpheres = new float[MAX_TREE_INSTANCES_TOTAL * 4];

	public int numCollisionCylinders = 0;
	public int numCollisionSpheres = 0;


	public ObjectsPatch(int index, float patchWidth, float patchHeight)
	{
		super(0.18f);

		this.index = index;

		this.spreadFactorX = patchWidth * 0.5f;
		this.spreadFactorZ = patchHeight * 0.5f;

		random = new Random();

		pineTreePoints = new float[48];
		pineTreePointsLODA = new float[64];
		pineTreePointsLODB = new float[64];

		hugeTreePoints = new float[48];
		hugeTreePointsLODA = new float[64];
		hugeTreePointsLODB = new float[64];

		palmTreePoints = new float[48];
		palmTreePointsLODA = new float[64];
		palmTreePointsLODB = new float[64];

		birchTreePoints = new float[48];
		birchTreePointsLODA = new float[64];
		birchTreePointsLODB = new float[64];

		fernPlantPoints = new float[48];
		fernPlantPointsLODA = new float[64];
		fernPlantPointsLODB = new float[64];

		weedPlantPoints = new float[48];
		weedPlantPointsLODA = new float[64];
		weedPlantPointsLODB = new float[64];

		bushPlantPoints = new float[48];
		bushPlantPointsLODA = new float[64];
		bushPlantPointsLODB = new float[64];

		palmPlantPoints = new float[48];
		palmPlantPointsLODA = new float[64];
		palmPlantPointsLODB = new float[64];

		rockAPoints = new float[48];
		rockAPointsLODA = new float[64];
		rockAPointsLODB = new float[64];

		rockBPoints = new float[48];
		rockBPointsLODA = new float[64];
		rockBPointsLODB = new float[64];

		rockCPoints = new float[384];
		rockCPointsLODA = new float[64];
		rockCPointsLODB = new float[64];
	}


	public void initialize()
	{
		completeStrict();
		spread(spreadFactorX, spreadFactorZ);
		initializeTreePoints();
		filterPoints();
	}


	public void reinitialize()
	{
		filterPoints();
		updateObjectsArrays();
	}


	private void initializeTreePoints()
	{
		for(int i=0; i < 48; i++)
		{
			pineTreePoints[i] = 0f;
			hugeTreePoints[i] = 0f;
			palmTreePoints[i] = 0f;
			birchTreePoints[i] = 0f;
			fernPlantPoints[i] = 0f;
			weedPlantPoints[i] = 0f;
			bushPlantPoints[i] = 0f;
			palmPlantPoints[i] = 0f;
			rockAPoints[i] = 0f;
			rockBPoints[i] = 0f;
			rockCPoints[i] = 0f;
		}

		for(int i=0; i<64; i++)
		{
			pineTreePointsLODA[i] = 0f;
			pineTreePointsLODB[i] = 0f;
			hugeTreePointsLODA[i] = 0f;
			hugeTreePointsLODB[i] = 0f;
			palmTreePointsLODA[i] = 0f;
			palmTreePointsLODB[i] = 0f;
			birchTreePointsLODA[i] = 0f;
			birchTreePointsLODB[i] = 0f;
			fernPlantPointsLODA[i] = 0f;
			fernPlantPointsLODB[i] = 0f;
			weedPlantPointsLODA[i] = 0f;
			weedPlantPointsLODB[i] = 0f;
			bushPlantPointsLODA[i] = 0f;
			bushPlantPointsLODB[i] = 0f;
			palmPlantPointsLODA[i] = 0f;
			palmPlantPointsLODB[i] = 0f;
			rockAPointsLODA[i] = 0f;
			rockAPointsLODB[i] = 0f;
			rockBPointsLODA[i] = 0f;
			rockBPointsLODB[i] = 0f;
			rockCPointsLODA[i] = 0f;
			rockCPointsLODB[i] = 0f;
		}

		/*for(int i=0; i < 1024; i++)
		{


		}*/

		for(int i=0; i < (MAX_TREE_INSTANCES_TOTAL*4); i++)
		{
			collisionCylinders[i] = 0f;
			collisionSpheres[i] = 0f;
		}
	}


	@SuppressWarnings("all")
	private void filterPoints()
	{
		int pineTreeCount = 0;
		int hugeTreeCount = 0;
		int palmTreeCount = 0;
		int birchTreeCount = 0;
		int fernPlantCount = 0;
		int weedPlantCount = 0;
		int bushPlantCount = 0;
		int palmPlantCount = 0;
		int rockACount = 0;
		int rockBCount = 0;
		int rockCCount = 0;

		int pineTreeOffset = 0;
		int hugeTreeOffset = 0;
		int palmTreeOffset = 0;
		int birchTreeOffset = 0;
		int fernPlantOffset = 0;
		int weedPlantOffset = 0;
		int bushPlantOffset = 0;
		int palmPlantOffset = 0;
		int rockAOffset = 0;
		int rockBOffset = 0;
		int rockCOffset = 0;

		int collisionCylindersOffset = 0;
		int collisionSpheresOffset = 0;

		final int size = points.size();
		float randomValue;
		float scaleValue;
		vec2 currentPoint;

		pineTreeNumPoints = 0;
		hugeTreeNumPoints = 0;
		palmTreeNumPoints = 0;
		birchTreeNumPoints = 0;
		fernPlantNumPoints = 0;
		weedPlantNumPoints = 0;
		bushPlantNumPoints = 0;
		palmPlantNumPoints = 0;
		rockANumPoints = 0;
		rockBNumPoints = 0;
		rockCNumPoints = 0;

		numCollisionCylinders = 0;
		numCollisionSpheres = 0;


		for(int i=0; i < size; i++)
		{
			currentPoint = points.get(i);
			randomValue = random.nextFloat();

			if(randomValue < 0.1125f)
			{
				scaleValue = random.nextFloat() * PINE_TREE_SCALE_DIFFERENCE + PINE_TREE_MIN_SCALE;

				pineTreePoints[pineTreeOffset++] = currentPoint.x;
				pineTreePoints[pineTreeOffset++] = currentPoint.y;
				pineTreePoints[pineTreeOffset++] = scaleValue;

				pineTreeCount++;

				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.z + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 2.9f * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 0f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.225f)
			{
				scaleValue = random.nextFloat() * HUGE_TREE_SCALE_DIFFERENCE + HUGE_TREE_MIN_SCALE;

				hugeTreePoints[hugeTreeOffset++] = currentPoint.x;
				hugeTreePoints[hugeTreeOffset++] = currentPoint.y;
				hugeTreePoints[hugeTreeOffset++] = scaleValue;

				hugeTreeCount++;

				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.z + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 4.5f * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 1f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.3375f)
			{
				scaleValue = random.nextFloat() * PALM_TREE_SCALE_DIFFERENCE + PALM_TREE_MIN_SCALE;

				palmTreePoints[palmTreeOffset++] = currentPoint.x;
				palmTreePoints[palmTreeOffset++] = currentPoint.y;
				palmTreePoints[palmTreeOffset++] = scaleValue;

				palmTreeCount++;

				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.z + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 2.4f * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 2f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.45f)
			{
				scaleValue = random.nextFloat() * BIRCH_TREE_SCALE_DIFFERENCE + BIRCH_TREE_MIN_SCALE;

				birchTreePoints[birchTreeOffset++] = currentPoint.x;
				birchTreePoints[birchTreeOffset++] = currentPoint.y;
				birchTreePoints[birchTreeOffset++] = scaleValue;

				birchTreeCount++;

				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.y + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 1.5f * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 3f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.6f)
			{
				scaleValue = random.nextFloat() * FERN_PLANT_SCALE_DIFFERENCE + FERN_PLANT_MIN_SCALE;

				fernPlantPoints[fernPlantOffset++] = currentPoint.x;
				fernPlantPoints[fernPlantOffset++] = currentPoint.y;
				fernPlantPoints[fernPlantOffset++] = scaleValue;

				fernPlantCount++;
			}
			else if(randomValue < 0.675f)
			{
				scaleValue = random.nextFloat() * WEED_PLANT_SCALE_DIFFERENCE + WEED_PLANT_MIN_SCALE;

				weedPlantPoints[weedPlantOffset++] = currentPoint.x;
				weedPlantPoints[weedPlantOffset++] = currentPoint.y;
				weedPlantPoints[weedPlantOffset++] = scaleValue;

				weedPlantCount++;
			}
			else if(randomValue < 0.75f)
			{
				scaleValue = random.nextFloat() * BUSH_PLANT_SCALE_DIFFERENCE + BUSH_PLANT_MIN_SCALE;

				bushPlantPoints[bushPlantOffset++] = currentPoint.x;
				bushPlantPoints[bushPlantOffset++] = currentPoint.y;
				bushPlantPoints[bushPlantOffset++] = scaleValue;

				bushPlantCount++;
			}
			else if(randomValue < 0.825f)
			{
				scaleValue = random.nextFloat() * PALM_PLANT_SCALE_DIFFERENCE + PALM_PLANT_MIN_SCALE;

				palmPlantPoints[palmPlantOffset++] = currentPoint.x;
				palmPlantPoints[palmPlantOffset++] = currentPoint.y;
				palmPlantPoints[palmPlantOffset++] = scaleValue;

				palmPlantCount++;
			}
			else if(randomValue < 0.85f)
			{
				scaleValue = random.nextFloat() * ROCK_A_SCALE_DIFFERENCE + ROCK_A_MIN_SCALE;

				rockAPoints[rockAOffset++] = currentPoint.x;
				rockAPoints[rockAOffset++] = currentPoint.y;
				rockAPoints[rockAOffset++] = scaleValue;

				collisionSpheres[collisionSpheresOffset++] = currentPosition.x + currentPoint.x + (0.51f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.y + (4.261f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.z + currentPoint.y + (0.903f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = 12f * scaleValue;//13.766f * scaleValue;
				numCollisionSpheres++;

				rockACount++;
			}
			else if(randomValue < 0.9f)
			{
				scaleValue = random.nextFloat() * ROCK_B_SCALE_DIFFERENCE + ROCK_B_MIN_SCALE;

				rockBPoints[rockBOffset++] = currentPoint.x;
				rockBPoints[rockBOffset++] = currentPoint.y;
				rockBPoints[rockBOffset++] = scaleValue;

				collisionSpheres[collisionSpheresOffset++] = currentPosition.x + currentPoint.x + (0.208f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.y + (-0.908f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.z + currentPoint.y + (0.692f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = 8.769f * scaleValue;
				numCollisionSpheres++;

				rockBCount++;
			}
			else if(randomValue < 1f)
			{
				scaleValue = random.nextFloat() * ROCK_C_SCALE_DIFFERENCE + ROCK_C_MIN_SCALE;

				rockCPoints[rockCOffset++] = currentPoint.x;
				rockCPoints[rockCOffset++] = currentPoint.y;
				rockCPoints[rockCOffset++] = scaleValue;

				collisionSpheres[collisionSpheresOffset++] = currentPosition.x + currentPoint.x + (0.246f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.y + (3.743f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.z + currentPoint.y;
				collisionSpheres[collisionSpheresOffset++] = 9f;
				numCollisionSpheres++;

				rockCCount++;
			}
		}

		pineTreeNumPoints = pineTreeCount;
		hugeTreeNumPoints = hugeTreeCount;
		palmTreeNumPoints = palmTreeCount;
		birchTreeNumPoints = birchTreeCount;
		fernPlantNumPoints = fernPlantCount;
		weedPlantNumPoints = weedPlantCount;
		bushPlantNumPoints = bushPlantCount;
		palmPlantNumPoints = palmPlantCount;
		rockANumPoints = rockACount;
		rockBNumPoints = rockBCount;
		rockCNumPoints = rockCCount;
	}


	public void setCurrentPosition(float x, float y, float z)
	{
		this.currentPosition.setValues(x,y,z);

		setIdentityM(model, 0);
		translateM(model, 0, x, y, z);
	}


	public vec3 getCurrentPosition()
	{
		return currentPosition;
	}


	public void update(float[] viewProjection, float[] lightViewProjection,  vec3 displacement)
	{
		this.viewProjection = viewProjection;
		this.currentPosition.add(displacement);

		setIdentityM(model, 0);
		translateM(model, 0, currentPosition.x , currentPosition.y, currentPosition.z);

		multiplyMM(modelViewProjection, 0, viewProjection, 0, model, 0);

		multiplyMM(lightModelViewProjection, 0, lightViewProjection, 0, model, 0);
	}


	public void updateObjectsArrays()
	{
		int offsetLODA = 0;
		int offsetLODB = 0;
		int countLODA = 0;
		int countLODB = 0;
		int collisionCylindersOffset = 0;
		int collisionCylindersCount = 0;
		int collisionSpheresOffset = 0;
		int collisionSpheresCount = 0;
		float pointX, pointZ, scale, distance;

		numCollisionCylinders = 0;
		numCollisionSpheres = 0;

		for(int i=0; i < pineTreeNumPoints; i++)
		{
			pointX = pineTreePoints[i*3];
			pointZ = pineTreePoints[i*3 + 1];
			scale  = pineTreePoints[i*3 + 2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				pineTreePointsLODA[offsetLODA++] = pointX;
				pineTreePointsLODA[offsetLODA++] = pointZ;
				pineTreePointsLODA[offsetLODA++] = scale;
				pineTreePointsLODA[offsetLODA++] = distance;// / 700f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 2.9f * scale;
				collisionCylinders[collisionCylindersOffset++] = 0.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				pineTreePointsLODB[offsetLODB++] = pointX;
				pineTreePointsLODB[offsetLODB++] = pointZ;
				pineTreePointsLODB[offsetLODB++] = scale;
				pineTreePointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		pineTreeNumInstances[LOD_A] = countLODA;
		pineTreeNumInstances[LOD_B] = countLODB;

		// huge tree

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < hugeTreeNumPoints; i++)
		{
			pointX = hugeTreePoints[i*3];
			pointZ = hugeTreePoints[i*3 + 1];
			scale  = hugeTreePoints[i*3 + 2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				hugeTreePointsLODA[offsetLODA++] = pointX;
				hugeTreePointsLODA[offsetLODA++] = pointZ;
				hugeTreePointsLODA[offsetLODA++] = scale;
				hugeTreePointsLODA[offsetLODA++] = distance;// / 700f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 4.5f * scale;
				collisionCylinders[collisionCylindersOffset++] = 1.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				hugeTreePointsLODB[offsetLODB++] = pointX;
				hugeTreePointsLODB[offsetLODB++] = pointZ;
				hugeTreePointsLODB[offsetLODB++] = scale;
				hugeTreePointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		hugeTreeNumInstances[LOD_A] = countLODA;
		hugeTreeNumInstances[LOD_B] = countLODB;

		// palmTree

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < palmTreeNumPoints; i++)
		{
			pointX = palmTreePoints[i*3];
			pointZ = palmTreePoints[i*3 + 1];
			scale  = palmTreePoints[i*3 + 2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				palmTreePointsLODA[offsetLODA++] = pointX;
				palmTreePointsLODA[offsetLODA++] = pointZ;
				palmTreePointsLODA[offsetLODA++] = scale;
				palmTreePointsLODA[offsetLODA++] = distance;// / 700f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 2.4f * scale;
				collisionCylinders[collisionCylindersOffset++] = 2.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				palmTreePointsLODB[offsetLODB++] = pointX;
				palmTreePointsLODB[offsetLODB++] = pointZ;
				palmTreePointsLODB[offsetLODB++] = scale;
				palmTreePointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		palmTreeNumInstances[LOD_A] = countLODA;
		palmTreeNumInstances[LOD_B] = countLODB;

		// birch tree

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < birchTreeNumPoints; i++)
		{
			pointX = birchTreePoints[i*3];
			pointZ = birchTreePoints[i*3 + 1];
			scale  = birchTreePoints[i*3 + 2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				birchTreePointsLODA[offsetLODA++] = pointX;
				birchTreePointsLODA[offsetLODA++] = pointZ;
				birchTreePointsLODA[offsetLODA++] = scale;
				birchTreePointsLODA[offsetLODA++] = distance;// / 700f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 1.5f * scale;
				collisionCylinders[collisionCylindersOffset++] = 3.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				birchTreePointsLODB[offsetLODB++] = pointX;
				birchTreePointsLODB[offsetLODB++] = pointZ;
				birchTreePointsLODB[offsetLODB++] = scale;
				birchTreePointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		birchTreeNumInstances[LOD_A] = countLODA;
		birchTreeNumInstances[LOD_B] = countLODB;

		// fern plant

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < fernPlantNumPoints; i++)
		{
			pointX = fernPlantPoints[i*3];
			pointZ = fernPlantPoints[i*3 +1];
			scale  = fernPlantPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				fernPlantPointsLODA[offsetLODA++] = pointX;
				fernPlantPointsLODA[offsetLODA++] = pointZ;
				fernPlantPointsLODA[offsetLODA++] = scale;
				fernPlantPointsLODA[offsetLODA++] = distance;// / 700f;

				countLODA++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				fernPlantPointsLODB[offsetLODB++] = pointX;
				fernPlantPointsLODB[offsetLODB++] = pointZ;
				fernPlantPointsLODB[offsetLODB++] = scale;
				fernPlantPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		fernPlantNumInstances[LOD_A] = countLODA;
		fernPlantNumInstances[LOD_B] = countLODB;

		// weed plant

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < weedPlantNumPoints; i++)
		{
			pointX = weedPlantPoints[i*3];
			pointZ = weedPlantPoints[i*3 +1];
			scale  = weedPlantPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				weedPlantPointsLODA[offsetLODA++] = pointX;
				weedPlantPointsLODA[offsetLODA++] = pointZ;
				weedPlantPointsLODA[offsetLODA++] = scale;
				weedPlantPointsLODA[offsetLODA++] = distance;// / 700f;

				countLODA++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				weedPlantPointsLODB[offsetLODB++] = pointX;
				weedPlantPointsLODB[offsetLODB++] = pointZ;
				weedPlantPointsLODB[offsetLODB++] = scale;
				weedPlantPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		weedPlantNumInstances[LOD_A] = countLODA;
		weedPlantNumInstances[LOD_B] = countLODB;

		// bush plant

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < bushPlantNumPoints; i++)
		{
			pointX = bushPlantPoints[i*3];
			pointZ = bushPlantPoints[i*3 +1];
			scale  = bushPlantPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				bushPlantPointsLODA[offsetLODA++] = pointX;
				bushPlantPointsLODA[offsetLODA++] = pointZ;
				bushPlantPointsLODA[offsetLODA++] = scale;
				bushPlantPointsLODA[offsetLODA++] = distance;// / 700f;

				countLODA++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				bushPlantPointsLODB[offsetLODB++] = pointX;
				bushPlantPointsLODB[offsetLODB++] = pointZ;
				bushPlantPointsLODB[offsetLODB++] = scale;
				bushPlantPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		bushPlantNumInstances[LOD_A] = countLODA;
		bushPlantNumInstances[LOD_B] = countLODB;

		// palm plant

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < palmPlantNumPoints; i++)
		{
			pointX = palmPlantPoints[i*3];
			pointZ = palmPlantPoints[i*3 +1];
			scale  = palmPlantPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				palmPlantPointsLODA[offsetLODA++] = pointX;
				palmPlantPointsLODA[offsetLODA++] = pointZ;
				palmPlantPointsLODA[offsetLODA++] = scale;
				palmPlantPointsLODA[offsetLODA++] = distance;// / 700f;

				countLODA++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				palmPlantPointsLODB[offsetLODB++] = pointX;
				palmPlantPointsLODB[offsetLODB++] = pointZ;
				palmPlantPointsLODB[offsetLODB++] = scale;
				palmPlantPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		palmPlantNumInstances[LOD_A] = countLODA;
		palmPlantNumInstances[LOD_B] = countLODB;

		// rock A

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < rockANumPoints; i++)
		{
			pointX = rockAPoints[i*3];
			pointZ = rockAPoints[i*3 +1];
			scale  = rockAPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				rockAPointsLODA[offsetLODA++] = pointX;
				rockAPointsLODA[offsetLODA++] = pointZ;
				rockAPointsLODA[offsetLODA++] = scale;
				rockAPointsLODA[offsetLODA++] = distance;// / 700f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.51f * scale);
				collisionSpheres[collisionSpheresOffset++] = (4.261f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ + (0.903f * scale);
				collisionSpheres[collisionSpheresOffset++] = 12f * scale; //13.766f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				rockAPointsLODB[offsetLODB++] = pointX;
				rockAPointsLODB[offsetLODB++] = pointZ;
				rockAPointsLODB[offsetLODB++] = scale;
				rockAPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		rockANumInstances[LOD_A] = countLODA;
		rockANumInstances[LOD_B] = countLODB;

		// rock B

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < rockBNumPoints; i++)
		{
			pointX = rockBPoints[i*3];
			pointZ = rockBPoints[i*3 +1];
			scale  = rockBPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				rockBPointsLODA[offsetLODA++] = pointX;
				rockBPointsLODA[offsetLODA++] = pointZ;
				rockBPointsLODA[offsetLODA++] = scale;
				rockBPointsLODA[offsetLODA++] = distance;// / 700f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.208f * scale);
				collisionSpheres[collisionSpheresOffset++] = (-0.908f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ + (0.692f * scale);
				collisionSpheres[collisionSpheresOffset++] = 8.769f * scale; //13.766f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				rockBPointsLODB[offsetLODB++] = pointX;
				rockBPointsLODB[offsetLODB++] = pointZ;
				rockBPointsLODB[offsetLODB++] = scale;
				rockBPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		rockBNumInstances[LOD_A] = countLODA;
		rockBNumInstances[LOD_B] = countLODB;

		// rock C

		offsetLODA = 0;
		offsetLODB = 0;
		countLODA = 0;
		countLODB = 0;

		for(int i=0; i < rockCNumPoints; i++)
		{
			pointX = rockCPoints[i*3];
			pointZ = rockCPoints[i*3 +1];
			scale  = rockCPoints[i*3 +2];

			pointX += currentPosition.x;
			pointZ += currentPosition.z;

			distance = distanceToOrigin(pointX, pointZ);

			if(distance < TREE_LOD_A_MAX_DISTANCE)
			{
				distance = distance * 0.0014285f; // 1/700
				distance = distance * distance;
				rockCPointsLODA[offsetLODA++] = pointX;
				rockCPointsLODA[offsetLODA++] = pointZ;
				rockCPointsLODA[offsetLODA++] = scale;
				rockCPointsLODA[offsetLODA++] = distance;// / 700f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.246f * scale);
				collisionSpheres[collisionSpheresOffset++] = (3.743f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ;
				collisionSpheres[collisionSpheresOffset++] = 9f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				distance = Math.min(1.0f, distance * 0.0014285f); // 1/700
				distance = distance * distance;
				rockCPointsLODB[offsetLODB++] = pointX;
				rockCPointsLODB[offsetLODB++] = pointZ;
				rockCPointsLODB[offsetLODB++] = scale;
				rockCPointsLODB[offsetLODB++] = distance;//Math.min(1.0f, distance / 700f);

				countLODB++;
			}
		}

		rockCNumInstances[LOD_A] = countLODA;
		rockCNumInstances[LOD_B] = countLODB;

		//

		numCollisionCylinders = collisionCylindersCount;
		numCollisionSpheres = collisionSpheresCount;
	}


	public float deleteTreeAfterCollision(int index)
	{
		float scale;
		int i = index;

		if(i < pineTreeNumInstances[LOD_A])
		{
			scale = pineTreePoints[i * 3 + 2];
			pineTreePoints[i * 3 + 2] = 0f;
			return scale;
		}

		i = i - pineTreeNumInstances[LOD_A];

		if (i < hugeTreeNumInstances[LOD_A])
		{
			scale = hugeTreePoints[i * 3 + 2];
			hugeTreePoints[i * 3 + 2] = 0f;
			return scale;
		}

		i = i - hugeTreeNumInstances[LOD_A];

		if(i < palmTreeNumInstances[LOD_A])
		{
			scale = palmTreePoints[i*3 + 2];
			palmTreePoints[i*3 + 2] = 0f;
			return scale;
		}

		else
		{
			i = i - palmTreeNumInstances[LOD_A];
			scale = birchTreePoints[i*3 + 2];
			birchTreePoints[i*3 + 2] = 0f;
			return scale;
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	// Save/Load state
	////////////////////////////////////////////////////////////////////////////////////////////

	public void saveState(FileOutputStream outputStream) throws IOException
	{
		int numPoints;
		StringBuilder builder = new StringBuilder();

		builder.setLength(0);
		builder.append("POSITION ");
		builder.append(currentPosition.x);	builder.append(" ");
		builder.append(currentPosition.y);	builder.append(" ");
		builder.append(currentPosition.z);	builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("SAMPLER_POINTS ");
		numPoints = points.size();
		builder.append(numPoints);
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(points.get(i).x);
			builder.append(" ");	builder.append(points.get(i).y);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("PINE_TREE_POINTS ");
		builder.append(pineTreeNumPoints);
		numPoints = pineTreePoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(pineTreePoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("HUGE_TREE_POINTS ");
		builder.append(hugeTreeNumPoints);
		numPoints = hugeTreePoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(hugeTreePoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("PALM_TREE_POINTS ");
		builder.append(palmTreeNumPoints);
		numPoints = palmTreePoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(palmTreePoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("BIRCH_TREE_POINTS ");
		builder.append(birchTreeNumPoints);
		numPoints = birchTreePoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(birchTreePoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("FERN_PLANT_POINTS ");
		builder.append(fernPlantNumPoints);
		numPoints = fernPlantPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(fernPlantPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("WEED_PLANT_POINTS ");
		builder.append(weedPlantNumPoints);
		numPoints = weedPlantPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(weedPlantPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("BUSH_PLANT_POINTS ");
		builder.append(bushPlantNumPoints);
		numPoints = bushPlantPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(bushPlantPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("PALM_PLANT_POINTS ");
		builder.append(palmPlantNumPoints);
		numPoints = palmPlantPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(palmPlantPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("ROCK_A_POINTS ");
		builder.append(rockANumPoints);
		numPoints = rockAPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(rockAPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("ROCK_B_POINTS ");
		builder.append(rockBNumPoints);
		numPoints = rockBPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(rockBPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());

		builder.setLength(0);
		builder.append("ROCK_C_POINTS ");
		builder.append(rockCNumPoints);
		numPoints = rockCPoints.length;
		for(int i=0; i<numPoints; i++)
		{
			builder.append(" ");	builder.append(rockCPoints[i]);
		}
		builder.append("\n");
		outputStream.write(builder.toString().getBytes());
	}


	public void loadState(BufferedReader bufferedReader) throws IOException
	{
		String line;
		String[] tokens;
		int numPoints;
		int offset;
		float x, y;

		// Read position
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		currentPosition.x = Float.parseFloat(tokens[1]);
		currentPosition.y = Float.parseFloat(tokens[2]);
		currentPosition.z = Float.parseFloat(tokens[3]);

		// Read sampler points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		points.clear();
		numPoints = Integer.parseInt(tokens[1]);
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			x = Float.parseFloat(tokens[offset++]);
			y = Float.parseFloat(tokens[offset++]);
			points.add(new vec2(x,y));
		}

		// Read pine tree points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		pineTreeNumPoints = Integer.parseInt(tokens[1]);
		numPoints = pineTreeNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			pineTreePoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read huge tree points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		hugeTreeNumPoints = Integer.parseInt(tokens[1]);
		numPoints = hugeTreeNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			hugeTreePoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read palm tree points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		palmTreeNumPoints = Integer.parseInt(tokens[1]);
		numPoints = palmTreeNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			palmTreePoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read birch tree points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		birchTreeNumPoints = Integer.parseInt(tokens[1]);
		numPoints = birchTreeNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			birchTreePoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read fern plant points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		fernPlantNumPoints = Integer.parseInt(tokens[1]);
		numPoints = fernPlantNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			fernPlantPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read weed plant points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		fernPlantNumPoints = Integer.parseInt(tokens[1]);
		numPoints = fernPlantNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			weedPlantPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read bush plant points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		bushPlantNumPoints = Integer.parseInt(tokens[1]);
		numPoints = bushPlantNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			bushPlantPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read palm plant points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		palmPlantNumPoints = Integer.parseInt(tokens[1]);
		numPoints = palmPlantNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			palmPlantPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read rock a points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		rockANumPoints = Integer.parseInt(tokens[1]);
		numPoints = rockANumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			rockAPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read rock b points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		rockBNumPoints = Integer.parseInt(tokens[1]);
		numPoints = rockBNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			rockBPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		// Read rock c points
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		rockCNumPoints = Integer.parseInt(tokens[1]);
		numPoints = rockCNumPoints * 3;
		offset = 2;
		for(int i=0; i < numPoints; i++)
		{
			rockCPoints[i] = Float.parseFloat(tokens[offset++]);
		}

		updateObjectsArrays();
	}
}