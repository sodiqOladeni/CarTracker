package ng.com.sodiqoladeni.cartracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SmsSentIntentService extends IntentService {

    private Context context;
    private static final String TAG = SmsSentIntentService.class.getSimpleName();

    public SmsSentIntentService() {
        super("SmsSentIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = s.edit();
            editor.putInt("SMS_SENT", s.getInt("SMS_SENT", 1)+1);
            Log.v(TAG, "sms-sent");
            editor.apply();
        }
    }
}
