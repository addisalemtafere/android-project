package com.tgurbanland.tgurbanland;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Spinner city, bidType, bidId;
    Button btnDownload;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "File Download", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        city = (Spinner) findViewById(R.id.spinner1);
        bidType = (Spinner) findViewById(R.id.spinner2);
        bidId = (Spinner) findViewById(R.id.spinner3);
        btnDownload = (Button) findViewById(R.id.download);

        List<String> cityList = new ArrayList<>();
        cityList.add("Mekele");
        cityList.add("Wikro");
        cityList.add("Shire");
        cityList.add("Sheraro");
        cityList.add("Axum");
        cityList.add("Alamata");
        cityList.add("Humera");
        cityList.add("Maychew");
        cityList.add("Adigrat");
        cityList.add("Adwa");

        List<String> bidTypeList = new ArrayList<>();
        bidTypeList.add("list of bid");
        bidTypeList.add("Notice");
        bidTypeList.add("Site location");
        bidTypeList.add("Rejected");
        bidTypeList.add("Winners");
        bidTypeList.add("Proclamation");


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cityList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(dataAdapter);

        ArrayAdapter<String> dataAdapterBid = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bidTypeList);
        dataAdapterBid.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bidType.setAdapter(dataAdapterBid);

        new SendRequest(new DataLoadedCallBack() {
            @Override
            public void onDataLoaded(List<String> strings) {
                ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, strings);
                dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bidId.setAdapter(dataAdapter3);
            }
        }).execute();

        download();


    }

    interface DataLoadedCallBack {
        void onDataLoaded(List<String> strings);
    }


    public class SendRequest extends AsyncTask<String, Void, List<String>> {

        DataLoadedCallBack callBack;

        public SendRequest(DataLoadedCallBack callBack) {
            this.callBack = callBack;
        }

        protected void onPreExecute() {
        }

        protected List<String> doInBackground(String... arg0) {
            List<String> bidIds = new ArrayList<>();
            try {

                URL url = new URL("http://www.tgurbanland.com/get_categories.php");

                JSONObject postDataParams = new JSONObject();


                postDataParams.put("name", "abhay");
                postDataParams.put("email", "abhay@gmail.com");

                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();

                    JSONObject json = new JSONObject(sb.toString());


                    JSONArray jsonArray = json.optJSONArray("categories");

                    //Iterate the jsonArray and print the info of JSONObjects
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);


                        String name = jsonObject.optString("id").toString();

                        bidIds.add(name);

                    }

                    Log.i(TAG, "sb: " + sb.toString());
                    Log.i(TAG, "json: " + json.toString());
                    Log.i(TAG, "listids: " + bidIds.toString());

                    return bidIds;

                }
            } catch (Exception e) {
                return null;
            }
            return null;

        }

        @Override
        protected void onPostExecute(List<String> result) {
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();

            callBack.onDataLoaded(result);


        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    public void download() {

        city = (Spinner) findViewById(R.id.spinner1);
        bidType = (Spinner) findViewById(R.id.spinner2);
        bidId = (Spinner) findViewById(R.id.spinner3);
        btnDownload = (Button) findViewById(R.id.download);

        btnDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,
                        "OnClickListener : " +
                                "\nSpinner 1 : " + String.valueOf(city.getSelectedItem()) +
                                "\nSpinner 2 : " + String.valueOf(bidType.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
                startDownloading("http://www.tgurbanland.com/lease/uploads/48050-59.pdf");
            }

        });

    }

    private long mDownloadEnqueueId;

    public void startDownloading(String url) {
//        if (!PermissionUtil.isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            PermissionUtil.requestPermission(this, PermissionUtil.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_rationale_write_external_storage), false);
//            Log.e(TAG, "startDownloading: " + ">>>>>> 111111");
//            return;
//        }
        Log.e(TAG, "startDownloading: " + ">>>>>> 222222");

//        mLoadToast.show();
//        String url = current.getValue(String.class);
        Log.e(TAG, ">>>> url: " + url);
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                //.setAllowedOverRoaming(false)
                .setTitle("Downloading..")
                .setDescription("Printable Antibiogram Data")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LatestAntibiogram.pdf");
        mDownloadEnqueueId = mDownloadManager.enqueue(request);
    }

    private DownloadManager mDownloadManager;
    @NonNull
    BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(mDownloadEnqueueId);
                Cursor c = mDownloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String notificationTitle = "pdf";
                        String notificationText = "file_downloaded";
                        showDownloadNotification(notificationTitle, notificationText);
//                        Snackbar.mak(mMainContent, notificationText, CustomView.SnackBarStyle.SUCCESS).setAction(getString(R.string.open), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                                startActivity(i);
//                            }
//                        }).show();
                    }
                }
            }
        }
    };

    private void showDownloadNotification(String contentTitle, String contentText) {

        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)//
                .setSmallIcon(R.mipmap.ic_launcher)//
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))//
                .setContentTitle(contentTitle)//
                .setContentText(contentText)
                .setLights(ContextCompat.getColor(this, R.color.colorPrimary), 2000, 5000)//
                .setTicker(contentTitle)//
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mBuilder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(4, mBuilder.build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
