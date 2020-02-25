package com.warnerbros.CCPATestApp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserSettingsFragment extends Fragment {

    String settings[], descriptions[];
    RecyclerView settingsRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_settings, container, false);
        Toolbar toolbar = v.findViewById(R.id.app_bar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText("Settings");
        title.setBackground(null);

        settingsRecycler = v.findViewById(R.id.settingsRecycler);

        settings = getResources().getStringArray(R.array.settings);
        descriptions = getResources().getStringArray(R.array.descriptions);

        SettingsAdapter settingsAdapter = new SettingsAdapter(getContext().getApplicationContext(), settings, descriptions);
        settingsRecycler.setAdapter(settingsAdapter);
        settingsRecycler.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

        return v;
    }


}
