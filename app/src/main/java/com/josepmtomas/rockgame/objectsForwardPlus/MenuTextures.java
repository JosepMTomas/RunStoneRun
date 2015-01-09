package com.josepmtomas.rockgame.objectsForwardPlus;

import android.content.Context;

import com.josepmtomas.rockgame.util.TextureHelper;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 01/01/2015.
 * @author Josep
 */
public class MenuTextures
{
	// 9 patch panel

	public final int background9PatchPanelTexture;

	// Main menu

	public final int newGameButtonIdleTexture;
	public final int newGameButtonSelectedTexture;
	public final int optionsButtonIdleTexture;
	public final int optionsButtonSelectedTexture;
	public final int creditsButtonIdleTexture;
	public final int creditsButtonSelectedTexture;

	// Options

	public final int optionsTitleTexture;
	public final int screenResolutionTitleTexture;
	public final int postProcessDetailTitleTexture;
	public final int musicTitleTexture;
	public final int soundEffectsTitleTexture;

	public final int backButtonIdleTexture;
	public final int backButtonSelectedTexture;

	public final int resolutionPercentageButton25IdleTexture;
	public final int resolutionPercentageButton50IdleTexture;
	public final int resolutionPercentageButton75IdleTexture;
	public final int resolutionPercentageButton100IdleTexture;
	public final int resolutionPercentageButton25SelectedTexture;
	public final int resolutionPercentageButton50SelectedTexture;
	public final int resolutionPercentageButton75SelectedTexture;
	public final int resolutionPercentageButton100SelectedTexture;

	public final int noButtonIdleTexture;
	public final int noButtonSelectedTexture;
	public final int yesButtonIdleTexture;
	public final int yesButtonSelectedTexture;

	public final int postProcessLowDetailButtonIdleTexture;
	public final int postProcessLowDetailButtonSelectedTexture;
	public final int postProcessHighDetailButtonIdleTexture;
	public final int postProcessHighDetailButtonSelectedTexture;

	public final int soundEnableButtonIdleTexture;
	public final int soundEnableButtonSelectedTexture;
	public final int soundDisableButtonIdleTexture;
	public final int soundDisableButtonSelectedTexture;

	// Credits

	public final int creditsTitleTexture;
	public final int developerTitleTexture;
	public final int composerTitleTexture;
	public final int effectsTitleTexture;
	public final int fontTitleTexture;

	// Pause

	public final int pauseTitleTexture;
	public final int resumeButtonIdleTexture;
	public final int resumeButtonSelectedTexture;
	public final int restartButtonIdleTexture;
	public final int restartButtonSelectedTexture;
	public final int endGameButtonIdleTexture;
	public final int endGameButtonSelectedTexture;

	// End Game dialog

	public final int endGameTitleTexture;
	public final int endGameTextTexture;


	public MenuTextures(Context context)
	{
		// 9 Patch panel

		background9PatchPanelTexture = TextureHelper.loadETC2Texture(context, "textures/menus/9patch.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Main menu

		newGameButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/new_game_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		newGameButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/new_game_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		optionsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		optionsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		creditsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/credits_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		creditsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/credits_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// options

		optionsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		screenResolutionTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/screen_resolution_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessDetailTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/post_process_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		musicTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/music_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		soundEffectsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/effects_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		backButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/back_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		backButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/back_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resolutionPercentageButton25IdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_25_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50IdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_50_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75IdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_75_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100IdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_100_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resolutionPercentageButton25SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_25_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton50SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_50_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton75SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_75_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resolutionPercentageButton100SelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resolution_100_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		noButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/no_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		yesButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/yes_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		noButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/no_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		yesButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/yes_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		postProcessLowDetailButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/low_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessHighDetailButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/high_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessLowDetailButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/low_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		postProcessHighDetailButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/high_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		soundEnableButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/sound_enabled_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		soundEnableButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/sound_enabled_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		soundDisableButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/sound_disabled_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		soundDisableButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/sound_disabled_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Credits

		creditsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/credits_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		developerTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/developer_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		composerTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/composer_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		effectsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/sound_effects_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		fontTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/font_type_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Pause

		pauseTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/pause_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resumeButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resume_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resumeButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resume_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		restartButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		restartButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		endGameButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		endGameButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// End game dialog

		endGameTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		endGameTextTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_text.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}
}
