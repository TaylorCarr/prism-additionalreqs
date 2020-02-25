package com.warnerbros.CCPATestApp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.MyViewHolder>{

    String settingsData[], descriptionsData[];
    Context context;

    public SettingsAdapter(Context ct, String setting[], String description[]) {
        context = ct;
        settingsData = setting;
        descriptionsData = description;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.settings_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.setting.setText(settingsData[position]);
        holder.description.setText(descriptionsData[position]);

        holder.settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(settingsData[position].equals("Notification Settings")) {
                    Intent intent1 = new Intent();
                    intent1.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent1.putExtra("android.provider.extra.APP_PACKAGE", context.getApplicationContext().getPackageName());
                    intent1.putExtra("app_package", context.getApplicationContext().getPackageName());
                    intent1.putExtra("app_uid", context.getApplicationContext().getApplicationInfo().uid);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(intent1);
                }else if(settingsData[position].equals("Privacy Settings")) {
                    Intent intent = new Intent(context.getApplicationContext(), PrivacySettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(intent);
                } else if(settingsData[position].equals("Privacy Policy")) {
                    Intent intent2=new Intent(Intent.ACTION_VIEW, Uri.parse("https://privacycenter.wb.com/"));
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(intent2);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsData.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView setting, description;
        LinearLayout settingsLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            setting = itemView.findViewById(R.id.settingText);
            description = itemView.findViewById(R.id.descriptionText);
            settingsLayout = itemView.findViewById(R.id.settingsLayout);
        }
    }
}
