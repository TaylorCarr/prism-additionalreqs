package com.warnerbros.CCPATestApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
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

public class SettingsFragment extends Fragment {

    private String country, region;
    boolean dns;
    Switch dnsSwitch;
    TextView policyCenterText,policyText;
    AlertDialog confirmation;
    AlertDialog.Builder builder;
    SharedPreferences settings;

    ArrayList<String> GDPR_Countries = new ArrayList<String>(
            Arrays.asList("Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Germany", "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia", "Spain", "Sweden"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_settings, container, false);

            policyCenterText = (TextView) v.findViewById(R.id.policyCenter);
            policyText = (TextView) v.findViewById(R.id.policyText);
            policyCenterText.setMovementMethod(LinkMovementMethod.getInstance());
            policyText.setMovementMethod(LinkMovementMethod.getInstance());

            dnsSwitch = v.findViewById(R.id.dnsSwitch);

            getData getData = new getData();
            getData.execute();

            settings = this.getActivity().getSharedPreferences("PREFS", MODE_PRIVATE);
            dns = settings.getBoolean("DNS", false);
            if(dns){
                dnsSwitch.setChecked(true);
            }

        dnsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dns = true;
                    Bundle networkExtrasBundle = new Bundle();
                    networkExtrasBundle.putInt("rdp", 1);
                    AdRequest request = new AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                            .build();

                    settings = getContext().getApplicationContext().getSharedPreferences("PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("gad_rdp", 1);
                    editor.putBoolean("DNS", dns);
                    editor.commit();


                    Toast.makeText(buttonView.getContext().getApplicationContext(), "No longer personalizing ads", Toast.LENGTH_LONG).show();
                } else {
                    builder = new AlertDialog.Builder(buttonView.getContext())
                            .setTitle("Confirm Opt-In")
                            .setMessage("Accepting this means that you acknowledge that you are opting-in to personalized advertisements")
                            .setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dns = false;
                            Bundle networkExtrasBundle = new Bundle();
                            networkExtrasBundle.putInt("rdp", 0);
                            AdRequest request = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                                    .build();
                            settings = getContext().getApplicationContext().getSharedPreferences("PREFS", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("gad_rdp", 0);
                            editor.putBoolean("DNS", dns);
                            editor.commit();

                            Toast.makeText(getContext().getApplicationContext(), "Processed Request", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dnsSwitch = getView().findViewById(R.id.dnsSwitch);
                            dialog.dismiss();
                            dnsSwitch.setChecked(true);
                            Toast.makeText(getContext().getApplicationContext(), "Cancelled Request", Toast.LENGTH_LONG).show();
                        }
                    });
                    confirmation = builder.create();
                    confirmation.show();
                }
            }
        });



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
            dnsSwitch = getView().findViewById(R.id.dnsSwitch);
            Gson g =  new Gson();
            GeoResponse res = g.fromJson(result, GeoResponse.class);
            country = res.getCountry();
            region = res.getRegionName();
            if(region.equals("Georgia")) {
                dnsSwitch.setVisibility(View.VISIBLE);
            }
        }

    }

}
