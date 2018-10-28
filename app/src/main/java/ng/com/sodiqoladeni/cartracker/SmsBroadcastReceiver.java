package ng.com.sodiqoladeni.cartracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SmsBroadcastReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String senderNumber = "";
        String format = bundle.getString("format");

        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                //Get phone number of the sender
               // senderNumber = msgs[i].getOriginatingAddress();
                strMessage = msgs[i].getMessageBody();
                try {
                    if (strMessage.contains("Tracking") &&
                            strMessage.contains("Latitude:") && strMessage.contains("Longitude:")) {
                            String currentLocation = strMessage.substring(3);
                            Log.v(TAG, strMessage.substring(3));

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("CURRENT_LAT", extractLocationInfo(currentLocation)[0]);
                        editor.putString("CURRENT_LONG", extractLocationInfo(currentLocation)[1]);
                        editor.apply();
                        NotificationUtils.notifyUserAboutLocation(context,
                                extractLocationInfo(currentLocation)[0], extractLocationInfo(currentLocation)[1]);
                    }
                }catch (NullPointerException e){
                    e.getStackTrace();
                }
            }
        }
    }

    private String[] extractLocationInfo(String s){
        String[] split = s.split(" ");
        String lat = split[3];
        String lon = split[5];
        return new String[] {lat, lon};
    }
}
