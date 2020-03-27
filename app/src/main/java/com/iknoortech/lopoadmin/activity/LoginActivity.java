package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.editText);
        edtPassword = findViewById(R.id.editText1);
    }

    public void loginAdmin(View view) {
        if (edtUsername.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        } else if (edtPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        } else if (!AppUtil.isInternetAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final DocumentReference docRef = db.collection(AppConstant.ADMINTABLE)
                    .document(AppConstant.ADMINKEY);
            AppUtil.showProgressDialog(this);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    AppUtil.closeProgressDialog();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.getString("username").equals(edtUsername.getText().toString()) &&
                                document.getString("password").equals(edtPassword.getText().toString())) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Please enter valid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
