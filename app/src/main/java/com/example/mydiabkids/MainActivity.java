package com.example.mydiabkids;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mydiabkids.glucosevalues.sensor.SensorFragment;
import com.example.mydiabkids.glucosevalues.sensor.SensorService;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.mydiabkids.ThemeFragment.BLUE;
import static com.example.mydiabkids.ThemeFragment.BROWN;
import static com.example.mydiabkids.ThemeFragment.GREEN;
import static com.example.mydiabkids.ThemeFragment.ORANGE;
import static com.example.mydiabkids.ThemeFragment.PINK;
import static com.example.mydiabkids.ThemeFragment.YELLOW;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isSensorRunning;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private SensorService mService;
    private boolean isBound = false;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).setOpenableLayout(drawerLayout).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(navView, navController);

        serviceIntent = new Intent(this, SensorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        unbindService(connection);
        isBound = false;
        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SensorService.SensorBinder binder = (SensorService.SensorBinder) service;
            mService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if(isSensorRunning.get()){
                isSensorRunning.set(false);
            }
            isBound = false;
        }
    };

    private void setTheme(){
        SharedPreferences themeSharedPreferences = this.getSharedPreferences(getString(R.string.theme_shared_pref), MODE_PRIVATE);
        SharedPreferences darkMode = this.getSharedPreferences(getString(R.string.dark_shared_pref), MODE_PRIVATE);
        int theme = themeSharedPreferences.getInt(getString(R.string.theme_shared_pref), PINK);
        boolean isDark = darkMode.getBoolean(getString(R.string.dark_shared_pref), false);
        switch (theme){
            case PINK:
                setTheme(R.style.PinkTheme);
                break;
            case BLUE:
                setTheme(R.style.BlueTheme);
                break;
            case GREEN:
                setTheme(R.style.GreenTheme);
                break;
            case YELLOW:
                setTheme(R.style.YellowTheme);
                break;
            case ORANGE:
                setTheme(R.style.OrangeTheme);
                break;
            case BROWN:
                setTheme(R.style.BrownTheme);
                break;
        }
        if(isDark) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

}
