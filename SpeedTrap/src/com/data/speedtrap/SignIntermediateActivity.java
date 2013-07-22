package com.data.speedtrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;


import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.facebook.DialogError;
import com.data.speedtrap.facebook.Facebook;
import com.data.speedtrap.facebook.Facebook.DialogListener;
import com.data.speedtrap.facebook.FacebookError;
import com.data.speedtrap.facebook.Util;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SignIntermediateActivity extends Activity   implements DialogListener{

  private Button btnSignIn;
  private Button btnSignUp;
  private TextView loginVia;
  private Typeface tf;
  private Typeface tf_bold;
  private LinearLayout loginFB;
  private Facebook facebook;
  private ProgressDialog dialog;
  public static final int REQUEST_EXIT = 1;
  private PlaceDataSQL placeData;
  private Preference prefs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_intermediate);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    if (placeData == null)
      placeData = new PlaceDataSQL(this);  
    if (prefs == null)
      prefs = new Preference(this);

    btnSignIn = (Button) findViewById(R.id.btnSignIn);
    btnSignUp = (Button) findViewById(R.id.btnSignUp);
    loginFB = (LinearLayout) findViewById(R.id.loginFB);
    loginVia = (TextView) findViewById(R.id.loginVia);
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy); 
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/ARIAL.TTF");

    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");

    btnSignIn.setTypeface(tf, 0);
    btnSignUp.setTypeface(tf_bold, 0);
    loginVia.setTypeface(tf, 0);


    TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    if(manager.getSimCountryIso() != null)
      GlobalData.countryISO = manager.getSimCountryIso().toUpperCase();
    else if(manager.getNetworkCountryIso() != null)
      GlobalData.countryISO = manager.getNetworkCountryIso().toUpperCase();

    btnSignIn.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(SignIntermediateActivity.this,
            SignInActivity.class);
        startActivityForResult(intent, REQUEST_EXIT);
      }
    });

    btnSignUp.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(SignIntermediateActivity.this,
            RegistartionActivity.class);
        startActivityForResult(intent, REQUEST_EXIT);
      }
    });

    loginFB.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        // TODO Auto-generated method stub
        if(DataFunctions.isNetworkAvailable(SignIntermediateActivity.this))
        {
          try {
            facebook = new Facebook("141398679364400");

            facebook.authorize(SignIntermediateActivity.this, new String[] {"publish_stream", "read_stream", "offline_access"}, SignIntermediateActivity.this);


          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        else
        {

          Toast.makeText(getApplicationContext(), getString(R.string.nonet), Toast.LENGTH_SHORT).show();
        }

      }
    });

  }


  @Override
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(SignIntermediateActivity.this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_EXIT) {
      if (resultCode == RESULT_OK) {
        this.finish();

      }
    }
    else
    {

      facebook.authorizeCallback(requestCode, resultCode, data);
    }
  }

  public void onComplete(Bundle values) {
    // TODO Auto-generated method stub
    if (values.isEmpty())
    {
      //"skip" clicked ?
      return;
    }
    else
    {
      try {
        try {
          JSONObject json = Util.parseJson(facebook.request("me"));
          if(json != null)
          {
            firstname = json.getString("first_name");
            lastname = json.getString("last_name");
            username = json.getString("username");

            if(DataFunctions.isNetworkAvailable(getApplicationContext()))
            { 
              new SubmitValuesAsync().execute();
            }
            else
            {
              int duration = Toast.LENGTH_SHORT;
              Context context = getApplicationContext();
              Toast toast = Toast.makeText(context, R.string.nonet, duration);
              toast.show();
            }
          }
        } catch (FacebookError e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        //     Intent intent = new Intent(SignIntermediateActivity.this,
        //         StartSpeedTrapActivity.class);
        //     startActivity(intent);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  public void onFacebookError(FacebookError e) {
    // TODO Auto-generated method stub
    System.out.println("Error: " + e.getMessage());
  }

  public void onError(DialogError e) {
    // TODO Auto-generated method stub
    System.out.println("Error: " + e.getMessage());
  }

  public void onCancel() {
    // TODO Auto-generated method stub

  }
  private String firstname = null;
  private String lastname = null;
  private String username = null;

  private class SubmitValuesAsync extends AsyncTask<Void, String, String> {
    JSONObject json = null;
    boolean logedin = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      if(dialog == null)
        dialog = ProgressDialog.show(SignIntermediateActivity.this, "",getString(R.string.please_wait), true);
    }

    @Override
    protected String doInBackground(Void... params) {
      String url = getResources().getString(R.string.servername);
      try {
        firstname = firstname.replace(" ", "%20");
      } catch (Exception e2) {
        e2.printStackTrace();
      }
      try {
        lastname = lastname.replace(" ", "%20");
      } catch (Exception e2) {
        e2.printStackTrace();
      }

      String strUrl = url + "registerUser=yes&token=trapspeedCodex&fname="+firstname+"&lname="+lastname+"&username="+username+"&password=''&fb=1";

      json = SpeedAPIController.getJSONfromURL_Get(strUrl);

      try {
        if (json != null) {
          if(json.has("timeout"))
          {            
            logedin = false;
          }
          else
          {
            JSONArray arrUserId = json.getJSONArray("items");

            for (int i = 0; i < arrUserId.length(); i++) {
              JSONObject e = arrUserId.getJSONObject(i);
              String userid = e.getString("id");
              DataFunctions.insertData(userid, firstname,
                  username, getApplicationContext());
              logedin = true;

              //          try {
              //            Intent intent = new Intent(SignIntermediateActivity.this,
              //                StartSpeedTrapActivity.class);
              //            startActivity(intent);
              //            finish();
              //          } catch (Exception e1) { 
              //            e1.printStackTrace();
              //          }
            }
          }
        }
      }
      catch (Exception ex) {
      }
      return null;
    }

    @Override
    protected void onPostExecute(String unused) {

      if (dialog !=null && dialog.isShowing())
      {
        dialog.dismiss();
        dialog =null;
      }

      if(logedin) 
      {
        new GetSpeedTrapData().execute();
      }
      else
      {

        if(json !=null )
        {
          if(json.has("timeout"))
          {
            showError();
          }
        }
      }
      //    if (dialog !=null && dialog.isShowing())
      //      dialog.dismiss();
    }
  }
  private void showError()
  {
    AlertDialog.Builder alerts = new AlertDialog.Builder(SignIntermediateActivity.this);
    alerts.setTitle("Login Error");
    alerts.setMessage("Request timed out");
    alerts.setPositiveButton(getString(R.string.yes),
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {

      }
    });
    alerts.show();
  }
  private class GetSpeedTrapData extends AsyncTask<String, String, String> {
    JSONObject json = null;
    boolean dataLoad = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      if(dialog == null)
        dialog = ProgressDialog.show(SignIntermediateActivity.this, "","Please wait while we import the camera locations. Depending upon your internet speed this might take several minutes", true);
    }

    @Override
    protected String doInBackground(String... params) {
      //String url = getResources().getString(R.string.servername);
      String url = getResources().getString(R.string.servername); 
      //String strUrl = url + "getData=yes&listAll=1";
      List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
      nameValuePair.add(new BasicNameValuePair("getData", "yes"));
      nameValuePair.add(new BasicNameValuePair("token", "trapspeedCodex"));
      nameValuePair.add(new BasicNameValuePair("listAll", "1"));
      nameValuePair.add(new BasicNameValuePair("country", GlobalData.countryISO));

      json = SpeedAPIController.getJSONfromURL_Post(url, nameValuePair);
      try {
        if (json != null) {
          if(json.has("timeout"))
          {            
            dataLoad = false;
          }
          else
          {
            JSONArray arrSpeed = json.getJSONArray("speedtrapData");

            for (int i = 0; i < arrSpeed.length(); i++) { //arrSpeed.length()
              JSONObject e = arrSpeed.getJSONObject(i);  

              insertNewLocation(e.getString("id"),e.getString("latitude"), e.getString("longitude"), e.getString("cameraType"), e.getString("direction"), e.getString("time"), e.getString("country"), e.getString("count"));
              dataLoad = true;
            }
          }
        }
      }
      catch (Exception ex) {

      }
      return null;
    }

    @Override
    protected void onPostExecute(String unused) {
      if (dialog !=null && dialog.isShowing())
      {
        dialog.dismiss();
        dialog = null;
      }
      if (json != null) {
        try {
          if(dataLoad){
            String resultDate = null;
            Date setDate = new Date();
            SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultDate = sfDate.format(setDate);
            prefs.setPreference("lastUpdatedTime", resultDate);
          }
          else
          {
            GlobalData.intialLoad = true;
          }
          Intent intent = new Intent(SignIntermediateActivity.this,
              StartSpeedTrapActivity.class);
          startActivity(intent);
          finish();
        } catch (Exception e1) { 
          e1.printStackTrace();
        }
      }
      //    if (json != null) 
      //      prefs.setPreference("downloadComplete", "true");
      //    onDataLoadComplete = true;


      // GlobalData.downloadComplete = true;
    }

  }

  private void insertNewLocation(String id,String latitude,String longitude,String camType,String direction,String createdTime,String countryCode,String count) {
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
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
