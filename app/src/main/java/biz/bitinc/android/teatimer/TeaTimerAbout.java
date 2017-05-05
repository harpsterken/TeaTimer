package biz.bitinc.android.teatimer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.content.res.Resources;

public class TeaTimerAbout extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tea_timer_about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set the application version verbiage
        Resources res = getResources();
        TextView text = (TextView) this.findViewById(R.id.app_version);
        text.setText(String.format(res.getString(R.string.about_version), BuildConfig.VERSION_NAME));
    }
}