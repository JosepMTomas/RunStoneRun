package com.josepmtomas.runstonerun;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.josepmtomas.runstonerun.algebra.vec3;
import com.josepmtomas.runstonerun.objects.CreditsMenu;
import com.josepmtomas.runstonerun.objects.EndGameMenu;
import com.josepmtomas.runstonerun.objects.GameOverMenu;
import com.josepmtomas.runstonerun.objects.Ground;
import com.josepmtomas.runstonerun.objects.GroundShield;
import com.josepmtomas.runstonerun.objects.HowToPlayMenu;
import com.josepmtomas.runstonerun.objects.Hud;
import com.josepmtomas.runstonerun.objects.LightInfo;
import com.josepmtomas.runstonerun.objects.MainMenu;
import com.josepmtomas.runstonerun.objects.MenuTextures;
import com.josepmtomas.runstonerun.objects.OptionsMenu;
import com.josepmtomas.runstonerun.objects.PauseMenu;
import com.josepmtomas.runstonerun.objects.PlayerRock;
import com.josepmtomas.runstonerun.objects.RestartMenu;
import com.josepmtomas.runstonerun.objects.Screen;
import com.josepmtomas.runstonerun.objects.SkyDome;
import com.josepmtomas.runstonerun.programs.ProgressBarProgram;
import com.josepmtomas.runstonerun.programs.ScorePanelProgram;
import com.josepmtomas.runstonerun.programs.UIPanelProgram;
import com.josepmtomas.runstonerun.util.FPSCounter;
import com.josepmtomas.runstonerun.util.PerspectiveCamera;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.josepmtomas.runstonerun.Constants.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 05/09/2014.
 * @author Josep
 */

public class ForwardRenderer implements Renderer
{
	private static final String TAG = "Renderer";

	private boolean isLoaded = false;

	// States
	private static final int RENDERER_STATE_PLAYING = 0;
	private static final int RENDERER_STATE_RESUMING = 1;
	private static final int RENDERER_STATE_MAIN_MENU = 2;
	private static final int RENDERER_STATE_OPTIONS_MENU = 3;
	private static final int RENDERER_STATE_CREDITS_MENU = 4;
	private static final int RENDERER_STATE_PAUSE_MENU = 5;
	private static final int RENDERER_STATE_END_GAME_MENU = 6;
	private static final int RENDERER_STATE_RESTART_MENU = 7;
	private static final int RENDERER_STATE_GAME_OVER_MENU = 8;
	private static final int RENDERER_STATE_HOW_TO_PLAY_MENU = 9;
	private static final int RENDERER_STATE_CHANGING_MENU = 10;
	private int rendererState = RENDERER_STATE_MAIN_MENU;

	private int FRAMEBUFFER_WIDTH = 1280; // 1794
	private int FRAMEBUFFER_HEIGHT = 720;	//1104
	private int REFLECTION_MAP_WIDTH = 320;
	private int REFLECTION_MAP_HEIGHT = 180;
	private int SHADOW_MAP_WIDTH = 512;
	private int SHADOW_MAP_HEIGHT = 512;

	private boolean isPaused = false;
	private boolean isPlaying = false;
	private boolean isSavedGame = false;

	private float[] framebufferDimensions = new float[2];

	private int renderWidth;			// Framebuffer width resolution
	private int renderHeight;			// Framebuffer height resolution
	private float renderAspectRatio;	// Framebuffer aspect ratio (width / height)

	private float screenWidth;	// Screen width
	private float screenHeight;	// Screen height

	// Camera
	private vec3 eyePos = new vec3(0.0f, 25.0f, 50.0f);
	private vec3 eyeLook = new vec3(0.0f, 20.0f, 0.0f);

	// Main render matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Main framebuffer objects
	private final int[] fboID = new int[4];
	private final int[] rboID = new int[4];
	private final int[] fboTexIDs = new int[4];
	private int currentResolution = 0;
	private final int[] drawBuffers = {GL_COLOR_ATTACHMENT0};

	// Reflection framebuffer
	private final int[] reflectionFboID = new int[1];
	private final int[] reflectionRboID = new int[1];
	private final int[] reflectionTexID = new int[1];
	private final int[] reflectionDrawBuffers = {GL_COLOR_ATTACHMENT0};

	// Shadow map framebuffer
	private final int[] shadowMapFboID = new int[1];
	private final int[] shadowMapTexID = new int[1];

	// Shadow map matrices
	private float[] shadowView = new float[16];
	private float[] shadowProjection = new float[16];
	private float[] shadowViewProjection = new float[16];
	private float[] shadowBias = new float[16];
	private float[] shadowBiasProjection = new float[16];
	private float[] shadowMatrix = new float[16];

	private Context context;	// Application context

	// FPS
	private int currentFPS = 0;
	private FPSCounter fpsCounter;

	// TIME
	private long startTime = 0;

	// Objects
	PlayerRock playerRock;
	GroundShield groundShield;
	Ground ground;
	SkyDome skyDome;

	Screen screen;
	Hud hud;

	LightInfo lightInfo;

	// Multiplier
	private float scoreMultiplierTime = 0;
	private float scoreMultiplierPercent = 0;

	// Score
	private float fScore = 0f;

	// PlayerRock state
	private int currentState = PLAYER_ROCK_MOVING;

	// GameActivity
	private GameActivity parent;

	// UI menus
	private MainMenu mainMenu;
	private OptionsMenu optionsMenu;
	private CreditsMenu creditsMenu;
	private PauseMenu pauseMenu;
	private EndGameMenu endGameMenu;
	private RestartMenu restartMenu;
	private GameOverMenu gameOverMenu;
	private HowToPlayMenu howToPlayMenu;

	// Shared preferences
	private SharedPreferences sharedPreferences;

	// Timer
	private float resumeTimer = 0;


	public ForwardRenderer(GameActivity parent, SharedPreferences sharedPreferences, float width, float height)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.sharedPreferences = sharedPreferences;

		this.FRAMEBUFFER_WIDTH = (int)width;
		this.FRAMEBUFFER_HEIGHT = (int)height;
		framebufferDimensions[0] = FRAMEBUFFER_WIDTH;
		framebufferDimensions[1] = FRAMEBUFFER_HEIGHT;

		this.renderWidth = (int)width;
		this.renderHeight = (int)height;
		this.renderAspectRatio = width / height;

		this.screenWidth = (int)width;
		this.screenHeight = (int)height;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig)
	{
		// Camera set up
		float near = 0.1f;
		float far = 1200.0f;
		float fov = 60;
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, near, far, renderAspectRatio, fov);
		setLookAtM(view, 0, 0f, 10f, 100f, 0f, 0f, 0f, 0f, 1f, 0f);
		perspectiveM(projection, 0, fov, renderAspectRatio, near, far);

		glEnable(GL_CULL_FACE);

		// Create and initialize objects
		lightInfo = new LightInfo();
		playerRock = new PlayerRock(parent, lightInfo);
		groundShield = new GroundShield(context);
		ground = new Ground(context,
				11, 9, 10, 10, 90f, 90f,
				3, 3, 450f, 450f,
				perspectiveCamera, lightInfo);
		ground.setPlayerRock(playerRock);
		skyDome = new SkyDome(context, lightInfo);

		screen = new Screen(context);

		fpsCounter = new FPSCounter();

		// UI
		UIPanelProgram uiPanelProgram = new UIPanelProgram(context);
		ProgressBarProgram progressBarProgram = new ProgressBarProgram(context);
		ScorePanelProgram scorePanelProgram = new ScorePanelProgram(context);
		MenuTextures menuTextures = new MenuTextures(context);
		hud = new Hud(context, this, uiPanelProgram, progressBarProgram, scorePanelProgram, menuTextures, screenWidth, screenHeight);
		mainMenu = new MainMenu(this, sharedPreferences, uiPanelProgram, scorePanelProgram, menuTextures, screenWidth, screenHeight);
		optionsMenu = new OptionsMenu(parent, this, sharedPreferences, uiPanelProgram, progressBarProgram, menuTextures, screenWidth, screenHeight);
		creditsMenu = new CreditsMenu(parent, this, uiPanelProgram, menuTextures, screenWidth, screenHeight);
		pauseMenu = new PauseMenu(this, uiPanelProgram, menuTextures, screenWidth, screenHeight);
		endGameMenu = new EndGameMenu(this, uiPanelProgram, menuTextures, screenWidth, screenHeight);
		restartMenu = new RestartMenu(this, uiPanelProgram, menuTextures, screenWidth, screenHeight);
		gameOverMenu = new GameOverMenu(this, uiPanelProgram, scorePanelProgram, menuTextures, screenWidth, screenHeight);
		howToPlayMenu = new HowToPlayMenu(this, uiPanelProgram, menuTextures, screenWidth, screenHeight);

		////////////////////////////////////////////////////////////////////////////////////////////
		// Reflection
		////////////////////////////////////////////////////////////////////////////////////////////

		// Reflection framebuffer & renderbuffer
		glGenFramebuffers(1, reflectionFboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, reflectionFboID[0]);
		glGenRenderbuffers(1, reflectionRboID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, reflectionRboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, REFLECTION_MAP_WIDTH, REFLECTION_MAP_HEIGHT);

		// Reflection texture
		glGenTextures(1, reflectionTexID, 0);
		glBindTexture(GL_TEXTURE_2D, reflectionTexID[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, REFLECTION_MAP_WIDTH, REFLECTION_MAP_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, reflectionTexID[0], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, reflectionRboID[0]);

		int reflectionFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(reflectionFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Reflection Framebuffer: OK");
		}
		else
		{
			Log.e(TAG, "Reflection Framebuffer: ERROR");
		}

		// Unbind the Framebuffer & texture
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);


		////////////////////////////////////////////////////////////////////////////////////////////
		// Shadow map framebuffer
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create an OpenGL texture object
		glGenTextures(1, shadowMapTexID, 0);
		//glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapTexID[0]);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		// Set up a FBO and use the shadow map texture as a single depth attachment
		glGenFramebuffers(1, shadowMapFboID, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFboID[0]);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMapTexID[0], 0);

		int shadowMapFboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);

		if(shadowMapFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Shadow Map Framebuffer: OK");
		}
		else
		{
			Log.d(TAG, "Shadow Map Framebuffer: ERROR");
		}

		// Unbind the Framebuffer & texture
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);


		////////////////////////////////////////////////////////////////////////////////////////////
		// Shadow map matrices
		////////////////////////////////////////////////////////////////////////////////////////////

		setLookAtM(shadowView, 0, 150f, 150f, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		orthoM(shadowProjection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		multiplyMM(shadowViewProjection, 0, shadowProjection, 0, shadowView, 0);

		setIdentityM(shadowBias, 0);
		translateM(shadowBias, 0, 0.5f, 0.5f, 0.5f);
		scaleM(shadowBias, 0, 0.5f, 0.5f, 0.5f);

		multiplyMM(shadowBiasProjection, 0, shadowBias, 0, shadowProjection, 0);
		multiplyMM(shadowMatrix, 0, shadowBiasProjection, 0, shadowView, 0);


		////////////////////////////////////////////////////////////////////////////////////////////
		// 100 % resolution framebuffer
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create the main render Framebuffer & Renderbuffer
		glGenFramebuffers(4, fboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[0]);
		glGenRenderbuffers(4, rboID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, rboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

		// Generate the texture on which FBO will render to
		glGenTextures(4, fboTexIDs, 0);
		glBindTexture(GL_TEXTURE_2D, fboTexIDs[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexIDs[0], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID[0]);

		int mainFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(mainFboStatus != GL_FRAMEBUFFER_COMPLETE)
		{
			Log.e(TAG, "Main Framebuffer (100): ERROR");
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// 75 % resolution framebuffer
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create the main render Framebuffer & Renderbuffer
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[1]);
		glBindRenderbuffer(GL_RENDERBUFFER, rboID[1]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, (int)(FRAMEBUFFER_WIDTH * 0.75f), (int)(FRAMEBUFFER_HEIGHT * 0.75f));

		// Generate the texture on which FBO will render to
		glBindTexture(GL_TEXTURE_2D, fboTexIDs[1]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, (int)(FRAMEBUFFER_WIDTH * 0.75f), (int)(FRAMEBUFFER_HEIGHT * 0.75f), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexIDs[1], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID[1]);

		mainFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(mainFboStatus != GL_FRAMEBUFFER_COMPLETE)
		{
			Log.e(TAG, "Main Framebuffer (75): ERROR");
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// 50 % resolution framebuffer
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create the main render Framebuffer & Renderbuffer
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[2]);
		glBindRenderbuffer(GL_RENDERBUFFER, rboID[2]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, (int)(FRAMEBUFFER_WIDTH * 0.5f), (int)(FRAMEBUFFER_HEIGHT * 0.5f));

		// Generate the texture on which FBO will render to
		glBindTexture(GL_TEXTURE_2D, fboTexIDs[2]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, (int)(FRAMEBUFFER_WIDTH * 0.5f), (int)(FRAMEBUFFER_HEIGHT * 0.5f), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexIDs[2], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID[2]);

		mainFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(mainFboStatus != GL_FRAMEBUFFER_COMPLETE)
		{
			Log.e(TAG, "Main Framebuffer (50): ERROR");
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// 25 % resolution framebuffer
		////////////////////////////////////////////////////////////////////////////////////////////

		// Create the main render Framebuffer & Renderbuffer
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[3]);
		glBindRenderbuffer(GL_RENDERBUFFER, rboID[3]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, (int)(FRAMEBUFFER_WIDTH * 0.25f), (int)(FRAMEBUFFER_HEIGHT * 0.25f));

		// Generate the texture on which FBO will render to
		glBindTexture(GL_TEXTURE_2D, fboTexIDs[3]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, (int)(FRAMEBUFFER_WIDTH * 0.25f), (int)(FRAMEBUFFER_HEIGHT * 0.25f), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexIDs[3], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID[3]);

		mainFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(mainFboStatus != GL_FRAMEBUFFER_COMPLETE)
		{
			Log.e(TAG, "Main Framebuffer (25): ERROR");
		}

		isLoaded = true;
		isPlaying = sharedPreferences.getBoolean("SavedGame", false);

		if(isPlaying)
		{
			Log.w(TAG, "There was a saved game ... loading state");
			loadState();
			Log.w(TAG, "Finished loading state ... pausing game");
			isSavedGame = true;
		}
		else
		{
			isSavedGame = false;
			Log.w(TAG, "No previous saved game");
		}

		startTime = System.nanoTime();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		parent.dismissDialog();
	}

	@Override
	public void onDrawFrame(GL10 unused)
	{
		glClearColor(lightInfo.backColor[0], lightInfo.backColor[1], lightInfo.backColor[2], 1.0f);

		long endTime = System.nanoTime() - startTime;
		float deltaTime = ((float)(endTime/1000))*0.000001f;

		if(isSavedGame)
		{
			rendererState = RENDERER_STATE_PLAYING;
			setPause(true);
			mainMenu.setNotVisible();
			//hud.setAppearing();
			update(0f);
			isSavedGame = false;
		}

		startTime = System.nanoTime();

		// Clear everything
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if(rendererState == RENDERER_STATE_RESUMING)
		{
			resumeTimer += deltaTime;
			if(resumeTimer >= PLAYER_RESUMING_TIME)
			{
				isPaused = false;
				rendererState = RENDERER_STATE_PLAYING;
			}
		}

		if(!isPaused)//deltaTime=0;
			update(deltaTime);

		hud.updateOther(deltaTime);
		mainMenu.update(deltaTime);
		optionsMenu.update(deltaTime, (float)currentFPS);
		creditsMenu.update(deltaTime);
		pauseMenu.update(deltaTime);
		endGameMenu.update(deltaTime);
		restartMenu.update(deltaTime);
		gameOverMenu.update(deltaTime);
		howToPlayMenu.update(deltaTime);

		shadowMapPass();
		reflectionPass();
		shadingPass();
		postProcessPass();

		currentFPS = fpsCounter.framesFromLastDeltas(deltaTime);//fpsCounter.countFrames();
	}


	private void update(float deltaTime)
	{
		lightInfo.update(deltaTime);

		multiplyMM(shadowBiasProjection, 0, shadowBias, 0, lightInfo.projection, 0);
		multiplyMM(shadowMatrix, 0, shadowBiasProjection, 0, lightInfo.view, 0);

		setLookAtM(view, 0, eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, 0f, 1f, 0f);
		multiplyMM(viewProjection, 0, projection, 0, view, 0);

		// Update View-Projection matrix on objects
		playerRock.update(viewProjection, deltaTime, ground.getNearestGroundPatchType());
		ground.update(viewProjection, shadowViewProjection, playerRock.getDisplacementVec3(), shadowMatrix, shadowMapTexID[0], deltaTime);
		skyDome.update(viewProjection);

		// score multiplier
		if(playerRock.state == PLAYER_ROCK_MOVING)
		{
			scoreMultiplierTime += deltaTime;
			scoreMultiplierPercent = scoreMultiplierTime * 0.5f;
			if (scoreMultiplierTime > 2f)
			{
				scoreMultiplierTime -= 2f;
				playerRock.incrementMultiplier();
			}
		}
		else if(playerRock.state == PLAYER_ROCK_RECOVERING || playerRock.state == PLAYER_ROCK_BOUNCING)
		{
			scoreMultiplierPercent = 0f;
			playerRock.scoreMultiplier = 0f;
			scoreMultiplierTime = 0f;
		}

		// Score
		float fScoreIncrement = playerRock.getDisplacementVec3().z * playerRock.scoreMultiplier;
		fScore += fScoreIncrement; //playerRock.getDisplacementVec3().z * playerRock.scoreMultiplier;

		// Update hud
		hud.update(fScore, fScoreIncrement, (int)(playerRock.scoreMultiplier*10), scoreMultiplierPercent, deltaTime);
		groundShield.update(deltaTime);

		// Game state
		int previousState = currentState;
		currentState = playerRock.state;
		if(previousState == PLAYER_ROCK_MOVING && currentState == PLAYER_ROCK_BOUNCING)
		{
			if(hud.hit(playerRock.lastObjectTypeHit))
				groundShield.hit();
		}
	}


	private void reflectionPass()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, reflectionFboID[0]);
		glDrawBuffers(GL_DRAW_FRAMEBUFFER, reflectionDrawBuffers, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, REFLECTION_MAP_WIDTH, REFLECTION_MAP_HEIGHT);

		glDisable(GL_CULL_FACE);
		playerRock.drawReflectionProxy(shadowMatrix, shadowMapTexID[0]);
		ground.drawReflections();
		glEnable(GL_CULL_FACE);
		skyDome.drawReflectionProxy();


		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}


	private void shadowMapPass()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFboID[0]);
		glClear(GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT);
		glCullFace(GL_FRONT);

		playerRock.drawShadowMap();
		ground.drawShadowMap(shadowViewProjection);

		glCullFace(GL_BACK);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);
	}


	private void shadingPass()
	{
		glViewport(0, 0, renderWidth, renderHeight);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[currentResolution]);
		glDrawBuffers(GL_DRAW_FRAMEBUFFER, drawBuffers, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glDepthFunc(GL_LEQUAL);
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);

		ground.draw(reflectionTexID[0], framebufferDimensions);

		glEnable(GL_CULL_FACE);

		skyDome.draw();
		playerRock.draw(shadowMatrix, shadowMapTexID[0]);

		if(groundShield.isVisible)
		{
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE);
			groundShield.draw(viewProjection);
			glDisable(GL_BLEND);
		}

		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}


	private void postProcessPass()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, (int)screenWidth, (int)screenHeight);

		screen.draw(fboTexIDs[currentResolution], playerRock.currentSpeed * MAX_PLAYER_SPEED_FACTOR);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		hud.draw();
		mainMenu.draw();
		optionsMenu.draw();
		creditsMenu.draw();
		pauseMenu.draw();
		endGameMenu.draw();
		restartMenu.draw();
		gameOverMenu.draw();
		howToPlayMenu.draw();
		glDisable(GL_BLEND);
	}

	public void releaseTouch()
	{
		if(isLoaded)
		{
			playerRock.releaseTouch();
			mainMenu.releaseTouch();
			pauseMenu.releaseTouch();
			howToPlayMenu.releaseTouch();
		}
	}

	public void touch(float x, float y)
	{
		float newX = (x * 2.0f) - screenWidth;
		newX = newX * 0.5f;
		float newY = (screenHeight - y) * 2.0f - screenHeight;
		newY = newY * 0.5f;

		if(isLoaded)
		{
			if(rendererState == RENDERER_STATE_PLAYING)
			{
				if(!hud.touch(newX, newY))
				{
					if(newX < 0)
					{
						playerRock.turnLeft();
					}
					else
					{
						playerRock.turnRight();
					}
				}
				else
				{
					pauseMenu.setAppearing();
					rendererState = RENDERER_STATE_PAUSE_MENU;
				}
			}
			else if(rendererState == RENDERER_STATE_PAUSE_MENU)
			{
				pauseMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_MAIN_MENU)
			{
				mainMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_OPTIONS_MENU)
			{
				optionsMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_CREDITS_MENU)
			{
				creditsMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_END_GAME_MENU)
			{
				endGameMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_RESTART_MENU)
			{
				restartMenu.touch(newX, newY);
			}
			else if(rendererState == RENDERER_STATE_GAME_OVER_MENU)
			{
				gameOverMenu.touch();
			}
			else if(rendererState == RENDERER_STATE_HOW_TO_PLAY_MENU)
			{
				howToPlayMenu.touch(newX, newY);
			}
		}
	}

	public void newGame()
	{
		setDefaultMenuPreferences();

		rendererState = RENDERER_STATE_PLAYING;
		isPaused = false;
		isPlaying = true;
		ground.newGame();
		hud.newGame();
		hud.setAppearing();

		playerRock.newGame(ground.getNearestGroundPatchType());
		playerRock.setAppearing();
		groundShield.newGame();
		fScore = 0f;
	}

	public void endGame()
	{
		isPlaying = false;
		playerRock.endGame();
		hud.setDisappearing();
		hud.endGame();
		groundShield.endGame();
	}

	public void gameOver()
	{
		isPlaying = false;
		playerRock.endGame();
		hud.setDisappearing();

		int highScore = sharedPreferences.getInt("LocalHighScore",0);

		if((int)fScore > highScore)
		{
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt("LocalHighScore", (int)fScore);
			editor.apply();

			rendererState = RENDERER_STATE_CHANGING_MENU;
			gameOverMenu.setAppearing((int)fScore, true);
		}
		else
		{
			rendererState = RENDERER_STATE_CHANGING_MENU;
			gameOverMenu.setAppearing((int)fScore, false);
		}

	}

	public void changingToCreditsMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		creditsMenu.setAppearing();
	}

	public void changingToHowToPlayMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		howToPlayMenu.setAppearing();
	}

	public void changingToEndGameMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		endGameMenu.setAppearing();
	}

	public void changingToRestartMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		restartMenu.setAppearing();
	}

	public void changedToCreditsMenu()
	{
		rendererState = RENDERER_STATE_CREDITS_MENU;
	}

	public void changedToEndGameMenu()
	{
		rendererState = RENDERER_STATE_END_GAME_MENU;
	}

	public void changedToGameOverMenu()
	{
		rendererState = RENDERER_STATE_GAME_OVER_MENU;
	}

	public void changedToMainMenu()
	{
		rendererState = RENDERER_STATE_MAIN_MENU;
	}

	public void changedToOptionMenu()
	{
		rendererState = RENDERER_STATE_OPTIONS_MENU;
	}

	public void changedToPauseMenu()
	{
		rendererState = RENDERER_STATE_PAUSE_MENU;
	}

	public void changedToRestartMenu()
	{
		rendererState = RENDERER_STATE_RESTART_MENU;
	}

	public void changedToHowToPlayMenu()
	{
		rendererState = RENDERER_STATE_HOW_TO_PLAY_MENU;
	}

	public void changingToOptionsMenuFromMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		optionsMenu.setAppearing(MAIN_MENU);
	}

	public void changingToOptionsMenuFromPauseMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		optionsMenu.setAppearing(PAUSE_MENU);
	}

	public void changingFromCreditsMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		mainMenu.setAppearing();
	}

	public void changingFromEndGameMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		mainMenu.setAppearing();
		isPaused = false;
	}

	public void changingFromEndGameMenuToPauseMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		pauseMenu.setAppearing();
	}

	public void changingFromGameOverMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		mainMenu.setAppearing();
	}

	public void changingFromHowToPlayMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		mainMenu.setAppearing();
	}

	public void changingFromOptionsMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		mainMenu.setAppearing();
	}

	public void changingFromOptionsMenuToPauseMenu()
	{
		rendererState = RENDERER_STATE_CHANGING_MENU;
		pauseMenu.setAppearing();
	}

	public void changedFromCreditsMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_MAIN_MENU;
	}

	public void changedFromEndGameMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_MAIN_MENU;
	}

	public void changedFromEndGameMenuToPauseMenu()
	{
		rendererState = RENDERER_STATE_PAUSE_MENU;
	}

	public void changedFromOptionsMenuToMainMenu()
	{
		rendererState = RENDERER_STATE_MAIN_MENU;
	}


	public void setResolution25()
	{
		currentResolution = 3;
		renderWidth = (int)(screenWidth * 0.25f);
		renderHeight = (int)(screenHeight * 0.25f);
		framebufferDimensions[0] = renderWidth;
		framebufferDimensions[1] = renderHeight;
	}


	public void setResolution50()
	{
		currentResolution = 2;
		renderWidth = (int)(screenWidth * 0.5f);
		renderHeight = (int)(screenHeight * 0.5f);
		framebufferDimensions[0] = renderWidth;
		framebufferDimensions[1] = renderHeight;
	}


	public void setResolution75()
	{
		currentResolution = 1;
		renderWidth = (int)(screenWidth * 0.75f);
		renderHeight = (int)(screenHeight * 0.75f);
		framebufferDimensions[0] = renderWidth;
		framebufferDimensions[1] = renderHeight;
	}


	public void setResolution100()
	{
		currentResolution = 0;
		renderWidth = (int)screenWidth;
		renderHeight = (int)screenHeight;
		framebufferDimensions[0] = renderWidth;
		framebufferDimensions[1] = renderHeight;
	}


	public void setNoPostProcessDetail()
	{
		screen.setNoPostProcess();
	}


	public void setLowPostProcessDetail()
	{
		screen.setLowPostProcess();
	}


	public void setHighPostProcessDetail()
	{
		screen.setHighPostProcess();
	}


	public void setPause(boolean value)
	{
		if(!value)
		{
			rendererState = RENDERER_STATE_RESUMING;
			resumeTimer = 0f;
			hud.setPauseButtonVisible();
			hud.resume();
		}
		else
		{
			if(rendererState == RENDERER_STATE_PLAYING)
			{
				hud.setPauseButtonNotVisible();
				pauseMenu.setAppearing();
				isPaused = true;
			}
		}
	}


	public void setMenuSpeed(boolean value)
	{
		playerRock.setNotVisibleSpeed(value);
	}


	public void setDefaultMenuPreferences()
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean("SpeedEnabled", false);
		editor.putBoolean("VisibilityEnabled", true);
		editor.apply();
	}


	public void onDestroy()
	{
		// Preferences (default values for menu preferences)
		setDefaultMenuPreferences();

		// State (save the state of the current game)
		String string = "SAVED_GAME "
				+ fScore + " " +
				+ lightInfo.timeOfDay + "\n";

		if(isPlaying)
		{
			try
			{
				FileOutputStream outputStream = context.openFileOutput(SAVED_GAME_FILE_NAME, Context.MODE_PRIVATE);
				outputStream.write(string.getBytes());

				playerRock.saveState(outputStream);
				groundShield.saveState(outputStream);
				hud.saveState(outputStream);
				ground.saveState(outputStream);

				outputStream.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			Log.w(TAG, "is playing - game saved");
		}
		else
		{
			Log.w(TAG, "is not playing - no game saved");
		}

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("SavedGame", isPlaying);
		editor.apply();
	}


	public void loadState()
	{
		String line;
		String[] tokens;

		try
		{
			FileInputStream fileInputStream = context.openFileInput(SAVED_GAME_FILE_NAME);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			line = bufferedReader.readLine();
			tokens = line.split(" ");
			fScore = Float.parseFloat(tokens[1]);
			lightInfo.timeOfDay = Float.parseFloat(tokens[2]);

			playerRock.loadState(bufferedReader);
			currentState = playerRock.state;

			groundShield.loadState(bufferedReader);
			hud.loadState(bufferedReader);
			ground.loadState(bufferedReader);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	public void onBackPressed()
	{
		if(rendererState == RENDERER_STATE_OPTIONS_MENU)
		{
			optionsMenu.onBackPressed();
		}
		else if(rendererState == RENDERER_STATE_HOW_TO_PLAY_MENU)
		{
			howToPlayMenu.onBackPressed();
		}
		else if(rendererState == RENDERER_STATE_CREDITS_MENU)
		{
			creditsMenu.onBackPressed();
		}
		else if(rendererState == RENDERER_STATE_PLAYING)
		{
			setPause(true);
		}
		else if(rendererState == RENDERER_STATE_RESTART_MENU)
		{
			restartMenu.onBackPressed();
		}
		else if(rendererState == RENDERER_STATE_END_GAME_MENU)
		{
			endGameMenu.onBackPressed();
		}
		else if(rendererState == RENDERER_STATE_GAME_OVER_MENU)
		{
			gameOverMenu.touch();
		}
		else
		{
			parent.exit();
		}
	}
}