package com.example.mydiabkids;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mydiabkids.glucosevalues.sensor.SensorFragment;
import com.example.mydiabkids.glucosevalues.sensor.SensorService;
import com.google.android.material.navigation.NavigationView;

import static com.example.mydiabkids.settings.ThemeFragment.BLUE;
import static com.example.mydiabkids.settings.ThemeFragment.BROWN;
import static com.example.mydiabkids.settings.ThemeFragment.GREEN;
import static com.example.mydiabkids.settings.ThemeFragment.ORANGE;
import static com.example.mydiabkids.settings.ThemeFragment.PINK;
import static com.example.mydiabkids.settings.ThemeFragment.YELLOW;
import static com.example.mydiabkids.glucosevalues.sensor.SensorService.isSensorRunning;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private SensorService mService;
    private boolean isBound = false;
    Intent serviceIntent;
    public static final String CHANNEL_ID = "First channel";
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    private SensorFragment sensorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            sensorFragment = (SensorFragment) getSupportFragmentManager().getFragment(savedInstanceState, "SensorFragment");
        }
*/
        drawerLayout = findViewById(R.id.drawer_layout);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).setOpenableLayout(drawerLayout).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(navView, navController);

        createNotificationChannel();

        serviceIntent = new Intent(this, SensorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        notificationManager = NotificationManagerCompat.from(this);
        Intent intent = new Intent(this, SensorFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.blood)
                .setContentTitle("Vigyázat!")
                .setContentText("Ha bezárod az alkalmazást, nem kapsz értesítéseket a szenzortól!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        notificationManager.notify(3, builder.build());
        stopService(serviceIntent);
        unbindService(connection);
        isBound = false;
        super.onDestroy();
    }

  /*  @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        getSupportFragmentManager().putFragment(outState, "SensorFragment", sensorFragment);
    }*/

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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NotificationChannel";
            String description = "Channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
