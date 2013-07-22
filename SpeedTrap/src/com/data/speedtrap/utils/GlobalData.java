package com.data.speedtrap.utils;

import java.util.ArrayList;

import android.location.Location;

import com.data.speedtrap.vo.VOLocation;

public class GlobalData {

  public static  boolean downloadComplete = false;
  public static VOLocation glVOLocation;
  public static  Location curLocation = new Location("demo");
  public static boolean isInsideCircle = false;
  public static boolean addedNewLocation = false;
  //public static boolean settingsOn = false;
  public static boolean deviceRegistered = false;
  public static boolean intialLoad = true;
  public static String countryISO = null;
  public static ArrayList<VOLocation> distanceArNew=new ArrayList<VOLocation>();
  public static int s = 0;
  public static String tim = null;
  public static String result = null;
}
 