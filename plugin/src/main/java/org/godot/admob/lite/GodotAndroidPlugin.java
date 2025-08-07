package org.godot.admob.lite;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

@SuppressWarnings("unused")
public class GodotAndroidPlugin extends GodotPlugin {
    private boolean canShowAds = false;
    private AdLogic adLogic;
    private UMPLogic umpLogic;

    public GodotAndroidPlugin(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotAdmobLite";
    }

    @Override
    public View onMainCreate(Activity activity) {
        // The 'activity' parameter is guaranteed to be non-null when this method is called.
        // In Kotlin, 'super.getActivity()!!' and 'activity!!' handle nullability.
        // In Java, we can directly use the 'activity' parameter.
        FrameLayout adContainer = new FrameLayout(activity);
        adLogic = new AdLogic(activity, adContainer);
        umpLogic = new UMPLogic(activity);

        ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        // Set the LayoutParams on the FrameLayout
        adContainer.setLayoutParams(layoutParams);
        return adContainer;
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo("on_initialization_complete", Boolean.class));
        signals.add(new SignalInfo("on_interstitial_loaded", String.class, Boolean.class));
        signals.add(new SignalInfo("on_interstitial_shown", String.class, Integer.class));
        signals.add(new SignalInfo("on_rewarded_loaded", String.class, Boolean.class));
        signals.add(new SignalInfo("on_reward_shown", String.class, Integer.class));
        signals.add(new SignalInfo("on_reward_received", String.class, Integer.class,
                String.class));
        return signals;
    }

    // GENERAL UMP METHODS
    @UsedByGodot
    public void enableDebugMode(String deviceId, int mode) {
        umpLogic.enableDebugMode(deviceId, mode);
    }

    @UsedByGodot
    public void showUmpForm() {
        umpLogic.gatherAndExecute(true, canShow -> {
            GodotAndroidPlugin.this.canShowAds = canShow;
            emitSignal("on_initialization_complete", canShowAds);
            if (canShow) {
                adLogic.initialize();
            }
        });
    }

    @UsedByGodot
    public void showPrivacyForm() {
        // getActivity() is guaranteed to be non-null here as it's called after onMainCreate
        umpLogic.forcePrivacyForm(getActivity(), () -> {
            // Callback for when the privacy form is dismissed/completed.
            // No specific action needed here
        });
    }

    @UsedByGodot
    public void debugResetForm() {
        umpLogic.debugReset();
    }

    // GENERAL AD METHODS

    @UsedByGodot
    public void initialize() { // Checks Google's UMP and also initializes ads if successful
        runOnUiThread(() -> umpLogic.gatherAndExecute(false, canShow -> {
            GodotAndroidPlugin.this.canShowAds = canShow;
            emitSignal("on_initialization_complete", canShowAds);
            if (canShow) {
                adLogic.initialize();
            }
        }));
    }

    @UsedByGodot
    public void loadAndShowBanner(String id, int size, int position) {
        if (!canShowAds) {
            return;
        }

        runOnUiThread(() -> adLogic.loadBanner(id, size, position));
    }

    @UsedByGodot
    public void destroyBanner(String id) {
        if (!canShowAds) {
            return;
        }

        runOnUiThread(() -> adLogic.destroyBanner(id));
    }

    @UsedByGodot
    public void loadInterstitial(String id) {
        if (!canShowAds) {
            emitSignal("on_interstitial_loaded", id, false);
            return;
        }

        runOnUiThread(() -> adLogic.loadInterstitial(id, success -> emitSignal(
                "on_interstitial_loaded", id, success)));
    }

    @UsedByGodot
    public void showInterstitial(String id) {
        if (!canShowAds) {
            emitSignal("on_interstitial_shown", id, 0); // 0 typically means failed/not shown
            return;
        }

        runOnUiThread(() -> adLogic.showInterstitial(id, status -> emitSignal(
                "on_interstitial_shown", id, status)));
    }

    @UsedByGodot
    public void releaseInterstitial(String id) {
        runOnUiThread(() -> adLogic.releaseInterstitial(id));
    }

    @UsedByGodot
    public void loadRewarded(String id) {
        if (!canShowAds) {
            emitSignal("on_rewarded_loaded", id, false);
            return;
        }

        runOnUiThread(() -> adLogic.loadRewarded(id, success -> emitSignal("on_rewarded_loaded",
                id, success)));
    }

    @UsedByGodot
    public void showRewarded(String id) {
        if (!canShowAds) {
            emitSignal("on_reward_shown", id, 0); // 0 typically means failed/not shown
            return;
        }

        runOnUiThread(() -> adLogic.showRewarded(id, status -> { // RewardedShowCallback
            emitSignal("on_reward_shown", id, status);
        }, (amount, type) -> { // RewardedRewardCallback
            emitSignal("on_reward_received", id, amount, type);
        }));
    }

    @UsedByGodot
    public void releaseRewarded(String id) {
        runOnUiThread(() -> adLogic.releaseRewarded(id));
    }
}