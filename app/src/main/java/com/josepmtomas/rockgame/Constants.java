package com.josepmtomas.rockgame;

import android.util.FloatMath;

/**
 * Created by Josep on 16/07/2014.
 */
public class Constants
{
	public static final float PI2 = (float)Math.PI * 2.0f;

	// Type sizes
	public static final int BYTES_PER_FLOAT = 4;
	public static final int BYTES_PER_INT = 4;
	public static final int BYTES_PER_SHORT = 2;

	// Mesh attributes components
	/*public static final int POSITION_COMPONENTS = 3;
	public static final int TEXCOORD_COMPONENTS = 2;
	public static final int NORMAL_COMPONENTS = 3;
	public static final int TANGENT_COMPONENTS = 4;
	public static final int COLOR_COMPONENTS = 3;
	public static final int TOTAL_COMPONENTS =
			POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS + COLOR_COMPONENTS;

	// Mesh attributes components offset (number of elements)
	public static final int POSITION_OFFSET = 0;
	public static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	public static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	public static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;
	public static final int COLOR_OFFSET = TANGENT_OFFSET + TANGENT_COMPONENTS;

	// Mesh attributes components offset (bytes)
	public static final int POSITION_BYTE_OFFSET = 0;
	public static final int TEXCOORD_BYTE_OFFSET = POSITION_BYTE_OFFSET + (POSITION_COMPONENTS * BYTES_PER_FLOAT);
	public static final int NORMAL_BYTE_OFFSET = TEXCOORD_BYTE_OFFSET + (TEXCOORD_COMPONENTS * BYTES_PER_FLOAT);
	public static final int TANGENT_BYTE_OFFSET = NORMAL_BYTE_OFFSET + (NORMAL_COMPONENTS * BYTES_PER_FLOAT);
	public static final int COLOR_BYTE_OFFSET = TANGENT_BYTE_OFFSET + (TANGENT_COMPONENTS * BYTES_PER_FLOAT);

	public static final int STRIDE = TOTAL_COMPONENTS * BYTES_PER_FLOAT;*/

	public static final int MAX_GRASS_INSTANCES = 128;
	public static final int MAX_TREE_INSTANCES = 128;
	public static final int MAX_TREE_INSTANCES_TOTAL = 512;

	public static final float PINE_TREE_MIN_SCALE = 0.9f;
	public static final float PINE_TREE_MAX_SCALE = 1.5f;
	public static final float PINE_TREE_SCALE_DIFFERENCE = PINE_TREE_MAX_SCALE - PINE_TREE_MIN_SCALE;

	public static final float HUGE_TREE_MIN_SCALE = 0.9f;
	public static final float HUGE_TREE_MAX_SCALE = 2.0f;
	public static final float HUGE_TREE_SCALE_DIFFERENCE = HUGE_TREE_MAX_SCALE - HUGE_TREE_MIN_SCALE;

	public static final float PALM_TREE_MIN_SCALE = 0.75f;
	public static final float PALM_TREE_MAX_SCALE = 1.0f;
	public static final float PALM_TREE_SCALE_DIFFERENCE = PALM_TREE_MAX_SCALE - PALM_TREE_MIN_SCALE;

	public static final float FERN_PLANT_MIN_SCALE = 1.0f;
	public static final float FERN_PLANT_MAX_SCALE = 1.7f;
	public static final float FERN_PLANT_SCALE_DIFFERENCE = FERN_PLANT_MAX_SCALE - FERN_PLANT_MIN_SCALE;

	public static final float WEED_PLANT_MIN_SCALE = 1f;
	public static final float WEED_PLANT_MAX_SCALE = 1.5f;
	public static final float WEED_PLANT_SCALE_DIFFERENCE = WEED_PLANT_MAX_SCALE - WEED_PLANT_MIN_SCALE;

	public static final float ROCK_A_MIN_SCALE = 0.75f;
	public static final float ROCK_A_MAX_SCALE = 1.5f;
	public static final float ROCK_A_SCALE_DIFFERENCE = ROCK_A_MAX_SCALE - ROCK_A_MIN_SCALE;

	public static final float ROCK_B_MIN_SCALE = 0.75f;
	public static final float ROCK_B_MAX_SCALE = 1.5f;
	public static final float ROCK_B_SCALE_DIFFERENCE = ROCK_B_MAX_SCALE - ROCK_B_MIN_SCALE;

	public static final int GROUND_PATCH_GROUND = 1;
	public static final int GROUND_PATCH_RIVER_ENTRY = 2;
	public static final int GROUND_PATCH_RIVER_MIDDLE = 3;
	public static final int GROUND_PATCH_RIVER_EXIT = 4;

	public static final int GROUND_PATCH_ROOT = 0;
	public static final int GROUND_PATCH_UP = 1;
	public static final int GROUND_PATCH_DOWN = 2;
	public static final int GROUND_PATCH_LEFT = 3;
	public static final int GROUND_PATCH_RIGHT = 4;
	public static final int GROUND_PATCH_UP_LEFT = 5;
	public static final int GROUND_PATCH_UP_RIGHT = 6;

	public static final int GROUND_PATCH_VERTICES_X = 10;
	public static final int GROUND_PATCH_VERTICES_Z = 10;

	public static final int GROUND_RIVER_PATCH_VERTICES_X = 10;
	public static final int GROUND_RIVER_PATCH_VERTICES_Z = 6;
	public static final int GROUND_RIVER_PATCH_POLYGONS_Z = GROUND_RIVER_PATCH_VERTICES_Z - 1;


	public static final int LOD_A = 0;
	public static final int LOD_B = 1;
	public static final int LOD_C = 2;

	public static final float GRASS_LOD_A_MAX_DISTANCE = 450f;
	public static final float GRASS_LOD_B_MAX_DISTANCE = 650f;

	public static final float TREE_LOD_A_MAX_DISTANCE = 650f;

	public static final int NUM_OBJECTS_PATCHES_X = 3;
	public static final int NUM_OBJECTS_PATCHES_Z = 3;


	public static final float MAX_PLAYER_SPEED = 2500f;
	public static final float MAX_PLAYER_SPEED_FACTOR = 0.0003333f;

	// PlayerRock states
	public static final int PLAYER_ROCK_MOVING = 1;
	public static final int PLAYER_ROCK_BOUNCING = 2;
	public static final int PLAYER_ROCK_RECOVERING = 3;
	public static final int PLAYER_ROCK_STOPPED = 4;

	public static final float PLAYER_RECOVERING_TIME = 3f;

	// HUD base points (X_Y)
	public static final int HUD_BASE_LEFT_TOP = 0;
	public static final int HUD_BASE_LEFT_CENTER = 1;
	public static final int HUD_BASE_LEFT_BOTTOM = 2;
	public static final int HUD_BASE_CENTER_TOP = 3;
	public static final int HUD_BASE_CENTER_CENTER = 4;
	public static final int HUD_BASE_CENTER_BOTTOM = 5;
	public static final int HUD_BASE_RIGHT_TOP = 6;
	public static final int HUD_BASE_RIGHT_CENTER = 7;
	public static final int HUD_BASE_RIGHT_BOTTOM = 8;
}
