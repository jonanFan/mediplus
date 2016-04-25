package es.mediplus.mediplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class Medicamentos extends Activity {

    private TableLayout table = null;
    private String[] firstColName = {"Medicamento", "Fecha reposición"};
    private Medicamento med;
    private Boolean stopMusic=true;

    //BD LOCAL
    private BaseLocal bl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicamentos);
        bl = new BaseLocal(this);
        table = (TableLayout)findViewById(R.id.tablaMedicamentos);
        TextView check = (TextView) findViewById(R.id.check);
        verTabla();
        SharedPreferences sharedPref = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
        String tipoUsuario = sharedPref.getString(Utils.CLIENT_TYPE,"default");
        if (!tipoUsuario.equalsIgnoreCase(Utils.ADMIN))
            findViewById(R.id.bNuevoMedicamento).setVisibility(View.GONE);
        Sincronizacion sincr= new Sincronizacion();

        int idCliente = Integer.parseInt(sharedPref.getString(Utils.CLIENT_ID, "-1"));
        sincr.realizarSincr(tipoUsuario, this, String.valueOf(idCliente));
    }

    public void anadirMedicamento(View v) {
        stopMusic=false;
        Intent intent = new Intent("es.mediplus.mediplus.MEDICAMENTOESPECIFICO");
        intent.putExtra("id", 1);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (stopMusic) {
            Musica.play();
        }
        stopMusic = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stopMusic)
            Musica.pause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPref2 = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
        String tipoUsuario = sharedPref2.getString(Utils.CLIENT_TYPE,"default");
        Sincronizacion sincr= new Sincronizacion();

        int idCliente = Integer.parseInt(sharedPref2.getString(Utils.CLIENT_ID, "-1"));
        sincr.realizarSincr(tipoUsuario, this.getApplicationContext(), String.valueOf(idCliente));
        stopMusic=false;
        this.finish();
    }

    public void verMedicamento(int idMed) {
        med = bl.obtenerMed(String.valueOf(idMed));
        if(med!=null) {
            stopMusic = false;
            Intent intent = new Intent("es.mediplus.mediplus.MEDICAMENTOESPECIFICO");
            intent.putExtra("id", 23);
            intent.putExtra("med", med);
            startActivityForResult(intent, 3);//request code
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent recogerDatos){
        if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                if(recogerDatos.getIntExtra("id",-1) == 1) {
                    med = (Medicamento)recogerDatos.getSerializableExtra("Medicamento");
                    bl.anadirMed(med,1);//añade medicamento a la BD local
                }
            }
            //else if(resultCode == Activity.RESULT_CANCELED);//x si queremos que haga algo
        }
        else if(requestCode == 3){//TODO poner request code 0
            if(resultCode == Activity.RESULT_OK) {
                if (recogerDatos.getIntExtra("id", -1) == 2) {
                    med = (Medicamento) recogerDatos.getSerializableExtra("Medicamento");
                    bl.editarMed(med);
                } else if (recogerDatos.getIntExtra("id", -1) == 3) {
                    bl.actualizarFlag(recogerDatos.getIntExtra("idLocal",-1),2);
                }
            }
        }
        verTabla();
    }

    public void verTabla(){
            ArrayList<Medicamento> medList = bl.obtenerMedis();
            TableRow row = new TableRow(this);
            TextView column;

            table.removeAllViews();
            table.setStretchAllColumns(true);
            table.setShrinkAllColumns(true);

            for (int i = 0; i <= 1; i++) {
                column = new TextView(this);
                column.setText(firstColName[i]);
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < Build.VERSION_CODES.M)
                    column.setTextColor(getResources().getColor(R.color.title));
                else
                    column.setTextColor(ContextCompat.getColor(this, R.color.title));
                column.setTextSize(22);
                column.setTypeface(null, Typeface.BOLD);
                column.setPaintFlags(column.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                column.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                row.addView(column);
            }
            table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            int j = 0;
            while (j < medList.size())
                {
                    if(medList.get(j).getFlag()!=2)
                    {
                        row = new TableRow(this);//NOMBRE
                        row.setId(medList.get(j).getIdLocal());//id row = idMedicamento!!
                        column = new TextView(this);
                        column.setText(medList.get(j).getNombre());
                        column.setTextSize(22);
                        column.setTextColor(getResources().getColor(R.color.textDark));
                        column.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        row.addView(column);

                        column = new TextView(this);//FECHA REPOSICIÓN
                        column.setText(medList.get(j).getFechaReposicion());
                        column.setTextSize(22);
                        column.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        char n = comprobarValidezFechas(medList.get(j).getFechaInicial(),medList.get(j).getFechaFinal());
                        int sdk = android.os.Build.VERSION.SDK_INT;
                        if (sdk < Build.VERSION_CODES.M) {
                            if (n == 'V')
                                column.setTextColor(getResources().getColor(R.color.textDark));
                            else if (n == 'A')
                                column.setTextColor(getResources().getColor(R.color.red));
                            else if (n == 'F')
                                column.setTextColor(getResources().getColor(R.color.darkgray));
                        }
                        else
                        {
                            if(n == 'V')
                                column.setTextColor(ContextCompat.getColor(this, R.color.textDark));
                            else if(n == 'A')
                                column.setTextColor(ContextCompat.getColor(this, R.color.red));
                            else if(n == 'F')
                                column.setTextColor(ContextCompat.getColor(this, R.color.darkgray));
                        }

                        row.addView(column);

                        row.setClickable(true);
                        row.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                verMedicamento(v.getId());//pasar el idRow = idMedicamento
                            }
                        });
                        table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    j++;
                }
    }

    private char comprobarValidezFechas(String fechaInicial, String fechaFinal){
        char n = 'V';
        Calendar ini, fin, hoy;
        ini = stringToCalendar(fechaInicial);
        fin = stringToCalendar(fechaFinal);
        hoy = Calendar.getInstance();
        hoy = stringToCalendar(calendarToDateString(hoy));

        if(ini.after(hoy))
            n = 'F';
        else if(fin.before(hoy))
            n = 'A';
        else
            n = 'V';
        return n;//Anterior, Valido, Futuro
    }

    public String calendarToDateString(Calendar cal) {
        return (new SimpleDateFormat("yyyy-MM-dd")).format(cal.getTime());
    }

    public Date stringToDate(String fecha){
        Date date = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = df.parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Calendar stringToCalendar(String fecha){
        Calendar cal = Calendar.getInstance();
        cal.setTime(stringToDate(fecha));
        return cal;
    }

    public void log(String s){
        Log.d("mediplus", s);
    }
}
