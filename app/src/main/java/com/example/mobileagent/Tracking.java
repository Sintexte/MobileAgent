package com.example.mobileagent;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Tracking extends Service {
    Timer time;


    public Tracking() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timer time = new Timer();
        Log.d("[***]debugService: ", "Started Tracking Service");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        time.cancel();
        Log.d("[***]debugService: ", "Stoped Tracking Service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Running", Toast.LENGTH_LONG).show();
        time = new Timer();
        time.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendLocation();
            }
        }, 0, MainActivity.TRACKINGINTERVALMS);

        return START_STICKY;
    }

    public void sendLocation() {
        String TAGERR = "[****] SERVICE ERROR: ";
        String TAG = "[****] SERVICE: ";
        Log.d("[****] SENDING LOCATION", "(0,0)");
        String url = MainActivity.host + "/setlocation";

        try {
            JSONObject jsonbody = new JSONObject();
            JSONObject jsondata = new JSONObject();

            SharedPreferences sharedpreferences = getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);

            //get gps location here
            LatLng location = getLocation();
            if(location == null){
                Log.e("[-]NULLLOCATIONERR", "");
                return;
            }

            jsondata.put("latitude", location.latitude);
            jsondata.put("longitude", location.longitude);


            jsonbody.put("token", sharedpreferences.getString("token", null));
            jsonbody.put("data", jsondata);


            final String mRequestBody = jsonbody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Location Sent [" + response + "]");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Not Supported");
                        return null;
                    }
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            Log.d(TAGERR, e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public LatLng getLocation() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("[-] NOPERMISSIONS: ", "getLocation" );
            return null;
        }else{
            Location location = locationManager.getLastKnownLocation(bestProvider);
            Double lat,lon;
            try {
                lat = location.getLatitude ();
                lon = location.getLongitude ();
                return new LatLng(lat, lon);
            }
            catch (NullPointerException e){
                e.printStackTrace();
                return null;
            }
        }

    }


}