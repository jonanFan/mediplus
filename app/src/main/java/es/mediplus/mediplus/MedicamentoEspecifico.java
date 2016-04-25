package es.mediplus.mediplus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MedicamentoEspecifico extends Activity{

    Medicamento med;
    Calendar mDateAndTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

    EditText nombre;
    Button fechaInicial;
    Button fechaFinal;
    EditText cantidadCompra;
    Button fechaReposicion;
    Button frecuenciaDiaria;
    Button frecuenciaHorariaET;

    String horaInicial;
    String frecuenciaHoraria;

    Button bAnadir;
    Button bEditar;
    Button bEliminar;

    private Boolean stopMusic=true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicamento_especifico);
        //obtener enlace a recursos del layout
        bAnadir = (Button) findViewById(R.id.bAnadirMedicamento);
        bEditar = (Button) findViewById(R.id.bEditarMedicamento);
        bEliminar = (Button) findViewById(R.id.bEliminarMedicamento);
        nombre = (EditText) findViewById(R.id.nombreET);
        fechaInicial = (Button) findViewById(R.id.inicioET);
        fechaFinal = (Button) findViewById(R.id.finET);
        frecuenciaDiaria = (Button) findViewById(R.id.diasTomaET);
        frecuenciaHorariaET = (Button) findViewById(R.id.horasTomaET);
        cantidadCompra = (EditText) findViewById(R.id.cantidadCompraET);
        fechaReposicion = (Button) findViewById(R.id.fechaProximaCompraET);



        //una vista para tres funcionalidades, dibuja la que debe para cada caso
        int id = getIntent().getExtras().getInt("id");
        String tipoUsuario = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE).getString(Utils.CLIENT_TYPE, "default");
        if(id == 1 && tipoUsuario.equalsIgnoreCase(Utils.ADMIN))//añadir
        {
            med = new Medicamento();
            bAnadir.setVisibility(View.VISIBLE);
            bEditar.setVisibility(View.GONE);
            bEliminar.setVisibility(View.GONE);
        }
        else if(id == 23)//ver -> editar eliminar
        {
            med = (Medicamento) getIntent().getExtras().getSerializable("med");//obtener med y actualizar pantalla
            medToLayout();
            if (tipoUsuario.equalsIgnoreCase(Utils.ADMIN)) {
                bAnadir.setVisibility(View.GONE);
                bEditar.setVisibility(View.VISIBLE);
                bEliminar.setVisibility(View.VISIBLE);
            } else {
                nombre.setKeyListener(null);
                fechaInicial.setKeyListener(null);
                fechaFinal.setKeyListener(null);
                frecuenciaDiaria.setKeyListener(null);
                frecuenciaHorariaET.setKeyListener(null);
                cantidadCompra.setKeyListener(null);
                fechaReposicion.setKeyListener(null);
                bAnadir.setVisibility(View.GONE);
                bEditar.setVisibility(View.GONE);
                bEliminar.setVisibility(View.GONE);
            }
        }
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

    public void medToLayout(){
        nombre.setText(med.getNombre());
        fechaReposicion.setText(med.getFechaReposicion());
        fechaInicial.setText(med.getFechaInicial());
        fechaFinal.setText(med.getFechaFinal());
        cantidadCompra.setText(String.valueOf(med.getCantidadCompra()));
        frecuenciaDiaria.setText(med.getFrecuenciaDiaria());//TODO por ahora sólo LMXJVSD, luego se podrá meter frecuencia (X días,meses, años,)
        horaInicial = med.getHoraInicial();
        frecuenciaHoraria = med.getFrecuenciaHoraria();
        frecuenciaHorariaET.setText(horaInicial + " cada " + frecuenciaHoraria + " horas");
    }

    /*  funciones de añadir y editar medicamento    */
    public void onClick(View v)
    {
        int idClick = v.getId();
        if(idClick == R.id.inicioET | idClick == R.id.finET | idClick == R.id.fechaProximaCompraET)
            onDateClicked(idClick);//fechas
        if(idClick ==  R.id.diasTomaET | idClick == R.id.horasTomaET)
            goToDialog(idClick);//dias-horas
    }

    public void onDateClicked(final int idClick) {
        Button b = (Button)findViewById(idClick);

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateToEditText(idClick);
            }
        };
        if(b.getText().toString() == "") {
            mDateAndTime = Calendar.getInstance();
            new DatePickerDialog(this, mDateListener, mDateAndTime.get(Calendar.YEAR),
                    mDateAndTime.get(Calendar.MONTH), mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
        }
        else {
            Date date = Utils.stringToDate(b.getText().toString());
            mDateAndTime.setTime(date);
            DatePickerDialog datepicker = new DatePickerDialog(this, mDateListener,
                    mDateAndTime.get(Calendar.YEAR),
                    mDateAndTime.get(Calendar.MONTH), mDateAndTime.get(Calendar.DAY_OF_MONTH));
            datepicker.show();
        }

    }

    public String calendarToDateString() {
        return (new SimpleDateFormat("yyyy-MM-dd")).format(mDateAndTime.getTime());
    }

    private void dateToEditText(final int idClick) {
        ((Button)findViewById(idClick)).setText(calendarToDateString());
    }

    public void goToDialog(int idClick){
        Intent intent = new Intent(this, Dialogos.class);
        int id = getIntent().getExtras().getInt("id");//añadir (1) eliminar o editar (23)
        intent.putExtra("idClick", idClick);
        intent.putExtra("id", id);
        if(idClick == R.id.diasTomaET)
            intent.putExtra("frecuenciaDiaria",frecuenciaDiaria.getText().toString());
        else if(idClick == R.id.horasTomaET) {
                intent.putExtra("frecuenciaHoraria", frecuenciaHoraria);
                intent.putExtra("horaInicial", horaInicial);
        }
        stopMusic=false;
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent recogerDatos){
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                int id = recogerDatos.getIntExtra("id",-1);
                if(id == R.id.diasTomaET)
                    frecuenciaDiaria.setText(recogerDatos.getStringExtra("frecuenciaDiaria"));
                else if(id == R.id.horasTomaET) {
                    horaInicial = recogerDatos.getStringExtra("horaInicial");
                    frecuenciaHoraria = recogerDatos.getStringExtra("frecuenciaHoraria");
                    frecuenciaHorariaET.setText(horaInicial + " cada " + frecuenciaHoraria + " horas");
                }
            }
            //else if(resultCode == Activity.RESULT_CANCELED);//x si queremos que haga algo
        }
    }

    public void construirMedicamento(){
        /*   recoger datos de la pantalla para guardar el medicamento    */
        //idMedicamento lo pone la BD local-remota
        med.setNombre(nombre.getText().toString());
        med.setFechaInicial(fechaInicial.getText().toString());
        med.setFechaFinal(fechaFinal.getText().toString());
        med.setFechaReposicion(fechaReposicion.getText().toString());
        med.setCantidadCompra(Integer.parseInt(cantidadCompra.getText().toString()));
        med.setFrecuenciaDiaria(frecuenciaDiaria.getText().toString());
        med.setHoraInicial(horaInicial);
        med.setFrecuenciaHoraria(frecuenciaHoraria);
    }

    private boolean comprobarNumeros(EditText et){
        boolean datoCorrecto = true;
        try {
            int num = Integer.parseInt(et.getText().toString());
            if(num < 0)
                datoCorrecto = false;
            else
                datoCorrecto = true;
        }catch (NumberFormatException e){
            datoCorrecto = false;
        }
        return datoCorrecto;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean comprobarDatos(){
        boolean datosCorrectos = true;

        if(nombre.getText().toString().equals("")){
            paintShapeBorder(nombre,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(nombre,true);
        if(fechaInicial.getText().toString().equals("")){
            paintShapeBorder(fechaInicial,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(fechaInicial,true);
        if(fechaFinal.getText().toString().equals("")){
            paintShapeBorder(fechaFinal,false);
            datosCorrectos = false;
        }
        else {
            if(Utils.stringToDate(fechaFinal.getText().toString()).before(Utils.stringToDate(fechaInicial.getText().toString()))){
                paintShapeBorder(fechaFinal,false);
                datosCorrectos = false;
            }
            else
                paintShapeBorder(fechaFinal, true);
        }
        if(frecuenciaDiaria.getText().toString().equals("")){
            paintShapeBorder(frecuenciaDiaria,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(frecuenciaDiaria,true);
        if(frecuenciaHorariaET.getText().toString().equals("")){
            paintShapeBorder(frecuenciaHorariaET,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(frecuenciaHorariaET,true);
        if(cantidadCompra.getText().toString().equals("") | !comprobarNumeros(cantidadCompra)){
            paintShapeBorder(cantidadCompra,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(cantidadCompra, true);
        if(fechaReposicion.getText().toString().equals("")){
            paintShapeBorder(fechaReposicion,false);
            datosCorrectos = false;
        }
        else
            paintShapeBorder(fechaReposicion,true);
        return datosCorrectos;
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void paintShapeBorder(Button et, boolean check){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.LOLLIPOP) {
            if(check)
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.edittextshape));
            else
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.editttextshapered));
        } else {
            if(check)
                et.setBackground(getDrawable(R.drawable.edittextshape));
            else
                et.setBackground(getDrawable(R.drawable.editttextshapered));
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void paintShapeBorder(EditText et, boolean check){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.LOLLIPOP) {
            if(check)
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.edittextshape));
            else
                et.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.editttextshapered));
        } else {
            if(check)
                et.setBackground(getDrawable(R.drawable.edittextshape));
            else
                et.setBackground(getDrawable(R.drawable.editttextshapered));
        }
    }

    /*  BOTONES DE AÑADIR/EDITAR-ELIMINAR   */
    public void volver(View v) {
        if(v.getId() == R.id.bEliminarMedicamento){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("id", 3);
            returnIntent.putExtra("idLocal",med.getIdLocal());
            setResult(Activity.RESULT_OK, returnIntent);
            stopMusic=false;
            this.finish();
        }
        else {
            if(comprobarDatos()) {
                Intent returnIntent = new Intent();
                construirMedicamento();//actualizar medicamento desde pantalla
                returnIntent.putExtra("Medicamento", med);
                if (v.getId() == R.id.bAnadirMedicamento)
                    returnIntent.putExtra("id", 1);
                else if (v.getId() == R.id.bEditarMedicamento)
                    returnIntent.putExtra("id", 2);
                setResult(Activity.RESULT_OK, returnIntent);
                stopMusic=false;
                this.finish();
            }
        }
    }


}
