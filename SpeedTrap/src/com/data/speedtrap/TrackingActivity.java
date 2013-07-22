package com.data.speedtrap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;
import com.data.speedtrap.vo.VODistance;
import com.data.speedtrap.vo.VOLocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TrackingActivity extends Activity {

  private EditText latituteField;
  private EditText longitudeField;
  private Button btnCalculate;
  private Location location;
  private boolean hasLocation = false;
  private LocationManager locationManager;
  public static final String PREFS_NAME = "MyPrefsFile";
  private String latShared = "0.0";
  private String lonShared = "0.0";
  private String countyName = null;
  private double latD = 0.0;
  private double lonD = 0.0;
  private MediaPlayer mp;
  private ImageView imgTrack;
  private ImageView warningButton;
  private ImageView warningSymbol;
  private LinearLayout rlWarningButton;
  private EditText edDistance; 
  private EditText edSpeed;
  private EditText edDirection;
  private ToggleButton tbMute;
  private TextView txtWarning;
  private TextView txtWarningMsg;
  private TextView txtAddress;
  private static SensorManager sensorService;
  private Sensor sensor;
  private SQLiteDatabase db = null;
  private   Cursor cursor = null;
  private PlaceDataSQL placeData;
  private String direction = "";
  private ArrayList<Float> distanceAscAr = new ArrayList<Float>();  
  private Typeface tf_bold;
  private Typeface tf;
  private VOLocation voLocation = null;
  boolean isGPSEnabled = false;

  // flag for network status
  boolean isNetworkEnabled = false;

  boolean canGetLocation = false;
  
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tracking);

    if (placeData == null) 
      placeData = new PlaceDataSQL(TrackingActivity.this);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    latituteField = (EditText) findViewById(R.id.edCurrentLat);
    longitudeField = (EditText) findViewById(R.id.edCurrentLon);
    edDistance = (EditText) findViewById(R.id.edDistance); 
    edSpeed = (EditText) findViewById(R.id.edSpeed); 
    edDirection = (EditText) findViewById(R.id.edDirection); 
    rlWarningButton = (LinearLayout) findViewById(R.id.rlWarningButton);
    txtWarning  = (TextView) findViewById(R.id.txtWarning);
    txtWarningMsg  = (TextView) findViewById(R.id.txtWarningMsg);
    txtAddress  = (TextView) findViewById(R.id.txtAddress);
    tbMute  = (ToggleButton) findViewById(R.id.tbMute);
    voLocation = GlobalData.glVOLocation;
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/ARIAL.TTF");
    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");
    txtWarning.setTypeface(tf_bold, 0);
    txtWarningMsg.setTypeface(tf, 0);
    btnCalculate = (Button) findViewById(R.id.btnCalculate);
    imgTrack = (ImageView) findViewById(R.id.trackView);
    mp = new MediaPlayer(); 
    rlWarningButton.setVisibility(View.INVISIBLE);
    new GetAddressData().execute();
    if(locationManager == null)
    {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, locationListener); 
    
    isGPSEnabled = locationManager
        .isProviderEnabled(LocationManager.GPS_PROVIDER);

    // getting network status
//    isNetworkEnabled = locationManager
//        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    if (!isGPSEnabled && !isNetworkEnabled) {
////      if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
////        showGPSDisabledAlertToUser();
//    } else {
//      this.canGetLocation = true;
//      // First get location from Network Provider
//      if (isNetworkEnabled) {
//        locationManager.requestLocationUpdates(
//            LocationManager.NETWORK_PROVIDER,
//            0, 0, locationListener);
//        Log.d("Network", "Network");
//        if (locationManager != null) {
//          location = locationManager
//              .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//          if(location != null){
//            if(GlobalData.countryISO != null){
//              locationChange(location);
//            }
//            else
//            {
//              if(locationManager != null)
//                locationManager.removeUpdates(locationListener);
//
////              AlertDialog.Builder alertDialog = new AlertDialog.Builder(
////                  TrackingActivity.this);
////              alertDialog.setTitle("Warning");
////              alertDialog.setMessage("Cannot access the country code");
////              alertDialog.setPositiveButton("OK",
////                  new DialogInterface.OnClickListener() {
////
////                public void onClick(DialogInterface arg0, int arg1) {
////                }
////              });
////
////              alertDialog.show();
//            }
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
              if(GlobalData.countryISO != null){
                locationChange(location);
              }
              else
              {
                if(locationManager != null)
                  locationManager.removeUpdates(locationListener);

//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
//                    TrackingActivity.this);
//                alertDialog.setTitle("Warning");
//                alertDialog.setMessage("Cannot access the country code");
//                alertDialog.setPositiveButton("OK",
//                    new DialogInterface.OnClickListener() {
//
//                  public void onClick(DialogInterface arg0, int arg1) {
//                  }
//                });
//
//                alertDialog.show();
              }    
            }
          }
        }
      }
    //}
    
    sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    if (sensor != null) {
      sensorService.registerListener(directionSensorEventListener, sensor,
          SensorManager.SENSOR_DELAY_NORMAL);

    } else {
      //      Toast.makeText(this, "ORIENTATION Sensor not found",
      //          Toast.LENGTH_LONG).show();
    }

    tbMute.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
        {
          if(mp!=null && mp.isPlaying())
            mp.setVolume(0, 0);
        }
        else
        {
          if(mp!=null && mp.isPlaying())
            mp.setVolume(0, 1);
        }
      }
    });
//
//    if(DataFunctions.isNetworkAvailable(getApplicationContext()))
//    { 
//      new SubmitValuesAsync().execute();
//    }
  }

  public void onBufferingUpdate(MediaPlayer arg0, int percent) {
  }
  public void onCompletion(MediaPlayer arg0) {
  }
  public void onPrepared(MediaPlayer mediaplayer) {
  }

  private SensorEventListener directionSensorEventListener = new SensorEventListener() {

    public void onSensorChanged(SensorEvent event) {

      if ( locC == null ) return;

      float azimuth = event.values[0];
      float baseAzimuth = azimuth;

      GeomagneticField geoField = new GeomagneticField( Double
          .valueOf( locC.getLatitude() ).floatValue(), Double
          .valueOf( locC.getLongitude() ).floatValue(),
          Double.valueOf( locC.getAltitude() ).floatValue(),
          System.currentTimeMillis() );

      azimuth -= geoField.getDeclination(); // converts magnetic north into true north

      // Store the bearingTo in the bearTo variable
      float bearTo = locC.bearingTo( voLocation.getLocation() );

      // If the bearTo is smaller than 0, add 360 to get the rotation clockwise.
      if (bearTo < 0) {
        bearTo = bearTo + 360;
      }

      //This is where we choose to point it
      float directions = bearTo - azimuth;

      // If the direction is smaller than 0, add 360 to get the rotation clockwise.
      if (directions < 0) {
        directions = directions + 360;
      }
      rotateImageView( imgTrack, drawab, directions );
      //edDistance.setText(directions+"");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // TODO Auto-generated method stub

    }
  };

  private void rotateImageView( ImageView imageView, int drawable, float rotate ) {

    // Decode the drawable into a bitmap
    if(drawab != 0)
    {
      Bitmap bitmapOrg = BitmapFactory.decodeResource( getResources(),
          drawable );

      // Get the width/height of the drawable
      DisplayMetrics dm = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(dm);
      int width = bitmapOrg.getWidth(), height = bitmapOrg.getHeight();

      // Initialize a new Matrix
      Matrix matrix = new Matrix();

      // Decide on how much to rotate
      rotate = rotate % 360;

      // Actually rotate the image
      matrix.postRotate( rotate, width, height );

      // recreate the new Bitmap via a couple conditions
      Bitmap rotatedBitmap = Bitmap.createBitmap( bitmapOrg, 0, 0, width, height, matrix, true );

      //imageView.setImageBitmap( rotatedBitmap );
      imageView.setImageDrawable(new BitmapDrawable(getResources(), rotatedBitmap));
      imageView.setScaleType( ScaleType.CENTER );
    }
  }
  private float fromD = 0;
  private LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      //      hasLocation = true;
      //
      //      GeoLocation loc = new GeoLocation();
      //      loc.getLocation(TrackingActivity.this, locationResult);

      locationChange(location);

    }

    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
  };
  private ArrayList<VODistance> distanceAr=new ArrayList<VODistance>();
  private boolean isCovered = false;
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

  
  private class GetAddressData extends AsyncTask<String, String, String> {
    
    private JSONObject resJson = null;
    
    @Override
    protected String doInBackground(String... params) { 
      String strUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+voLocation.getLocation().getLatitude()+","+voLocation.getLocation().getLongitude()+"&sensor=true";

      resJson = SpeedAPIController.getJSONfromURL_Get(strUrl);      
      return null;
    }

    @Override
    protected void onPostExecute(String unused) {
      if(resJson != null) 
      {
        try {
          if(resJson.has("results")){
          JSONObject js = (JSONObject) resJson.getJSONArray("results").get(0);
          String str = (String) js.get("formatted_address");
          str = str.substring(0, str.lastIndexOf(", ")) + "";
          txtAddress.setText(str);
          }
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  private Location locC = null;
  private int drawab = 0;
  private void locationChange(Location location) {
    // TODO Auto-generated method stub
    if(location != null){
      String str1 = String.valueOf(location.getLatitude());
      String str2 = String.valueOf(location.getLongitude());
      latituteField.setText(str1);
      longitudeField.setText(str2);
      getDirection(location.getBearing());
      locC = location;
      //getAddress();
      
      float distance = location.distanceTo(voLocation.getLocation());
      String cam = voLocation.getCamType();
      String direction = voLocation.getDirection();

      //edDistance.setText(distance+" "+cam+" "+directionx);
      if(distance <= 70.0)
        isCovered = true;

      if(distance <= 510.0 && isDecreasing(distance) && (direction.equals(directionx) || direction.equals("1") || direction.equals("0")))//!IsSorted(distance)  && isDecreasing(distance) || !isCovered)
      {
        showFlash();
        imgTrack.setVisibility(View.VISIBLE);
        rlWarningButton.setVisibility(View.VISIBLE);
        tbMute.setVisibility(View.VISIBLE);
        AssetFileDescriptor ins = null;
        if(mp != null)
        {
          mp.reset();
          mp.setLooping(true);
        }
        try {
          if(distance <= 100.0)
          {            
            imgTrack.setImageResource(R.drawable.warning_red);
            drawab = R.drawable.warning_red;
            ins = getAssets().openFd("beep5Second.mp3");
            mp.setDataSource(ins.getFileDescriptor(), ins.getStartOffset(),  ins.getLength() );
          }
          else if(distance <= 300.0)
          {
            imgTrack.setImageResource(R.drawable.warning_yellow);
            drawab = R.drawable.warning_yellow;
            ins = getAssets().openFd("beep2Second.mp3");
            mp.setDataSource(ins.getFileDescriptor(), ins.getStartOffset(),  ins.getLength());

          }
          else if(distance <= 510.0)
          {             
            imgTrack.setImageResource(R.drawable.warning_green);
            drawab = R.drawable.warning_green;
            ins = getAssets().openFd("beep3Second.mp3");
            mp.setDataSource(ins.getFileDescriptor(), ins.getStartOffset(),  ins.getLength());
          }
          txtWarningMsg.setText("You are approximately " + Math.round(distance)+" ms away from");
          if(cam.equals("1"))
          {
            rlWarningButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.warning_display_mobile));
            txtWarning.setText(getResources().getString(R.string.mobilespeedtrap));
          }
          else if(cam.equals("2"))
          {
            rlWarningButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.warning_display_fixed));
            txtWarning.setText(getResources().getString(R.string.fixedspeedtrap));
          }
          else if(cam.equals("3"))
          {
            rlWarningButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.warning_display_traffic));
            txtWarning.setText(getResources().getString(R.string.trafficlighttrap));
          }

          ins.close();
        }
        catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } 
        try {
          mp.prepare();
        } catch (IllegalStateException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) { 
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        mp.start();
      }
      else
      {


        imgTrack.clearAnimation();
        imgTrack.setVisibility(View.INVISIBLE);
        rlWarningButton.setVisibility(View.INVISIBLE);
        tbMute.setVisibility(View.INVISIBLE);
        if(mp!=null && mp.isPlaying())
          mp.stop();

        if (placeData != null) {
          placeData.close();
        }
        if (sensor != null) {
          sensorService.unregisterListener(directionSensorEventListener);
        }        

        locationManager.removeUpdates(locationListener);
        if(voLocation.getCamType().equals("1") && isCovered)
        {
          Intent intent = new Intent();
          intent.setClass(TrackingActivity.this, SeeSpeedTrapActivity.class);
          startActivity(intent);   
          finish();
        }
        else
        {
          GlobalData.glVOLocation = null;
          Intent intent = new Intent();
          intent.setClass(TrackingActivity.this, StartSpeedTrapActivity.class);
          startActivity(intent);   
          finish();
        }
      }
      //}
    }
    else
    {
    }
  }

  private int m = 0;
  void getAddress(){
    if(m == 0)
    {
      try{
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = 
            gcd.getFromLocation(voLocation.getLocation().getLatitude(), voLocation.getLocation().getLongitude(),100);
        if (addresses.size() > 0) {
          Address returnedAddress = addresses.get(0);
          StringBuilder strReturnedAddress = new StringBuilder("\n");
          for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
          }
          txtAddress.setText(strReturnedAddress.toString());
        }
      }
      catch(IOException ex){
        txtAddress.setText(ex.getMessage().toString());
      }
      m = 1;
    }
  }

  class DistanceComparator implements Comparator<VODistance> {

    public int compare(VODistance first, VODistance second) {
      return Float.valueOf(first.getDistance()).compareTo(Float.valueOf(second.getDistance()));
    }

  }

  private float tempDistance = (float) 0.0;

  private boolean isDecreasing(float distance)
  {
    boolean decreased = false;

    if(tempDistance == 0.0 ||tempDistance > distance)
      decreased = true;

    tempDistance = distance;

    return decreased;    
  }

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
  
  private class SubmitValuesAsync extends AsyncTask<Void, String, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      String url = getResources().getString(R.string.servername);  
      String resultDate = null;

      Date setDate = new Date();
      SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      resultDate = sfDate.format(setDate);
      resultDate = resultDate.replace(" ", "%20");

      String strUrl = url + "recordWarning=yes&token=trapspeedCodex&latitude="+GlobalData.glVOLocation.getLocation().getLatitude()+"&longitude="+GlobalData.glVOLocation.getLocation().getLongitude()+"&camera_type="+GlobalData.glVOLocation.getCamType()+"&time="+resultDate;//+"&countrycode="+GlobalData.countryISO
      //edDistance.setText(strUrl);
      SpeedAPIController.getJSONfromURL_Get_NoReturn(strUrl);

      return null;
    }
  }

  public float findMin(float[] array) {
    // Finds and returns min
    float min = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }
    return min;
  }

  private void showFlash()
  {    
    Animation animation = new AlphaAnimation(1.0f, 0.3f); 
    animation.setDuration(200);
    animation.setInterpolator(new LinearInterpolator()); 
    animation.setRepeatCount(Animation.INFINITE); 
    //animation.setRepeatMode(Animation.REVERSE);    
    imgTrack.startAnimation(animation);   
  }

  public void onPause(Bundle savedInstanceState) {
    locationManager.removeUpdates(locationListener);
    locationManager=null;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
        && keyCode == KeyEvent.KEYCODE_BACK) {
      onBackPressed();
    }

    return super.onKeyDown(keyCode, event);
  }

  public void onBackPressed() { 
    locationManager.removeUpdates(locationListener);

    locationManager = null;

    imgTrack.clearAnimation();
    if(mp!= null)
    {
      if(mp.isPlaying())
      {
        mp.stop();           
      }      
      mp.release();
    }    

    if (placeData != null) {
      placeData.close();
    }
    if (sensor != null) {
      sensorService.unregisterListener(directionSensorEventListener);
    }
    if(locationManager == null)
      finish();    
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    imgTrack.clearAnimation();
    if (sensor != null) {
      sensorService.unregisterListener(directionSensorEventListener);
    }
  }
}
