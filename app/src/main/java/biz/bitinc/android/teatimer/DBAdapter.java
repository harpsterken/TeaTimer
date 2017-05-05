package biz.bitinc.android.teatimer;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

class DBAdapter {

    // Create local variables
    private final Context context;
    private final DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    // Constructor for class
    DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    // Database helper class
    private class DatabaseHelper extends SQLiteOpenHelper {

        // Constructor for class
        DatabaseHelper(Context context) {
            super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        }

        // OnCreate method - create table from sql.xml in res/raw
        public void onCreate(SQLiteDatabase db) {
            String s;
            try {
                InputStream in = context.getResources().openRawResource(R.raw.sql);
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(in, null);
                NodeList statements = doc.getElementsByTagName("statement");
                for (int i = 0; i < statements.getLength(); i++) {
                    s = statements.item(i).getChildNodes().item(0).getNodeValue();
                    db.execSQL(s);
                }
            } catch (Throwable t) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        // onUpgrade method - drop tables and recreate them
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS teas");
            onCreate(db);
        }
    }

    // Open the database
    void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }

    // Close the database   
    void close() {
        DBHelper.close();
    }

    // Retrieve all the tea types
    Cursor getAllTypes() throws SQLException {
        Cursor mCursor =
                db.query(true, Constants.DATABASE_TABLE, new String[]{
                        Constants.TEAS_TYPE}, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // Retrieve a specific tea type
    Cursor getType(String type) throws SQLException {
        Cursor mCursor =
                db.query(true, Constants.DATABASE_TABLE, new String[]{
                                Constants.TEAS_ROWID, Constants.TEAS_TYPE, Constants.TEAS_DESCRIPTION, Constants.TEAS_STEEPTEMP,
                                Constants.TEAS_STEEPTIME, Constants.TEAS_ALARMSOUND},
                        Constants.TEAS_TYPE + "= '" + type + "'", null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
