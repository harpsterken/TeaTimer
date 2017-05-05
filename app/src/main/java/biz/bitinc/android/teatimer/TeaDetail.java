package biz.bitinc.android.teatimer;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TeaDetail extends Activity {

    // Define class scope variables
    int state = 0;
    long startTime = 0;
    private int mins;
    private int secs;
    private String displayTime = null;

    // Define local database related variables
    private String TEAS_TYPE = null;
    private String TEAS_DESC = null;
    private long TEAS_TEMP = 0;
    private long TEAS_TIME = 0;
    String TEAS_ALARM = null;

    // Define local variables for activity
    TextView TeasType;
    TextView TeasDesc;
    TextView TeasTemp;
    private TextView TeasTime;
    private Button startstopbtn;
    Uri alarmUri;

    // Define TeaCounter 
    private TeaCounter counter;

    // Define media player
    private final Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    private final MediaPlayer player = new MediaPlayer();

    // Define Wake Lock
    private PowerManager.WakeLock mWakeLock;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tea_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get the timer alarm sound URI
        SharedPreferences teaTimerPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String alarmUriString = teaTimerPrefs.getString("alarmURI", null);

        if (alarmUriString != null) {
            alarmUri = Uri.parse(alarmUriString);
        } else {
            alarmUri = defaultAlarmUri;
        }

        // Create Power Manager and initialize wake lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Tea Timer");

        // Instantiate new count down timer
        counter = new TeaCounter(((int) TEAS_TIME * 1000), 100);

        // Link local variables to XML layout identifiers
        TeasType = (TextView) findViewById(R.id.TeasType);
        TeasDesc = (TextView) findViewById(R.id.TeasDesc);
        TeasTemp = (TextView) findViewById(R.id.TeasSteepTemp);
        TeasTime = (TextView) findViewById(R.id.TeasSteepTime);
        startstopbtn = (Button) findViewById(R.id.StartStopBtn);

        // Query database and populate string of teas
        getTeaDetail(TeaList.selectedType);

        // If Teas_Type = null then quit the activity (only happens when app is shutdown while running on this activity)
        if (TEAS_TYPE == null) {
            finish();
        }

        // Calculate celsius temperature
        float celsius = TEAS_TEMP;
        int celInt = 0;
        if (celsius > 0) {
            celsius = ((celsius - 32) / 9) * 5;
            celInt = (int) celsius;
        }

        // Format database time in seconds to mm:ss
        mins = (int) TEAS_TIME / 60;
        secs = (int) TEAS_TIME - (mins * 60);
        displayTime = Integer.toString(mins);
        if (secs < 10) {
            displayTime = displayTime + ":0" + Integer.toString(secs);
        } else {
            displayTime = displayTime + ":" + Integer.toString(secs);
        }

        // Populate regular textview display fields
        TeasType.setText(Utilities.formatHTML(getString(R.string.teatype) + " " + TEAS_TYPE));
        TeasDesc.setText(TEAS_DESC);
        TeasTemp.setText(Utilities.formatHTML(getString(R.string.steeptemp) + " " + (Long.toString(TEAS_TEMP)) + (char) 186 + " F  /  " + (Integer.toString(celInt)) + (char) 186 + " C"));
        TeasTime.setText(displayTime);

        // Create audio player
        createPlayer();
    }

    // If back key pressed, cancel any running counter otherwise it will get orphaned
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();

            try {
                mWakeLock.release();
            } catch (Exception e) {
                // TODO
            }
            counter.cancel();
        }
        return super.onKeyDown(keyCode, event);
    }

    // Start/Stop button handler
    public void startstopbtn_handler(View view) {
        switch (state) {
            // start
            case 0:
                try {
                    mWakeLock.acquire();
                } catch (Exception e) {
                    // TODO
                }
                counter = new TeaCounter(((int) TEAS_TIME * 1000), 100);
                startTime = System.currentTimeMillis();
                counter.start();
                startstopbtn.setText(R.string.stop);
                state = 1;
                break;
            // stop
            case 1:
                if (player.isPlaying()) {
                    player.stop();
                }
                try {
                    mWakeLock.release();
                } catch (Exception e) {
                    // TODO
                }
                counter.cancel();
                startstopbtn.setText(R.string.reset);
                state = 2;
                break;
            // reset
            case 2:
                mins = (int) TEAS_TIME / 60;
                secs = (int) TEAS_TIME - (mins * 60);
                displayTime = Integer.toString(mins);
                if (secs < 10) {
                    displayTime = displayTime + ":0" + Integer.toString(secs);
                } else {
                    displayTime = displayTime + ":" + Integer.toString(secs);
                }
                TeasTime.setText(displayTime);
                startstopbtn.setText(R.string.start);
                state = 0;
                break;
        }
    }

    // Query database and retrieve information for selected tea
    void getTeaDetail(String type) {
        DBAdapter db = new DBAdapter(this);
        db.close();
        db.open();
        Cursor cur = db.getType(type);
        if (cur.moveToFirst()) {
            TEAS_TYPE = cur.getString(cur.getColumnIndex(Constants.TEAS_TYPE));
            TEAS_DESC = cur.getString(cur.getColumnIndex(Constants.TEAS_DESCRIPTION));
            TEAS_TEMP = cur.getLong(cur.getColumnIndex(Constants.TEAS_STEEPTEMP));
            TEAS_TIME = cur.getLong(cur.getColumnIndex(Constants.TEAS_STEEPTIME));
            TEAS_ALARM = cur.getString(cur.getColumnIndex(Constants.TEAS_ALARMSOUND));
        }
        db.close();
    }

    // Create an audio player to use to play alert when tea is done steeping
    void createPlayer() {
        try {
            player.setDataSource(getBaseContext(), alarmUri);
        } catch (Exception e1) {
            if (e1 instanceof IllegalArgumentException || e1 instanceof SecurityException ||
                    e1 instanceof IllegalStateException || e1 instanceof IOException) {
                e1.printStackTrace();
            } else {
                throw new RuntimeException(e1);
            }
        }
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
        }
    }


    // Extend class CountDowntimer
    public class TeaCounter extends CountDownTimer {

        TeaCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            // Bring app to front when timer goes off
            Intent intent = new Intent(Constants.INTENT_ACTION_TEA_DETAIL);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            // Set displayed text and play alarm
            TeasTime.setText(R.string.tea_time);
            try {
                player.prepare();
                player.start();
            } catch (Exception e) {
                if (e instanceof IllegalStateException || e instanceof IOException) {
                    e.printStackTrace();
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        public void onTick(long millisUntilFinished) {
            int remaining = (int) (millisUntilFinished / 1000);
            mins = remaining / 60;
            secs = remaining - (mins * 60);
            displayTime = Integer.toString(mins);
            if (secs < 10) {
                displayTime = displayTime + ":0" + Integer.toString(secs);
            } else {
                displayTime = displayTime + ":" + Integer.toString(secs);
            }
            TeasTime.setText(displayTime);
        }
    }
}