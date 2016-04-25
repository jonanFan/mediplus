package es.mediplus.mediplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ene on 02/03/2016.
 */
public class Ajustes extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Musica.stop();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new AjustesFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean play = sharedPref.getBoolean("musica", false);

        if (play)
            Musica.start(this, R.raw.audio);
    }
}