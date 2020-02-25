package com.warnerbros.CCPATestApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendorListAdapter extends RecyclerView.Adapter<VendorListAdapter.MyViewHolder>{

    String vendorsData[];
    Context context;

    public VendorListAdapter(Context ct, String vendor[]) {
        context = ct;
        vendorsData = vendor;
    }

    @NonNull
    @Override
    public VendorListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.vendors_row, parent, false);
        return new VendorListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorListAdapter.MyViewHolder holder, final int position) {
        holder.vendor.setText(vendorsData[position]);
    }

    @Override
    public int getItemCount() {
        return vendorsData.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView vendor;
        LinearLayout vendorLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            vendor = itemView.findViewById(R.id.vendorText);
            vendorLayout = itemView.findViewById(R.id.vendorsLayout);
        }
    }
}
