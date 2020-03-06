package com.warnerbros.CCPATestApp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PrivacySettingsActivity : AppCompatActivity() {
    private var country: String? = null
    private var region: String? = null
    var dns = false
    var toolbar: Toolbar? = null
    var dnsSwitch: Switch? = null
    var policyCenterText: TextView? = null
    var policyText: TextView? = null
    var vendorText: TextView? = null
    var title: TextView? = null
    var dnsFlagText: TextView? = null
    var iabStringText: TextView? = null
    var confirmation: AlertDialog? = null
    var builder: AlertDialog.Builder? = null
    var settings: SharedPreferences? = null
    var GDPR_Countries = ArrayList(
            Arrays.asList("Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Germany", "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia", "Spain", "Sweden"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_settings)
        toolbar = findViewById(R.id.app_bar)
        title = toolbar!!.findViewById(R.id.toolbar_title)
        title!!.setText("Privacy Settings")
        title!!.setBackground(null)
        toolbar!!.setNavigationIcon(resources.getDrawable(R.drawable.ic_arrow_back_black_24dp))
        toolbar!!.setNavigationOnClickListener(View.OnClickListener { v ->
            val intent = Intent(v.context, MainActivity::class.java)
            intent.putExtra("fragment", "UserSettingsFragment")
            v.context.startActivity(intent)
        })
        dnsFlagText = findViewById<View>(R.id.dnsFlagText) as TextView
        iabStringText = findViewById<View>(R.id.iabStringText) as TextView
        policyCenterText = findViewById<View>(R.id.policyCenter) as TextView
        policyText = findViewById<View>(R.id.policyText) as TextView
        vendorText = findViewById<View>(R.id.vendorText) as TextView
        policyText!!.movementMethod = LinkMovementMethod.getInstance()
        dnsSwitch = findViewById(R.id.dnsSwitch)
        val getData = getData()
        getData.execute()
        settings = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        dns = settings!!.getBoolean("DNS", false)
        if (dns) {
            dnsSwitch!!.setChecked(true)
            dnsFlagText!!.text = "DNS Currently Enabled"
            iabStringText!!.text = "IAB String: " + " "
        }
        vendorText!!.setOnClickListener { v ->
            val i = Intent(v.context, VendorListActivity::class.java)
            startActivity(i)
        }
        policyCenterText!!.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://privacycenter.wb.com/"))
            val json = buildJson()
            val sendJson = sendJson()
            sendJson.execute("https://dev.privacycenter.wb.com/index.php/wp-json/appdata/", json.toString())
            startActivity(i)
        }
        policyText!!.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.warnerbros.com/privacy/"))
            startActivity(i)
        }
        dnsSwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                optOut()
                Toast.makeText(buttonView.context.applicationContext, "No longer personalizing ads", Toast.LENGTH_LONG).show()
            } else {
                builder = AlertDialog.Builder(buttonView.context)
                        .setTitle("Confirm Opt-In")
                        .setMessage("Accepting this means that you acknowledge that you are opting-in to personalized advertisements")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                builder!!.setPositiveButton("Accept", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    optIn()
                    Toast.makeText(applicationContext, "Processed Request", Toast.LENGTH_LONG).show()
                })
                builder!!.setNegativeButton("Decline", DialogInterface.OnClickListener { dialog, which ->
                    dnsSwitch = findViewById(R.id.dnsSwitch)
                    dialog.dismiss()
                    dnsSwitch!!.setChecked(true)
                    Toast.makeText(applicationContext, "Cancelled Request", Toast.LENGTH_LONG).show()
                })
                confirmation = builder!!.create()
                confirmation!!.show()
            }
        })
    }

    private fun optOut() {
        dns = true
        val networkExtrasBundle = Bundle()
        networkExtrasBundle.putInt("rdp", 1)
        val request = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, networkExtrasBundle)
                .build()
        settings = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val editor = settings!!.edit()
        editor.putInt("gad_rdp", 1)
        editor.putBoolean("DNS", dns)
        editor.commit()
        FirebaseAnalytics.getInstance(this).setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, "false")
        dnsFlagText!!.text = "DNS Currently Enabled"
    }

    private fun optIn() {
        dns = false
        val networkExtrasBundle = Bundle()
        networkExtrasBundle.putInt("rdp", 0)
        val request = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, networkExtrasBundle)
                .build()
        settings = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val editor = settings!!.edit()
        editor.putInt("gad_rdp", 0)
        editor.putBoolean("DNS", dns)
        editor.commit()
        FirebaseAnalytics.getInstance(this).setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, "false")
        dnsFlagText!!.text = "DNS Currently Disabled"
    }

    inner class getData : AsyncTask<String?, String?, String>() {
        var urlConnection: HttpURLConnection? = null
        protected override fun doInBackground(vararg params: String?): String? {
            val result = StringBuilder()
            try {
                val url = URL("http://ip-api.com/json/")
                urlConnection = url.openConnection() as HttpURLConnection
                val `in`: InputStream = BufferedInputStream(urlConnection!!.inputStream)
                val reader = BufferedReader(InputStreamReader(`in`))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    result.append(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection!!.disconnect()
            }
            return result.toString()
        }

        override fun onPostExecute(result: String) {
            dnsSwitch = findViewById(R.id.dnsSwitch)
            val g = Gson()
            val res = g.fromJson(result, GeoResponse::class.java)
            country = res.country
            region = res.regionName
            if (region == "Georgia") {
                dnsSwitch!!.setVisibility(View.VISIBLE)
            }
        }
    }

    private inner class sendJson : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String? {
            var data = ""
            var httpURLConnection: HttpURLConnection? = null
            try {
                httpURLConnection = URL(params[0]).openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection!!.doOutput = true
                val os = DataOutputStream(httpURLConnection.outputStream)
                os.writeBytes("PostData=" + params[1])
                os.flush()
                os.close()
                val `in` = httpURLConnection.inputStream
                val inputStreamReader = InputStreamReader(`in`)
                var inputStreamData = inputStreamReader.read()
                while (inputStreamData != -1) {
                    val curr = inputStreamData.toChar()
                    inputStreamData = inputStreamReader.read()
                    data += curr
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                httpURLConnection?.disconnect()
            }
            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.e("Tag", result)
        }
    }

    fun buildJson(): JSONObject {
        val json = JSONObject()
        val appDetails = JSONObject()
        val userDetails = JSONObject()
        val alternateIds = JSONArray()
        val alternateIds1 = JSONObject()
        val alternateIds2 = JSONObject()
        try {
            appDetails.put("appAssetId", "WBMOBXXXX")
            appDetails.put("appName", "WMPrivacyTestApp")
            appDetails.put("additionalInfo", "Build Version:1.0.0;AppPlatform:ANDROID;")
            userDetails.put("firstName", null)
            userDetails.put("lastName", null)
            userDetails.put("email", "")
            userDetails.put("region", null)
            alternateIds1.put("idType", "IDFV")
            alternateIds1.put("id", "YYY-YYY-YYY")
            alternateIds1.put("context", "")
            alternateIds2.put("idType", "CUSTOM")
            alternateIds2.put("id", "YYY-YYY-YYY")
            alternateIds2.put("context", "")
            alternateIds.put(alternateIds1)
            alternateIds.put(alternateIds2)
            json.put("accessKey", "valid_api_key")
            json.put("requestType", "DO_NOT_SELL")
            json.put("appDetails", appDetails)
            json.put("userDetails", userDetails)
            json.put("alternateIds", alternateIds)
        } catch (e: JSONException) {
            Toast.makeText(applicationContext, "Build JSON Failed", Toast.LENGTH_SHORT).show()
        }
        return json
    }
}