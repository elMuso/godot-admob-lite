package org.godot.admob.lite;

import android.app.Activity;

import androidx.preference.PreferenceManager;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class UMPLogic {
    private final Activity activity;
    private final ConsentInformation consentInformation;
    private final ConsentDebugSettings.Builder debugSettings;

    public UMPLogic(Activity activity) {
        this.activity = activity;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        this.debugSettings = new ConsentDebugSettings.Builder(activity);
    }

    public void enableDebugMode(String deviceId, int mode) {
        debugSettings.addTestDeviceHashedId(deviceId);
        switch (mode) {
            case 0:
                debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED);
                break;
            case 1:
                debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA);
                break;
            case 2:
                debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE);
                break;
            case 3:
                debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER);
                break;
        }
    }

    public void gatherAndExecute(boolean force, final OnCanShowAds canShowAds) {
        ConsentRequestParameters params =
                new ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings.build()).build();

        consentInformation.requestConsentInfoUpdate(activity, params, () -> {
                    if (needsUMP(activity) || force) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                                formError -> needsPrivacy(activity, canShowAds,
                                        formError == null && consentInformation.canRequestAds())

                        );
                    } else {
                        needsPrivacy(activity, canShowAds, consentInformation.canRequestAds());
                    }
                }, requestConsentError -> needsPrivacy(activity, canShowAds, true)

        );
    }

    private void needsPrivacy(Activity activity, OnCanShowAds canShowAds, boolean result) {
        if (consentInformation.getPrivacyOptionsRequirementStatus() == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity,
                    formError -> canShowAds.onResult(result));
        } else {
            canShowAds.onResult(result);
        }
    }

    public void forcePrivacyForm(Activity activity, Runnable callback) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> callback.run());
    }

    public boolean needsUMP(Activity act) {
        return PreferenceManager.getDefaultSharedPreferences(act).getInt("IABTCF_gdprApplies", 1) == 1;
    }

    public void debugReset() {
        consentInformation.reset();
    }

    public interface OnCanShowAds {
        void onResult(boolean result);
    }
}