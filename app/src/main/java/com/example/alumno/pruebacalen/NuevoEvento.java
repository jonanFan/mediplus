package com.example.alumno.pruebacalen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

    EditText nombre, lugar, descripcion;
    Button fechaInicio, fechaFinal, anadir;


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

    public void introducirFecha(final View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialogo_fecha, null);
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.tituloFecha));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker date=(DatePicker)dialogView.findViewById(R.id.datePicker);
                TimePicker time=(TimePicker)dialogView.findViewById(R.id.timePicker);
                if (v.getId() == R.id.inicioEven) {
                    fechaInicio.setText(getFecha(date, time));
                } else {
                    fechaFinal.setText(getFecha(date, time));
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

    private String getFecha(DatePicker date, TimePicker time){

        String string=new String(date.getYear()+"-"+date.getMonth()+"-"+date.getDayOfMonth());
        if (Build.VERSION.SDK_INT >= 23 )
            string+=" "+time.getHour()+":"+time.getMinute();
        else
            string+=" "+time.getCurrentHour()+":"+time.getCurrentMinute();

        return string;
    }

    public void anadirCita(View v) {
        if(nombre.getText().toString().equals("") || fechaInicio.getText().toString().equals("") || fechaFinal.getText().toString().equals(""))
            Toast.makeText(this,"Es obligatorio rellenar los 3 primeros campos",Toast.LENGTH_SHORT).show();
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("nombre", nombre.getText().toString());
            returnIntent.putExtra("fechaInicio", fechaInicio.getText().toString());
            returnIntent.putExtra("fechaFinal", fechaFinal.getText().toString());
            returnIntent.putExtra("lugar", lugar.getText().toString());
            returnIntent.putExtra("descripcion", descripcion.getText().toString());
            setResult(Activity.RESULT_OK, returnIntent);
            this.finish();
        }
    }

}
