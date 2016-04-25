package es.mediplus.mediplus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Created by jon on 11/03/16.
 */
public class NuevoEvento extends Activity {

    private EditText nombre, lugar, descripcion;
    private Button fechaInicio, fechaFinal, anadir;
    private Boolean check = false, stopMusic=true;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuevo_evento);

        nombre = (EditText) findViewById(R.id.nombreEven);
        fechaInicio = (Button) findViewById(R.id.inicioEven);
        fechaFinal = (Button) findViewById(R.id.finEven);
        lugar = (EditText) findViewById(R.id.lugarEven);
        descripcion = (EditText) findViewById(R.id.descripcionEven);
        anadir = (Button) findViewById(R.id.anadirEven);

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

    public void introducirFecha(final View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialogo_fecha, null);
        /*TimePicker time=(TimePicker)dialogView.findViewById(R.id.timePicker);
        time.setIs24HourView(true);*///TODO PONER ESTO PARA QUE SEA 24H
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.tituloFecha));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker date = (DatePicker) dialogView.findViewById(R.id.datePicker);
                TimePicker time = (TimePicker) dialogView.findViewById(R.id.timePicker);
                if (v.getId() == R.id.inicioEven) {
                    fechaInicio.setText(getFecha(date, time));
                    if (fechaFinal.getText().toString() == "") {
                        fechaFinal.setText(getFecha(date, time));
                    }


                } else {
                    fechaFinal.setText(getFecha(date, time));
                    if (fechaInicio.getText().toString() == "") {
                        fechaInicio.setText(getFecha(date, time));
                    }
                }

                if (Utils.stringToDate(fechaInicio.getText().toString()).compareTo(Utils.stringToDate(fechaFinal.getText().toString())) > 0) {
                    Toast.makeText(NuevoEvento.this, "La fecha final debe ser posterior a la fecha inicial", Toast.LENGTH_SHORT).show();
                    check = false;
                    paintShapeBorder(fechaInicio, false);
                    paintShapeBorder(fechaFinal, false);
                } else {
                    check = true;
                    paintShapeBorder(fechaInicio, true);
                    paintShapeBorder(fechaFinal, true);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void paintShapeBorder(Button et, boolean check) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.LOLLIPOP) {
            if (check)
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.edittextshape));
            else
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.editttextshapered));
        } else {
            if (check)
                et.setBackground(getDrawable(R.drawable.edittextshape));
            else
                et.setBackground(getDrawable(R.drawable.editttextshapered));
        }
    }

    private String getFecha(DatePicker date, TimePicker time) {

        String string = String.valueOf(date.getYear()) + "-" + String.valueOf(date.getMonth() + 1) + "-" + String.valueOf(date.getDayOfMonth());
        if (Build.VERSION.SDK_INT >= 23)
        {
            string += " " + time.getHour() + ":";

            if(time.getMinute()<10)
            {
                string+="0";
            }
            string+=time.getMinute();
        }
        else
        {
            string += " " + time.getCurrentHour() + ":";

            if(time.getCurrentMinute()<10)
            {
                string+="0";
            }
            string+=time.getCurrentMinute();
        }
        return string;
    }


    public void anadirCita(View v) {
        if (check) {
            if (nombre.getText().toString().equals("") || fechaInicio.getText().toString().equals("") || fechaFinal.getText().toString().equals("")) {
                Toast.makeText(this, "Es obligatorio rellenar los 3 primeros campos", Toast.LENGTH_SHORT).show();
            } else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("nombre", nombre.getText().toString());
                returnIntent.putExtra("fechaInicio", fechaInicio.getText().toString());
                returnIntent.putExtra("fechaFinal", fechaFinal.getText().toString());
                returnIntent.putExtra("lugar", lugar.getText().toString());
                returnIntent.putExtra("descripcion", descripcion.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                stopMusic=false;
                this.finish();
            }
        } else {
            Toast.makeText(NuevoEvento.this, "La fecha final debe ser posterior a la fecha inicial", Toast.LENGTH_SHORT).show();

        }
    }

}
