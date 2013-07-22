package com.data.speedtrap.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;
import android.util.Log;

public class SpeedAPIController {
  private static DefaultHttpClient client = getClient();
  private static int TIMEOUT_MILLISEC = 40000;

  private static synchronized DefaultHttpClient getClient() {

    DefaultHttpClient defaultClient = new DefaultHttpClient();

    ClientConnectionManager mgr = defaultClient.getConnectionManager();

    HttpParams params = defaultClient.getParams();
    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
        mgr.getSchemeRegistry()), params);

    return client;
  }

  public static JSONObject getJSONfromURL_Get(String url) {
    InputStream is = null;
    String result = "";
    JSONObject jsonReturnObj = null;
    DefaultHttpClient httpclient = (DefaultHttpClient) getClient();
    try {
      if (android.os.Build.VERSION.SDK_INT > 9) {
        StrictMode.ThreadPolicy policy = 
            new StrictMode.ThreadPolicy.Builder().permitAll().build(); 
        StrictMode.setThreadPolicy(policy);
      }
      
      HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      HttpConnectionParams.setSoTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      
      
//      HttpParams httpParams = new BasicHttpParams();
//
//          HttpConnectionParams.setConnectionTimeout(httpParams,
//          TIMEOUT_MILLISEC);
      HttpGet httpget = new HttpGet(url);
      HttpResponse response = httpclient.execute(httpget);
      HttpEntity entity = response.getEntity();
      is = entity.getContent();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          is, "iso-8859-1"), 8);
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      is.close();
      result = sb.toString();

      jsonReturnObj = new JSONObject(result);
    } catch (JSONException e) {
      Log.e("log_tag", "Error parsing data " + e.toString());
    }
    catch (ConnectTimeoutException e) {
      try {
        jsonReturnObj = new JSONObject("{\"timeout\" : \"true\"}");
      } catch (JSONException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    catch (Exception e) {
      Log.e("log_tag", "Error parsing data " + e.toString());
    }  
    return jsonReturnObj;
  }

  public static JSONObject getJSONfromURL_Post(String url, List<NameValuePair> nameValuePair) {
    InputStream is = null;
    String result = "";
    JSONObject jsonReturnObj = null;
    DefaultHttpClient httpclient = (DefaultHttpClient) getClient();
    try {
      if (android.os.Build.VERSION.SDK_INT > 9) {
        StrictMode.ThreadPolicy policy = 
            new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
      } 
      HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      HttpConnectionParams.setSoTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      HttpPost httppost = new HttpPost(url);
      httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
      HttpResponse response = httpclient.execute(httppost);
      HttpEntity entity = response.getEntity();
      is = entity.getContent();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          is, "iso-8859-1"), 8);
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n"); 
      }
      is.close();
      result = sb.toString();
      if(!result.equals("[]\n"))
      jsonReturnObj = new JSONObject(result);
    } 
    catch (UnsupportedEncodingException e) {
      Log.e("log_tag_unsupport", "Error unsupported encoding " + e.toString());
    }
    catch (ConnectTimeoutException e) {
      try {
        jsonReturnObj = new JSONObject("{\"timeout\" : \"true\"}");
      } catch (JSONException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    catch (JSONException e) {
      Log.e("log_tag_json", "Error parsing data " + e.toString());
    }
    catch (Exception e) {
      Log.e("log_tag_common", "Error " + e.toString());
    }  
    return jsonReturnObj;
  }
  
  public static void getJSONfromURL_Get_NoReturn(String url) {
    InputStream is = null;
    String result = "";
    DefaultHttpClient httpclient = (DefaultHttpClient) getClient();
    try {
      if (android.os.Build.VERSION.SDK_INT > 9) {
        StrictMode.ThreadPolicy policy = 
            new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
      }
      
      HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      HttpConnectionParams.setSoTimeout(httpclient.getParams(), TIMEOUT_MILLISEC);
      
      HttpGet httpget = new HttpGet(url);
      HttpResponse response = httpclient.execute(httpget);
      HttpEntity entity = response.getEntity();
      is = entity.getContent();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          is, "iso-8859-1"), 8);
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      is.close();
      result = sb.toString();
    } 
    catch (ConnectTimeoutException e) {
      Log.e("log_tag", "Error parsing data " + e.toString());
    }
    catch (Exception e) {
      Log.e("log_tag", "Error parsing data " + e.toString());
    }  
  }
}
