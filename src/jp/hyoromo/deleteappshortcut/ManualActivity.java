package jp.hyoromo.deleteappshortcut;

import java.util.Locale;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class ManualActivity extends Activity {
    private static final int RESULT_EXIT = 9;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_layout);

        // AppVersion取得
        String ver = "";
        try {
            ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }
        TextView txt = (TextView) findViewById(R.id.manual_txt_ver);
        txt.setText("ver " + ver);

        // WebView
        WebView web = (WebView) findViewById(R.id.manual_web_separator);
        web.loadData("<body bgcolor=\"#FFFFFF\"><hr /></body>", "text/html", "UTF-8");

        web = (WebView) findViewById(R.id.manual_web_mes);
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            web.loadUrl("file:///android_asset/index_ja.html");
        } else {
            web.loadUrl("file:///android_asset/index.html");
        }
    }

    /**
     * menuボタン作成
     */
    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manual, menu);
        return true;
    }

    /**
     * meny押下時のイベント
     */
    @Override
    public final boolean onOptionsItemSelected(MenuItem items) {
        switch (items.getItemId()) {
        case R.id.menu_manual_exit:
            setResult(RESULT_EXIT);
            finish();
            return true;
        }
        return false;
    }
}