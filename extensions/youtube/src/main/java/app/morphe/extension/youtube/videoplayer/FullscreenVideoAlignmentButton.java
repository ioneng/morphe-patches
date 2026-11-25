/*
 * Copyright 2026 Morphe contributors.
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 */

package app.morphe.extension.youtube.videoplayer;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.youtube.patches.FullscreenVideoScalePatch;
import app.morphe.extension.youtube.patches.FullscreenVideoScalePatch.VideoVerticalAlignment;
import app.morphe.extension.youtube.patches.LegacyPlayerControlsPatch;
import app.morphe.extension.youtube.settings.Settings;
import app.morphe.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class FullscreenVideoAlignmentButton {
    @Nullable
    private static LegacyPlayerControlButton legacy;
    private static WeakReference<ImageView> overlayButtonRef = new WeakReference<>(null);

    /** Injection point for modern player controls. */
    public static void initializeButton(View controlsView) {
        try {
            if (LegacyPlayerControlsPatch.RESTORE_OLD_PLAYER_BUTTONS
                    || !Settings.FULLSCREEN_VIDEO_ALIGNMENT_BUTTON.get()) {
                return;
            }
            ImageView added = PlayerOverlayButton.addButton(
                    controlsView,
                    PlayerIcons.name(Settings.FULLSCREEN_VIDEO_ALIGNMENT.get().iconBaseName),
                    FullscreenVideoAlignmentButton::isButtonEnabled,
                    view -> cycleAlignment(),
                    null
            );
            overlayButtonRef = new WeakReference<>(added);
        } catch (Exception ex) {
            Logger.printException(() -> "initializeAlignmentButton failure", ex);
        }
    }

    /** Injection point for legacy player controls. */
    public static void initializeLegacyButton(View controlsView) {
        try {
            if (!LegacyPlayerControlsPatch.RESTORE_OLD_PLAYER_BUTTONS) {
                return;
            }
            legacy = new LegacyPlayerControlButton(
                    controlsView,
                    "fullscreen_video_alignment_button",
                    null,
                    Settings.FULLSCREEN_VIDEO_ALIGNMENT.get().iconBaseName,
                    () -> isButtonEnabled()
                            ? LegacyPlayerControlButton.ButtonVisibility.ENABLED
                            : LegacyPlayerControlButton.ButtonVisibility.DISABLED,
                    view -> cycleAlignment(),
                    null
            );
        } catch (Exception ex) {
            Logger.printException(() -> "initializeLegacyAlignmentButton failure", ex);
        }
    }

    private static boolean isButtonEnabled() {
        return Settings.FULLSCREEN_VIDEO_ALIGNMENT_BUTTON.get()
                && PlayerType.getCurrent() == PlayerType.WATCH_WHILE_FULLSCREEN;
    }

    private static void cycleAlignment() {
        try {
            VideoVerticalAlignment current = Settings.FULLSCREEN_VIDEO_ALIGNMENT.get();
            VideoVerticalAlignment next = switch (current) {
                case CENTER -> VideoVerticalAlignment.TOP;
                case TOP -> VideoVerticalAlignment.BOTTOM;
                case BOTTOM -> VideoVerticalAlignment.CENTER;
            };
            Settings.FULLSCREEN_VIDEO_ALIGNMENT.save(next);
            updateButtonIcon(next);
            FullscreenVideoScalePatch.applyScale();
        } catch (Exception ex) {
            Logger.printException(() -> "cycleAlignment failure", ex);
        }
    }

    private static void updateButtonIcon(VideoVerticalAlignment alignment) {
        Utils.verifyOnMainThread();
        final int icon = PlayerIcons.id(alignment.iconBaseName);
        ImageView overlayButton = overlayButtonRef.get();
        if (overlayButton != null) {
            overlayButton.setImageResource(icon);
        }
        if (legacy != null) {
            legacy.setIcon(icon);
        }
    }
}
