package ng.com.sodiqoladeni.cartracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

import dmax.dialog.SpotsDialog;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private AlertDialog alertDialog;
    private SharedPreferences prefs;
    private String latitude, longitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkForSmsPermission();


        /** Setup the shared preference listener **/
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        final ToggleButton toggleButton = findViewById(R.id.btn_switch);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = toggleButton.getText().toString();
                switch (state){
                    case "ON":
                        toggleButton.setText("OFF");
                        sendSmsToSystemBoard(MainActivity.this, "L");
                        prefs.edit().putBoolean(getString(R.string.c_state), true).apply();
                        break;
                    case "OFF":
                        toggleButton.setText("ON");
                        sendSmsToSystemBoard(MainActivity.this, "U");
                        prefs.edit().putBoolean(getString(R.string.c_state), false).apply();
                        break;
                }
            }
        });

        ImageView img = findViewById(R.id.icon_location);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToSystemBoard(MainActivity.this, "@");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** Cleanup the shared preference listener **/
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        p.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle b = intent.getExtras();
        if (b != null){
            latitude = b.getString("LAT");
            longitude = b.getString("LONG");
            Log.v(TAG, "from new intent "+latitude+" : "+longitude);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (latitude == null || longitude == null) {

            String lat = prefs.getString("CURRENT_LAT", "7.4154961");
            String lon = prefs.getString("CURRENT_LONG", "3.9048360");

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
            float zoomLevel = 10.0f; //This goes upto 21
            mMap.addMarker(new MarkerOptions().position(sydney).title("Lat: "+lat+" Long: "+lon));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));
        }else{
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
            float zoomLevel = 10.0f; //This goes upto 21
            mMap.addMarker(new MarkerOptions().position(sydney).title("Lat: "+latitude+" Long: "+longitude));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));
        }
    }


    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted");
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    PERMISSIONS_REQUEST_SEND_SMS);
        }
    }

    private void sendSmsToSystemBoard(Context context, String sms){

        startDialog("Please wait");
        //Generate random number for Sms Sent Pending Intent
        Random randSent = new Random();

        //Generate time stamp for Sms Delivered Pending Intent
        Random randDelvd = new Random();

        // Set pending intents to broadcast
        //Notify SmsSentIntentService.class when message has been delivered
        Intent smsSentIntent = new Intent(context, SmsSentIntentService.class);
        //Build the Pending intent
        PendingIntent sentIntent = PendingIntent.getService(context, randSent.nextInt(),
                smsSentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Notify SmsSentIntentService.class when message has been delivered
        Intent smsDeliveredIntent = new Intent(context, SmsDeliverIntentService.class);
        //Build the Pending intent
        PendingIntent deliveryIntent = PendingIntent.getService(context, randDelvd.nextInt(),
                smsDeliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Use SmsManager. to send the message
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                "08165831509",
                //Service center address can be set to null
                null,
                //e.g SMEC 08038325215 1500 0000
                sms,
                //PendingIntent to call when the message has been sent
                sentIntent,
                //PendingIntent to call when the message has been delivered
                deliveryIntent
        );
    }

    private void startDialog(String m){
         alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage(m)
                .setCancelable(false).build();
        alertDialog.show();
    }

    private void stopDialog(){
        if (alertDialog != null){
            alertDialog.cancel();
            AlertDialog.Builder  alert = new AlertDialog.Builder(this);
            alert.setMessage("Signal has been sent, you will be notify shortly");
            alert.setPositiveButton("Ok", null);
            alert.show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences s, String key) {
        Log.v(TAG, "onSharedPreferenceChanged");
        switch (key){
            case "CURRENT_LOCATION":
                String location = s.getString("CURRENT_LOCATION", "");
                Toast.makeText(this, location, Toast.LENGTH_SHORT).show();
                break;
            case "SMS_DELIVERED":
                lockUnlockbasedOnState(prefs.getBoolean(getString(R.string.c_state), false));
                stopDialog();
                break;
            case "SMS_SENT":
                lockUnlockbasedOnState(prefs.getBoolean(getString(R.string.c_state), false));
                stopDialog();
                break;
        }
    }

    private void lockUnlockbasedOnState(@NonNull Boolean state){
        ToggleButton iconLock = findViewById(R.id.btn_switch);
        if (state){
            iconLock.setText("ON");
        }else{
            iconLock.setText("OFF");
        }
    }
}
