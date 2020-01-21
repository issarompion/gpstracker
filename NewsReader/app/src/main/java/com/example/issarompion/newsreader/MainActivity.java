package com.example.issarompion.newsreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    LinearLayout layout;
    JSONArray json;
    ScrollView scroll;
    TextView DV;
    TextView EV;
    ImageView IV;
    Date date;
    BroadcastReceiver receiver;
    public static String IMEI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        IMEI = (String) telephonyManager.getDeviceId();


        configureReceiver();


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        layout = (LinearLayout) findViewById(R.id.ll);
        scroll = (ScrollView) findViewById(R.id.scroll);
        DV = new TextView(this);
        EV = new TextView(this);
        IV = new ImageView(this);

         date = new Date();

        try { json = getJSONArray(date); } catch (IOException e) { e.printStackTrace(); }

        try { addItems(DV, EV, IV);} catch (JSONException e) { e.printStackTrace(); }

        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!scroll.canScrollVertically(1)) {
                    // bottom of scroll view
                    date = yesterday(date);
                    try { json = getJSONArray(date); } catch (IOException e) { e.printStackTrace(); }
                    try { addItems(DV, EV, IV);} catch (JSONException e) { e.printStackTrace(); }
                }
                }
        });

    }


    public JSONArray getJSONArray(Date date) throws IOException{

        String endDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
        String startDate = new SimpleDateFormat("yyyy-MM-dd").format(yesterday(date));

        String url = "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY&start_date="+startDate+"&end_date="+endDate;


        URLConnection connection = new URL(url).openConnection();
        connection.connect();

        BufferedReader r  = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        try {
            json = new JSONArray(sb.toString());
            System.out.println(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void addItems(TextView DateView, TextView ExpView, ImageView ImageView) throws JSONException {

            JSONObject jsonO = json.getJSONObject(0);

            String date = jsonO.getString("date");
            String explanation = jsonO.getString("explanation");
            String imageUrl = jsonO.getString("url");


            DateView.append(date);
            DateView.setGravity(Gravity.CENTER);
            DateView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            ExpView.append(explanation);
            ImageView.setImageDrawable(LoadImageFromWebOperations(imageUrl));

            layout.addView(ImageView);
            layout.addView(DateView);
            layout.addView(ExpView);

            DV = new TextView(this);
            EV = new TextView(this);
            IV = new ImageView(this);

        }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public Date yesterday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }


    private void configureReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.issarompion.broadcast");
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);
    }
}
