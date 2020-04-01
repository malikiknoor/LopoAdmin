package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.activity.product.AddNewMobileActvity;
import com.iknoortech.lopoadmin.adapter.CategoryListAdapter;
import com.iknoortech.lopoadmin.model.category.CategoryTable;
import com.iknoortech.lopoadmin.model.user.UserTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;

import java.util.ArrayList;

import static com.iknoortech.lopoadmin.util.AppUtil.closeProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.isInternetAvailable;
import static com.iknoortech.lopoadmin.util.AppUtil.showProgressDialog;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView imgBack, imgAdd;
    private int catCount = 0, proCount = 0;
    private ArrayList<CategoryTable> categoryTable;
    private PopupMenu catMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerView3);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imgBack = findViewById(R.id.imageView15);
        imgAdd = findViewById(R.id.imageView16);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryTable.size() > 0) {
                    showMenu();
                } else {
                    Toast.makeText(ProductListActivity.this, "No Category Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        if (!AppUtil.isInternetAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            getCategoryList();
            getProductList();
        }
        super.onResume();
    }

    private void getProductList() {
        proCount = 0;
        showProgressDialog(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.PRODUCT_TABLE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getProductDetail(document.getId(), task.getResult().size());
                                }
                            } else {
                                closeProgressDialog();
                                Toast.makeText(ProductListActivity.this,
                                        "No Data Available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            closeProgressDialog();
                            Toast.makeText(ProductListActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressDialog();
                Toast.makeText(ProductListActivity.this, "" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProductDetail(String id, final int size) {
        proCount++;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection(AppConstant.PRODUCT_TABLE)
                .document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (proCount == size) {
                        AppUtil.closeProgressDialog();
                    }
                    DocumentSnapshot document = task.getResult();

                } else {
                    closeProgressDialog();
                    Toast.makeText(ProductListActivity.this, ""
                            + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressDialog();
                Toast.makeText(ProductListActivity.this, "" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCategoryList() {
        categoryTable = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.CATEGORY_TABLE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getCategoryDetails(document.getId(), task.getResult().size());
                                }
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductListActivity.this, "" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCategoryDetails(String id, final int size) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.CATEGORY_TABLE).document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            CategoryTable table = snapshot.toObject(CategoryTable.class);
                            categoryTable.add(table);
                        }
                    }
                });
    }

    private void showMenu() {
        catMenu = new PopupMenu(ProductListActivity.this, imgAdd);
        for (int i = 0; i < categoryTable.size(); i++) {
            catMenu.getMenu().add(categoryTable.get(i).getName());
        }
        catMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String selected = item.getTitle().toString();
                for (int i = 0; i < categoryTable.size(); i++) {
                    if (categoryTable.get(i).getName().equals(selected)) {
                        Intent intent = null;
                        if (categoryTable.get(i).getCategoryId().equals("1")) {
                            intent = new Intent(ProductListActivity.this, AddNewMobileActvity.class);
                        } else if (categoryTable.get(i).getCategoryId().equals("2")) {

                        } else if (categoryTable.get(i).getCategoryId().equals("3")) {

                        } else if (categoryTable.get(i).getCategoryId().equals("4")) {

                        } else if (categoryTable.get(i).getCategoryId().equals("5")) {

                        } else if (categoryTable.get(i).getCategoryId().equals("6")) {

                        } else if (categoryTable.get(i).getCategoryId().equals("7")) {

                        }
                        intent.putExtra("data", categoryTable.get(i));
                        startActivity(intent);
                        break;
                    }
                }
                return false;
            }
        });

        catMenu.show();
    }
}
