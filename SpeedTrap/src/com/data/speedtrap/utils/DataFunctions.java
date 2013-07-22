package com.data.speedtrap.utils;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.vo.VOUser;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class DataFunctions {

  private  static Locale locale = new Locale("en");

  public static boolean isEmailValid(String email) {
    boolean isValid = false;

    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    CharSequence inputStr = email;

    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputStr);
    if (matcher.matches()) {
      isValid = true;
    }
    return isValid;
    // return true;
  }

  public static boolean isNetworkAvailable(Context context) {
    try {
      ConnectivityManager cm = (ConnectivityManager) context
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo i = cm.getActiveNetworkInfo();
      if (i == null)
        return false;
      boolean isConnected = (cm.getActiveNetworkInfo() != null && cm
          .getActiveNetworkInfo().isConnectedOrConnecting());

      return isConnected;
    } catch (Exception e) {
      // TODO: handle exception
      Toast.makeText(context, " Network error " + e.toString(),
          Toast.LENGTH_LONG).show();
      return false;
    }
  }

  public static double getGaussian(){
    double MEAN = 100.0f; 
    double VARIANCE = 5.0f;
    Random fRandom = new Random();
    return MEAN + fRandom.nextGaussian() * VARIANCE;
  }

  public static void SetLang(Context context)
  {
    Preference prefs = new Preference(context);
    String lang = null;

    if(prefs.getPreference("SelectedLanguage").equals("1"))
    {
      lang = "en";
    }
    else if(prefs.getPreference("SelectedLanguage").equals("2"))
    {
      lang = "da";
    }
    else if(prefs.getPreference("SelectedLanguage").equals("3"))
    {
      lang = "es";
    }
    else if(prefs.getPreference("SelectedLanguage").equals("4"))
    {
      lang = "fr";
    }
    else if(prefs.getPreference("SelectedLanguage").equals("5"))
    {
      lang = "de";
    }
    else
    { 
      lang = "en";
    }

    locale= new Locale(lang);
    Locale.setDefault(locale);

    Configuration config= new Configuration();
    config.locale=locale;
    context.getResources().updateConfiguration(config,context.getResources().getDisplayMetrics());
  }

  public static void insertData(String id, String fname,
      String email, Context context) {
    try {
      VOUser obUser = new VOUser(context);
      //obUser.setSelectedUserName(username);
      obUser.setUserId(id);
      obUser.setSelectedFirstName(fname);
      obUser.setUseremail(email);

    } catch (Exception e) {
      // TODO Auto-generated catch block
    }
  }

  /**
   * Calculates the end-point from a given source at a given range (meters)
   * and bearing (degrees). This methods uses simple geometry equations to
   * calculate the end-point.
   * 
   * @param point
   *            Point of origin
   * @param range
   *            Range in meters
   * @param bearing
   *            Bearing in degrees
   * @return End-point from the source given the desired range and bearing.
   */
  public static Location calculateDerivedPosition(Location point,
      double range, double bearing)
  {
    double EarthRadius = 6371000; // m

    double latA = Math.toRadians(point.getLatitude());
    double lonA = Math.toRadians(point.getLongitude());
    double angularDistance = range / EarthRadius;
    double trueCourse = Math.toRadians(bearing);

    double lat = Math.asin(
        Math.sin(latA) * Math.cos(angularDistance) +
        Math.cos(latA) * Math.sin(angularDistance)
        * Math.cos(trueCourse));

    double dlon = Math.atan2(
        Math.sin(trueCourse) * Math.sin(angularDistance)
        * Math.cos(latA),
        Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

    double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

    lat = Math.toDegrees(lat);
    lon = Math.toDegrees(lon);

    Location newPoint = new Location("dummyprovider");
    newPoint.setLatitude(lat);
    newPoint.setLongitude(lon);
    return newPoint;

  }

  public static boolean pointIsInCircle(Location pointForCheck, Location center,
      double radius) {
    if (getDistanceBetweenTwoPoints(pointForCheck, center) <= radius)
      return true;
    else
      return false;
  }

  public static double getDistanceBetweenTwoPoints(Location p1, Location p2) {
    double R = 6371000; // m
    double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
    double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
    double lat1 = Math.toRadians(p1.getLatitude());
    double lat2 = Math.toRadians(p2.getLatitude());
    
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
        * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = R * c;

    return d;
  }
}
