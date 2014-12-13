package com.josepmtomas.rockgame;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.Log;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.objectsForwardPlus.Ground;
import com.josepmtomas.rockgame.objectsForwardPlus.Hud;
import com.josepmtomas.rockgame.objectsForwardPlus.LightInfo;
import com.josepmtomas.rockgame.objectsForwardPlus.PlayerRock;
import com.josepmtomas.rockgame.objectsForwardPlus.Screen;
import com.josepmtomas.rockgame.objectsForwardPlus.SkyDome;
import com.josepmtomas.rockgame.objectsForwardPlus.TestTree;
import com.josepmtomas.rockgame.objectsForwardPlus.TestUI;
import com.josepmtomas.rockgame.util.FPSCounter;
import com.josepmtomas.rockgame.util.PerspectiveCamera;
import com.josepmtomas.rockgame.util.TouchState;

import static com.josepmtomas.rockgame.Constants.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 05/09/2014.
 */
public class ForwardPlusRenderer implements Renderer
{
	private static final String TAG = "Forward+";

	private static final int SHADOWMAP_SIZE = 256;

	private int FRAMEBUFFER_WIDTH = 1280; // 1794
	private int FRAMEBUFFER_HEIGHT = 720;	//1104
	private int REFLECTION_MAP_WIDTH = 320;
	private int REFLECTION_MAP_HEIGHT = 180;
	private int SHADOW_MAP_WIDTH = 512;
	private int SHADOW_MAP_HEIGHT = 512;

	private float FRAMEBUFFER_ASPECT_RATIO = 0.5625f;

	private float[] framebufferDimensions = new float[2];

	private int renderWidth;			// Screen width resolution
	private int renderHeight;			// Screen height resolution
	private float renderAspectRatio;	// Screen aspect ratio (width / height)

	// Camera
	private PerspectiveCamera perspectiveCamera;
	//private vec3 eyePos = new vec3(0.0f, 25.0f, 50.0f);
	private vec3 eyePos = new vec3(0.0f, 25.0f, 50.0f);
	private vec3 eyeLook = new vec3(0.0f, 20.0f, 0.0f);
	private float near = 0.1f;
	private float far = 1200.0f;	// 1200.0
	private float fov = 60;
	private float xRotation = 0.0f;
	private float yRotation = 0.0f;

	// Main render matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Main framebuffer objects
	private final int[] fboID = new int[1];
	private final int[] rboID = new int[1];
	private final int[] fboTexID = new int[1];
	private final int[] drawBuffers = {GL_COLOR_ATTACHMENT0};

	// Reflection framebuffer
	private final int[] reflectionFboID = new int[1];
	private final int[] reflectionRboID = new int[1];
	private final int[] reflectionTexID = new int[1];
	private final int[] reflectionDrawBuffers = {GL_COLOR_ATTACHMENT0};

	// Shadow map framebuffer
	private final int[] shadowMapFboID = new int[1];
	private final int[] shadowMapRboID = new int[1];
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
	private int FPSCap = 20;
	private long framePeriodNS = 1000000000 / FPSCap;
	private FPSCounter fpsCounter;

	// TIME
	private long startTime = 0;
	private long endTime = 0;
	private float deltaTime = 0;
	private long timeToFrame = 0;
	private long updateTime = 0;
	private long drawTime = 0;

	// Objects
	PlayerRock playerRock;
	Ground ground;
	SkyDome skyDome;

	TestTree testTree;

	Screen screen;
	Hud hud;

	LightInfo lightInfo;

	// Multiplier
	private float scoreMultiplierValue = 1;
	private float scoreMultiplierTime = 0;
	private float scoreMultiplierPercent = 0;

	// State
	private TouchState leftTouchState = TouchState.NOT_TOUCHING;
	private TouchState rightTouchState = TouchState.NOT_TOUCHING;

	// Score
	private float fScore = 0f;
	private int iScore = 0;

	// GameActivity
	private GameActivity parent;


	public ForwardPlusRenderer(GameActivity parent, float width, float height, float resolutionPercentage)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();

		this.FRAMEBUFFER_WIDTH = (int)(width * resolutionPercentage);
		this.FRAMEBUFFER_HEIGHT = (int)(height * resolutionPercentage);
		framebufferDimensions[0] = FRAMEBUFFER_WIDTH;
		framebufferDimensions[1] = FRAMEBUFFER_HEIGHT;

		//this.context = context;
		this.renderWidth = (int)width;
		this.renderHeight = (int)height;
		this.renderAspectRatio = width / height;

		fpsCounter = new FPSCounter();
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig)
	{
		// GL init
		//glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Camera set up
		perspectiveCamera = new PerspectiveCamera(eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, near, far, renderAspectRatio, fov);
		setLookAtM(view, 0, 0f, 10f, 100f, 0f, 0f, 0f, 0f, 1f, 0f);
		perspectiveM(projection, 0, fov, renderAspectRatio, near, far);

		glEnable(GL_CULL_FACE);
		//glCullFace(GL_BACK);

		lightInfo = new LightInfo();
		// Create and initialize objects
		playerRock = new PlayerRock(parent, lightInfo);
		ground = new Ground(context,
				11, 9, 10, 10, 90f, 90f,
				3, 3, 450f, 450f,
				perspectiveCamera, lightInfo);
		ground.setPlayerRock(playerRock);
		ground.setPerspectiveCamera(perspectiveCamera); //TODO: delete
		skyDome = new SkyDome(context, lightInfo);
		testTree = new TestTree(context);

		screen = new Screen(context, 1, 1);
		//hud = new Hud(context, renderWidth, renderHeight);
		hud = new Hud(context, renderWidth, renderHeight, 107f, 150f);




		int[] result = new int[3];
		glGetIntegerv(GL_MAX_VERTEX_UNIFORM_BLOCKS, result, 0);
		glGetIntegerv(GL_MAX_UNIFORM_BLOCK_SIZE, result, 1);
		glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, result, 2);

		Log.i(TAG, "Max uniform blocks in vertex shader = " + result[0] + " binding locations");
		Log.i(TAG, "Max uniform block size = " + result[1] + " Bytes / " + (result[1]/Constants.BYTES_PER_FLOAT) + " Floats / " + ((result[1]/Constants.BYTES_PER_FLOAT)/16) + " Matrices(mat4)");
		Log.i(TAG, "Max texture image units = " + result[2]);


		/**************************************** REFLECTION ***************************************/

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


		/********************************** SHADOW MAP FRAMEBUFFER ********************************/

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


		/************************************ SHADOW MAP MATRICES *********************************/

		setLookAtM(shadowView, 0, 150f, 150f, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		//setLookAtM(shadowView, 0, 300f, 200f, -200f, 0f, 0f, -200f, 0f, 1f, 0f);
		//perspectiveM(shadowProjection, 0, 90f, 1f, 0.1f, 500f);
		orthoM(shadowProjection, 0, -500f, 500f, -500f, 500f, 0.5f, 1000f);
		//TODO: orthoM(shadowPerspective, 0, );
		multiplyMM(shadowViewProjection, 0, shadowProjection, 0, shadowView, 0);

		/*this.shadowView = lightInfo.view;
		this.shadowProjection = lightInfo.projection;
		this.shadowViewProjection = lightInfo.viewProjection;*/

		setIdentityM(shadowBias, 0);
		translateM(shadowBias, 0, 0.5f, 0.5f, 0.5f);
		scaleM(shadowBias, 0, 0.5f, 0.5f, 0.5f);

		multiplyMM(shadowBiasProjection, 0, shadowBias, 0, shadowProjection, 0);
		multiplyMM(shadowMatrix, 0, shadowBiasProjection, 0, shadowView, 0);


		/********************************** MAIN RENDER FRAMEBUFFER *******************************/

		// Create the main render Framebuffer & Renderbuffer
		glGenFramebuffers(1, fboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[0]);
		glGenRenderbuffers(1, rboID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, rboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

		// Generate the texture on which FBO will render to
		glGenTextures(1, fboTexID, 0);
		glBindTexture(GL_TEXTURE_2D, fboTexID[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexID[0], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID[0]);

		int mainFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(mainFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Main Framebuffer: OK");
		}
		else
		{
			Log.e(TAG, "Main Framebuffer: ERROR");
		}

		startTime = System.nanoTime();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height)
	{

	}

	@Override
	public void onDrawFrame(GL10 unused)
	{
		glClearColor(lightInfo.backColor[0], lightInfo.backColor[1], lightInfo.backColor[2], 1.0f);

		endTime = System.nanoTime() - startTime;
		deltaTime = ((float)(endTime/1000))*0.000001f;
		//Log.w("CurrentFrame", " "+ endTime/1000000 + " ms");

		//Log.w("DeltaTime", " = " + deltaTime + " s");

		startTime = System.nanoTime();
		// Clear everything
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		/*startTime = SystemClock.elapsedRealtime();
		update();
		endTime = SystemClock.elapsedRealtime();
		updateTime += endTime - startTime;

		startTime = SystemClock.elapsedRealtime();*/
		update(deltaTime);
		shadowMapPass();
		reflectionPass();
		//lightCullingPass();
		glViewport(0, 0, renderWidth, renderHeight);
		depthPrePass();
		shadingPass();
		postProcessPass();
		//endTime = SystemClock.elapsedRealtime();
		//drawTime += endTime - startTime;

		/*timeToFrame = framePeriodNS - (endTime - startTime);
		SystemClock.sleep(timeToFrame/1000000);
		fpsCounter.logFrame();*/

		currentFPS = fpsCounter.logFrameWithAverage();

		/*if(fpsCounter.logFrame(updateTime, drawTime))
		{
			updateTime = 0;
			drawTime = 0;
		}*/
	}


	private void update(float deltaTime)
	{
		lightInfo.update(deltaTime);

		multiplyMM(shadowBiasProjection, 0, shadowBias, 0, lightInfo.projection, 0);
		multiplyMM(shadowMatrix, 0, shadowBiasProjection, 0, lightInfo.view, 0);

		setLookAtM(view, 0, eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, 0f, 1f, 0f);
		rotateM(view, 0, yRotation, 1f, 0f, 0f);
		rotateM(view, 0, xRotation, 0f, 1f, 0f);

		multiplyMM(viewProjection, 0, projection, 0, view, 0);

		/*rotateM(shadowView, 0, 10f, 0f, 1f, 0f);
		multiplyMM(shadowViewProjection, 0, shadowProjection, 0, shadowView, 0);
		multiplyMM(shadowMatrix, 0, shadowBiasProjection, 0, shadowView, 0);*/

		// Update View-Projection matrix on objects
		playerRock.update(viewProjection, deltaTime);
		playerRock.updateLightMatrices(shadowViewProjection);
		ground.update(viewProjection, shadowViewProjection, playerRock.getDisplacementVec3(), shadowMatrix, shadowMapTexID[0], deltaTime);
		skyDome.update(viewProjection);
		testTree.update(viewProjection);

		////////////

		if(leftTouchState.isTouching())
		{
			//displacement.setValues(1f, 0.0f, playerRock.getDisplacement());
			////displacement.setValues(playerRock.getDisplacement());
			playerRock.turnLeft();
		}
		else if(rightTouchState.isTouching())
		{
			//displacement.setValues(-1f, 0.0f, playerRock.getDisplacement());
			////displacement.setValues(playerRock.getDisplacement());
			playerRock.turnRight();
		}
		else
		{
			//displacement.setValues(0.0f, 0.0f, playerRock.getDisplacement());
			////displacement.setValues(playerRock.getDisplacement());
			playerRock.releaseTouch();
		}

		// score multiplier
		if(playerRock.state == PLAYER_ROCK_MOVING)
		{
			scoreMultiplierTime += deltaTime;
			scoreMultiplierPercent = scoreMultiplierTime * 0.5f;
			if (scoreMultiplierTime > 2f)
			{
				scoreMultiplierTime -= 2f;
				//scoreMultiplierValue += 0.1f;
				playerRock.scoreMultiplier += 0.1f;
			}
		}
		else if(playerRock.state == PLAYER_ROCK_RECOVERING)
		{
			scoreMultiplierPercent = 0f;
			//scoreMultiplierValue = 0f;
			playerRock.scoreMultiplier = 0f;
			scoreMultiplierTime = 0f;
		}

		// Score
		fScore += playerRock.getDisplacementVec3().z * playerRock.scoreMultiplier;

		// Update hud
		hud.update((int)fScore, (int)(playerRock.scoreMultiplier*10), scoreMultiplierPercent, currentFPS);
	}


	private void reflectionPass()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, reflectionFboID[0]);
		glDrawBuffers(GL_DRAW_FRAMEBUFFER, reflectionDrawBuffers, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, REFLECTION_MAP_WIDTH, REFLECTION_MAP_HEIGHT);

		//TODO: render reflection proxies
		glDisable(GL_CULL_FACE);
		playerRock.drawReflectionProxy(shadowMatrix, shadowMapTexID[0]);
		ground.drawReflections();
		glEnable(GL_CULL_FACE);
		skyDome.drawReflectionProxy();


		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}


	private void lightCullingPass()
	{

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


	private void depthPrePass()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboID[0]);
		glDrawBuffers(GL_DRAW_FRAMEBUFFER, drawBuffers, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

		// Z pre-pass
		glEnable(GL_DEPTH_TEST);	// We want depth test
		glDepthFunc(GL_LESS);		// We want the nearest pixels
		glColorMask(false, false, false, false);	//Disable color, it's useless, we only want depth
		glDepthMask(true);			// Ask z writing

		//int[] buffers = {GL_DEPTH_ATTACHMENT};
		//glDrawBuffers(1, buffers, 0);

		//TODO: draw depth pre-pass
		playerRock.drawDepthPrePass();
		//ground.drawDepthPrePass();
	}


	private void shadingPass()
	{
		// Real render
		//glEnable(GL_DEPTH_TEST);	// We still want depth test
		glDepthFunc(GL_LEQUAL);		// Only draw pixels if they are the closest ones
		glColorMask(true, true, true, true);	// We want color this time
		//glDepthMask(false);			// Writing the z component is useless now, we already have it
		glDepthMask(false);

		//int[] buffers = {GL_COLOR_ATTACHMENT0};
		//glDrawBuffers(1, buffers, 0);

		//glDepthMask(false);
		//glColorMask(true, true, true, true);
		//glDepthFunc(GL_LEQUAL);

		//glEnable(GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		//TODO: draw scene normally
		//playerRock.draw(shadowMatrix, shadowMapTexID[0]);
		/////////ground.draw(reflectionTexID[0], framebufferDimensions);


		/**glDepthMask(true);
		testTree.drawTrunk();

		//glDisable(GL_DEPTH_TEST);
		//glDepthMask(false);
		glDisable(GL_CULL_FACE);
		//glEnable(GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		testTree.drawLeaves();
		glEnable(GL_CULL_FACE);
		glDepthMask(true);**/

		glEnable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(true);

		/**float treeScale = 100f;
		vec2 point;
		for(int i=0; i < sampler.points.size(); i++)
		{
			point = sampler.points.get(i);
			testTree.drawAtPosition(point.x*treeScale, 0.0f, point.y*treeScale);
		}**/

		ground.draw(reflectionTexID[0], framebufferDimensions);

		glEnable(GL_CULL_FACE);

		skyDome.draw();
		playerRock.draw(shadowMatrix, shadowMapTexID[0]);

		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}


	private void postProcessPass()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, renderWidth, renderHeight);

		screen.draw(fboTexID[0], playerRock.currentSpeed * MAX_PLAYER_SPEED_FACTOR);
		//screen.draw(reflectionTexID[0]);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		hud.draw();
		glDisable(GL_BLEND);
	}


	public void pressLeft()
	{
		leftTouchState = TouchState.TOUCHING;
		rightTouchState = TouchState.NOT_TOUCHING;
	}

	public void pressRight()
	{
		leftTouchState = TouchState.NOT_TOUCHING;
		rightTouchState = TouchState.TOUCHING;
	}

	public void releaseTouch()
	{
		leftTouchState = TouchState.NOT_TOUCHING;
		rightTouchState = TouchState.NOT_TOUCHING;
	}

	public void scroll()
	{
		//playerRock.hit();
	}


	public void handleTouchDrag(float deltaX, float deltaY)
	{
		/*xRotation += deltaX / 10f;
		yRotation += deltaY / 10f;

		if(yRotation < -90) yRotation = -90;
		else if(yRotation > 90) yRotation = 90;*/

		//playerRock.currentPositionY += deltaY;
	}


	public void deleteGL()
	{
		skyDome.deleteGL();
		ground.deleteGL();
		playerRock.deleteGL();

		// Framebuffers
		glDeleteFramebuffers(1, fboID, 0);
		glDeleteFramebuffers(1, reflectionFboID, 0);
		glDeleteFramebuffers(1, shadowMapFboID, 0);

		// Renderbuffers
		glDeleteRenderbuffers(1, rboID, 0);
		glDeleteRenderbuffers(1, reflectionRboID, 0);
		glDeleteRenderbuffers(1, shadowMapRboID, 0);

		// Textures
		glDeleteTextures(1, fboTexID, 0);
		glDeleteTextures(1, reflectionTexID, 0);
		glDeleteTextures(1, shadowMapTexID, 0);
	}


}

