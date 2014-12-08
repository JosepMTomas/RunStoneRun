package com.josepmtomas.rockgame;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.objectsES30.Ground;
import com.josepmtomas.rockgame.objectsES30.PlayerRock;
import com.josepmtomas.rockgame.objectsES30.Screen;
import com.josepmtomas.rockgame.objectsES30.Skybox;
import com.josepmtomas.rockgame.objectsES30.VAOMesh;
import com.josepmtomas.rockgame.util.FPSCounter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 23/08/2014.
 */
public class DeferredRenderer implements Renderer
{
	private static final String TAG = "DeferredRenderer";

	private static final int SHADOW_MAP_SIZE = 512;

	private static final int FRAMEBUFFER_WIDTH = 720;
	private static final int FRAMEBUFFER_HEIGHT = 1280;

	private int renderWidth;			// Screen width resolution
	private int renderHeight;			// Screen height resolution
	private float renderAspectRatio;	// Screen aspect ratio (width / height)

	private Context context;	// Application context

	// Touch screen
	private float xRotation = 0.0f;
	private float yRotation = 0.0f;

	// Camera
	private vec3 eyePos = new vec3(0.0f, 10.0f, 100.0f);
	private vec3 eyeLook = new vec3(0.0f, 0.0f, 0.0f);
	private float near = 0.1f;
	private float far = 500.0f;
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] inverseProjectionMatrix = new float[16];

	private float[] currentVPMatrix = new float[16];
	private float[] inverseVPMatrix = new float[16];
	private float[] previousVPMatrix = new float[16];

	// Light
	private vec3 lightPosition = new vec3(50f, 50f, 0.0f);

	// Depth
	// Normals + (diffuse + roughness) + specular + shadow
	private int debugVisualization = 4;
	private int[] deferredFboID = new int[1];
	private int[] deferredRboID = new int[1];
	private int[] deferredTexID = new int[5];
	private int[] deferredDrawBuffers = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3};

	// Post-process
	private boolean postProcessActive = false;
	private int[] postProcessFboID = new int[1];
	private int[] postProcessTexID = new int[1];
	private int[] postProcessDrawBuffers = {GL_COLOR_ATTACHMENT0};

	// Shadow map
	private int[] shadowMapGenTexID = new int[1];
	private int[] shadowMapGenFboID = new int[1];
	private int[] shadowMapGenRboID = new int[1];
	private int[] shadowMapFilterTexID = new int[2];
	private int[] shadowMapFilterFboID = new int[1];

	private float[] shadowModelViewMatrix = new float[16];
	private float[] shadowPerspectiveMatrix = new float[16];
	private float[] shadowBiasMatrix = new float[16];
	private float[] shadowPerspectiveBiasMatrix = new float[16];
	private float[] shadowProjectionMatrix = new float[16];

	private float[] MV_L = new float[16];
	private float[] P_L = new float[16];
	private float[] B = new float[16];
	private float[] BP = new float[16];
	private float[] S = new float[16];

	// Scene
	VAOMesh vaoMesh;
	Ground ground;
	Screen screen;
	PlayerRock playerRock;
	Skybox skybox;

	// FPS
	FPSCounter fpsCounter = new FPSCounter();

	/**********************************************************************************************/

	public DeferredRenderer(Context context, float renderWidth, float renderHeight)
	{
		this.context = context;
		this.renderWidth = (int)renderWidth;
		this.renderHeight = (int)renderHeight;
		this.renderAspectRatio = renderWidth / renderHeight;

		Log.e(TAG, "Screen size = " + renderWidth + " x " + renderHeight);
		Log.e(TAG, "Aspect ratio = " + renderAspectRatio);
	}

	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
	{
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		setLookAtM(viewMatrix, 0, 0f, 10f, 100f, 0f, 0f, 0f, 0f, 1f, 0f);
		perspectiveM(projectionMatrix, 0, 60, renderAspectRatio, near, far);
		invertM(inverseProjectionMatrix, 0, projectionMatrix, 0);

		multiplyMM(inverseVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
		multiplyMM(previousVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

		vaoMesh = new VAOMesh(context, R.raw.torusknot2);
		ground = new Ground(context, 5, 9, 50, 50, 50f, 50f);
		screen = new Screen(context, renderWidth, renderHeight);
		playerRock = new PlayerRock(context);
		playerRock.initialize();

		skybox = new Skybox(context, 20f, 10f, 20f);

		glEnable(GL_DEPTH_TEST);
		//glEnable(GL_CULL_FACE);

		int[] glMaxTextureSize = new int[1];
		glGetIntegerv(GL_MAX_TEXTURE_SIZE, glMaxTextureSize, 0);
		Log.e(TAG, "Max. texture size = " + glMaxTextureSize[0]);

		/************************************ DEFERRED BUFFERS ************************************/

		// Initialize the framebuffer and renderbuffer objects color and depth attachments.
		// The renderbuffer is required for depth testing and its precision is specified using the
		// glRenderbufferStorage function.
		glGenFramebuffers(1, deferredFboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, deferredFboID[0]);
		glGenRenderbuffers(1, deferredRboID, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, deferredRboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

		// Generate the off-screen textures on which the FBO will render to.
		glGenTextures(5, deferredTexID, 0);

		// Set the texture parameters for the first texture (normals).
		glBindTexture(GL_TEXTURE_2D, deferredTexID[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_R16F, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RED, GL_FLOAT, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Set the texture parameters for the second texture ( diffuse(RGB) + roughness(A)).
		glBindTexture(GL_TEXTURE_2D, deferredTexID[1]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Set the texture parameters for the third texture (specular).
		glBindTexture(GL_TEXTURE_2D, deferredTexID[2]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Set the texture parameters for the fourth texture (shadow).
		glBindTexture(GL_TEXTURE_2D, deferredTexID[3]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Set the texture parameters for the fifth texture (depth).
		/*glBindTexture(GL_TEXTURE_2D, deferredTexID[4]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, renderWidth, renderHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);*/

		// Attach the textures to the framebuffer
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, deferredTexID[0], 0);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, deferredTexID[1], 0);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, deferredTexID[2], 0);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT3, GL_TEXTURE_2D, deferredTexID[3], 0);
		//glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, deferredTexID[4], 0);

		// Attach Renderbuffer to the framebuffer
		glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, deferredRboID[0]);

		// Check Framebuffer completeness
		int deferredFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(deferredFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Deferred buffers FBO setup OK");
		}
		else
		{
			Log.e(TAG, "Deferred buffers FBO setup FAILED");
		}

		screen.setBuffers(deferredTexID);

		// Unbind the textures and the framebuffer
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		/**************************** POST-PROCESS ************************************************/

		// Create the framebuffer object
		glGenFramebuffers(1, postProcessFboID, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, postProcessFboID[0]);

		// Create an OpenGL texture object to save the framebuffer result
		glGenTextures(1, postProcessTexID, 0);
		glBindTexture(GL_TEXTURE_2D, postProcessTexID[0]);

		glBindTexture(GL_TEXTURE_2D, postProcessTexID[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Attach the textures to the framebuffer
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, postProcessTexID[0], 0);

		// Check Framebuffer completeness
		int postProcessFboStatus = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
		if(postProcessFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "Post process FBO setup OK");
		}
		else
		{
			Log.e(TAG, "Post process FBO setup FAILED");
		}

		// Unbind the textures and the framebuffer
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		/****************************** SHADOW MAP ************************************************/

		// Create an OpenGL texture object which will be the shadow map texture and bind it.
		glGenTextures(1, shadowMapGenTexID, 0);
		glBindTexture(GL_TEXTURE_2D, shadowMapGenTexID[0]);

		// Create a blank texture
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0, GL_RGBA, GL_FLOAT, null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 4);
		glGenerateMipmap(GL_TEXTURE_2D);
		//glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
		//glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

		// Set the texture parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		// Set up an FBO and use the shadow map texture as a single depth attachment.
		glGenFramebuffers(1, shadowMapGenFboID, 0);
		glGenRenderbuffers(1, shadowMapGenRboID, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMapGenFboID[0]);
		glBindFramebuffer(GL_RENDERBUFFER, shadowMapGenRboID[0]);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,shadowMapGenTexID[0], 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, shadowMapGenRboID[0]);
		//glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMapGenTexID[0], 0);

		int shadowGenFboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(shadowGenFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "ShadowMap generation FBO setup OK");
		}
		else
		{
			Log.e(TAG, "ShadowMap generation FBO setup FAILED");
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		glGenFramebuffers(1, shadowMapFilterFboID, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFilterFboID[0]);
		glGenTextures(2, shadowMapFilterTexID, 0);
		for(int i=0; i<2; i++)
		{
			glActiveTexture(GL_TEXTURE1+i);
			glBindTexture(GL_TEXTURE_2D, shadowMapFilterTexID[i]);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0, GL_RGBA, GL_FLOAT, null);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0+i, GL_TEXTURE_2D, shadowMapFilterTexID[i], 0);
		}

		int shadowFilterFboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(shadowFilterFboStatus == GL_FRAMEBUFFER_COMPLETE)
		{
			Log.d(TAG, "ShadowMap filter FBO setup OK");
		}
		else
		{
			Log.e(TAG, "ShadowMap filter FBO setup FAILED");
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		// Using the position and the direction of the light, set up the shadow matrix by combining
		// the light model-view matrix, projection matrix, and the bias matrix. For reducing runtime
		// calculation, we store the combined projection and bias matrix at initialization.
		setLookAtM(shadowModelViewMatrix, 0, lightPosition.x, lightPosition.y, lightPosition.z, 0f, 0f, 0f, 0f, 1f, 0f);
		perspectiveM(shadowPerspectiveMatrix, 0, 90.0f, 1.0f, 1.0f, 500.0f);

		setIdentityM(shadowBiasMatrix, 0);
		translateM(shadowBiasMatrix, 0, 0.5f, 0.5f, 0.5f);
		scaleM(shadowBiasMatrix, 0, 0.55f, 0.55f, 0.55f);

		multiplyMM(shadowPerspectiveBiasMatrix, 0, shadowBiasMatrix, 0, shadowPerspectiveMatrix, 0);
		multiplyMM(shadowProjectionMatrix, 0, shadowPerspectiveBiasMatrix, 0, shadowModelViewMatrix, 0);

		/******************************** SHADOW MAP FILTERING ************************************/

	}

	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		//this.renderWidth = width;
		//this.renderHeight = height;
		//this.renderAspectRatio = (float)width / (float)height;

		//glViewport(0, 0, renderWidth, renderHeight);
	}

	public void onDrawFrame(GL10 gl10)
	{
		update();

		//shadowPass1();
		//shadowPass2();
		deferredPass1();
		deferredPass2();

		glClear(GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, renderWidth, renderHeight);
		//glClear(GL_DEPTH_BUFFER_BIT);
		playerRock.draw(viewMatrix, projectionMatrix);

		fpsCounter.logFrame();
	}


	public void shadowPass1()
	{
		//glDisable(GL_CULL_FACE);
		// Bind the FBO and render the scene from the point of view of the light. Enable front-face
		// culling so that the back-face depth values are rendered.
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMapGenFboID[0]);
		glClear(GL_DEPTH_BUFFER_BIT);
		glCullFace(GL_FRONT);
		glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);

		//ground.drawShadowMap(shadowModelViewMatrix, shadowPerspectiveMatrix);
		vaoMesh.drawShadowPass(shadowModelViewMatrix, shadowPerspectiveMatrix);

		// Disable FBO, restore default culling
		glCullFace(GL_BACK);
		//glEnable(GL_CULL_FACE);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}


	private void shadowPass2()
	{

	}


	private void deferredPass1()
	{
		// Enable FBO
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, deferredFboID[0]);
		glDrawBuffers(4, deferredDrawBuffers, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

		//vaoMesh.drawDeferred();
		ground.drawDeferred(currentVPMatrix, shadowProjectionMatrix, shadowMapGenTexID[0]);
		skybox.draw();

		// Disable FBO
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}



	private void deferredPass2()
	{
		if(postProcessActive)
		{
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, postProcessFboID[0]);
			glDrawBuffers(1, postProcessDrawBuffers, 0);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glViewport(0, 0, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT);

			screen.draw(inverseVPMatrix, inverseProjectionMatrix, debugVisualization);

			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

			postProcessPass();
		}
		else
		{
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glViewport(0, 0, renderWidth, renderHeight);

			screen.draw(inverseVPMatrix, inverseProjectionMatrix, debugVisualization);
		}
	}


	private void postProcessPass()
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, renderWidth, renderHeight);

		screen.drawPostProcess(postProcessTexID[0], deferredTexID[3], currentVPMatrix, previousVPMatrix);
	}


	private void update()
	{
		multiplyMM(previousVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

		//renderAspectRatio = 0.5f;
		//perspectiveM(projectionMatrix, 0, 45f, renderAspectRatio, 0.1f, 1000f);
		setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, eyeLook.x, eyeLook.y, eyeLook.z, 0f, 1f, 0f);
		rotateM(viewMatrix, 0, yRotation, 1f, 0f, 0f);
		rotateM(viewMatrix, 0, xRotation, 0f, 1f, 0f);

		vaoMesh.setMatrices(viewMatrix, projectionMatrix, 0.0f);

		multiplyMM(currentVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
		invertM(inverseVPMatrix, 0, currentVPMatrix, 0);


		playerRock.update(currentVPMatrix);
		ground.update(viewMatrix, new vec3(playerRock.getDisplacement()), 0.0f);

		skybox.setMatrices(viewMatrix, projectionMatrix);
		skybox.update();
	}


	public void setDebugVisualization(int index)
	{
		debugVisualization = index;
	}


	public void switchPostProcess()
	{
		postProcessActive = !postProcessActive;
	}


	public void handleTouch()
	{

	}

	public void handleTouchDrag(float deltaX, float deltaY)
	{
		xRotation += deltaX / 10f;
		yRotation += deltaY / 10f;

		if(yRotation < -90) yRotation = -90;
		else if(yRotation > 90) yRotation = 90;

		update();
	}
}
