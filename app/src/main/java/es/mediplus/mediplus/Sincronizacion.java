package es.mediplus.mediplus;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import es.mediplus.ssh.SSH;

/**
 * Created by josu on 19/03/16.
 */
public class Sincronizacion {

    static Connection cx = null;
    static ResultSet resultSet = null;
    static Statement statement = null;
    private BaseLocal bl;
    private ArrayList<Medicamento> listMed2;
    private String tipoUsr;
    String idCli;
    Medicamento medAux;
    Medicamento med1;
    Medicamento med2;

    public void realizarSincr(String tipoUsuario, Context cont, String idCliente) {

        if (Utils.isNetworkActive(cont)) {
            bl = new BaseLocal(cont);
            med1 = new Medicamento();
            med2 = new Medicamento();
            idCli = idCliente;
            tipoUsr = tipoUsuario;
            VerBD cxTask = new VerBD(cont, cont.getString(R.string.mysqlUser), cont.getString(R.string.mysqlPwd), cont.getString(R.string.mysqlHost), Integer.valueOf(cont.getString(R.string.mysqlPort)));
            cxTask.execute((Void) null);
        }


    }

    public void realizarSincr(String tipoUsuario, Medicamentos cont, String idCliente) {

        if (Utils.isNetworkActive(cont)) {
            bl = new BaseLocal(cont);
            med1 = new Medicamento();
            med2 = new Medicamento();
            idCli = idCliente;
            tipoUsr = tipoUsuario;
            VerBD cxTask = new VerBD(cont, cont.getString(R.string.mysqlUser), cont.getString(R.string.mysqlPwd), cont.getString(R.string.mysqlHost), Integer.valueOf(cont.getString(R.string.mysqlPort)));
            cxTask.execute((Void) null);
        }


    }

    //CX CON LA BD REMOTA
    public class VerBD extends AsyncTask<Void, Void, Boolean> {
        private Medicamentos medicamentos = null;
        private Context context;
        private final String mEmail;//"nuestra cuenta de usuario para entrar a mysql DB"
        private final String mPassword;
        private final String mHost;
        private final int mPort;
        private ArrayList<Medicamento> listAux = null;
        private SSH ssh;
        String verQuery = "SELECT * FROM Medicamentos WHERE Clientes_idClientes = '" + idCli + "'";

        public VerBD(Medicamentos cont, String email, String password, String host, int port) {
            this.context = cont;
            this.medicamentos = cont;
            mEmail = email;
            mPassword = password;
            mHost = host;
            mPort = port;
        }

        public VerBD(Context cont, String email, String password, String host, int port) {
            this.context = cont;
            mEmail = email;
            mPassword = password;
            mHost = host;
            mPort = port;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ssh = new SSH(mHost, mPort, mPort, mEmail, mPassword);
                if (ssh.connect() == 0) {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    cx = DriverManager.getConnection("jdbc:mysql://localhost:" + mPort + "/mediplus", mEmail, mPassword);
                    statement = cx.createStatement();

                    if (tipoUsr.equalsIgnoreCase(Utils.ADMIN)) {
                        ArrayList<Medicamento> listMed1 = bl.obtenerMedis();
                        listMed2 = new ArrayList<Medicamento>();

                        resultSet = statement.executeQuery(verQuery);

                        while (resultSet.next()) {
                            medAux = new Medicamento();
                            medAux.setIdMedicamentos(resultSet.getInt("idMedicamentos"));
                            medAux.setNombre(resultSet.getString("nombre"));
                            medAux.setFechaInicial(resultSet.getString("inicio"));
                            medAux.setFechaFinal(resultSet.getString("final"));
                            medAux.setHoraInicial(resultSet.getString("horaInicio"));
                            medAux.setFrecuenciaHoraria(resultSet.getString("frecuenciaHoraria"));
                            medAux.setFrecuenciaDiaria(resultSet.getString("frecuenciaDiaria"));
                            medAux.setCantidadCompra(resultSet.getInt("cantidadTotal"));
                            medAux.setFechaReposicion(resultSet.getString("fechaReposicion"));
                            medAux.setTimeStamp(resultSet.getString("timestamp"));

                            listMed2.add(medAux);
                        }

                        for (int i = 0; i < listMed1.size(); i++) {

                            med1 = listMed1.get(i);
                            log("EL flag al sincronizar el medicamento " + med1.getNombre() + "es " + med1.getFlag());
                            if (med1.getFlag() == 2) {
                                log("Entro a eliminar con id remoto " + med1.getIdMedicamentos());
                                String queryBorrar = hacerQueryBorrar(med1.getIdMedicamentos());
                                statement.executeUpdate(queryBorrar);
                            }

                            if (med1.getFlag() == 1) {
                                Boolean check = comprobarExistencia(med1.getIdMedicamentos());
                                if (!check) {

                                    String queryInsert = hacerQueryInsert(med1);
                                    statement.executeUpdate(queryInsert);
                                    log("Hace Insert");
                                } else {
                                    if (Long.parseLong(med1.getTimeStamp()) > Long.parseLong(med2.getTimeStamp())) {
                                        String queryUpdate = hacerQueryUpdate(med1);
                                        statement.executeUpdate(queryUpdate);
                                        log("Hace update");
                                    }
                                }
                            }
                        }
                    }

                    ArrayList<Medicamento> listAux = bl.obtenerMedis();

                    for (int i = 0; i < listAux.size(); i++) {
                        ReminderManager.cancelReminder(context, listAux.get(i));
                    }

                    bl.eliminarTabla();


                    resultSet = statement.executeQuery(verQuery);

                    listMed2 = new ArrayList<>();

                    while (resultSet.next()) {
                        medAux = new Medicamento();
                        medAux.setIdMedicamentos(resultSet.getInt("idMedicamentos"));
                        log("Lo he metido en la BBDD con el id: " + medAux.getIdMedicamentos());
                        medAux.setNombre(resultSet.getString("nombre"));
                        medAux.setFechaInicial(resultSet.getString("inicio"));
                        medAux.setFechaFinal(resultSet.getString("final"));
                        medAux.setHoraInicial(resultSet.getString("horaInicio"));
                        medAux.setFrecuenciaHoraria(resultSet.getString("frecuenciaHoraria"));
                        medAux.setFrecuenciaDiaria(resultSet.getString("frecuenciaDiaria"));
                        medAux.setCantidadCompra(resultSet.getInt("cantidadTotal"));
                        medAux.setFechaReposicion(resultSet.getString("fechaReposicion"));
                        medAux.setTimeStamp(resultSet.getString("timestamp"));

                        listMed2.add(medAux);
                    }

                    for (int i = 0; i < listMed2.size(); i++) {
                        ReminderManager.setReminder(context, listMed2.get(i));
                        Log.d("inigo:", Integer.toString(listMed2.get(i).getNumAlarmas()));
                        bl.anadirMed(listMed2.get(i), 0);
                    }

                } else {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (medicamentos != null)
                medicamentos.verTabla();
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
                if (cx != null)
                    cx.close();
                if (ssh != null)
                    ssh.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String hacerQueryUpdate(Medicamento med) {
        String sqlQuery = "UPDATE `Medicamentos` SET nombre='" + med.getNombre() + "',inicio='" + med.getFechaInicial() + "',final='"
                + med.getFechaFinal() + "',horaInicio='" + med.getHoraInicial() + "',frecuenciaHoraria='" + med.getFrecuenciaHoraria()
                + "',frecuenciaDiaria='" + med.getFrecuenciaDiaria() + "',cantidadTotal='" + med.getCantidadCompra()
                + "',fechaReposicion='" + med.getFechaReposicion() + "',timestamp='" + med.getTimeStamp() + "' WHERE idMedicamentos='"
                + med.getIdMedicamentos() + "'";

        return sqlQuery;
    }

    private String hacerQueryInsert(Medicamento med) {
        String sqlQuery = "INSERT INTO `Medicamentos` " +
                "(Clientes_idClientes,nombre,inicio,final,horaInicio,frecuenciaHoraria,frecuenciaDiaria,cantidadTotal,fechaReposicion,timestamp)" +
                " VALUES ('" + idCli + "','"
                + med.getNombre() + "','" + med.getFechaInicial()
                + "','" + med.getFechaFinal() + "','" + med.getHoraInicial()
                + "','" + med.getFrecuenciaHoraria() + "','" + med.getFrecuenciaDiaria() + "','"
                + med.getCantidadCompra() + "','" + med.getFechaReposicion() + "','"
                + med.getTimeStamp() + "')";


        return sqlQuery;
    }

    private Boolean comprobarExistencia(int idMedicamentos) {
        Boolean check = false;

        log("Vamos a comprobar si existe el med " + idMedicamentos);

        for (int i = 0; i < listMed2.size(); i++) {
            Medicamento medCheck = listMed2.get(i);
            log("Se compara con: " + medCheck.getIdMedicamentos());
            if (idMedicamentos == medCheck.getIdMedicamentos()) {
                check = true;
                med2 = medCheck;
            }

        }


        return check;
    }

    private String hacerQueryBorrar(int idMed) {
        String sqlQueryBorrar = "DELETE FROM Medicamentos WHERE idMedicamentos = '" + idMed + "'";
        return sqlQueryBorrar;
    }

    public void log(String s) {
        Log.d("mediplus", s);
    }

    /*public void enviarNuevoMedicamento(){
        sqlQuery="INSERT INTO `Medicamentos` " +
                "(Clientes_idClientes,nombre,inicio,final,horaInicio,frecuenciaHoraria,frecuenciaDiaria,cantidadTotal,fechaReposicion,timestamp)" +
                " VALUES ('" + idCliente + "','"
                + med.getNombre() + "','" + med.getFechaInicial()
                + "','" + med.getFechaFinal() + "','"+med.getHoraInicial()
                + "','" + med.getFrecuenciaHoraria() + "','" + med.getFrecuenciaDiaria() + "','"
                + med.getCantidadCompra() + "','" + med.getFechaReposicion() + "','"
                + med.getTimeStamp() + "')";
    }

    public void eliminarMedicamento(String idMediicamento){
        sqlQuery = "DELETE FROM Medicamentos WHERE nombre = '" + idMediicamento + "'";
    }*/
}
