package com.data.speedtrap.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker implements LocationListener {

  private Context mContext;
  public boolean isGPSEnabled = false;
  private boolean canGetLocation = false;
  private Location location;
  private double latitude; 
  private double longitude;

  // The minimum distance to change Updates in meters
  private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

  // The minimum time between updates in milliseconds
  private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

  // Declaring a Location Manager
  protected LocationManager locationManager;

  public GPSTracker(Context context) {
    this.mContext = context;
    getLocation();
  }

  public Location getLocation() 
  {
    try 
    {
      locationManager = (LocationManager) mContext
          .getSystemService(Context.LOCATION_SERVICE);

      // getting GPS status
      isGPSEnabled = locationManager
          .isProviderEnabled(LocationManager.GPS_PROVIDER);

      Log.v("isGPSEnabled", "=" + isGPSEnabled);

      if (isGPSEnabled) 
      {
        if (location == null) 
        {
          locationManager.requestLocationUpdates(
              LocationManager.GPS_PROVIDER,
              MIN_TIME_BW_UPDATES,
              MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
          Log.d("GPS Enabled", "GPS Enabled");
          if (locationManager != null) 
          {
            location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) 
            {
              latitude = location.getLatitude();
              longitude = location.getLongitude();
            }
          }
        }
      }
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }

    return location;
  }
  
  /**
   * Stop using GPS listener Calling this function will stop using GPS in your
   * app
   * */
  public void stopUsingGPS() {
      if (locationManager != null) {
          locationManager.removeUpdates(GPSTracker.this);
      }
  }

  /**
   * Function to get latitude
   * */
  public double getLatitude() {
      if (location != null) {
          latitude = location.getLatitude();
      }

      // return latitude
      return latitude;
  }

  /**
   * Function to get longitude
   * */
  public double getLongitude() {
      if (location != null) {
          longitude = location.getLongitude();
      }

      // return longitude
      return longitude;
  }

  /**
   * Function to check GPS/wifi enabled
   * 
   * @return boolean
   * */
  public boolean canGetLocation() {
      return this.canGetLocation;
  }

  /**
   * Function to show settings alert dialog On pressing Settings button will
   * lauch Settings Options
   * */
  public void showSettingsAlert() {
      AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

      // Setting Dialog Title
      alertDialog.setTitle("GPS is settings");

      // Setting Dialog Message
      alertDialog
              .setMessage("GPS is not enabled. Do you want to go to settings menu?");

      // On pressing Settings button
      alertDialog.setPositiveButton("Settings",
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      Intent intent = new Intent(
                              Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                      mContext.startActivity(intent);
                  }
              });

      // on pressing cancel button
      alertDialog.setNegativeButton("Cancel",
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      dialog.cancel();
                  }
              });

      // Showing Alert Message
      alertDialog.show();
  }
  
  public void onLocationChanged(Location arg0) {
    // TODO Auto-generated method stub

  }

  public void onProviderDisabled(String arg0) {
    // TODO Auto-generated method stub

  }

  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }
  public static abstract class LocationResult{
    public abstract void gotLocation(Location location);
  }
}
