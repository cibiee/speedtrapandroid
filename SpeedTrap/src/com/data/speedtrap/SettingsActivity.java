package com.data.speedtrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.data.speedtrap.facebook.AsyncFacebookRunner;
import com.data.speedtrap.facebook.AsyncFacebookRunner.RequestListener;
import com.data.speedtrap.facebook.DialogError;
import com.data.speedtrap.facebook.Facebook;
import com.data.speedtrap.facebook.Facebook.DialogListener;
import com.data.speedtrap.facebook.FacebookError;
import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.twitter.PrepareRequestTokenActivity;
import com.data.speedtrap.twitter.TwitterUtils;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.vo.VOUser;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity implements DialogListener{
  private Typeface tf;
  private Typeface tf_bold;
  private TextView txtSettings;
  private TextView warningpossible;
  private TextView appgreatadv;
  private TextView turnon;
  private TextView recommendFriends;
  private ImageView btnFB;
  private ImageView btnTwitter;
  private Preference prefs;
  private SharedPreferences shareddPrefs;
  private String preAct;
  private Facebook facebook;
  private  Bundle valuesB;
  private final Handler mTwitterHandler = new Handler();

  final Runnable mUpdateTwitterNotification = new Runnable() {
    public void run() {
      //Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    if (prefs == null)
      prefs = new Preference(this);
    this.shareddPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/ARIAL.TTF");
    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");
    try{
      Bundle extras = getIntent().getExtras();
      preAct = extras.getString("ACT");

      //int SDK_INT = android.os.Build.VERSION.SDK_INT;

      ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
      toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          // TODO Auto-generated method stub
          if(isChecked){
            prefs.setPreference("settings", "on");
          }else{
            prefs.setPreference("settings", "off");
          }
          prefs.setPreference("settingsChanged", "true");
        }
      });

      if(prefs.getPreference("settings") != null && prefs.getPreference("settings").equals("on")){
        toggleButton.setChecked(true);
      }
      else
      {
        toggleButton.setChecked(false);
      }
    }
    catch(Exception ex){
      String str = ex.getLocalizedMessage();
    }
    txtSettings = (TextView) findViewById(R.id.txtSettings);
    warningpossible = (TextView) findViewById(R.id.warningpossible);
    appgreatadv = (TextView) findViewById(R.id.appgreatadv);
    turnon = (TextView) findViewById(R.id.turnon);
    recommendFriends = (TextView) findViewById(R.id.recommendFriends);

    btnFB = (ImageView) findViewById(R.id.btnFB);
    btnFB.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(DataFunctions.isNetworkAvailable(SettingsActivity.this))
        {
          try {
            facebook = new Facebook("141398679364400");

            //facebook.authorize(SignIntermediateActivity.this, new String[] {"user_photos,photo_upload,publish_checkins,publish_actions,publish_stream,read_stream,offline_access"},Facebook.FORCE_DIALOG_AUTH,SignIntermediateActivity.this);
            facebook.authorize(SettingsActivity.this, new String[] {"user_photos,photo_upload,publish_checkins,publish_actions,publish_stream,read_stream,offline_access"}, SettingsActivity.this);


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

    btnTwitter = (ImageView) findViewById(R.id.btnTw);
    btnTwitter.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        // TODO Auto-generated method stub
        if(DataFunctions.isNetworkAvailable(getApplicationContext()))
        {
          try
          { 

            new AsyncTask<SharedPreferences,Object, Boolean>() {

              @Override
              protected Boolean doInBackground(SharedPreferences... params) {
                return TwitterUtils.isAuthenticated(params[0]);
              }

              @Override
              protected void onPostExecute(Boolean isAuthenticated) {
                if (isAuthenticated) {
                  // Do processing after successful authentication
                  sendTweet();
                }
                else {
                  // Do processing after authentication failure
                  Intent i = new Intent(getApplicationContext(), PrepareRequestTokenActivity.class);
                  i.putExtra("tweet_msg",getTweetMsg());
                  startActivity(i);
                }
              }
            }.execute(shareddPrefs);

          }
          catch (Exception e) {
            // TODO: handle exception
            String st = e.toString();
          }
        }
        else
        {

          Toast.makeText(getApplicationContext(), getString(R.string.nonet), Toast.LENGTH_SHORT).show();

        }
      }

    });
    txtSettings.setTypeface(tf_bold, 0);
    warningpossible.setTypeface(tf, 0);
    appgreatadv.setTypeface(tf, 0);
    turnon.setTypeface(tf, 0);
    recommendFriends.setTypeface(tf, 0);

  }

  private String getTweetMsg() {
    VOUser obUser = new VOUser(this);
    return obUser.getSelectedFirstName()+" "+getString(R.string.facebookPost)+" "+"www.speed-trap-app.com";
  } 

  public void sendTweet() {
    Thread t = new Thread() {
      public void run() {

        try {
          TwitterUtils.sendTweet(shareddPrefs, getTweetMsg());
          mTwitterHandler.post(mUpdateTwitterNotification);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

    };
    t.start();
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
    if(preAct.equals("Start")){
      Intent intent = new Intent();
      intent.setClass(SettingsActivity.this, StartSpeedTrapActivity.class);
      startActivity(intent);   
      finish();
    }
    else if(preAct.equals("Mode"))
    {
      Intent intent = new Intent();
      intent.setClass(SettingsActivity.this, SelectModeActivity.class);
      startActivity(intent);   
      finish();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);


    facebook.authorizeCallback(requestCode, resultCode, data);
  }


  public void onComplete(Bundle values) {
    if (values.isEmpty())
    {
      //"skip" clicked ?
      return;
    }
    if (!values.containsKey("post_id"))
    {
      try
      {
        valuesB = values;
        new postFacebook().execute();

      }
      catch (Exception e)
      {
        // TODO: handle exception
        System.out.println(e.getMessage());
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

  public void updateStatus(String accessToken){
    try {
      Bundle bundle = new Bundle();
      bundle.putString(Facebook.TOKEN,accessToken);
      VOUser obUser = new VOUser(this);
      
      bundle.putString("description",obUser.getSelectedFirstName()+" "+getString(R.string.facebookPost));
      bundle.putString("link", "www.speed-trap-app.com");
      bundle.putString("picture", "http://speed-trap-app.com/images/logo.png");

      String response = facebook.request("/me/feed",bundle,"POST");
      AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
      mAsyncRunner.request(null, bundle, "POST", new RequestListener() {
        public void onMalformedURLException(MalformedURLException e, Object state) {

        }


        public void onIOException(IOException e, Object state) {
          e.printStackTrace();
        }

        public void onFileNotFoundException(FileNotFoundException e, Object state) {
          e.printStackTrace();
        }

        public void onFacebookError(FacebookError e, Object state) {
          e.printStackTrace();
        }

        public void onComplete(String response, Object state) {
        }

      }, null);
    } catch (Exception e) {
      String st =e.toString();
      st = st+"";
    } 
  }

  class postFacebook extends AsyncTask<Void, Void, Void>
  {

    @Override
    protected void onPreExecute() {
      // TODO Auto-generated method stub
      super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      updateStatus(valuesB.getString(Facebook.TOKEN));
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      // TODO Auto-generated method stub
      super.onPostExecute(result);
      //Toast.makeText(getApplicationContext(), "posted", Toast.LENGTH_SHORT).show();
    }

  }
}