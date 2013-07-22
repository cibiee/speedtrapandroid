package com.data.speedtrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.JSONfunctions;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class SignInActivity extends Activity {

  private EditText edUserName;
  private EditText edPassword;
  private TextView login;
  private String username;
  private String password;
  private ProgressDialog dialog;
  private Typeface tf;
  private PlaceDataSQL placeData;
  private Preference prefs;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signin);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    init();

    if (placeData == null)
      placeData = new PlaceDataSQL(this);  

    if (prefs == null)
      prefs = new Preference(this);

    login.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        try {

          username = edUserName.getText().toString();
          password = edPassword.getText().toString();
          dialog = ProgressDialog.show(SignInActivity.this, "",
              getString(R.string.please_wait), true);

          if(username.trim().length() > 0 &&  password.trim().length() >0 )
          {

            if(DataFunctions.isNetworkAvailable(SignInActivity.this))
            {
              submitData();
            }
            else
            {
              Toast.makeText(SignInActivity.this, getString(R.string.nonet), Toast.LENGTH_SHORT).show();
            }
          }
          else
          {
            Toast.makeText(SignInActivity.this, getString(R.string.allfieldsrequired), Toast.LENGTH_SHORT).show();
          }

        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    });
  }

  private void init() {
    edUserName = (EditText) findViewById(R.id.edUserName);
    edPassword = (EditText) findViewById(R.id.edPassword);
    login = (TextView) findViewById(R.id.login);
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");

    edUserName.setTypeface(tf, 0);
    edPassword.setTypeface(tf, 0);
    login.setTypeface(tf, 0);
  }

  @Override
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(SignInActivity.this);
  }

  private void submitData() 
  {
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

  private class SubmitValuesAsync extends AsyncTask<Void, String, String> {
    JSONObject json = null;
    String errMessage =""; 
    boolean logedin = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      if(dialog !=null)
        dialog.show();

    }

    @Override
    protected String doInBackground(Void... params) {
      try{ 
        String url =  getString(R.string.servername);
        String param = "login=yes&token=trapspeedCodex&username="+username+"&password="+password;
        json = SpeedAPIController.getJSONfromURL_Get(url+param);

        if(json !=null )
        {
          if(json.has("timeout"))
          {            
            logedin = false;
          }
          else
          {
            JSONArray  userDetails = json.getJSONArray("items");

            for(int i=0;i<userDetails.length();i++){      
              JSONObject e = userDetails.getJSONObject(i);
              if(e.getString("id").equals("0"))
              {           
                errMessage = e.getString("message");
                logedin = false;
              } 
              else
              {
                DataFunctions.insertData(e.getString("id"), e.getString("fname"),
                    e.getString("email"), getApplicationContext());

                logedin = true;
              }
            }  
          }
        }
      }
      catch(JSONException e)        
      {

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
        if(errMessage.length() > 5){
          AlertDialog.Builder alert = new AlertDialog.Builder(SignInActivity.this);
          alert.setTitle("Login Error");
          alert.setMessage(""+errMessage);
          alert.setPositiveButton(getString(R.string.yes),
              new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
          });
          alert.show();
        }
      }
    }
  }
  private void showError()
  {
    AlertDialog.Builder alerts = new AlertDialog.Builder(SignInActivity.this);
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
        dialog = ProgressDialog.show(SignInActivity.this, "","Please wait while we import the camera locations. Depending upon your internet speed this might take several minutes", true);
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
        if(json !=null )
        {
          if(json.has("timeout"))
          {            
            dataLoad = false;
          }
          else
          {
          JSONArray arrSpeed = json.getJSONArray("speedtrapData");

          for (int i = 0; i < arrSpeed.length(); i++) { //arrSpeed.length()
            JSONObject e = arrSpeed.getJSONObject(i);  

            insertNewLocation(e.getString("id"),e.getString("latitude"), e.getString("longitude"), e.getString("cameraType"), e.getString("direction"), e.getString("time"), e.getString("country"),e.getString("count"));
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
      //if (json != null) {
      try {
        if(dataLoad){
        String resultDate = null;
        Date setDate = new Date();
        SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        resultDate = sfDate.format(setDate);

        prefs.setPreference("lastUpdatedTime", resultDate);
        GlobalData.intialLoad = false;
        }
        else
        {
          GlobalData.intialLoad = true;
        }
        Intent intent = new Intent(SignInActivity.this,
            StartSpeedTrapActivity.class);
        startActivity(intent);
        setResult(RESULT_OK, null);
        finish();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
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
