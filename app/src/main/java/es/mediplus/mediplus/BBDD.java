package es.mediplus.mediplus;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by josu on 16/03/16.
 */
public class BBDD extends ContentProvider
{
    private static final String DATABASE_NAME = "BaseLocalMedicamentos";
    private static final String MEDI_TABLE = "medicamentos";
    private static final int DATABASE_VERSION = 4;
    private static final String  AUTHORITY = "es.mediplus.mediplus.BBDD";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/medicamentos");
    public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/medicamentos/");
    private static final UriMatcher uriMatcher;

    //Campos
    public static final String ID_LOCAL = "id_local";
    public static final String ID_MEDICAMENTO = "id_medicamento";
    public static final String FLAG = "flag";
    public static final String NOMBRE = "nombre";
    public static final String INICIO = "inicio";
    public static final String FINAL = "final";
    public static final String HORA_INICIO = "_hora_inicio";
    public static final String FREQ_HORARIA = "freq_horaria";
    public static final String FREQ_DIARIA = "freq_diaria";
    public static final String CANTIDAD_TOTAL = "cantidad_total";
    public static final String FECHA_REPOSICION = "fecha_reposicion";
    public static final String NUM_ALARMAS = "num_alarmas";
    public static final String TIMESTAMP = "timestamp";

    private static final HashMap<String, String> mMap;
    private SQLiteDatabase db;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            createTables(db);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            Log.w("CalendarProvider", "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS events");
            onCreate(db);
        }

        //Crear la tabla (meter los campos)
        @SuppressLint("SQLiteString")
        private void createTables(SQLiteDatabase db){
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MEDI_TABLE + "(" + ID_LOCAL + " integer primary key autoincrement, "
                    + ID_MEDICAMENTO + " integer, " +
                    FLAG + " integer, " + NOMBRE + " STRING, " + INICIO + " STRING, "
                    + FINAL + " STRING, "+ HORA_INICIO + " STRING, " + FREQ_HORARIA + " STRING, "
                    + FREQ_DIARIA + " integer, "+ CANTIDAD_TOTAL + " integer, "
                    + FECHA_REPOSICION + " STRING, "+ NUM_ALARMAS + " integer, " + TIMESTAMP +" STRING);");
        }
    }

    @Override
    public boolean onCreate()
    {
        Context context = getContext();
        DatabaseHelper DBHelper = new DatabaseHelper(context);
        db = DBHelper.getWritableDatabase();
        return (db != null);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(MEDI_TABLE);

        if(uriMatcher.match(uri) == 1){
            sqlBuilder.setProjectionMap(mMap);
        }
        //ES PARA DEFINIR QUE QUERIES VAS A HACER

        else if(uriMatcher.match(uri) == 2){
            sqlBuilder.setProjectionMap(mMap);
            sqlBuilder.appendWhere(ID_LOCAL + "=?");///////////////////////////////////////////////*ID_MEDICAMENTO POR ID_LOCAL*/
            selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[]{uri.getLastPathSegment()});
        }/*else if(uriMatcher.match(uri) == 3){
            sqlBuilder.setProjectionMap(mMap);
            sqlBuilder.appendWhere(START + ">=? OR ");
            sqlBuilder.appendWhere(END + "<=?");
            List<String> list = uri.getPathSegments();
            String start = list.get(1);
            String end = list.get(2);
            selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,new String[] {start,end});
        }*/
        Cursor c = sqlBuilder.query(db, projection, selection, selectionArgs,null,null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        long rowID = db.insert(MEDI_TABLE,null, values);
        Uri _uri = null;
        if(rowID > 0)
        {
            _uri = ContentUris.withAppendedId(CONTENT_ID_URI_BASE, rowID);
            getContext().getContentResolver().notifyChange(uri,null);

        }
        else
        {
            throw new SQLException("Failed to insert row into " + uri);
        }
        return _uri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        int num = uriMatcher.match(uri);
        if(num == 1){
            count = db.delete(MEDI_TABLE, selection,selectionArgs);
        }
        //PARA HACER DIFERENTES DELETES

        /*else if(num == 2){
            String id = uri.getPathSegments().get(1);
            count = db.delete(MEDI_TABLE, ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" +
                            selection + ')' : ""),
                    selectionArgs);
        }*/
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override


    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        int count = 0;
        int num = uriMatcher.match(uri);
        if(num == 1){
            count = db.update(MEDI_TABLE, values, selection, selectionArgs);
        }

        //esto es como en los anteriores para hacer queries concretas

        /*

        else if(num == 2){
            count = db.update(MEDI_TABLE, values, ID + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" +
                            selection + ')' : ""),
                    selectionArgs);
        }else{
            throw new IllegalArgumentException(
                    "Unknown URI " + uri);
        }*/


        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,MEDI_TABLE,1);
        uriMatcher.addURI(AUTHORITY,MEDI_TABLE + "/#",2);
        uriMatcher.addURI(AUTHORITY, MEDI_TABLE+"/#/#", 3);

        mMap = new HashMap<>();
        mMap.put(ID_LOCAL,ID_LOCAL);
        mMap.put(ID_MEDICAMENTO,ID_MEDICAMENTO);
        mMap.put(FLAG, FLAG);
        mMap.put(NOMBRE, NOMBRE);
        mMap.put(INICIO, INICIO);
        mMap.put(FINAL, FINAL);
        mMap.put(HORA_INICIO, HORA_INICIO);
        mMap.put(FREQ_HORARIA, FREQ_HORARIA);
        mMap.put(FREQ_DIARIA,FREQ_DIARIA);
        //mMap.put(DIAS_SEMANA, DIAS_SEMANA);//ESTO YA SOBRA...
        mMap.put(CANTIDAD_TOTAL, CANTIDAD_TOTAL);
        mMap.put(FECHA_REPOSICION, FECHA_REPOSICION);
        mMap.put(NUM_ALARMAS, NUM_ALARMAS);
        mMap.put(TIMESTAMP, TIMESTAMP);
    }
}