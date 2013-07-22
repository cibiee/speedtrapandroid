package com.data.speedtrap.preference;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preference {

  Context myContext;

  public Preference(Context ctx) {
    myContext = ctx;
  }

  /*
   * Store a preference via key -> value
   */
  public void setPreference(String key, String value) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myContext);

    SharedPreferences.Editor editor = prefs.edit();

    editor.putString(key, value);
    editor.commit();  // important!  Don't forget!

  }
  public void setPreferenceInteger(String key, int value) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myContext);

    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(key, value);
    editor.commit();  // important!  Don't forget!

  }

  public int getPreferenceInteger(String key) {
    int val = 0;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myContext);
    val = prefs.getInt(key, 0);

    return val;
  }

  public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    JSONArray a = new JSONArray();
    for (int i = 0; i < values.size(); i++) {
      a.put(values.get(i));
    }
    if (!values.isEmpty()) {
      editor.putString(key, a.toString());
    } else {
      editor.putString(key, null);
    }
    editor.commit();
  }

  public static ArrayList<String> getStringArrayPref(Context context, String key) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String json = prefs.getString(key, null);
    ArrayList<String> urls = new ArrayList<String>();
    if (json != null) {
      try {
        JSONArray a = new JSONArray(json);
        for (int i = 0; i < a.length(); i++) {
          String Video = a.optString(i);

          urls.add(Video);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return urls;
  }
  public void clearAllPreferences() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myContext);

    SharedPreferences.Editor editor = prefs.edit();
    editor.clear();
    editor.commit();  // important!  Don't forget!

  }

  public String getPreference(String key) {
    String val = "";
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myContext);
    val = prefs.getString(key, "");

    return val;
  }


}