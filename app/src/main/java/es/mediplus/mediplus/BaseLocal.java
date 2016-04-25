package es.mediplus.mediplus;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class BaseLocal extends Activity {
    private Calendar cal;
    private Context context;

    BaseLocal(Context con) {
        context = con;
    }

    public void anadirMed(Medicamento med, int flag) {
        cal = Calendar.getInstance();
        long time = cal.getTimeInMillis();
        String timestamp = Long.toString(time);

        ContentValues values = new ContentValues();


        //values.put(BBDD.ID_MEDICAMENTO, med.getIdMedicamentos());////////////////////////////////////////////////////////////////
        if (med.getFlag() == 2) {
            log("med eliminar:" + med.getIdLocal());
            values.put(BBDD.FLAG, med.getFlag());
        } else {
            values.put(BBDD.FLAG, flag);
        }
        if (med.getIdMedicamentos() == -1)
            med.setIdMedicamentos(0);
        values.put(BBDD.ID_MEDICAMENTO, med.getIdMedicamentos());
        values.put(BBDD.NOMBRE, med.getNombre());
        values.put(BBDD.INICIO, med.getFechaInicial());
        values.put(BBDD.FINAL, med.getFechaFinal());
        values.put(BBDD.HORA_INICIO, med.getHoraInicial());
        values.put(BBDD.FREQ_HORARIA, med.getFrecuenciaHoraria());
        values.put(BBDD.FREQ_DIARIA, med.getFrecuenciaDiaria());
        values.put(BBDD.CANTIDAD_TOTAL, med.getCantidadCompra());
        values.put(BBDD.FECHA_REPOSICION, med.getFechaReposicion());
        values.put(BBDD.NUM_ALARMAS, med.getNumAlarmas());
        values.put(BBDD.TIMESTAMP, timestamp);

        context.getContentResolver().insert(BBDD.CONTENT_URI, values);
    }

    public void editarMed(Medicamento med) {
        cal = Calendar.getInstance();
        long time = cal.getTimeInMillis();
        String timestamp = Long.toString(time);
        log("editarlocal: " + med.getIdMedicamentos());
        ContentValues values = new ContentValues();
        values.put(BBDD.NOMBRE, med.getNombre());
        values.put(BBDD.INICIO, med.getFechaInicial());
        values.put(BBDD.FINAL, med.getFechaFinal());
        values.put(BBDD.HORA_INICIO, med.getHoraInicial());
        values.put(BBDD.FREQ_HORARIA, med.getFrecuenciaHoraria());
        values.put(BBDD.FREQ_DIARIA, med.getFrecuenciaDiaria());
        values.put(BBDD.CANTIDAD_TOTAL, med.getCantidadCompra());
        values.put(BBDD.FECHA_REPOSICION, med.getFechaReposicion());
        values.put(BBDD.NUM_ALARMAS, med.getNumAlarmas());
        values.put(BBDD.TIMESTAMP, timestamp);
        values.put(BBDD.FLAG, 1);

        context.getContentResolver().update(BBDD.CONTENT_URI, values, "?=" + BBDD.ID_LOCAL, new String[]{String.valueOf(med.getIdLocal())});
    }

    /*  private void eliminar(String id)
      {
          context.getContentResolver().delete(BBDD.CONTENT_URI, BBDD.ID_LOCAL + "=?", new String[]{id});//ID_LOCAL
      }
  */
    public void eliminarTabla() {
        context.getContentResolver().delete(BBDD.CONTENT_URI, null, null);
    }

    public Medicamento obtenerMed(String id) {
        Medicamento med = new Medicamento();
                                    /*BBDD.ID_MEDICAMENTO POR ID_LOCAL*/
        Cursor c = context.getContentResolver().query(BBDD.CONTENT_URI, new String[]{BBDD.ID_LOCAL, BBDD.FLAG,
                        BBDD.NOMBRE, BBDD.INICIO, BBDD.FINAL, BBDD.HORA_INICIO, BBDD.FREQ_HORARIA, BBDD.FREQ_DIARIA,
                        BBDD.CANTIDAD_TOTAL, BBDD.FECHA_REPOSICION, BBDD.NUM_ALARMAS,BBDD.TIMESTAMP}
                , "?=" + BBDD.ID_LOCAL, new String[]{id}, null);

        //Obtener lo valores
        if (c != null) {
            if (c.moveToFirst()) {

                med.setIdLocal(c.getInt(0));
                med.setFlag(Integer.parseInt(c.getString(1)));
                med.setNombre(c.getString(2));
                med.setFechaInicial(c.getString(3));
                med.setFechaFinal(c.getString(4));
                med.setHoraInicial(c.getString(5));
                med.setFrecuenciaHoraria(c.getString(6));
                med.setFrecuenciaDiaria(c.getString(7));
                med.setCantidadCompra(Integer.parseInt(c.getString(8)));
                med.setFechaReposicion(c.getString(9));
                med.setNumAlarmas(c.getInt(10));
                med.setTimeStamp(c.getString(11));
            } else
                med = null;
            c.close();
        }

        return med;
    }


    public ArrayList<Medicamento> obtenerMedis() {

        ArrayList<Medicamento> med = new ArrayList<>();
        Medicamento medicamento;
        //////BBDD.ID_MEDICAMENTO////////
        Cursor c = context.getContentResolver().query(BBDD.CONTENT_URI, new String[]{BBDD.ID_LOCAL, BBDD.ID_MEDICAMENTO, BBDD.FLAG,
                        BBDD.NOMBRE, BBDD.INICIO, BBDD.FINAL, BBDD.HORA_INICIO, BBDD.FREQ_HORARIA, BBDD.FREQ_DIARIA,
                        BBDD.CANTIDAD_TOTAL, BBDD.FECHA_REPOSICION, BBDD.NUM_ALARMAS, BBDD.TIMESTAMP}
                , null, null, null);


        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    medicamento = new Medicamento();
                    medicamento.setIdLocal(c.getInt(0));
                    medicamento.setIdMedicamentos(c.getInt(1));
                    medicamento.setFlag(c.getInt(2));
                    medicamento.setNombre(c.getString(3));
                    medicamento.setFechaInicial(c.getString(4));
                    medicamento.setFechaFinal(c.getString(5));
                    medicamento.setHoraInicial(c.getString(6));
                    medicamento.setFrecuenciaHoraria(c.getString(7));
                    medicamento.setFrecuenciaDiaria(c.getString(8));
                    medicamento.setCantidadCompra(c.getInt(9));
                    medicamento.setFechaReposicion(c.getString(10));
                    medicamento.setNumAlarmas(c.getInt(11));
                    medicamento.setTimeStamp(c.getString(12));
                    med.add(medicamento);

                } while (c.moveToNext());
            }
            c.close();
        }

        return med;
    }

    public void actualizarFlag(int id, int flag) {
        ContentValues values = new ContentValues();
        values.put(BBDD.FLAG, flag);
        context.getContentResolver().update(BBDD.CONTENT_URI, values, "?=" + BBDD.ID_LOCAL, new String[]{String.valueOf(id)});
    }


    public void log(String s) {
        Log.d("mediplus", s);
    }
}
