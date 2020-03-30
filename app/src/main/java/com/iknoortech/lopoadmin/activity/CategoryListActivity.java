package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.adapter.CategoryListAdapter;
import com.iknoortech.lopoadmin.model.category.CategoryTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;

import java.util.ArrayList;

public class CategoryListActivity extends AppCompatActivity {

    private ImageView imgBack, imgAdd;
    private RecyclerView rvCategory;
    private CategoryListAdapter adapter;
    private ArrayList<CategoryTable> categoryTable;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        rvCategory = findViewById(R.id.recyclerView2);
        imgBack = findViewById(R.id.imageView10);
        imgAdd = findViewById(R.id.imageView12);
        rvCategory.setLayoutManager(new GridLayoutManager(this, 3));
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtil.isInternetAvailable(CategoryListActivity.this)){

                }else{
                    Toast.makeText(CategoryListActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        if (AppUtil.isInternetAvailable(this)) {
            getCategoryList();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    private void getCategoryList() {
        count = 0;
        categoryTable = new ArrayList<>();
        adapter = new CategoryListAdapter(this, categoryTable);
        rvCategory.setAdapter(adapter);
        AppUtil.showProgressDialog(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.CATEGORY_TABLE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                getCategoryDetails(document.getId(), task.getResult().size());
                            }
                        } else {
                            Toast.makeText(CategoryListActivity.this, "" +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtil.closeProgressDialog();
                Toast.makeText(CategoryListActivity.this, "" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCategoryDetails(String id, final int size) {
        count++;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.CATEGORY_TABLE).document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (count == size) {
                                AppUtil.closeProgressDialog();
                            }
                            DocumentSnapshot snapshot = task.getResult();
                            CategoryTable table = snapshot.toObject(CategoryTable.class);
                            categoryTable.add(table);
                            adapter.notifyDataSetChanged();
                        } else {
                            AppUtil.closeProgressDialog();
                            Toast.makeText(CategoryListActivity.this, "" +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtil.closeProgressDialog();
                Toast.makeText(CategoryListActivity.this, "" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
