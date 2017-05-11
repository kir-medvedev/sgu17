package ru.sgu.csiit.sgu17;


import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.sgu.csiit.sgu17.db.SguDbContract;
import ru.sgu.csiit.sgu17.db.SguDbHelper;

final class SguRssLoader extends AsyncTaskLoader<List<Article>> {

    private static final String URL = "http://www.sgu.ru/news.xml";
    private static final String LOG_TAG = "SguRssLoader";

    private List<Article> data;

    SguRssLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public List<Article> loadInBackground() {
        List<Article> res = null;
        List<Article> netData = null;

        try {
            String httpResponse = NetUtils.httpGet(URL);
            netData = RssUtils.parseRss(httpResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get HTTP response: " + e.getMessage(), e);
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "Failed to parse RSS: " + e.getMessage(), e);
        }

        SQLiteDatabase db = new SguDbHelper(getContext()).getWritableDatabase();

        // Load object into the database.

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
                        Log.d(LOG_TAG, "skipped article guid=" + a.guid);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Load object from the database.

        Cursor cursor = db.query(SguDbContract.TABLE_NAME, new String[]{
                SguDbContract.COLUMN_TITLE,
                SguDbContract.COLUMN_DESCRIPTION,
                SguDbContract.COLUMN_PUBDATE,
                SguDbContract.COLUMN_LINK
        }, null, null, null, null, SguDbContract.COLUMN_PUBDATE + " DESC");
        try {
            res = new ArrayList<>();
            while (cursor.moveToNext()) {
                Article article = new Article();
                article.title = cursor.getString(0);
                article.description = cursor.getString(1);
                article.pubDate = cursor.getString(2);
                article.link = cursor.getString(3);
                res.add(article);
            }
        } finally {
            cursor.close();
        }

        return res;
    }

    @Override
    protected void onReset() {
        this.data = null;
    }
}
