package com.example.actapriceproyect.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SessionManager {
    private static final String PREF_NAME = "ActaPricePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER = "user_name";

    private final SharedPreferences prefs;

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String fetchAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserName(String userName) {
        prefs.edit().putString(KEY_USER, userName).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER, "Inspector");
    }

    public void saveUserRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getUserRole() {
        return prefs.getString(KEY_ROLE, "GUEST");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
