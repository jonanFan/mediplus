package es.mediplus.mediplus;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.util.ArrayList;
import java.util.Arrays;

public class Menu extends Activity {

    private Boolean exit = false;
    private GoogleAccountCredential credential;
    private boolean stopMusic = true;
    private CalendarioAsyncTask calendarAsyncTask;
    private ProgressDialog progress = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        SharedPreferences settings = getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Utils.SCOPES)).
                setBackOff(new ExponentialBackOff()).setSelectedAccountName(settings.getString(Utils.CUENTA_USUARIO, null));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean playMusic = sharedPref.getBoolean("musica", false);

        if (playMusic)
            Musica.start(this, R.raw.audio);

        Sincronizacion sincr = new Sincronizacion();

        sharedPref = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
        int idCliente = Integer.parseInt(sharedPref.getString(Utils.CLIENT_ID, "-1"));
        String tipoUsuario = sharedPref.getString(Utils.CLIENT_TYPE, "default");

        sincr.realizarSincr(tipoUsuario, this, String.valueOf(idCliente));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stopMusic) {
            Musica.play();
        }
        stopMusic = true;

        if (isGooglePlayServicesAvailable()) {

            GoogleAccountCredential aux = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Utils.SCOPES)).
                    setBackOff(new ExponentialBackOff()).setSelectedAccountName(credential.getSelectedAccountName());
            if (aux.getSelectedAccountName() != null) {
                calendarAsyncTask = new CalendarioAsyncTask(Menu.this, credential);
                calendarAsyncTask.execute((Void) null);
                // calendarAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                returnLoging(null);
                Toast.makeText(this, "Has borrado tu cuenta de Google", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stopMusic)
            Musica.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Musica.stop();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability available = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                available.isGooglePlayServicesAvailable(this);
        if (available.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GoogleApiAvailability available = GoogleApiAvailability.getInstance();
                Dialog dialog = available.getErrorDialog(
                        Menu.this, connectionStatusCode,
                        Utils.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    public void goCalendario(View v) {
        stopMusic = false;
        Intent intent = new Intent("es.mediplus.mediplus.CALENDARIO");
        startActivityForResult(intent, Activity.RESULT_OK);
    }

    public void goMedicamentos(View v) {
        stopMusic = false;
        Intent intent = new Intent("es.mediplus.mediplus.MEDICAMENTOS");
        startActivity(intent);
    }

    public void goAjustes(View v) {
        stopMusic = false;
        Intent intent = new Intent("es.mediplus.mediplus.AJUSTES");
        startActivity(intent);
    }

    public void returnLoging(View v) {
        progress = new ProgressDialog(this);
        progress.setMessage("Iñigo esta trabajando en las notificaciones. Por favor espere...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setProgress(0);
        progress.setMax(100);
        progress.setCancelable(false);
        progress.show();
        aumentarProgreso();

        BaseLocal bl = new BaseLocal(Menu.this);

        ArrayList<Medicamento> listAux = bl.obtenerMedis();

        for (int i = 0; i < listAux.size(); i++) {
            ReminderManager.cancelReminder(this, listAux.get(i));
        }

        ExitAsyncTask exitAsyncTask = new ExitAsyncTask();
        exitAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void aumentarProgreso() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(progress.getProgress() + 1);
                if (progress.getProgress() < 100)
                    aumentarProgreso();
            }
        }, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("asd", "El resultcode es " + requestCode + "y resultok es " + RESULT_OK + " y resultcancel es " + RESULT_CANCELED);
        switch (resultCode) {
            case RESULT_OK:
                break;

            case RESULT_CANCELED:
                returnLoging(null);
                break;
        }
    }

    public void finishing() {
        Toast.makeText(this, "Lo sentimos, no ha sido capaz de hacerlo :(", Toast.LENGTH_LONG).show();
        CalendarioLogica logica = new CalendarioLogica(Menu.this);
        logica.deleteAll();
        BaseLocal bl = new BaseLocal(Menu.this);

        SharedPreferences preferencias = getSharedPreferences(Utils.CALENDAR_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.apply();

        SharedPreferences.Editor edito = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE).edit();
        edito.clear();
        edito.apply();

        SharedPreferences sharedPref1 = PreferenceManager.getDefaultSharedPreferences(Menu.this);
        sharedPref1.edit().clear().apply();

        Intent intent = new Intent("es.mediplus.mediplus.LOGING");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        progress.dismiss();
        bl.eliminarTabla();
        this.finish();
        startActivity(intent);
    }

    public class ExitAsyncTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {
            while (calendarAsyncTask != null && calendarAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //TODO CAMBIAR ESTO PARA LA VERSION FINAL
            progress.setProgress(90);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            finishing();
        }
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            this.finish(); // finish activity
        } else {
            Toast.makeText(this, "Presiona Atras otra vez para salir.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }
}
