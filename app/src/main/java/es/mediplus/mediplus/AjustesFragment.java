package es.mediplus.mediplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import es.mediplus.ssh.SSH;

/**
 * Created by ene on 02/03/2016.
 */
public class AjustesFragment extends PreferenceFragment {
    static Connection cx = null;
    static Statement statement = null;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ajustes);
        context=getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceTask cxTask = new PreferenceTask(getString(R.string.mysqlUser), getString(R.string.mysqlPwd), getString(R.string.mysqlHost), Integer.valueOf(getString(R.string.mysqlPort)));

        cxTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public class PreferenceTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;//"nuestra cuenta de usuario para entrar a mysql DB"
        private final String mPassword;
        private final String mHost;
        private final int    mPort;
        private SSH ssh;

        PreferenceTask(String email, String password, String host, int port) {
            mEmail = email;
            mPassword = password;
            mHost=host;
            mPort=port;
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            SharedPreferences sharedPref = context.getSharedPreferences(Utils.CLIENT_PREFERENCES, Context.MODE_PRIVATE);
            String name = sharedPref.getString(Utils.CLIENT_ID, "default");

            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String avisoCitas = sharedPref.getString("rCitasT","h")+sharedPref.getString("rCitasV","1");
            String avisoMedicamentos = sharedPref.getString("rMedicamentosT","h")+sharedPref.getString("rMedicamentosV","1");
            String telefono = sharedPref.getString("numTfn", "666666666");


            String sqlQuery = "UPDATE `Clientes` SET telefono='" + telefono + "',tiempoCita='" + avisoCitas + "',tiempoMedicamento='"
                    + avisoMedicamentos + "' WHERE idClientes='"
                    + name + "'";

            //cx con mysql
            try {
                ssh=new SSH(mHost, mPort, mPort, mEmail, mPassword);
                if(ssh.connect()==0) {
                    Log.d("asd","Parece que funciona");
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                cx = DriverManager.getConnection("jdbc:mysql://localhost:"+mPort+"/mediplus", mEmail, mPassword);
                statement = cx.createStatement();
                statement.executeUpdate(sqlQuery);
                }
                else {
                    Log.d("asd","Parece que no funciona");
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
            try {
                if (statement != null)
                    statement.close();
                if (cx != null)
                    cx.close();
                if(ssh!=null)
                    ssh.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
