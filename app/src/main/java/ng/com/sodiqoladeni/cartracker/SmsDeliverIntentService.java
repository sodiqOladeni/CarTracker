package ng.com.sodiqoladeni.cartracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SmsDeliverIntentService extends IntentService {

    private Context context;
    private static final String TAG = SmsDeliverIntentService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public SmsDeliverIntentService() {
        super("SmsDeliverIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = s.edit();
            editor.putInt("SMS_DELIVERED", s.getInt("SMS_DELIVERED", 1)+1);
            Log.v(TAG, "sms-delivered");
            editor.apply();
        }
    }
}
