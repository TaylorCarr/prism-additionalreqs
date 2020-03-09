package com.warnerbros.CCPATestApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.turner.nexus.wmPrivacySdk.WmPrivacySdk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static com.google.firebase.analytics.FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS;

public class PrivacySettingsActivity extends AppCompatActivity {

    private String country, region;
    boolean dns;
    Toolbar toolbar;
    Switch dnsSwitch;
    TextView policyCenterText,policyText, vendorText, title, dnsFlagText, iabStringText;
    AlertDialog confirmation;
    AlertDialog.Builder builder;
    SharedPreferences settings;
    WmPrivacySdk privacyInstance = new WmPrivacySdk();


    ArrayList<String> GDPR_Countries = new ArrayList<String>(
            Arrays.asList("Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Germany", "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia", "Spain", "Sweden"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        toolbar = findViewById(R.id.app_bar);
        title = toolbar.findViewById(R.id.toolbar_title);
        title.setText("Privacy Settings");
        title.setBackground(null);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("fragment", "UserSettingsFragment");
                v.getContext().startActivity(intent);
            }
        });

        dnsFlagText = (TextView) findViewById(R.id.dnsFlagText);
        iabStringText = (TextView) findViewById(R.id.iabStringText);
        policyCenterText = (TextView) findViewById(R.id.policyCenter);
        policyText = (TextView) findViewById(R.id.policyText);
        vendorText = (TextView) findViewById(R.id.vendorText);
        policyText.setMovementMethod(LinkMovementMethod.getInstance());
        dnsSwitch = findViewById(R.id.dnsSwitch);

        privacyInstance.initPrism("WarnerMedia Privacy Test App","prod",this);

        getData getData = new getData();
        getData.execute();

        settings = getSharedPreferences("PREFS", MODE_PRIVATE);
        dns = settings.getBoolean("DNS", false);
        if(dns){
            dnsSwitch.setChecked(true);
            dnsFlagText.setText("DNS Currently Enabled");
            iabStringText.setText("IAB String: " + privacyInstance.getUSPString(this));
        }

        vendorText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent i = new Intent(v.getContext(), VendorListActivity.class);
                startActivity(i);
            }
        });

        policyCenterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent i= new Intent(Intent.ACTION_VIEW, Uri.parse("https://privacycenter.wb.com/"));
                JSONObject json = buildJson();
                sendJson sendJson = new sendJson();
                sendJson.execute("https://dev.privacycenter.wb.com/index.php/wp-json/appdata/", json.toString());
                startActivity(i);
            }
        });

        policyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.warnerbros.com/privacy/"));
                startActivity(i);
            }
        });

        dnsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    optOut();
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
                            optIn();
                            Toast.makeText(getApplicationContext(), "Processed Request", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dnsSwitch = findViewById(R.id.dnsSwitch);
                            dialog.dismiss();
                            dnsSwitch.setChecked(true);
                            Toast.makeText(getApplicationContext(), "Cancelled Request", Toast.LENGTH_LONG).show();
                        }
                    });
                    confirmation = builder.create();
                    confirmation.show();
                }
            }
        });
    }

    private void optOut(){
        dns = true;
        Bundle networkExtrasBundle = new Bundle();
        networkExtrasBundle.putInt("rdp", 1);
        AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                .build();

        settings = getSharedPreferences("PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("gad_rdp", 1);
        editor.putBoolean("DNS", dns);
        editor.commit();
        FirebaseAnalytics.getInstance(this).setUserProperty( ALLOW_AD_PERSONALIZATION_SIGNALS, "false" );

        privacyInstance.ccpaDoNotShare();
        System.out.println("DNS is currently: " + privacyInstance.getUSPBoolean(this) );
        dnsFlagText.setText("DNS Currently Enabled");

    }

    private void optIn(){
        dns = false;
        Bundle networkExtrasBundle = new Bundle();
        networkExtrasBundle.putInt("rdp", 0);
        AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                .build();
        settings = getSharedPreferences("PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("gad_rdp", 0);
        editor.putBoolean("DNS", dns);
        editor.commit();
        FirebaseAnalytics.getInstance(this).setUserProperty( ALLOW_AD_PERSONALIZATION_SIGNALS, "true" );
        privacyInstance.ccpaShareData();
        dnsFlagText.setText("DNS Currently Disabled");

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
            dnsSwitch = findViewById(R.id.dnsSwitch);
            Gson g =  new Gson();
            GeoResponse res = g.fromJson(result, GeoResponse.class);
            country = res.getCountry();
            region = res.getRegionName();
            if(region.equals("Georgia")) {
                dnsSwitch.setVisibility(View.VISIBLE);
            }
        }

    }

    private class sendJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
                os.writeBytes("PostData=" + params[1]);
                os.flush();
                os.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char curr = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += curr;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("Tag", result);
        }
    }

    public JSONObject buildJson() {
        JSONObject json = new JSONObject();
        JSONObject appDetails = new JSONObject();
        JSONObject userDetails = new JSONObject();
        JSONArray alternateIds = new JSONArray();
        JSONObject alternateIds1 = new JSONObject();
        JSONObject alternateIds2 = new JSONObject();
        try {
            appDetails.put("appAssetId","WBMOBXXXX");
            appDetails.put("appName","CCPA Test App");
            appDetails.put("additionalInfo","Build Version:xxx;AppPlatform:ANDROID;");

            userDetails.put("firstName",null);
            userDetails.put("lastName",null);
            userDetails.put("email","");
            userDetails.put("region",null);

            alternateIds1.put("idType", "IDFV");
            alternateIds1.put("id","YYY-YYY-YYY");
            alternateIds1.put("context", "");

            alternateIds2.put("idType", "CUSTOM");
            alternateIds2.put("id","YYY-YYY-YYY");
            alternateIds2.put("context", "");

            alternateIds.put(alternateIds1);
            alternateIds.put(alternateIds2);

            json.put("accessKey", "valid_api_key");
            json.put("requestType", "DO_NOT_SELL");
            json.put("appDetails", appDetails);
            json.put("userDetails", userDetails);
            json.put("alternateIds", alternateIds);
        }catch(JSONException e){
            Toast.makeText(getApplicationContext(),"Build JSON Failed",Toast.LENGTH_SHORT).show();
        }
        return json;
    }
}
