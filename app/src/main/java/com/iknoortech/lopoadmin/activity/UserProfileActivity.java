package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.model.main.MainTableList;
import com.iknoortech.lopoadmin.model.user.UserTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.lopoadmin.util.AppUtil.closeProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.isInternetAvailable;
import static com.iknoortech.lopoadmin.util.AppUtil.setDate;
import static com.iknoortech.lopoadmin.util.AppUtil.showProgressDialog;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private UserTable userTable;
    private CircleImageView userImage;
    private ImageView imgEdt;
    private EditText edtName, edtPhone, edtEmail, edtPassword;
    private TextView tvLoginStatus, tvUserId, tvRegistrationDate;
    private Button btnUpdate;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userTable = (UserTable) getIntent().getSerializableExtra("userDetails");

        userImage = findViewById(R.id.circleImageView);
        imgEdt = findViewById(R.id.imageView5);
        edtName = findViewById(R.id.editText5);
        edtPhone = findViewById(R.id.editText6);
        edtEmail = findViewById(R.id.editText7);
        edtPassword = findViewById(R.id.editText8);
        tvLoginStatus = findViewById(R.id.textView7);
        tvUserId = findViewById(R.id.textView8);
        tvRegistrationDate = findViewById(R.id.textView9);
        btnUpdate = findViewById(R.id.button4);
        mAuth = FirebaseAuth.getInstance();
        setUserProfile();

    }

    private void setUserProfile() {
        if (!userTable.getImage().isEmpty()) {
            Glide.with(this).load(userTable.getImage()).into(userImage);
        }

        edtName.setText(userTable.getName());
        edtPhone.setText(userTable.getPhone());
        edtEmail.setText(userTable.getEmail());
        edtPassword.setText(userTable.getPassword());
        tvLoginStatus.setText("Login Status: " + userTable.getIsLogin());
        tvUserId.setText("Firebase User Id: " + userTable.getUserId());
        tvRegistrationDate.setText("Registration Date: " + setDate(userTable.getRegistrationDate()));

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInternetAvailable(UserProfileActivity.this)) {
                    Toast.makeText(UserProfileActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        if (!AppUtil.isInternetAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    private void loginUser() {
        showProgressDialog(this);
        mAuth.signInWithEmailAndPassword(userTable.getEmail(), userTable.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            if (!edtEmail.getText().toString().equals(userTable.getEmail())) {
                                updateEmailId(mAuth.getCurrentUser());
                            } else if (!edtPassword.getText().toString().equals(userTable.getPassword())) {
                                updatePassword(mAuth.getCurrentUser());
                            } else {
                                updateProfile();
                            }
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Authentication failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updatePassword(final FirebaseUser user) {
        showProgressDialog(this);
        user.updatePassword(edtPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("password", edtPassword.getText().toString());
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(AppConstant.USER_TABLE)
                                    .document(userTable.getUserId())
                                    .update(user);
                            userTable.setPassword(edtPassword.getText().toString());
                            loginUser();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "" +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateProfile() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", edtName.getText().toString());
        user.put("phone", edtPhone.getText().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.USER_TABLE)
                .document(userTable.getUserId())
                .update(user);
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateEmailId(final FirebaseUser user) {
        showProgressDialog(this);
        user.updateEmail(edtEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", edtEmail.getText().toString());
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(AppConstant.USER_TABLE)
                                    .document(userTable.getUserId())
                                    .update(user);
                            userTable.setEmail(edtEmail.getText().toString());
                            loginUser();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "" +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
