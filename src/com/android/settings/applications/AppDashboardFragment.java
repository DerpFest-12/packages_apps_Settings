/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.applications;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceGroup;

import com.android.internal.util.derp.derpUtils.LauncherUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import com.derp.support.preferences.SwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Settings page for apps. */
@SearchIndexable
public class AppDashboardFragment extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "AppDashboardFragment";
    private AppsPreferenceController mAppsPreferenceController;

    private static final String KEY_MISC = "app_info_misc";

    private static final String KEY_LAUNCHER_SWITCHER_CATEGORY = "launcher_switcher_category";
    private static final String KEY_LAUNCHER_SWITCHER = "launcher_switcher";

    private ListPreference mLauncherSwitcher;

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AppsPreferenceController(context));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MANAGE_APPLICATIONS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_apps_and_notifications;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.apps;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SpecialAppAccessPreferenceController.class).setSession(getSettingsLifecycle());
        mAppsPreferenceController = use(AppsPreferenceController.class);
        mAppsPreferenceController.setFragment(this /* fragment */);
        getSettingsLifecycle().addObserver(mAppsPreferenceController);

        final HibernatedAppsPreferenceController hibernatedAppsPreferenceController =
                use(HibernatedAppsPreferenceController.class);
        getSettingsLifecycle().addObserver(hibernatedAppsPreferenceController);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLauncherSwitcher = (ListPreference) findPreference(KEY_LAUNCHER_SWITCHER);

        final int status = LauncherUtils.getAvailableStatus(getContext(), true);
        final int launcher = LauncherUtils.getLauncher();
        if (LauncherUtils.hasNoLauncher(status)) {
            getPreferenceScreen().removePreference(
                    getPreferenceScreen().findPreference(KEY_LAUNCHER_SWITCHER_CATEGORY));
        } else {
            ArrayList<CharSequence> entriesList = new ArrayList<CharSequence>();
            ArrayList<CharSequence> valuesList = new ArrayList<CharSequence>();
            if (LauncherUtils.isPixelAvailable(status)) {
                entriesList.add(getResources().getString(R.string.launcher_pixel));
                valuesList.add("0");
            }
            if (LauncherUtils.isDerpAvailable(status)) {
                entriesList.add(getResources().getString(R.string.launcher_derp));
                valuesList.add("1");
            }
            if (LauncherUtils.isLawnchairAvailable(status)) {
                entriesList.add(getResources().getString(R.string.launcher_lawnchair));
                valuesList.add("2");
            }
            if (entriesList.isEmpty()) {
                getPreferenceScreen().removePreference(
                        getPreferenceScreen().findPreference(KEY_LAUNCHER_SWITCHER_CATEGORY));
            } else {
                CharSequence[] entries = new CharSequence[entriesList.size()];
                CharSequence[] values = new CharSequence[valuesList.size()];
                for (int i = 0; i < entriesList.size(); ++i) {
                    entries[i] = entriesList.get(i);
                    values[i] = valuesList.get(i);
                }
                mLauncherSwitcher.setEntries(entries);
                mLauncherSwitcher.setEntryValues(values);
                mLauncherSwitcher.setValue(String.valueOf(launcher));
                mLauncherSwitcher.setSummary(mLauncherSwitcher.getEntry());
                mLauncherSwitcher.setOnPreferenceChangeListener(this);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLauncherSwitcher) {
            int value = Integer.parseInt((String) newValue);
            LauncherUtils.setLauncher(value);
            switch (value) {
                default:
                case 0:
                    mLauncherSwitcher.setSummary(getResources().getString(R.string.launcher_pixel));
                    break;
                case 1:
                    mLauncherSwitcher.setSummary(getResources().getString(R.string.launcher_derp));
                    break;
                case 2:
                    mLauncherSwitcher.setSummary(getResources().getString(R.string.launcher_lawnchair));
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.apps;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context);
                }
            };
}
