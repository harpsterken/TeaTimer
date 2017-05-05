package biz.bitinc.android.teatimer;

import java.util.ArrayList;
import java.io.IOException;
import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.media.RingtoneManager;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.content.SharedPreferences;

public class TeaTimerAlarm extends ListActivity {

    ArrayList<String> alarmList = new ArrayList<>();
    Uri alarmUri;
    MediaPlayer mPlayer = new MediaPlayer();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tea_timer_alarm);

        // Force portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create string array of alarm names and URI's
        alarmList = getAlarmList();
        if (alarmList != null) {
            // Create string array of alarm names (not the URI) from alarmList
            ArrayList<String> alarms = new ArrayList<>();
            for (int i = 0; i < alarmList.size(); i++) {
                String alarmListString = alarmList.get(i);
                int begIndex = alarmListString.lastIndexOf("/");
                if (begIndex != -1) {
                    String parsedAlarmName = alarmListString.substring(begIndex + 1);
                    alarms.add(i, parsedAlarmName);
                }
            }

            // Create string array of alarm URI's from alarmList
            ArrayList<String> alarmURIs = new ArrayList<>();
            for (int i = 0; i < alarmList.size(); i++) {
                String alarmListString = alarmList.get(i);
                int endIndex = alarmListString.lastIndexOf("/");
                if (endIndex != -1) {
                    String parsedAlarmUri = alarmListString.substring(0, endIndex);
                    alarmURIs.add(i, parsedAlarmUri);
                }
            }

            // Display list of alarm names and allow one to be selected
            ListView lv;
            lv = (ListView) findViewById(android.R.id.list);
            // Use custom layout so we can control the text size, etc.
            lv.setAdapter(new ArrayAdapter<>(this, R.layout.alarm_listview, alarms));

            // If an alarm was previously saved then set the alarm as selected in the list
            SharedPreferences teaTimerPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
            String alarmUriString = teaTimerPrefs.getString("alarmURI", null);
            if (alarmUriString != null) {
                // find saved alarm uri in alarmList
                int index = alarmURIs.indexOf(alarmUriString);
                if (index != -1) {
                    lv.setItemChecked(index, true);
                    alarmUri = Uri.parse(alarmUriString);
                }
            }

            lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    // Get the actual URI of the selected alarm sound and play the sound
                    String alarmListItem = alarmList.get(pos);
                    int endIndex = alarmListItem.lastIndexOf("/");
                    alarmUri = Uri.parse(alarmListItem.substring(0, endIndex));
                    mPlayer.reset();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mPlayer.setDataSource(getApplicationContext(), alarmUri);
                    } catch (Exception e) {
                        if (e instanceof IllegalArgumentException || e instanceof SecurityException ||
                                e instanceof IllegalStateException || e instanceof IOException) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        mPlayer.prepare();
                    } catch (Exception e1) {
                        if (e1 instanceof IllegalArgumentException || e1 instanceof SecurityException ||
                                e1 instanceof IllegalStateException || e1 instanceof IOException) {
                            e1.printStackTrace();
                        }
                    }
                    mPlayer.start();
                }
            });
        }
    }

    // Build list of notification sounds for user to choose from for tea alarm
    public ArrayList<String> getAlarmList() {
        RingtoneManager manager2 = new RingtoneManager(this);
        manager2.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor2 = manager2.getCursor();
        ArrayList<String> list = new ArrayList<>();
        while (cursor2.moveToNext()) {
            String id = cursor2.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor2.getString(RingtoneManager.URI_COLUMN_INDEX);
            String alarmName = cursor2.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            list.add(uri + "/" + id + "/" + alarmName);
        }
        return list;
    }

    // Click handler for alarmCancelBtn
    public void alarmCancelBtn(View view) {
        // If a sound was played, release the media player
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
        }
        finish();
    }

    // Click handler for alarmSaveBtn
    public void alarmSaveBtn(View view) {
        // If sound was selected, save its URI in shared preferences
        if (alarmUri != null) {
            SharedPreferences teaTimerPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
            SharedPreferences.Editor teaTimerPrefsEditor = teaTimerPrefs.edit();
            teaTimerPrefsEditor.putString("alarmURI", alarmUri.toString());
            teaTimerPrefsEditor.apply();
        }
        // If a sound was played, release the media player
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
        }
        finish();
    }
}