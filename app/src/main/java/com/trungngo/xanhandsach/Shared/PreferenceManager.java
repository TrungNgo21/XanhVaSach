package com.trungngo.xanhandsach.Shared;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.User;

public class PreferenceManager {

  private final SharedPreferences sharedPreferences;

  public PreferenceManager(Context context) {
    sharedPreferences =
        context.getSharedPreferences(Constant.KEY_SHARED_CONTEXT, Context.MODE_PRIVATE);
  }

  public void putCurrentUser(UserDto currentUser) {
    SharedPreferences.Editor userEditor = sharedPreferences.edit();
    Gson gson = new Gson();
    String currentUserJson = gson.toJson(currentUser);
    userEditor.putString(Constant.KEY_CURRENT_USER, currentUserJson);
    userEditor.apply();
  }

  public UserDto getCurrentUser() {
    String currentUserJson = sharedPreferences.getString(Constant.KEY_CURRENT_USER, null);
    Gson gson = new Gson();
    return gson.fromJson(currentUserJson, UserDto.class);
  }

  public void putCacheUser(User cacheUser) {
    SharedPreferences.Editor userEditor = sharedPreferences.edit();
    Gson gson = new Gson();
    String currentUserJson = gson.toJson(cacheUser);
    userEditor.putString(Constant.KEY_CACHE_REGISTER, currentUserJson);
    userEditor.apply();
  }

  public User getCacheUser() {
    String currentUserJson = sharedPreferences.getString(Constant.KEY_CACHE_REGISTER, null);
    Gson gson = new Gson();
    return gson.fromJson(currentUserJson, User.class);
  }

  public void clearCacheUser() {
    SharedPreferences.Editor userEditor = sharedPreferences.edit();
    Gson gson = new Gson();
    String currentUserJson = gson.toJson(null);
    userEditor.putString(Constant.KEY_CACHE_REGISTER, currentUserJson);
    userEditor.apply();
  }

  public void putBoolean(String key, boolean value) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, value);
    editor.apply();
  }

  public boolean getBoolean(String key) {
    return sharedPreferences.getBoolean(key, false);
  }

  public void putString(String key, String value) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(key, value);
    editor.apply();
  }

  public String getString(String key) {
    return sharedPreferences.getString(key, null);
  }

  public void clear() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.clear();
    editor.apply();
  }
}
