package com.josepmtomas.rockgame.objectsForwardPlus;

import android.util.Log;

import static com.josepmtomas.rockgame.Constants.*;
import static com.josepmtomas.rockgame.algebra.operations.*;
import com.josepmtomas.rockgame.algebra.vec2;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.poissonGeneration.BoundarySampler;
import com.josepmtomas.rockgame.util.PerspectiveCamera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES30.*;
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
	public int currentLOD = LOD_A;

	// Patch properties
	private final float patchWidth;
	private final float patchHeight;
	private final float spreadFactorX;
	private final float spreadFactorZ;

	private Random random;

	public vec3 currentPosition = new vec3(0.0f);


	/*private FloatBuffer pineTreeMatricesBuffer;
	public int[] pineTreeMatricesUbo = new int[1];
	public int pineTreeNumInstances;*/

	/*public ArrayList<vec2> pineTreePositions = new ArrayList<vec2>();
	public ArrayList<vec2> hugeTreePositions = new ArrayList<vec2>();
	public ArrayList<vec2> fernPlantPositions = new ArrayList<vec2>();*/

	////public float[] pineTreeMatrices;
	/*public float[] pineTreeMatricesLODA;
	public float[] pineTreeMatricesLODB;
	public float[] pineTreeIndicesLODA;
	public float[] pineTreeIndicesLODB;*/

	public float[] pineTreePoints;
	public int pineTreeNumPoints;
	public float[] pineTreePointsLODA;
	public float[] pineTreePointsLODB;

	public float[] hugeTreePoints;
	public int hugeTreeNumPoints;
	public float[] hugeTreePointsLODA;
	public float[] hugeTreePointsLODB;

	public float[] fernPlantPoints;
	public int fernPlantNumPoints;
	public float[] fernPlantPointsLODA;
	public float[] fernPlantPointsLODB;

	public float[] weedPlantPoints;
	public int weedPlantNumPoints;
	public float[] weedPlantPointsLODA;
	public float[] weedPlantPointsLODB;

	public float[] rockAPoints;
	public int rockANumPoints;
	public float[] rockAPointsLODA;
	public float[] rockAPointsLODB;

	public float[] rockBPoints;
	public int rockBNumPoints;
	public float[] rockBPointsLODA;
	public float[] rockBPointsLODB;


	public int[] pineTreeNumInstances = {0,0};
	public int[] hugeTreeNumInstances = {0,0};
	public int[] fernPlantNumInstances = {0,0};
	public int[] weedPlantNumInstances = {0,0};
	public int[] rockANumInstances = {0,0};
	public int[] rockBNumInstances = {0,0};

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

		////pineTreeMatrices = new float[4096];
		pineTreePoints = new float[384];
		pineTreePointsLODA = new float[1024];
		pineTreePointsLODB = new float[1024];
		/*pineTreeMatricesLODA = new float[MAX_TREE_INSTANCES * 16];
		pineTreeMatricesLODB = new float[MAX_TREE_INSTANCES * 16];
		pineTreeIndicesLODA = new float[MAX_TREE_INSTANCES * 16];
		pineTreeIndicesLODB = new float[MAX_TREE_INSTANCES * 16];*/

		hugeTreePoints = new float[384];
		hugeTreePointsLODA = new float[1024];
		hugeTreePointsLODB = new float[1024];

		fernPlantPoints = new float[384];
		fernPlantPointsLODA = new float[1024];
		fernPlantPointsLODB = new float[1024];

		weedPlantPoints = new float[384];
		weedPlantPointsLODA = new float[1024];
		weedPlantPointsLODB = new float[1024];

		rockAPoints = new float[384];
		rockAPointsLODA = new float[1024];
		rockAPointsLODB = new float[1024];

		rockBPoints = new float[384];
		rockBPointsLODA = new float[1024];
		rockBPointsLODB = new float[1024];


		/*pineTreeMatrices = new float[MAX_TREE_INSTANCES * 16];
		pineTreeNumInstances = 0;

		hugeTreeMatrices = new float[MAX_TREE_INSTANCES * 16]*/

		//initialize();
	}


	public void initialize()
	{
		//createTreeUniformBuffer();
		completeStrict();
		spread(spreadFactorX, spreadFactorZ);
		initializeTreePoints();
		filterPoints();
		//updateTreeUniformBuffer();
	}


	public void reinitialize()
	{
		filterPoints();
	}


	private void createTreeUniformBuffer()
	{
		/*for(int i=0; i< Constants.MAX_GRASS_INSTANCES; i++)
		{
			setIdentityM(pineTreeMatrices, i*16);
		}

		pineTreeMatricesBuffer = ByteBuffer
				.allocateDirect(pineTreeMatrices.length * Constants.BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(pineTreeMatrices);
		pineTreeMatricesBuffer.position(0);

		glGenBuffers(1, pineTreeMatricesUbo, 0);
		glBindBuffer(GL_UNIFORM_BUFFER, pineTreeMatricesUbo[0]);
		glBufferData(GL_UNIFORM_BUFFER, pineTreeMatricesBuffer.capacity() * Constants.BYTES_PER_FLOAT, pineTreeMatricesBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);*/
	}


	private void initializeTreePoints()
	{
		for(int i=0; i < 384; i++)
		{
			pineTreePoints[i] = 0f;
			hugeTreePoints[i] = 0f;
			fernPlantPoints[i] = 0f;
			weedPlantPoints[i] = 0f;
			rockAPoints[i] = 0f;
			rockBPoints[i] = 0f;
		}

		for(int i=0; i < 1024; i++)
		{
			pineTreePointsLODA[i] = 0f;
			pineTreePointsLODB[i] = 0f;
			hugeTreePointsLODA[i] = 0f;
			hugeTreePointsLODB[i] = 0f;
			fernPlantPointsLODA[i] = 0f;
			fernPlantPointsLODB[i] = 0f;
			weedPlantPointsLODA[i] = 0f;
			weedPlantPointsLODB[i] = 0f;
			rockAPointsLODA[i] = 0f;
			rockAPointsLODB[i] = 0f;
			rockBPointsLODA[i] = 0f;
			rockBPointsLODB[i] = 0f;
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
		int fernPlantCount = 0;
		int weedPlantCount = 0;
		int rockACount = 0;
		int rockBCount = 0;

		int pineTreeOffset = 0;
		int hugeTreeOffset = 0;
		int fernPlantOffset = 0;
		int weedPlantOffset = 0;
		int rockAOffset = 0;
		int rockBOffset = 0;

		int collisionCylindersOffset = 0;
		int collisionSpheresOffset = 0;

		final int size = points.size();
		float randomValue;
		float scaleValue;
		vec2 currentPoint;

		pineTreeNumPoints = 0;
		hugeTreeNumPoints = 0;
		fernPlantNumPoints = 0;
		weedPlantNumPoints = 0;
		rockANumPoints = 0;
		rockBNumPoints = 0;

		numCollisionCylinders = 0;
		numCollisionSpheres = 0;

		//pineTreePositions.clear();
		//hugeTreePositions.clear();
		//fernPlantPositions.clear();

		for(int i=0; i < size; i++)
		{
			currentPoint = points.get(i);
			randomValue = random.nextFloat();

			//TODO(different objects): if(random.nextFloat() < 0.5)
			if(randomValue < 0.2)
			{
				scaleValue = random.nextFloat() * PINE_TREE_SCALE_DIFFERENCE + PINE_TREE_MIN_SCALE;

				pineTreePoints[pineTreeOffset++] = currentPoint.x;
				pineTreePoints[pineTreeOffset++] = currentPoint.y;
				pineTreePoints[pineTreeOffset++] = scaleValue;

				pineTreeCount++;

				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.z + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 1.0f;// * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 1f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.4)
			{
				scaleValue = random.nextFloat() * HUGE_TREE_SCALE_DIFFERENCE + HUGE_TREE_MIN_SCALE;

				hugeTreePoints[hugeTreeOffset++] = currentPoint.x;
				hugeTreePoints[hugeTreeOffset++] = currentPoint.y;
				hugeTreePoints[hugeTreeOffset++] = scaleValue;

				hugeTreeCount++;

				// TODO: collision cylinders
				collisionCylinders[collisionCylindersOffset++] = currentPosition.x + currentPoint.x;
				collisionCylinders[collisionCylindersOffset++] = currentPosition.z + currentPoint.y;
				collisionCylinders[collisionCylindersOffset++] = 1.0f;// * scaleValue;
				collisionCylinders[collisionCylindersOffset++] = 2f;
				numCollisionCylinders++;
			}
			else if(randomValue < 0.6)
			{
				scaleValue = random.nextFloat() * FERN_PLANT_SCALE_DIFFERENCE + FERN_PLANT_MIN_SCALE;

				fernPlantPoints[fernPlantOffset++] = currentPoint.x;
				fernPlantPoints[fernPlantOffset++] = currentPoint.y;
				fernPlantPoints[fernPlantOffset++] = scaleValue;

				fernPlantCount++;
			}
			else if(randomValue < 0.8)
			{
				scaleValue = random.nextFloat() * WEED_PLANT_SCALE_DIFFERENCE + WEED_PLANT_MIN_SCALE;

				weedPlantPoints[weedPlantOffset++] = currentPoint.x;
				weedPlantPoints[weedPlantOffset++] = currentPoint.y;
				weedPlantPoints[weedPlantOffset++] = scaleValue;

				weedPlantCount++;
			}
			else if(randomValue < 0.9)
			{
				scaleValue = random.nextFloat() * ROCK_A_SCALE_DIFFERENCE + ROCK_A_MIN_SCALE;

				rockAPoints[rockAOffset++] = currentPoint.x;
				rockAPoints[rockAOffset++] = currentPoint.y;
				rockAPoints[rockAOffset++] = scaleValue;

				//TODO: collision spheres
				collisionSpheres[collisionSpheresOffset++] = currentPosition.x + currentPoint.x + (0.51f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.y + (4.261f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = currentPosition.z + currentPoint.y + (0.903f * scaleValue);
				collisionSpheres[collisionSpheresOffset++] = 12f * scaleValue;//13.766f * scaleValue;
				numCollisionSpheres++;

				rockACount++;
			}
			else if(randomValue < 1.1)
			{
				scaleValue = random.nextFloat() * ROCK_B_SCALE_DIFFERENCE + ROCK_B_MIN_SCALE;

				rockBPoints[rockBOffset++] = currentPoint.x;
				rockBPoints[rockBOffset++] = currentPoint.y;
				rockBPoints[rockBOffset++] = scaleValue;

				rockBCount++;
			}
		}

		//pineTreeNumInstances = pineTreeCount;
		pineTreeNumPoints = pineTreeCount;
		hugeTreeNumPoints = hugeTreeCount;
		fernPlantNumPoints = fernPlantCount;
		weedPlantNumPoints = weedPlantCount;
		rockANumPoints = rockACount;
		rockBNumPoints = rockBCount;
	}


	private void updateTreeUniformBuffer()
	{
		/*numTreeInstances = Math.min(treePositions.size(), Constants.MAX_TREE_INSTANCES);
		int numBytes = numTreeInstances * 16;

		for(int i=0; i < numTreeInstances; i++)
		{
			setIdentityM(treeMatrices, i*16);
			translateM(treeMatrices, i*16, treePositions.get(i).x * spreadFactorX, 0f, treePositions.get(i).y * spreadFactorZ);
			rotateM(treeMatrices, i*16, random.nextFloat() * 180f, 0f, 1f, 0f);
			scaleM(treeMatrices, i*16, 1f,random.nextFloat() * 0.5f + 1.0f, 1f);
		}

		treeMatricesBuffer.position(0);
		treeMatricesBuffer.put(treeMatrices, 0, numBytes);
		treeMatricesBuffer.position(0);

		glBindBuffer(GL_UNIFORM_BUFFER, treeMatricesUbo[0]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, numBytes * Constants.BYTES_PER_FLOAT, treeMatricesBuffer);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);*/
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
					collisionCylinders[collisionCylindersOffset++] = 2.0f;
					collisionCylinders[collisionCylindersOffset++] = 1.0f;

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
				collisionCylinders[collisionCylindersOffset++] = 2.0f;
				collisionCylinders[collisionCylindersOffset++] = 2.0f;

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

				countLODA++;
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

		//

		numCollisionCylinders = collisionCylindersCount;
		numCollisionSpheres = collisionSpheresCount;
	}

	public boolean updateLOD()
	{
		/*LODCounter--;

		if(LODCounter < 0)
		{
			currentLOD = LOD_B;
			LODCounter = 2;
		}
		else if(LODCounter < 2)
		{
			currentLOD = LOD_A;
		}*/
		int previousLOD = currentLOD;

		if(distanceToOrigin(currentPosition) <= 650f)
		{
			currentLOD = LOD_A;
		}
		else
		{
			currentLOD = LOD_B;
		}

		if(currentLOD == previousLOD) return false;
		else return true;
	}
}
