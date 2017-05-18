package ru.sgu.csiit.sgu17;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class PrefsFragment extends Fragment {

    private Switch wifiOnlySwitch;
    private Switch notificationSwitch;
    private Switch periodicUpdates;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.prefs_fragment, container, false);
        this.wifiOnlySwitch = (Switch) v.findViewById(R.id.wi_fi_only_sw);
        this.notificationSwitch = (Switch) v.findViewById(R.id.notifications_sw);
        this.periodicUpdates = (Switch) v.findViewById(R.id.periodic_updates_sw);
        init();
        return v;
    }

    private void init() {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        wifiOnlySwitch.setChecked(prefs.getBoolean("wifiOnly", false));
        wifiOnlySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onWiFiSwitched(isChecked);
            }
        });

        notificationSwitch.setChecked(prefs.getBoolean("notifications", true));
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onNotificationsSwitched(isChecked);
            }
        });

        periodicUpdates.setChecked(prefs.getBoolean("periodicUpdates", true));
        periodicUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onPeriodicUpdatesSwitched(isChecked);
            }
        });
    }

    private void onWiFiSwitched(boolean checked) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("wifiOnly", checked);
        editor.apply();
    }

    private void onNotificationsSwitched(boolean checked) {
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putBoolean("notifications", checked)
                .apply();
    }

    private void onPeriodicUpdatesSwitched(boolean checked) {
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putBoolean("periodicUpdates", checked)
                .apply();
    }

}
