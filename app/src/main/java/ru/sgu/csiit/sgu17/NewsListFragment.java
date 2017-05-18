package ru.sgu.csiit.sgu17;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.sgu.csiit.sgu17.service.RefreshService;

public class NewsListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Article>> {

    private static final String LOG_TAG = "NewsListActivity";

    private final RefreshBroadcastReceiver refreshBroadcastReceiver = new RefreshBroadcastReceiver();
    private final ArrayList<Article> data = new ArrayList<>();
    private NewsItemAdapter dataAdapter;

    private final class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isResumed()) {
                getActivity().getLoaderManager().restartLoader(0, null, NewsListFragment.this);
                Toast.makeText(getActivity(), "Data refreshed", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public interface Listener {
        void OnArticleClicked(Article article);
        void OnPreferencesClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dataAdapter = new NewsItemAdapter(getActivity(), data);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.news_list_fragment, container, false);

        ListView newsList = (ListView) v.findViewById(R.id.news_list);
        newsList.setAdapter(dataAdapter);
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = (Article) parent.getItemAtPosition(position);
                if (isResumed()) {
                    Listener l = (Listener) getActivity();
                    l.OnArticleClicked(article);
                }
            }
        });

        Button refreshBtn = (Button) v.findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getActivity(), RefreshService.class);
                getActivity().startService(serviceIntent);
            }
        });

        v.findViewById(R.id.prefs_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Listener) getActivity()).OnPreferencesClicked();
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(RefreshService.REFRESH_ACTION);
        getActivity().registerReceiver(refreshBroadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(refreshBroadcastReceiver);
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");
        return new SguRssLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> loaderData) {
        Log.d(LOG_TAG, "onLoadFinished " + loader.hashCode());
        data.clear();
        data.addAll(loaderData);
        dataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        Log.d(LOG_TAG, "onLoaderReset " + loader.hashCode());
    }

}
