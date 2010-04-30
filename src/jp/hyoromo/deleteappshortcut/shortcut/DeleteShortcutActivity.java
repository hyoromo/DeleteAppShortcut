package jp.hyoromo.deleteappshortcut.shortcut;

import java.util.ArrayList;
import java.util.List;

import jp.hyoromo.deleteappshortcut.ManualActivity;
import jp.hyoromo.deleteappshortcut.R;
import jp.hyoromo.deleteappshortcut.shortcut.data.AppInfo;
import jp.hyoromo.deleteappshortcut.shortcut.data.GridRowData;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeleteShortcutActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final int REQUEST_SET_MANUAL = 1;
    private static final int RESULT_EXIT = 9;
    private static GridView mGrid;
    private static AppsAdapter mAdapter;
    private static ArrayList<AppInfo> mAppList;
    private static LinearLayout.LayoutParams mParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.grid);
        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mProgressDialog;

        LoadTask() {
            mProgressDialog = new ProgressDialog(DeleteShortcutActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage(getResources().getString(R.string.shortcut_progressdialog_title));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... v) {
            // 画面サイズを取得
            int wallpaperSizeY = getWallpaperDesiredMinimumHeight();
            if (wallpaperSizeY >= 800) {
                mParams = new LinearLayout.LayoutParams(60, 60);
            } else if (wallpaperSizeY >= 480) {
                mParams = new LinearLayout.LayoutParams(44, 44);
            } else {
                mParams = new LinearLayout.LayoutParams(32, 32);
            }

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            PackageManager manager = getPackageManager();
            List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
            mAppList = new ArrayList<AppInfo>();
            for (ResolveInfo info : apps) {
                AppInfo appInfo = new AppInfo();
                appInfo.title = info.loadLabel(manager);
                appInfo.packageName = info.activityInfo.applicationInfo.packageName;
                appInfo.icon = info.activityInfo.loadIcon(manager);
                mAppList.add(appInfo);
            }
            mGrid = (GridView) findViewById(R.id.grid);
            mGrid.setOnItemClickListener(DeleteShortcutActivity.this);
            mAdapter = new AppsAdapter(getApplicationContext());
            onProgressUpdate();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... v) {
        }

        @Override
        protected final void onPostExecute(Void v) {
            mGrid.setAdapter(mAdapter);
            mProgressDialog.dismiss();
        }
    }

    public class AppsAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        public AppsAdapter(Context context) {
            mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            GridRowData row;

            if (v == null || v.getTag() == null) {
                v = mInflater.inflate(R.layout.grid_row, null);
                row = new GridRowData();
                row.txt = (TextView) v.findViewById(R.id.grid_row_txt);
                row.img = (ImageView) v.findViewById(R.id.grid_row_img);
                row.img.setLayoutParams(mParams);

                v.setTag(row);
            } else {
                row = (GridRowData) v.getTag();
            }
            AppInfo info = getItem(position);
            row.txt.setText(info.title);
            row.img.setImageDrawable(info.icon);

            return v;
        }

        public final int getCount() {
            return mAppList.size();
        }

        public final AppInfo getItem(int position) {
            return mAppList.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfo appInfo = (AppInfo) parent.getItemAtPosition(position);

        // アイコン作成(めんどうなのでやめた)
        //        BitmapDrawable bd = (BitmapDrawable) appInfo.icon;
        //        Bitmap bmpBack = bd.getBitmap();
        //        Bitmap bmpFront = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        //        Canvas canvas = new Canvas(bmpBack);
        //        int widthSize = bmpBack.getWidth() / 2;
        //        canvas.drawBitmap(bmpFront, widthSize, 0, null);
        //        BitmapDrawable drawable = new BitmapDrawable(bmpBack);
        //        drawable.draw(canvas);

        // ショートカット作成
        Intent shortcutIntent = new Intent("android.intent.action.DELETE");
        shortcutIntent.setData(Uri.parse("package:" + appInfo.packageName));

        // 作成したショートカットを設定するIntent。ここでショートカット名とアイコンも設定。
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.title);

        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * menuボタン作成
     */
    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shortcut, menu);
        return true;
    }

    /**
     * meny押下時のイベント
     */
    @Override
    public final boolean onOptionsItemSelected(MenuItem items) {
        switch (items.getItemId()) {
        case R.id.menu_shortcut_manual:
            Intent intent = new Intent(this, ManualActivity.class);
            startActivityForResult(intent, REQUEST_SET_MANUAL);
            return true;
        case R.id.menu_shortcut_exit:
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_EXIT) {
            finish();
        } else if (resultCode == RESULT_CANCELED) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mParams = null;
        mAppList = null;
        mAdapter = null;
        mGrid = null;
    }
}