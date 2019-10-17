package com.example.serverapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.os.Build;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    /* Check and request for permissions */
    public  boolean haveStoragePermission()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkSelfPermission(android.Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.INTERNET}, 1);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check and request for permissions
        haveStoragePermission();

        //Intent to get input data from Application A
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //Call AsyncTask to send request, receive json and process response
        if(intent.getExtras()!=null)
            new CallAPI(this, intent.getExtras().getString("Id")).execute();
    }
}

class CallAPI extends AsyncTask<String, String, String> {

    MainActivity activityInstance;
    String sendText = "";

    public CallAPI(MainActivity instance, String text){
        //set context variables if required
        activityInstance = instance;
        sendText = text;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        //data string to post
        return result.toString();
    }

    @Override
    protected String doInBackground(String... params) {

        String urlString = "https://postman-echo.com/post"; // URL to call
        String data = "1"; //data to post
        HashMap<String, String> postDataParams = new HashMap<String, String>(); ;
        OutputStream out = null;
        String response = "";

        //Add data from input of Application A
        postDataParams.put("data", sendText);

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            out.close();

            urlConnection.connect();

            int responseCode=urlConnection.getResponseCode();

            //get response string
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Response", response);

        try {
            //Get json object, key, values
            JSONObject reader = new JSONObject(response);
            JSONObject json = reader.getJSONObject("json");
            response = json.getString("data");
            Log.d("Json Response", response);
        }
        catch (final JSONException e) {
            Log.e("JSONException","Json parsing error: " + e.getMessage());
        }

        //Return thr result to Application A
        Intent sendIntent = new Intent();
        sendIntent.putExtra("Res", response);
        if (activityInstance.getParent() == null) {
            activityInstance.setResult(Activity.RESULT_OK, sendIntent);
        } else {
            activityInstance.getParent().setResult(Activity.RESULT_OK, sendIntent);
        }
        activityInstance.finish();

        return response;
    }
}


