package com.data.speedtrap;

import com.data.speedtrap.vo.VOUser;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {

  private Thread mSplashThread;
  private VOUser obUser;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    
    if (obUser == null)
      obUser = new VOUser(this);
  }

  @Override
  protected void onResume() { 
    super.onResume();
    mSplashThread = new Thread() {
      @Override
      public void run() {
        try {
          int waitTime = 1000;
          synchronized (this) {
            if(obUser.getUserId().length() == 0)
            {
            }
            else
            {
              waitTime = 1000;
            }
            wait(waitTime);
          }
        } 
        catch (InterruptedException ex) {
        }
        if (obUser.getUserId().length() == 0) {
                    
          try {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this,
                SelectLanguageActivity.class);
            startActivity(intent);
            finish();
          } catch (Exception e) {
            e.printStackTrace(); 
          }
        } else {
          try {       
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, StartSpeedTrapActivity.class);
            startActivity(intent);
            finish();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }     
     }
    };

    try {
      mSplashThread.start();
    } catch (Exception e) {
    }
  }
}
