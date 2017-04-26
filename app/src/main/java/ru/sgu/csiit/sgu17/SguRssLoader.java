package ru.sgu.csiit.sgu17;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

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
        try {
            String httpResponse = NetUtils.httpGet(URL);
            res = RssUtils.parseRss(httpResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get HTTP response: " + e.getMessage(), e);
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "Failed to parse RSS: " + e.getMessage(), e);
        }
        return res;
    }

    @Override
    protected void onReset() {
        this.data = null;
    }
}
