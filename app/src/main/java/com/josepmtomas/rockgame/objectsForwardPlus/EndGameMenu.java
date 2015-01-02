package com.josepmtomas.rockgame.objectsForwardPlus;

import com.josepmtomas.rockgame.ForwardPlusRenderer;
import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 01/01/2015.
 */
public class EndGameMenu
{
	private GameActivity parent;
	private ForwardPlusRenderer renderer;
	private UIPanelProgram uiPanelProgram;
	private MenuTextures menuTextures;

	// State
	private int currentState = UI_STATE_NOT_VISIBLE;

	// Menu common attributes
	private static final float dialogAppearTime = 0.5f;
	private static final float dialogDisappearTime = 0.5f;
	private float dialogTimer = 0f;
	private float dialogOpacity = 1f;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// Panels
	private final int uiPanelVaoHandle;
	private final int ui9PatchPanelVaoHandle;

	// Background panel
	private float[] backgroundPanelCurrentScale = new float[2];
	private float[] backgroundPanelCurrentPosition = new float[2];

	// End Game Title
	private float[] endGameTitleScale = new float[2];
	private float[] endGameTitlePosition = new float[2];
	private float[] endGameTitleCurrentScale = new float[2];
	private float[] endGameTitleCurrentPosition = new float[2];

	// End Game Text
	private float[] endGameTextScale = new float[2];
	private float[] endGameTextPosition = new float[2];
	private float[] endGameTextCurrentScale = new float[2];
	private float[] endGameTextCurrentPosition = new float[2];


	public EndGameMenu(GameActivity parent, ForwardPlusRenderer renderer, UIPanelProgram panelProgram, MenuTextures textures, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.renderer = renderer;
		this.uiPanelProgram = panelProgram;
		this.menuTextures = textures;

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);
		ui9PatchPanelVaoHandle = UIHelper.make9PatchPanel(screenHeight * 1.1f, screenHeight * 0.5f, screenHeight * 0.1f, UI_BASE_CENTER_CENTER);

		createMatrices(screenWidth, screenHeight);
		setPositions(screenWidth, screenHeight);
	}


	private void createMatrices(float screenWidth, float screenHeight)
	{
		float right = screenWidth * 0.5f;
		float left = -right;
		float top = screenHeight * 0.5f;
		float bottom = -top;
		float near = 0.5f;
		float far = 10f;

		// Initialize view matrix
		setIdentityM(view, 0);
		setLookAtM(view, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);

		// Initialize projection matrix
		setIdentityM(projection, 0);
		orthoM(projection, 0, left, right, bottom, top, near, far);

		// Calculate view x projection matrix
		multiplyMM(viewProjection, 0, projection, 0, view, 0);
	}


	private void setPositions(float screenWidth, float screenHeight)
	{
		float titleHeight = screenHeight * 0.1f;
		float titleWidth = titleHeight * 10f;

		// Background panel
		backgroundPanelCurrentScale[0] = 1f;
		backgroundPanelCurrentScale[1] = 1f;
		backgroundPanelCurrentPosition[0] = 0f;
		backgroundPanelCurrentPosition[1] = 0f;

		// End Game Title
		endGameTitleScale[0] = titleWidth;
		endGameTitleScale[1] = titleHeight;
		endGameTitlePosition[0] = 0f;
		endGameTitlePosition[1] = titleHeight * 1.5f;
		endGameTitleCurrentScale[0] = endGameTitleScale[0];
		endGameTitleCurrentScale[1] = endGameTitleScale[1];
		endGameTitleCurrentPosition[0] = endGameTitlePosition[0];
		endGameTitleCurrentPosition[1] = endGameTitlePosition[1];

		// End Game Text
		endGameTextScale[0] = titleWidth;
		endGameTextScale[1] = titleHeight * 1.5f;
		endGameTextPosition[0] = 0f;
		endGameTextPosition[1] = 0f;
		endGameTextCurrentScale[0] = endGameTextScale[0];
		endGameTextCurrentScale[1] = endGameTextScale[1];
		endGameTextCurrentPosition[0] = endGameTextPosition[0];
		endGameTextCurrentPosition[1] = endGameTextPosition[1];
	}


	public void setAppearing()
	{
		currentState = UI_STATE_APPEARING;
	}


	public void update(float deltaTime)
	{

	}


	public void draw()
	{
		uiPanelProgram.useProgram();

		// Background panel
		glBindVertexArray(ui9PatchPanelVaoHandle);
		uiPanelProgram.setUniforms(viewProjection, backgroundPanelCurrentScale, backgroundPanelCurrentPosition, menuTextures.background9PatchPanelTexture, dialogOpacity * 0.75f);
		glDrawElements(GL_TRIANGLES, 54, GL_UNSIGNED_SHORT, 0);

		// End game title
		glBindVertexArray(uiPanelVaoHandle);
		uiPanelProgram.setUniforms(viewProjection, endGameTitleCurrentScale, endGameTitleCurrentPosition, menuTextures.endGameTitleTexture, dialogOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		// End game text
		uiPanelProgram.setUniforms(viewProjection, endGameTextCurrentScale, endGameTextCurrentPosition, menuTextures.endGameTextTexture, dialogOpacity);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
	}
}