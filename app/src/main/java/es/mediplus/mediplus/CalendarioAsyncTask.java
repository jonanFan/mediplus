package es.mediplus.mediplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.tyczj.extendedcalendarview.CalendarProvider;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarioAsyncTask extends AsyncTask<Void, Void, Void> implements Serializable{

    private GoogleAccountCredential credential;
    private Calendar services;

    private Activity context;
    private CalendarioVista vista = null;
    private CalendarioLogica calendarioLogica;

    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    CalendarioAsyncTask(Activity activity, GoogleAccountCredential credential) {
        this.context = activity;
        this.credential = credential;
        calendarioLogica = new CalendarioLogica(this.context);
    }

    CalendarioAsyncTask(CalendarioVista activity, GoogleAccountCredential credential) {
        this.context = activity;
        this.vista = activity;
        this.credential = credential;
        calendarioLogica = new CalendarioLogica(this.context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        services = new Calendar.Builder(transport, jsonFactory, credential).setApplicationName("Mediplus").build();
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        String calendarId;
        Log.d("asd", "Empiezo con el thread del calendario");

        if (Utils.isNetworkActive(context)) {
            SharedPreferences settings = context.getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
            calendarId = settings.getString(Utils.CALENDAR_ID, null);
            if (calendarId == null || !calendarExists(calendarId)) {
                Log.d("asd", "No lo tenia guardado o el calendario ya no existe");
                calendarId = getCalendarList();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Utils.CALENDAR_ID, calendarId);
                editor.remove(Utils.SYNC_TOKEN);
                editor.apply();
                uploadEventChanges(calendarId, true);

            } else {
                Log.d("asd", "Lo tenia guardado y existe");
                uploadEventChanges(calendarId, false);
            }
            downloadEventChanges(calendarId);
        } else
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return null;
    }


    private void uploadEventChanges(String calendarId, boolean full) {
        deleteEventsRemote(calendarId);
        addEventsRemote(calendarId, full);
    }

    private void addEventsRemote(String calendarId, boolean full) {
        Cursor c;
        if (full) {
            c = context.getContentResolver().query(CalendarProvider.CONTENT_URI, new String[]{CalendarProvider.ID, CalendarProvider.EVENT,
                    CalendarProvider.DESCRIPTION, CalendarProvider.LOCATION, CalendarProvider.START, CalendarProvider.END, CalendarProvider.COLOR, CalendarProvider.ID_GOOGLE}, null, null, null);
        } else {
            c = context.getContentResolver().query(CalendarProvider.CONTENT_URI, new String[]{CalendarProvider.ID, CalendarProvider.EVENT,
                    CalendarProvider.DESCRIPTION, CalendarProvider.LOCATION, CalendarProvider.START, CalendarProvider.END, CalendarProvider.COLOR, CalendarProvider.ID_GOOGLE}, CalendarProvider.ID_GOOGLE + "=?", new String[]{""}, null);
        }

        try {
            if (c != null && c.getCount() != 0 && c.moveToFirst()) {
                do {
                    Log.d("asd", "Añadiendo evento a remoto");

                    Event event = new Event();
                    event.setSummary(c.getString(1));
                    if (!(c.getString(2).equals("")))//Descripcion
                        event.setDescription(c.getString(2));
                    if (!(c.getString(3).equals("")))//Location
                        event.setLocation(c.getString(3));

                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").withLocale(Locale.getDefault());
                    Log.d("asd", "La hora que envio a google es " + dtf.print(c.getLong(4)) + " " + dtf.print(c.getLong(5)));
                    DateTime startDateTime = new DateTime(dtf.print(c.getLong(4)));
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone(TimeZone.getDefault().getID());
                    event.setStart(start);

                    DateTime endDateTime = new DateTime(dtf.print(c.getLong(5)));
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone(TimeZone.getDefault().getID());
                    event.setEnd(end);

                    EventReminder[] reminderOverrides = new EventReminder[]{
                            new EventReminder().setMethod("email").setMinutes(2 * 60),//TODO NOTIFICACIONES DE LOS EVENTOS EN GOOGLE
                          //  new EventReminder().setMethod("popup").setMinutes(60),
                    };
                    Event.Reminders reminders = new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);

                    event = services.events().insert(calendarId, event).execute();
                    calendarioLogica.updateGoogleId(c.getInt(0), event.getId());
                } while (c.moveToNext());
                c.close();
                Log.d("asd", "EVENTO CREADO EN GOOGLE");
            }
        } catch (UserRecoverableAuthIOException e) {
            Log.d("asd", "Entra a pedir autorizacion");
            context.startActivityForResult(e.getIntent(), Utils.REQUEST_AUTHORIZATION);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            addEventsRemote(calendarId, full);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (c != null)
            c.close();
    }


    private void deleteEventsRemote(String calendarId) {
        Cursor c = context.getContentResolver().query(CalendarProvider.CONTENT_URI, new String[]{CalendarProvider.ID, CalendarProvider.ID_GOOGLE, CalendarProvider.FLAG}, CalendarProvider.FLAG + "=?", new String[]{CalendarProvider.ELIMINAR}, null);

        if (c != null && c.getCount() != 0) {
            if (c.moveToFirst()) {
                do {
                    if (!(c.getString(1).equals(""))) {
                        Log.d("asd", "Eliminando evento en google");
                        deleteFromGoogle(calendarId, c.getString(1));
                    } else {
                        Log.d("asd", "Eliminando evento en local");
                        calendarioLogica.deleteDate(c.getInt(0));
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    private void deleteFromGoogle(String calendarId, String eventId) {
        try {
            services.events().delete(calendarId, eventId).execute();
        } catch (UserRecoverableAuthIOException e) {
            Log.d("asd", "Entra a pedir autorizacion");
            context.startActivityForResult(e.getIntent(), Utils.REQUEST_AUTHORIZATION);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            deleteFromGoogle(calendarId, eventId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadEventChanges(String calendarId) {
        Calendar.Events.List eventsList;
        String pageToken = null;
        Events events;
        String syncToken;


        SharedPreferences settings = context.getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        try {
            eventsList = services.events().list(calendarId);
            syncToken = settings.getString(Utils.SYNC_TOKEN, null);

            if (syncToken == null) {
                Log.d("asd", "El token era null asi que hacemos un full sync");
                Date oneYear = Utils.getRelativeDate(java.util.Calendar.YEAR, -1);
                eventsList.setTimeMin(new DateTime(oneYear, TimeZone.getDefault()));
                //  TimeZone.getTimeZone("UTC")));
                //calendarioLogica.deleteAll();  //TODO Esto haria que predominara el calendario remoto de google
            } else {
                Log.d("asd", "Hacemos un incremental sync");
                eventsList.setSyncToken(syncToken);
            }


            do {
                eventsList.setPageToken(pageToken);
                events = eventsList.execute();
                List<Event> eventos = events.getItems();

                if (eventos.size() == 0)
                    Log.d("asd", "No habia ningun evento para sincronizar");
                else {
                    for (Event evento : eventos) {
                        descargaEvento(evento);
                    }
                }

                pageToken = events.getNextPageToken();


            }
            while (pageToken != null);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Utils.SYNC_TOKEN, events.getNextSyncToken());
            editor.apply();
            Log.d("asd", "Sincronizacion completada");

        } catch (UserRecoverableAuthIOException e) {
            Log.d("asd", "Entra a pedir autorizacion");
            context.startActivityForResult(e.getIntent(), Utils.REQUEST_AUTHORIZATION);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            downloadEventChanges(calendarId);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) {
                SharedPreferences.Editor editor = context.getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.remove(Utils.SYNC_TOKEN);
                editor.apply();
                downloadEventChanges(calendarId);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("asd", "He entrado en este fallo y no se por que!!!");
        }

    }

    private void descargaEvento(Event evento) {
        //TODO ESTE ES EL METODO DONDE TIENES QUE PONER LAS NOTIFICACIONES DE LAS CITAS (SE LLAMA UNA VEZ POR CADA CITA QUE SE HA CAMBIADO)
        //TIENES QUE TRABAJAR CON EL ID DE GOOGLE SIEMPRE
        //String userType=context.getSharedPreferences(Utils.CLIENT_PREFERENCES, Context.MODE_PRIVATE).getString(Utils.CLIENT_TYPE, "null");

        Cursor c = context.getContentResolver().query(CalendarProvider.CONTENT_URI, new String[]{CalendarProvider.ID, CalendarProvider.ID_GOOGLE}, CalendarProvider.ID_GOOGLE + "=?", new String[]{evento.getId()}, null);

        if (evento.getStatus().equals("cancelled")) {
            Log.d("asd", "ELIMINAR EVENTO");
            Cita cita = new Cita(evento.getId());
            //TODO iñigo aqui va eliminar cita (me falta por crearla todavia)
            ReminderManager.cancelReminder(context, cita);
            calendarioLogica.deleteDate(evento.getId());    //este es el id de google
        } else {
            Log.d("asd", "evento sincronizado: " + evento.getSummary() + " " + evento.getStatus() + " " + evento.getRecurringEventId() + " " + evento.getRecurrence());
            if (evento.getRecurrence() == null) {
                Cita cita=new Cita((evento.getSummary() == null) ? "Sin titulo" : evento.getSummary(), (evento.getDescription() == null) ? "" : evento.getDescription(), (evento.getLocation() == null) ? "" : evento.getLocation(), evento.getId());
                if (evento.getStart().getDateTime() != null) {
                    cita.setTime(evento.getStart().getDateTime().toString(), evento.getEnd().getDateTime().toString(), true, false);
                } else {
                    cita.setTime(((evento.getStart().getDate().toString()) + "T00:00"), ((evento.getEnd().getDate().toString()) + "T23:59"), true, true);
                }

                if (c == null || c.getCount() == 0) {

                    Log.d("asd", "NUEVO EVENTO: AÑADIENDO");

                    //TODO iñigo aqui va añadir cita
                    ReminderManager.setReminder(context, cita);
                    calendarioLogica.addDate(cita);

                } else {
                    if (c.moveToFirst()) {
                        Log.d("asd", "EVENTO MODIFICADO");

                        //TODO iñigo aqui va modificar cita
                        cita.setIdLocal(c.getInt(0));
                        ReminderManager.updateReminder(context, cita);
                        calendarioLogica.updateDateFromGoogle(cita);
                    } else
                        Log.d("asd", "Algo ha fallado al modificar el evento");
                }
            } else {
                Log.d("asd", "ERROR GOOGLE: Me ha llegado un evento recurrente.. Voy a pasar de el");
            }
            if (c != null) {
                c.close();
            }
        }
    }

    protected boolean calendarExists(String calendarId) {
        boolean exists;
        try {
            CalendarListEntry calendarListEntry = services.calendarList().get(calendarId).execute();
            exists = calendarListEntry != null;
        } catch (UserRecoverableAuthIOException e) {
            Log.d("asd", "Entra a pedir autorizacion");
            context.startActivityForResult(e.getIntent(), Utils.REQUEST_AUTHORIZATION);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            exists = calendarExists(calendarId);
        } catch (IOException e) {
            e.printStackTrace();
            exists = false;
        }
        return exists;
    }

    private String getCalendarList() {
        // Iterate through entries in calendar list
        String calendarId = null;
        String pageToken = null;
        boolean exists = false;
        boolean retry = true;
        CalendarList calendarList;

        do {
            try {
                calendarList = services.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();
                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals(Utils.CALENDAR_NAME)) {
                        calendarId = calendarListEntry.getId();
                        exists = true;
                        break;
                    }
                }
                retry = false;
                pageToken = calendarList.getNextPageToken();

            } catch (UserRecoverableAuthIOException e) {
                Log.d("asd", "Entra a pedir autorizacion");
                context.startActivityForResult(e.getIntent(), Utils.REQUEST_AUTHORIZATION);

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (pageToken != null && !exists || retry);

        //Añadir calendario
        if (!exists) {
            Log.d("asd", "creando calendario");
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary(Utils.CALENDAR_NAME);
            calendar.setTimeZone(TimeZone.getDefault().getID());
            try {
                calendarId = (services.calendars().insert(calendar).execute()).getId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Log.d("asd", "estaba creado");

        return calendarId;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d("asd", "Thread saliendo");
        super.onPostExecute(aVoid);
        if (vista != null)
            vista.refrescarVista();
        Log.d("asd", "Thread muerto");
    }
}
