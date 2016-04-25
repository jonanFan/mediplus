package es.mediplus.mediplus;

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import es.mediplus.ssh.SSH;

public class Loging extends Activity {


    private UserLogingTask CxTask = null;
    static Connection cx = null;
    static String sqlQuery = null;
    static ResultSet resultSet = null;
    static Statement statement = null;
    private Button edcorreo = null;
    private EditText edpwd = null;
    private String correo = null;
    private String pwd = null;
    private ProgressDialog progress;
    private String tipoUsuario;
    private GoogleAccountCredential credential;
    private Boolean exit=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loging);
        edcorreo = (Button) findViewById(R.id.editText);
        edpwd = (EditText) findViewById(R.id.editText2);

        edpwd.setTypeface(Typeface.DEFAULT);
        edpwd.setTransformationMethod(new PasswordTransformationMethod());

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Utils.SCOPES)).
                setBackOff(new ExponentialBackOff()).setSelectedAccountName(settings.getString(Utils.CUENTA_USUARIO, null));


        if (getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE).getString(Utils.CUENTA_USUARIO, "default").equals("default")) {
            edcorreo.setText("");
            edpwd.setText("");
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
                        Loging.this, connectionStatusCode,
                        Utils.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    public void goMenu(View v) {
        correo = edcorreo.getText().toString();
        pwd = edpwd.getText().toString();

        if (correo.isEmpty() == false && !pwd.isEmpty()) {

            if(Utils.isNetworkActive(this))
            {
                progress = ProgressDialog.show(this, "Comprobando...", "Realizando conexion", true);

                CxTask = new UserLogingTask(getString(R.string.mysqlUser), getString(R.string.mysqlPwd), getString(R.string.mysqlHost), Integer.valueOf(getString(R.string.mysqlPort)));
                CxTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
                Toast.makeText(this, "No tiene conexion a internet", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "Introduzca el correo y la contraseña", Toast.LENGTH_SHORT).show();
        }


    }

    public void showLogingError() {
        Toast.makeText(this, "Usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show();

        paintShapeBorder(edcorreo, false);
        paintShapeBorder(edpwd, false);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void paintShapeBorder(EditText et, boolean check) {
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

    public void goRegistro(View v) {
        paintShapeBorder(edcorreo, true);
        paintShapeBorder(edpwd, true);
        Intent intent = new Intent("es.mediplus.mediplus.REGISTRO");
        startActivity(intent);
    }

    public class UserLogingTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;//"nuestra cuenta de usuario para entrar a mysql DB"
        private final String mPassword;
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
        protected Boolean doInBackground(Void... params) {
            sqlQuery = "SELECT * FROM Clientes WHERE correoGoogle = '" + correo + "'";
            //cx con mysql
            try {
                ssh=new SSH(mHost, mPort, mPort, mEmail, mPassword);
                if(ssh.connect()==0) {
                    Log.d("asd","Parece que funciona");
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    cx = DriverManager.getConnection("jdbc:mysql://localhost:" + mPort + "/mediplus", mEmail, mPassword);
                    statement = cx.createStatement();
                    resultSet = statement.executeQuery(sqlQuery);
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
            CxTask = null;
            String pwdAdmin = null;
            String pwdPaciente = null;
            String pwdFamiliar = null;
            String numTelf = null;
            String avisoMedicamento = null;
            String avisoCita = null;


            int idCliente = -1;

            if (cx != null && statement != null && resultSet != null) {
                try {
                    resultSet.next();
                    idCliente = resultSet.getInt(1);//id
                    pwdAdmin = resultSet.getString("pwdAdmin");
                    pwdPaciente = resultSet.getString("pwdPaciente");
                    pwdFamiliar = resultSet.getString("pwdFamiliar");
                    numTelf = resultSet.getString("telefono");
                    avisoCita = resultSet.getString("tiempoCita");
                    avisoMedicamento = resultSet.getString("tiempoMedicamento");


                    if (pwdAdmin.equalsIgnoreCase(pwd))
                        tipoUsuario = Utils.ADMIN;
                    if (pwdPaciente.equalsIgnoreCase(pwd))
                        tipoUsuario = Utils.PACIENTE;
                    if (pwdFamiliar.equalsIgnoreCase(pwd))
                        tipoUsuario = Utils.FAMILIAR;
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (pwd.equalsIgnoreCase(pwdAdmin) || pwd.equalsIgnoreCase(pwdFamiliar) || pwd.equalsIgnoreCase(pwdPaciente)) {
                    Intent intent = new Intent("es.mediplus.mediplus.MENU");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    SharedPreferences preferencias = getSharedPreferences(Utils.CLIENT_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString(Utils.CLIENT_ID, String.valueOf(idCliente));
                    editor.putString(Utils.CLIENT_TYPE, tipoUsuario);
                    editor.apply();
                    Log.d("mediplus", ".idcliente: " + idCliente + " tipo usuario: " + tipoUsuario);
                    preferencias = PreferenceManager.getDefaultSharedPreferences(Loging.this);
                    editor = preferencias.edit();
                    editor.putString("numTfn", numTelf);
                    editor.putString("rCitasT", String.valueOf(avisoCita.charAt(0)));
                    editor.putString("rCitasV", String.valueOf(avisoCita.substring(1)));
                    editor.putString("rMedicamentosT", String.valueOf(avisoMedicamento.charAt(0)));
                    editor.putString("rMedicamentosV", String.valueOf(avisoMedicamento.substring(1)));
                    editor.apply();
                    finish();
                    startActivity(intent);

                } else {
                    showLogingError();
                }
            }

            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
                if (cx != null)
                    cx.close();
                if(ssh!=null)
                    ssh.disconnect();
                progress.dismiss();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getSharedPreferences(Utils.CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Utils.CUENTA_USUARIO, accountName);
                        editor.apply();

                        edcorreo.setText(accountName);
                    }
                }
                else if( resultCode== RESULT_CANCELED)
                    Log.d("asd","Entro a esta mierda");
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_loging, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            this.finish(); // finish activity
        } else {
            Toast.makeText(this, "Presiona Atras otra vez para salir.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }
}
