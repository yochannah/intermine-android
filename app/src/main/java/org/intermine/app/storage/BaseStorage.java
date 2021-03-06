package org.intermine.app.storage;

/*
 * Copyright (C) 2015 InterMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intermine.app.InterMineApplication;
import org.intermine.app.R;
import org.intermine.app.util.Collections;
import org.intermine.app.util.Strs;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * @author Daria Komkova <Daria.Komkova @ hotmail.com>
 */
public abstract class BaseStorage implements Storage {
    @Inject
    SharedPreferences mPreferences;

    private Context mContext;

    private Gson mMapper;

    private Set<String> mDefaultMineNames;

    public BaseStorage(Context ctx) {
        mContext = ctx;
        mMapper = new Gson();

        InterMineApplication app = InterMineApplication.get(ctx);
        app.inject(this);

        String[] mineNamesArr = ctx.getResources().getStringArray(R.array.mines_names);
        String[] mineNamesUrls = ctx.getResources().getStringArray(R.array.mines_service_urls);
        String[] mineNamesWebAppUrls = ctx.getResources().getStringArray(R.array.mines_web_app_urls);
        mDefaultMineNames = new HashSet<>(Arrays.asList(mineNamesArr));
        for (int i = 0; i < mineNamesArr.length; i++) {
            setMineUrl(mineNamesArr[i], mineNamesUrls[i]);
            setMineWebAppUrl(mineNamesArr[i], mineNamesWebAppUrls[i]);
        }
    }

    @Override
    public String getUserToken(String mineName) {
        return mPreferences.getString(USER_TOKEN_KEY + mineName, Strs.EMPTY_STRING);
    }

    @Override
    public void setUserToken(String mineName, String token) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_TOKEN_KEY + mineName, token);
        editor.commit();
    }

    @Override
    public Set<String> getMineNames() {
        return mPreferences.getStringSet(MINE_NAMES_KEY, mDefaultMineNames);
    }

    @Override
    public void setMineNames(Set<String> mineNames) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(MINE_NAMES_KEY, new HashSet<>(mineNames));
        editor.commit();
    }

    @Override
    public Set<String> getSelectedMineNames() {
        return mPreferences.getStringSet(SELECTED_MINE_NAMES_KEY, mDefaultMineNames);
    }

    @Override
    public void setSelectedMineNames(Set<String> mineNames) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(SELECTED_MINE_NAMES_KEY, new HashSet<>(mineNames));
        editor.commit();
    }

    @Override
    public String getMineUrl(String mine) {
        return mPreferences.getString(MINE_URL_KEY + mine, Strs.EMPTY_STRING);
    }

    @Override
    public void setMineUrl(String mine, String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(MINE_URL_KEY + mine, url);
        editor.commit();
    }

    @Override
    public String getMineWebAppUrl(String mine) {
        return mPreferences.getString(MINE_URL_WEB_APP_KEY + mine, Strs.EMPTY_STRING);
    }

    @Override
    public void setMineWebAppUrl(String mine, String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(MINE_URL_WEB_APP_KEY + mine, url);
        editor.commit();
    }

    @Override
    public void setWorkingMineName(String mineName) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(WORKING_MINE_NAME_KEY, mineName);
        editor.commit();
    }

    @Override
    public String getWorkingMineName() {
        return mPreferences.getString(WORKING_MINE_NAME_KEY, "FlyMine");
    }

    @Override
    public boolean hasUserLearnedDrawer() {
        return mPreferences.getBoolean(USER_LEARNED_DRAWER, false);
    }

    @Override
    public void setUserLearnedDrawer(boolean userLearnedDrawer) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(USER_LEARNED_DRAWER, userLearnedDrawer);
        editor.commit();
    }

    @Override
    public void setTypeFields(String mineName, Map<String, List<String>> typeFields) {
        String json = mMapper.toJson(typeFields);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TYPE_FIELDS_KEY + mineName, json);
        editor.commit();
    }

    @Override
    public Map<String, List<String>> getTypeFields(String mineName) {
        String json = mPreferences.getString(TYPE_FIELDS_KEY + mineName, Strs.EMPTY_STRING);

        if (!json.isEmpty()) {
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            return mMapper.fromJson(json, type);
        }
        return java.util.Collections.emptyMap();
    }

    protected Context getContext() {
        return mContext;
    }
}