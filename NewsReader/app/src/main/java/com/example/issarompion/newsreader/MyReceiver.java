package com.example.issarompion.newsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MyReceiver extends BroadcastReceiver {


    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        String[] location = intent.getStringArrayExtra("position");


        JSONObject json = new JSONObject();
        try {
            json.put("imei",MainActivity.IMEI);
            JSONObject coordinates = new JSONObject();
            coordinates.put("lat",location[0]);
            coordinates.put("lng",location[1]);
            json.put("coordinates", coordinates);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(json.toString());


        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        StringEntity entity = null;
        try {
            entity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        asyncHttpClient.post(context,"http://10.0.2.2:8080/location",entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody); // this is your response string
                System.out.println("Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Here you write code if there's error
                System.out.println("Failure");
            }
        });






    }
}
