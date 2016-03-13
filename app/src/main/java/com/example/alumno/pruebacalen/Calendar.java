package com.example.alumno.pruebacalen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import org.joda.time.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by jon on 7/03/16.
 */
public class Calendar extends Activity implements View.OnClickListener {

    private java.util.Calendar cal;
    private ExtendedCalendarView extendedCalendarView;
    private int startYear, startMonth, startDay, startHour, startMinute;
    private int stopYear, stopMonth, stopDay, stopHour, stopMinute;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        init();
    }

    protected void setTime(String fechaInicio, String fechaFinal) {
        String[] inicio=fechaInicio.split("-| |:");
        String[] fin=fechaFinal.split("-| |:");

        this.startYear = Integer.parseInt(inicio[0]);
        this.startMonth = Integer.parseInt(inicio[1]);
        this.startDay = Integer.parseInt(inicio[2]);
        this.startHour = Integer.parseInt(inicio[3]);
        this.startMinute = Integer.parseInt(inicio[4]);

        //Log.d("asd","El inicio es "+startYear+"-"+startMonth+"-"+startDay+" "+startHour+":"+startMinute);

        this.stopYear = Integer.parseInt(fin[0]);
        this.stopMonth = Integer.parseInt(fin[1]);
        this.stopDay = Integer.parseInt(fin[2]);
        this.stopHour = Integer.parseInt(fin[3]);
        this.stopMinute = Integer.parseInt(fin[4]);
        //Log.d("asd","El final es "+stopYear+"-"+stopMonth+"-"+stopDay+" "+stopHour+":"+stopMinute);

    }

    protected void init() {
        restartTime();
        this.cal = java.util.Calendar.getInstance();
        this.extendedCalendarView = new ExtendedCalendarView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        extendedCalendarView.setLayoutParams(params);

        FloatingActionButton floatingActionButton = new FloatingActionButton(this);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = getpixels(extendedCalendarView.getDps() - 10);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        floatingActionButton.setLayoutParams(params);
        floatingActionButton.setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            floatingActionButton.setImageDrawable(this.getResources().getDrawable(R.drawable.plus, this.getTheme()));
        } else {
            floatingActionButton.setImageDrawable(this.getResources().getDrawable(R.drawable.plus));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            floatingActionButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.blue,this.getTheme()));
        }else {
            floatingActionButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.blue));
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent("com.example.alumno.pruebacalen.NUEVOEVENTO"), 1);
            }
        });
        extendedCalendarView.addView(floatingActionButton);

        ((RelativeLayout) findViewById(R.id.base)).addView(extendedCalendarView);


        ExtendedCalendarView.OnDayClickListener days = new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
                refreshEvents(day, position);
            }
        };
        extendedCalendarView.setOnDayClickListener(days);

       // new TodayEventsTask(extendedCalendarView).execute(); //TODO ESTO NO TIRA MUY BIEN PERO FUNCIONA

    }

    /*public class TodayEventsTask extends AsyncTask<Void,Void,Boolean> {

        ExtendedCalendarView cal;;
        TodayEventsTask(ExtendedCalendarView view)
        {
            this.cal=view;
        }

        @Override

        protected Boolean doInBackground(Void... params) {

            while (cal.getTodayPosition() == -1){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            refreshEvents(cal.getTodayDay(),cal.getTodayPosition());
        }
    }*/

    public void onActivityResult(int requestCode, int resultCode, Intent recogerDatos){
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                String nombre=recogerDatos.getStringExtra("nombre");
                String fechaInicio=recogerDatos.getStringExtra("fechaInicio");
                String fechaFinal=recogerDatos.getStringExtra("fechaFinal");
                String lugar=recogerDatos.getStringExtra("lugar");
                String descripcion=recogerDatos.getStringExtra("descripcion");

                setTime(fechaInicio, fechaFinal);
                addDate(nombre,descripcion,lugar);
            }
        }
    }


    protected void refreshEvents(Day day, int position) {

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Tagger tagger=new Tagger(position);
        ArrayList<Event> array = day.getEvents();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.scroll);
        linearLayout.removeAllViews();
        TextView textView;
        for (com.tyczj.extendedcalendarview.Event even : array) {
            textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setText(even.getTitle() + ": " + even.getDescription() + "\n\tFECHA\n\t\t" + even.getStartDate("yyyy-MM-dd HH:mm") + " A " + even.getEndDate("yyyy-MM-dd HH:mm")); //TODO CAMBIAR COMO SE VISUALIZAN LOS EVENTOS
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

    private class Tagger{ //TODO APAÑO PARA PODER REFRESCAR LOS EVENTOS
        private int position;
        private Event event;

        public Tagger(int position)
        {
            this.position=position;
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

    protected int getpixels(int dp) {
        float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected void restartTime() {
        this.startYear = -1;
        this.startMonth = -1;
        this.startDay = -1;
        this.startHour = -1;
        this.startMinute = -1;

        this.stopYear = -1;
        this.stopMonth = -1;
        this.stopDay = -1;
        this.stopHour = -1;
        this.stopMinute = -1;
    }

    protected void addDate(String eventname, String description, String location) {
        try {
            if (startYear == -1)
                throw new Exception("No has indicado los valores de las horas");

            cal.set(startYear, startMonth, startDay, startHour, startMinute);
            long time = cal.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put(CalendarProvider.COLOR, Event.COLOR_RED);
            values.put(CalendarProvider.DESCRIPTION, description);
            values.put(CalendarProvider.LOCATION, location);
            values.put(CalendarProvider.EVENT, eventname);
            values.put(CalendarProvider.START, time);
            values.put(CalendarProvider.START_DAY, getJulianDay(time));

            cal.set(stopYear, stopMonth, stopDay, stopHour, stopMinute);
            time = cal.getTimeInMillis();
            values.put(CalendarProvider.END, time);
            values.put(CalendarProvider.END_DAY, getJulianDay(time));

            this.getContentResolver().insert(CalendarProvider.CONTENT_URI, values);

            restartTime();
            extendedCalendarView.refreshCalendar();

        } catch (Exception ex) {
            Log.d("asd", "ERROR;" + ex.getMessage());
        }

    }

    protected long getJulianDay(long time) {
        return DateTimeUtils.toJulianDayNumber(time);

        //return Time.getJulianDay(time, TimeUnit.MILLISECONDS.toSeconds(timeZone.getOffset(time)));
    }

    protected void deleteDate(long id) {
        getContentResolver().delete(CalendarProvider.CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
        //getContentResolver().delete(CalendarProvider.CONTENT_URI, "event=? AND description =?", new String[]{"prueba", "asda"});
    }

   /* protected void deleteAll() {
        this.getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
    }*/

    @Override
    public void onClick(View v) {
        final Tagger tagger=(Tagger)v.getTag();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(tagger.getEvent().getTitle());
        alert.setMessage("FECHA\n" + " De " + tagger.getEvent().getStartDate("yyyy-MM-dd HH:mm") + "\n A " + tagger.getEvent().getEndDate("yyyy-MM-dd HH:mm") + "\n\nDESCRIPCION\n" + tagger.getEvent().getDescription() + "\n\nLUGAR\n" + tagger.getEvent().getLocation());
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDate(tagger.getEvent().getEventId());
                Day day=extendedCalendarView.getDayAfterEventDelete(tagger.getPosition()); //TODO REFRESCA LOS DIAS SIN BORRAR TODA LA PANTALLA
                refreshEvents(day,tagger.getPosition());
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
