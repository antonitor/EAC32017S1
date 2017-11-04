package cat.xtec.ioc.eac3_2017s1.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cat.xtec.ioc.eac3_2017s1.Data.MediaContract.MediaTable;

/**
 * Classe que hereta de SQLiteOpenHelper i gestiona la creació de les taules
 * de la base de dades i permet recuperarne un objecte SQLiteDataBase per
 * tal d'interaccionar-hi
 */
public class MediaDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "media.db";
    private static final int DATABASE_VERSION = 1;


    public MediaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MEDIA_TABLE = "CREATE TABLE " + MediaTable.TABLE_NAME + " (" +
                MediaTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MediaTable.COLUMN_FILE_NAME + " TEXT NOT NULL, " +
                MediaTable.COLUMN_PATH + " TEXT NOT NULL, " +
                MediaTable.COLUMN_IS_VIDEO + " INTEGER NOT NULL," +
                MediaTable.COLUMN_LATITUDE + " FLOAT NOT NULL, " +
                MediaTable.COLUMN_LONGITUDE + " FLOAT NOT NULL " +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_MEDIA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MediaTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
