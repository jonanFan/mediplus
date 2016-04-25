package es.mediplus.mediplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.CalendarScopes;

import org.joda.time.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jon on 18/03/16.
 */
public class Utils {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final String[] SCOPES = {CalendarScopes.CALENDAR};


    static final String CALENDAR_NAME = "Mediplus-citas";
    static final String CALENDAR_ID = "calendar_id";
    static final String CUENTA_USUARIO = "cuenta_usuario";
    static final String SYNC_TOKEN = "syncToken";
    static final String CALENDAR_PREFERENCES = "calendar_Preferences";

    static final String CLIENT_ID = "idCliente";
    static final String CLIENT_TYPE = "tipoCliente";
    static final String CLIENT_PREFERENCES = "client_Preferences";

    static final String ADMIN = "ADMIN";
    static final String PACIENTE = "PACIENTE";
    static final String FAMILIAR = "FAMILIAR";

    public static int getpixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static boolean isNetworkActive(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static Date getRelativeDate(int field, int amount) {
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(now);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static double getJulianDay(long time) {
        return DateTimeUtils.toJulianDay(time);
    }


    public static Date stringToDate(String fecha) {
        Date date = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            date = df.parse(fecha);
        } catch (ParseException e) {
            try {
                date = df.parse(fecha + " 00:00");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return date;
    }

    public static Calendar stringToCalendar(String fecha) {
        Date date = null;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            date = df.parse(fecha);
        } catch (ParseException e) {
            try {
                date = df.parse(fecha + " 00:00");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        calendar.setTime(date);
        return calendar;
    }

  /*public static String getAvisoCita(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String avisoCita = sharedPref.getString("rCitasT","h")+sharedPref.getString("rCitasV","1");
        return avisoCita;
    }

    public static String getAvisoMedicamento(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String avisoMed = sharedPref.getString("rMedicamentosT","h")+sharedPref.getString("rMedicamentosV","1");
        return avisoMed;
    }

    public static void setAvisoCita(String avisoCita,SharedPreferences.Editor editor){
        editor.putString("rCitasT",String.valueOf(avisoCita.charAt(0)));
        editor.putString("rCitasV",String.valueOf(avisoCita.substring(1)));

    }

    public static void setAvisoMedicamento(String avisoMedicamento,SharedPreferences.Editor editor){
        editor.putString("rMedicamentosT",String.valueOf(avisoMedicamento.charAt(0)));
        editor.putString("rMedicamentosV",String.valueOf(avisoMedicamento.substring(1)));

    }
*/
}
