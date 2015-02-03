package com.josepmtomas.rockgame.objects;

import android.content.Context;

import com.josepmtomas.rockgame.util.TextureHelper;

import static android.opengl.GLES30.*;

/**
 * Created by Josep on 01/01/2015.
 * @author Josep
 */
public class MenuTextures
{
	// Game title

	public final int gameTitleTexture;

	// HUD

	public final int numbersAtlasTexture;
	public final int pauseButtonIdleTexture;
	public final int pauseButtonSelectedTexture;
	public final int progressBarTexture;

	// 9 patch panel

	public final int background9PatchPanelTexture;

	// Main menu

	public final int recordButtonTexture;

	public final int newGameButtonIdleTexture;
	public final int newGameButtonSelectedTexture;
	public final int howToPlayButtonIdleTexture;
	public final int howToPlayButtonSelectedTexture;
	public final int optionsButtonIdleTexture;
	public final int optionsButtonSelectedTexture;
	public final int creditsButtonIdleTexture;
	public final int creditsButtonSelectedTexture;

	public final int speedNormalTexture;
	public final int speedFastTexture;
	public final int visibilityEnabledTexture;
	public final int visibilityDisabledTexture;

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
	public final int texturesTitleTexture;
	public final int originalModelsTitleTexture;
	public final int hughesTitleTexture;
	public final int tomislavTitleTexture;
	public final int specialThanksTitleTexture;

	// Pause

	public final int pauseTitleTexture;
	public final int resumeButtonIdleTexture;
	public final int resumeButtonSelectedTexture;
	public final int restartButtonIdleTexture;
	public final int restartButtonSelectedTexture;
	public final int endGameButtonIdleTexture;
	public final int endGameButtonSelectedTexture;
	public final int optionsBigButtonIdleTexture;
	public final int optionsBigButtonSelectedTexture;

	// End Game dialog

	public final int endGameTitleTexture;
	public final int endGameTextTexture;

	// Restart dialog

	public final int restartTitleTexture;
	public final int restartTextTexture;

	// Game over

	public final int gameOverTitleTexture;
	public final int finalScoreTitleTexture;
	public final int touchToContinueTitleTexture;
	public final int newRecordTitleTexture;


	public MenuTextures(Context context)
	{
		// Game title

		gameTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/game_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// HUD

		numbersAtlasTexture = TextureHelper.loadETC2Texture(context, "textures/menus/numbers_atlas.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		pauseButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/pause_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		pauseButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/pause_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		progressBarTexture = TextureHelper.loadETC2Texture(context, "textures/menus/progress_bar_alpha.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// 9 Patch panel

		background9PatchPanelTexture = TextureHelper.loadETC2Texture(context, "textures/menus/9patch.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Main menu

		recordButtonTexture = TextureHelper.loadETC2Texture(context, "textures/menus/record_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		newGameButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/new_game_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		newGameButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/new_game_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		howToPlayButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/how_to_play_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		howToPlayButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/how_to_play_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		optionsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		optionsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		creditsButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/credits_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		creditsButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/credits_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		speedNormalTexture = TextureHelper.loadETC2Texture(context, "textures/menus/speed_normal.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		speedFastTexture = TextureHelper.loadETC2Texture(context, "textures/menus/speed_fast.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		visibilityEnabledTexture = TextureHelper.loadETC2Texture(context, "textures/menus/visibility_enabled.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		visibilityDisabledTexture = TextureHelper.loadETC2Texture(context, "textures/menus/visibility_disabled.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

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
		texturesTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/textures_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		originalModelsTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/original_models_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		hughesTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/hughes_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		tomislavTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/tomislav_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		specialThanksTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/special_thanks_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Pause

		pauseTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/pause_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		resumeButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resume_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		resumeButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/resume_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		restartButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		restartButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		endGameButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		endGameButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		optionsBigButtonIdleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_big_idle.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		optionsBigButtonSelectedTexture = TextureHelper.loadETC2Texture(context, "textures/menus/options_big_selected.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// End game dialog

		endGameTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		endGameTextTexture = TextureHelper.loadETC2Texture(context, "textures/menus/end_game_text.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Restart dialog

		restartTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		restartTextTexture = TextureHelper.loadETC2Texture(context, "textures/menus/restart_text.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);

		// Game Over

		gameOverTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/game_over_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		finalScoreTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/final_score_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		touchToContinueTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/touch_to_continue_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
		newRecordTitleTexture = TextureHelper.loadETC2Texture(context, "textures/menus/new_record_title.mp3", GL_COMPRESSED_RGBA8_ETC2_EAC, false, true);
	}
}
