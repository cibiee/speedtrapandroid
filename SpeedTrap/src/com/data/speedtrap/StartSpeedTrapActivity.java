package com.data.speedtrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;
import com.data.speedtrap.vo.VODistance;
import com.data.speedtrap.vo.VOLocation;
import com.google.android.gcm.GCMRegistrar;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StartSpeedTrapActivity extends Activity{
  private ImageView startButton;
  private ProgressDialog dialog;
  private PlaceDataSQL placeData;
  private Location location;
  private boolean hasLocation = false; 
  private LocationManager locationManager;
  private double latD = 0.0;
  private double lonD = 0.0;
  private   Cursor cursor = null;
  private SQLiteDatabase db = null;
  private EditText edDist;
  private Typeface tf;
  private TextView touchthearrow;
  private boolean onDataLoadComplete = true;
  private ImageView btnSettings;
  private ImageView btnInfo;
  private Preference prefs;
  public static final int REQUEST_EXIT = 1;
  private static final int MSG_SIGNAL_LOST = 1;
  private static final int GPS_SIGNAL_LOST_TIME = 30000; // 30 seconds
  protected PowerManager.WakeLock mWakeLock;
  //flag for GPS status
  boolean isGPSEnabled = false;

  // flag for network status
  boolean isNetworkEnabled = false;

  boolean canGetLocation = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DataFunctions.SetLang(StartSpeedTrapActivity.this); 
    setContentView(R.layout.activity_start_speed_trap);

    if (prefs == null)
      prefs = new Preference(this);

    if (placeData == null)
      placeData = new
      
      PlaceDataSQL(this);  

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int width = size.x;
    int height = size.y;
    
//    Intent intent = new Intent(StartSpeedTrapActivity.this, SpeedDataRefreshReceiver.class);
//    startService(intent);
    
    if(!GlobalData.deviceRegistered)
    {
      GCMRegistrar.checkDevice(StartSpeedTrapActivity.this);
      GCMRegistrar.checkManifest(StartSpeedTrapActivity.this);
      if (GCMRegistrar.isRegistered(StartSpeedTrapActivity.this)) {
        Log.d("info", GCMRegistrar.getRegistrationId(StartSpeedTrapActivity.this));
      }
      final String regId = GCMRegistrar.getRegistrationId(StartSpeedTrapActivity.this);

      if (regId.equals("")) {
        GCMRegistrar.register(StartSpeedTrapActivity.this, "185013872672");
        Log.d("info", GCMRegistrar.getRegistrationId(StartSpeedTrapActivity.this));

      } else {
        Log.d("info", "already registered as" + regId);
      }
      GlobalData.deviceRegistered = true;
    }

    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
    this.mWakeLock.acquire();

    TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    if(manager.getSimCountryIso() != null)
      GlobalData.countryISO = manager.getSimCountryIso().toUpperCase();
    else if(manager.getNetworkCountryIso() != null)
      GlobalData.countryISO = manager.getNetworkCountryIso().toUpperCase();



    if(locationManager == null)
    {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    isGPSEnabled = locationManager
        .isProviderEnabled(LocationManager.GPS_PROVIDER);

    if (isGPSEnabled) {
      if (location == null) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locationListenerAddNewLocations);
        if (locationManager != null) {
          location = locationManager
              .getLastKnownLocation(LocationManager.GPS_PROVIDER);
          if(location != null){
            if(GlobalData.countryISO != null){
              postDelayedGpsLostCheck();
              locationChange(location);
            }
            else
            {
              if(locationManager != null)
              {
                locationManager.removeUpdates(locationListener);
                locationManager.removeUpdates(locationListenerAddNewLocations);
              }
            }    
          }
        }
      }
    }
    else
    {
      if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        showGPSDisabledAlertToUser();    
    }
    // }


    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");

    touchthearrow = (TextView) findViewById(R.id.touchthearrow);
    touchthearrow.setTypeface(tf, 0);

    edDist = (EditText) findViewById(R.id.edDist);
    startButton = (ImageView) findViewById(R.id.startButton);
    startButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {

        if(GlobalData.countryISO != null){

          if(DataFunctions.isNetworkAvailable(StartSpeedTrapActivity.this))
          {
            if(locationManager != null)
            {
              locationManager.removeUpdates(locationListener);   
              locationManager.removeUpdates(locationListenerAddNewLocations);
            }
            mHandler.removeMessages(MSG_SIGNAL_LOST);
            Intent intent = new Intent(StartSpeedTrapActivity.this, SelectModeActivity.class);
            startActivity(intent);
            finish();
          }
          else
          {
            Toast.makeText(StartSpeedTrapActivity.this, getString(R.string.nonet),
                Toast.LENGTH_SHORT).show();
          }

        }
        else
        {
          //          AlertDialog.Builder alertDialog = new AlertDialog.Builder(
          //              StartSpeedTrapActivity.this);
          //          alertDialog.setTitle("Warning");
          //          alertDialog.setMessage("Cannot access the country code");
          //          alertDialog.setPositiveButton("OK",
          //              new DialogInterface.OnClickListener() {
          //
          //            public void onClick(DialogInterface arg0, int arg1) {
          //            }
          //          });
          //
          //          alertDialog.show();
        }
      }
    });
    btnSettings = (ImageView) findViewById(R.id.btnSettings);
    btnSettings.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(locationManager != null)
        {
          locationManager.removeUpdates(locationListener);  
          locationManager.removeUpdates(locationListenerAddNewLocations);
        }
        mHandler.removeMessages(MSG_SIGNAL_LOST);
        Intent intent = new Intent(StartSpeedTrapActivity.this, SettingsActivity.class);
        intent.putExtra("ACT", "Start");
        startActivity(intent);
        finish();
      }
    });
    btnInfo = (ImageView) findViewById(R.id.btnInfo);
    btnInfo.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(locationManager != null)
        {
          locationManager.removeUpdates(locationListener);   
          locationManager.removeUpdates(locationListenerAddNewLocations);
        }
        mHandler.removeMessages(MSG_SIGNAL_LOST);
        Intent intent = new Intent(StartSpeedTrapActivity.this, SpeedTrapApproachActivity.class);
        intent.putExtra("ACT", "Start");
        startActivity(intent);
        finish();
      }
    });
    //    if(!GlobalData.downloadComplete)

    if(GlobalData.countryISO != null){
      if(GlobalData.intialLoad)
      {
        new GetSpeedTrapData().execute();
        GlobalData.intialLoad = false;
      }
      //      if(prefs.getPreference("downloadComplete") == null || !prefs.getPreference("downloadComplete").equals("true"))
      //      {
      //        new GetSpeedTrapData().execute();
      //      }
    }
    else
    {
      //      AlertDialog.Builder alertDialog = new AlertDialog.Builder(
      //          StartSpeedTrapActivity.this);
      //      alertDialog.setTitle("Warning");
      //      alertDialog.setMessage("Cannot access the country code");
      //      alertDialog.setPositiveButton("OK",
      //          new DialogInterface.OnClickListener() {
      //
      //        public void onClick(DialogInterface arg0, int arg1) {
      //        }
      //      });
      //
      //      alertDialog.show();
    }
  }

  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case MSG_SIGNAL_LOST:
        handleGpsSignalLost();
        break;
      }
    };
  };

  private boolean alreadyShowingAlert = false;

  private void showGPSDisabledAlertToUser(){
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
    .setCancelable(false)
    .setPositiveButton("Goto Settings Page To Enable GPS",
        new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int id){
        Intent callGPSSettingIntent = new Intent(
            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(callGPSSettingIntent);
      }
    });
    alertDialogBuilder.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int id){
        dialog.cancel();
      }
    });
    AlertDialog alert = alertDialogBuilder.create();
    alert.show();
  }

  private void handleGpsSignalLost() {
    if(!alreadyShowingAlert)
    {
      //      AlertDialog.Builder alertDialog = new AlertDialog.Builder(
      //          StartSpeedTrapActivity.this);
      //      alertDialog.setTitle("Warning");
      //      alertDialog.setMessage("The signal is weak. The GPS location provider is not available at this time.");
      //      alertDialog.setPositiveButton("OK",
      //          new DialogInterface.OnClickListener() {
      //
      //        public void onClick(DialogInterface arg0, int arg1) {
      //          alreadyShowingAlert = false;
      //        }
      //      });
      //
      //      alertDialog.show();
      alreadyShowingAlert = true;
    }
  }

  private void postDelayedGpsLostCheck() {
    mHandler.removeMessages(MSG_SIGNAL_LOST);
    mHandler.sendEmptyMessageDelayed(MSG_SIGNAL_LOST, GPS_SIGNAL_LOST_TIME);
  }

  @Override 
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(StartSpeedTrapActivity.this);
  }

  private LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      if(GlobalData.countryISO != null){
        postDelayedGpsLostCheck();
        locationChange(location);
      }
      else
      {
        if(locationManager != null)
        {
          locationManager.removeUpdates(locationListener);
          locationManager.removeUpdates(locationListenerAddNewLocations);
        }

        //        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
        //            StartSpeedTrapActivity.this);
        //        alertDialog.setTitle("Warning");
        //        alertDialog.setMessage("Cannot access the country code");
        //        alertDialog.setPositiveButton("OK",
        //            new DialogInterface.OnClickListener() {
        //
        //          public void onClick(DialogInterface arg0, int arg1) {
        //          }
        //        });
        //
        //        alertDialog.show();
      }
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
  };
  
  
  private LocationListener locationListenerAddNewLocations = new LocationListener() {
    public void onLocationChanged(Location location) {
      //new GetSpeedTrapData().execute();
      new GetSpeedTrapDataInserted().execute();
      Log.d("Fet", "sgsg");
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
  };
  
  
  private String COL_X= "Latitude";
  private String COL_Y= "Longitude";
  private double cuadras = (double)-0.02  ; 
  private float distance = (float) 0.0;
  private ArrayList<VODistance> distanceVOArl=new ArrayList<VODistance>();

  private int d = 0;
  private int j = 0;
  private int t = 0;

  private void locationChange(Location location) {
    int count = 0;
    if(location != null){ 
      try{
        getDirection(location.getBearing());
        edDist.setText("fsfsf "+directionx);  
        distanceVOArl.clear();
        if(!GlobalData.isInsideCircle || GlobalData.addedNewLocation || (prefs.getPreference("settingsChanged") != null && prefs.getPreference("settingsChanged").equals("true")))
        {
          if( GlobalData.distanceArNew.size() > 0) 
            GlobalData.distanceArNew.clear();
          GlobalData.curLocation.setLatitude(location.getLatitude());
          GlobalData.curLocation.setLongitude(location.getLongitude());
          String sql = "select Latitude,Longitude,CamType,Direction,Count from CamGeoLocations";
          boolean settingsTrue = false;
          if(prefs.getPreference("settings") != null && prefs.getPreference("settings").equals("on"))
          {
            settingsTrue = true;
          }
          cursor = getRawEvents(sql);
          //          edDist.setText(GlobalData.isInsideCircle+
          //              " fdf "+ GlobalData.addedNewLocation+" "+prefs.getPreference("settingsChanged")+" "+cursor.getCount());  
          String st ="";
          if(cursor.getCount() >0)
          {
            while (cursor.moveToNext()) {
              t++;
              String strLat = cursor.getString(cursor
                  .getColumnIndex("Latitude"));
              String strLon = cursor.getString(cursor
                  .getColumnIndex("Longitude"));
              String camType = cursor.getString(cursor
                  .getColumnIndex("CamType"));
              String direction = cursor.getString(cursor
                  .getColumnIndex("Direction"));
              String strCount = cursor.getString(cursor
                  .getColumnIndex("Count"));
              if(strCount == null)
                strCount = "0";
              st = st+" "+strLat+" "+strLon+" "+camType+" "+direction+" "+strCount;
              edDist.setText(st);
              latD = Double.valueOf(strLat);
              lonD = Double.valueOf(strLon);

              Location loc = new Location("Point A");
              loc.setLongitude(lonD);
              loc.setLatitude(latD); 
              if(location.distanceTo(loc)< 7000)
              {
                if(camType.equals("1"))
                {
                  // edDist.setText(camType);
                  int camCount = Integer.parseInt(strCount);

                  if(!settingsTrue)
                  {
                    VOLocation voLocation = new VOLocation();
                    voLocation.setLocation(loc);
                    voLocation.setCamType(camType);
                    voLocation.setDirection(direction);
                    voLocation.setPassedTheLocation("0");
                    GlobalData.distanceArNew.add(voLocation);
                    d++;
                  }
                  else
                  {
                    if(camCount >= 2)
                    { 
                      VOLocation voLocation = new VOLocation();
                      voLocation.setLocation(loc);
                      voLocation.setCamType(camType);
                      voLocation.setDirection(direction);
                      voLocation.setPassedTheLocation("0");
                      GlobalData.distanceArNew.add(voLocation);
                      d++;
                    }
                  }
                } 
                else if(camType.equals("2") || camType.equals("3"))
                {
                  VOLocation voLocation = new VOLocation();
                  voLocation.setLocation(loc);
                  voLocation.setCamType(camType);
                  voLocation.setDirection(direction);
                  voLocation.setPassedTheLocation("0"); 
                  GlobalData.distanceArNew.add(voLocation);
                  j++;
                }
              }
            }
          }
          if(!GlobalData.isInsideCircle)
            GlobalData.isInsideCircle = true;
          if(GlobalData.addedNewLocation)
            GlobalData.addedNewLocation = false;
          if(prefs.getPreference("settingsChanged") != null && prefs.getPreference("settingsChanged").equals("true"))
            prefs.setPreference("settingsChanged", "false");
        }
        if(!DataFunctions.pointIsInCircle(location, GlobalData.curLocation, 7000))
        {
          GlobalData.isInsideCircle = false;
        }
        String str = "  ";//GlobalData.tim+
        //edDist.setText(" "+GlobalData.distanceArNew.size());  
        for(int k=0;k<GlobalData.distanceArNew.size();k++)
        {
          distance = location.distanceTo(GlobalData.distanceArNew.get(k).getLocation()); 


          String direction = GlobalData.distanceArNew.get(k).getDirection();
          boolean isD = isDecreasing(distance,k);
          str += "   "+Math.round(distance)+" "+isD+" "+GlobalData.distanceArNew.get(k).getDirection();
          if(distance <= 510.0  && isD && (direction.equals(directionx) || direction.equals("1") || direction.equals("0")))//&& isDecreasing(distance//&& GlobalData.distanceArNew.get(k).getPassedTheLocation().equals("0")
          {
            VODistance voDistance = new VODistance();
            voDistance.setDistance(distance);
            VOLocation vLoc = new VOLocation();
            vLoc.setLocation(GlobalData.distanceArNew.get(k).getLocation());
            vLoc.setCamType(GlobalData.distanceArNew.get(k).getCamType());
            vLoc.setDirection(GlobalData.distanceArNew.get(k).getDirection());
            voDistance.setLocation(vLoc);
            distanceVOArl.add(voDistance);

            count++;
          }          
        }
        if(count > 0)
        {
          VODistance vominDistance = Collections.min(distanceVOArl, new DistanceComparator());

          GlobalData.glVOLocation = new VOLocation();
          GlobalData.glVOLocation.setLocation(vominDistance.getLocation().getLocation());
          GlobalData.glVOLocation.setCamType(vominDistance.getLocation().getCamType());
          GlobalData.glVOLocation.setDirection(vominDistance.getLocation().getDirection());

          locationManager.removeUpdates(locationListener);   
          locationManager.removeUpdates(locationListenerAddNewLocations);
          mHandler.removeMessages(MSG_SIGNAL_LOST);
          Intent intent = new Intent();
          intent.setClass(StartSpeedTrapActivity.this, TrackingActivity.class);
          startActivity(intent); 
          finish();
        }
        else
        {
          edDist.setText(GlobalData.distanceArNew.size()+" "+str+" "+directionx); //GlobalData.s+" dfv "+
        }

        if(db != null && db.isOpen())
          db.close();
      }
      catch(Exception e)
      {
      }
    }
  }
  private String directionx = null;
  private void getDirection(float azimuth) {
    if(azimuth >= 315 || azimuth < 45)
    {
      directionx = "S";
    }
    else if(azimuth >= 45 && azimuth < 135)
    {
      directionx = "W";
    }
    else if(azimuth >= 135 && azimuth < 225)
    {
      directionx = "N";
    }
    else if(azimuth >= 225 && azimuth < 315)
    {
      directionx = "E";
    }
  }
  private ArrayList<Float> distanceAscAr = new ArrayList<Float>(); 
  public boolean IsSorted(float distance)
  {
    boolean sorted = true;
    String strAsc = "";
    if(distanceAscAr.size() < 3)
    {
      distanceAscAr.add(distance);
    }
    else
    {
      distanceAscAr.remove(0);
      distanceAscAr.add(2, distance);
    }
    String str = "";
    for(int k = 0; k < distanceAscAr.size(); k++)
    {
      str += distanceAscAr.get(k)+" ";
    }

    if(distanceAscAr.size() == 1)
      sorted = false;

    for (int i = 1; i < distanceAscAr.size(); i++)
    {
      if (distanceAscAr.get(i-1) > distanceAscAr.get(i))
      {
        sorted = false;
        strAsc = "Desc";
      }
    }

    //edDistance.setText(str+" dist "+strAsc);
    return sorted;
  }


  private float tempDistance = (float) 0.0;

  private ArrayList<Float> distanceAr= new ArrayList<Float>();

  private boolean isDecreasing(float distance,int i)
  {
    boolean decreased = false;
    if(distanceAr.size() < (i+1))
      distanceAr.add(distance);
    else
    {
      if(distanceAr.get(i) > distance)
        decreased = true;

      distanceAr.set(i, distance);
      //tempDistance = distance;
    }
    return decreased;    
  }

  class DistanceComparator implements Comparator<VODistance> {

    public int compare(VODistance first, VODistance second) {
      return Float.valueOf(first.getDistance()).compareTo(Float.valueOf(second.getDistance()));
    }

  }

  private Cursor getRawEvents(String sql) {
    Cursor cursor = null;
    try {
      db = (placeData).getReadableDatabase();
      cursor = db.rawQuery(sql, null);
      // startManagingCursor(cursor);
    } catch (Exception e) {

      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
        cursor = null;
      }
    } 
    return cursor;
  }

  private class GetSpeedTrapData extends AsyncTask<String, String, String> {
    JSONObject json = null;

    @Override
    protected String doInBackground(String... params) {
      //String url = getResources().getString(R.string.servername);
      String url = getResources().getString(R.string.servername); 
      //String strUrl = url + "getData=yes&listAll=1";
      String updatedTime = "";
      if(prefs.getPreference("lastUpdatedTime") != null)
      {
        updatedTime = prefs.getPreference("lastUpdatedTime");
      }      

      List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
      nameValuePair.add(new BasicNameValuePair("getData", "yes"));
      nameValuePair.add(new BasicNameValuePair("token", "trapspeedCodex"));
      if(updatedTime != "")
      {
        TimeZone tz = TimeZone.getDefault();
        String timeZone = null;
        timeZone = tz.getID();

        nameValuePair.add(new BasicNameValuePair("listAll", "0"));
        nameValuePair.add(new BasicNameValuePair("dateModified", updatedTime));
        nameValuePair.add(new BasicNameValuePair("timeZone", timeZone));
      }
      else
      {
        nameValuePair.add(new BasicNameValuePair("listAll", "1"));
      }

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
            //dataLoad = false;
          }
          else
          {
            JSONArray arrSpeed = json.getJSONArray("speedtrapData");

            for (int i = 0; i < arrSpeed.length(); i++) { //arrSpeed.length()
              JSONObject e = arrSpeed.getJSONObject(i);  

              db = (placeData).getWritableDatabase();
              db.delete("CamGeoLocations", "Id=? ",
                  new String[] {  e.getString("id") });

              insertNewLocation(e.getString("id"),e.getString("latitude"), e.getString("longitude"), e.getString("cameraType"), e.getString("direction"), e.getString("time"), e.getString("country"));
            }
            String resultDate = null;
            Date setDate = new Date();
            SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultDate = sfDate.format(setDate);

            prefs.setPreference("lastUpdatedTime", resultDate);
          }
        }
        if(db != null && db.isOpen())
          db.close();
      }
      catch (Exception ex) {

      }

    }

  }

  private void insertNewLocation(String id,String latitude,String longitude,String camType,String direction,String createdTime,String countryCode) {
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
        db.insert("CamGeoLocations", null, values);  

        db.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_start_speed_trap, menu);
    return true;
  }  

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      onBackPressed();
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onDestroy() {
    this.mWakeLock.release();
    super.onDestroy();
  }

  public void onBackPressed() {
    if(locationManager != null)
    {
      locationManager.removeUpdates(locationListener);
      locationManager.removeUpdates(locationListenerAddNewLocations);
    }
    mHandler.removeMessages(MSG_SIGNAL_LOST);
    
    //stopService(new Intent(StartSpeedTrapActivity.this, SpeedDataRefreshReceiver.class));
    finish();
    //System.exit(0);
  }
  
  private class GetSpeedTrapDataInserted extends AsyncTask<String, String, String> {
    JSONObject json = null;
    Context context;
    SQLiteDatabase db = null;
    public GetSpeedTrapDataInserted() {
      // TODO Auto-generated constructor stub
      db = placeData
          .getWritableDatabase();
    }

    @Override
    protected String doInBackground(String... params) {
      String url = getResources().getString(R.string.servername); 
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
}
