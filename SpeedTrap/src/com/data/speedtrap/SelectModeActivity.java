package com.data.speedtrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SelectModeActivity extends Activity implements OnClickListener{

  private PlaceDataSQL placeData;
  private String camType = null;
  private LocationManager locationManager;
  private boolean hasLocation = false;
  private String strLatitude = null;
  private String strLongitude = null;
  private EditText latituteField;
  private EditText longitudeField;
  private EditText  edHasLocation;
  private LinearLayout btnSpeedCam;
  private LinearLayout btnFixedCam;
  private LinearLayout btnLightCam;
  private Button addNewLocation;
  private ImageView upArrow;
  private ImageView downArrow;
  private ImageView twoHeadArrow;
  private Button start;
  private Button insertCsv;
  private static SensorManager sensorService;
  private Sensor sensor;
  private String direction = null;
  private String selectedDirection = null;
  private String countryName = null;
  private double curLat = 0.0;
  private double curLon = 0.0;
  private Location location;
  //private Location locationx;
  private Location templocation;
  private Typeface tf;
  private TextView txtMobilespeedtrap;
  private TextView txtFixedspeedtrap;
  private TextView txtTrafficlighttrap;
  private TextView txtDirection;
  private ImageView btnSettings;
  private ImageView btnInfo;

  //flag for GPS status
  boolean isGPSEnabled = false;

  // flag for network status
  boolean isNetworkEnabled = false;

  boolean canGetLocation = false;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_mode);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    latituteField = (EditText) findViewById(R.id.edCurrentLat);
    longitudeField = (EditText) findViewById(R.id.edCurrentLon);
    edHasLocation = (EditText) findViewById(R.id.edHasLocation);
    btnSpeedCam = (LinearLayout) findViewById(R.id.btnSpeedCam);
    btnFixedCam = (LinearLayout) findViewById(R.id.btnFixedCam);
    btnLightCam = (LinearLayout) findViewById(R.id.btnLightCam);
    upArrow = (ImageView) findViewById(R.id.upArrow);
    downArrow = (ImageView) findViewById(R.id.downArrow);
    twoHeadArrow = (ImageView) findViewById(R.id.twoHeadArrow);
    btnSettings = (ImageView) findViewById(R.id.btnSettings);
    btnInfo = (ImageView) findViewById(R.id.btnInfo);
    selectCamType(100);

    addNewLocation = (Button) findViewById(R.id.addNewLocation);
    start = (Button) findViewById(R.id.start);
    insertCsv = (Button) findViewById(R.id.insertCsv);

    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");

    txtMobilespeedtrap = (TextView) findViewById(R.id.txtMobilespeedtrap);
    txtMobilespeedtrap.setTypeface(tf, 0);    
    txtFixedspeedtrap = (TextView) findViewById(R.id.txtFixedspeedtrap);
    txtFixedspeedtrap.setTypeface(tf, 0);
    txtTrafficlighttrap = (TextView) findViewById(R.id.txtTrafficlighttrap);
    txtTrafficlighttrap.setTypeface(tf, 0);
    txtDirection = (TextView) findViewById(R.id.txtDirection);
    txtDirection.setTypeface(tf, 0);

    if (placeData == null)
      placeData = new PlaceDataSQL(this);   
    if(locationManager == null)
    {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
 
    isGPSEnabled = locationManager
        .isProviderEnabled(LocationManager.GPS_PROVIDER);

    // getting network status
//    isNetworkEnabled = locationManager
//        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    if (!isGPSEnabled && !isNetworkEnabled) {
//      // no network provider is enabled
//    } else {
//      this.canGetLocation = true;
//      // First get location from Network Provider
//      if (isNetworkEnabled) {
//        locationManager.requestLocationUpdates(
//            LocationManager.NETWORK_PROVIDER,
//            1000, 2, locationListener);
//        Log.d("Network", "Network");
//        if (locationManager != null) {
//          location = locationManager
//              .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//          if(location != null){
//            strLatitude = String.valueOf(location.getLatitude());
//            strLongitude = String.valueOf(location.getLongitude());
//            curLat = location.getLatitude();
//            curLon = location.getLongitude();
//
//            latituteField.setText(strLatitude);
//            longitudeField.setText(strLongitude);     
//          }
//        }
//      }
      // if GPS Enabled get lat/long using GPS Services
      if (isGPSEnabled) {
        if (location == null) {
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locationListener);
          if (locationManager != null) {
            location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
              strLatitude = String.valueOf(location.getLatitude());
              strLongitude = String.valueOf(location.getLongitude());
              curLat = location.getLatitude();
              curLon = location.getLongitude();

              latituteField.setText(strLatitude);
              longitudeField.setText(strLongitude);     
            }
          }
        }
      }
    //}
    btnSpeedCam.setOnClickListener(this);
    btnFixedCam.setOnClickListener(this);
    btnLightCam.setOnClickListener(this);
    upArrow.setOnClickListener(this);
    downArrow.setOnClickListener(this);
    twoHeadArrow.setOnClickListener(this);
    addNewLocation.setOnClickListener(this);
    start.setOnClickListener(this);
    btnSettings.setOnClickListener(this);
    btnInfo.setOnClickListener(this);
    insertCsv.setOnClickListener(this);

    sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    if (sensor != null) {
      sensorService.registerListener(directionSensorEventListener, sensor,
          SensorManager.SENSOR_DELAY_NORMAL);

    } 
  }
  private String avai = "unavailable";
  private LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      hasLocation = true;
      locationChange(location);
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {
      switch (status) {
      case LocationProvider.OUT_OF_SERVICE:
      {
        hasLocation = false;        
        avai = "outofservice";
        break;
      }
      case LocationProvider.TEMPORARILY_UNAVAILABLE:
      {
        hasLocation = true;   
        avai = "unavailable";
        latituteField.setText(provider);
        // edHasLocation.setText("false");
        break;
      }
      case LocationProvider.AVAILABLE:
      {
        hasLocation = true;    
        latituteField.setText(provider);
        avai = "available";
        break;
      }
      default:
        break;
      }
    }
  }; 

  @Override
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(SelectModeActivity.this);
  }

  private void locationChange(Location location) {
    if(location != null){
      strLatitude = String.valueOf(location.getLatitude());
      strLongitude = String.valueOf(location.getLongitude());
      curLat = location.getLatitude();
      curLon = location.getLongitude();

      latituteField.setText(strLatitude);
      longitudeField.setText(strLongitude);     
    }
    else
    {
    }
  }

  private String azimuthe = "0.0";
  private SensorEventListener directionSensorEventListener = new SensorEventListener() {

    public void onSensorChanged(SensorEvent event) {
      float azimuth = event.values[0];      
      getDirection(azimuth);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // TODO Auto-generated method stub

    }
  };

  /**
   * @param azimuth
   */
  private void getDirection(float azimuth) {
    if(azimuth >= 315 || azimuth < 45)
    {
      direction = "N";
    }
    else if(azimuth >= 45 && azimuth < 135)
    {
      direction = "E";
    }
    else if(azimuth >= 135 && azimuth < 225)
    {
      direction = "S";
    }
    else if(azimuth >= 225 && azimuth < 315)
    {
      direction = "W";
    }

    edHasLocation.setText(azimuth+" "+direction);
  }

  public void onClick(View v) {
    if(v.getId() == R.id.btnSpeedCam)
    {
      camType = "1";
      selectCamType(255);
    }
    else if(v.getId() == R.id.btnFixedCam){
      camType = "2";
      selectCamType(255);
    }
    else if(v.getId() == R.id.btnLightCam){
      camType = "3";
      selectCamType(255);
    }
    else if(v.getId() == R.id.upArrow){
      if(direction != null)
      {
        selectedDirection = direction;      
//        if(hasLocation)
//        {
          submit();
        //}
        //insertNewLocation();
      }
      //      else
      //        noGPSLocation();
    }
    else if(v.getId() == R.id.downArrow){
      if(direction != null)
      {
        selectedDirection = reverseDirection(direction);
        //if(hasLocation)
          submit();

        //insertNewLocation();
      }
      //      else
      //        noGPSLocation();
    }
    else if(v.getId() == R.id.twoHeadArrow){
      selectedDirection = "1";
      //if(hasLocation)
        submit();
      //insertNewLocation();
      //      else
      //        noGPSLocation();

    }
    else if(v.getId() == R.id.addNewLocation){
      //insertNewLocation();
    }
    else if(v.getId() == R.id.start){
      Intent intent = new Intent(SelectModeActivity.this, TrackingActivity.class);
      startActivity(intent);
      finish();
      //placeData.copyDBToSDCard();
    }
    else if(v.getId() == R.id.btnSettings){
      if (sensor != null) {
        sensorService.unregisterListener(directionSensorEventListener);
      }
      if(locationManager != null)
        locationManager.removeUpdates(locationListener);
      Intent intent = new Intent(SelectModeActivity.this, SettingsActivity.class);
      intent.putExtra("ACT", "Mode");
      startActivity(intent);
      finish();
    }
    else if(v.getId() == R.id.btnInfo){
      if (sensor != null) {
        sensorService.unregisterListener(directionSensorEventListener);
      }
      if(locationManager != null)
        locationManager.removeUpdates(locationListener);
      Intent intent = new Intent(SelectModeActivity.this, SpeedTrapApproachActivity.class);
      intent.putExtra("ACT", "Mode");
      startActivity(intent);
      finish();
    }
    else if(v.getId() == R.id.insertCsv){
      //insertFromCSV();
    }
  }

  private void submit()
  {
    if(hasLocation && strLatitude != null && strLongitude != null && camType != null && selectedDirection != null)
      new SubmitValuesAsync().execute();

    Intent intent = new Intent();
    intent.setClass(SelectModeActivity.this, StartSpeedTrapActivity.class);
    startActivity(intent);   
    finish();
  }

  /**
   * @param i 
   * 
   */ 
  private void selectCamType(int i) {
    upArrow.getDrawable().mutate().setAlpha(i);
    upArrow.invalidate();
    downArrow.getDrawable().mutate().setAlpha(i);
    downArrow.invalidate();
    twoHeadArrow.getDrawable().mutate().setAlpha(i);
    twoHeadArrow.invalidate();
  }

  private void insertFromCSV() {
    // TODO Auto-generated method stub
    try 
    {
      SQLiteDatabase db = placeData
          .getWritableDatabase();
      //boolean isinarea = isInArea(loc,25,location);
      ContentValues values;
      //AssetManager assetManager = getAssets();
      InputStream is = getAssets().open("Blitzer_mobil_10.csv");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      try {
        String line = null;
        String lat = null;
        String lon = null;
        while ((line = reader.readLine()) != null) {
          String[] RowData = line.split(",");
          lon = RowData[0];
          lat = RowData[1];

          Geocoder geocoderCounty = new Geocoder(SelectModeActivity.this);
          try {
            List<Address> addresses = geocoderCounty.getFromLocation(Double.valueOf(lat),
                Double.valueOf(lon), 1);
            if (addresses.size() > 0)
            {
              Address address = addresses.get(0);
              countryName = address.getCountryCode(); 
            }
          } catch (IOException e) {
            e.printStackTrace();
          }

          values = new ContentValues(); 
          String resultDate = null;
          Date setDate = new Date();
          SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          resultDate = sfDate.format(setDate);
          values.put("Latitude", lat);
          values.put("Longitude", lon);
          values.put("CamType", "0");
          values.put("CreatedTime", resultDate);
          values.put("CountryCode", countryName); 
          values.put("Direction", "");
          db.insert("CamGeoLocations", null, values);   

          //cursor = getRawEvents("select * from CamGeoLocations");

          //          AlertDialog.Builder alertDialog = new AlertDialog.Builder(
          //              SelectModeActivity.this);
          //          alertDialog.setTitle("Values");
          //          alertDialog.setMessage(strLatitude+" "+strLongitude+" "+camType+" "+resultDate+" "+selectedDirection+" "+countryName+" "+cursor.getCount());
          //          alertDialog.setPositiveButton("OK",
          //              new DialogInterface.OnClickListener() {
          //
          //            public void onClick(DialogInterface arg0, int arg1) {
          //            }
          //          });
          //
          //          alertDialog.show();
          // do something with "data" and "value"
        }
      }
      catch (IOException ex) {
        // handle exception
        ex.printStackTrace();
      }
      finally {
        try {
          is.close();
        }
        catch (IOException e) {
          // handle exception
          e.printStackTrace();
        }
      }
      //db.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String reverseDirection(String direction)
  {
    String directionReturn = null;
    if(direction.equals("N"))
    {
      directionReturn = "S";
    }
    else if(direction.equals("S"))
    {
      directionReturn = "N";
    }
    else if(direction.equals("E"))
    {
      directionReturn = "W";
    }
    else if(direction.equals("W"))
    {
      directionReturn = "E";
    }
    return directionReturn;
  }

  private SQLiteDatabase db = null;
  private   Cursor cursor = null;

  private Cursor getRawEvents(String sql) {
    Cursor cursorData = null;
    // SQLiteDatabase db = null;
    try {
      db = (placeData).getReadableDatabase();
      cursorData = db.rawQuery(sql, null);
    } catch (Exception e) {
      if (cursorData != null && !cursorData.isClosed()) {
        cursorData.close();
        cursorData = null;
      }

    } finally {

      // db.close();
    }
    return cursorData;
  }

  private boolean isInArea( double lat2, double lon2, float radius, double lat1, double lon1) {
    int R = 6371;
    //double lat1 = loc.getLatitude();
    //double lon1 = loc.getLongitude();
    //double lat2 = center.getLatitude();
    //double lon2 = center.getLongitude();
    double dLat = (lat2 - lat1) * Math.PI / 180;
    double dLon = (lon2 - lon1) * Math.PI / 180;
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = R * c;
    return (d * 1000 <= radius);
  };

  //private ProgressDialog dialog;
  private class SubmitValuesAsync extends AsyncTask<Void, String, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      String url = getResources().getString(R.string.servername);      
      String strUrl = url + "recordCam=yes&token=trapspeedCodex&latitude="+strLatitude+"&longitude="+strLongitude+"&cameraType="+camType+"&direction="+selectedDirection;//+"&countrycode="+GlobalData.countryISO

      SpeedAPIController.getJSONfromURL_Get_NoReturn(strUrl);



      return null;


      //      try {
      //        if (json != null) 
      //        {
      //          if(json.has("timeout"))
      //          {            
      //            //logedin = false;
      //          }
      //          else
      //          {
      //            JSONArray arrUserId = json.getJSONArray("items");
      //
      //            for (int i = 0; i < arrUserId.length(); i++) {
      //              JSONObject e = arrUserId.getJSONObject(i);
      //              status = e.getString("status");            
      //            }
      //          }
      //        }
      //      }
      //      catch (Exception e) 
      //      {
      //        String str = e.getLocalizedMessage();

      //        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
      //            SelectModeActivity.this);
      //        alertDialog.setTitle("Values");
      //        alertDialog.setMessage(str);
      //        alertDialog.setPositiveButton("OK",
      //            new DialogInterface.OnClickListener() {
      //
      //          public void onClick(DialogInterface arg0, int arg1) {
      //          }
      //        });
      //
      //        alertDialog.show();
      //}
    }

    //    @Override
    //    protected void onPostExecute(String unused) {
    //      if(status != null)
    //      {

    //        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
    //            SelectModeActivity.this);
    //        alertDialog.setTitle("Values");
    //        alertDialog.setMessage(status);
    //        alertDialog.setPositiveButton("OK",
    //            new DialogInterface.OnClickListener() {
    //
    //          public void onClick(DialogInterface arg0, int arg1) {
    //          }
    //        });
    //
    //        alertDialog.show();
    //      }
    //      else
    //      {
    //        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
    //            SelectModeActivity.this);
    //        alertDialog.setTitle("Values");
    //        alertDialog.setMessage("Not inserted");
    //        alertDialog.setPositiveButton("OK",
    //            new DialogInterface.OnClickListener() {
    //
    //          public void onClick(DialogInterface arg0, int arg1) {
    //          }
    //        });
    //
    //        alertDialog.show();
  }
  //insertNewLocation();
  //}

  private void insertNewLocation() {
    if(placeData != null && camType != null)
    {
      try 
      {
        SQLiteDatabase db = placeData
            .getWritableDatabase();
        //boolean isinarea = isInArea(loc,25,location);
        ContentValues values;
        values = new ContentValues(); 
        boolean isinarea = false;
        if(hasLocation && strLatitude != null && strLongitude != null)// && selectedDirection != null
        {
          cursor = getRawEvents("select * from CamGeoLocations");
          if(db == null)
            db = (placeData).getReadableDatabase();

          while (cursor.moveToNext()) {
            String strLat = cursor.getString(cursor
                .getColumnIndex("Latitude"));
            String strLon = cursor.getString(cursor
                .getColumnIndex("Longitude"));

            double latD = Double.valueOf(strLat);
            double lonD = Double.valueOf(strLon);

            isinarea = isInArea(latD,lonD,500,curLat,curLon);

            if(isinarea)
              break;
          }
          if(!isinarea)
          {
            String resultDate = null; 
            Date setDate = new Date();
            SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultDate = sfDate.format(setDate);
            values.put("Latitude", strLatitude);
            values.put("Longitude", strLongitude);
            values.put("CamType", camType);
            values.put("CreatedTime", resultDate);
            values.put("CountryCode", GlobalData.countryISO);
            values.put("Direction", selectedDirection);//selectedDirection
            values.put("Count", "0");
            db.insert("CamGeoLocations", null, values);  
            //cursor = getRawEvents("select * from CamGeoLocations");
            GlobalData.addedNewLocation = true;
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SelectModeActivity.this);
            alertDialog.setTitle("Values");
            alertDialog.setMessage(strLatitude+" "+strLongitude+" "+camType+" "+resultDate+" "+selectedDirection+" "+countryName+" "+cursor.getCount());
            alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface arg0, int arg1) {
              }
            });

            alertDialog.show();
          }
          else
          {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SelectModeActivity.this);
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("Already inserted a fixed cam in this location");
            alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface arg0, int arg1) {
              }
            });
            alertDialog.show();
          }
        }

        Intent intent = new Intent();
        intent.setClass(SelectModeActivity.this, StartSpeedTrapActivity.class);
        startActivity(intent);   
        finish();

        db.close();
      }
      catch (Exception e) { String str = e.getLocalizedMessage();

      //      AlertDialog.Builder alertDialog = new AlertDialog.Builder(
      //          SelectModeActivity.this);
      //      alertDialog.setTitle("Values");
      //      alertDialog.setMessage(str);
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
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (sensor != null) {
      sensorService.unregisterListener(directionSensorEventListener);
    }
    if(locationManager != null)
      locationManager.removeUpdates(locationListener);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // TODO Auto-generated method stub
    return super.onKeyDown(keyCode, event);
  }

  public void onBackPressed() {
    if (sensor != null) {
      sensorService.unregisterListener(directionSensorEventListener);
    }
    if(locationManager != null)
      locationManager.removeUpdates(locationListener);

    Intent intent = new Intent();
    intent.setClass(SelectModeActivity.this, StartSpeedTrapActivity.class);
    startActivity(intent);   
    finish();
  }

  private void noGPSLocation()
  {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
        SelectModeActivity.this);
    alertDialog.setTitle("Warning");
    alertDialog.setMessage("The signal is weak. The GPS location provider is not available at this time.");
    alertDialog.setPositiveButton("OK",
        new DialogInterface.OnClickListener() {

      public void onClick(DialogInterface arg0, int arg1) {
      }
    });

    alertDialog.show();
  }
}
