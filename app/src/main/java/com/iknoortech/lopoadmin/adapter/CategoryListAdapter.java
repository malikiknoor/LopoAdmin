package com.iknoortech.lopoadmin.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.activity.AddNewCategoryActivity;
import com.iknoortech.lopoadmin.activity.CategoryListActivity;
import com.iknoortech.lopoadmin.model.category.CategoryTable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CategoryTable> arrayList;

    public CategoryListAdapter(Context context, ArrayList<CategoryTable> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CategoryListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryListAdapter.ViewHolder holder, final int position) {

        holder.tvTitle.setText(arrayList.get(position).getName());
        Glide.with(context).load(arrayList.get(position).getImage()).into(holder.imgBanner);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddNewCategoryActivity.class);
                intent.putExtra("data", arrayList.get(position));
                intent.putExtra("categoryId", arrayList.get(position).getCategoryId());
                intent.putExtra("type", "old");
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgBanner;
        private TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBanner = itemView.findViewById(R.id.imageView11);
            tvTitle = itemView.findViewById(R.id.textView12);

        }
    }
}
