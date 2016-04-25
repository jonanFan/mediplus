package es.mediplus.mediplus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dialogos extends Activity {

    int ctvID[] = {R.id.lunes, R.id.martes, R.id.miercoles, R.id.jueves, R.id.viernes, R.id.sabado, R.id.domingo};
    String diasSemana = "LMXJVSD";
    TextView elegirSemanaTV;
    String semana;
    String hora;
    EditText frecuenciaHoraria;
    TimePicker horaInicial;
    int idClick = -1;
    Boolean stopMusic=true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogos);
        idClick = getIntent().getExtras().getInt("idClick");//dias, horas
        //int id = getIntent().getIntExtra("id",-1);//añadir 1 , eliminar, editar 23
        if (idClick == R.id.diasTomaET) {
            findViewById(R.id.vistaSemana).setVisibility(View.VISIBLE);
            findViewById(R.id.vistaDia).setVisibility(View.GONE);
            elegirSemanaTV = (TextView) findViewById(R.id.elegirSemanaTV);
            semana = getIntent().getStringExtra("frecuenciaDiaria");
            if (semana.equals("") != true) {
                semanaToLayout();
            }
            setCTVListeners();
        }
        if (idClick == R.id.horasTomaET) {
            findViewById(R.id.vistaSemana).setVisibility(View.GONE);
            findViewById(R.id.vistaDia).setVisibility(View.VISIBLE);
            frecuenciaHoraria = (EditText) findViewById(R.id.frecuenciaHoraria);
            horaInicial = (TimePicker) findViewById(R.id.horaInicial);
            frecuenciaHoraria.setText(getIntent().getStringExtra("frecuenciaHoraria"));
            hora = getIntent().getStringExtra("horaInicial");
            if (hora != null) {
                Date date = stringToDate(hora);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (Build.VERSION.SDK_INT >= 23) {
                    horaInicial.setHour(cal.get(Calendar.HOUR_OF_DAY));
                    horaInicial.setMinute(cal.get(Calendar.MINUTE));
                } else {
                    horaInicial.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                    horaInicial.setCurrentMinute(cal.get(Calendar.MINUTE));
                }

            }
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .9));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stopMusic)
            Musica.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stopMusic) {
            Musica.play();
        }
        stopMusic = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopMusic=false;
        this.finish();
    }

    public void setCTVListeners() {
        int ctvID[] = {R.id.lunes, R.id.martes, R.id.miercoles, R.id.jueves, R.id.viernes, R.id.sabado, R.id.domingo};
        CheckedTextView ctv;
        for (int i = 0; i < 7; i++) {
            ctv = (CheckedTextView) findViewById(ctvID[i]);
            ctv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckedTextView ctv = (CheckedTextView) findViewById(v.getId());
                    ctv.toggle();
                }
            });
        }
    }

    public Date stringToDate(String hora) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = df.parse(hora);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

    public void semanaToLayout() {
        CheckedTextView ctv;
        for (int i = 0; i < 7; i++) {
            ctv = (CheckedTextView) findViewById(ctvID[i]);
            if (semana.charAt(i) == diasSemana.charAt(i))
                ctv.setChecked(true);
            else
                ctv.setChecked(false);
        }
    }

    public String layoutToSemana() {
        CheckedTextView ctv;
        semana = "";
        for (int i = 0; i < 7; i++) {
            ctv = (CheckedTextView) findViewById(ctvID[i]);
            if (ctv.isChecked())
                semana += diasSemana.charAt(i);
            else
                semana += '-';
        }
        return semana;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean comprobarFrecuenciaHoraria() {
        boolean datosCorrectos = true;
        //que haya especificado hora inicial y frecuencia
        if (frecuenciaHoraria.getText().toString().equals(null)
                | frecuenciaHoraria.getText().toString().equals("")
                | !comprobarNumeroHora(frecuenciaHoraria)) {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.LOLLIPOP) {
                frecuenciaHoraria.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.editttextshapered));
            } else {
                frecuenciaHoraria.setBackground(getDrawable(R.drawable.editttextshapered));
            }
            datosCorrectos = false;
        }
        return datosCorrectos;
    }

    private boolean comprobarNumeroHora(EditText et) {
        boolean datoCorrecto = true;
        try {
            int num = Integer.parseInt(et.getText().toString());
            if (num < 0 | num > 23 | (num % 1 != 0))
                datoCorrecto = false;
            else
                datoCorrecto = true;
        } catch (NumberFormatException e) {
            datoCorrecto = false;
        }
        return datoCorrecto;
    }

    public boolean comprobarFrecuenciaDiaria() {
        //que se haya seleccionado al menos un día
        for (int i = 0; i < diasSemana.length(); i++) {
            layoutToSemana();
            if (semana.charAt(i) == diasSemana.charAt(i))
                return true;
        }
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.M)
            elegirSemanaTV.setTextColor(getResources().getColor(R.color.red));
        else
            elegirSemanaTV.setTextColor(ContextCompat.getColor(this, R.color.red));
        return false;
    }

    public void guardar(View v) {
        boolean datosCorrectos = false;
        String horaBien;
        if (idClick == R.id.horasTomaET) {
            datosCorrectos = comprobarFrecuenciaHoraria();
            if (datosCorrectos == true) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("frecuenciaHoraria", frecuenciaHoraria.getText().toString());
                if (Build.VERSION.SDK_INT >= 23)
                {
                    horaBien= horaInicial.getHour() + ":";

                    if(horaInicial.getMinute()<10)
                    {
                        horaBien+="0";
                    }
                    horaBien+=horaInicial.getMinute();

                    returnIntent.putExtra("horaInicial", horaBien);

                }

                else
                {
                    horaBien= horaInicial.getCurrentHour() + ":";

                    if(horaInicial.getCurrentMinute()<10)
                    {
                        horaBien+="0";
                    }
                    horaBien+=horaInicial.getCurrentMinute();

                    returnIntent.putExtra("horaInicial", horaBien);
                }
                returnIntent.putExtra("id", idClick);
                setResult(Activity.RESULT_OK, returnIntent);
                stopMusic=false;
                this.finish();
            }
        } else if (idClick == R.id.diasTomaET) {
            datosCorrectos = comprobarFrecuenciaDiaria();
            if (datosCorrectos == true) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("frecuenciaDiaria", layoutToSemana());
                returnIntent.putExtra("id", idClick);
                setResult(Activity.RESULT_OK, returnIntent);
                stopMusic=false;
                this.finish();
            }
        }
    }

    public void cancelar(View v) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        stopMusic=false;
        this.finish();
    }


    public void log(String s) {
        Log.d("mediplus", s);
    }
}

