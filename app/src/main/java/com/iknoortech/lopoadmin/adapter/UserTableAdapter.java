package com.iknoortech.lopoadmin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.activity.UserProfileActivity;
import com.iknoortech.lopoadmin.model.user.UserTable;
import com.iknoortech.lopoadmin.util.AppUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserTableAdapter extends RecyclerView.Adapter<UserTableAdapter.ViewHolder> {

    private ArrayList<UserTable> arrayList;
    private Context context;

    public UserTableAdapter(ArrayList<UserTable> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.tvName.setText("Name: " + arrayList.get(position).getName());
        holder.tvEmail.setText("Email: " + arrayList.get(position).getEmail());
        holder.tvMobile.setText("Mobile: " + arrayList.get(position).getPhone());
        holder.tvLogin.setText("Login Status: " + arrayList.get(position).getIsLogin());

        if (!arrayList.get(position).getImage().isEmpty()) {
            Glide.with(context).load(arrayList.get(position).getImage()).into(holder.imgUser);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userDetails", arrayList.get(position));
                context.startActivity(intent);
            }
        });

        holder.imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!arrayList.get(position).getImage().isEmpty()) {
                    AppUtil.seeFullImage(context, arrayList.get(position).getImage(), holder.imgUser);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgUser;
        private TextView tvName, tvEmail, tvMobile, tvLogin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUser = itemView.findViewById(R.id.imageView1);
            tvName = itemView.findViewById(R.id.textView1);
            tvEmail = itemView.findViewById(R.id.textView2);
            tvMobile = itemView.findViewById(R.id.textView3);
            tvLogin = itemView.findViewById(R.id.textView4);
        }
    }
}
