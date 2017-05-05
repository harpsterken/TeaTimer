package biz.bitinc.android.teatimer;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

@SuppressWarnings("deprecation")
class Utilities {

    static Spanned formatHTML(String string) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(string);
        }
    }
}
