package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.adapter.UserTableAdapter;
import com.iknoortech.lopoadmin.model.user.UserTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

import static com.iknoortech.lopoadmin.util.AppUtil.closeProgressDialog;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";
    private ArrayList<UserTable> userTable;
    private RecyclerView rvUser;
    private UserTableAdapter adapter;
    private ImageView backImage, imgAdd;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        backImage = findViewById(R.id.imageView6);
        imgAdd = findViewById(R.id.imageView7);
        rvUser = findViewById(R.id.recyclerView1);
        rvUser.setLayoutManager(new LinearLayoutManager(this));

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserListActivity.this, CreateNewUserActivity.class));
            }
        });
    }

    private void getUserId() {
        count = 0;
        AppUtil.showProgressDialog(this);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.USER_TABLE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getUserDetail(document.getId(), task.getResult().size());
                                }
                            } else {
                                Toast.makeText(UserListActivity.this, "No data Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(UserListActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getUserDetail(String id, final int size) {
        count++;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection(AppConstant.USER_TABLE)
                .document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (count == size) {
                        AppUtil.closeProgressDialog();
                    }
                    DocumentSnapshot document = task.getResult();
                    UserTable userData = document.toObject(UserTable.class);
                    userTable.add(userData);
                    adapter.notifyDataSetChanged();
                } else {
                    closeProgressDialog();
                    Toast.makeText(UserListActivity.this, ""
                            + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressDialog();
                Toast.makeText(UserListActivity.this, ""+
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter() {
        adapter = new UserTableAdapter(userTable, this);
        rvUser.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        if (AppUtil.isInternetAvailable(this)) {
            userTable = new ArrayList<>();
            setAdapter();
            getUserId();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    public void goToAddNewUser(View view) {

    }


}
