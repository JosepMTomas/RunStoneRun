package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;
import android.util.Log;

import com.josepmtomas.rockgame.GameActivity;
import com.josepmtomas.rockgame.programsForwardPlus.UIPanelProgram;
import com.josepmtomas.rockgame.util.TextureHelper;
import com.josepmtomas.rockgame.util.UIHelper;

import static com.josepmtomas.rockgame.Constants.*;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.*;

/**
 * Created by Josep on 22/12/2014.
 */
public class MainMenu
{
	private GameActivity parent;
	private Context context;

	// Matrices
	private float[] view = new float[16];
	private float[] projection = new float[16];
	private float[] viewProjection = new float[16];

	// UI Panel
	private int uiPanelVaoHandle;

	// New game button
	private int newGameButtonIdleTexture;
	private int newGameButtonSelectedTexture;
	private int newGameButtonCurrentTexture;
	private float[] newGameButtonPosition = new float[2];
	private float[] newGameButtonScale = new float[2];
	private float[] newGameButtonLimits = new float[4];

	// Options button
	private int optionsButtonIdleTexture;
	private int optionsButtonSelectedTexture;
	private int optionsButtonCurrentTexture;
	private float[] optionsButtonPosition = new float[2];
	private float[] optionsButtonScale = new float[2];
	private float[] optionsButtonLimits = new float[4];

	// Credits button
	private int creditsButtonIdleTexture;
	private int creditsButtonSelectedTexture;
	private int creditsButtonCurrentTexture;
	private float[] creditsButtonPosition = new float[2];
	private float[] creditsButtonScale = new float[2];
	private float[] creditsButtonLimits = new float[4];

	// renderer ui programs
	UIPanelProgram uiPanelProgram;


	public MainMenu(GameActivity parent, UIPanelProgram panelProgram, float screenWidth, float screenHeight)
	{
		this.parent = parent;
		this.context = parent.getApplicationContext();
		this.uiPanelProgram = panelProgram;

		createMatrices(screenWidth, screenHeight);
		loadTextures();

		uiPanelVaoHandle = UIHelper.makePanel(1f, 1f, UI_BASE_CENTER_CENTER);

		createButtons(screenWidth, screenHeight);
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


	private void loadTextures()
	{
		newGameButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/new_game_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		newGameButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/new_game_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		newGameButtonCurrentTexture = newGameButtonIdleTexture;

		optionsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/options_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		optionsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/options_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		optionsButtonCurrentTexture = optionsButtonIdleTexture;

		creditsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/credits_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		creditsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/main_menu/credits_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		creditsButtonCurrentTexture = creditsButtonIdleTexture;

	}


	private void createButtons(float screenWidth, float screenHeight)
	{
		float width = screenWidth * 0.3f;
		float height = screenHeight * 0.16f;

		createNewGameButton(width, height);
		createOptionsButton(width, height);
		createCreditsButton(width, height);
	}


	private void createNewGameButton(float width, float height)
	{
		newGameButtonPosition[0] = 0f;
		newGameButtonPosition[1] = height * 0.5f;

		newGameButtonScale[0] = width;
		newGameButtonScale[1] = height;

		// left-right-bottom-top
		newGameButtonLimits[0] = newGameButtonPosition[0] - newGameButtonScale[0];
		newGameButtonLimits[1] = newGameButtonPosition[0] + newGameButtonScale[0];
		newGameButtonLimits[2] = newGameButtonPosition[1] - newGameButtonScale[1];
		newGameButtonLimits[3] = newGameButtonPosition[1] + newGameButtonScale[1];
	}


	private void createOptionsButton(float width, float height)
	{
		optionsButtonPosition[0] = newGameButtonPosition[0];
		optionsButtonPosition[1] = newGameButtonPosition[1] - height;

		optionsButtonScale[0] = width;
		optionsButtonScale[1] = height;

		optionsButtonLimits[0] = optionsButtonPosition[0] - optionsButtonScale[0];
		optionsButtonLimits[1] = optionsButtonPosition[0] + optionsButtonScale[0];
		optionsButtonLimits[2] = optionsButtonPosition[1] - optionsButtonScale[1];
		optionsButtonLimits[3] = optionsButtonPosition[1] + optionsButtonScale[1];
	}


	private void createCreditsButton(float width, float height)
	{
		creditsButtonPosition[0] = optionsButtonPosition[0];
		creditsButtonPosition[1] = optionsButtonPosition[1] - height;

		creditsButtonScale[0] = width;
		creditsButtonScale[1] = height;

		creditsButtonLimits[0] = creditsButtonPosition[0] - creditsButtonScale[0];
		creditsButtonLimits[1] = creditsButtonPosition[0] + creditsButtonScale[0];
		creditsButtonLimits[2] = creditsButtonPosition[1] - creditsButtonScale[1];
		creditsButtonLimits[3] = creditsButtonPosition[1] + creditsButtonScale[1];
	}


	public void touch(float x, float y)
	{
		//Log.w("MainMenu", "Touch : x = " + x + " : y = " + y);
		if(y >= newGameButtonLimits[2] && y <= newGameButtonLimits[3])
		{
			touchNewGameButton();
		}
	}


	public void release()
	{
		newGameButtonCurrentTexture = newGameButtonIdleTexture;
		optionsButtonCurrentTexture = optionsButtonIdleTexture;
		creditsButtonCurrentTexture = creditsButtonIdleTexture;
	}


	private void touchNewGameButton()
	{
		newGameButtonCurrentTexture = newGameButtonSelectedTexture;
	}


	private void touchOptionsButton()
	{
		optionsButtonCurrentTexture = optionsButtonSelectedTexture;
	}


	private void touchCreditsButton()
	{
		creditsButtonCurrentTexture = creditsButtonSelectedTexture;
	}


	public void draw()
	{
		uiPanelProgram.useProgram();
		glBindVertexArray(uiPanelVaoHandle);

		uiPanelProgram.setUniforms(viewProjection, newGameButtonScale, newGameButtonPosition, newGameButtonCurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		uiPanelProgram.setUniforms(viewProjection, optionsButtonScale, optionsButtonPosition, optionsButtonCurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

		uiPanelProgram.setUniforms(viewProjection, creditsButtonScale, creditsButtonPosition, creditsButtonCurrentTexture, 1f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
	}
}
