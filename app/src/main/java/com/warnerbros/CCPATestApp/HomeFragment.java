package com.warnerbros.CCPATestApp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    boolean dns;
    private String country, region;
    AdView mAdView;
    TextView homeText, settingsText;
    SharedPreferences settings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAdView = (AdView) v.findViewById(R.id.testAd);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("EF0600F38A7C201000993B6569A7051D").build();
        mAdView.loadAd(adRequest);

        homeText = v.findViewById(R.id.homeText);
        settingsText = v.findViewById(R.id.settingsText);
        settings = this.getActivity().getSharedPreferences("PREFS", MODE_PRIVATE);

        dns = settings.getBoolean("DNS", false);
        if (dns) {
            settingsText.setText("DNS enabled");
        } else {
            settingsText.setText("DNS disabled");
        }

        getData getD = new getData();
        getD.execute();

            return v;
    }


    public class getData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://ip-api.com/json/");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            homeText = (TextView) getView().findViewById(R.id.homeText);
            Gson g =  new Gson();
            GeoResponse res = g.fromJson(result, GeoResponse.class);
            country = res.getCountry();
            region = res.getRegionName();
            homeText.setText(country + ", " + region);
        }

    }

}
