package es.mediplus.mediplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;

/**
 * Created by jon on 19/04/16.
 */
public class Inicio extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
        String name = sharedPref.getString(Utils.CLIENT_ID, "default");

        if (!name.matches("default")) {
            Intent intent = new Intent("es.mediplus.mediplus.MENU");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.finish();
            startActivity(intent);

        } else {
            Intent intent = new Intent("es.mediplus.mediplus.LOGING");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.finish();
            startActivity(intent);
        }
    }


}