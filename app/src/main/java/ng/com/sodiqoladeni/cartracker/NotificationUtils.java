package ng.com.sodiqoladeni.cartracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

public class NotificationUtils {
    private static final int DETAILS_PAGE_PENDING_INTENT_ID = 3300;
    private static final String SHOW_USER_ADS_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";
    private static final int SHOW_USER_ADS_NOTIFICATION_ID = 500;


    public static void notifyUserAboutLocation(Context context, String lat,
                                                   String lon){

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    SHOW_USER_ADS_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context, SHOW_USER_ADS_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                //.addAction(R.drawable.ic_location_on, "SHOW", detailsPageIntent(context, lat, lon))
                .setContentText("Current location for your car\n"+"Latitude: "+lat+"\nLongitude: "+lon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Current location for your car\n"+"Latitude: "+lat+"\nLongitude: "+lon))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(detailsPageIntent(context, lat, lon))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(SHOW_USER_ADS_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static PendingIntent detailsPageIntent(Context context, String lat, String lon) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("LAT", lat);
        intent.putExtra("LONG", lon);
        return PendingIntent.getActivity(
                context,
                DETAILS_PAGE_PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

    }
}
