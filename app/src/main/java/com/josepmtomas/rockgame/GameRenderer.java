package com.josepmtomas.rockgame;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView.Renderer;
import android.util.FloatMath;
import android.util.Log;

import static com.josepmtomas.rockgame.algebra.operations.*;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.objects.Ball;
import com.josepmtomas.rockgame.objects.Grass;
import com.josepmtomas.rockgame.objects.GroundManager;
import com.josepmtomas.rockgame.objects.Rock;
import com.josepmtomas.rockgame.objectsES30.Mirror;
import com.josepmtomas.rockgame.objectsES30.PlayerRock;
import com.josepmtomas.rockgame.objectsES30.VAOMesh;
import com.josepmtomas.rockgame.objectsES30.Ground;
import com.josepmtomas.rockgame.util.FPSCounter;
import com.josepmtomas.rockgame.util.PerspectiveCamera;
import com.josepmtomas.rockgame.util.TouchState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;



/**
 * Created by Josep on 16/07/2014.
 */
public class GameRenderer implements Renderer
{
	private final static String TAG = "GameRenderer";

	private int width = 0;
	private int height = 0;

	private TouchState leftTouchState = TouchState.NOT_TOUCHING;
	private TouchState rightTouchState = TouchState.NOT_TOUCHING;

	private FPSCounter fpsCounter = new FPSCounter();

	private Context context;
	private Rock rock;
	private Ball ball;
	private GroundManager groundManager;
	private Grass grass;
	private Ground ground;

	private PlayerRock playerRock;
	private Mirror mirror;

	private VAOMesh vaoMesh;

	private final float[] projectionMatrix = new float[16];
	private final float[] modelMatrix = new float[16];
	private final float[] viewMatrix = new float[16];
	private final float[] modelViewMatrix = new float[16];
	private final float[] viewProjectionMatrix = new float[16];
	private final float[] modelViewProjectionMatrix = new float[16];

	private vec3 displacement = new vec3(0.0f);
	private float[] playerDisplacement;


	// Camera
	private float fov = 80.0f;
	private float aspect;
	private float near = 1;
	private float far = 1000;
	private vec3 eyePos = new vec3(0f,30f,100f);
	private vec3 eyePos2 = new vec3(0f,30f,100f);
	private vec3 eyeLook = new vec3(0f,0f,0f);
	private float[] eyePosV = new float[4];
	private float[] eyeLookV = new float[4];
	private PerspectiveCamera perspectiveCamera;

	private vec3 lightPos = new vec3(100f, 100f, 0f);

	private float modelRotation = 0;
	private float xRotation = 0;
	private float yRotation = 0;

	private float time = 0.0f;

	// Shadows stuff
	private float[] border = {1f, 0f, 0f, 0f};
	private int[] shadowMapTexID = new int[1];
	private int[] fboID = new int[1];
	private static final int SHADOWMAP_WIDTH = 512;
	private static final int SHADOWMAP_HEIGHT = 512;
	private float[] MV_L = new float[16];
	private float[] P_L = new float[16];
	private float[] B = new float[16];
	private float[] BP = new float[16];
	private float[] S = new float[16];

	private int shadowFrame = 0;
	private int shadowTurn = 3;

	// Mirror stuff
	private int[] mirrorFboID = new int[1];		// Framebuffer
	private int[] mirrorRbID = new int[1];		// Renderbuffer
	private int[] mirrorTexID = new int[1];		// Texture
	private int[] mirrorDrawBuffers = {GL_COLOR_ATTACHMENT0};
	private static final int RENDERBUFFER_WIDTH = 256;
	private static final int RENDERBUFFER_HEIGHT = 256;

	private vec3 R = new vec3(0.0f);
	private float[] mirrorProjectionMatrix = new float[16];
	private float[] mirrorViewMatrix = new float[16];

	// Dynamic cubemap
	private static final int CUBEMAP_SIZE = 128;
	private int[] dynamicCubeMapTexID = new int[1];
	private int[] dynamicCubeMapFboID = new int[1];
	private int[] dynamicCubeMapRboID = new int[1];
	private float[] dynamicCubeMapProjetionMatrix = new float[16];
	private int cubeFrame = 0;


	/**********************************************************************************************/


	public GameRenderer(Context context, float width, float height)
	{
		this.context = context;

		this.aspect = width / height;
		this.width = (int)width;
		this.height = (int)height;

		perspectiveCamera = new PerspectiveCamera(eyePos, eyeLook, aspect, fov, near, far);
	}


	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
	{
		vaoMesh = new VAOMesh(context, com.josepmtomas.rockgame.R.raw.torusknot2);

		playerRock = new PlayerRock(context);
		playerRock.initialize();

		ground = new Ground(context, 9, 13, 10, 10, 100f, 100f);
		ground.setCamera(perspectiveCamera);

		mirror = new Mirror(context, 40.0f, 40.0f);
		mirror.setTranslation(0.0f, 20.0f, -20.0f);
		//mirror.setRotation(0.0f, -30.0f, 0.0f);
		mirror.initialize();

		/*Log.d(TAG, "Testing point inside frustum");
		if(perspectiveCamera.pointInFrustum(0f,0f,-50f))
			Log.d(TAG, "Point inside");
		else
			Log.e(TAG, "Point outside");*/

		glClearColor(0.2f, 0.2f, 0.2f, 0.0f);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);

		/** Shadowmap creation **/

		// Create an OpenGL texture object which will be our shadow map texture.
		glGenTextures(1, shadowMapTexID, 0);
		//glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, shadowMapTexID[0]);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		// Set up an FBO and use the shadow map texture as a single depth attachment. This will store
		// the scene's depth from the point of view of light

		glGenFramebuffers(1, fboID, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, fboID[0]);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMapTexID[0], 0);

		int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);

		if(fboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.i(TAG, "FBO setup successful.");
		}
		else
		{
			Log.e(TAG, "FBO setup failed.");
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		/************************/

		// Using the position and the direction of the light, set up the shadow matrix (S) by
		// combining the light modelview matrix (MV_L), projection matrix (P_L), and the bias matrix
		// (B). For reducing runtime calculation, we store the combined projection and bias matrix
		// (BP) at initialization.

		setLookAtM(MV_L, 0, lightPos.x, lightPos.y, lightPos.z, 0f, 0f, 0f, 0f, 1f, 0f);
		perspectiveM(P_L, 0, 90.0f, 1.0f, 1.0f, 500f);

		setIdentityM(B, 0);
		translateM(B, 0, 0.5f, 0.5f, 0.5f);
		scaleM(B, 0, 0.5f, 0.5f, 0.5f);

		multiplyMM(BP, 0, B, 0, P_L, 0);
		multiplyMM(S, 0, BP, 0, MV_L, 0);

		/**************** MIRROR *********************/

		// Initialize the framebuffer and renderbuffer objects' color and depth attachments
		// respectively. The render buffer is required if we need depth testing for the offscreen
		// rendering, and the depth precision is specified using the glRenderbufferStorage function.

		glGenFramebuffers(1, mirrorFboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mirrorFboID[0]);
		glGenRenderbuffers(1, mirrorRbID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, mirrorRbID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, RENDERBUFFER_WIDTH, RENDERBUFFER_HEIGHT);

		// Generate the offscreen texture on which FBO will render to. The last parameter of
		// glTexImage2D is NULL, which tells OpenGL that we de not have any content yet, please
		// provide a new block of GPU memory which gets filled when the FBO is used as a render
		// target.

		glGenTextures(1, mirrorTexID, 0);
		glBindTexture(GL_TEXTURE_2D, mirrorTexID[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, RENDERBUFFER_WIDTH, RENDERBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Attach Renderbuffer to the bound Framebuffer object and check for Framebuffer completeness
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mirrorTexID[0], 0);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mirrorRbID[0]);

		fboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(fboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Mirror FBO complete.");
		}
		else
		{
			Log.e(TAG, "Error in FBO setup.");
		}

		// Unbind the Framebuffer
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		/******************* DYNAMIC CUBE MAP ********************/

		perspectiveM(dynamicCubeMapProjetionMatrix, 0, 90f, 1f, 0.1f, 100f);

		// Create a cubemap texture object
		glGenTextures(1, dynamicCubeMapTexID, 0);
		glActiveTexture(GL_TEXTURE10);
		glBindTexture(GL_TEXTURE_CUBE_MAP, dynamicCubeMapTexID[0]);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGBA, CUBEMAP_SIZE, CUBEMAP_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

		//Set up an FBO with the cubemap texture as an attachment
		glGenFramebuffers(1, dynamicCubeMapFboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, dynamicCubeMapFboID[0]);
		glGenRenderbuffers(1, dynamicCubeMapFboID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, dynamicCubeMapRboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, CUBEMAP_SIZE, CUBEMAP_SIZE);
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, dynamicCubeMapRboID[0]);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X, dynamicCubeMapTexID[0],0);

		fboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(fboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Dynamic CubeMap FBO complete.");
		}
		else
		{
			Log.e(TAG, "Error in Dynamic CubeMap FBO setup.");
		}

	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		this.width = width;
		this.height = height;

		glViewport(0, 0, width, height);

		aspect = (float)width / (float)height;
	}

	@Override
	public void onDrawFrame(GL10 unused)
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		update();

		/*************** MIRROR ****************/
/*
		// change the view matrix to where the mirror is reflect the view vector in the mirror
		// normal direction
		multiplyMV(eyePosV, 0, viewMatrix, 0, eyePos.data4(), 0);
		multiplyMV(eyeLookV, 0, viewMatrix, 0, eyeLook.data4(), 0);

		vec3 tempPos = new vec3(eyePosV);
		tempPos.x = -tempPos.x;
		tempPos.y = -tempPos.y;
		vec3 tempLook = new vec3(eyeLookV);
		//R = reflectV(substract(tempPos, tempLook), mirror.getNormalVec3());

		R = reflectV(substract(eyePos2, eyeLook), mirror.getNormalVec3());

		//Log.d("tempPos","(" + tempPos.x + ", " + tempPos.y + ", " + tempPos.z + ")");
		//Log.d("eyePos2","(" + eyePos2.x + ", " + eyePos2.y + ", " + eyePos2.z + ")");
		//Log.d("R","(" + R.x + ", " + R.y + ", " + R.z + ")");
		//Log.d("R2","(" + R2.x + ", " + R2.y + ", " + R2.z + ")");

		//place the virtual camera at the mirror position
		vec3 mirrorPosition = mirror.getPositionVec3();
		//vec3 mirrorLook = add(mirrorPosition, mirror.getNormalVec3());
		vec3 mirrorLook = add(mirrorPosition, R);
		vec3 mirrorLookV = normalize(substract(mirrorLook, mirrorPosition));
		float mirrorNear = 10f;
		mirrorPosition.add(-mirrorLookV.x * mirrorNear , -mirrorLookV.y * mirrorNear, -mirrorLookV.z * mirrorNear);
		////mirrorLook = reflectV(negate(mirrorLook), mirror.getNormalVec3());
		//Log.d(TAG, "MirrorPos = (" + mirrorPosition.x + ", " + mirrorPosition.y + ", " + mirrorPosition.z + ")");
		//Log.d(TAG, "MirrorLook = (" + mirrorLook.x + ", " + mirrorLook.y + ", " + mirrorLook.z + ")");
		perspectiveM(mirrorProjectionMatrix,  0, fov, 1.0f, near, far);
		setLookAtM(mirrorViewMatrix, 0, mirrorPosition.x, mirrorPosition.y, mirrorPosition.z,
				mirrorLook.x, mirrorLook.y, mirrorLook.z, 0f, 1f, 0f);

		//since mirror image is laterally inverted, we multiply the MV matrix by (-1,1,1)
		scaleM(mirrorViewMatrix, 0, 1f, 1f, 1f);

		//enable FBO
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mirrorFboID[0]);
		//render to colour attachment 0
		glDrawBuffers(1, mirrorDrawBuffers, 0);
		//clear the colour and depth buffers
		glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, RENDERBUFFER_WIDTH, RENDERBUFFER_HEIGHT);

		//TODO: draw elements in reflection
		drawReflections(mirrorViewMatrix, mirrorProjectionMatrix);

		//unbind the FBO
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		//restore the default back buffer
		//glDrawBuffer(GL_BACK_LEFT);
*/
		/***********************************/

		if(cubeFrame == 0)
		{
			drawDynamicCubeMap();
		}
		cubeFrame++;
		cubeFrame =cubeFrame % 5;

		/***********************************/

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if(shadowFrame == 0)
		{
			// Bind the FBO and render the scene from the point of view of the light. Enable front-face
			// culling so that the back-face depth values are rendered.

			glBindFramebuffer(GL_FRAMEBUFFER, fboID[0]);
			glClear(GL_DEPTH_BUFFER_BIT);
			glViewport(0, 0, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);
			drawSceneLightPass(MV_L, P_L);
		}
		shadowFrame++;
		shadowFrame = shadowFrame % shadowTurn;

		// Disable FBO, restore default viewport, and render the scene normally from the point of
		// view of the camera

		glCullFace(GL_BACK);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, width, height);
		drawScene();

		/***********************************/

		/**vaoMesh.setMatrices(viewMatrix, projectionMatrix, time);
		vaoMesh.draw();**/

		//playerRock.draw();

		//ground.draw(viewProjectionMatrix, time);

		/******************************************************************************************/
		fpsCounter.logFrame();
	}


	private void drawSceneLightPass(float[] view, float[] projection)
	{
		glCullFace(GL_FRONT);
		ground.drawShadowMap(view, projection);
		playerRock.drawShadowMap(view, projection);

		glCullFace(GL_BACK);
		mirror.drawShadowPass(view, projection);
	}


	private void drawReflections(float[] view, float[] projection)
	{
		playerRock.draw(view, projection);
		ground.draw(view, projection, S, shadowMapTexID[0], time);
	}


	private void drawDynamicCubeMap()
	{
		float[] cubeMapViewMatrix = new float[16];

		// Set the viewport to the size of the offscreen texture and render the scene six times
		// without the reflective object to the six sides of the cubemap using FBO.
		glViewport(0, 0, CUBEMAP_SIZE, CUBEMAP_SIZE);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, dynamicCubeMapFboID[0]);

		// POSITIVE X
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, 1f, 10f, 0f, 0f, -1f, 0f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		// NEGATIVE X
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_NEGATIVE_X, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, -1f, 10f, 0f, 0f, -1f, 0f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		// POSITIVE Y
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_Y, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, 0f, 11f, 0f, 0f, 0f, -1f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		// NEGATIVE Y
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, 0f, 9f, 0f, 0f, 0f, -1f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		// POSITIVE Z
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_Z, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, 0f, 10f, 1f, 0f, -1f, 0f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		// NEGATIVE Z
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, dynamicCubeMapTexID[0], 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setLookAtM(cubeMapViewMatrix, 0, 0f, 10f, 0f, 0f, 10f, -1f, 0f, -1f, 0f);
		ground.draw(cubeMapViewMatrix, dynamicCubeMapProjetionMatrix, S, shadowMapTexID[0], time);

		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		// Restore the viewport
		glViewport(0, 0, width, height);
	}


	private void drawScene()
	{
		//Log.e("EyePos2", "(" + eyePos2.x + ", " + eyePos2.y + ", " + eyePos2.z + ")");
		playerRock.draw(dynamicCubeMapTexID[0], eyePos2);

		ground.draw(viewMatrix, projectionMatrix, S, shadowMapTexID[0], time);

		//mirror.draw(mirrorTexID[0]);
	}


	public void update()
	{
		modelRotation -= 1.0;
		//time += 0.025;
		time += 0.05;

		setIdentityM(modelMatrix, 0);
		//translateM(modelMatrix, 0, 0f, 10f, 0f);
		rotateM(modelMatrix, 0, modelRotation, 1f, 0f, 0f);
		//scaleM(modelMatrix, 0, 0.5f, 0.5f, 0.5f);
		perspectiveM(projectionMatrix, 0, fov, aspect, near, far);
		//setLookAtM(viewMatrix, 0, eyePos2.x, eyePos2.y, eyePos2.z, eyeLook.x, eyeLook.y, eyeLook.z, 0f, 1f, 0f);
		setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, 0f, 1f, 0f);
		rotateM(viewMatrix, 0, yRotation, 1f, 0f, 0f);
		rotateM(viewMatrix, 0, xRotation, 0f, 1f, 0f);

		/******************/
		//TODO: eyePos rotation

		eyePos2 = rotate(eyePos, new vec3(1f,0f,0f), yRotation);
		eyePos2 = rotate(eyePos2, new vec3(0f,1f,0f), xRotation);

		/******************/

		multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

		multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
		//multiplyMM(modelViewProjectionMatrix, 0, modelViewMatrix, 0, projectionMatrix, 0);


		//rock.update(modelMatrix, modelViewProjectionMatrix, eyePos);

		//ball.update(viewMatrix, projectionMatrix);

		/*setIdentityM(modelMatrix, 0);
		multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);*/

		//groundPatch.update(viewMatrix, projectionMatrix);

		grass.update(viewMatrix, projectionMatrix, time);

		//groundManager.update(viewMatrix, projectionMatrix, time);


		playerRock.update(viewProjectionMatrix);
		playerDisplacement = playerRock.getDisplacement();

		if(leftTouchState.isTouching())
		{
			//displacement.setValues(1f, 0.0f, playerRock.getDisplacement());
			displacement.setValues(playerDisplacement);
			playerRock.turnLeft();
		}
		else if(rightTouchState.isTouching())
		{
			//displacement.setValues(-1f, 0.0f, playerRock.getDisplacement());
			displacement.setValues(playerDisplacement);
			playerRock.turnRight();
		}
		else
		{
			//displacement.setValues(0.0f, 0.0f, playerRock.getDisplacement());
			displacement.setValues(playerDisplacement);
			playerRock.releaseTouch();
		}

		ground.update(viewMatrix, displacement, time);

		mirror.update(viewMatrix, projectionMatrix);

		//groundLane.update(viewMatrix, projectionMatrix, time);
		//.update(viewMatrix, projectionMatrix, time);
		//patch2.update(viewMatrix, projectionMatrix, time);
	}


	public void handleTouchDrag(float deltaX, float deltaY)
	{
		xRotation += deltaX / 10f;
		yRotation += deltaY / 10f;

		if(yRotation < -90) yRotation = -90;
		else if(yRotation > 90) yRotation = 90;

		update();
	}


	public int getWidth()
	{
		return width;
	}


	public int getHeight()
	{
		return height;
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
}
