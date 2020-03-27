package com.iknoortech.lopoadmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.listner.MainTableClickListner;
import com.iknoortech.lopoadmin.model.main.MainTableList;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MainTableList> arrayList;
    private MainTableClickListner listner;

    public MainListAdapter(Context context, ArrayList<MainTableList> arrayList,
                           MainTableClickListner listner) {
        this.context = context;
        this.arrayList = arrayList;
        this.listner = listner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_main_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.title.setText(arrayList.get(position).getTitle());
        holder.logo.setBackgroundResource(arrayList.get(position).getLogo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listner != null){
                    listner.onItemClick(arrayList.get(position).getTitle());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView logo;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textView);
            logo = itemView.findViewById(R.id.imageView);
        }
    }
}
