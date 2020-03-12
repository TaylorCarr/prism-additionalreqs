package com.warnermedia.PrivacyTestApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VendorListActivity extends AppCompatActivity {

    String vendors[];
    RecyclerView vendorsRecycler;
    Toolbar toolbar;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendors);

        vendors = getResources().getStringArray(R.array.vendors);
        vendorsRecycler = findViewById(R.id.vendorsRecycler);
        VendorListAdapter vendorsListAdapter = new VendorListAdapter(this, vendors);
        vendorsRecycler.setAdapter(vendorsListAdapter);
        vendorsRecycler.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.app_bar);
        title = toolbar.findViewById(R.id.toolbar_title);
        title.setText("Third-Party Vendors");
        title.setBackground(null);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PrivacySettingsActivity.class);
                v.getContext().startActivity(intent);
            }
        });

    }

}
