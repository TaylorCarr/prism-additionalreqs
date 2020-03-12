package com.warnermedia.PrivacyTestApp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static android.content.Context.MODE_PRIVATE;

public class InfoFragment extends Fragment {


    private boolean dns;
    private AdView mAdView;
    private SharedPreferences settings;
    private Bundle networkExtrasBundle;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_info, container, false);
            Toolbar toolbar = v.findViewById(R.id.app_bar);
            TextView title = toolbar.findViewById(R.id.toolbar_title);
            title.setText("Information");
            title.setBackground(null);

            mAdView = (AdView) v.findViewById(R.id.infoAd);
            networkExtrasBundle = new Bundle();
            settings = this.getActivity().getSharedPreferences("PREFS", MODE_PRIVATE);
            dns = settings.getBoolean("DNS", false);
            System.out.println("DNS IS CURRENTLY: " + dns);
            if(dns){
            networkExtrasBundle.putInt("rdp", 1);
            }   else {
            networkExtrasBundle.putInt("rdp", 0);
            }

        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle).addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("EF0600F38A7C201000993B6569A7051D").build();
        mAdView.loadAd(adRequest);

            return v;
    }


}
