package ru.sgu.csiit.sgu17;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class NewsListActivity extends Activity
        implements NewsListFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list_activity);
    }

    @Override
    public void OnArticleClicked(Article article) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            PreviewFragment fragment = new PreviewFragment();
            fragment.getArguments().putString("url", article.link);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            PreviewFragment f = (PreviewFragment) getFragmentManager()
                    .findFragmentById(R.id.preview_fragment);
            f.getArguments().putString("url", article.link);
            f.reload();
        }
    }
}
