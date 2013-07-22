package com.data.speedtrap;

import com.data.speedtrap.utils.DataFunctions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class SpeedTrapApproachActivity extends Activity{
  
  private Typeface tf;
  private Typeface tf_bold;
  private TextView willwarn;
  private TextView appOne;
  private TextView appTwo;
  private TextView appThree;
  private TextView plhelpothers;
  private TextView threeeasyseps;
  private TextView stepOne;
  private TextView stepTwo;
  private TextView stepThree;
  private TextView drivecarefully;
  private TextView speeding;
  private TextView termsofuse;
  private TextView privacypolicy;
  private String preAct;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_approach_speed_trap);
    
    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/ARIAL.TTF");
    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");
    Bundle extras = getIntent().getExtras();
    preAct = extras.getString("ACT");
    
    willwarn = (TextView) findViewById(R.id.willwarn);
    appOne = (TextView) findViewById(R.id.appOne);
    appTwo = (TextView) findViewById(R.id.appTwo);
    appThree = (TextView) findViewById(R.id.appThree);
    plhelpothers = (TextView) findViewById(R.id.plhelpothers);
    threeeasyseps = (TextView) findViewById(R.id.threeeasyseps);
    stepOne = (TextView) findViewById(R.id.stepOne);
    stepTwo = (TextView) findViewById(R.id.stepTwo);
    stepThree = (TextView) findViewById(R.id.stepThree);
    drivecarefully = (TextView) findViewById(R.id.drivecarefully);
    speeding = (TextView) findViewById(R.id.speeding);
    termsofuse = (TextView) findViewById(R.id.termsofuse);
    privacypolicy = (TextView) findViewById(R.id.privacypolicy);
    willwarn.setTypeface(tf_bold, 0);
    appOne.setTypeface(tf, 0);
    appTwo.setTypeface(tf, 0);
    appThree.setTypeface(tf, 0);
    plhelpothers.setTypeface(tf, 0);
    threeeasyseps.setTypeface(tf, 0);
    appOne.setTypeface(tf, 0);
    stepOne.setTypeface(tf, 0);
    stepTwo.setTypeface(tf, 0);
    stepThree.setTypeface(tf, 0);
    drivecarefully.setTypeface(tf_bold, 0);
    speeding.setTypeface(tf_bold, 0);
    termsofuse.setTypeface(tf_bold, 0);
    privacypolicy.setTypeface(tf_bold, 0);
  }
  
  @Override
  protected void onResume() {
   super.onResume();
   DataFunctions.SetLang(SpeedTrapApproachActivity.this);
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
      intent.setClass(SpeedTrapApproachActivity.this, StartSpeedTrapActivity.class);
      startActivity(intent);   
      finish();
    }
    else if(preAct.equals("Mode"))
    {
      Intent intent = new Intent();
      intent.setClass(SpeedTrapApproachActivity.this, SelectModeActivity.class);
      startActivity(intent);   
      finish();
    }
  }
}