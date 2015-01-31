package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.util.FloatMath;
import android.util.Log;

import static com.josepmtomas.rockgame.Constants.*;

import com.josepmtomas.rockgame.algebra.operations;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.programs.BrokenTreeProgram;
import com.josepmtomas.rockgame.programs.DepthPrePassProgram;
import com.josepmtomas.rockgame.programs.GrassLowProgram;
import com.josepmtomas.rockgame.programs.GrassProgram;
import com.josepmtomas.rockgame.programs.GroundProgram;
import com.josepmtomas.rockgame.programs.GroundSimpleProgram;
import com.josepmtomas.rockgame.programs.ObjectsPatchDebugProgram;
import com.josepmtomas.rockgame.programs.RockLowProgram;
import com.josepmtomas.rockgame.programs.RockProgram;
import com.josepmtomas.rockgame.programs.ShadowPassInstancedProgram;
import com.josepmtomas.rockgame.programs.ShadowPassProgram;
import com.josepmtomas.rockgame.programs.TreeLeavesProgram;
import com.josepmtomas.rockgame.programs.TreeProgram;
import com.josepmtomas.rockgame.programs.TreeReflectionProgram;
import com.josepmtomas.rockgame.programs.TreeShadowPassProgram;
import com.josepmtomas.rockgame.programs.TreeTrunkProgram;
import com.josepmtomas.rockgame.programs.WaterProgram;
import com.josepmtomas.rockgame.util.PerspectiveCamera;
import com.josepmtomas.rockgame.util.TextureHelper;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 06/09/2014.
 */
public class Ground
{
	private final static String TAG = "Ground";

	private static final int POSITION_COMPONENTS = 3;
	private static final int TEXCOORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
	private static final int TANGENT_COMPONENTS = 4;

	private final static int RIVER_MIN_LENGTH = 1;
	private final static int RIVER_MAX_LENGTH = 3;
	private final static int RIVER_LENGTH_DIFFERENCE = RIVER_MAX_LENGTH - RIVER_MIN_LENGTH;
	private final static int RIVER_WAIT = 18;
	private final static float RIVER_APPEAR_CHANCE = 0.9f;

	// Random number generator
	private Random random = new Random();

	// Ground generation state
	private int generatorState = GROUND_PATCH_GROUND;
	private int currentRiverLength;
	private int currentRiverCount;

	// VBO & VAO handles for: positions, texture coordinates, normals, tangents & elements
	private final int[] vboHandles = new int[5];
	private int[] riverEntryVboHandles = new int[5];
	private int[] riverMiddleVboHandles = new int[2];
	private int[] riverMiddleVaoHandle = new int[1];
	private int[] riverExitVboHandles = new int[5];
	private int[] riverExitReflectionVboHandles = new int[2];	// The only difference are position & normals

	// VBO & VAO handle for shadow mapping: no patches
	private final int[] vboShadowHandles = new int[2];
	private final int[] vaoShadowHandle = new int[1];

	// Camera
	private PerspectiveCamera perspectiveCamera;

	// Light Info
	private LightInfo lightInfo;

	// Player rock
	private PlayerRock playerRock;

	// Matrices
	private float[] viewProjection;			// Main render view-projection matrix
	private float[] lightViewProjection;	// Shadow map framebuffer view-projection matrix

	// Patch common attributes
	private float[] patchPositions;
	private float[] patchTexCoords;
	private float[] patchNormals;
	private float[] patchTangents;
	private short[] patchElements;

	// Shadow pass attributes
	private float[] shadowPlanePositions;
	private short[] shadowPlaneElements;

	// Visibility pass attributes
	private float[] patchCullingPoints;

	// Ground patches definition
	private int numGroundPatchesX;
	private int numGroundPatchesZ;
	private int numGroundPolygonsX;
	private int numGroundPolygonsZ;
	private int numGroundVerticesX;
	private int numGroundVerticesZ;
	private float groundPatchWidth;
	private float groundPatchHeight;

	private final float minGroundOffsetX;
	private final float maxGroundOffsetX;
	//private float minOffsetZ;
	private final float maxGroundOffsetZ;

	private int centerLaneIndex;

	private int groundLeftmostIndex;
	private int groundRightmostIndex;
	private int groundLowerIndex;
	private int groundUpperIndex;

	// Objects patches definition
	private int numObjectsPatchesX;
	private int numObjectsPatchesZ;
	private final float objectsPatchWidth;
	private final float objectsPatchHeight;

	private final float minObjectsOffsetX;
	private final float maxObjectsOffsetX;
	private final float maxObjectsOffsetZ;

	private int objectsLeftmostIndex;
	private int objectsRightmostIndex;
	private int objectsLowerIndex;
	private int objectsUpperIndex;

	// Ground patches
	private GroundPatch[][] groundPatches;
	private int[][] groundVaoHandles;
	private int[][] groundDepthPrePassVaoHandles;
	private Float maximumVariance;

	// Objects patches
	private ObjectsPatch[][] objectsPatches;

	// Objects
	private Tree pineTree;
	private Tree hugeTree;
	private Tree palmTree;
	private Tree birchTree;
	private Plant fernPlant;
	private Plant weedPlant;
	private Plant bushPlant;
	private Plant palmPlant;
	private Rock rockA;
	private Rock rockB;
	private Rock rockC;

	// Broken objects
	private boolean drawBrokenTree = false;
	private int brokenTreeRootVaoHandle;
	private int brokenTreeTopVaoHandle;
	private int brokenTreeRootNumElementsToDraw;
	private int brokenTreeTopNumElementsToDraw;
	private int brokenTreeRootShadowVaoHandle;
	private int brokenTreeTopShadowVaoHandle;
	private int brokenTreeRootShadowNumElementsToDraw;
	private int brokenTreeTopShadowNumElementsToDraw;
	private int brokenTreeTexture;
	private BrokenTree brokenPineTree;
	private BrokenTree brokenHugeTree;
	private BrokenTree brokenPalmTree;
	private BrokenTree brokenBirchTree;

	// Broken parameters & matrices
	private float[] brokenTreeInitialRootY = {0f, 0f, 0f, 0f};
	private float[] brokenTreeInitialTopY = {53.871f, 25f, 58.0f, 30.0f};
	private float[] brokenTreeInitialForce = {1f, 1f, 2f, 2f};
	private float brokenTreeDistanceZ = 0f;
	private float brokenTreeForce = 0f;
	private float brokenTreeScale = 0f;
	private float brokenTreeX = 0f;
	private float brokenTreeZ = 0f;
	private float brokenTreeRootY = 0f;
	private float brokenTreeTopY = 0f;
	private float brokenTreeRootRotationAngle = 0f;
	private float brokenTreeTopRotationAngle = 0f;
	private float[] brokenTreeRootModel = new float[16];
	private float[] brokenTreeTopModel = new float[16];
	private float[] brokenTreeRootModelViewProjection = new float[16];
	private float[] brokenTreeTopModelViewProjection = new float[16];

	// Movement
	private vec3 displacement;

	// Grass
	private GrassProgram grassProgram;
	private GrassLowProgram grassLowProgram;
	private GrassPatch grassPatch;
	private final int grassVaoHandle;
	private int grassPatchTexture;

	// River
	private int riverWaitCount = 15;
	private int riverCurrentLength;

	// Textures
	private int pineTreeTexture;
	private int pineTreeReflectionProxyTexture;
	private int hugeTreeTexture;
	private int hugeTreeReflectionProxyTexture;
	private int palmTreeTexture;
	private int palmTreeReflectionProxyTexture;
	private int birchTreeTexture;
	private int birchTreeReflectionProxyTexture;
	private int fernPlantTexture;
	private int fernPlantReflectionProxyTexture;
	private int weedPlantTexture;
	private int weedPlantReflectionProxyTexture;
	private int bushPlantTexture;
	private int bushPlantReflectionProxyTexture;
	private int palmPlantTexture;
	private int palmPlantReflectionProxyTexture;
	private int rockADiffuseTexture;
	private int rockANormalTexture;
	private int rockBDiffuseTexture;
	private int rockBNormalTexture;
	private int rockCDiffuseTexture;
	private int rockCNormalTexture;
	private int[] groundTextures;
	private int[] groundNormalTextures;

	// Shadow map
	private float[] shadowMatrix;
	private int shadowMapSampler;


	// TODO: Shader programs
	DepthPrePassProgram depthPrePassProgram;
	ShadowPassProgram shadowPassProgram;
	ShadowPassInstancedProgram shadowPassInstancedProgram;

	GroundProgram groundProgram;
	GroundSimpleProgram groundSimpleProgram;
	WaterProgram waterProgram;

	TreeProgram treeProgram;
	TreeShadowPassProgram treeShadowPassProgram;
	TreeReflectionProgram treeReflectionProgram;
	TreeTrunkProgram treeTrunkProgram;
	TreeLeavesProgram treeLeavesProgram;

	RockProgram rockProgram;
	RockLowProgram rockLowProgram;

	BrokenTreeProgram brokenTreeProgram;

	// TODO: debug
	ObjectsPatchDebugProgram objectsPatchDebugProgram;

	private int[] waterTextures;

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Objects uniform buffers
	////////////////////////////////////////////////////////////////////////////////////////////////

	private float[] pineTreeArrayLODA;
	private float[] pineTreeArrayLODB;
	private FloatBuffer pineTreeArrayBufferLODA;
	private FloatBuffer pineTreeArrayBufferLODB;
	private final int[] pineTreeArrayUbo;
	private int[] pineTreeNumInstances = {0,0};

	private float[] hugeTreeArrayLODA;
	private float[] hugeTreeArrayLODB;
	private FloatBuffer hugeTreeArrayBufferLODA;
	private FloatBuffer hugeTreeArrayBufferLODB;
	private final int[] hugeTreeArrayUbo;
	private int[] hugeTreeNumInstances = {0,0};

	private float[] palmTreeArrayLODA;
	private float[] palmTreeArrayLODB;
	private FloatBuffer palmTreeArrayBufferLODA;
	private FloatBuffer palmTreeArrayBufferLODB;
	private final int[] palmTreeArrayUbo;
	private int[] palmTreeNumInstances = {0,0};

	private float[] birchTreeArrayLODA;
	private float[] birchTreeArrayLODB;
	private FloatBuffer birchTreeArrayBufferLODA;
	private FloatBuffer birchTreeArrayBufferLODB;
	private final int[] birchTreeArrayUbo;
	private int[] birchTreeNumInstances = {0,0};

	private float[] fernPlantArrayLODA;
	private float[] fernPlantArrayLODB;
	private FloatBuffer fernPlantArrayBufferLODA;
	private FloatBuffer fernPlantArrayBufferLODB;
	private final int[] fernPlantArrayUbo;
	private int[] fernPlantNumInstances = {0,0};

	private float[] weedPlantArrayLODA;
	private float[] weedPlantArrayLODB;
	private FloatBuffer weedPlantArrayBufferLODA;
	private FloatBuffer weedPlantArrayBufferLODB;
	private final int[] weedPlantArrayUbo;
	private int[] weedPlantNumInstances = {0,0};

	private float[] bushPlantArrayLODA;
	private float[] bushPlantArrayLODB;
	private FloatBuffer bushPlantArrayBufferLODA;
	private FloatBuffer bushPlantArrayBufferLODB;
	private final int[] bushPlantArrayUbo;
	private int[] bushPlantNumInstances = {0,0};

	private float[] palmPlantArrayLODA;
	private float[] palmPlantArrayLODB;
	private FloatBuffer palmPlantArrayBufferLODA;
	private FloatBuffer palmPlantArrayBufferLODB;
	private final int[] palmPlantArrayUbo;
	private int[] palmPlantNumInstances = {0,0};

	private float[] rockAArrayLODA;
	private float[] rockAArrayLODB;
	private FloatBuffer rockAArrayBufferLODA;
	private FloatBuffer rockAArrayBufferLODB;
	private final int[] rockAArrayUbo;
	private int[] rockANumInstances = {0,0};

	private float[] rockBArrayLODA;
	private float[] rockBArrayLODB;
	private FloatBuffer rockBArrayBufferLODA;
	private FloatBuffer rockBArrayBufferLODB;
	private final int[] rockBArrayUbo;
	private int[] rockBNumInstances = {0,0};

	private float[] rockCArrayLODA;
	private float[] rockCArrayLODB;
	private FloatBuffer rockCArrayBufferLODA;
	private FloatBuffer rockCArrayBufferLODB;
	private final int[] rockCArrayUbo;
	private int[] rockCNumInstances = {0,0};

	// Grass
	private int[] grassUbo;
	private float[] grassArrayLODA = new float[4096];
	private float[] grassArrayLODB = new float[4096];
	private float[] grassArrayLODC = new float[4096];
	private FloatBuffer grassBufferLODA;
	private FloatBuffer grassBufferLODB;
	private FloatBuffer grassBufferLODC;
	private int[] grassNumInstances = {0,0,0};

	// Tree culling //TODO
	private float[] pineTreeCullingPoints;

	// Debug Plane
	public int[] debugPlaneVboHandles = new int[2];
	public int[] debugPlaneVaoHandle = new int[1];


	/**********************************************************************************************/

	public Ground(Context context,
				  int numGroundPatchesX, int numGroundPatchesZ, int numGroundPolygonsX, int numGroundPolygonsZ, float groundPatchWidth, float groundPatchHeight,
				  int numObjectsPatchesX, int numObjectsPatchesZ, float objectsPatchWidth, float objectsPatchHeight, PerspectiveCamera perspectiveCamera, LightInfo lightInfo)
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// Light Info
		////////////////////////////////////////////////////////////////////////////////////////////

		this.lightInfo = lightInfo;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Camera
		////////////////////////////////////////////////////////////////////////////////////////////

		this.perspectiveCamera = perspectiveCamera;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Ground patches attributes
		////////////////////////////////////////////////////////////////////////////////////////////

		this.numGroundPatchesX = numGroundPatchesX;
		this.numGroundPatchesZ = numGroundPatchesZ;
		this.numGroundPolygonsX = numGroundPolygonsX;
		this.numGroundPolygonsZ = numGroundPolygonsZ;
		this.numGroundVerticesX = numGroundPolygonsX + 1;
		this.numGroundVerticesZ = numGroundPolygonsZ + 1;
		this.groundPatchWidth = groundPatchWidth;
		this.groundPatchHeight = groundPatchHeight;

		this.maximumVariance = 0.15f;

		centerLaneIndex = (numGroundPatchesX-1)/2;

		groundLeftmostIndex = 0;
		groundRightmostIndex = numGroundPatchesX-1;
		groundUpperIndex = numGroundPatchesZ-1;
		groundLowerIndex = 0;

		minGroundOffsetX = ((float)centerLaneIndex + 0.5f) * -groundPatchWidth;
		maxGroundOffsetX = ((float)centerLaneIndex + 0.5f) * groundPatchWidth;
		//minOffsetZ = ((float)centerLaneIndex + 0.5f) * -patchHeight;
		//maxOffsetZ = ((float)groundUpperIndex/2.0f + 0.5f) * patchHeight;
		maxGroundOffsetZ = groundPatchHeight;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Objects patches attributes
		////////////////////////////////////////////////////////////////////////////////////////////

		this.numObjectsPatchesX = numObjectsPatchesX;
		this.numObjectsPatchesZ = numObjectsPatchesZ;
		this.objectsPatchWidth = objectsPatchWidth;
		this.objectsPatchHeight = objectsPatchHeight;

		this.minObjectsOffsetX = ((numObjectsPatchesX-1)/2 + 0.5f) * -objectsPatchWidth;
		this.maxObjectsOffsetX = -minObjectsOffsetX;
		this.maxObjectsOffsetZ = objectsPatchHeight;

		this.objectsLeftmostIndex = 0;
		this.objectsRightmostIndex = numObjectsPatchesX-1;
		this.objectsUpperIndex = numObjectsPatchesZ-1;
		this.objectsLowerIndex = 0;

		////////////////////////////////////////////////////////////////////////////////////////////
		// Objects
		////////////////////////////////////////////////////////////////////////////////////////////

		grassPatch = new GrassPatch(context);
		grassVaoHandle = grassPatch.getVaoHandle();

		//tree = new Tree(context, "models/test_tree_trunk.vbm", "models/test_tree_leaves.vbm");
		String[] pineTreeLODs = {
				"models/pine_tree_lod_a.vbm",
				"models/pine_tree_lod_b.vbm"
		};
		pineTree = new Tree(context, pineTreeLODs);
		pineTree.addShadowGeometry("models/pine_tree_shadow.vbm");
		pineTree.addReflectionGeometry("models/pine_tree_reflection_proxy.vbm");
		pineTree.addCullingPoints("models/pine_tree_culling.vbm");
		pineTreeCullingPoints = pineTree.cullingPoints;

		String[] hugeTreeLODs = {
				"models/huge_tree_lod_a.vbm",
				"models/huge_tree_lod_b.vbm"
		};
		hugeTree = new Tree(context, hugeTreeLODs);
		hugeTree.addShadowGeometry("models/huge_tree_shadow.vbm");
		hugeTree.addReflectionGeometry("models/huge_tree_reflection_proxy.vbm");

		String[] palmTreeLODs = {
				"models/palm_tree_lod_a.vbm",
				"models/palm_tree_lod_b.vbm"
		};
		palmTree = new Tree(context, palmTreeLODs);
		palmTree.addShadowGeometry("models/palm_tree_shadow.vbm");
		palmTree.addReflectionGeometry("models/palm_tree_reflection_proxy.vbm");

		String[] birchTreeLODs = {
				"models/birch_tree_lod_a.vbm",
				"models/birch_tree_lod_b.vbm"
		};
		birchTree = new Tree(context, birchTreeLODs);
		birchTree.addShadowGeometry("models/birch_tree_shadow.vbm");
		birchTree.addReflectionGeometry("models/birch_tree_reflection_proxy.vbm");

		String[] fernPlantLODs = {
				"models/fern_plant_lod_a.vbm",
				"models/fern_plant_lod_b.vbm"
		};
		fernPlant = new Plant(context, fernPlantLODs);
		fernPlant.addShadowGeometry("models/fern_plant_shadow.vbm");
		fernPlant.addReflectionGeometry("models/fern_plant_reflection_proxy.vbm");

		String[] weedPlantLODs = {
				"models/weed_plant_lod_a.vbm",
				"models/weed_plant_lod_b.vbm"
		};
		weedPlant = new Plant(context, weedPlantLODs);
		weedPlant.addShadowGeometry("models/weed_plant_shadow.vbm");
		weedPlant.addReflectionGeometry("models/weed_plant_reflection.vbm");

		String[] bushPlantLODs = {
				"models/bush_plant_lod_a_nfc.vbm",
				"models/bush_plant_lod_b_nfc.vbm"
		};
		bushPlant = new Plant(context, bushPlantLODs);
		bushPlant.addShadowGeometry("models/bush_plant_shadow.vbm");
		bushPlant.addReflectionGeometry("models/bush_plant_reflection.vbm");

		String[] palmPlantLODs = {
				"models/palm_plant_lod_a.vbm",
				"models/palm_plant_lod_b.vbm"
		};
		palmPlant = new Plant(context, palmPlantLODs);
		palmPlant.addShadowGeometry("models/palm_plant_lod_a.vbm");
		palmPlant.addReflectionGeometry("models/palm_plant_reflection.vbm");

		rockA = new Rock(context, "models/rock_a_lod_a_2.vbm", "models/rock_a_lod_b_2.vbm");
		rockA.addShadowGeometry("models/rock_a_lod_b.vbm");
		rockA.addReflectionGeometry("models/rock_a_reflection.vbm");

		rockB = new Rock(context, "models/rock_b_lod_a.vbm", "models/rock_b_lod_b.vbm");
		rockB.addShadowGeometry("models/rock_b_lod_b.vbm");
		rockB.addReflectionGeometry("models/rock_b_reflection.vbm");

		rockC = new Rock(context, "models/rock_c_lod_a.vbm", "models/rock_c_lod_b.vbm");
		rockC.addShadowGeometry("models/rock_c_lod_b.vbm");
		rockC.addReflectionGeometry("models/rock_c_reflection.vbm");

		brokenPineTree = new BrokenTree(context, "models/pine_tree_broken_root.vbm", "models/pine_tree_broken_top.vbm");
		brokenPineTree.addShadowGeometry("models/pine_tree_broken_root_shadow.vbm", "models/pine_tree_broken_top_shadow.vbm");

		brokenHugeTree = new BrokenTree(context, "models/huge_tree_broken_root.vbm", "models/huge_tree_broken_top.vbm");
		brokenHugeTree.addShadowGeometry("models/huge_tree_broken_root_shadow.vbm", "models/huge_tree_broken_top_shadow.vbm");

		brokenPalmTree = new BrokenTree(context, "models/palm_tree_broken_root.vbm", "models/palm_tree_broken_top.vbm");
		brokenPalmTree.addShadowGeometry("models/palm_tree_broken_root_shadow.vbm", "models/palm_tree_broken_top_shadow.vbm");

		brokenBirchTree = new BrokenTree(context, "models/birch_tree_broken_root.vbm", "models/birch_tree_broken_top.vbm");
		brokenBirchTree.addShadowGeometry("models/birch_tree_broken_root_shadow.vbm", "models/birch_tree_broken_top_shadow.vbm");

		////////////////////////////////////////////////////////////////////////////////////////////
		// Shader programs
		////////////////////////////////////////////////////////////////////////////////////////////

		depthPrePassProgram = new DepthPrePassProgram(context);
		shadowPassProgram = new ShadowPassProgram(context);
		shadowPassInstancedProgram = new ShadowPassInstancedProgram(context);
		grassProgram = new GrassProgram(context);
		grassLowProgram = new GrassLowProgram(context);
		groundProgram = new GroundProgram(context);
		groundSimpleProgram = new GroundSimpleProgram(context);
		waterProgram = new WaterProgram(context);
		treeProgram = new TreeProgram(context);
		treeShadowPassProgram = new TreeShadowPassProgram(context);
		treeReflectionProgram = new TreeReflectionProgram(context);
		treeTrunkProgram = new TreeTrunkProgram(context);
		treeLeavesProgram = new TreeLeavesProgram(context);
		rockProgram = new RockProgram(context);
		rockLowProgram = new RockLowProgram(context);
		brokenTreeProgram = new BrokenTreeProgram(context);

		//TODO: debug (delete later)
		objectsPatchDebugProgram = new ObjectsPatchDebugProgram(context);

		String[] grass1Texture = {
				"textures/grass1/grass1_512_mip_0.mp3",
				"textures/grass1/grass1_512_mip_1.mp3",
				"textures/grass1/grass1_512_mip_2.mp3",
				"textures/grass1/grass1_512_mip_3.mp3",
				"textures/grass1/grass1_512_mip_4.mp3",
				"textures/grass1/grass1_512_mip_5.mp3",
				"textures/grass1/grass1_512_mip_6.mp3",
				"textures/grass1/grass1_512_mip_7.mp3",
				"textures/grass1/grass1_512_mip_8.mp3"};

		String[] waterTexture = {
				//"textures/water_ripples/water_ripples_normal_mip_0.mp3",
				"textures/water_ripples/water_ripples_normal_mip_1.mp3",
				"textures/water_ripples/water_ripples_normal_mip_2.mp3",
				"textures/water_ripples/water_ripples_normal_mip_3.mp3",
				"textures/water_ripples/water_ripples_normal_mip_4.mp3",
				"textures/water_ripples/water_ripples_normal_mip_5.mp3",
				"textures/water_ripples/water_ripples_normal_mip_6.mp3",
				"textures/water_ripples/water_ripples_normal_mip_7.mp3",
				"textures/water_ripples/water_ripples_normal_mip_8.mp3"
		};

		waterTextures = new int[1];
		waterTextures[0] = TextureHelper.loadETC2Texture(context, waterTexture, GL_COMPRESSED_RGB8_ETC2, false, true);

		//int compressedTexture = TextureHelper.loadETC2Texture(context,"textures/grass_color_mip_0.pkm", GL_COMPRESSED_RGBA8_ETC2_EAC, false, false);

		// Common attributes arrays (static information)
		patchPositions = new float[numGroundVerticesX * numGroundVerticesZ * POSITION_COMPONENTS];
		patchTexCoords = new float[numGroundVerticesX * numGroundVerticesZ * TEXCOORD_COMPONENTS];
		patchNormals = new float[numGroundVerticesX * numGroundVerticesZ * NORMAL_COMPONENTS];
		patchTangents = new float[numGroundVerticesX * numGroundVerticesZ * TANGENT_COMPONENTS];
		patchElements = new short[numGroundPolygonsX * numGroundPolygonsZ * 6];

		shadowPlanePositions = new float[12];
		shadowPlaneElements = new short[6];

		patchCullingPoints = new float[15];

		/************************************** GROUND PATCHES ************************************/

		//TODO: debug
		this.createDebugPlane();

		// Base patch creation
		this.createGroundBasePatch();
		this.createGroundBaseShadowPlane();
		this.createCullingPoints();

		this.buildBasePatchBuffers();
		this.buildShadowPlaneBuffers();

		// River patches creation
		this.createRiverEntryBasePatch();
		this.createRiverMiddleBasePatch();
		this.createRiverExitBasePatch();
		this.createRiverExitReflectionBasePatch();

		this.createGroundPatches();

		this.createGrassBuffers();

		/************************************** OBJECTS PATCHES ***********************************/

		pineTreeArrayUbo = new int[2];
		hugeTreeArrayUbo = new int[2];
		palmTreeArrayUbo = new int[2];
		birchTreeArrayUbo = new int[2];
		fernPlantArrayUbo = new int[2];
		weedPlantArrayUbo = new int[2];
		bushPlantArrayUbo = new int[2];
		palmPlantArrayUbo = new int[2];
		rockAArrayUbo = new int[2];
		rockBArrayUbo = new int[2];
		rockCArrayUbo = new int[2];

		this.createObjectsPatches();		// Create the patches (class)
		Log.d(TAG, "Created objects patches");
		this.createObjectsBuffers();		// Create the patches buffers and its object buffers
		Log.d(TAG, "Created objects buffers");
		this.updateObjectsPatchesBuffer();	// Update the patches reference positions
		Log.d(TAG, "Updated objects patches buffers");
		Log.d(TAG, "Updated objects buffers");

		/*************************************** TEXTURES *****************************************/

		String[] testGrassTexture = {
				"textures/test_grass/test_grass_mip_0.mp3",
				"textures/test_grass/test_grass_mip_1.mp3",
				"textures/test_grass/test_grass_mip_2.mp3",
				"textures/test_grass/test_grass_mip_3.mp3",
				"textures/test_grass/test_grass_mip_4.mp3",
				"textures/test_grass/test_grass_mip_5.mp3",
				"textures/test_grass/test_grass_mip_6.mp3",
				"textures/test_grass/test_grass_mip_7.mp3"
		};

		String[] grassTextureMips = {
				"textures/grass/diffuse_mip_0.mp3",
				"textures/grass/diffuse_mip_1.mp3",
				"textures/grass/diffuse_mip_2.mp3",
				"textures/grass/diffuse_mip_3.mp3",
				"textures/grass/diffuse_mip_4.mp3",
				"textures/grass/diffuse_mip_5.mp3",
				"textures/grass/diffuse_mip_6.mp3",
				"textures/grass/diffuse_mip_7.mp3"
		};
		//Log.d(TAG, "Loading grass patch texture");
		grassPatchTexture = TextureHelper.loadETC2Texture(context, grassTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		//grassPatchTexture = TextureHelper.loadETC2Texture(context, testGrassTexture, GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2, false, true);

		String[] pineTreeTextureMips = {
				"textures/pine_tree/diffuse_mip_0.mp3",
				"textures/pine_tree/diffuse_mip_1.mp3",
				"textures/pine_tree/diffuse_mip_2.mp3",
				"textures/pine_tree/diffuse_mip_3.mp3",
				"textures/pine_tree/diffuse_mip_4.mp3",
				"textures/pine_tree/diffuse_mip_5.mp3",
				"textures/pine_tree/diffuse_mip_6.mp3",
				"textures/pine_tree/diffuse_mip_7.mp3"
		};
		//Log.d(TAG, "Loading pine branch texture");
		pineTreeTexture = TextureHelper.loadETC2Texture(context, pineTreeTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] pineTreeReflectionProxyTextureMips = {
				"textures/pine_tree/reflection_proxy_mip_0.mp3",
				"textures/pine_tree/reflection_proxy_mip_1.mp3",
				"textures/pine_tree/reflection_proxy_mip_2.mp3",
				"textures/pine_tree/reflection_proxy_mip_3.mp3",
				"textures/pine_tree/reflection_proxy_mip_4.mp3",
				"textures/pine_tree/reflection_proxy_mip_5.mp3",
				"textures/pine_tree/reflection_proxy_mip_6.mp3",
				"textures/pine_tree/reflection_proxy_mip_7.mp3"
		};
		//Log.d(TAG, "Loading pine reflection proxy texture");
		pineTreeReflectionProxyTexture = TextureHelper.loadETC2Texture(context, pineTreeReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] hugeTreeTextureMips = {
				"textures/huge_tree/diffuse_mip_0.mp3",
				"textures/huge_tree/diffuse_mip_1.mp3",
				"textures/huge_tree/diffuse_mip_2.mp3",
				"textures/huge_tree/diffuse_mip_3.mp3",
				"textures/huge_tree/diffuse_mip_4.mp3",
				"textures/huge_tree/diffuse_mip_5.mp3",
				"textures/huge_tree/diffuse_mip_6.mp3",
				"textures/huge_tree/diffuse_mip_7.mp3"
		};
		//Log.d(TAG, "Loading huge tree texture");
		hugeTreeTexture = TextureHelper.loadETC2Texture(context, hugeTreeTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] hugeTreeReflectionProxyTextureMips = {
				"textures/huge_tree/reflection_proxy_mip_0.mp3",
				"textures/huge_tree/reflection_proxy_mip_1.mp3",
				"textures/huge_tree/reflection_proxy_mip_2.mp3",
				"textures/huge_tree/reflection_proxy_mip_3.mp3",
				"textures/huge_tree/reflection_proxy_mip_4.mp3",
				"textures/huge_tree/reflection_proxy_mip_5.mp3",
				"textures/huge_tree/reflection_proxy_mip_6.mp3",
				"textures/huge_tree/reflection_proxy_mip_7.mp3"
		};
		//Log.d(TAG, "Loading huge tree reflection proxy texture");
		hugeTreeReflectionProxyTexture = TextureHelper.loadETC2Texture(context, hugeTreeReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] palmTreeTextureMips = {
				"textures/palm_tree/diffuse_mip_0.mp3",
				"textures/palm_tree/diffuse_mip_1.mp3",
				"textures/palm_tree/diffuse_mip_2.mp3",
				"textures/palm_tree/diffuse_mip_3.mp3",
				"textures/palm_tree/diffuse_mip_4.mp3",
				"textures/palm_tree/diffuse_mip_5.mp3",
				"textures/palm_tree/diffuse_mip_6.mp3",
				"textures/palm_tree/diffuse_mip_7.mp3"
		};
		//Log.d(TAG, "Loading palm tree texture");
		palmTreeTexture = TextureHelper.loadETC2Texture(context, palmTreeTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] palmTreeReflectionProxyTextureMips = {
				"textures/palm_tree/reflection_proxy_mip_0.mp3",
				"textures/palm_tree/reflection_proxy_mip_1.mp3",
				"textures/palm_tree/reflection_proxy_mip_2.mp3",
				"textures/palm_tree/reflection_proxy_mip_3.mp3",
				"textures/palm_tree/reflection_proxy_mip_4.mp3",
				"textures/palm_tree/reflection_proxy_mip_5.mp3",
				"textures/palm_tree/reflection_proxy_mip_6.mp3",
				"textures/palm_tree/reflection_proxy_mip_7.mp3"
		};
		//Log.d(TAG, "Loading huge tree reflection proxy texture");
		palmTreeReflectionProxyTexture = TextureHelper.loadETC2Texture(context, palmTreeReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] birchTreeTextureMips = {
				"textures/birch_tree/diffuse_mip_0.mp3",
				"textures/birch_tree/diffuse_mip_1.mp3",
				"textures/birch_tree/diffuse_mip_2.mp3",
				"textures/birch_tree/diffuse_mip_3.mp3",
				"textures/birch_tree/diffuse_mip_4.mp3",
				"textures/birch_tree/diffuse_mip_5.mp3",
				"textures/birch_tree/diffuse_mip_6.mp3",
				"textures/birch_tree/diffuse_mip_7.mp3"
		};
		birchTreeTexture = TextureHelper.loadETC2Texture(context, "textures/birch_tree/diffuse_mip_0.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] birchTreeReflectionProxyTextureMips = {
				"textures/birch_tree/reflection_proxy_mip_0.mp3",
				"textures/birch_tree/reflection_proxy_mip_1.mp3",
				"textures/birch_tree/reflection_proxy_mip_2.mp3",
				"textures/birch_tree/reflection_proxy_mip_3.mp3",
				"textures/birch_tree/reflection_proxy_mip_4.mp3",
				"textures/birch_tree/reflection_proxy_mip_5.mp3",
				"textures/birch_tree/reflection_proxy_mip_6.mp3",
				"textures/birch_tree/reflection_proxy_mip_7.mp3"
		};
		birchTreeReflectionProxyTexture = TextureHelper.loadETC2Texture(context, birchTreeReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] fernPlantTextureMips = {
				"textures/fern_plant/fern_plant_mip_0.mp3",
				"textures/fern_plant/fern_plant_mip_1.mp3",
				"textures/fern_plant/fern_plant_mip_2.mp3",
				"textures/fern_plant/fern_plant_mip_3.mp3",
				"textures/fern_plant/fern_plant_mip_4.mp3",
				"textures/fern_plant/fern_plant_mip_5.mp3",
				"textures/fern_plant/fern_plant_mip_6.mp3",
				"textures/fern_plant/fern_plant_mip_7.mp3"
		};
		//Log.d(TAG, "Loading fern plant texture");
		fernPlantTexture = TextureHelper.loadETC2Texture(context, fernPlantTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] fernPlantReflectionProxyTextureMips = {
				"textures/fern_plant/reflection_proxy_mip_0.mp3",
				"textures/fern_plant/reflection_proxy_mip_1.mp3",
				"textures/fern_plant/reflection_proxy_mip_2.mp3",
				"textures/fern_plant/reflection_proxy_mip_3.mp3",
				"textures/fern_plant/reflection_proxy_mip_4.mp3",
				"textures/fern_plant/reflection_proxy_mip_5.mp3",
				"textures/fern_plant/reflection_proxy_mip_6.mp3",
				"textures/fern_plant/reflection_proxy_mip_7.mp3"
		};
		fernPlantReflectionProxyTexture = TextureHelper.loadETC2Texture(context, fernPlantReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] weedPlantTextureMips = {
				"textures/weed_plant/diffuse_mip_0.mp3",
				"textures/weed_plant/diffuse_mip_1.mp3",
				"textures/weed_plant/diffuse_mip_2.mp3",
				"textures/weed_plant/diffuse_mip_3.mp3",
				"textures/weed_plant/diffuse_mip_4.mp3",
				"textures/weed_plant/diffuse_mip_5.mp3",
				"textures/weed_plant/diffuse_mip_6.mp3",
				"textures/weed_plant/diffuse_mip_7.mp3"
		};
		//Log.d(TAG, "Loading weeds plant texture");
		weedPlantTexture = TextureHelper.loadETC2Texture(context, weedPlantTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] weedPlantReflectionProxyTextureMips = {
				"textures/weed_plant/reflection_proxy_mip_0.mp3",
				"textures/weed_plant/reflection_proxy_mip_1.mp3",
				"textures/weed_plant/reflection_proxy_mip_2.mp3",
				"textures/weed_plant/reflection_proxy_mip_3.mp3",
				"textures/weed_plant/reflection_proxy_mip_4.mp3",
				"textures/weed_plant/reflection_proxy_mip_5.mp3",
				"textures/weed_plant/reflection_proxy_mip_6.mp3",
				"textures/weed_plant/reflection_proxy_mip_7.mp3"
		};
		weedPlantReflectionProxyTexture = TextureHelper.loadETC2Texture(context, weedPlantReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] bushPlantTextureMips = {
				"textures/bush_plant/diffuse_mip_0.mp3",
				"textures/bush_plant/diffuse_mip_1.mp3",
				"textures/bush_plant/diffuse_mip_2.mp3",
				"textures/bush_plant/diffuse_mip_3.mp3",
				"textures/bush_plant/diffuse_mip_4.mp3",
				"textures/bush_plant/diffuse_mip_5.mp3",
				"textures/bush_plant/diffuse_mip_6.mp3",
				"textures/bush_plant/diffuse_mip_7.mp3"
		};
		bushPlantTexture = TextureHelper.loadETC2Texture(context, bushPlantTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] bushPlantReflectionProxyTextureMips = {
				"textures/bush_plant/reflection_proxy_mip_0.mp3",
				"textures/bush_plant/reflection_proxy_mip_1.mp3",
				"textures/bush_plant/reflection_proxy_mip_2.mp3",
				"textures/bush_plant/reflection_proxy_mip_3.mp3",
				"textures/bush_plant/reflection_proxy_mip_4.mp3",
				"textures/bush_plant/reflection_proxy_mip_5.mp3",
				"textures/bush_plant/reflection_proxy_mip_6.mp3",
				"textures/bush_plant/reflection_proxy_mip_7.mp3"
		};
		bushPlantReflectionProxyTexture = TextureHelper.loadETC2Texture(context, bushPlantReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] palmPlantTextureMips = {
				"textures/palm_plant/diffuse_mip_0.mp3",
				"textures/palm_plant/diffuse_mip_1.mp3",
				"textures/palm_plant/diffuse_mip_2.mp3",
				"textures/palm_plant/diffuse_mip_3.mp3",
				"textures/palm_plant/diffuse_mip_4.mp3",
				"textures/palm_plant/diffuse_mip_5.mp3",
				"textures/palm_plant/diffuse_mip_6.mp3",
				"textures/palm_plant/diffuse_mip_7.mp3"
		};
		palmPlantTexture = TextureHelper.loadETC2Texture(context, palmPlantTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] palmPlantReflectionProxyTextureMips = {
				"textures/palm_plant/reflection_proxy_mip_0.mp3",
				"textures/palm_plant/reflection_proxy_mip_1.mp3",
				"textures/palm_plant/reflection_proxy_mip_2.mp3",
				"textures/palm_plant/reflection_proxy_mip_3.mp3",
				"textures/palm_plant/reflection_proxy_mip_4.mp3",
				"textures/palm_plant/reflection_proxy_mip_5.mp3",
				"textures/palm_plant/reflection_proxy_mip_6.mp3",
				"textures/palm_plant/reflection_proxy_mip_7.mp3"
		};
		palmPlantReflectionProxyTexture = TextureHelper.loadETC2Texture(context, palmPlantReflectionProxyTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		String[] rockADiffuseTextureMips = {
				//"textures/rock_a/diffuse_mip_0.mp3",
				"textures/rock_a/diffuse_mip_1.mp3",
				"textures/rock_a/diffuse_mip_2.mp3",
				"textures/rock_a/diffuse_mip_3.mp3",
				"textures/rock_a/diffuse_mip_4.mp3",
				"textures/rock_a/diffuse_mip_5.mp3",
				"textures/rock_a/diffuse_mip_6.mp3",
				"textures/rock_a/diffuse_mip_7.mp3",
				"textures/rock_a/diffuse_mip_8.mp3"
		};
		//Log.d(TAG, "Loading rock a diffuse texture");
		rockADiffuseTexture = TextureHelper.loadETC2Texture(context, rockADiffuseTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] rockANormalTextureMips = {
				//"textures/rock_a/normal_mip_0.mp3",
				"textures/rock_a/normal_mip_1.mp3",
				"textures/rock_a/normal_mip_2.mp3",
				"textures/rock_a/normal_mip_3.mp3",
				"textures/rock_a/normal_mip_4.mp3",
				"textures/rock_a/normal_mip_5.mp3",
				"textures/rock_a/normal_mip_6.mp3",
				"textures/rock_a/normal_mip_7.mp3",
				"textures/rock_a/normal_mip_8.mp3"
		};
		//Log.d(TAG, "Loading rock a normal texture");
		rockANormalTexture = TextureHelper.loadETC2Texture(context, rockANormalTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] rockBDiffuseTextureMips = {
				//"textures/rock_b/diffuse_mip_0.mp3",
				"textures/rock_b/diffuse_mip_1.mp3",
				"textures/rock_b/diffuse_mip_2.mp3",
				"textures/rock_b/diffuse_mip_3.mp3",
				"textures/rock_b/diffuse_mip_4.mp3",
				"textures/rock_b/diffuse_mip_5.mp3",
				"textures/rock_b/diffuse_mip_6.mp3",
				"textures/rock_b/diffuse_mip_7.mp3",
				"textures/rock_b/diffuse_mip_8.mp3",
		};
		Log.d(TAG, "Loading rock b diffuse texture");
		rockBDiffuseTexture = TextureHelper.loadETC2Texture(context, rockBDiffuseTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] rockBNormalTextureMips = {
				//"textures/rock_b/normal_mip_0.mp3",
				"textures/rock_b/normal_mip_1.mp3",
				"textures/rock_b/normal_mip_2.mp3",
				"textures/rock_b/normal_mip_3.mp3",
				"textures/rock_b/normal_mip_4.mp3",
				"textures/rock_b/normal_mip_5.mp3",
				"textures/rock_b/normal_mip_6.mp3",
				"textures/rock_b/normal_mip_7.mp3",
				"textures/rock_b/normal_mip_8.mp3",
		};
		//Log.d(TAG, "Loading rock b normal texture");
		rockBNormalTexture = TextureHelper.loadETC2Texture(context, rockBNormalTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] rockCDiffuseTextureMips = {
				//"textures/rock_c/diffuse_mip_0.mp3",
				"textures/rock_c/diffuse_mip_1.mp3",
				"textures/rock_c/diffuse_mip_2.mp3",
				"textures/rock_c/diffuse_mip_3.mp3",
				"textures/rock_c/diffuse_mip_4.mp3",
				"textures/rock_c/diffuse_mip_5.mp3",
				"textures/rock_c/diffuse_mip_6.mp3",
				"textures/rock_c/diffuse_mip_7.mp3",
				"textures/rock_c/diffuse_mip_8.mp3",
		};
		//Log.d(TAG, "Loading rock c diffuse texture");
		rockCDiffuseTexture = TextureHelper.loadETC2Texture(context, rockCDiffuseTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] rockCNormalTextureMips = {
				//"textures/rock_c/normal_mip_0.mp3",
				"textures/rock_c/normal_mip_1.mp3",
				"textures/rock_c/normal_mip_2.mp3",
				"textures/rock_c/normal_mip_3.mp3",
				"textures/rock_c/normal_mip_4.mp3",
				"textures/rock_c/normal_mip_5.mp3",
				"textures/rock_c/normal_mip_6.mp3",
				"textures/rock_c/normal_mip_7.mp3",
				"textures/rock_c/normal_mip_8.mp3",
		};
		//Log.d(TAG, "Loading rock c normal texture");
		rockCNormalTexture = TextureHelper.loadETC2Texture(context, rockCNormalTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] groundBlackTextureMips = {
				//"textures/ground/ground_black_mip_0.mp3",
				"textures/ground/ground_black_mip_1.mp3",
				"textures/ground/ground_black_mip_2.mp3",
				"textures/ground/ground_black_mip_3.mp3",
				"textures/ground/ground_black_mip_4.mp3",
				"textures/ground/ground_black_mip_5.mp3",
				"textures/ground/ground_black_mip_6.mp3",
				"textures/ground/ground_black_mip_7.mp3",
				"textures/ground/ground_black_mip_8.mp3"
		};
		String[] groundWhiteTextureMips = {
				//"textures/ground/ground_white_mip_0.mp3",
				"textures/ground/ground_white_mip_1.mp3",
				"textures/ground/ground_white_mip_2.mp3",
				"textures/ground/ground_white_mip_3.mp3",
				"textures/ground/ground_white_mip_4.mp3",
				"textures/ground/ground_white_mip_5.mp3",
				"textures/ground/ground_white_mip_6.mp3",
				"textures/ground/ground_white_mip_7.mp3",
				"textures/ground/ground_white_mip_8.mp3"
		};
		groundTextures = new int[2];
		groundTextures[0] = TextureHelper.loadETC2Texture(context, groundBlackTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);
		groundTextures[1] = TextureHelper.loadETC2Texture(context, groundWhiteTextureMips, GL_COMPRESSED_RGB8_ETC2, false, true);

		String[] groundBlackNormalTextureMips = {
				//"textures/ground/ground_black_normal_mip_0.mp3",
				"textures/ground/ground_black_normal_mip_1.mp3",
				"textures/ground/ground_black_normal_mip_2.mp3",
				"textures/ground/ground_black_normal_mip_3.mp3",
				"textures/ground/ground_black_normal_mip_4.mp3",
				"textures/ground/ground_black_normal_mip_5.mp3",
				"textures/ground/ground_black_normal_mip_6.mp3",
				"textures/ground/ground_black_normal_mip_7.mp3",
				"textures/ground/ground_black_normal_mip_8.mp3"
		};
		String[] groundWhiteNormalTextureMips = {
				//"textures/ground/ground_white_normal_mip_0.mp3",
				"textures/ground/ground_white_normal_mip_1.mp3",
				"textures/ground/ground_white_normal_mip_2.mp3",
				"textures/ground/ground_white_normal_mip_3.mp3",
				"textures/ground/ground_white_normal_mip_4.mp3",
				"textures/ground/ground_white_normal_mip_5.mp3",
				"textures/ground/ground_white_normal_mip_6.mp3",
				"textures/ground/ground_white_normal_mip_7.mp3",
				"textures/ground/ground_white_normal_mip_8.mp3"
		};
		groundNormalTextures = new int[2];
		groundNormalTextures[0] = TextureHelper.loadETC2Texture(context, groundBlackNormalTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		groundNormalTextures[1] = TextureHelper.loadETC2Texture(context, groundWhiteNormalTextureMips, GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}


	private void createGroundBasePatch()
	{
		int index;
		int indexLeft;
		int indexDown;
		int indexLeftDown;

		int positionsOffset = 0;
		int texCoordsOffset = 0;
		int normalsOffset = 0;
		int tangentsOffset = 0;
		int elementsOffset = 0;

		float currentX;
		float currentZ;
		float minimumX = (-groundPatchWidth / 2.0f);
		float minimumZ = (groundPatchHeight / 2.0f);
		float incrementX = groundPatchWidth / (float)numGroundPolygonsX;
		float incrementZ = groundPatchWidth / (float)numGroundPolygonsZ;

		// Create the vertex attributes
		for(int i=0; i < numGroundVerticesZ; i++)
		{
			currentZ = minimumZ - (i * incrementZ);
			for(int j=0; j < numGroundVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				patchPositions[positionsOffset++] = currentX;
				patchPositions[positionsOffset++] = 0.0f;
				patchPositions[positionsOffset++] = currentZ;

				// Vertex texture coordinate (U V)
				// Vertex texture coordinate (U V)
				patchTexCoords[texCoordsOffset++] = (float)j / (float)numGroundPolygonsX;
				patchTexCoords[texCoordsOffset++] = (float)i / (float)numGroundPolygonsZ;

				// Vertex normal (X Y Z)
				patchNormals[normalsOffset++] = 0.0f;
				patchNormals[normalsOffset++] = 1.0f;
				patchNormals[normalsOffset++] = 0.0f;

				// Vertex tangent (X Y Z W)
				patchTangents[tangentsOffset++] = 1.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 1.0f;
			}
		}

		// Create patch elements
		for(int i=1; i < numGroundVerticesZ; i++)
		{
			for (int j=1; j < numGroundVerticesX; j++)
			{
				index = j + (i * numGroundVerticesX);
				indexLeft = (j-1) + (i * numGroundVerticesX);
				indexDown = j + ((i-1) * numGroundVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numGroundVerticesX);

				// First face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeftDown;
				patchElements[elementsOffset++] = (short)indexDown;

				// Second face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeft;
				patchElements[elementsOffset++] = (short)indexLeftDown;
			}
		}
	}


	/**
	 * Creates the geometry information for the river entry patch (except for the vertex colors)
	 */
	private void createRiverEntryBasePatch()
	{
		final int numArcVertices = 6;
		final int numArcPolygons = numArcVertices - 1;
		final float arcIncrement = ((float)Math.PI * 0.5f) / numArcPolygons;
		final float texCoordIncrement = 0.5f / numArcPolygons;
		final float yLength = groundPatchHeight * 0.5f;
		final float zLength = groundPatchHeight * -0.5f;

		int index;
		int indexLeft;
		int indexDown;
		int indexLeftDown;

		int positionsOffset = 0;
		int texCoordsOffset = 0;
		int normalsOffset = 0;
		int tangentsOffset = 0;
		int elementsOffset = 0;

		float currentX;
		float currentY;
		float currentZ;
		float minimumX = (-groundPatchWidth / 2.0f);
		float minimumZ = (groundPatchHeight / 2.0f);
		float incrementX = groundPatchWidth / (float)numGroundPolygonsX;
		float incrementZ = groundPatchWidth / (float)numGroundPolygonsZ;

		// Create the vertex attributes
		for(int i=0; i < GROUND_RIVER_PATCH_VERTICES_Z; i++)
		{
			currentY = FloatMath.cos(i * arcIncrement);
			currentZ = FloatMath.sin(i * arcIncrement) * zLength;
			for(int j=0; j < numGroundVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				patchPositions[positionsOffset++] = currentX;
				patchPositions[positionsOffset++] = (currentY - 1.0f) * yLength;
				patchPositions[positionsOffset++] = currentZ+(groundPatchHeight*0.5f);

				// Vertex texture coordinate (U V)
				patchTexCoords[texCoordsOffset++] = (float)j / (float)numGroundPolygonsX;
				patchTexCoords[texCoordsOffset++] = (float)i * texCoordIncrement;

				// Vertex normal (X Y Z)
				patchNormals[normalsOffset++] = 0.0f;
				patchNormals[normalsOffset++] = currentY;
				patchNormals[normalsOffset++] = currentZ / zLength;

				// Vertex tangent (X Y Z W)
				patchTangents[tangentsOffset++] = 1.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 0.0f;
				patchTangents[tangentsOffset++] = 1.0f;
			}
		}

		// Create patch elements
		for(int i=1; i < GROUND_RIVER_PATCH_VERTICES_Z; i++)
		{
			for (int j=1; j < numGroundVerticesX; j++)
			{
				index = j + (i * numGroundVerticesX);
				indexLeft = (j-1) + (i * numGroundVerticesX);
				indexDown = j + ((i-1) * numGroundVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numGroundVerticesX);

				// First face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeftDown;
				patchElements[elementsOffset++] = (short)indexDown;

				// Second face
				patchElements[elementsOffset++] = (short)index;
				patchElements[elementsOffset++] = (short)indexLeft;
				patchElements[elementsOffset++] = (short)indexLeftDown;
			}
		}

		FloatBuffer positionsBuffer;
		FloatBuffer texCoordsBuffer;
		FloatBuffer normalsBuffer;
		FloatBuffer tangentsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(patchPositions.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchPositions);
		positionsBuffer.position(0);

		texCoordsBuffer = ByteBuffer
				.allocateDirect(patchTexCoords.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTexCoords);
		texCoordsBuffer.position(0);

		normalsBuffer = ByteBuffer
				.allocateDirect(patchNormals.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchNormals);
		normalsBuffer.position(0);

		tangentsBuffer = ByteBuffer
				.allocateDirect(patchTangents.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTangents);
		tangentsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(patchElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(patchElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(5, riverEntryVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[1]);
		glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * BYTES_PER_FLOAT, texCoordsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[2]);
		glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverEntryVboHandles[3]);
		glBufferData(GL_ARRAY_BUFFER, tangentsBuffer.capacity() * BYTES_PER_FLOAT, tangentsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverEntryVboHandles[4]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	private void createRiverMiddleBasePatch()
	{
		final int numVerticesX = 2;
		final int numVerticesZ = 2;
		final int numPolygonsX = numVerticesX - 1;
		final int numPolygonsZ = numVerticesZ - 1;
		int verticesOffset = 0;
		int elementsOffset = 0;
		int index, indexLeft, indexDown, indexLeftDown;

		int positionByteOffset = 0;
		int texCoordByteOffset = positionByteOffset + (POSITION_COMPONENTS * BYTES_PER_FLOAT);
		int normalByteOffset = texCoordByteOffset + (TEXCOORD_COMPONENTS * BYTES_PER_FLOAT);
		int tangentByteOffset = normalByteOffset + (NORMAL_COMPONENTS * BYTES_PER_FLOAT);
		int byteStride = (POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS) * BYTES_PER_FLOAT;

		final float minimumX = groundPatchWidth * -0.5f;
		final float minimumZ = groundPatchHeight * 0.5f;
		final float y = -10.0f;

		float[] vertices = new float[12 * numVerticesX * numVerticesZ];
		short[] elements = new short[numPolygonsX * numPolygonsZ * 6];

		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;


		////////////////////////////////////////////////////////////////////////////////////////////
		// Create vertex information
		////////////////////////////////////////////////////////////////////////////////////////////

		for(int z=0; z < numVerticesZ; z++)
		{
			for(int x=0; x < numVerticesX; x++)
			{
				// Positions
				vertices[verticesOffset++] = minimumX + ((float)x * groundPatchWidth);
				vertices[verticesOffset++] = y;
				vertices[verticesOffset++] = minimumZ - ((float)z * groundPatchHeight);
				// Texture coordinates
				vertices[verticesOffset++] = (float)x / (float)numPolygonsX;
				vertices[verticesOffset++] = (float)z / (float)numPolygonsZ;
				// Normals
				vertices[verticesOffset++] = 0f;
				vertices[verticesOffset++] = 1f;
				vertices[verticesOffset++] = 0f;
				// Tangents
				vertices[verticesOffset++] = 1f;
				vertices[verticesOffset++] = 0f;
				vertices[verticesOffset++] = 0f;
				vertices[verticesOffset++] = 1f;
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// Create polygons
		////////////////////////////////////////////////////////////////////////////////////////////

		for(int i=1; i < numVerticesZ; i++)
		{
			for (int j=1; j < numVerticesX; j++)
			{
				index = j + (i * numVerticesX);
				indexLeft = (j-1) + (i * numVerticesX);
				indexDown = j + ((i-1) * numVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numVerticesX);

				// First face
				elements[elementsOffset++] = (short)index;
				elements[elementsOffset++] = (short)indexLeftDown;
				elements[elementsOffset++] = (short)indexDown;

				// Second face
				elements[elementsOffset++] = (short)index;
				elements[elementsOffset++] = (short)indexLeft;
				elements[elementsOffset++] = (short)indexLeftDown;
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// Build the client buffers in native memory
		////////////////////////////////////////////////////////////////////////////////////////////

		verticesBuffer = ByteBuffer
				.allocateDirect(vertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		////////////////////////////////////////////////////////////////////////////////////////////
		// Create and populate the buffer objects
		////////////////////////////////////////////////////////////////////////////////////////////

		glGenBuffers(2, riverMiddleVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, riverMiddleVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverMiddleVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		////////////////////////////////////////////////////////////////////////////////////////////
		// Create the Vertex Array Object
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create the VAO
		glGenVertexArrays(1, riverMiddleVaoHandle, 0);
		glBindVertexArray(riverMiddleVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, riverMiddleVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, byteStride, positionByteOffset);

		// Vertex texture coordinates
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, riverMiddleVboHandles[0]);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, byteStride, texCoordByteOffset);

		// Vertex normals
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, riverMiddleVboHandles[0]);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, byteStride, normalByteOffset);

		// Vertex tangents
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, riverMiddleVboHandles[0]);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, byteStride, tangentByteOffset);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverMiddleVboHandles[1]);

		glBindVertexArray(0);
	}


	private void createRiverExitBasePatch()
	{
		final int numArcPolygons = GROUND_RIVER_PATCH_VERTICES_Z - 1;
		final float arcIncrement = ((float)Math.PI * 0.5f) / numArcPolygons;
		final float arcStart = ((float)Math.PI * 1.5f);
		final float texCoordIncrement = 0.5f / numArcPolygons;
		final float yLength = groundPatchHeight * 0.5f;
		final float zLength = groundPatchHeight * -0.5f;

		float[] positions = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * POSITION_COMPONENTS];
		float[] texCoords = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * TEXCOORD_COMPONENTS];
		float[] normals = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * NORMAL_COMPONENTS];
		float[] tangents = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * TANGENT_COMPONENTS];
		short[] elements = new short[numGroundPolygonsX * GROUND_RIVER_PATCH_POLYGONS_Z * 6];

		int index;
		int indexLeft;
		int indexDown;
		int indexLeftDown;

		int positionsOffset = 0;
		int texCoordsOffset = 0;
		int normalsOffset = 0;
		int tangentsOffset = 0;
		int elementsOffset = 0;

		float currentX;
		float currentY;
		float currentZ;
		float minimumX = (-groundPatchWidth / 2.0f);
		float minimumZ = (groundPatchHeight / 2.0f);
		float incrementX = groundPatchWidth / (float)numGroundPolygonsX;
		float incrementZ = groundPatchWidth / (float)numGroundPolygonsZ;

		// Create the vertex attributes
		for(int i=0; i < GROUND_RIVER_PATCH_VERTICES_Z; i++)
		{
			currentY = FloatMath.cos(arcStart + (i * arcIncrement));
			currentZ = FloatMath.sin(arcStart + (i * arcIncrement)) * zLength;
			for(int j=0; j < numGroundVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				positions[positionsOffset++] = currentX;
				positions[positionsOffset++] = (currentY - 1.0f) * yLength;
				positions[positionsOffset++] = currentZ-(groundPatchHeight*0.5f);

				// Vertex texture coordinate (U V)
				texCoords[texCoordsOffset++] = (float)j / (float)numGroundPolygonsX;
				texCoords[texCoordsOffset++] = (float)i * texCoordIncrement;

				// Vertex normal (X Y Z)
				normals[normalsOffset++] = 0.0f;
				normals[normalsOffset++] = currentY;
				normals[normalsOffset++] = currentZ / zLength;

				// Vertex tangent (X Y Z W)
				tangents[tangentsOffset++] = 1.0f;
				tangents[tangentsOffset++] = 0.0f;
				tangents[tangentsOffset++] = 0.0f;
				tangents[tangentsOffset++] = 1.0f;
			}
		}

		// Create patch elements
		for(int i=1; i < GROUND_RIVER_PATCH_VERTICES_Z; i++)
		{
			for (int j=1; j < numGroundVerticesX; j++)
			{
				index = j + (i * numGroundVerticesX);
				indexLeft = (j-1) + (i * numGroundVerticesX);
				indexDown = j + ((i-1) * numGroundVerticesX);
				indexLeftDown = (j-1) + ((i-1) * numGroundVerticesX);

				// First face
				elements[elementsOffset++] = (short)index;
				elements[elementsOffset++] = (short)indexLeftDown;
				elements[elementsOffset++] = (short)indexDown;

				// Second face
				elements[elementsOffset++] = (short)index;
				elements[elementsOffset++] = (short)indexLeft;
				elements[elementsOffset++] = (short)indexLeftDown;
			}
		}

		FloatBuffer positionsBuffer;
		FloatBuffer texCoordsBuffer;
		FloatBuffer normalsBuffer;
		FloatBuffer tangentsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(positions.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(positions);
		positionsBuffer.position(0);

		texCoordsBuffer = ByteBuffer
				.allocateDirect(texCoords.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(texCoords);
		texCoordsBuffer.position(0);

		normalsBuffer = ByteBuffer
				.allocateDirect(normals.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(normals);
		normalsBuffer.position(0);

		tangentsBuffer = ByteBuffer
				.allocateDirect(tangents.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(tangents);
		tangentsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(5, riverExitVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[1]);
		glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * BYTES_PER_FLOAT, texCoordsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[2]);
		glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitVboHandles[3]);
		glBufferData(GL_ARRAY_BUFFER, tangentsBuffer.capacity() * BYTES_PER_FLOAT, tangentsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, riverExitVboHandles[4]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	private void createRiverExitReflectionBasePatch()
	{
		final int numArcPolygons = GROUND_RIVER_PATCH_VERTICES_Z - 1;
		final float arcIncrement = ((float)Math.PI * 0.5f) / numArcPolygons;
		final float arcStart = ((float)Math.PI * 1.5f);
		final float yLength = groundPatchHeight * 0.5f;
		final float zLength = groundPatchHeight * -0.5f;

		float[] positions = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * POSITION_COMPONENTS];
		float[] normals = new float[numGroundVerticesX * GROUND_RIVER_PATCH_VERTICES_Z * NORMAL_COMPONENTS];

		int positionsOffset = 0;
		int texCoordsOffset = 0;
		int normalsOffset = 0;
		int tangentsOffset = 0;
		int elementsOffset = 0;

		float currentX;
		float currentY;
		float currentZ;
		float minimumX = (-groundPatchWidth / 2.0f);
		float minimumZ = (groundPatchHeight / 2.0f);
		float incrementX = groundPatchWidth / (float)numGroundPolygonsX;
		float incrementZ = groundPatchWidth / (float)numGroundPolygonsZ;

		// Create the vertex attributes
		for(int i=0; i < GROUND_RIVER_PATCH_VERTICES_Z; i++)
		{
			currentY = FloatMath.cos(arcStart + (i * arcIncrement));
			currentZ = FloatMath.sin(arcStart + (i * arcIncrement)) * zLength;
			for(int j=0; j < numGroundVerticesX; j++)
			{
				currentX = minimumX + (j * incrementX);

				// Vertex position (X Y Z)
				positions[positionsOffset++] = currentX;
				positions[positionsOffset++] = (-currentY + 0.5f) * yLength;
				positions[positionsOffset++] = currentZ-(groundPatchHeight*0.5f);

				// Vertex normal (X Y Z)
				normals[normalsOffset++] = 0.0f;
				normals[normalsOffset++] = currentY;
				normals[normalsOffset++] = currentZ / zLength;
			}
		}

		FloatBuffer positionsBuffer;
		FloatBuffer normalsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(positions.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(positions);
		positionsBuffer.position(0);

		normalsBuffer = ByteBuffer
				.allocateDirect(normals.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(normals);
		normalsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(2, riverExitReflectionVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitReflectionVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, riverExitReflectionVboHandles[1]);
		glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);
	}


	private void createGroundBaseShadowPlane()
	{
		float planeWidth = numGroundPatchesX * groundPatchWidth;
		float planeHeight = numGroundPatchesZ * groundPatchHeight;

		float bottom = planeHeight / 2.0f;
		float top = -bottom;
		float right = planeWidth / 2.0f;
		float left = -right;
		float height = -0.5f;

		// D - C
		// | \ |
		// A - B

		// A = 0
		shadowPlanePositions[0] = left;
		shadowPlanePositions[1] = height;
		shadowPlanePositions[2] = bottom;

		// B = 1
		shadowPlanePositions[3] = right;
		shadowPlanePositions[4] = height;
		shadowPlanePositions[5] = bottom;

		// C = 2
		shadowPlanePositions[6] = right;
		shadowPlanePositions[7] = height;
		shadowPlanePositions[8] = top;

		// D = 3
		shadowPlanePositions[9] = left;
		shadowPlanePositions[10] = height;
		shadowPlanePositions[11] = top;

		// First polygon
		shadowPlaneElements[0] = 0;
		shadowPlaneElements[1] = 1;
		shadowPlaneElements[2] = 3;

		// Second polygon
		shadowPlaneElements[3] = 1;
		shadowPlaneElements[4] = 2;
		shadowPlaneElements[5] = 3;
	}


	private void createCullingPoints()
	{
		float right = groundPatchWidth / 2.0f;
		float left = -right;
		float bottom = groundPatchHeight / 2.0f;
		float top = -bottom;

		// A
		patchCullingPoints[0] = left;
		patchCullingPoints[1] = 0.0f;
		patchCullingPoints[2] = bottom;

		// B
		patchCullingPoints[3] = right;
		patchCullingPoints[4] = 0.0f;
		patchCullingPoints[5] = bottom;

		// C
		patchCullingPoints[6] = right;
		patchCullingPoints[7] = 0.0f;
		patchCullingPoints[8] = top;

		// D
		patchCullingPoints[9] = left;
		patchCullingPoints[10] = 0.0f;
		patchCullingPoints[11] = top;

		//E
		patchCullingPoints[12] = 0.0f;
		patchCullingPoints[13] = 0.0f;
		patchCullingPoints[14] = top;
	}


	private void buildBasePatchBuffers()
	{
		FloatBuffer positionsBuffer;
		FloatBuffer texCoordsBuffer;
		FloatBuffer normalsBuffer;
		FloatBuffer tangentsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(patchPositions.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchPositions);
		positionsBuffer.position(0);

		texCoordsBuffer = ByteBuffer
				.allocateDirect(patchTexCoords.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTexCoords);
		texCoordsBuffer.position(0);

		normalsBuffer = ByteBuffer
				.allocateDirect(patchNormals.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchNormals);
		normalsBuffer.position(0);

		tangentsBuffer = ByteBuffer
				.allocateDirect(patchTangents.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(patchTangents);
		tangentsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(patchElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(patchElements);
		elementsBuffer.position(0);

		// Create and populate the buffer objects
		glGenBuffers(5, vboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[1]);
		glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * BYTES_PER_FLOAT, texCoordsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[2]);
		glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, vboHandles[3]);
		glBufferData(GL_ARRAY_BUFFER, tangentsBuffer.capacity() * BYTES_PER_FLOAT, tangentsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboHandles[4]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	private void buildShadowPlaneBuffers()
	{
		FloatBuffer positionsBuffer;
		ShortBuffer elementsBuffer;

		// Build the client buffers in native memory
		positionsBuffer = ByteBuffer
				.allocateDirect(shadowPlanePositions.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(shadowPlanePositions);
		positionsBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(shadowPlaneElements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(shadowPlaneElements);
		elementsBuffer.position(0);

		/******************************************************************************************/

		glGenBuffers(2, vboShadowHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboShadowHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT,  positionsBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboShadowHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		/******************************************************************************************/

		glGenVertexArrays(1, vaoShadowHandle, 0);
		glBindVertexArray(vaoShadowHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vboShadowHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboShadowHandles[1]);

		glBindVertexArray(0);
	}


	private void createGroundPatches()
	{
		int index = 0;

		float minimumX = -(float)((numGroundPatchesX-1)/2) * groundPatchWidth;
		float minimumZ = groundPatchHeight;

		// Patches
		groundPatches = new GroundPatch[numGroundPatchesX][numGroundPatchesZ];
		groundVaoHandles = new int[numGroundPatchesX][numGroundPatchesZ];
		groundDepthPrePassVaoHandles = new int[numGroundPatchesX][numGroundPatchesZ];

		// FIRST LANE
		groundPatches[0][0] = new GroundPatch(index++, numGroundVerticesX, numGroundVerticesZ, groundPatchWidth, groundPatchHeight, maximumVariance, vboHandles, patchCullingPoints, perspectiveCamera);
		groundPatches[0][0].setRiverVboHandles(riverEntryVboHandles, riverExitVboHandles, riverExitReflectionVboHandles);
		groundPatches[0][0].initialize(GROUND_PATCH_ROOT, null, null);
		groundPatches[0][0].setCurrentPosition(minimumX, 0.0f, minimumZ);

		for(int z=1; z < numGroundPatchesZ; z++)
		{
			groundPatches[0][z] = new GroundPatch(index++, numGroundVerticesX, numGroundVerticesZ, groundPatchWidth, groundPatchHeight, maximumVariance, vboHandles, patchCullingPoints, perspectiveCamera);
			groundPatches[0][z].setRiverVboHandles(riverEntryVboHandles, riverExitVboHandles, riverExitReflectionVboHandles);
			groundPatches[0][z].initialize(
					GROUND_PATCH_UP,
					groundPatches[0][z-1].getVertexColors(GROUND_PATCH_UP),
					null);
			groundPatches[0][z].setCurrentPosition(minimumX, 0.0f, minimumZ - (float)z*groundPatchHeight);
		}

		// NEXT LANES
		for(int x=1; x < numGroundPatchesX; x++)
		{
			groundPatches[x][0] = new GroundPatch(index++, numGroundVerticesX, numGroundVerticesZ, groundPatchWidth, groundPatchHeight, maximumVariance, vboHandles, patchCullingPoints, perspectiveCamera);
			groundPatches[x][0].setRiverVboHandles(riverEntryVboHandles, riverExitVboHandles, riverExitReflectionVboHandles);
			groundPatches[x][0].initialize(
					GROUND_PATCH_LEFT,
					null,
					groundPatches[x-1][0].getVertexColors(GROUND_PATCH_RIGHT));
			groundPatches[x][0].setCurrentPosition(minimumX + x*groundPatchWidth, 0.0f, minimumZ);

			for(int z=1; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z] = new GroundPatch(index++, numGroundVerticesX, numGroundVerticesZ, groundPatchWidth, groundPatchHeight, maximumVariance, vboHandles, patchCullingPoints, perspectiveCamera);
				groundPatches[x][z].setRiverVboHandles(riverEntryVboHandles, riverExitVboHandles, riverExitReflectionVboHandles);
				groundPatches[x][z].initialize(
						GROUND_PATCH_UP_LEFT,
						groundPatches[x][z-1].getVertexColors(GROUND_PATCH_UP),
						groundPatches[x-1][z].getVertexColors(GROUND_PATCH_RIGHT));
				groundPatches[x][z].setCurrentPosition(minimumX + x*groundPatchWidth, 0.0f, minimumZ - z*groundPatchHeight);
			}
		}

		// Get the vertex array objects created by each patch
		for(int x=0; x < numGroundPatchesX; x++)
		{
			for(int z=0; z < numGroundPatchesZ; z++)
			{
				groundVaoHandles[x][z] = groundPatches[x][z].getGroundVaoHandle();
				groundDepthPrePassVaoHandles[x][z] = groundPatches[x][z].getDepthPrePassVaoHandle();
			}
		}
	}


	public void update(float[] viewProjection, float[] lightViewProjection, vec3 displacement, float[] shadowMatrix, int shadowMapSampler, float deltaTime)
	{
		this.viewProjection = viewProjection;
		this.lightViewProjection = lightViewProjection;
		this.displacement = displacement;
		this.shadowMatrix = shadowMatrix;
		this.shadowMapSampler = shadowMapSampler;


		////////////////////////////////////////////////////////////////////////////////////////////
		// Broken Tree
		////////////////////////////////////////////////////////////////////////////////////////////

		if(drawBrokenTree)
		{
			brokenTreeX += displacement.x;
			brokenTreeZ += displacement.z;
			brokenTreeDistanceZ += displacement.z;

			// Tree falls
			brokenTreeForce = brokenTreeForce - (9.8f * deltaTime);
			brokenTreeTopY += brokenTreeForce;

			brokenTreeRootRotationAngle -= 1.0f * deltaTime;
			brokenTreeTopRotationAngle += playerRock.currentSpeed * 0.25f * deltaTime;

			// root model matrix
			setIdentityM(brokenTreeRootModel, 0);
			translateM(brokenTreeRootModel, 0, brokenTreeX, brokenTreeRootY, brokenTreeZ);
			//rotateM(brokenTreeRootModel, 0, brokenTreeRootRotationAngle, 1f, 0f, 0f);
			scaleM(brokenTreeRootModel, 0, brokenTreeScale, brokenTreeScale, brokenTreeScale);

			multiplyMM(brokenTreeRootModelViewProjection, 0, viewProjection, 0, brokenTreeRootModel, 0);

			// top model matrix
			setIdentityM(brokenTreeTopModel, 0);
			translateM(brokenTreeTopModel, 0, brokenTreeX, brokenTreeTopY, brokenTreeZ);
			rotateM(brokenTreeTopModel, 0, brokenTreeTopRotationAngle, 1f, 0f, 0f);
			scaleM(brokenTreeTopModel, 0, brokenTreeScale, brokenTreeScale, brokenTreeScale);

			multiplyMM(brokenTreeTopModelViewProjection, 0, viewProjection, 0, brokenTreeTopModel, 0);

			if(brokenTreeDistanceZ > 50f)
			{
				drawBrokenTree = false;
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// Objects patches
		////////////////////////////////////////////////////////////////////////////////////////////

		for(int x=0; x < numObjectsPatchesX; x++)
		{
			for(int z=0; z < numObjectsPatchesZ; z++)
			{
				objectsPatches[x][z].update(viewProjection, lightViewProjection, displacement);
			}
		}

		if(objectsPatches[objectsRightmostIndex][objectsLowerIndex].getCurrentPosition().x > maxObjectsOffsetX)
		{
			newLeftObjectsPatch();
			Log.d(TAG, "NEW LEFT OBJECTS PATCH");
		}
		else if(objectsPatches[objectsLeftmostIndex][objectsLowerIndex].getCurrentPosition().x < minObjectsOffsetX)
		{
			newRightObjectsPatch();
			Log.d(TAG, "NEW RIGHT OBJECTS PATCH");
		}

		if(objectsPatches[0][objectsLowerIndex].getCurrentPosition().z > maxObjectsOffsetZ)
		{
			newUpObjectsPatch();
			Log.d(TAG, "NEW UP OBJECTS PATCH");
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// Ground patches
		////////////////////////////////////////////////////////////////////////////////////////////

		for(int x=0; x < numGroundPatchesX; x++)
		{
			for(int z=0; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z].update(viewProjection, displacement);
			}
		}

		// Check horizontal offset
		if(groundPatches[groundRightmostIndex][groundLowerIndex].getCurrentPosition().x > maxGroundOffsetX)
		{
			newLeftGroundPatch();
		}
		else if(groundPatches[groundLeftmostIndex][groundLowerIndex].getCurrentPosition().x < minGroundOffsetX)
		{
			newRightGroundPatch();
		}

		if(groundPatches[0][groundLowerIndex].getCurrentPosition().z > maxGroundOffsetZ)
		{
			newUpGroundPatch();
		}

		updateGrassBuffers();

		////////////////////////////////////////////////////////////////////////////////////////////
		// Objects collisions
		////////////////////////////////////////////////////////////////////////////////////////////

		int position;
		int treeType;
		float posX, posY, posZ, radius, scale, type, distance;

		for(int x=0; x < numObjectsPatchesX; x++)
		{
			for(int z=0; z < numObjectsPatchesZ; z++)
			{
				for(int i=0; i < objectsPatches[x][z].numCollisionCylinders; i++)
				{
					position = i*4;
					posX   = objectsPatches[x][z].collisionCylinders[position];
					posZ   = objectsPatches[x][z].collisionCylinders[position + 1];
					radius = objectsPatches[x][z].collisionCylinders[position + 2];
					type   = objectsPatches[x][z].collisionCylinders[position + 3];

					distance = FloatMath.sqrt((posX*posX) + (posZ*posZ));

					if(distance <= (radius + playerRock.rockRadius))
					{
						if(playerRock.state == PLAYER_ROCK_MOVING)
						{
							treeType = (int)type;
							scale = objectsPatches[x][z].deleteTreeAfterCollision(i);

							brokenTreeX = posX;
							brokenTreeZ = posZ;
							brokenTreeRootY = brokenTreeInitialRootY[treeType];
							brokenTreeTopY = brokenTreeInitialTopY[treeType] * scale;
							brokenTreeRootRotationAngle = 0f;
							brokenTreeTopRotationAngle = 0f;
							brokenTreeDistanceZ = 0f;
							brokenTreeForce = brokenTreeInitialForce[treeType];
							brokenTreeScale = scale;

							if(treeType == 0)
							{
								// Assign main geometry
								brokenTreeRootVaoHandle = brokenPineTree.rootVaoHandle[0];
								brokenTreeTopVaoHandle = brokenPineTree.topVaoHandle[0];
								brokenTreeRootNumElementsToDraw = brokenPineTree.numRootElementsToDraw;
								brokenTreeTopNumElementsToDraw = brokenPineTree.numTopElementsToDraw;

								// Assign shadow geometry
								brokenTreeRootShadowVaoHandle = brokenPineTree.rootShadowVaoHandle[0];
								brokenTreeTopShadowVaoHandle = brokenPineTree.topShadowVaoHandle[0];
								brokenTreeRootShadowNumElementsToDraw = brokenPineTree.numRootShadowElementsToDraw;
								brokenTreeTopShadowNumElementsToDraw = brokenPineTree.numTopShadowElementsToDraw;

								// Assign texture & make the broken tree drawable
								brokenTreeTexture = pineTreeTexture;
								drawBrokenTree = true;
							}
							else if(treeType == 1)
							{
								// Assign main geometry
								brokenTreeRootVaoHandle = brokenHugeTree.rootVaoHandle[0];
								brokenTreeTopVaoHandle = brokenHugeTree.topVaoHandle[0];
								brokenTreeRootNumElementsToDraw = brokenHugeTree.numRootElementsToDraw;
								brokenTreeTopNumElementsToDraw = brokenHugeTree.numTopElementsToDraw;

								// Assign shadow geometry
								brokenTreeRootShadowVaoHandle = brokenHugeTree.rootShadowVaoHandle[0];
								brokenTreeTopShadowVaoHandle = brokenHugeTree.topShadowVaoHandle[0];
								brokenTreeRootShadowNumElementsToDraw = brokenHugeTree.numRootShadowElementsToDraw;
								brokenTreeTopShadowNumElementsToDraw = brokenHugeTree.numTopShadowElementsToDraw;

								// Assign texture & make the broken tree drawable
								brokenTreeTexture = hugeTreeTexture;
								drawBrokenTree = true;
							}
							else if(treeType == 2)
							{
								// Assign main geometry
								brokenTreeRootVaoHandle = brokenPalmTree.rootVaoHandle[0];
								brokenTreeTopVaoHandle = brokenPalmTree.topVaoHandle[0];
								brokenTreeRootNumElementsToDraw = brokenPalmTree.numRootElementsToDraw;
								brokenTreeTopNumElementsToDraw = brokenPalmTree.numTopElementsToDraw;

								//Assign shadow geometry
								brokenTreeRootShadowVaoHandle = brokenPalmTree.rootShadowVaoHandle[0];
								brokenTreeTopShadowVaoHandle = brokenPalmTree.topShadowVaoHandle[0];
								brokenTreeRootShadowNumElementsToDraw = brokenPalmTree.numRootShadowElementsToDraw;
								brokenTreeTopShadowNumElementsToDraw = brokenPalmTree.numTopShadowElementsToDraw;

								// Assign texture & make the broken tree drawable
								brokenTreeTexture = palmTreeTexture;
								drawBrokenTree = true;
							}
							else //if(treeType == 3)
							{
								// Assign main geometry
								brokenTreeRootVaoHandle = brokenBirchTree.rootVaoHandle[0];
								brokenTreeTopVaoHandle = brokenBirchTree.topVaoHandle[0];
								brokenTreeRootNumElementsToDraw = brokenBirchTree.numRootElementsToDraw;
								brokenTreeTopNumElementsToDraw = brokenBirchTree.numTopElementsToDraw;

								//Assign shadow geometry
								brokenTreeRootShadowVaoHandle = brokenBirchTree.rootShadowVaoHandle[0];
								brokenTreeTopShadowVaoHandle = brokenBirchTree.topShadowVaoHandle[0];
								brokenTreeRootShadowNumElementsToDraw = brokenBirchTree.numRootShadowElementsToDraw;
								brokenTreeTopShadowNumElementsToDraw = brokenBirchTree.numTopShadowElementsToDraw;

								// Assign texture & make the broken tree drawable
								brokenTreeTexture = birchTreeTexture;
								drawBrokenTree = true;
							}
						}
						playerRock.hit(0);
						break;
					}
				}

				for(int i=0; i < objectsPatches[x][z].numCollisionSpheres; i++)
				{
					position = i*4;
					posX   = objectsPatches[x][z].collisionSpheres[position];
					posY   = objectsPatches[x][z].collisionSpheres[position + 1];
					posZ   = objectsPatches[x][z].collisionSpheres[position + 2];
					radius = objectsPatches[x][z].collisionSpheres[position + 3];

					distance = FloatMath.sqrt((posX*posX) + (posY*posY) +(posZ*posZ));

					if(distance <= (radius + playerRock.rockRadius))
					{
						playerRock.hit(1);
						break;
					}
				}
			}
		}

		updateObjectsPatchesBuffer();
	}


	public void drawDepthPrePass()
	{
		depthPrePassProgram.useProgram();

		//TODO: camera frustum culling
		for(int i=0; i < numGroundPatchesX; i++)
		{
			for (int j = 0; j < numGroundPatchesZ; j++)
			{
				if(groundPatches[i][j].visible)
				{
					depthPrePassProgram.setUniforms(groundPatches[i][j].getModelViewProjectionMatrix());

					glBindVertexArray(groundVaoHandles[i][j]);
					glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);
				}
			}
		}
	}


	public void drawReflections()
	{
		treeReflectionProgram.useProgram();

		treeReflectionProgram.setCommonUniforms(viewProjection, pineTreeReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(pineTreeArrayUbo[LOD_A]);
		glBindVertexArray(pineTree.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, pineTree.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, pineTreeNumInstances[LOD_A]);


		treeReflectionProgram.setCommonUniforms(viewProjection, hugeTreeReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(hugeTreeArrayUbo[LOD_A]);
		glBindVertexArray(hugeTree.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, hugeTree.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, hugeTreeNumInstances[LOD_A]);


		treeReflectionProgram.setCommonUniforms(viewProjection, palmTreeReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(palmTreeArrayUbo[LOD_A]);
		glBindVertexArray(palmTree.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, palmTree.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, palmTreeNumInstances[LOD_A]);


		treeReflectionProgram.setCommonUniforms(viewProjection, birchTreeReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(birchTreeArrayUbo[LOD_A]);
		glBindVertexArray(birchTree.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, birchTree.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, birchTreeNumInstances[LOD_A]);

		treeReflectionProgram.setCommonUniforms(viewProjection, fernPlantReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(fernPlantArrayUbo[LOD_A]);
		glBindVertexArray(fernPlant.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, fernPlant.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, fernPlantNumInstances[LOD_A]);

		treeReflectionProgram.setCommonUniforms(viewProjection, weedPlantReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(weedPlantArrayUbo[LOD_A]);
		glBindVertexArray(weedPlant.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, weedPlant.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, weedPlantNumInstances[LOD_A]);

		treeReflectionProgram.setCommonUniforms(viewProjection, bushPlantReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(bushPlantArrayUbo[LOD_A]);
		glBindVertexArray(bushPlant.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, bushPlant.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, bushPlantNumInstances[LOD_A]);

		treeReflectionProgram.setCommonUniforms(viewProjection, palmPlantReflectionProxyTexture);
		treeReflectionProgram.setSpecificUniforms(palmPlantArrayUbo[LOD_A]);
		glBindVertexArray(palmPlant.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, palmPlant.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, palmPlantNumInstances[LOD_A]);

		glEnable(GL_CULL_FACE);

		rockLowProgram.useProgram();

		rockLowProgram.setCommonUniforms(viewProjection, rockADiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockAArrayUbo[LOD_A]);
		glBindVertexArray(rockA.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockA.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, rockANumInstances[LOD_A]);

		rockLowProgram.setCommonUniforms(viewProjection, rockBDiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockBArrayUbo[LOD_A]);
		glBindVertexArray(rockB.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockB.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, rockBNumInstances[LOD_A]);

		rockLowProgram.setCommonUniforms(viewProjection, rockCDiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockCArrayUbo[LOD_A]);
		glBindVertexArray(rockC.reflectionVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockC.numReflectionElementsToDraw, GL_UNSIGNED_SHORT, 0, rockCNumInstances[LOD_A]);
	}


	public void drawShadowMap(float[] lightViewProjection)
	{
		shadowPassProgram.useProgram();
		shadowPassProgram.setUniforms(lightViewProjection);

		glBindVertexArray(vaoShadowHandle[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		glDisable(GL_CULL_FACE);

		if(drawBrokenTree)
		{
			shadowPassProgram.useProgram();

			shadowPassProgram.setUniforms(brokenTreeRootModelViewProjection);
			glBindVertexArray(brokenTreeRootShadowVaoHandle);
			glDrawElements(GL_TRIANGLES, brokenTreeRootShadowNumElementsToDraw, GL_UNSIGNED_SHORT, 0);

			shadowPassProgram.setUniforms(brokenTreeTopModelViewProjection);
			glBindVertexArray(brokenTreeTopShadowVaoHandle);
			glDrawElements(GL_TRIANGLES, brokenTreeTopShadowNumElementsToDraw, GL_UNSIGNED_SHORT, 0);
		}

		treeShadowPassProgram.useProgram();


		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, pineTreeArrayUbo[LOD_A]);
		glBindVertexArray(pineTree.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, pineTree.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, pineTreeNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, hugeTreeArrayUbo[LOD_A]);
		glBindVertexArray(hugeTree.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, hugeTree.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, hugeTreeNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, palmTreeArrayUbo[LOD_A]);
		glBindVertexArray(palmTree.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, palmTree.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, palmTreeNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, birchTreeArrayUbo[LOD_A]);
		glBindVertexArray(birchTree.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, birchTree.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, birchTreeNumInstances[LOD_A]);


		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, fernPlantArrayUbo[LOD_A]);
		glBindVertexArray(fernPlant.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, fernPlant.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, fernPlantNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, weedPlantArrayUbo[LOD_A]);
		glBindVertexArray(weedPlant.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, weedPlant.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, weedPlantNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, bushPlantArrayUbo[LOD_A]);
		glBindVertexArray(bushPlant.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, bushPlant.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, bushPlantNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, palmPlantArrayUbo[LOD_A]);
		glBindVertexArray(palmPlant.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, palmPlant.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, palmPlantNumInstances[LOD_A]);


		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, rockAArrayUbo[LOD_A]);
		glBindVertexArray(rockA.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockA.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, rockANumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, rockBArrayUbo[LOD_A]);
		glBindVertexArray(rockB.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockB.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, rockBNumInstances[LOD_A]);

		treeShadowPassProgram.setUniforms(lightInfo.viewProjection, rockCArrayUbo[LOD_A]);
		glBindVertexArray(rockC.shadowVaoHandle[0]);
		glDrawElementsInstanced(GL_TRIANGLES, rockC.numShadowElementsToDraw, GL_UNSIGNED_SHORT, 0, rockCNumInstances[LOD_A]);

		glEnable(GL_CULL_FACE);
	}


	public void draw(int reflectionSampler, float[] dimensions)
	{
		groundProgram.useProgram();
		groundProgram.setCommonUniforms(groundTextures, groundNormalTextures[0], shadowMapSampler, reflectionSampler, waterTextures[0], shadowMatrix, dimensions);

		//TODO: camera frustum culling
		for(int i=0; i < numGroundPatchesX; i++)
		{
			for (int j=0; j < numGroundPatchesZ; j++)
			{
				if(groundPatches[i][j].visible)
				{
					groundProgram.setPatchUniforms(groundPatches[i][j].getModelMatrix(), groundPatches[i][j].getModelViewProjectionMatrix(), groundPatches[i][j].currentLOD);

					switch(groundPatches[i][j].type)
					{
						case GROUND_PATCH_GROUND:
							glBindVertexArray(groundVaoHandles[i][j]);
							glDrawElements(GL_TRIANGLES, patchElements.length, GL_UNSIGNED_SHORT, 0);
							break;
						case GROUND_PATCH_RIVER_ENTRY:
							glBindVertexArray(groundPatches[i][j].riverEntryVaoHandle[0]);
							glDrawElements(GL_TRIANGLES, 300, GL_UNSIGNED_SHORT, 0);
							break;
						case GROUND_PATCH_RIVER_EXIT:
							glBindVertexArray(groundPatches[i][j].riverExitVaoHandle[0]);
							glDrawElements(GL_TRIANGLES, 300, GL_UNSIGNED_SHORT, 0);
							break;
						default:
							break;
					}
				}
			}
		}

		// River water
		waterProgram.useProgram();
		waterProgram.setCommonUniforms(shadowMatrix, dimensions, shadowMapSampler, reflectionSampler, waterTextures[0]);
		glBindVertexArray(riverMiddleVaoHandle[0]);

		for(int i=0; i < numGroundPatchesX; i++)
		{
			for (int j = 0; j < numGroundPatchesZ; j++)
			{
				if (groundPatches[i][j].visible)
				{
					if(groundPatches[i][j].type != GROUND_PATCH_GROUND)
					{
						waterProgram.setSpecificUniforms(groundPatches[i][j].getModelMatrix(), groundPatches[i][j].getModelViewProjectionMatrix());
						glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
					}
				}
			}
		}

		// Grass
		grassProgram.useProgram();
		grassProgram.setCommonUniforms(shadowMatrix, viewProjection, shadowMapSampler, grassPatchTexture);

		glBindVertexArray(grassVaoHandle);

		grassProgram.setSpecificUniforms(grassUbo[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, 18, GL_UNSIGNED_SHORT, 0, grassNumInstances[LOD_A]);

		grassLowProgram.useProgram();
		grassLowProgram.setCommonUniforms(viewProjection, grassPatchTexture);

		grassLowProgram.setSpecificUniforms(grassUbo[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, 12, GL_UNSIGNED_SHORT, 0, grassNumInstances[LOD_B]);

		grassLowProgram.setSpecificUniforms(grassUbo[LOD_C]);
		glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0, grassNumInstances[LOD_C]);


		if(drawBrokenTree)
		{
			brokenTreeProgram.useProgram();
			brokenTreeProgram.setCommonUniforms(brokenTreeTexture);

			brokenTreeProgram.setSpecificUniforms(brokenTreeRootModel, brokenTreeRootModelViewProjection);
			glBindVertexArray(brokenTreeRootVaoHandle);
			glDrawElements(GL_TRIANGLES, brokenTreeRootNumElementsToDraw, GL_UNSIGNED_SHORT, 0);

			brokenTreeProgram.setSpecificUniforms(brokenTreeTopModel, brokenTreeTopModelViewProjection);
			glBindVertexArray(brokenTreeTopVaoHandle);
			glDrawElements(GL_TRIANGLES, brokenTreeTopNumElementsToDraw, GL_UNSIGNED_SHORT, 0);
		}


		treeProgram.useProgram();
		treeProgram.setCommonUniforms(viewProjection, pineTreeTexture);

		treeProgram.setSpecificUniforms(pineTreeArrayUbo[LOD_A]);
		glBindVertexArray(pineTree.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, pineTree.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, pineTreeNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(pineTreeArrayUbo[LOD_B]);
		glBindVertexArray(pineTree.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, pineTree.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, pineTreeNumInstances[LOD_B]);

		////

		treeProgram.setCommonUniforms(viewProjection, palmTreeTexture);
		treeProgram.setSpecificUniforms(palmTreeArrayUbo[LOD_A]);
		glBindVertexArray(palmTree.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, palmTree.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, palmTreeNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(palmTreeArrayUbo[LOD_B]);
		glBindVertexArray(palmTree.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, palmTree.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, palmTreeNumInstances[LOD_B]);

		////

		treeProgram.setCommonUniforms(viewProjection, birchTreeTexture);
		treeProgram.setSpecificUniforms(birchTreeArrayUbo[LOD_A]);
		glBindVertexArray(birchTree.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, birchTree.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, birchTreeNumInstances[LOD_A]);
		treeProgram.setSpecificUniforms(birchTreeArrayUbo[LOD_B]);
		glBindVertexArray(birchTree.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, birchTree.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, birchTreeNumInstances[LOD_B]);

		////

		treeProgram.setCommonUniforms(viewProjection, palmPlantTexture);

		treeProgram.setSpecificUniforms(palmPlantArrayUbo[LOD_A]);
		glBindVertexArray(palmPlant.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, palmPlant.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, palmPlantNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(palmPlantArrayUbo[LOD_B]);
		glBindVertexArray(palmPlant.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, palmPlant.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, palmPlantNumInstances[LOD_B]);

		////

		glEnable(GL_CULL_FACE);

		treeProgram.setCommonUniforms(viewProjection, hugeTreeTexture);

		treeProgram.setSpecificUniforms(hugeTreeArrayUbo[LOD_A]);
		glBindVertexArray(hugeTree.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, hugeTree.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, hugeTreeNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(hugeTreeArrayUbo[LOD_B]);
		glBindVertexArray(hugeTree.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, hugeTree.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, hugeTreeNumInstances[LOD_B]);

		////



		////

		treeProgram.setCommonUniforms(viewProjection, fernPlantTexture);

		treeProgram.setSpecificUniforms(fernPlantArrayUbo[LOD_A]);
		glBindVertexArray(fernPlant.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, fernPlant.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, fernPlantNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(fernPlantArrayUbo[LOD_B]);
		glBindVertexArray(fernPlant.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, fernPlant.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, fernPlantNumInstances[LOD_B]);

		////

		treeProgram.setCommonUniforms(viewProjection, weedPlantTexture);

		treeProgram.setSpecificUniforms(weedPlantArrayUbo[LOD_A]);
		glBindVertexArray(weedPlant.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, weedPlant.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, weedPlantNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(weedPlantArrayUbo[LOD_B]);
		glBindVertexArray(weedPlant.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, weedPlant.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, weedPlantNumInstances[LOD_B]);

		////

		treeProgram.setCommonUniforms(viewProjection, bushPlantTexture);

		treeProgram.setSpecificUniforms(bushPlantArrayUbo[LOD_A]);
		glBindVertexArray(bushPlant.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, bushPlant.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, bushPlantNumInstances[LOD_A]);

		treeProgram.setSpecificUniforms(bushPlantArrayUbo[LOD_B]);
		glBindVertexArray(bushPlant.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, bushPlant.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, bushPlantNumInstances[LOD_B]);


		////


		rockProgram.useProgram();

		rockProgram.setCommonUniforms(viewProjection, rockADiffuseTexture, rockANormalTexture);
		rockProgram.setSpecificUniforms(rockAArrayUbo[LOD_A]);
		glBindVertexArray(rockA.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, rockA.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, rockANumInstances[LOD_A]);

		rockProgram.setCommonUniforms(viewProjection, rockBDiffuseTexture, rockBNormalTexture);
		rockProgram.setSpecificUniforms(rockBArrayUbo[LOD_A]);
		glBindVertexArray(rockB.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, rockB.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, rockBNumInstances[LOD_A]);

		rockProgram.setCommonUniforms(viewProjection, rockCDiffuseTexture, rockCNormalTexture);
		rockProgram.setSpecificUniforms(rockCArrayUbo[LOD_A]);
		glBindVertexArray(rockC.vaoHandles[LOD_A]);
		glDrawElementsInstanced(GL_TRIANGLES, rockC.numElementsToDraw[LOD_A], GL_UNSIGNED_SHORT, 0, rockCNumInstances[LOD_A]);

		rockLowProgram.useProgram();

		rockLowProgram.setCommonUniforms(viewProjection, rockADiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockAArrayUbo[LOD_B]);
		glBindVertexArray(rockA.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, rockA.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, rockANumInstances[LOD_B]);

		rockLowProgram.setCommonUniforms(viewProjection, rockBDiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockBArrayUbo[LOD_B]);
		glBindVertexArray(rockB.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, rockB.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, rockBNumInstances[LOD_B]);

		rockLowProgram.setCommonUniforms(viewProjection, rockCDiffuseTexture);
		rockLowProgram.setSpecificUniforms(rockCArrayUbo[LOD_B]);
		glBindVertexArray(rockC.vaoHandles[LOD_B]);
		glDrawElementsInstanced(GL_TRIANGLES, rockC.numElementsToDraw[LOD_B], GL_UNSIGNED_SHORT, 0, rockCNumInstances[LOD_B]);
	}


	private void newUpGroundPatch()
	{
		int current = groundLeftmostIndex;
		int previous = groundRightmostIndex;

		if(generatorState == GROUND_PATCH_GROUND)
		{
			groundPatches[current][groundLowerIndex].type = GROUND_PATCH_GROUND;
			groundPatches[current][groundLowerIndex].setCurrentPosition(
					operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
			);
			groundPatches[current][groundLowerIndex].reinitialize(
					GROUND_PATCH_UP,
					groundPatches[current][groundUpperIndex].getVertexColors(GROUND_PATCH_UP),
					null
			);

			for(int x=1; x < numGroundPatchesX; x++)
			{
				current = (current + 1) % numGroundPatchesX;
				previous = (previous + 1) % numGroundPatchesX;

				groundPatches[current][groundLowerIndex].type = GROUND_PATCH_GROUND;
				groundPatches[current][groundLowerIndex].setCurrentPosition(
						operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
				);
				groundPatches[current][groundLowerIndex].reinitialize(
						GROUND_PATCH_UP_LEFT,
						groundPatches[current][groundUpperIndex].getVertexColors(GROUND_PATCH_UP),
						groundPatches[previous][groundLowerIndex].getVertexColors(GROUND_PATCH_RIGHT)
				);
			}
			Log.d(TAG, "New(UP) | state = GROUND | riverWait = "+riverWaitCount);

			riverWaitCount = Math.max(0, riverWaitCount-1);
		}
		else if(generatorState == GROUND_PATCH_RIVER_ENTRY)
		{
			groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_ENTRY;
			groundPatches[current][groundLowerIndex].setCurrentPosition(
					operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
			);
			groundPatches[current][groundLowerIndex].reinitialize(
					GROUND_PATCH_UP,
					groundPatches[current][groundUpperIndex].getVertexColors(GROUND_PATCH_UP),
					null
			);

			for(int x=1; x < numGroundPatchesX; x++)
			{
				current = (current + 1) % numGroundPatchesX;
				previous = (previous + 1) % numGroundPatchesX;

				groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_ENTRY;
				groundPatches[current][groundLowerIndex].setCurrentPosition(
						operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
				);
				groundPatches[current][groundLowerIndex].reinitialize(
						GROUND_PATCH_UP_LEFT,
						groundPatches[current][groundUpperIndex].getVertexColors(GROUND_PATCH_UP),
						groundPatches[previous][groundLowerIndex].getVertexColors(GROUND_PATCH_LEFT)
				);
			}
			Log.d(TAG, "New(UP) | state = RIVER_ENTRY | riverWait = "+riverWaitCount);

			generatorState = GROUND_PATCH_RIVER_MIDDLE;
		}
		else if(generatorState == GROUND_PATCH_RIVER_MIDDLE)
		{
			groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_MIDDLE;
			groundPatches[current][groundLowerIndex].setCurrentPosition(
					operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
			);

			for(int x=1; x < numGroundPatchesX; x++)
			{
				current = (current + 1) % numGroundPatchesX;
				previous = (previous + 1) % numGroundPatchesX;

				groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_MIDDLE;
				groundPatches[current][groundLowerIndex].setCurrentPosition(
						operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
				);
			}

			currentRiverCount++;
			Log.d(TAG, "New(UP) | state = RIVER_MIDDLE | riverCount= "+currentRiverCount);

			if(currentRiverCount == currentRiverLength)
			{
				generatorState = GROUND_PATCH_RIVER_EXIT;
				currentRiverCount = 0;
			}
		}
		else if(generatorState == GROUND_PATCH_RIVER_EXIT)
		{
			groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_EXIT;
			groundPatches[current][groundLowerIndex].setCurrentPosition(
					operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
			);
			groundPatches[current][groundLowerIndex].reinitialize(GROUND_PATCH_ROOT, null, null);


			for(int x=1; x < numGroundPatchesX; x++)
			{
				current = (current + 1) % numGroundPatchesX;
				previous = (previous + 1) % numGroundPatchesX;

				groundPatches[current][groundLowerIndex].type = GROUND_PATCH_RIVER_EXIT;
				groundPatches[current][groundLowerIndex].setCurrentPosition(
						operations.subtract(groundPatches[current][groundUpperIndex].getCurrentPosition(), new vec3(0f, 0f, groundPatchHeight))
				);
				groundPatches[current][groundLowerIndex].reinitialize(
						GROUND_PATCH_LEFT,
						null,
						groundPatches[previous][groundLowerIndex].getVertexColors(GROUND_PATCH_RIGHT)
				);
			}
			Log.d(TAG, "New(UP) | state = RIVER_EXIT | riverWait = "+riverWaitCount);

			generatorState = GROUND_PATCH_GROUND;
		}

		groundLowerIndex++;
		groundLowerIndex = groundLowerIndex % numGroundPatchesZ;

		groundUpperIndex++;
		groundUpperIndex = groundUpperIndex % numGroundPatchesZ;
	}


	private void newUpObjectsPatch()
	{
		int current;
		vec3 currentPos;

		if(groundPatches[0][groundUpperIndex].type == GROUND_PATCH_GROUND)
		{
			if(random.nextFloat() < RIVER_APPEAR_CHANCE && riverWaitCount == 0)
			{
				// Change the generator state
				generatorState = GROUND_PATCH_RIVER_ENTRY;

				// Generate the river length
				currentRiverLength = random.nextInt(RIVER_LENGTH_DIFFERENCE) + RIVER_MIN_LENGTH;
				currentRiverCount = 0;
				riverWaitCount = 11 + currentRiverLength;

				// Reposition the new patches
				current = objectsLeftmostIndex;
				currentPos = objectsPatches[current][objectsUpperIndex].getCurrentPosition();
				objectsPatches[current][objectsLowerIndex].setCurrentPosition(currentPos.x, currentPos.y, currentPos.z - (objectsPatchHeight + groundPatchHeight *((float)currentRiverLength + 2f )));
				objectsPatches[current][objectsLowerIndex].reinitialize();

				for(int x=1; x < numObjectsPatchesX; x++)
				{
					current = (current + 1) % numObjectsPatchesX;
					currentPos = objectsPatches[current][objectsUpperIndex].getCurrentPosition();

					objectsPatches[current][objectsLowerIndex].setCurrentPosition(currentPos.x, currentPos.y, currentPos.z - (objectsPatchHeight + groundPatchHeight *((float)currentRiverLength + 2f)));
					objectsPatches[current][objectsLowerIndex].reinitialize();
				}

				objectsLowerIndex++;
				objectsLowerIndex = objectsLowerIndex % numObjectsPatchesZ;

				objectsUpperIndex++;
				objectsUpperIndex = objectsUpperIndex % numObjectsPatchesZ;
			}
			else
			{
				// Reposition the new patches
				current = objectsLeftmostIndex;
				currentPos = objectsPatches[current][objectsUpperIndex].getCurrentPosition();
				objectsPatches[current][objectsLowerIndex].setCurrentPosition(currentPos.x, currentPos.y, currentPos.z - objectsPatchHeight);
				objectsPatches[current][objectsLowerIndex].reinitialize();

				for(int x=1; x < numObjectsPatchesX; x++)
				{
					current = (current + 1) % numObjectsPatchesX;
					currentPos = objectsPatches[current][objectsUpperIndex].getCurrentPosition();

					objectsPatches[current][objectsLowerIndex].setCurrentPosition(currentPos.x, currentPos.y, currentPos.z - objectsPatchHeight);
					objectsPatches[current][objectsLowerIndex].reinitialize();
				}

				objectsLowerIndex++;
				objectsLowerIndex = objectsLowerIndex % numObjectsPatchesZ;

				objectsUpperIndex++;
				objectsUpperIndex = objectsUpperIndex % numObjectsPatchesZ;
			}
		}
	}


	private void newLeftGroundPatch()
	{
		int current = groundLowerIndex;
		int previous = groundUpperIndex;

		groundPatches[groundRightmostIndex][current].type = groundPatches[groundLeftmostIndex][current].type;
		groundPatches[groundRightmostIndex][current].setCurrentPosition(
				operations.subtract(groundPatches[groundLeftmostIndex][current].getCurrentPosition(), new vec3(groundPatchWidth, 0f, 0f))
		);
		groundPatches[groundRightmostIndex][current].reinitialize(
				GROUND_PATCH_RIGHT,
				null,
				groundPatches[groundLeftmostIndex][current].getVertexColors(GROUND_PATCH_LEFT)
		);

		for(int z=1; z < numGroundPatchesZ; z++)
		{
			current = (current + 1) % numGroundPatchesZ;
			previous = (previous + 1) % numGroundPatchesZ;

			groundPatches[groundRightmostIndex][current].type = groundPatches[groundLeftmostIndex][current].type;
			groundPatches[groundRightmostIndex][current].setCurrentPosition(
					operations.subtract(groundPatches[groundLeftmostIndex][current].getCurrentPosition(), new vec3(groundPatchWidth, 0f, 0f))
			);
			groundPatches[groundRightmostIndex][current].reinitialize(
					GROUND_PATCH_UP_RIGHT,
					groundPatches[groundRightmostIndex][previous].getVertexColors(GROUND_PATCH_UP),
					groundPatches[groundLeftmostIndex][current].getVertexColors(GROUND_PATCH_LEFT)
			);
		}

		groundRightmostIndex--;
		if(groundRightmostIndex < 0) groundRightmostIndex = numGroundPatchesX-1;

		groundLeftmostIndex--;
		if(groundLeftmostIndex < 0) groundLeftmostIndex = numGroundPatchesX-1;
	}


	private void newLeftObjectsPatch()
	{
		int current = objectsLowerIndex;
		vec3 currentPos;

		currentPos = objectsPatches[objectsLeftmostIndex][current].getCurrentPosition();
		objectsPatches[objectsRightmostIndex][current].setCurrentPosition(currentPos.x - objectsPatchWidth, currentPos.y, currentPos.z);
		objectsPatches[objectsRightmostIndex][current].reinitialize();

		for(int z=1; z < numObjectsPatchesZ; z++)
		{
			current = (current + 1) % numObjectsPatchesZ;

			currentPos = objectsPatches[objectsLeftmostIndex][current].getCurrentPosition();
			objectsPatches[objectsRightmostIndex][current].setCurrentPosition(currentPos.x - objectsPatchWidth, currentPos.y, currentPos.z);
			objectsPatches[objectsRightmostIndex][current].reinitialize();
		}

		objectsRightmostIndex--;
		if(objectsRightmostIndex < 0) objectsRightmostIndex = numObjectsPatchesX-1;

		objectsLeftmostIndex--;
		if(objectsLeftmostIndex < 0) objectsLeftmostIndex = numObjectsPatchesX-1;
	}


	private void newRightGroundPatch()
	{
		int current = groundLowerIndex;
		int previous = groundUpperIndex;

		groundPatches[groundLeftmostIndex][current].type = groundPatches[groundRightmostIndex][current].type;
		groundPatches[groundLeftmostIndex][current].setCurrentPosition(
				operations.add(groundPatches[groundRightmostIndex][current].getCurrentPosition(), new vec3(groundPatchWidth, 0f, 0f))
		);
		groundPatches[groundLeftmostIndex][current].reinitialize(
				GROUND_PATCH_LEFT,
				null,
				groundPatches[groundRightmostIndex][current].getVertexColors(GROUND_PATCH_RIGHT)
		);

		for(int z=1; z < numGroundPatchesZ; z++)
		{
			current = (current + 1) % numGroundPatchesZ;
			previous = (previous + 1) % numGroundPatchesZ;

			groundPatches[groundLeftmostIndex][current].type = groundPatches[groundRightmostIndex][current].type;
			groundPatches[groundLeftmostIndex][current].setCurrentPosition(
					operations.add(groundPatches[groundRightmostIndex][current].getCurrentPosition(), new vec3(groundPatchWidth, 0f, 0f))
			);
			groundPatches[groundLeftmostIndex][current].reinitialize(
					GROUND_PATCH_UP_LEFT,
					groundPatches[groundLeftmostIndex][previous].getVertexColors(GROUND_PATCH_UP),
					groundPatches[groundRightmostIndex][current].getVertexColors(GROUND_PATCH_RIGHT)
			);
		}

		groundRightmostIndex++;
		groundRightmostIndex = groundRightmostIndex % (numGroundPatchesX);

		groundLeftmostIndex++;
		groundLeftmostIndex = groundLeftmostIndex % (numGroundPatchesX);
	}


	private void newRightObjectsPatch()
	{
		int current = objectsLowerIndex;
		vec3 currentPos;

		currentPos = objectsPatches[objectsRightmostIndex][current].getCurrentPosition();
		objectsPatches[objectsLeftmostIndex][current].setCurrentPosition(currentPos.x + objectsPatchWidth, currentPos.y, currentPos.z);
		objectsPatches[objectsLeftmostIndex][current].reinitialize();

		for(int z=1; z < numObjectsPatchesZ; z++)
		{
			current = (current + 1) % numObjectsPatchesZ;

			currentPos = objectsPatches[objectsRightmostIndex][current].getCurrentPosition();
			objectsPatches[objectsLeftmostIndex][current].setCurrentPosition(currentPos.x + objectsPatchWidth, currentPos.y, currentPos.z);
			objectsPatches[objectsLeftmostIndex][current].reinitialize();
		}

		objectsRightmostIndex++;
		objectsRightmostIndex = objectsRightmostIndex % numObjectsPatchesX;

		objectsLeftmostIndex++;
		objectsLeftmostIndex = objectsLeftmostIndex % numObjectsPatchesX;
	}

	/*************************************** OBJECTS PATCHES **************************************/

	private void createObjectsPatches()
	{
		int index = 0;

		float minimumX = -(float)((numObjectsPatchesX-1)/2) * objectsPatchWidth;
		float minimumZ = 0f;

		// Patches
		objectsPatches = new ObjectsPatch[numObjectsPatchesX][numObjectsPatchesZ];

		// FIRST LANE
		objectsPatches[0][0] = new ObjectsPatch(index++, objectsPatchWidth, objectsPatchHeight, perspectiveCamera);
		objectsPatches[0][0].setCurrentPosition(minimumX, 0.0f, minimumZ);
		objectsPatches[0][0].initialize();
		objectsPatches[0][0].setCullingPoints(pineTreeCullingPoints);

		for(int z=1; z < numObjectsPatchesZ; z++)
		{
			objectsPatches[0][z] = new ObjectsPatch(index++, objectsPatchWidth, objectsPatchHeight, perspectiveCamera);
			objectsPatches[0][z].setCurrentPosition(minimumX, 0.0f, minimumZ - (float)z*objectsPatchHeight);
			objectsPatches[0][z].initialize();
			objectsPatches[0][z].setCullingPoints(pineTreeCullingPoints);
		}

		// NEXT LANES
		for(int x=1; x < numObjectsPatchesX; x++)
		{
			objectsPatches[x][0] = new ObjectsPatch(index++, objectsPatchWidth, objectsPatchHeight, perspectiveCamera);
			objectsPatches[x][0].setCurrentPosition(minimumX + x*objectsPatchWidth, 0.0f, minimumZ);
			objectsPatches[x][0].initialize();
			objectsPatches[x][0].setCullingPoints(pineTreeCullingPoints);

			for(int z=1; z < numObjectsPatchesZ; z++)
			{
				objectsPatches[x][z] = new ObjectsPatch(index++, objectsPatchWidth, objectsPatchHeight, perspectiveCamera);
				objectsPatches[x][z].setCurrentPosition(minimumX + x*objectsPatchWidth, 0.0f, minimumZ - z*objectsPatchHeight);
				objectsPatches[x][z].initialize();
				objectsPatches[x][z].setCullingPoints(pineTreeCullingPoints);
			}
		}
	}


	private void createObjectsBuffers()
	{
		pineTreeArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		pineTreeArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		hugeTreeArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		hugeTreeArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		palmTreeArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		palmTreeArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		birchTreeArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		birchTreeArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		fernPlantArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		fernPlantArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		weedPlantArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		weedPlantArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		bushPlantArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		bushPlantArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		palmPlantArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		palmPlantArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		rockAArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		rockAArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		rockBArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		rockBArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];

		rockCArrayLODA = new float[MAX_TREE_INSTANCES_TOTAL * 4];
		rockCArrayLODB = new float[MAX_TREE_INSTANCES_TOTAL * 4];


		for(int i=0; i < MAX_TREE_INSTANCES_TOTAL; i++)
		{
			pineTreeArrayLODA[i*4] = 0f;
			pineTreeArrayLODA[i*4 + 1] = 0f;
			pineTreeArrayLODA[i*4 + 2] = 0f;
			pineTreeArrayLODA[i*4 + 3] = 0f;

			pineTreeArrayLODB[i*4] = 0f;
			pineTreeArrayLODB[i*4 + 1] = 0f;
			pineTreeArrayLODB[i*4 + 2] = 0f;
			pineTreeArrayLODB[i*4 + 3] = 0f;


			hugeTreeArrayLODA[i*4] = 0f;
			hugeTreeArrayLODA[i*4 + 1] = 0f;
			hugeTreeArrayLODA[i*4 + 2] = 0f;
			hugeTreeArrayLODA[i*4 + 3] = 0f;

			hugeTreeArrayLODB[i*4] = 0f;
			hugeTreeArrayLODB[i*4 + 1] = 0f;
			hugeTreeArrayLODB[i*4 + 2] = 0f;
			hugeTreeArrayLODB[i*4 + 3] = 0f;


			palmTreeArrayLODA[i*4] = 0f;
			palmTreeArrayLODA[i*4 + 1] = 0f;
			palmTreeArrayLODA[i*4 + 2] = 0f;
			palmTreeArrayLODA[i*4 + 3] = 0f;

			palmTreeArrayLODB[i*4] = 0f;
			palmTreeArrayLODB[i*4 + 1] = 0f;
			palmTreeArrayLODB[i*4 + 2] = 0f;
			palmTreeArrayLODB[i*4 + 3] = 0f;


			birchTreeArrayLODA[i*4] = 0f;
			birchTreeArrayLODA[i*4 + 1] = 0f;
			birchTreeArrayLODA[i*4 + 2] = 0f;
			birchTreeArrayLODA[i*4 + 3] = 0f;

			birchTreeArrayLODB[i*4] = 0f;
			birchTreeArrayLODB[i*4 + 1] = 0f;
			birchTreeArrayLODB[i*4 + 2] = 0f;
			birchTreeArrayLODB[i*4 + 3] = 0f;


			fernPlantArrayLODA[i*4] = 0f;
			fernPlantArrayLODA[i*4 + 1] = 0f;
			fernPlantArrayLODA[i*4 + 2] = 0f;
			fernPlantArrayLODA[i*4 + 3] = 0f;

			fernPlantArrayLODB[i*4] = 0f;
			fernPlantArrayLODB[i*4 + 1] = 0f;
			fernPlantArrayLODB[i*4 + 2] = 0f;
			fernPlantArrayLODB[i*4 + 3] = 0f;


			weedPlantArrayLODA[i*4] = 0f;
			weedPlantArrayLODA[i*4 + 1] = 0f;
			weedPlantArrayLODA[i*4 + 2] = 0f;
			weedPlantArrayLODA[i*4 + 3] = 0f;

			weedPlantArrayLODB[i*4] = 0f;
			weedPlantArrayLODB[i*4 + 1] = 0f;
			weedPlantArrayLODB[i*4 + 2] = 0f;
			weedPlantArrayLODB[i*4 + 3] = 0f;


			bushPlantArrayLODA[i*4] = 0f;
			bushPlantArrayLODA[i*4 + 1] = 0f;
			bushPlantArrayLODA[i*4 + 2] = 0f;
			bushPlantArrayLODA[i*4 + 3] = 0f;

			bushPlantArrayLODB[i*4] = 0f;
			bushPlantArrayLODB[i*4 + 1] = 0f;
			bushPlantArrayLODB[i*4 + 2] = 0f;
			bushPlantArrayLODB[i*4 + 3] = 0f;


			palmPlantArrayLODA[i*4] = 0f;
			palmPlantArrayLODA[i*4 + 1] = 0f;
			palmPlantArrayLODA[i*4 + 2] = 0f;
			palmPlantArrayLODA[i*4 + 3] = 0f;

			palmPlantArrayLODB[i*4] = 0f;
			palmPlantArrayLODB[i*4 + 1] = 0f;
			palmPlantArrayLODB[i*4 + 2] = 0f;
			palmPlantArrayLODB[i*4 + 3] = 0f;


			rockAArrayLODA[i*4] = 0f;
			rockAArrayLODA[i*4 + 1] = 0f;
			rockAArrayLODA[i*4 + 2] = 0f;
			rockAArrayLODA[i*4 + 3] = 0f;

			rockAArrayLODB[i*4] = 0f;
			rockAArrayLODB[i*4 + 1] = 0f;
			rockAArrayLODB[i*4 + 2] = 0f;
			rockAArrayLODB[i*4 + 3] = 0f;


			rockBArrayLODA[i*4] = 0f;
			rockBArrayLODA[i*4 + 1] = 0f;
			rockBArrayLODA[i*4 + 2] = 0f;
			rockBArrayLODA[i*4 + 3] = 0f;

			rockBArrayLODB[i*4] = 0f;
			rockBArrayLODB[i*4 + 1] = 0f;
			rockBArrayLODB[i*4 + 2] = 0f;
			rockBArrayLODB[i*4 + 3] = 0f;

			rockCArrayLODA[i*4] = 0f;
			rockCArrayLODA[i*4 + 1] = 0f;
			rockCArrayLODA[i*4 + 2] = 0f;
			rockCArrayLODA[i*4 + 3] = 0f;

			rockCArrayLODB[i*4] = 0f;
			rockCArrayLODB[i*4 + 1] = 0f;
			rockCArrayLODB[i*4 + 2] = 0f;
			rockCArrayLODB[i*4 + 3] = 0f;

		}


		pineTreeArrayBufferLODA = ByteBuffer
				.allocateDirect(pineTreeArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(pineTreeArrayLODA);
		pineTreeArrayBufferLODA.position(0);

		pineTreeArrayBufferLODB = ByteBuffer
				.allocateDirect(pineTreeArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(pineTreeArrayLODB);
		pineTreeArrayBufferLODB.position(0);


		hugeTreeArrayBufferLODA = ByteBuffer
				.allocateDirect(hugeTreeArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(hugeTreeArrayLODA);
		hugeTreeArrayBufferLODA.position(0);

		hugeTreeArrayBufferLODB = ByteBuffer
				.allocateDirect(hugeTreeArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(hugeTreeArrayLODB);
		hugeTreeArrayBufferLODB.position(0);


		palmTreeArrayBufferLODA = ByteBuffer
				.allocateDirect(palmTreeArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(palmTreeArrayLODA);
		palmTreeArrayBufferLODA.position(0);

		palmTreeArrayBufferLODB = ByteBuffer
				.allocateDirect(palmTreeArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(palmTreeArrayLODB);
		palmTreeArrayBufferLODB.position(0);


		birchTreeArrayBufferLODA = ByteBuffer
				.allocateDirect(birchTreeArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(birchTreeArrayLODA);
		birchTreeArrayBufferLODA.position(0);

		birchTreeArrayBufferLODB = ByteBuffer
				.allocateDirect(birchTreeArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(birchTreeArrayLODB);
		birchTreeArrayBufferLODB.position(0);


		fernPlantArrayBufferLODA = ByteBuffer
				.allocateDirect(fernPlantArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(fernPlantArrayLODA);
		fernPlantArrayBufferLODA.position(0);

		fernPlantArrayBufferLODB = ByteBuffer
				.allocateDirect(fernPlantArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(fernPlantArrayLODB);
		fernPlantArrayBufferLODB.position(0);


		weedPlantArrayBufferLODA = ByteBuffer
				.allocateDirect(weedPlantArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(weedPlantArrayLODA);
		weedPlantArrayBufferLODA.position(0);

		weedPlantArrayBufferLODB = ByteBuffer
				.allocateDirect(weedPlantArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(weedPlantArrayLODB);
		weedPlantArrayBufferLODB.position(0);


		bushPlantArrayBufferLODA = ByteBuffer
				.allocateDirect(bushPlantArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(bushPlantArrayLODA);
		bushPlantArrayBufferLODA.position(0);

		bushPlantArrayBufferLODB = ByteBuffer
				.allocateDirect(bushPlantArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(bushPlantArrayLODB);
		bushPlantArrayBufferLODB.position(0);


		palmPlantArrayBufferLODA = ByteBuffer
				.allocateDirect(palmPlantArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(palmPlantArrayLODA);
		palmPlantArrayBufferLODA.position(0);

		palmPlantArrayBufferLODB = ByteBuffer
				.allocateDirect(palmPlantArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(palmPlantArrayLODB);
		palmPlantArrayBufferLODB.position(0);


		rockAArrayBufferLODA = ByteBuffer
				.allocateDirect(rockAArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockAArrayLODA);
		rockAArrayBufferLODA.position(0);

		rockAArrayBufferLODB = ByteBuffer
				.allocateDirect(rockAArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockAArrayLODB);
		rockAArrayBufferLODB.position(0);


		rockBArrayBufferLODA = ByteBuffer
				.allocateDirect(rockBArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockBArrayLODA);
		rockBArrayBufferLODA.position(0);

		rockBArrayBufferLODB = ByteBuffer
				.allocateDirect(rockBArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockBArrayLODB);
		rockBArrayBufferLODB.position(0);

		rockCArrayBufferLODA = ByteBuffer
				.allocateDirect(rockCArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockCArrayLODA);
		rockCArrayBufferLODA.position(0);

		rockCArrayBufferLODB = ByteBuffer
				.allocateDirect(rockCArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(rockCArrayLODB);
		rockCArrayBufferLODB.position(0);

		glGenBuffers(2, pineTreeArrayUbo, 0);
		glGenBuffers(2, hugeTreeArrayUbo, 0);
		glGenBuffers(2, palmTreeArrayUbo, 0);
		glGenBuffers(2, birchTreeArrayUbo, 0);
		glGenBuffers(2, fernPlantArrayUbo, 0);
		glGenBuffers(2, weedPlantArrayUbo, 0);
		glGenBuffers(2, bushPlantArrayUbo, 0);
		glGenBuffers(2, palmPlantArrayUbo, 0);
		glGenBuffers(2, rockAArrayUbo, 0);
		glGenBuffers(2, rockBArrayUbo, 0);
		glGenBuffers(2, rockCArrayUbo, 0);


		glBindBuffer(GL_UNIFORM_BUFFER, pineTreeArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, pineTreeArrayBufferLODA.capacity() * BYTES_PER_FLOAT, pineTreeArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, pineTreeArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, pineTreeArrayBufferLODB.capacity() * BYTES_PER_FLOAT, pineTreeArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, hugeTreeArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, hugeTreeArrayBufferLODA.capacity() * BYTES_PER_FLOAT, hugeTreeArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, hugeTreeArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, hugeTreeArrayBufferLODB.capacity() * BYTES_PER_FLOAT, hugeTreeArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, palmTreeArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, palmTreeArrayBufferLODA.capacity() * BYTES_PER_FLOAT, palmTreeArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, palmTreeArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, palmTreeArrayBufferLODB.capacity() * BYTES_PER_FLOAT, palmTreeArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, birchTreeArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, birchTreeArrayBufferLODA.capacity() * BYTES_PER_FLOAT, birchTreeArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, birchTreeArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, birchTreeArrayBufferLODB.capacity() * BYTES_PER_FLOAT, birchTreeArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, fernPlantArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, fernPlantArrayBufferLODA.capacity() * BYTES_PER_FLOAT, fernPlantArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, fernPlantArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, fernPlantArrayBufferLODB.capacity() * BYTES_PER_FLOAT, fernPlantArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, weedPlantArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, weedPlantArrayBufferLODA.capacity() * BYTES_PER_FLOAT, weedPlantArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, weedPlantArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, weedPlantArrayBufferLODB.capacity() * BYTES_PER_FLOAT, weedPlantArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, bushPlantArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, bushPlantArrayBufferLODA.capacity() * BYTES_PER_FLOAT, bushPlantArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, bushPlantArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, bushPlantArrayBufferLODB.capacity() * BYTES_PER_FLOAT, bushPlantArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, palmPlantArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, palmPlantArrayBufferLODA.capacity() * BYTES_PER_FLOAT, palmPlantArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, palmPlantArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, palmPlantArrayBufferLODB.capacity() * BYTES_PER_FLOAT, palmPlantArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, rockAArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, rockAArrayBufferLODA.capacity() * BYTES_PER_FLOAT, rockAArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, rockAArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, rockAArrayBufferLODB.capacity() * BYTES_PER_FLOAT, rockAArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, rockBArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, rockBArrayBufferLODA.capacity() * BYTES_PER_FLOAT, rockBArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, rockBArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, rockBArrayBufferLODB.capacity() * BYTES_PER_FLOAT, rockBArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, rockCArrayUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, rockCArrayBufferLODA.capacity() * BYTES_PER_FLOAT, rockCArrayBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, rockCArrayUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, rockCArrayBufferLODB.capacity() * BYTES_PER_FLOAT, rockCArrayBufferLODB, GL_DYNAMIC_DRAW);


		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	private void updateObjectsPatchesBuffer()
	{
		int numElements;
		int pineTreeOffsetLODA = 0;
		int pineTreeOffsetLODB = 0;
		int hugeTreeOffsetLODA = 0;
		int hugeTreeOffsetLODB = 0;
		int palmTreeOffsetLODA = 0;
		int palmTreeOffsetLODB = 0;
		int birchTreeOffsetLODA = 0;
		int birchTreeOffsetLODB = 0;
		int fernPlantOffsetLODA = 0;
		int fernPlantOffsetLODB = 0;
		int weedPlantOffsetLODA = 0;
		int weedPlantOffsetLODB = 0;
		int bushPlantOffsetLODA = 0;
		int bushPlantOffsetLODB = 0;
		int palmPlantOffsetLODA = 0;
		int palmPlantOffsetLODB = 0;
		int rockAOffsetLODA = 0;
		int rockAOffsetLODB = 0;
		int rockBOffsetLODA = 0;
		int rockBOffsetLODB = 0;
		int rockCOffsetLODA = 0;
		int rockCOffsetLODB = 0;

		// IMPORTANT! reset instance count
		pineTreeNumInstances[LOD_A] = 0;
		pineTreeNumInstances[LOD_B] = 0;

		hugeTreeNumInstances[LOD_A] = 0;
		hugeTreeNumInstances[LOD_B] = 0;

		palmTreeNumInstances[LOD_A] = 0;
		palmTreeNumInstances[LOD_B] = 0;

		birchTreeNumInstances[LOD_A] = 0;
		birchTreeNumInstances[LOD_B] = 0;

		fernPlantNumInstances[LOD_A] = 0;
		fernPlantNumInstances[LOD_B] = 0;

		weedPlantNumInstances[LOD_A] = 0;
		weedPlantNumInstances[LOD_B] = 0;

		bushPlantNumInstances[LOD_A] = 0;
		bushPlantNumInstances[LOD_B] = 0;

		palmPlantNumInstances[LOD_A] = 0;
		palmPlantNumInstances[LOD_B] = 0;

		rockANumInstances[LOD_A] = 0;
		rockANumInstances[LOD_B] = 0;

		rockBNumInstances[LOD_A] = 0;
		rockBNumInstances[LOD_B] = 0;

		rockCNumInstances[LOD_A] = 0;
		rockCNumInstances[LOD_B] = 0;

		for(int x=0; x < numObjectsPatchesX; x++)
		{
			for(int z=0; z < numObjectsPatchesZ; z++)
			{
				objectsPatches[x][z].updateObjectsArrays();

				// pine tree

				numElements = objectsPatches[x][z].pineTreeNumInstances[LOD_A];
				if(numElements > 0)
				{
					pineTreeNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].pineTreePointsLODA, 0, pineTreeArrayLODA, pineTreeOffsetLODA, numElements);
					pineTreeOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].pineTreeNumInstances[LOD_B];
				if(numElements > 0)
				{
					pineTreeNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].pineTreePointsLODB, 0, pineTreeArrayLODB, pineTreeOffsetLODB, numElements);
					pineTreeOffsetLODB += numElements;
				}

				// huge tree

				numElements = objectsPatches[x][z].hugeTreeNumInstances[LOD_A];
				if(numElements > 0)
				{
					hugeTreeNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].hugeTreePointsLODA, 0, hugeTreeArrayLODA, hugeTreeOffsetLODA, numElements);
					hugeTreeOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].hugeTreeNumInstances[LOD_B];
				if(numElements > 0)
				{
					hugeTreeNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].hugeTreePointsLODB, 0, hugeTreeArrayLODB, hugeTreeOffsetLODB, numElements);
					hugeTreeOffsetLODB += numElements;
				}

				// palm tree

				numElements = objectsPatches[x][z].palmTreeNumInstances[LOD_A];
				if(numElements > 0)
				{
					palmTreeNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].palmTreePointsLODA, 0, palmTreeArrayLODA, palmTreeOffsetLODA, numElements);
					palmTreeOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].palmTreeNumInstances[LOD_B];
				if(numElements > 0)
				{
					palmTreeNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].palmTreePointsLODB, 0, palmTreeArrayLODB, palmTreeOffsetLODB, numElements);
					palmTreeOffsetLODB += numElements;
				}

				// birch tree

				numElements = objectsPatches[x][z].birchTreeNumInstances[LOD_A];
				if(numElements > 0)
				{
					birchTreeNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].birchTreePointsLODA, 0, birchTreeArrayLODA, birchTreeOffsetLODA, numElements);
					birchTreeOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].birchTreeNumInstances[LOD_B];
				if(numElements > 0)
				{
					birchTreeNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].birchTreePointsLODB, 0, birchTreeArrayLODB, birchTreeOffsetLODB, numElements);
					birchTreeOffsetLODB += numElements;
				}

				// fern plant

				numElements = objectsPatches[x][z].fernPlantNumInstances[LOD_A];
				if(numElements > 0)
				{
					fernPlantNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].fernPlantPointsLODA, 0, fernPlantArrayLODA, fernPlantOffsetLODA, numElements);
					fernPlantOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].fernPlantNumInstances[LOD_B];
				if(numElements > 0)
				{
					fernPlantNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].fernPlantPointsLODB, 0, fernPlantArrayLODB, fernPlantOffsetLODB, numElements);
					fernPlantOffsetLODB += numElements;
				}

				// weed plant

				numElements = objectsPatches[x][z].weedPlantNumInstances[LOD_A];
				if(numElements > 0)
				{
					weedPlantNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].weedPlantPointsLODA, 0, weedPlantArrayLODA, weedPlantOffsetLODA, numElements);
					weedPlantOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].weedPlantNumInstances[LOD_B];
				if(numElements > 0)
				{
					weedPlantNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].weedPlantPointsLODB, 0, weedPlantArrayLODB, weedPlantOffsetLODB, numElements);
					weedPlantOffsetLODB += numElements;
				}

				// bush plant

				numElements = objectsPatches[x][z].bushPlantNumInstances[LOD_A];
				if(numElements > 0)
				{
					bushPlantNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].bushPlantPointsLODA, 0, bushPlantArrayLODA, bushPlantOffsetLODA, numElements);
					bushPlantOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].bushPlantNumInstances[LOD_B];
				if(numElements > 0)
				{
					bushPlantNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].bushPlantPointsLODB, 0, bushPlantArrayLODB, bushPlantOffsetLODB, numElements);
					bushPlantOffsetLODB += numElements;
				}

				// palm plant

				numElements = objectsPatches[x][z].palmPlantNumInstances[LOD_A];
				if(numElements > 0)
				{
					palmPlantNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].palmPlantPointsLODA, 0, palmPlantArrayLODA, palmPlantOffsetLODA, numElements);
					palmPlantOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].palmPlantNumInstances[LOD_B];
				if(numElements > 0)
				{
					palmPlantNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].palmPlantPointsLODB, 0, palmPlantArrayLODB, palmPlantOffsetLODB, numElements);
					palmPlantOffsetLODB += numElements;
				}

				// rock A

				numElements = objectsPatches[x][z].rockANumInstances[LOD_A];
				if(numElements > 0)
				{
					rockANumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockAPointsLODA, 0, rockAArrayLODA, rockAOffsetLODA, numElements);
					rockAOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].rockANumInstances[LOD_B];
				if(numElements > 0)
				{
					rockANumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockAPointsLODB, 0, rockAArrayLODB, rockAOffsetLODB, numElements);
					rockAOffsetLODB += numElements;
				}

				// rock B

				numElements = objectsPatches[x][z].rockBNumInstances[LOD_A];
				if(numElements > 0)
				{
					rockBNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockBPointsLODA, 0, rockBArrayLODA, rockBOffsetLODA, numElements);
					rockBOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].rockBNumInstances[LOD_B];
				if(numElements > 0)
				{
					rockBNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockBPointsLODB, 0, rockBArrayLODB, rockBOffsetLODB, numElements);
					rockBOffsetLODB += numElements;
				}

				// rock C

				numElements = objectsPatches[x][z].rockCNumInstances[LOD_A];
				if(numElements > 0)
				{
					rockCNumInstances[LOD_A] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockCPointsLODA, 0, rockCArrayLODA, rockCOffsetLODA, numElements);
					rockCOffsetLODA += numElements;
				}

				numElements = objectsPatches[x][z].rockCNumInstances[LOD_B];
				if(numElements > 0)
				{
					rockCNumInstances[LOD_B] += numElements;
					numElements = numElements * 4;
					System.arraycopy(objectsPatches[x][z].rockCPointsLODB, 0, rockCArrayLODB, rockCOffsetLODB, numElements);
					rockCOffsetLODB += numElements;
				}
			}
		}

		pineTreeArrayBufferLODA.position(0);
		pineTreeArrayBufferLODA.put(pineTreeArrayLODA, 0, pineTreeArrayLODA.length);
		pineTreeArrayBufferLODA.position(0);

		pineTreeArrayBufferLODB.position(0);
		pineTreeArrayBufferLODB.put(pineTreeArrayLODB, 0, pineTreeArrayLODB.length);
		pineTreeArrayBufferLODB.position(0);

		hugeTreeArrayBufferLODA.position(0);
		hugeTreeArrayBufferLODA.put(hugeTreeArrayLODA, 0, hugeTreeArrayLODA.length);
		hugeTreeArrayBufferLODA.position(0);

		hugeTreeArrayBufferLODB.position(0);
		hugeTreeArrayBufferLODB.put(hugeTreeArrayLODB, 0, hugeTreeArrayLODB.length);
		hugeTreeArrayBufferLODB.position(0);

		palmTreeArrayBufferLODA.position(0);
		palmTreeArrayBufferLODA.put(palmTreeArrayLODA, 0, palmTreeArrayLODA.length);
		palmTreeArrayBufferLODA.position(0);

		palmTreeArrayBufferLODB.position(0);
		palmTreeArrayBufferLODB.put(palmTreeArrayLODB, 0, palmTreeArrayLODB.length);
		palmTreeArrayBufferLODB.position(0);

		birchTreeArrayBufferLODA.position(0);
		birchTreeArrayBufferLODA.put(birchTreeArrayLODA, 0, birchTreeArrayLODA.length);
		birchTreeArrayBufferLODA.position(0);

		birchTreeArrayBufferLODB.position(0);
		birchTreeArrayBufferLODB.put(birchTreeArrayLODB, 0, birchTreeArrayLODB.length);
		birchTreeArrayBufferLODB.position(0);

		fernPlantArrayBufferLODA.position(0);
		fernPlantArrayBufferLODA.put(fernPlantArrayLODA, 0, fernPlantArrayLODA.length);
		fernPlantArrayBufferLODA.position(0);

		fernPlantArrayBufferLODB.position(0);
		fernPlantArrayBufferLODB.put(fernPlantArrayLODB, 0, fernPlantArrayLODB.length);
		fernPlantArrayBufferLODB.position(0);

		weedPlantArrayBufferLODA.position(0);
		weedPlantArrayBufferLODA.put(weedPlantArrayLODA, 0, weedPlantArrayLODA.length);
		weedPlantArrayBufferLODA.position(0);

		weedPlantArrayBufferLODB.position(0);
		weedPlantArrayBufferLODB.put(weedPlantArrayLODB, 0, weedPlantArrayLODB.length);
		weedPlantArrayBufferLODB.position(0);

		bushPlantArrayBufferLODA.position(0);
		bushPlantArrayBufferLODA.put(bushPlantArrayLODA, 0, bushPlantArrayLODA.length);
		bushPlantArrayBufferLODA.position(0);

		bushPlantArrayBufferLODB.position(0);
		bushPlantArrayBufferLODB.put(bushPlantArrayLODB, 0, bushPlantArrayLODB.length);
		bushPlantArrayBufferLODB.position(0);

		palmPlantArrayBufferLODA.position(0);
		palmPlantArrayBufferLODA.put(palmPlantArrayLODA, 0, palmPlantArrayLODA.length);
		palmPlantArrayBufferLODA.position(0);

		palmPlantArrayBufferLODB.position(0);
		palmPlantArrayBufferLODB.put(palmPlantArrayLODB, 0, palmPlantArrayLODB.length);
		palmPlantArrayBufferLODB.position(0);

		rockAArrayBufferLODA.position(0);
		rockAArrayBufferLODA.put(rockAArrayLODA, 0, rockAArrayLODA.length);
		rockAArrayBufferLODA.position(0);

		rockAArrayBufferLODB.position(0);
		rockAArrayBufferLODB.put(rockAArrayLODB, 0, rockAArrayLODB.length);
		rockAArrayBufferLODB.position(0);

		rockBArrayBufferLODA.position(0);
		rockBArrayBufferLODA.put(rockBArrayLODA, 0, rockBArrayLODA.length);
		rockBArrayBufferLODA.position(0);

		rockBArrayBufferLODB.position(0);
		rockBArrayBufferLODB.put(rockBArrayLODB, 0, rockBArrayLODB.length);
		rockBArrayBufferLODB.position(0);

		rockCArrayBufferLODA.position(0);
		rockCArrayBufferLODA.put(rockCArrayLODA, 0, rockCArrayLODA.length);
		rockCArrayBufferLODA.position(0);

		rockCArrayBufferLODB.position(0);
		rockCArrayBufferLODB.put(rockCArrayLODB, 0, rockCArrayLODB.length);
		rockCArrayBufferLODB.position(0);

		glBindBuffer(GL_UNIFORM_BUFFER, pineTreeArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, pineTreeOffsetLODA * BYTES_PER_FLOAT, pineTreeArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, pineTreeArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, pineTreeOffsetLODB * BYTES_PER_FLOAT, pineTreeArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, hugeTreeArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, hugeTreeOffsetLODA * BYTES_PER_FLOAT, hugeTreeArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, hugeTreeArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, hugeTreeOffsetLODB * BYTES_PER_FLOAT, hugeTreeArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, palmTreeArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, palmTreeOffsetLODA * BYTES_PER_FLOAT, palmTreeArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, palmTreeArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, palmTreeOffsetLODB * BYTES_PER_FLOAT, palmTreeArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, birchTreeArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, birchTreeOffsetLODA * BYTES_PER_FLOAT, birchTreeArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, birchTreeArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, birchTreeOffsetLODB * BYTES_PER_FLOAT, birchTreeArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, fernPlantArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, fernPlantOffsetLODA * BYTES_PER_FLOAT, fernPlantArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, fernPlantArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, fernPlantOffsetLODB * BYTES_PER_FLOAT, fernPlantArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, weedPlantArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, weedPlantOffsetLODA * BYTES_PER_FLOAT, weedPlantArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, weedPlantArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, weedPlantOffsetLODB * BYTES_PER_FLOAT, weedPlantArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, bushPlantArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, bushPlantOffsetLODA * BYTES_PER_FLOAT, bushPlantArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, bushPlantArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, bushPlantOffsetLODB * BYTES_PER_FLOAT, bushPlantArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, palmPlantArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, palmPlantOffsetLODA * BYTES_PER_FLOAT, palmPlantArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, palmPlantArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, palmPlantOffsetLODB * BYTES_PER_FLOAT, palmPlantArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, rockAArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockAOffsetLODA * BYTES_PER_FLOAT, rockAArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, rockAArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockAOffsetLODB * BYTES_PER_FLOAT, rockAArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, rockBArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockBOffsetLODA * BYTES_PER_FLOAT, rockBArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, rockBArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockBOffsetLODB * BYTES_PER_FLOAT, rockBArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, rockCArrayUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockCOffsetLODA * BYTES_PER_FLOAT, rockCArrayBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, rockCArrayUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, rockCOffsetLODB * BYTES_PER_FLOAT, rockCArrayBufferLODB);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	public void setPlayerRock(PlayerRock playerRock)
	{
		this.playerRock = playerRock;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// Grass
	////////////////////////////////////////////////////////////////////////////////////////////////

	private void createGrassBuffers()
	{
		for(int i=0; i < 4096; i++)
		{
			grassArrayLODA[i] = 0f;
			grassArrayLODB[i] = 0f;
			grassArrayLODC[i] = 0f;
		}

		grassBufferLODA = ByteBuffer
				.allocateDirect(grassArrayLODA.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(grassArrayLODA);
		grassBufferLODA.position(0);

		grassBufferLODB = ByteBuffer
				.allocateDirect(grassArrayLODB.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(grassArrayLODB);
		grassBufferLODB.position(0);

		grassBufferLODC = ByteBuffer
				.allocateDirect(grassArrayLODC.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(grassArrayLODC);
		grassBufferLODC.position(0);

		grassUbo = new int[3];
		glGenBuffers(3, grassUbo, 0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_A]);
		glBufferData(GL_UNIFORM_BUFFER, grassBufferLODA.capacity() * BYTES_PER_FLOAT, grassBufferLODA, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_B]);
		glBufferData(GL_UNIFORM_BUFFER, grassBufferLODB.capacity() * BYTES_PER_FLOAT, grassBufferLODB, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_C]);
		glBufferData(GL_UNIFORM_BUFFER, grassBufferLODC.capacity() * BYTES_PER_FLOAT, grassBufferLODC, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	private void updateGrassBuffers()
	{
		int sumLODA = 0;
		int sumLODB = 0;
		int sumLODC = 0;
		int numPoints;

		for(int z=0; z < numGroundPatchesZ; z++)
		{
			if(groundPatches[0][z].type == GROUND_PATCH_GROUND)
			{
				for(int x=0; x < numGroundPatchesX; x++)
				{
					groundPatches[x][z].updateGrassPointsArray();

					numPoints = groundPatches[x][z].numGrassPointsPerLOD[LOD_A];
					if(numPoints > 0)
					{
						System.arraycopy(groundPatches[x][z].grassPointsLODA, 0, grassArrayLODA, sumLODA*4, numPoints*4);
						sumLODA += numPoints;
					}

					numPoints = groundPatches[x][z].numGrassPointsPerLOD[LOD_B];
					if(numPoints > 0)
					{
						System.arraycopy(groundPatches[x][z].grassPointsLODB, 0, grassArrayLODB, sumLODB*4, numPoints*4);
						sumLODB += numPoints;
					}

					numPoints = groundPatches[x][z].numGrassPointsPerLOD[LOD_C];
					if(numPoints > 0)
					{
						System.arraycopy(groundPatches[x][z].grassPointsLODC, 0, grassArrayLODC, sumLODC*4, numPoints*4);
						sumLODC += numPoints;
					}
				}
			}
		}

		grassNumInstances[LOD_A] = sumLODA;
		grassNumInstances[LOD_B] = sumLODB;
		grassNumInstances[LOD_C] = sumLODC;

		grassBufferLODA.put(grassArrayLODA);
		grassBufferLODA.position(0);

		grassBufferLODB.put(grassArrayLODB);
		grassBufferLODB.position(0);

		grassBufferLODC.put(grassArrayLODC);
		grassBufferLODC.position(0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_A]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, 4096 * BYTES_PER_FLOAT, grassBufferLODA);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_B]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, 4096 * BYTES_PER_FLOAT, grassBufferLODB);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBuffer(GL_UNIFORM_BUFFER, grassUbo[LOD_C]);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, 4096 * BYTES_PER_FLOAT, grassBufferLODC);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		/*for(int x=0; x < numGroundPatchesX; x++)
		{
			for(int z=0; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z].updateGrassPointsArray();

				sumLODA += groundPatches[x][z].numGrassPointsPerLOD[LOD_A];
				sumLODB += groundPatches[x][z].numGrassPointsPerLOD[LOD_B];
				sumLODC += groundPatches[x][z].numGrassPointsPerLOD[LOD_C];
			}
		}

		grassCountMax[LOD_A] = Math.max(grassCountMax[LOD_A], sumLODA);
		grassCountMax[LOD_B] = Math.max(grassCountMax[LOD_B], sumLODB);
		grassCountMax[LOD_C] = Math.max(grassCountMax[LOD_C], sumLODC);

		Log.d("GrassLODCount", "A("+grassCountMax[LOD_A]+") B("+grassCountMax[LOD_B]+") C("+grassCountMax[LOD_C]+")");*/
	}


	public void newGame()
	{
		//restartGroundPatches();
		restartObjectsPatches();
	}


	private void restartGroundPatches()
	{
		groundLeftmostIndex = 0;
		groundRightmostIndex = numGroundPatchesX-1;
		groundUpperIndex = numGroundPatchesZ-1;
		groundLowerIndex = 0;

		float minimumX = -(float)((numGroundPatchesX-1)/2) * groundPatchWidth;
		float minimumZ = groundPatchHeight;

		// FIRST LANE
		groundPatches[0][0].type = GROUND_PATCH_GROUND;
		groundPatches[0][0].reinitialize(GROUND_PATCH_ROOT, null, null);
		groundPatches[0][0].setCurrentPosition(minimumX, 0.0f, minimumZ);

		for(int z=1; z < numGroundPatchesZ; z++)
		{
			groundPatches[0][z].type = GROUND_PATCH_GROUND;
			groundPatches[0][z].reinitialize(
					GROUND_PATCH_UP,
					groundPatches[0][z-1].getVertexColors(GROUND_PATCH_UP),
					null);
			groundPatches[0][z].setCurrentPosition(minimumX, 0.0f, minimumZ - (float)z*groundPatchHeight);
		}

		// NEXT LANES
		for(int x=1; x < numGroundPatchesX; x++)
		{
			groundPatches[x][0].type = GROUND_PATCH_GROUND;
			groundPatches[x][0].reinitialize(
					GROUND_PATCH_LEFT,
					null,
					groundPatches[x-1][0].getVertexColors(GROUND_PATCH_RIGHT));
			groundPatches[x][0].setCurrentPosition(minimumX + x*groundPatchWidth, 0.0f, minimumZ);

			for(int z=1; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z].type = GROUND_PATCH_GROUND;
				groundPatches[x][z].reinitialize(
						GROUND_PATCH_UP_LEFT,
						groundPatches[x][z-1].getVertexColors(GROUND_PATCH_UP),
						groundPatches[x-1][z].getVertexColors(GROUND_PATCH_RIGHT));
				groundPatches[x][z].setCurrentPosition(minimumX + x*groundPatchWidth, 0.0f, minimumZ - z*groundPatchHeight);
			}
		}
	}


	private void restartObjectsPatches()
	{
		//float displacementZ = objectsPatches[0][objectsLowerIndex].getCurrentPosition().z - (objectsPatchHeight * 5f);
		float displacementZ = groundPatches[0][groundLowerIndex].getCurrentPosition().z - (objectsPatchHeight * 5f);

		//objectsLeftmostIndex = 0;
		//objectsRightmostIndex = numObjectsPatchesX - 1;
		objectsLowerIndex = 0;
		objectsUpperIndex = numObjectsPatchesZ - 1;

		//float displacementZ = objectsPatchHeight * 5f;
		vec3 currentPos;

		riverWaitCount = RIVER_WAIT * 2;

		for(int z=0; z < numObjectsPatchesZ; z++)
		{
			for(int x=0; x < numObjectsPatchesX; x++)
			{
				currentPos = objectsPatches[x][z].getCurrentPosition();
				objectsPatches[x][z].setCurrentPosition(currentPos.x, currentPos.y, /*currentPos.z*/ displacementZ - ((float)z * objectsPatchHeight));
				objectsPatches[x][z].reinitialize();
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// Save state
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void saveState(FileOutputStream outputStream) throws IOException
	{
		StringBuilder builder = new StringBuilder();

		builder.append("GROUND ");
		builder.append(Integer.toString(groundUpperIndex));		builder.append(" ");
		builder.append(Integer.toString(groundLowerIndex));		builder.append(" ");
		builder.append(Integer.toString(groundLeftmostIndex));	builder.append(" ");
		builder.append(Integer.toString(groundRightmostIndex));	builder.append(" ");
		builder.append(Integer.toString(objectsUpperIndex));		builder.append(" ");
		builder.append(Integer.toString(objectsLowerIndex));		builder.append(" ");
		builder.append(Integer.toString(objectsLeftmostIndex));		builder.append(" ");
		builder.append(Integer.toString(objectsRightmostIndex));
		builder.append("\n");

		builder.append("NUM_GROUND_PATCHES ");
		builder.append(Integer.toString(numGroundPatchesX));
		builder.append(" ");
		builder.append(Integer.toString(numGroundPatchesZ));
		builder.append("\n");

		builder.append("NUM_OBJECTS_PATCHES ");
		builder.append(Integer.toString(numObjectsPatchesX));
		builder.append(" ");
		builder.append(Integer.toString(numObjectsPatchesZ));
		builder.append("\n");

		outputStream.write(builder.toString().getBytes());

		for(int x=0; x < numGroundPatchesX; x++)
		{
			for(int z=0; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z].saveState(outputStream);
			}
		}

		for(int x=0; x < numObjectsPatchesX; x++)
		{
			for(int z=0; z < numObjectsPatchesZ; z++)
			{
				objectsPatches[x][z].saveState(outputStream);
			}
		}
	}


	public void loadState(BufferedReader bufferedReader) throws IOException
	{
		String line;
		String[] tokens;

		// Get indices
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		groundUpperIndex = Integer.parseInt(tokens[1]);
		groundLowerIndex = Integer.parseInt(tokens[2]);
		groundLeftmostIndex = Integer.parseInt(tokens[3]);
		groundRightmostIndex = Integer.parseInt(tokens[4]);
		objectsUpperIndex = Integer.parseInt(tokens[5]);
		objectsLowerIndex = Integer.parseInt(tokens[6]);
		objectsLeftmostIndex = Integer.parseInt(tokens[7]);
		objectsRightmostIndex = Integer.parseInt(tokens[8]);

		// Get the number of ground patches
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numGroundPatchesX = Integer.parseInt(tokens[1]);
		numGroundPatchesZ = Integer.parseInt(tokens[2]);

		// Get the number of objects patches
		line = bufferedReader.readLine();
		tokens = line.split(" ");
		numObjectsPatchesX = Integer.parseInt(tokens[1]);
		numObjectsPatchesZ = Integer.parseInt(tokens[2]);

		for(int x=0; x < numGroundPatchesX; x++)
		{
			for(int z=0; z < numGroundPatchesZ; z++)
			{
				groundPatches[x][z].loadState(bufferedReader);
			}
		}

		updateGrassBuffers();

		for(int x=0; x < numObjectsPatchesX; x++)
		{
			for(int z=0; z < numObjectsPatchesZ; z++)
			{
				objectsPatches[x][z].loadState(bufferedReader);
			}
		}
	}


	public int getNearestGroundPatchType()
	{
		float z1, z2;
		int type1, type2;
		int index = groundLowerIndex;

		z1 = groundPatches[0][index].getCurrentPosition().z;
		type1 = groundPatches[0][index].type;

		index++;
		index = index % numGroundPatchesZ;
		z2 = groundPatches[0][index].getCurrentPosition().z;
		type2 = groundPatches[0][index].type;

		if(z1 < z2)
		{
			return type1;
		}
		else
		{
			return type2;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	///// DEBUG !!!!
	////////////////////////////////////////////////////////////////////////////////////////////////

	private void createDebugPlane()
	{
		float[] vertices = new float[POSITION_COMPONENTS * 4];
		float left = -objectsPatchWidth * 0.5f;
		float right = objectsPatchWidth * 0.5f;
		float up = -objectsPatchHeight * 0.5f;
		float down = objectsPatchHeight * 0.5f;
		float height = 5f;

		// A
		vertices[0] = left;
		vertices[1] = height;
		vertices[2] = down;

		// B
		vertices[3] = right;
		vertices[4] = height;
		vertices[5] = down;

		// C
		vertices[6] = right;
		vertices[7] = height;
		vertices[8] = up;

		// D
		vertices[9] = left;
		vertices[10] = height;
		vertices[11] = up;

		short[] elements = new short[6];

		// A - B - C
		elements[0] = 0;
		elements[1] = 1;
		elements[2] = 2;

		// A - C - D
		elements[3] = 0;
		elements[4] = 2;
		elements[5] = 3;

		// Create the native buffers
		FloatBuffer verticesBuffer;
		ShortBuffer elementsBuffer;

		verticesBuffer = ByteBuffer
				.allocateDirect(vertices.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		verticesBuffer.position(0);

		elementsBuffer = ByteBuffer
				.allocateDirect(elements.length * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(elements);
		elementsBuffer.position(0);

		// Create the VBOs
		glGenBuffers(2, debugPlaneVboHandles, 0);

		glBindBuffer(GL_ARRAY_BUFFER, debugPlaneVboHandles[0]);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer.capacity() * BYTES_PER_FLOAT, verticesBuffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, debugPlaneVboHandles[1]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer.capacity() * BYTES_PER_SHORT, elementsBuffer, GL_STATIC_DRAW);

		// Create the VAO
		glGenVertexArrays(1, debugPlaneVaoHandle, 0);
		glBindVertexArray(debugPlaneVaoHandle[0]);

		// Vertex positions
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, debugPlaneVboHandles[0]);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, debugPlaneVboHandles[1]);

		glBindVertexArray(0);
	}
}
