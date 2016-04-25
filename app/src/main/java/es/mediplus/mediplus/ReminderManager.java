
package es.mediplus.mediplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by inigo on 8/04/16.
 */


/*
Nota: para manejar los reminders que he pospuesto, utilizo la siguiente logica:

los reminders de cada medicamento son independientes. los de cada cita tambien.

cada reminder tiene un id (int) diferente, interno a reminderManager. se diferencian dos intervalos:
id [0, reminderRepetitions): reservados para posposiciones de una notificacion
id [reminderRepetitions, infinito): para uso de las notificaciones programadas normalmente

cuando se postpone una alarma, se añade al intent un extra indicando cuántas veces se ha pospuesto dicha alarma
el valor de este extra será siempre de id+1. cuando el contenido esté vacío (null) se asumirá un valor 0 (no se
ha pospuesto la alarma). cuando el valor sea reminderRepetitions, hemos llegado al límite, hay que llamar al jefe
 */
public class ReminderManager {

    /* ----------------------------    FOR LOGGING    ---------------------------- */
    private static final String TAG ="inigo: ReminderManager";

    /* ----------------------------    CONSTANTES    ---------------------------- */

    // creo mis propio Action para los intent de medicina y citas
    public static final String ACTION_REMIND_MED = BuildConfig.APPLICATION_ID +".action.REMIND_MED";
    public static final String ACTION_REMIND_CITE = BuildConfig.APPLICATION_ID +".action.REMIND_CITE";

    //creo los nombres (con prefijo) para los Extras de los intents
    public static final String EXTRA_MED = BuildConfig.APPLICATION_ID+".intent.EXTRA_MED";
    public static final String EXTRA_CITE = BuildConfig.APPLICATION_ID+".intent.EXTRA_CITE";
    public static final String EXTRA_VIBRATION = BuildConfig.APPLICATION_ID+".intent.EXTRA_VIBRATION";
    public static final String EXTRA_POSTPONE_TIMES= BuildConfig.APPLICATION_ID+"intent.EXTRA_POSTPONE_TIMES";

    /* ----------------------------    CONSTRUCTOR    ---------------------------- */

    // le hago un constructor pasándole un contexto, por si se quiere coger las preferencias
    public ReminderManager (Context context) {
       importPreferences(context);
    }

    // lo hago privado para que no me puedan instanciar la clase
    private ReminderManager () {    }

    /* ----------------------------    MEMBERS    ---------------------------- */

    // vibracion activada por defecto
    private static boolean vibrationActivated = true;

    // tiempo de antelacion con el que se avisa para medicamentos. default 15 mins
    //private static long medPrevisionTimeMilis = 1000 * 60 * 15; //TODO: inigo: ponerlo a 15 mins:descomentar esta linea
    private static long medPrevisionTimeMilis = 0; //ahora está a 0 para que vayamos haciendo pruebas

    // tiempo de antelacion con el que se avisa para citas. por defecto 30 mins
    private static long citePrevisionTimeMilis = 1000 * 60 * 30;

    // tiempo a esperar para volver a notificar si se pospone la alarma. default 5 mins. //TODO: inigo: ponerlo a 5 mins
    //private static int postponeTimeMilis = 1000 * 50 * 5;
    private static long postponeTimeMilis = 1000 * 10; //ahora estamos en 10 segundos

    // si es un paciente, hay que activar las alarmas!
    private static boolean isPaciente = false;

    // numero de veces que el user puede darle a posponer antes de avisar como si no se la tomara
    private static int reminderRepetitions = 3;

    private static int latestPostponedMedId;
    private static String latestPostponedCiteId;

    private static String numTfnoJefe;

    // el alarmManager y pending intent que utilizare
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    /* ----------------------------    METHODS: MEDICAMENTOS    ---------------------------- */

    //overload
    public static void setReminder(Context con, Medicamento medicamento) {
        setReminder(con, medicamento,medPrevisionTimeMilis,vibrationActivated);
    }
    //overload
    public static void setReminder(Context con, Medicamento medicamento, long prevTime) {
        setReminder(con, medicamento, prevTime, vibrationActivated);
    }
    //overload
    public static void setReminder(Context con, Medicamento medicamento, boolean vibrate) {
        setReminder(con, medicamento, medPrevisionTimeMilis, vibrate);
    }

    //esta si que trabaja
    public static void setReminder(Context context, Medicamento medicamento, long prevTime, boolean vibrate) {//TODO REVISAR

        importPreferences(context);

        if (isPaciente) {       // si no es paciente no hay nada que hacer chato!
            if (mustActivate(medicamento)) {        // Si estoy entre las fechas límite...

                alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = createIntent(context, medicamento);
                intent.putExtra(EXTRA_VIBRATION, vibrate);

                ArrayList<Long> delayTimes = getTriggerDelays(medicamento); //obtengo los delays para las alarmas

                if (delayTimes.isEmpty())
                    Log.d(TAG, "setReminder: ojo!!! que el delayTimes esta vacio! no se programaran recordatorios");
                // TODO: inigo: remove de aqui al for para debugging
                String medLog = "setReminder(" + medicamento.getNombre() + "): se programan ";
                int numAlarm = delayTimes.size();
                medicamento.setNumAlarmas(numAlarm);
                medLog += Integer.toString(numAlarm);
                medLog += " alarmas";
                Log.d(TAG, medLog);

                Calendar calendarNow = Calendar.getInstance();
                calendarNow.setTimeInMillis(System.currentTimeMillis());

                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMMM-dd || HH:mm:ss");

                numAlarm = reminderRepetitions;     //se van a reservar desde reminderRepetitions hasta 0 para las posposiciones

                for (long delay : delayTimes) {     //para cada alarma necesaria asigno una alarma

                    alarmIntent = PendingIntent.getActivity(context, numAlarm, intent, 0);
                    numAlarm++;

                    if (Build.VERSION.SDK_INT >= 19) {
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + delay - prevTime, alarmIntent);
                        //TODO: remove these lines for debugging
                        Log.d(TAG, "setReminder(" + medicamento.getNombre() + "): alarma en " + fmt.format(calendarNow.getTimeInMillis() + delay - prevTime));
                    } else {
                        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + delay - prevTime, alarmIntent);
                    }
                }
            }
        }
    }

    public static void postponeReminder (Context context, Medicamento medicamento, long postTimeMilis) {
        //le iba a pasar tambien el booleano con la vibracion pero que se joda y le vibre si no se la quiere tomar

		if (latestPostponedMedId==0)
			latestPostponedMedId=medicamento.getIdLocal();

        if (medicamento.getIdLocal()!=latestPostponedMedId ) {

            latestPostponedMedId = medicamento.getIdLocal();

            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = createIntent(context, medicamento);
            intent.putExtra(EXTRA_VIBRATION, true);


            Calendar calendarNow = Calendar.getInstance();
            calendarNow.setTimeInMillis(System.currentTimeMillis());

            for (int i = 0; i < getReminderRepetitions(); i++) {    //los dejo ya todos programados desde aqui, sera la activity la que cancele
                intent.putExtra(EXTRA_POSTPONE_TIMES, i + 1);
                alarmIntent = PendingIntent.getActivity(context, i, intent, 0);     //le resto uno para que empiece en 0

                if (Build.VERSION.SDK_INT >= 19) {
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + (1 + i) * postTimeMilis, alarmIntent);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + (1 + i) * postTimeMilis, alarmIntent);
                }
            }
        }
    }

    // Cancela los reminders pendientes para medicamento (los postpone NO)
    public static void cancelReminder (Context con, Medicamento medicamento){
        //TODO ALGO ESTA MAL, PORQUE EL CANCEL DEBERIA BORRAR TODO, LAS POSTPONED INCLUIDAS
        //TODO HACE ALGO RARO

        if(isPaciente) {
            alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

            // no necesito añadirle los extras para cancelarlo
            Intent intent = createIntent(con, medicamento);

            for (int i = getReminderRepetitions(); i < medicamento.getNumAlarmas() + getReminderRepetitions(); i++) {
                alarmIntent = PendingIntent.getActivity(con, i, intent, 0);
                if (alarmMgr != null) {
                    alarmMgr.cancel(alarmIntent);
                }
            }
            //TODO: inigo: remove this log if it works
            Log.d(TAG, "cancelReminder(" + medicamento.getNombre() + "): " + Integer.toString(medicamento.getNumAlarmas()) + " instancias");
        }
    }

    // Cancela los reminders que he puesto al postponer
    public static void cancelPostponed (Context con, Medicamento medicamento){

        alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

        // no necesito añadirle los extras para cancelarlo
        Intent intent = createIntent (con, medicamento);

        for (int i=0; i< getReminderRepetitions(); i++) {
            alarmIntent = PendingIntent.getActivity(con, i, intent, 0);
            if (alarmMgr!= null) {
                alarmMgr.cancel(alarmIntent);
            }
        }
    }

    //cutrillo, cancela todos y luego los vuelve a poner
    public static void updateReminder (Context con, Medicamento medicamento) {
        cancelReminder(con, medicamento);
        setReminder(con, medicamento);
    }

    /* ----------------------------    METHODS: CITAS    ---------------------------- */

    //overload
    public static void setReminder (Context con, Cita cita){
        setReminder(con, cita, citePrevisionTimeMilis, vibrationActivated);
    }
    //overload
    public static void setReminder (Context con, Cita cita, long prevTimeMilis ){
        setReminder(con, cita, prevTimeMilis, vibrationActivated);
    }
    //overload
    public static void setReminder (Context con, Cita cita, boolean vibrate) {
        setReminder(con, cita, citePrevisionTimeMilis, vibrate);
    }

    // esta si que trabaja
    public static void setReminder(Context con, Cita cita, long prevTimeMilis, boolean vibrate) {//TODO REVISAR

        //TODO: reminderCita || comprobar que funciona
        importPreferences(con);

        if (isPaciente) {       // si no es paciente no hay nada que hacer chato!
            if (mustActivate(cita)) {        // Si estoy entre las fechas límite...

                alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
                Intent intent = createIntent(con, cita);
                intent.putExtra(EXTRA_VIBRATION, vibrate);

                // TODO: inigo: remove de aqui al for para debugging
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMMM-dd || HH:mm:ss");
                String medLog = "setReminder(" + cita.getEventName() + "): se programa alarma a las";

                Calendar calendarNow = Calendar.getInstance();
                calendarNow.setTimeInMillis(System.currentTimeMillis());

                Calendar calendarCiteBegin = Calendar.getInstance();
                calendarCiteBegin.set(Calendar.YEAR, cita.getStartYear());
                calendarCiteBegin.set(Calendar.MONTH, cita.getStartMonth());
                calendarCiteBegin.set(Calendar.DAY_OF_MONTH, cita.getStartYear());       //TODO: cual de todos los day es?
                calendarCiteBegin.set(Calendar.HOUR_OF_DAY, cita.getStartHour());
                calendarCiteBegin.set(Calendar.MINUTE, cita.getStartMinute());

                Date scheduleTime = new Date(calendarCiteBegin.getTimeInMillis() - prevTimeMilis);

                alarmIntent = PendingIntent.getActivity(con, reminderRepetitions, intent, 0); //solo programo una alarma, con id reminderRepetitions

                if (Build.VERSION.SDK_INT >= 19) {
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, scheduleTime.getTime(), alarmIntent);
                    medLog += fmt.format(scheduleTime);
                    Log.d(TAG, medLog);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, scheduleTime.getTime(), alarmIntent);
                    medLog += fmt.format(scheduleTime);
                    Log.d(TAG, medLog);
                }
            }
        }
    }

    //TODO: creo que estan bien ya todos los methods, pero comprobar por si acaso
    public static void postponeReminder (Context context, Cita cita, long postTimeMilis) {
        //le iba a pasar tambien el booleano con la vibracion pero que se joda y le vibre si no se la quiere tomar

		if (latestPostponedCiteId==null)
			latestPostponedCiteId=cita.getIdGoogle();

        if (!cita.getIdGoogle().equalsIgnoreCase(latestPostponedCiteId)) {

            latestPostponedCiteId=cita.getIdGoogle();

            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = createIntent(context, cita);
            intent.putExtra(EXTRA_VIBRATION, true);

            Calendar calendarNow = Calendar.getInstance();
            calendarNow.setTimeInMillis(System.currentTimeMillis());

            for (int i = 0; i < getReminderRepetitions(); i++) {    //los dejo ya todos programados desde aqui, sera la activity la que cancele
                intent.putExtra(EXTRA_POSTPONE_TIMES, i + 1);
                alarmIntent = PendingIntent.getActivity(context, i, intent, 0);     //le resto uno para que empiece en 0
                if (Build.VERSION.SDK_INT >= 19) {
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + (1 + i) * postTimeMilis, alarmIntent);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendarNow.getTimeInMillis() + (1 + i) * postTimeMilis, alarmIntent);
                }
            }
        }
    }

    // Cancela los reminders pendientes para medicamento (los postpone NO)
    public static void cancelReminder(Context con, Cita cita) {//TODO MAS DE LO MISMO QUE EL ANTERIOR

        if(isPaciente) {
            alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

            // no necesito añadirle los extras para cancelarlo
            Intent intent = createIntent(con, cita);

            alarmIntent = PendingIntent.getActivity(con, reminderRepetitions, intent, 0);   //solo cancelo la alarma con id=reminderRepetitions
            if (alarmMgr != null) {
                alarmMgr.cancel(alarmIntent);
            }
            //TODO: inigo: remove this log if it works
            Log.d(TAG, "cancelReminder(" + cita.getEventName() + ")");
        }
    }

    // Cancela los reminders que he puesto al postponer
    public static void cancelPostponed (Context con, Cita cita){

        alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

        // no necesito añadirle los extras para cancelarlo
        Intent intent = createIntent (con, cita);

        for (int i=0; i< getReminderRepetitions(); i++) {
            alarmIntent = PendingIntent.getActivity(con, i, intent, 0);
            if (alarmMgr!= null) {
                alarmMgr.cancel(alarmIntent);
            }
        }
    }

    //cutrillo, cancela todos y luego los vuelve a poner
    public static void updateReminder (Context con, Cita cita) {
        cancelReminder(con, cita);
        setReminder(con, cita);
    }

    /* ----------------------------    UTILITIES    ---------------------------- */

    public static void setRemindersOnBoot(Context con) {

        Log.d(TAG, "setRemindersOnBoot: me llaman bien");

        if (isPaciente) {

            BaseLocal baseLocal = new BaseLocal(con);
            ArrayList<Medicamento> medicamentoArrayList = baseLocal.obtenerMedis();

            for (Medicamento m : medicamentoArrayList) {
                setReminder(con, m);
            }

            //TODO: inigo: añadir la parte de citas

        }
    }

    //TODO: llamar jefe: mirar si tira
    public static void llamarAlJefe(Context context) {
        Intent intent = new Intent(android.content.Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+numTfnoJefe));
        context.startActivity(intent);
    }

    public static void importPreferences (Context context) {

        numTfnoJefe = PreferenceManager.getDefaultSharedPreferences(context).getString("numTfn",null);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String userType=context.getSharedPreferences(Utils.CLIENT_PREFERENCES, Context.MODE_PRIVATE).getString(Utils.CLIENT_TYPE, "null");
        String citaType = sharedPref.getString("rCitasT", "h");
        String citaValue= sharedPref.getString("rCitasV", "1");
        String medType = sharedPref.getString("rMedicamentosT","h");
        String medValue= sharedPref.getString("rMedicamentosV","1");

        long citeMultiplier=1000*60*60, medMultiplier=1000*60*60;

        if (citaType.equalsIgnoreCase("d"))
            citeMultiplier=1000*60*60*24;
        else if (citaType.equalsIgnoreCase("m"))
            citeMultiplier=1000*60;

        if (medType.equalsIgnoreCase("d"))
            medMultiplier=1000*60*60*24;
        else if (medType.equalsIgnoreCase("m"))
            medMultiplier=1000*60;
        if (userType.equalsIgnoreCase(Utils.PACIENTE)) {
            isPaciente = true;
            Log.d(TAG,"Se entra como paciente");
        }
        citePrevisionTimeMilis = citeMultiplier*Integer.parseInt(citaValue);
        medPrevisionTimeMilis = medMultiplier*Integer.parseInt(medValue);
    }

    /* ----------------------------    INTERNAL TOOLS    ---------------------------- */

    private static Intent createIntent (Context context, Medicamento medicamento){
        Intent intent = new Intent();
        intent.setClass(context, ReminderMedActivity.class)
                .setType(String.valueOf(medicamento.getIdLocal()))
                .setAction(ACTION_REMIND_MED)
                .putExtra(EXTRA_MED, medicamento);
        return intent;
    }

    private static Intent createIntent (Context context, Cita cita){
        Intent intent = new Intent();
        intent.setClass(context, ReminderCiteActivity.class)
                .setType(String.valueOf(cita.getIdGoogle()))
                .setAction(ACTION_REMIND_CITE);
        return intent;
    }

    private static boolean mustActivate (Medicamento medicamento){//TODO ACTIVA LA NOTIFICACION SI ESTAMOS ANTES O DURANTE TRATAMIENTO, COMPROBAR QUE EL COMPARETO LO HE PUESTO BIEN

        //Date nowDate = new Date ();
        //Date medInitDate = Utils.stringToDate(medicamento.getFechaInicial()+" "+medicamento.getHoraInicial());
        // Date medEndDate = Utils.stringToDate(medicamento.getFechaFinal()+" "+medicamento.getHoraInicial());

        Calendar nowDate = Calendar.getInstance();
        Calendar medInitDate = Utils.stringToCalendar(medicamento.getFechaInicial()+" "+medicamento.getHoraInicial());
        Calendar medEndDate=Utils.stringToCalendar(medicamento.getFechaFinal()+" "+medicamento.getHoraInicial()); //TODO PREGUNTAR ESTO PERO YO CREO QUE ES ASI
        medEndDate.add(Calendar.DAY_OF_MONTH,1);

        if ( (nowDate.compareTo(medInitDate) >= 0 && nowDate.before(medEndDate)) || nowDate.before(medInitDate) ) {
            //estoy dentro de los límites de la alarma: en mitad de tratamiento |o| antes de empezarlo
            Log.d(TAG, "mustActivate: hay que activar alarmas para el medicamento "+medicamento.getNombre());
            return true;
        }
        else {
            Log.d(TAG, "mustActivate: NO hay que activar alarmas para el medicamento "+medicamento.getNombre());
            return false;
        }
    }

    private static boolean mustActivate (Cita cita){ //TODO ACTIVA LA NOTIFICACION SI ESTAMOS ANTES DE LA CITA, COMPROBAR QUE EL COMPARE TO LO QUE PUESTO BIEN

        Calendar nowDate = Calendar.getInstance();

        Calendar citeInitDate = Calendar.getInstance();
        citeInitDate.set(Calendar.YEAR, cita.getStartYear());
        citeInitDate.set(Calendar.MONTH, cita.getStartMonth());
        citeInitDate.set(Calendar.DAY_OF_MONTH, cita.getStartDay());       //TODO: cual de todos los day es?
        citeInitDate.set(Calendar.HOUR_OF_DAY, cita.getStartHour());
        citeInitDate.set(Calendar.MINUTE, cita.getStartMinute());
        citeInitDate.set(Calendar.SECOND,0);
        citeInitDate.set(Calendar.MILLISECOND,0);

        if (nowDate.compareTo(citeInitDate) <= 0) {
            //estoy antes del inicio de la cita
            Log.d(TAG, "mustActivate: hay que activar alarma para cita="+cita.getEventName());
            return true;
        }
        else {
            Log.d(TAG, "mustActivate: NO hay que activar alarma para cita="+cita.getEventName());
            return false;
        }
    }

    private static void getActivationDays (boolean []store, String days) {

        //TODO: inigo: quitar esto cuando funcione
        int diaspuestos=0;

        if (store.length != 7)
            Log.d(TAG, "getActivationDays: falsch boolean array size");
        else {
            for (int i=0; i!=store.length; i++) {   //recorremos el string
                if (days.charAt(i)!= '-') {         //si el elemento NO es un guión '-'
                    store [i]= true;                //ese día si que tengo que poner alerta
                    //TODO: inigo: quitar esto cuando funcione
                    diaspuestos++;
                }
            }
            Log.d(TAG, "getActivationDays: hay "+Integer.toString(diaspuestos)+" dias activados");
        }
    }

    private static ArrayList<Long> getTriggerDelays (Medicamento medicamento){

        ArrayList<Long> delaysArray= new ArrayList<Long>();
                                //LUN   MAR     MIE    JUE    VIE    SAB    DOM
        boolean [] daysOfWeek = {false, false, false, false, false, false, false};

        getActivationDays(daysOfWeek, medicamento.getFrecuenciaDiaria());

        Calendar medFinalCalendar = Calendar.getInstance();
        medFinalCalendar.setTime(Utils.stringToDate(medicamento.getFechaFinal()));
        medFinalCalendar.set(Calendar.HOUR_OF_DAY, 23);
        medFinalCalendar.set(Calendar.MINUTE, 59);        //lo pongo ese día a las 23:59

        Calendar medInitialCalendar = Calendar.getInstance();
        medInitialCalendar.setTime(Utils.stringToDate(medicamento.getFechaInicial()+" "+medicamento.getHoraInicial()));

        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());

        Calendar operateCalendar= Calendar.getInstance();
        operateCalendar.setTime(medInitialCalendar.getTime());

        int intervalBetweenAlarmsHours = Integer.parseInt(medicamento.getFrecuenciaHoraria());

        Log.d(TAG, "getTriggerDelays: intervaloEnHoras= "+ Integer.toString(intervalBetweenAlarmsHours));

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMMM-dd || HH:mm:ss");
        String fechNow = fmt.format(nowCalendar.getTime());
        String fechIni = fmt.format(medInitialCalendar.getTime());
        String fechFin = fmt.format(medFinalCalendar.getTime());
        Log.d(TAG, "getTriggerDelays:  ahora: "+fechNow);
        Log.d(TAG, "getTriggerDelays: initMed: "+fechIni);
        Log.d(TAG, "getTriggerDelays: finMed: "+fechFin);


        //caso: el instante actual está en mitad del tratamiento. preparo operateCalendar
        if (nowCalendar.after(medInitialCalendar)) {
            Log.d(TAG, "getTriggerDelays: estoy en mitad de tratamiento. entro en el primer if!");
            while (operateCalendar.before(nowCalendar))
                operateCalendar.add(Calendar.HOUR, intervalBetweenAlarmsHours);
        }

        // esto es comun a ambos casos: ya estoy en mitad del tratamiento
        while (operateCalendar.before(medFinalCalendar)) {
            //while (operateCalendar.compareTo(medFinalCalendar) == -1) { // el -1 indica que operateCalendar todavia es Anterior a finalCalendar
            //Log.d(TAG, "getTriggerDelays: estoy ya en el while");
            if (dayOk(operateCalendar, daysOfWeek)) {
                long toAdd = operateCalendar.getTimeInMillis()-nowCalendar.getTimeInMillis();
                delaysArray.add(toAdd); //si el dia es bueno, añado alarma
            }
            operateCalendar.add(Calendar.HOUR, intervalBetweenAlarmsHours); //añado el intervalo en horas y prosigo
        }
        //Log.d(TAG, "getTriggerDelays: al acabar: length (delaysArray)= "+ Integer.toString(delaysArray.size()));
        return  delaysArray;
    }

    private static boolean dayOk (Calendar calendar, boolean [] daysOfWeek) {

        int LUN=0, MAR=1, MIE=2, JUE=3, VIE=4, SAB=5, DOM=6;
        int calDay = calendar.get(Calendar.DAY_OF_WEEK);

        if (daysOfWeek.length!= 7) {
            //Log.d(TAG, "dayOk: falsch boolean array!");
            return false;
        }
        if (calDay == Calendar.MONDAY && daysOfWeek[LUN]){
            //Log.d(TAG, "dayOk: Monday Activated");
            return true;
        }
        if (calDay == Calendar.TUESDAY && daysOfWeek[MAR]){
            //Log.d(TAG, "dayOk: Tuesday Activated");
            return true;
        }
        if (calDay == Calendar.WEDNESDAY && daysOfWeek[MIE]){
            //Log.d(TAG, "dayOk: Wednesday Activated");
            return true;
        }
        if (calDay == Calendar.THURSDAY && daysOfWeek[JUE]){
            //Log.d(TAG, "dayOk: Thursday Activated");
            return true;
        }
        if (calDay == Calendar.FRIDAY && daysOfWeek[VIE]){
            //Log.d(TAG, "dayOk: Friday Activated");
            return true;
        }
        if (calDay == Calendar.SATURDAY && daysOfWeek[SAB]){
            //Log.d(TAG, "dayOk: Saturday Activated");
            return true;
        }
        if (calDay == Calendar.SUNDAY && daysOfWeek[DOM]){
            //Log.d(TAG, "dayOk: Sunday Activated");
            return true;
        }
        return false;
    }

    /* ----------------------------    SETTERS    ---------------------------- */

    // No hay setters. Ha!

    /* ----------------------------    GETTERS    ---------------------------- */

    public static long getPostponeTimeMilis() { return postponeTimeMilis; }

    public static int getReminderRepetitions() {
        return reminderRepetitions;
    }

}
