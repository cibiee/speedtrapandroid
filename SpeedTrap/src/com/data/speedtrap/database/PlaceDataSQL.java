package com.data.speedtrap.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** Helper to the database, manages versions and creation */
public class PlaceDataSQL extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "Speedtrap";
  private static final int DATABASE_VERSION = 1;

  private Context context;

  public PlaceDataSQL(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    try {
      
      db.execSQL("CREATE TABLE CamGeoLocations (Id VARCHAR(20),Latitude VARCHAR(30), Longitude VARCHAR(30), CamType  VARCHAR(30), Direction  VARCHAR(30), CreatedTime VARCHAR(100), CountryCode VARCHAR(10), Count VARCHAR(10))");
      
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion >= newVersion)
      return;

    if (oldVersion == 1) {
      Log.d("New Version", "Datas can be upgraded");
    }

    Log.d("Sample Data", "onUpgrade : " + newVersion);
  }
}
