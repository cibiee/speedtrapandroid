package com.data.speedtrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.data.speedtrap.database.PlaceDataSQL;
import com.data.speedtrap.json.JSONfunctions;
import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;
import com.data.speedtrap.vo.VOUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class RegistartionActivity extends Activity {

  private EditText edFirstName;
  private EditText edLastName;
  private EditText edEmail;
  private EditText edPassword;
  private CheckBox chkAccept;
  private TextView signUp;
  private VOUser obUser;
  private Drawable error_indicator;
  private String firstname; 
  private String lastname;
  private String password;
  private String email;
  private ProgressDialog dialog;
  private ProgressBar progressBar; 
  private Boolean userExist = false;
  private Boolean isTermsChecked = false;
  private Typeface tf;
  private PlaceDataSQL placeData;
  private Preference prefs;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    init();

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    if (placeData == null)
      placeData = new PlaceDataSQL(this);  
    if (prefs == null)
      prefs = new Preference(this);

    chkAccept.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isTermsChecked = isChecked;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getCurrentFocus()
            .getWindowToken(), 0);
      }      
    });

    signUp.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(isTermsChecked)
        {
          if(DataFunctions.isNetworkAvailable(getApplicationContext()))
          {
            firstname = edFirstName.getText().toString();
            lastname = edLastName.getText().toString();
            email = edEmail.getText().toString();
            password = edPassword.getText().toString();
            dialog = ProgressDialog.show(RegistartionActivity.this, "",
                getString(R.string.please_wait), true);

            if (firstname.equalsIgnoreCase("")
                || email.equalsIgnoreCase("")
                || password.equalsIgnoreCase("")
                || lastname.equalsIgnoreCase("")) {

              String mess = getString(R.string.allfieldsrequired);
              Toast.makeText(RegistartionActivity.this, mess,
                  Toast.LENGTH_SHORT).show();
            } else if (!DataFunctions.isEmailValid(email)) {
              String messvalid = getString(R.string.validemail);

              Toast.makeText(RegistartionActivity.this, messvalid,
                  Toast.LENGTH_SHORT).show();
            } else {

              if (!userExist) {
                submitData(); 
              } else {
                String mes = getString(R.string.emailusernamenotvalid);
                Toast.makeText(RegistartionActivity.this, mes,
                    Toast.LENGTH_SHORT).show();
              }
            }
          }
          else
          {
            Toast.makeText(RegistartionActivity.this, getString(R.string.nonet),
                Toast.LENGTH_SHORT).show();
          }
        }
        else
        {
          Toast.makeText(RegistartionActivity.this, "Please check the terms of use.",
              Toast.LENGTH_SHORT).show();
        }
      }
    });

    edEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() { 
      public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
          if (!DataFunctions.isEmailValid(edEmail.getText()
              .toString())) { 
            String messvalid = getString(R.string.validemail);
            edEmail.setError(messvalid);
          }
          else
          {
            if (edEmail.getText().length() > 0) {
              //              String url = getResources().getString(
              //                  R.string.servername);
              //              JSONObject json = SpeedAPIController.getJSONfromURL_Get(url
              //                  + "checkusername=yes&token=trapspeedCodex&username="
              //                  + edEmail.getText());
              //              try {
              //
              //                JSONArray workoutList = json.getJSONArray("items");
              //
              //                for (int i = 0; i < workoutList.length(); i++) {
              //                  JSONObject e = workoutList.getJSONObject(i);
              //                  String status = e.getString("status");
              //                  if (Boolean.parseBoolean(status)) {
              //                    edEmail.setError(getString(R.string.usernamealready));
              //                    edEmail.setFocusable(true);
              //                    userExist = true;
              //                  } else {
              //                    edEmail.setError(null);
              //                    userExist = false;
              //                  }
              //                }
              //              } catch (Exception ex) {
              //              }
              new ValidateAsync().execute();
            }
          }
        }
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(RegistartionActivity.this);
  }

  private void init() {
    if (obUser == null)
      obUser = new VOUser(this);

    edFirstName = (EditText) findViewById(R.id.edFirstName);
    edLastName = (EditText) findViewById(R.id.edLastName);
    edEmail = (EditText) findViewById(R.id.edEmail);
    edPassword = (EditText) findViewById(R.id.edPassword);
    chkAccept = (CheckBox) findViewById(R.id.chkAccept);
    signUp = (TextView) findViewById(R.id.signUp);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");

    edFirstName.setTypeface(tf, 0);
    edPassword.setTypeface(tf, 0);
    edLastName.setTypeface(tf, 0);
    edEmail.setTypeface(tf, 0);
    chkAccept.setTypeface(tf, 0);
    signUp.setTypeface(tf, 0);

    if (error_indicator == null)
      error_indicator = getResources().getDrawable(R.drawable.icon);
    int left = 0;
    int top = 0;
 
    int right = error_indicator.getIntrinsicHeight();
    int bottom = error_indicator.getIntrinsicWidth();
    edFirstName.addTextChangedListener(new InputValidator(edFirstName));
    edLastName.addTextChangedListener(new InputValidator(edLastName));
    edPassword.addTextChangedListener(new InputValidator(edPassword));

    error_indicator.setBounds(new Rect(left, top, right, bottom));
  }

  private class InputValidator implements TextWatcher {
    private EditText et;

    private InputValidator(EditText editText) {
      this.et = editText;
    }

    public void afterTextChanged(Editable s) {

    }

    public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before,
        int count) {
      if (s.length() != 0) {
        switch (et.getId()) {
        case R.id.edFirstName: {
          edFirstName.setError(null);
          if (!Pattern.matches("^[a-zA-Z]{1,16}$", s)) {

            edFirstName.setError(getString(R.string.firstname)+" "
                + getString(R.string.onlyalphabets));

          }
          break;
        }
        case R.id.edLastName: {
          edLastName.setError(null);
          if (!Pattern.matches("^[a-zA-Z ]{1,16}$", s)) {
            edLastName.setError(getString(R.string.lastname)+" "
                + getString(R.string.onlyalphabets));

          }
          break;
        }
        case R.id.edPassword: {
          edPassword.setError(null);
          if (!Pattern.matches("^[a-zA-Z0-9]{1,16}$", s)) {
            edPassword.setError(getString(R.string.password)+" "
                + getString(R.string.onlycontainalphanumeric));

          }
        }
        break;
        }
      }
    }
  }

  private void submitData() {
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


  private class ValidateAsync extends AsyncTask<Void, String, String> {
    JSONObject json = null;
    private  String status = null;
    boolean checkedin = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      progressBar.setVisibility(View.VISIBLE);
      //dialogv = ProgressDialog.show(RegistartionActivity.this, "","Checking username availability", true);
    }

    @Override
    protected String doInBackground(Void... params) {


      String url = getResources().getString(
          R.string.servername);
      json = SpeedAPIController.getJSONfromURL_Get(url
          + "checkusername=yes&token=trapspeedCodex&username="
          + edEmail.getText());
      if(json !=null )
      {
        if(json.has("timeout"))
        {            
          checkedin = false;
        }
        else
        {
          try {

            JSONArray workoutList = json.getJSONArray("items");

            for (int i = 0; i < workoutList.length(); i++) {
              JSONObject e = workoutList.getJSONObject(i);
              status = e.getString("status");
              checkedin = true;
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(String unused) {
      //      if (dialogv !=null && dialogv.isShowing())
      //        dialogv.dismiss();
      progressBar.setVisibility(View.GONE);
      if(checkedin){
        if(status != null){
          if (Boolean.parseBoolean(status)) {
            edEmail.setError(getString(R.string.usernamealready));
            edEmail.setFocusable(true);
            userExist = true;
          } else {
            edEmail.setError(null);
            userExist = false;
          }
        }
      }
      else
      {
        if(json !=null )
        {
          if(json.has("timeout"))
          {
            edEmail.setText("");
            edEmail.requestFocus();
            showError();
          }
        }
      }
    }
  }

  private class SubmitValuesAsync extends AsyncTask<Void, String, String> {
    JSONObject json = null;
    boolean logedin = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      if(dialog !=null)
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
      password = password.replace("\n", "");
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

      String strUrl = url + "registerUser=yes&token=trapspeedCodex&fname="+firstname+"&lname="+lastname+"&username="+email+"&password="+password+"&fb=0";

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
                  email, getApplicationContext()); 
              logedin = true;
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
      if(logedin)
      {
        new GetSpeedTrapData().execute();
      }
      else
      {
        //        if (dialog !=null && dialog.isShowing())
        //          dialog.dismiss();
        if(json !=null )
        {
          if(json.has("timeout"))
          {
            showError();
          }
        }
      }
    }
  }

  private void showError()
  {
    AlertDialog.Builder alerts = new AlertDialog.Builder(RegistartionActivity.this);
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
        dialog = ProgressDialog.show(RegistartionActivity.this, "","Please wait while we import the camera locations. Depending upon your internet speed this might take several minutes", true);
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
      if (json != null) {
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

          Intent intent = new Intent(RegistartionActivity.this,
              StartSpeedTrapActivity.class);
          startActivity(intent);
          setResult(RESULT_OK, null);
          finish();
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      //      if (json != null) 
      //        prefs.setPreference("downloadComplete", "true");
      //      onDataLoadComplete = true;


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
