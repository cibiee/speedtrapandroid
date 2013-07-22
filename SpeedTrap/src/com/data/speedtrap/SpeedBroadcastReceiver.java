package com.data.speedtrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.utils.GlobalData;
import com.data.speedtrap.vo.VOUser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SpeedBroadcastReceiver extends BroadcastReceiver {
  private VOUser obUser ;
  private PlaceDataSQL placeData;
  private Preference prefs;
  private Context pcontext;

  @Override
  public void onReceive(Context context, Intent intent) {

    setResult(Activity.RESULT_OK, null, null);

    pcontext = context;

    if(obUser == null)
      obUser =  new VOUser(context) ; 

    if (placeData == null)
      placeData = new PlaceDataSQL(context);  
    if (prefs == null)
      prefs = new Preference(context);


    if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {

      handleRegistration(context, intent);
    }
    if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {

      //handleMessage(context, intent);
      //GlobalData.s++;
    }
  }

  @SuppressWarnings("deprecation")
  private void handleMessage(Context context, Intent intent)
  {
    //Toast.makeText(context, "notifyyyy", Toast.LENGTH_SHORT).show();
    new GetSpeedTrapData(context).execute();

  }

  private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
    StringBuilder sb = new StringBuilder();
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return sb.toString();
  }


  private void handleRegistration(Context context, Intent intent) {
    if(obUser == null)
      obUser =  new VOUser(context) ;

    contextx = context;
    registration = intent.getStringExtra("registration_id");
    if (intent.getStringExtra("error") != null) {
      // Registration failed, should try again later.
      Log.d("c2dm", "registration failed");
      String error = intent.getStringExtra("error");
      if(error == "SERVICE_NOT_AVAILABLE"){
        Log.d("c2dm", "SERVICE_NOT_AVAILABLE");
      }else if(error == "ACCOUNT_MISSING"){
        Log.d("c2dm", "ACCOUNT_MISSING");
      }else if(error == "AUTHENTICATION_FAILED"){
        Log.d("c2dm", "AUTHENTICATION_FAILED");
      }else if(error == "TOO_MANY_REGISTRATIONS"){
        Log.d("c2dm", "TOO_MANY_REGISTRATIONS");
      }else if(error == "INVALID_SENDER"){
        Log.d("c2dm", "INVALID_SENDER");
      }else if(error == "PHONE_REGISTRATION_ERROR"){
        Log.d("c2dm", "PHONE_REGISTRATION_ERROR");
      }
    } else if (intent.getStringExtra("unregistered") != null) {
      // unregistration done, new messages from the authorized sender will be rejected
      Log.d("c2dm", "unregistered");

    } else if (registration != null) {

    }
    new UserRegistartion().execute();
  }

  private Context contextx = null;
  private String registration = null;
  private class UserRegistartion extends AsyncTask<String, String, String> {


    @Override
    protected String doInBackground(String... arg0) {
      // TODO Auto-generated method stub
      try {
        if(obUser.getUserId().length() > 0)
        {
          String url = contextx.getString(R.string.servername)+"android_key="+registration+"&token=trapspeedCodex&user_id="+obUser.getUserId();
          HttpClient httpclient = new DefaultHttpClient();

          // Prepare a request object
          HttpGet httpget = new HttpGet(url);
          httpget.addHeader(new BasicHeader("Accept", "application/json"));
          //httpget.getParams().setParameter("format", "JSON");


          // Execute the request 
          HttpResponse response;

          String result = null;
          response = httpclient.execute(httpget);

          // Get hold of the response entity
          HttpEntity entity = response.getEntity();
          // If the response does not enclose an entity, there is no need
          // to worry about connection release

          if (entity != null) {
            // A Simple Response Read
            InputStream instream = entity.getContent();
            result = convertStreamToString(instream);

            // Closing the input stream will trigger connection release
            instream.close();
          }
        }
      } catch (ClientProtocolException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (Exception e) {
        String st = e.toString();
        st = st+"";
      }
      return null;
    }
  }

  private class GetSpeedTrapData extends AsyncTask<String, String, String> {
    JSONObject json = null;
    Context context;
    SQLiteDatabase db = null;
    public GetSpeedTrapData(Context context) {
      // TODO Auto-generated constructor stub
      this.context = context;
      db = placeData
          .getWritableDatabase();
    }

    @Override
    protected String doInBackground(String... params) {
      String url = pcontext.getResources().getString(R.string.servername); 
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
