package com.project.emergencyhelpapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int count = 0;
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 2;

    private MyNavigationService myNavigationService;
    private boolean isBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyNavigationService.LocalBinder binder = (MyNavigationService.LocalBinder) service;
            myNavigationService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind to MyNavigationService
        Intent intent = new Intent(this, MyNavigationService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // Request necessary permissions
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PackageManager.PERMISSION_GRANTED
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = getIntent().getExtras();
        String V1 = extras.getString(Intent.EXTRA_TEXT);
        Log.d("NumberMainActivity", V1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    public void addRelative(View v) {
        Intent i = new Intent(getApplicationContext(), AddRelative.class);
        startActivity(i);
    }

    public void helplineNumbers(View v) {
        Intent i = new Intent(getApplicationContext(), helplineCall.class);
        startActivity(i);
    }

    public void triggers(View v) {
        sendTextMessage();
//
//        Log.d("trigger", "playing trigger");
//        Intent i = new Intent(getApplicationContext(), TrigActivity.class);
//        startActivity(i);
    }

    public void LogOut(View v) {
        try {
            Log.d("LogOut", "Logging out...");
            Intent i = new Intent(getApplicationContext(), Logout.class);
            startActivity(i);
            finish(); // Optional: Finish the current activity to prevent the user from returning to it via the back button
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error logging out", Toast.LENGTH_SHORT).show();
        }
    }

    private int volumeUpCount = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN) {
            // Volume up button pressed
            volumeUpCount++;
            if (volumeUpCount == 5) {
                // Volume up button pressed 5 times
                Log.d("Inside DKE", "Calling function send text msg");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions if not granted
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSION_REQUEST_SEND_SMS);
                } else {
                    // Permissions already granted, send SMS and get location
                    sendTextMessage();
                }
                volumeUpCount = 0; // Reset the count
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void sendTextMessage() {
        if (isBound) {
            myNavigationService.getCurrentLocation(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        String locationMessage = "I AM IN DISTRESS!!! PLEASE HELP ME!! Location: " +
                                "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();

                        // Get contact numbers from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                        ArrayList<String> contactNumbers = dbHelper.getContactNumbers();
                        if (contactNumbers.isEmpty()) {
                            Log.d("NR", "Numbers not retrieved");
                        }
                        Log.d("Location","Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                        SmsManager mySmsManager = SmsManager.getDefault();

                        // Send text message to each contact number
                        for (String number : contactNumbers) {
                            Log.d("Inside DKE", number + " Sending msg ");
                            mySmsManager.sendTextMessage(number, null, locationMessage, null, null);
                            Toast.makeText(MainActivity.this, "Sending msg to: " + number, Toast.LENGTH_SHORT).show();
                        }

                        Log.d("number",contactNumbers.get(1));

                        helplineCall helplineCallinstance = new helplineCall();
                        helplineCallinstance.callPhoneNumber(MainActivity.this, contactNumbers.get(1));

                    } else {
                        Toast.makeText(MainActivity.this, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendTextMessage();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // Location permission granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
