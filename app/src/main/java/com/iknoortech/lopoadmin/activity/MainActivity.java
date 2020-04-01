package com.iknoortech.lopoadmin.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.adapter.MainListAdapter;
import com.iknoortech.lopoadmin.listner.MainTableClickListner;
import com.iknoortech.lopoadmin.model.main.MainTableList;
import com.iknoortech.lopoadmin.util.AppConstant;

import java.util.ArrayList;

import static com.iknoortech.lopoadmin.util.AppConstant.CATEGORY_TABLE;
import static com.iknoortech.lopoadmin.util.AppConstant.PRODUCT_TABLE;
import static com.iknoortech.lopoadmin.util.AppConstant.USER_TABLE;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MainTableList> tableList;
    private RecyclerView recyclerView;

    private MainTableClickListner listner = new MainTableClickListner() {
        @Override
        public void onItemClick(String name) {
            switch (name) {
                case USER_TABLE:
                    startActivity(new Intent(MainActivity.this, UserListActivity.class));
                    break;
                case CATEGORY_TABLE:
                    startActivity(new Intent(MainActivity.this, CategoryListActivity.class));
                    break;
                case PRODUCT_TABLE:
                    startActivity(new Intent(MainActivity.this, ProductListActivity.class));
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        tableList = new ArrayList<>();
        tableList.add(new MainTableList(USER_TABLE, R.drawable.ic_person_24dp));
        tableList.add(new MainTableList(CATEGORY_TABLE, R.drawable.ic_category));
        tableList.add(new MainTableList(PRODUCT_TABLE, R.drawable.ic_product));
        recyclerView.setAdapter(new MainListAdapter(this, tableList, listner));
    }

}
