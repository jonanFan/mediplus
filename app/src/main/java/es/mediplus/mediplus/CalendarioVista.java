package es.mediplus.mediplus;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jon on 7/03/16.
 */
public class CalendarioVista extends Activity implements View.OnClickListener, Serializable {

    private ExtendedCalendarView extendedCalendarView;
    private CalendarioLogica calendarioLogica;
    private GoogleAccountCredential credential;
    private String type;
    private CalendarioAsyncTask calendarAsyncTask;
    private boolean stopMusic = true;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendario);
        SharedPreferences sharedPref = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
        type = sharedPref.getString(Utils.CLIENT_TYPE, "null");
        init();
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Utils.SCOPES)).
                setBackOff(new ExponentialBackOff()).setSelectedAccountName((getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE)).getString(Utils.CUENTA_USUARIO, null));
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
                calendarAsyncTask = new CalendarioAsyncTask(this, credential);
                //calendarAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                calendarAsyncTask.execute((Void) null);
            } else {
                Toast.makeText(this, "Has borrado tu cuenta de Google", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_CANCELED, null);
                this.finish();
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
    public void onBackPressed() {
        super.onBackPressed();
        stopMusic = false;
        setResult(Activity.RESULT_OK, null);
        this.finish();
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
                        CalendarioVista.this, connectionStatusCode,
                        Utils.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    protected void init() {

        calendarioLogica = new CalendarioLogica(this);
        this.extendedCalendarView = new ExtendedCalendarView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        extendedCalendarView.setLayoutParams(params);

        if (type.equalsIgnoreCase(Utils.ADMIN)) {
            FloatingActionButton floatingActionButton = new FloatingActionButton(this);
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Utils.getpixels(this, extendedCalendarView.getDps() - 10);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            floatingActionButton.setLayoutParams(params);
            floatingActionButton.setClickable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                floatingActionButton.setImageDrawable(this.getResources().getDrawable(R.drawable.plus, this.getTheme()));
            } else {
                floatingActionButton.setImageDrawable(this.getResources().getDrawable(R.drawable.plus));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                floatingActionButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.blue, this.getTheme()));
            } else {
                floatingActionButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.blue));
            }

            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopMusic = false;
                    startActivityForResult(new Intent("com.example.alumno.pruebacalen.NUEVOEVENTO"), 1);
                }
            });
            extendedCalendarView.addView(floatingActionButton);
        }

        ((RelativeLayout) findViewById(R.id.base)).addView(extendedCalendarView);


        ExtendedCalendarView.OnDayClickListener days = new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
                refreshEvents(day, position);
            }
        };
        extendedCalendarView.setOnDayClickListener(days);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent recogerDatos) {
        super.onActivityResult(requestCode, resultCode, recogerDatos);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Cita cita=new Cita(recogerDatos.getStringExtra("nombre"),recogerDatos.getStringExtra("descripcion"),recogerDatos.getStringExtra("lugar"));
                    cita.setTime(recogerDatos.getStringExtra("fechaInicio"),recogerDatos.getStringExtra("fechaFinal"), false, false);
                    addDate(cita);
                }
                break;
         /*   case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && recogerDatos != null &&
                        recogerDatos.getExtras() != null) {
                    String accountName =
                            recogerDatos.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Utils.CUENTA_USUARIO, accountName);
                        editor.apply();
                    }
                }
                break;*/
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, recogerDatos);
    }

    protected void refreshEvents(Day day, int position) {

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Tagger tagger = new Tagger(position);
        ArrayList<Event> array = day.getEvents();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.scroll);
        linearLayout.removeAllViews();
        TextView textView;
        for (com.tyczj.extendedcalendarview.Event even : array) {
            if (even.getFlag() != 1) {
                textView = new TextView(this);
                textView.setLayoutParams(params);
                textView.setText(even.getTitle() + ": " + even.getDescription() + "\n\tFECHA\n\t\t" + even.getStartDate("yyyy-MM-dd HH:mm") + " A " + even.getEndDate("yyyy-MM-dd HH:mm"));
                tagger.setEvent(even);
                textView.setTag(tagger);
                textView.setOnClickListener(this);
                linearLayout.addView(textView);
                View v = new View(this);
                v.setBackgroundColor(0xFF00FF00);
                v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                linearLayout.addView(v);
            }
        }
    }

    private class Tagger {
        private int position;
        private Event event;

        public Tagger(int position) {
            this.position = position;
        }

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public int getPosition() {
            return position;
        }
    }

    protected void addDate(Cita cita) {
        calendarioLogica.addDate(cita);
        extendedCalendarView.refreshCalendar();
    }

    protected void deleteDate(long id) {
        calendarioLogica.sendDelete(id);
    }

    @Override
    public void onClick(View v) {
        final Tagger tagger = (Tagger) v.getTag();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(tagger.getEvent().getTitle());
        alert.setMessage("FECHA\n" + " De " + tagger.getEvent().getStartDate("yyyy-MM-dd HH:mm") + "\n A " + tagger.getEvent().getEndDate("yyyy-MM-dd HH:mm") +
                ((tagger.getEvent().getDescription().equals("")) ? "" : "\n\nDESCRIPCION\n" + tagger.getEvent().getDescription()) +
                ((tagger.getEvent().getLocation().equals("")) ? "" : "\n\nLUGAR\n" + tagger.getEvent().getLocation()));
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (type.equalsIgnoreCase(Utils.ADMIN)) {
            alert.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteDate(tagger.getEvent().getEventId());
                    Day day = extendedCalendarView.getDayAfterEventDelete(tagger.getPosition());
                    refreshEvents(day, tagger.getPosition());
                    dialog.dismiss();
                }
            });
        }
        alert.show();
    }

    public void refrescarVista() {
        extendedCalendarView.refreshCalendar();
    }
}
