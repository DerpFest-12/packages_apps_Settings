package com.google.android.settings.overlay;

import android.content.Context;

import android.os.SystemProperties;

import com.android.settings.fuelgauge.PowerUsageFeatureProvider;
import com.android.settings.fuelgauge.PowerUsageFeatureProviderImpl;
import com.android.settings.accounts.AccountFeatureProvider;
import com.google.android.settings.accounts.AccountFeatureProviderGoogleImpl;
import com.google.android.settings.fuelgauge.PowerUsageFeatureProviderGoogleImpl;

public final class FeatureFactoryImpl extends com.android.settings.overlay.FeatureFactoryImpl {

    private AccountFeatureProvider mAccountFeatureProvider;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;
    private final boolean isPowerUsageProviderGoogle =
                                  SystemProperties.getBoolean("persist.powerusage_provider_google" , true);

    @Override
    public AccountFeatureProvider getAccountFeatureProvider() {
        if (mAccountFeatureProvider == null) {
            mAccountFeatureProvider = new AccountFeatureProviderGoogleImpl();
        }
        return mAccountFeatureProvider;
    }

    @Override
    public PowerUsageFeatureProvider getPowerUsageFeatureProvider(Context context) {
        if (mPowerUsageFeatureProvider == null) {
            if (isPowerUsageProviderGoogle) {
                mPowerUsageFeatureProvider = new PowerUsageFeatureProviderGoogleImpl(
                        context.getApplicationContext());
            } else {
                 mPowerUsageFeatureProvider = new PowerUsageFeatureProviderImpl(
                        context.getApplicationContext());
            }
        }
        return mPowerUsageFeatureProvider;
    }
}
