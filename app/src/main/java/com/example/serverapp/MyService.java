package com.example.serverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
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
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        String sendText = "";


        //Get user input from intent data
        if(intent.getExtras()!=null)
            sendText = intent.getExtras().getString("Id");

        //Call async task to communicate with server
        if(intent.getExtras()!=null)
            new ServerCall(getApplicationContext(), intent.getExtras().getString("Id")).execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}


class ServerCall extends AsyncTask<String, String, String> {

    Context appContext;
    String sendText = "";

    public ServerCall(Context cntxt, String temp){
        //set context variables if required
        appContext = cntxt;

        //Initialise the user input to send
        sendText = temp;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Build Url post parameters and encode it using UTF-8
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

        return result.toString();
    }

    @Override
    protected String doInBackground(String... params) {

        String urlString = "https://postman-echo.com/post"; // URL to call
        HashMap<String, String> postDataParams = new HashMap<String, String>(); ;
        OutputStream out = null;
        String response = ""; //response string
        postDataParams.put("data", sendText); //data to send

        try {
            //Url Connection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            out.close();

            //Open connection
            urlConnection.connect();

            int responseCode=urlConnection.getResponseCode();

            //Read response
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

        try {
            //Parse Json Response
            JSONObject reader = new JSONObject(response);
            JSONObject json = reader.getJSONObject("json");
            response = json.getString("data");
        }
        catch (final JSONException e) {
            Log.e("JSONException","Json parsing error: " + e.getMessage());
        }

        //Send response string to client application
        Intent i = new Intent("INTENT_RESPONSE");
        i.putExtra("Res", response);
        appContext.sendBroadcast(i);

        return null;
    }
}

