package com.josepmtomas.rockgame.objects;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;
import com.josepmtomas.rockgame.algebra.vec2;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.poissonGeneration.BoundarySampler;
import com.josepmtomas.rockgame.util.PerspectiveCamera;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static android.opengl.Matrix.*;

/**
 * Created by Josep on 04/10/2014.
 */
public class ObjectsPatch extends BoundarySampler
{
	private static final String TAG = "ObjectsPatch";

	public int index;

	// Camera
	private PerspectiveCamera perspectiveCamera;

	// LOD
	//public int currentLOD = LOD_A;

	// Patch properties
	private final float patchWidth;
	private final float patchHeight;
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

	// Culling points
	private float[] pineTreeCullingPoints;

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


	public ObjectsPatch(int index, float patchWidth, float patchHeight, PerspectiveCamera perspectiveCamera)
	{
		super(0.18f);

		this.index = index;

		this.perspectiveCamera = perspectiveCamera;

		this.patchWidth = patchWidth;
		this.patchHeight = patchHeight;
		this.spreadFactorX = patchWidth * 0.5f;
		this.spreadFactorZ = patchHeight * 0.5f;

		random = new Random();

		pineTreePoints = new float[384];
		pineTreePointsLODA = new float[1024];
		pineTreePointsLODB = new float[1024];

		hugeTreePoints = new float[384];
		hugeTreePointsLODA = new float[1024];
		hugeTreePointsLODB = new float[1024];

		palmTreePoints = new float[384];
		palmTreePointsLODA = new float[1024];
		palmTreePointsLODB = new float[1024];

		birchTreePoints = new float[384];
		birchTreePointsLODA = new float[1024];
		birchTreePointsLODB = new float[1024];

		fernPlantPoints = new float[384];
		fernPlantPointsLODA = new float[1024];
		fernPlantPointsLODB = new float[1024];

		weedPlantPoints = new float[384];
		weedPlantPointsLODA = new float[1024];
		weedPlantPointsLODB = new float[1024];

		bushPlantPoints = new float[384];
		bushPlantPointsLODA = new float[1024];
		bushPlantPointsLODB = new float[1024];

		palmPlantPoints = new float[384];
		palmPlantPointsLODA = new float[1024];
		palmPlantPointsLODB = new float[1024];

		rockAPoints = new float[384];
		rockAPointsLODA = new float[1024];
		rockAPointsLODB = new float[1024];

		rockBPoints = new float[384];
		rockBPointsLODA = new float[1024];
		rockBPointsLODB = new float[1024];

		rockCPoints = new float[384];
		rockCPointsLODA = new float[1024];
		rockCPointsLODB = new float[1024];
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
		for(int i=0; i < 384; i++)
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

		for(int i=0; i < 1024; i++)
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

		for(int i=0; i < (MAX_TREE_INSTANCES_TOTAL*4); i++)
		{
			collisionCylinders[i] = 0f;
			collisionSpheres[i] = 0f;
		}
	}


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

		//pineTreeNumInstances = pineTreeCount;
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


	public void setCullingPoints(float[] pine)
	{
		pineTreeCullingPoints = pine;
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

		// Update collision cylinders
		/*for(int i=0; i < numCollisionCylinders; i++)
		{
			collisionCylinders[i*4] += displacement.x;
			collisionCylinders[i*4 + 1] += displacement.z;
		}*/

		//updateObjectsArrays();
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
				//TODO (enable in the future): if(perspectiveCamera.anyPointInFrustum(pineTreeCullingPoints, pointX, 0f, pointZ))
				{
					pineTreePointsLODA[offsetLODA++] = pointX;
					pineTreePointsLODA[offsetLODA++] = pointZ;
					pineTreePointsLODA[offsetLODA++] = scale;
					pineTreePointsLODA[offsetLODA++] = distance / 800f;

					collisionCylinders[collisionCylindersOffset++] = pointX;
					collisionCylinders[collisionCylindersOffset++] = pointZ;
					collisionCylinders[collisionCylindersOffset++] = 2.9f * scale;
					collisionCylinders[collisionCylindersOffset++] = 0.0f;

					countLODA++;
					collisionCylindersCount++;
				}
			}
			else
			{
				pineTreePointsLODB[offsetLODB++] = pointX;
				pineTreePointsLODB[offsetLODB++] = pointZ;
				pineTreePointsLODB[offsetLODB++] = scale;
				pineTreePointsLODB[offsetLODB++] = distance / 800f;

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
				hugeTreePointsLODA[offsetLODA++] = pointX;
				hugeTreePointsLODA[offsetLODA++] = pointZ;
				hugeTreePointsLODA[offsetLODA++] = scale;
				hugeTreePointsLODA[offsetLODA++] = distance / 800f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 4.5f * scale;
				collisionCylinders[collisionCylindersOffset++] = 1.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				hugeTreePointsLODB[offsetLODB++] = pointX;
				hugeTreePointsLODB[offsetLODB++] = pointZ;
				hugeTreePointsLODB[offsetLODB++] = scale;
				hugeTreePointsLODB[offsetLODB++] = distance / 800f;

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
				palmTreePointsLODA[offsetLODA++] = pointX;
				palmTreePointsLODA[offsetLODA++] = pointZ;
				palmTreePointsLODA[offsetLODA++] = scale;
				palmTreePointsLODA[offsetLODA++] = distance / 800f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 2.4f * scale;
				collisionCylinders[collisionCylindersOffset++] = 2.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				palmTreePointsLODB[offsetLODB++] = pointX;
				palmTreePointsLODB[offsetLODB++] = pointZ;
				palmTreePointsLODB[offsetLODB++] = scale;
				palmTreePointsLODB[offsetLODB++] = distance / 800f;

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
				birchTreePointsLODA[offsetLODA++] = pointX;
				birchTreePointsLODA[offsetLODA++] = pointZ;
				birchTreePointsLODA[offsetLODA++] = scale;
				birchTreePointsLODA[offsetLODA++] = distance / 800f;

				collisionCylinders[collisionCylindersOffset++] = pointX;
				collisionCylinders[collisionCylindersOffset++] = pointZ;
				collisionCylinders[collisionCylindersOffset++] = 1.5f * scale;
				collisionCylinders[collisionCylindersOffset++] = 3.0f;

				countLODA++;
				collisionCylindersCount++;
			}
			else
			{
				birchTreePointsLODB[offsetLODB++] = pointX;
				birchTreePointsLODB[offsetLODB++] = pointZ;
				birchTreePointsLODB[offsetLODB++] = scale;
				birchTreePointsLODB[offsetLODB++] = distance / 800f;

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
				fernPlantPointsLODA[offsetLODA++] = pointX;
				fernPlantPointsLODA[offsetLODA++] = pointZ;
				fernPlantPointsLODA[offsetLODA++] = scale;
				fernPlantPointsLODA[offsetLODA++] = distance / 800f;

				countLODA++;
			}
			else
			{
				fernPlantPointsLODB[offsetLODB++] = pointX;
				fernPlantPointsLODB[offsetLODB++] = pointZ;
				fernPlantPointsLODB[offsetLODB++] = scale;
				fernPlantPointsLODB[offsetLODB++] = distance / 800f;

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
				weedPlantPointsLODA[offsetLODA++] = pointX;
				weedPlantPointsLODA[offsetLODA++] = pointZ;
				weedPlantPointsLODA[offsetLODA++] = scale;
				weedPlantPointsLODA[offsetLODA++] = distance / 800f;

				countLODA++;
			}
			else
			{
				weedPlantPointsLODB[offsetLODB++] = pointX;
				weedPlantPointsLODB[offsetLODB++] = pointZ;
				weedPlantPointsLODB[offsetLODB++] = scale;
				weedPlantPointsLODB[offsetLODB++] = distance / 800f;

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
				bushPlantPointsLODA[offsetLODA++] = pointX;
				bushPlantPointsLODA[offsetLODA++] = pointZ;
				bushPlantPointsLODA[offsetLODA++] = scale;
				bushPlantPointsLODA[offsetLODA++] = distance / 800f;

				countLODA++;
			}
			else
			{
				bushPlantPointsLODB[offsetLODB++] = pointX;
				bushPlantPointsLODB[offsetLODB++] = pointZ;
				bushPlantPointsLODB[offsetLODB++] = scale;
				bushPlantPointsLODB[offsetLODB++] = distance / 800f;

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
				palmPlantPointsLODA[offsetLODA++] = pointX;
				palmPlantPointsLODA[offsetLODA++] = pointZ;
				palmPlantPointsLODA[offsetLODA++] = scale;
				palmPlantPointsLODA[offsetLODA++] = distance / 800f;

				countLODA++;
			}
			else
			{
				palmPlantPointsLODB[offsetLODB++] = pointX;
				palmPlantPointsLODB[offsetLODB++] = pointZ;
				palmPlantPointsLODB[offsetLODB++] = scale;
				palmPlantPointsLODB[offsetLODB++] = distance / 800f;

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
				rockAPointsLODA[offsetLODA++] = pointX;
				rockAPointsLODA[offsetLODA++] = pointZ;
				rockAPointsLODA[offsetLODA++] = scale;
				rockAPointsLODA[offsetLODA++] = distance / 800f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.51f * scale);
				collisionSpheres[collisionSpheresOffset++] = (4.261f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ + (0.903f * scale);
				collisionSpheres[collisionSpheresOffset++] = 12f * scale; //13.766f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				rockAPointsLODB[offsetLODB++] = pointX;
				rockAPointsLODB[offsetLODB++] = pointZ;
				rockAPointsLODB[offsetLODB++] = scale;
				rockAPointsLODB[offsetLODB++] = distance / 800f;

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
				rockBPointsLODA[offsetLODA++] = pointX;
				rockBPointsLODA[offsetLODA++] = pointZ;
				rockBPointsLODA[offsetLODA++] = scale;
				rockBPointsLODA[offsetLODA++] = distance / 800f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.208f * scale);
				collisionSpheres[collisionSpheresOffset++] = (-0.908f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ + (0.692f * scale);
				collisionSpheres[collisionSpheresOffset++] = 8.769f * scale; //13.766f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				rockBPointsLODB[offsetLODB++] = pointX;
				rockBPointsLODB[offsetLODB++] = pointZ;
				rockBPointsLODB[offsetLODB++] = scale;
				rockBPointsLODB[offsetLODB++] = distance / 800f;

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
				rockCPointsLODA[offsetLODA++] = pointX;
				rockCPointsLODA[offsetLODA++] = pointZ;
				rockCPointsLODA[offsetLODA++] = scale;
				rockCPointsLODA[offsetLODA++] = distance / 800f;

				collisionSpheres[collisionSpheresOffset++] = pointX + (0.246f * scale);
				collisionSpheres[collisionSpheresOffset++] = (3.743f * scale);
				collisionSpheres[collisionSpheresOffset++] = pointZ;
				collisionSpheres[collisionSpheresOffset++] = 9f * scale;

				countLODA++;
				collisionSpheresCount++;
			}
			else
			{
				rockCPointsLODB[offsetLODB++] = pointX;
				rockCPointsLODB[offsetLODB++] = pointZ;
				rockCPointsLODB[offsetLODB++] = scale;
				rockCPointsLODB[offsetLODB++] = distance / 800f;

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

	//TODO: incomplete
	public void saveState(FileOutputStream outputStream, int x, int z) throws IOException
	{
		int numPoints;
		StringBuilder builder = new StringBuilder();

		builder.append("OBJECTS_PATCH ");
		builder.append(x);	builder.append(" ");
		builder.append(z);	builder.append(" ");
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

	}
}
