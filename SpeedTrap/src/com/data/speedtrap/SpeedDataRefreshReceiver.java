package com.data.speedtrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.utils.GlobalData;
import com.data.speedtrap.vo.VOUser;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SpeedDataRefreshReceiver extends Service {

  private static final String TAG = "com.data.speedtrap";
  private boolean shouldContinue = true;
  private final Handler handler = new Handler();
  private Thread thread;
  private VOUser obUser ;
  private PlaceDataSQL placeData;
  private Preference prefs;
  private int i = 0;
  
  private Runnable mUpdateTwitterNotification = new Runnable() {
    public void run() {
      //Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
      if(i == 0)
      new GetSpeedTrapData().execute();
      
      i++;
    }
  };
  
  @Override
  public void onCreate() {
    Log.i(TAG, "Service onCreate");
    if(obUser == null)
      obUser =  new VOUser(getApplicationContext()) ; 

    if (placeData == null)
      placeData = new PlaceDataSQL(getApplicationContext());  
    if (prefs == null)
      prefs = new Preference(getApplicationContext());

    
    thread = new Thread()
    {
        @Override
        public void run() {
            try {
                while(shouldContinue) {
                    sleep(10000);
                    i = 0;
                    handler.post(mUpdateTwitterNotification);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i(TAG, "Service onStartCommand");

   
//    for (int i = 0; i < 3; i++)
//    {
//      long endTime = System.currentTimeMillis() + 10*1000;
//      while (System.currentTimeMillis() < endTime) {
//        synchronized (this) {
//          try {
//            wait(endTime - System.currentTimeMillis());
//          } catch (Exception e) {
//          }
//        }
//      }
//      Log.i(TAG, "Service running");
//    }
    
    thread.start();
    
    return Service.START_STICKY;
  }

  private class GetSpeedTrapData extends AsyncTask<String, String, String> {
    JSONObject json = null;
    Context context;
    SQLiteDatabase db = null;
    public GetSpeedTrapData() {
      db = placeData
          .getWritableDatabase();
    }

    @Override
    protected String doInBackground(String... params) {
      String url = getApplicationContext().getResources().getString(R.string.servername); 
      String updatedTime = null;
      if(prefs.getPreference("lastUpdatedTime") != null)
      {
        updatedTime = prefs.getPreference("lastUpdatedTime");
      }

      TimeZone tz = TimeZone.getDefault();
      String timeZone = null;
      timeZone = tz.getID();

      List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
      nameValuePair.add(new BasicNameValuePair("getData", "yes"));
      nameValuePair.add(new BasicNameValuePair("token", "trapspeedCodex"));
      nameValuePair.add(new BasicNameValuePair("listAll", "0"));
      nameValuePair.add(new BasicNameValuePair("dateModified", updatedTime));
      nameValuePair.add(new BasicNameValuePair("timeZone", timeZone));
      nameValuePair.add(new BasicNameValuePair("country", GlobalData.countryISO));

      json = SpeedAPIController.getJSONfromURL_Post(url, nameValuePair);

      return null;
    }

    @Override
    protected void onPostExecute(String unused) { 
      try {
        if (json != null) {
          if(json.has("timeout"))
          {            

          }
          else
          {
            JSONArray arrSpeed = json.getJSONArray("speedtrapData");

            for (int i = 0; i < arrSpeed.length(); i++) { 
              JSONObject e = arrSpeed.getJSONObject(i);  
              
              db = (placeData).getWritableDatabase();
              db.delete("CamGeoLocations", "Id=? ",
                  new String[] {  e.getString("id") });
              insertNewLocation(e.getString("id"),e.getString("latitude"), e.getString("longitude"), e.getString("cameraType"), e.getString("direction"), e.getString("time"), e.getString("country"),e.getString("count"),context);
            }

            String resultDate = null;
            Date setDate = new Date();
            SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultDate = sfDate.format(setDate);
            GlobalData.tim = GlobalData.tim+" "+resultDate;
            GlobalData.addedNewLocation = true;
            prefs.setPreference("lastUpdatedTime", resultDate);            
          }
        }
        if(db != null && db.isOpen())
          db.close();
      }
      catch (Exception ex) {
        if(db != null && db.isOpen())
          db.close();
      }
    }
  }

  private void insertNewLocation(String id,String latitude,String longitude,String camType,String direction,String createdTime,String countryCode, String count,Context context) {
    {
      try 
      {
        SQLiteDatabase db = placeData
            .getWritableDatabase();
        ContentValues values;
        values = new ContentValues(); 
        
        values.put("Id", id);
        values.put("Latitude", latitude);
        values.put("Longitude", longitude);
        values.put("CamType", camType);
        values.put("CreatedTime", createdTime);
        values.put("CountryCode", countryCode); 
        values.put("Direction", direction);
        values.put("Count", count);
        db.insert("CamGeoLocations", null, values);  
        db.close();
        //Toast.makeText(context, "Data inserted sucddddcessfully "+latitude+" "+longitude+" "+camType,Toast.LENGTH_LONG).show();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    Log.i(TAG, "Service onBind");
    return null;
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "Service onDestroy");
    shouldContinue = false; 
    try {
      thread.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  } 
  
}