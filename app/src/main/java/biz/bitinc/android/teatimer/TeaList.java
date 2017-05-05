package biz.bitinc.android.teatimer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Menu;

public class TeaList extends ListActivity {

    static String selectedType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tea_list);


        // Force portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Create string array of teas
        String[] Teas;
        Teas = getTeaList();

        // Display list of teas and allow one to be selected
        if (Teas != null) {
            ListView lv;
            lv = (ListView) findViewById(android.R.id.list);
            // Use custom layout so we can control the text size, etc.
            lv.setAdapter(new ArrayAdapter<>(this, R.layout.tea_list_listview, Teas));
            // If tea selected then start new activity - TeaDetail
            lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedType = (String) ((TextView) view).getText();
                    selectedType = selectedType.trim();
                    Intent intent = new Intent(Constants.INTENT_ACTION_TEA_DETAIL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tea_detail, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(this, TeaTimerAbout.class));
                return true;
            case R.id.action_teaalarm:
                startActivity(new Intent(this, TeaTimerAlarm.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Query database and retrieve names of all teas
    String[] getTeaList() {
        DBAdapter db = new DBAdapter(this);
        db.close();
        db.open();
        Cursor cur = db.getAllTypes();
        int i = 0;
        String[] workArray = new String[100];
        String type;
        if (cur.moveToFirst()) {
            type = cur.getString(cur.getColumnIndex(Constants.TEAS_TYPE));
            workArray[i] = type;
            i++;
        }
        while (cur.moveToNext()) {
            type = cur.getString(cur.getColumnIndex(Constants.TEAS_TYPE));
            workArray[i] = type;
            i++;
        }
        db.close();
        // Manipulate array to strip nulls and return clean string array
        List<String> list = new ArrayList<>(Arrays.asList(workArray));
        list.removeAll(Collections.<String>singletonList(null));
        String[] teaArray;
        teaArray = list.toArray(new String[0]);
        return teaArray;
    }
}