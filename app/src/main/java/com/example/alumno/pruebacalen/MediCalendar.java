package com.example.alumno.pruebacalen;

import android.content.ContentValues;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by jon on 7/03/16.
 */
public class MediCalendar {

    private Calendar cal;
    //private TimeZone timeZone;
    private Context context;
    private int startYear, startMonth, startDay, startHour, startMinute;
    private int stopYear, stopMonth, stopDay, stopHour, stopMinute;

    public MediCalendar(Context context)
    {
        this.cal=Calendar.getInstance();
      //  this.timeZone=TimeZone.getDefault();
        this.context=context;
        restartTime();
    }

    public void setTime(int startYear, int startMonth, int startDay, int startHour, int startMinute, int stopYear, int stopMonth, int stopDay, int stopHour, int stopMinute)
    {
        this.startYear=startYear;
        this.startMonth=startMonth;
        this.startDay=startDay;
        this.startHour=startHour;
        this.startMinute=startMinute;

        this.stopYear=stopYear;
        this.stopMonth=stopMonth;
        this.stopDay=stopDay;
        this.stopHour=stopHour;
        this.stopMinute=stopMinute;
    }

    public void restartTime()
    {
        this.startYear=-1;
        this.startMonth=-1;
        this.startDay=-1;
        this.startHour=-1;
        this.startMinute=-1;

        this.stopYear=-1;
        this.stopMonth=-1;
        this.stopDay=-1;
        this.stopHour=-1;
        this.stopMinute=-1;
    }

    public void addDate(ExtendedCalendarView calendarView, String eventname, String description, String location)
    {
        try {
            if(startYear==-1)
                throw new Exception("No has indicado los valores de las horas");

            cal.set(startYear, startMonth - 1, startDay, startHour, startMinute);
            long time = cal.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put(CalendarProvider.COLOR, Event.COLOR_RED);
            values.put(CalendarProvider.DESCRIPTION, description);
            values.put(CalendarProvider.LOCATION, location);
            values.put(CalendarProvider.EVENT, eventname);
            values.put(CalendarProvider.START, time);
            values.put(CalendarProvider.START_DAY, getJulianDay(time));

            cal.set(stopYear, stopMonth - 1, stopDay, stopHour, stopMinute);
            time = cal.getTimeInMillis();
            values.put(CalendarProvider.END, time);
            values.put(CalendarProvider.END_DAY, getJulianDay(time));

            context.getContentResolver().insert(CalendarProvider.CONTENT_URI, values);

            restartTime();
            calendarView.refreshCalendar();

        }
        catch (Exception ex)
        {
            Log.d("asd", "ERROR;"+ex.getMessage());
        }

    }

    private int getJulianDay(long time)
    {
        GregorianCalendar date=(GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        date.setTime(new Date(time));
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND,0);

        return date.getActualMaximum(1);//ESTO FALLA
        //return Time.getJulianDay(time, TimeUnit.MILLISECONDS.toSeconds(timeZone.getOffset(time)));
    }

    public void deleteDate(int id)
    {
        context.getContentResolver().delete(CalendarProvider.CONTENT_URI, "_id=?", new String[] {String.valueOf(id)});
        //getContentResolver().delete(CalendarProvider.CONTENT_URI, "event=? AND description =?", new String[]{"prueba", "asda"});
    }

    public void deleteAll(){
        context.getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
    }

}
