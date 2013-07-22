package com.data.speedtrap;

import com.data.speedtrap.json.SpeedAPIController;
import com.data.speedtrap.utils.DataFunctions;
import com.data.speedtrap.utils.GlobalData;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class SeeSpeedTrapActivity extends Activity{

  private Button btnYes;
  private Button btnNo;
  private TextView seethespeedtrap;
  private Typeface tf_bold;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_see_speed_trap);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    btnYes = (Button) findViewById(R.id.btnYes);
    seethespeedtrap = (TextView) findViewById(R.id.seethespeedtrap);
    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");    
    btnYes.setTypeface(tf_bold, 0);
    seethespeedtrap.setTypeface(tf_bold, 0);
    btnYes.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(DataFunctions.isNetworkAvailable(getApplicationContext()))
        {
          new SubmitValuesAsync("1").execute();
          Intent intent = new Intent();
          intent.setClass(SeeSpeedTrapActivity.this, StartSpeedTrapActivity.class);
          startActivity(intent);   
          finish();
        }
      }
    });

    btnNo = (Button) findViewById(R.id.btnNo);
    btnNo.setTypeface(tf_bold, 0);
    btnNo.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        if(DataFunctions.isNetworkAvailable(getApplicationContext()))
        {
          new SubmitValuesAsync("0").execute();
          Intent intent = new Intent();
          intent.setClass(SeeSpeedTrapActivity.this, StartSpeedTrapActivity.class);
          startActivity(intent);   
          finish();
        }
      }
    });
  }
  
  private class SubmitValuesAsync extends AsyncTask<Void, String, Void> {
    String confirm = null;
    public SubmitValuesAsync(String confirm)
    {
      this.confirm = confirm;
    }

    @Override
    protected Void doInBackground(Void... params) {
      String url = getResources().getString(R.string.servername);

      String strUrl = url + "confirmTrap=yes&token=trapspeedCodex&latitude="+GlobalData.glVOLocation.getLocation().getLatitude()+"&longitude="+GlobalData.glVOLocation.getLocation().getLongitude()+"&confirm="+confirm;

      SpeedAPIController.getJSONfromURL_Get_NoReturn(strUrl);

      return null;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    DataFunctions.SetLang(SeeSpeedTrapActivity.this);
  }
}
