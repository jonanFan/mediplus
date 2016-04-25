package es.mediplus.mediplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

// TODO: obviamente cambiar los mensajes de los toast, no vamos a llamar a ningún enfermo "putilla"

public class ReminderMedActivity extends Activity implements View.OnClickListener{

    private Medicamento medicamento;

    private boolean vibrate=true;
    private static final long[] vibratePattern = {500, 500};
    private Vibrator vibrator;

    private static AsyncRingtonePlayer asyncRingtonePlayer;

    private Intent receivedIntent;

    private static int postponedTimes;
    private boolean llamadoAlJefe=false;

    /* ----------------------------    FOR LOGGING    ---------------------------- */
    private static final String TAG ="inigo:RemindrMedActy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_reminder);
    }

    // voy a pasar unas cuantas cosas al método onStart para ver si así funciona mejor
    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        /* ----------------------------     WINDOW OVER EVERYTHING    ---------------------------- */

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // Hide navigation bar to minimize accidental tap on Home key
        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        /* ----------------------------     INTENT INFO    ---------------------------- */

        receivedIntent = getIntent();
        Bundle extras = receivedIntent.getExtras();
        if (extras == null) Log.d(TAG, "los extras recibidos en el intent son null");

        /* ----------------------------     POSTPONED TIMES    ---------------------------- */

        postponedTimes=extras.getInt(ReminderManager.EXTRA_POSTPONE_TIMES, 0); //si no viene con nada, le pongo 0
        if (postponedTimes==ReminderManager.getReminderRepetitions()) {
            ReminderManager.llamarAlJefe(this);
        }

        else {

            /* ----------------------------     VIBRATION    ---------------------------- */

            vibrate = extras.getBoolean(ReminderManager.EXTRA_VIBRATION);
            if (vibrate) {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(vibratePattern, 0); //el cero es para reptir indefinidamente
            }
            /* ----------------------------   DISPLAY MEDICAMENTO    ---------------------------- */

            medicamento = (Medicamento) extras.getSerializable(ReminderManager.EXTRA_MED);

            TextView textView = (TextView) findViewById(R.id.activity_reminder_med_info);
            textView.setText(medicamento.getNombre());

            // TODO: elegir que vamos a mostrar para cada medicamento
            /*

            NOTA: si quiero añadir texto en vez de sustituir, uso textView.appendText ("STRING_PARA_APPEND");

            textView = (TextView) findViewById(R.id.activity_reminder_med_second_info);
            textView.setText("ALGUN STRING HABRIA QUE PONER AQUI");

            textView = (TextView) findViewById(R.id.activity_reminder_med_third_info);
            textView.setText("ALGUN STRING HABRIA QUE PONER AQUI");*/


            /* ----------------------------   PLAY RINGTONE    ---------------------------- */

            asyncRingtonePlayer = new AsyncRingtonePlayer(this, "stringInventado");
            asyncRingtonePlayer.play(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

            //set the onclicklisteners for the buttons
            View buttonDone = findViewById(R.id.button_med_done);
            View buttonPostpone = findViewById(R.id.button_med_postpone);
            View buttonNotWanna = findViewById(R.id.button_med_not_wanna);

            buttonDone.setOnClickListener(this);
            buttonNotWanna.setOnClickListener(this);
            buttonPostpone.setOnClickListener(this);

        }
    }

    //los toast de este metodo se podrian cambiar por algo distinto, pero ya se vera
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_med_done) {
            Log.d(TAG, "onClick en button_done");
            vibrator.cancel();
            asyncRingtonePlayer.stop();

            Toast.makeText(this, R.string.med_taken, Toast.LENGTH_LONG).show();

            if (postponedTimes!= 0) {
                ReminderManager.cancelPostponed(this,medicamento);
            }

            this.finish();
        }
        else if (v.getId() == R.id.button_med_not_wanna) {
            Log.d(TAG, "onClick en button_not_wanna");
            vibrator.cancel();
            asyncRingtonePlayer.stop();

            Toast.makeText(this, R.string.med_cancelled, Toast.LENGTH_LONG).show();

            ReminderManager.llamarAlJefe(this);

            this.finish();
        }
        else if (v.getId() == R.id.button_med_postpone) {
            Log.d(TAG, "onClick en button_postpone");
            vibrator.cancel();
            asyncRingtonePlayer.stop();

            if (postponedTimes==0)
                ReminderManager.postponeReminder(this, medicamento, ReminderManager.getPostponeTimeMilis());

            Toast.makeText(this, R.string.med_postponed, Toast.LENGTH_LONG).show();

            this.finish();
        }
    }

    // por si el user escapa de alguna manera del dialogo, le paro vibracion y sonido
    @Override
    protected void onStop() {
        super.onStop();
        if (!llamadoAlJefe){
            vibrator.cancel();
            asyncRingtonePlayer.stop();
        }
    }

    // esto lo copio de /home/inigo/AndroidStudioProjects/DeskClock/src/com/android/desclock/alarms/AlarmActivity.java
    private void hideNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}
