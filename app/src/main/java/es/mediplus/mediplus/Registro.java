package es.mediplus.mediplus;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import es.mediplus.ssh.SSH;

/**
 * Created by ene on 02/03/2016.
 */
public class Registro extends Activity {

    private GoogleAccountCredential credential;

    private EditText eTnombre = null;
    private EditText eTapellido1 = null;
    private EditText eTapellido2 = null;
    private EditText eTtelefono = null;
    private EditText eTpwdAdmin = null;
    private EditText eTpwdPaciente = null;
    private EditText eTpwdFamiliar = null;
    private Button eTcorreo = null;

    private String nombre = null;
    private String apellido1 = null;
    private String apellido2 = null;
    private String telefono = null;
    private String pwdAdmin = null;
    private String pwdFamiliar = null;
    private String pwdPaciente = null;
    private String correo = null;

    static String sqlQuery = null;
    static ResultSet resultSet = null;
    static Statement statement = null;
    static Connection cx = null;
    ProgressDialog progressDialog;


    String check = "El usuario se ha registrado correctamente";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Utils.SCOPES)).
                setBackOff(new ExponentialBackOff());

        eTnombre = (EditText) findViewById(R.id.editTextR1);
        eTapellido1 = (EditText) findViewById(R.id.editTextR2);
        eTapellido2 = (EditText) findViewById(R.id.editTextR3);
        eTtelefono = (EditText) findViewById(R.id.editTextR4);
        eTpwdAdmin = (EditText) findViewById(R.id.editTextR5);
        eTpwdPaciente = (EditText) findViewById(R.id.editTextR6);
        eTpwdFamiliar = (EditText) findViewById(R.id.editTextR7);
        eTcorreo = (Button) findViewById(R.id.editTextR8);
    }

    public void registro(View v) {
        obtenerDatos();
        boolean check2 = comprobarDatosRellenos();
        if (check2) {
            comprobacion();
           /* try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, check, Toast.LENGTH_SHORT).show();*/
        }
    }

    private void obtenerDatos() {
        nombre = eTnombre.getText().toString();
        apellido1 = eTapellido1.getText().toString();
        apellido2 = eTapellido2.getText().toString();
        telefono = eTtelefono.getText().toString();
        pwdAdmin = eTpwdAdmin.getText().toString();
        pwdPaciente = eTpwdPaciente.getText().toString();
        pwdFamiliar = eTpwdFamiliar.getText().toString();
        correo = eTcorreo.getText().toString();

    }

    private boolean comprobarDatosRellenos() {
        if (nombre.isEmpty() || apellido1.isEmpty() || apellido2.isEmpty() == true || telefono.isEmpty() || pwdAdmin.isEmpty() || pwdPaciente.isEmpty() || pwdFamiliar.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Es obligatorio rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isNumber(telefono)) {
            Toast.makeText(this, "El numero de telefono debe ser un numero", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pwdAdmin.equalsIgnoreCase(pwdFamiliar) || pwdFamiliar.equalsIgnoreCase(pwdPaciente) || pwdPaciente.equalsIgnoreCase(pwdAdmin)) {
            Toast.makeText(this, "Las contaseñas de administrador, paciente y familiar no pueden coincidir", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }


    private boolean isNumber(String cadena) {
        try {
            Long.parseLong(cadena);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void comprobacion() {
        UserLogingTask cxTask = new UserLogingTask(getString(R.string.mysqlUser), getString(R.string.mysqlPwd), getString(R.string.mysqlHost), Integer.valueOf(getString(R.string.mysqlPort)));
        cxTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void showRegisterDialog() {
        progressDialog = ProgressDialog.show(this, "Comprobando...", "Realizando conexion", true);
    }

    private void showToast() {
        Toast.makeText(this, check, Toast.LENGTH_SHORT).show();
    }

    public class UserLogingTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;//"nuestra cuenta de usuario para entrar a mysql DB"
        private final String mPassword;
        private boolean userOK = false;
        private final String mHost;
        private final int    mPort;
        private SSH ssh;


        UserLogingTask(String email, String password, String host, int port) {
            mEmail = email;
            mPassword = password;
            mHost=host;
            mPort=port;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showRegisterDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            sqlQuery = "SELECT * FROM Clientes WHERE correoGoogle = '" + correo + "'";
            String sqlQuery2 = "INSERT INTO `Clientes` (nombre,apellido1,apellido2,telefono,pwdAdmin,pwdPaciente,pwdFamiliar,correoGoogle,tiempoCita, tiempoMedicamento) " +
                    "VALUES ('" + nombre + "','" + apellido1 + "','" + apellido2 + "','" + telefono + "','" + pwdAdmin + "','" + pwdPaciente + "','" + pwdFamiliar + "','" + correo + "','" + "d01" +
                    "','" + "h01" + "')";

            //cx con mysql
            try {
                ssh=new SSH(mHost, mPort, mPort, mEmail, mPassword);
                if(ssh.connect()==0) {
                    Log.d("asd", "Parece que funciona");
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                cx = DriverManager.getConnection("jdbc:mysql://localhost:"+mPort+"/mediplus", mEmail, mPassword);
                statement = cx.createStatement();

                resultSet = statement.executeQuery(sqlQuery);

                if (!resultSet.next()) {
                    statement.executeUpdate(sqlQuery2);
                    check = "El usuario se ha registrado correctamente";
                    userOK = true;
                } else {
                    check = "Ya existe una cuenta con ese correo";
                    userOK = false;
                }
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
        protected void onPostExecute(final Boolean success) {
            progressDialog.dismiss();
            showToast();
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
                if (cx != null)
                    cx.close();
                if(ssh!=null)
                    ssh.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (userOK)
                finish();
        }
    }


    public void chooseAccount(View v) {
        if (isGooglePlayServicesAvailable()) {
            startActivityForResult(credential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability available = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                available.isGooglePlayServicesAvailable(this);
        if (available.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GoogleApiAvailability available = GoogleApiAvailability.getInstance();
                Dialog dialog = available.getErrorDialog(
                        Registro.this,connectionStatusCode,
                        Utils.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case Utils.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        eTcorreo.setText(accountName);
                    }
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }
}
