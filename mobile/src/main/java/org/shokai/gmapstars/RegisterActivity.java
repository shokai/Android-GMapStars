package org.shokai.gmapstars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;

import org.shokai.gmapstars.lib.GMap;
import org.shokai.gmapstars.lib.GeofenceManager;


public class RegisterActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "RegisterActivity";

    private GoogleApiClient mGoogleApiClient;

    private TextView mTextView;
    private Button mButtonAddGeofence, mButtonRemoveGeofence;
    private GMap.Location location;
    private GeofenceManager geofenceManager;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mIntent = this.getIntent();
        if(mIntent == null || !mIntent.getAction().equals(Intent.ACTION_SEND)){
            Log.e(TAG, "invalid Intent");
            Toast.makeText(this, "invalid Intent", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mTextView = (TextView) findViewById(R.id.textView);
        mButtonAddGeofence = (Button) findViewById(R.id.buttonAddGefence);
        mButtonRemoveGeofence = (Button) findViewById(R.id.buttonRemoveGefence);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();

        geofenceManager = new GeofenceManager(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        GMap.getLocation(mIntent, new GMap.LocationCallback() {
            @Override
            public void onSuccess(GMap.Location location) {
                Log.i("location", location.toString());
                mTextView.setText(location.toString());
                RegisterActivity.this.location = location;
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        });

        // Geofenceを登録する
        mButtonAddGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (location == null || geofenceManager == null) return;
                geofenceManager.add(location, new ResultCallback() {
                    @Override
                    public void onResult(Result result) {
                        Toast.makeText(RegisterActivity.this, result.getStatus().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
    }
}
