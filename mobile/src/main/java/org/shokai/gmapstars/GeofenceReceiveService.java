package org.shokai.gmapstars;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.identity.intents.AddressConstants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceReceiveService extends IntentService {

    private static String TAG = "GeofenceReceiveService";

    public GeofenceReceiveService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null && !event.hasError()) {
            int transition = event.getGeofenceTransition();
            Geofence fence = event.getTriggeringGeofences().get(0);
            Bundle extras = intent.getExtras();

            String id = fence.getRequestId();
            String url = extras.getString("URL");
            String name = extras.getString("NAME");
            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.i("TAG", name+" "+url);
                sendWearIntent(Uri.parse(id), name);
            }
        }
    }


    public void sendWearIntent(Uri uri, String name){
        Log.i(TAG, "sendWearIntent");

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notif = new NotificationCompat.Builder(this)
                .setContentTitle("GMap Stars")
                .setContentText(name+"が近いです")
                .setSmallIcon(android.R.drawable.btn_default_small)
                .addAction(android.R.drawable.ic_dialog_map, "Mapを開く", pIntent)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(0, notif);
    }

}
