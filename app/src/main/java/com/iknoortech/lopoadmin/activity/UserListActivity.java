package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        rvUser = findViewById(R.id.recyclerView1);
        rvUser.setLayoutManager(new LinearLayoutManager(this));

        if (AppUtil.isInternetAvailable(this)) {
            userTable = new ArrayList<>();
            setAdapter();
            getUserId();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserId() {
        AppUtil.showProgressDialog(this);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.USER_TABLE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getUserDetail(document.getId());
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

    private void getUserDetail(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection(AppConstant.USER_TABLE)
                .document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    UserTable userData = document.toObject(UserTable.class);
                    userTable.add(userData);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UserListActivity.this, ""
                            + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAdapter() {
        adapter = new UserTableAdapter(userTable, this);
        rvUser.setAdapter(adapter);
    }
}
