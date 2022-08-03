package com.example.mobileagent;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileagent.databinding.ActivityPanelBinding;

public class PanelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static public SharedPreferences sharedpreferences;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityPanelBinding binding;
    private NavigationView navigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent locationservice = new Intent(this,Tracking.class);

        binding = ActivityPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarPanel.toolbar);
        binding.appBarPanel.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_panel);

        //Navigation Item Selected Listener
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Shared Prefs
        sharedpreferences = getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        navigationView.setNavigationItemSelectedListener(PanelActivity.this);

        if(!isMyServiceRunning(Tracking.class)){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                Log.d("[-]PERMISSIONFAILURE", "Location Permission not granted");
                this.finishAffinity();
            }else{
                // Write you code here if permission already given.
                if(CheckGpsStatus()){
                    Log.d("[+]PERMISSIONSUCCESS", "Location Permission granted ...");
                    Log.d("[+]PERMISSIONSUCCESS", "Starting Tracking Service");
                    Toast.makeText(this, "PERMISSION SUCCESS",Toast.LENGTH_SHORT).show();
                    startService(locationservice);

                }else{
                    Intent myIntent = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS );
                    Toast.makeText(this, "Veuillez Activer Service de Localisation",Toast.LENGTH_LONG).show();
                    startActivity(myIntent);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.panel, menu);

        TextView TextViewUsername = (TextView) findViewById(R.id.textView_username);
        TextView TextViewEmail = (TextView) findViewById(R.id.textView_email);
        TextViewUsername.setText(sharedpreferences.getString("username", "user"));
        TextViewEmail.setText(sharedpreferences.getString("email", "email@email.com"));
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_panel);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //when an item is clicked
        switch (item.getItemId()){
            case R.id.nav_disconnect:{
                Disconnect();
                break;
            }
            case R.id.nav_home:{
                break;
            }
        }
        return false;
    }

    public void Disconnect(){
        Intent locationservice = new Intent(this, Tracking.class);
        if(isMyServiceRunning(Tracking.class)){
            stopService(locationservice);
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("connected",false);
        editor.putString("token",null);
        editor.putString("username",null);
        editor.putString("email",null);
        editor.commit();


        Intent login = new Intent(PanelActivity.this, MainActivity.class);
        startActivity(login);
        finish();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean CheckGpsStatus(){
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GpsStatus;
    }


}