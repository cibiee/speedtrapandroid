package com.data.speedtrap;

import java.util.ArrayList;
import java.util.Locale;

import com.data.speedtrap.preference.Preference;
import com.data.speedtrap.vo.VOLanguage;
import com.data.speedtrap.vo.VOUser;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

public class SelectLanguageActivity extends ListActivity {

 // private Spinner spLanguages;
  private VOUser obUser;
  private int langPosi = 0;
  private String selectedLocale="en";
  private Preference prefs;
  private ListView listLang;
  private TextView welcomeTo;
  private TextView chooseUrLang;
  private Typeface tf;
  private Typeface tf_bold;
  private ArrayList<VOLanguage> alLanguages ;
  private LanguageAdapter listAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_language);

    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    if (prefs == null)
      prefs = new Preference(this);

    if (obUser == null)
      obUser = new VOUser(this);
    
    if (tf == null)
      tf = Typeface.createFromAsset(getAssets(), "fonts/ARIAL.TTF");
    
    if (tf_bold == null)
      tf_bold = Typeface.createFromAsset(getAssets(), "fonts/Arial Bold.ttf");
    
    if(alLanguages == null)
      alLanguages = new ArrayList<VOLanguage>();
    
    welcomeTo = (TextView) findViewById(R.id.welcomeTo);
    chooseUrLang = (TextView) findViewById(R.id.chooseUrLang);
    welcomeTo.setTypeface(tf, 0);
    chooseUrLang.setTypeface(tf, 0);
    
    listLang = getListView();
    String lstLang []= getResources().getStringArray(R.array.language_arrays);
    for(int i=0;i < lstLang.length;i++)
    {
      VOLanguage lang = new VOLanguage();
      lang.setName(lstLang[i]);
      lang.setChecked(false); 
      alLanguages.add(lang);
    }

    this.listAdapter = new LanguageAdapter(this, R.layout.listrow_language,
        alLanguages);
    setListAdapter(this.listAdapter);

    listLang.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> arg0, View view, int pos,
          long arg3) {
        VOLanguage items = alLanguages.get(pos);
        if(items.getChecked())
        {
          items.setChecked(false);
        }
        else
        {
          items.setChecked(true);

          langPosi = pos + 1;

          if (langPosi == 1) {
            selectedLocale = "en";
            obUser.setSelectedLanguage("1");
          } 
          else if (langPosi == 2) {
            selectedLocale = "da";
            obUser.setSelectedLanguage("2");
          }
          else if (langPosi == 3) {
            selectedLocale = "es";
            obUser.setSelectedLanguage("3");
          }
          else if (langPosi == 4) {
            selectedLocale = "fr";
            obUser.setSelectedLanguage("4");
          }
          else if (langPosi == 5) {
            selectedLocale = "de";
            obUser.setSelectedLanguage("5");
          }
        }
        alLanguages.set(pos,items);
        setListAdapter(listAdapter);

        Locale locale= new Locale(selectedLocale);
        Locale.setDefault(locale);
        prefs.setPreference("selectedLocale", selectedLocale);
        Configuration config= new Configuration();
        config.locale=locale;
        view.getResources().updateConfiguration(config,getResources().getDisplayMetrics());

        Intent intent = new Intent(SelectLanguageActivity.this,
            SignIntermediateActivity.class);
        startActivity(intent);
        finish();
      }
    });
  }

  private class LanguageAdapter extends ArrayAdapter<VOLanguage> {

    private VOLanguage language;
    private ImageView imgSelected = null;
    public LanguageAdapter(Context context, int textViewResourceId,
        ArrayList<VOLanguage> items) { 
      super(context,textViewResourceId,items);
    }

    @Override 
    public View getView(int position, View view, ViewGroup parent) { 

      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater
            .inflate(R.layout.listrow_language, null);
      }
      language = getItem(position);
      if(language !=null) 
      {
        TextView txtLang = (TextView) view.findViewById(R.id.txtLang); 
        txtLang.setTypeface(tf_bold, 0);
        imgSelected = (ImageView) view.findViewById(R.id.imgSelected); 
        txtLang.setText(language.getName());
        if(language.getChecked())
          imgSelected.setVisibility(View.VISIBLE);
        else
          imgSelected.setVisibility(View.GONE);

        imgSelected.setBackgroundDrawable(null);
      }
      return view;
    }
  }

}
