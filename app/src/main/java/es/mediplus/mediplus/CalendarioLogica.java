package es.mediplus.mediplus;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Event;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jon on 19/03/16.
 */
public class CalendarioLogica implements Serializable {
    //private int startYear, startMonth, startDay, startHour, startMinute;
    //private int stopYear, stopMonth, stopDay, stopHour, stopMinute;
    private Context context;
    private java.util.Calendar cal;


    CalendarioLogica(Context context) {
        this.context = context;
        this.cal = java.util.Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
    }

    protected void addDate(Cita cita) {
        try {
            if (cita.getStartYear() == -1)
                throw new Exception("No has indicado los valores de las horas");

            cal.set(cita.getStartYear(), cita.getStartMonth(), cita.getStartDay(), cita.getStartHour(), cita.getStartMinute(), 0);
            long time = cal.getTimeInMillis();
            // Log.d("Asd","Al añadir los valores son "+startYear+"-"+startMonth+"-"+startDay+" "+startHour+":"+startMinute+" "+stopYear+"-"+stopMonth+"-"+stopDay+" "+stopHour+":"+stopMinute);

            ContentValues values = new ContentValues();
            values.put(CalendarProvider.COLOR, Event.COLOR_RED);
            values.put(CalendarProvider.DESCRIPTION, cita.getDescription());
            values.put(CalendarProvider.LOCATION, cita.getLocation());
            values.put(CalendarProvider.EVENT, cita.getEventName());
            values.put(CalendarProvider.ID_GOOGLE, cita.getIdGoogle());
            values.put(CalendarProvider.FLAG, 0);
            values.put(CalendarProvider.START, time);
            values.put(CalendarProvider.START_DAY, Utils.getJulianDay(time));
            //Log.d("asd", "STARTDAY:el julian day es " + Utils.getJulianDay(time));

            cal.set(cita.getStopYear(), cita.getStopMonth(), cita.getStopDay(), cita.getStopHour(), cita.getStopMinute(), 0);
            time = cal.getTimeInMillis();
            values.put(CalendarProvider.END, time);
            values.put(CalendarProvider.END_DAY, Utils.getJulianDay(time));
            // Log.d("asd", "ENDDAY:el julian day es " + Utils.getJulianDay(time));

            context.getContentResolver().insert(CalendarProvider.CONTENT_URI, values);

            //restartTime();

        } catch (Exception ex) {
            Log.d("asd", "ERROR;" + ex.getMessage());
        }

    }

    protected void deleteDate(long id) {
        context.getContentResolver().delete(CalendarProvider.CONTENT_URI, CalendarProvider.ID + "=?", new String[]{String.valueOf(id)});
        //getContentResolver().delete(CalendarProvider.CONTENT_URI, "event=? AND description =?", new String[]{"prueba", "asda"});
    }

    protected void deleteDate(String idGoogle) {
        context.getContentResolver().delete(CalendarProvider.CONTENT_URI, CalendarProvider.ID_GOOGLE + "=?", new String[]{idGoogle});
        //getContentResolver().delete(CalendarProvider.CONTENT_URI, "event=? AND description =?", new String[]{"prueba", "asda"});
    }

    protected void sendDelete(long id) {
        ContentValues values = new ContentValues();
        values.put(CalendarProvider.FLAG, 1);
        context.getContentResolver().update(CalendarProvider.CONTENT_URI, values, CalendarProvider.ID + "=?", new String[]{String.valueOf(id)});
    }

    protected void updateGoogleId(long id, String idGoogle) {
        ContentValues values = new ContentValues();
        values.put(CalendarProvider.ID_GOOGLE, idGoogle);
        context.getContentResolver().update(CalendarProvider.CONTENT_URI, values, CalendarProvider.ID + "=?", new String[]{String.valueOf(id)});
    }

    protected void updateDateFromGoogle(Cita cita) {

        cal.set(cita.getStartYear(), cita.getStartMonth(), cita.getStartDay(), cita.getStartHour(), cita.getStartMinute(), 0);
        long time = cal.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(CalendarProvider.COLOR, Event.COLOR_RED);
        values.put(CalendarProvider.DESCRIPTION, cita.getDescription());
        values.put(CalendarProvider.LOCATION, cita.getLocation());
        values.put(CalendarProvider.EVENT, cita.getEventName());
        values.put(CalendarProvider.ID_GOOGLE, cita.getIdGoogle());
        values.put(CalendarProvider.START, time);
        values.put(CalendarProvider.START_DAY, Utils.getJulianDay(time));

        cal.set(cita.getStopYear(), cita.getStopMonth(), cita.getStopDay(), cita.getStopHour(), cita.getStopMinute(), 0);
        time = cal.getTimeInMillis();
        values.put(CalendarProvider.END, time);
        values.put(CalendarProvider.END_DAY, Utils.getJulianDay(time));

        context.getContentResolver().update(CalendarProvider.CONTENT_URI, values, CalendarProvider.ID + "=?", new String[]{String.valueOf(cita.getIdLocal())});
        //restartTime();
    }


    protected void deleteAll() {
        context.getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
    }

}
