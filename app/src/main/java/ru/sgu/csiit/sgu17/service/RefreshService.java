package ru.sgu.csiit.sgu17.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import ru.sgu.csiit.sgu17.Article;
import ru.sgu.csiit.sgu17.NetUtils;
import ru.sgu.csiit.sgu17.NewsListActivity;
import ru.sgu.csiit.sgu17.R;
import ru.sgu.csiit.sgu17.RssUtils;
import ru.sgu.csiit.sgu17.db.SguDbContract;
import ru.sgu.csiit.sgu17.db.SguDbHelper;

public class RefreshService extends Service {

    public static final String REFRESH_ACTION = "ru.sgu.csiit.sgu17.service" +
            ".RefreshService.ACTION_REFRESH";

    private static final String LOG_TAG = "RefreshService";
    private static final String URL = "http://www.sgu.ru/news.xml";

    private Thread refreshThread;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    loadData();
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        if (intent != null && refreshThread == null) {
            this.refreshThread = new Thread(refreshRunnable);
            refreshThread.start();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }

    private void onPostRefresh() {
        Log.d(LOG_TAG, "data refreshed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(REFRESH_ACTION);
        sendBroadcast(broadcastIntent);
        sendDataRefreshedNotification();
    }

    private void loadData() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                showInProgressNotification();
            }
        });

        List<Article> netData = null;
        try {
            String httpResponse = NetUtils.httpGet(URL);
            netData = RssUtils.parseRss(httpResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get HTTP response: " + e.getMessage(), e);
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "Failed to parse RSS: " + e.getMessage(), e);
        }
        // Load object into the database.
        SQLiteDatabase db = new SguDbHelper(RefreshService.this).getWritableDatabase();
        db.beginTransaction();
        try {
            if (netData != null) {
                for (Article a : netData) {
                    ContentValues cv = new ContentValues();
                    cv.put(SguDbContract.COLUMN_GUID, a.guid);
                    cv.put(SguDbContract.COLUMN_TITLE, a.title);
                    cv.put(SguDbContract.COLUMN_DESCRIPTION, a.description);
                    cv.put(SguDbContract.COLUMN_LINK, a.link);
                    cv.put(SguDbContract.COLUMN_PUBDATE, a.pubDate);
                    long insertedId = db.insertWithOnConflict(SguDbContract.TABLE_NAME,
                            null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                    if (insertedId == -1L)
                        Log.i(LOG_TAG, "skipped article guid=" + a.guid);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                hideInProgressNotification();
                onPostRefresh();
            }
        });
    }

    private void sendDataRefreshedNotification() {
        Intent startIntent = new Intent(this, NewsListActivity.class);
        PendingIntent notificationIntent = PendingIntent.getActivity(
                this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("SGU RSS data refreshed")
                .setContentText("Press notification to open")
                .setContentIntent(notificationIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.sgu_main)
                .build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, notification);
    }

    private void showInProgressNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("SGU RSS data is refreshing")
                .setContentText("Wait until complete")
                .setOngoing(true)
                .setSmallIcon(R.drawable.sgu_main)
                .build();
        startForeground(1, notification);
    }

    private void hideInProgressNotification() {
        stopForeground(false);
    }
}
