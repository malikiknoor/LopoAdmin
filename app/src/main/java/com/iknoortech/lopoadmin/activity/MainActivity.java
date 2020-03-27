package com.iknoortech.lopoadmin.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.adapter.MainListAdapter;
import com.iknoortech.lopoadmin.listner.MainTableClickListner;
import com.iknoortech.lopoadmin.model.main.MainTableList;
import com.iknoortech.lopoadmin.util.AppConstant;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MainTableList> tableList;
    private RecyclerView recyclerView;

    private MainTableClickListner listner = new MainTableClickListner() {
        @Override
        public void onItemClick(String name) {
            startActivity(new Intent(MainActivity.this, UserListActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        tableList = new ArrayList<>();
        tableList.add(new MainTableList(AppConstant.USER_TABLE, R.drawable.ic_person_24dp));
        recyclerView.setAdapter(new MainListAdapter(this, tableList, listner));
    }
}
