package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.model.main.MainTableList;
import com.iknoortech.lopoadmin.model.user.UserTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;
import com.iknoortech.lopoadmin.util.RealPathUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.lopoadmin.util.AppConstant.PROFILE_IMAGE_TABLE;
import static com.iknoortech.lopoadmin.util.AppUtil.closeProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.isInternetAvailable;
import static com.iknoortech.lopoadmin.util.AppUtil.rotateImageIfRequired;
import static com.iknoortech.lopoadmin.util.AppUtil.seeFullImage;
import static com.iknoortech.lopoadmin.util.AppUtil.setDate;
import static com.iknoortech.lopoadmin.util.AppUtil.showProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.uploadImageToServer;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private UserTable userTable;
    private CircleImageView userImage;
    private ImageView imgEdt, backImage;
    private EditText edtName, edtPhone, edtEmail, edtPassword;
    private TextView tvLoginStatus, tvUserId, tvRegistrationDate;
    private Button btnUpdate, btnDelete;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private boolean isDeleteUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userTable = (UserTable) getIntent().getSerializableExtra("userDetails");

        userImage = findViewById(R.id.circleImageView);
        backImage = findViewById(R.id.imageView4);
        imgEdt = findViewById(R.id.imageView5);
        edtName = findViewById(R.id.editText5);
        edtPhone = findViewById(R.id.editText6);
        edtEmail = findViewById(R.id.editText7);
        edtPassword = findViewById(R.id.editText8);
        tvLoginStatus = findViewById(R.id.textView7);
        tvUserId = findViewById(R.id.textView8);
        tvRegistrationDate = findViewById(R.id.textView9);
        btnUpdate = findViewById(R.id.button4);
        btnDelete = findViewById(R.id.button6);
        mAuth = FirebaseAuth.getInstance();
        setUserProfile();

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalStoragePermission()) {
                    CharSequence[] items = {"Gallery", "Remove profile pic"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                    builder.setTitle("Select one");

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos == 0) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), 0);
                            } else if (pos == 1) {
                                removeProfilePic();
                            }
                        }
                    });
                    builder.show();
                } else {
                    requestExternalStoragePermission();
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userTable.getImage().isEmpty()) {
                    seeFullImage(UserProfileActivity.this, userTable.getImage(), userImage);
                } else if (imageUri != null) {
                    seeFullImage(UserProfileActivity.this,
                            RealPathUtil.getPath(UserProfileActivity.this, imageUri), userImage);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setTitle("Delete user");
                builder.setMessage("Do you want to delete this user ?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (!isInternetAvailable(UserProfileActivity.this)) {
                            Toast.makeText(UserProfileActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                        } else {
                            isDeleteUser = true;
                            loginUser();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                        .show();
            }
        });

    }

    private void removeProfilePic() {
        showProgressDialog(this);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(PROFILE_IMAGE_TABLE).child(userTable.getUserId() + ".jpg");
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        closeProgressDialog();
                        Map<String, Object> user = new HashMap<>();
                        user.put("image", "");
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection(AppConstant.USER_TABLE)
                                .document(userTable.getUserId())
                                .update(user);
                        Toast.makeText(UserProfileActivity.this,
                                "Profile image removed successfully", Toast.LENGTH_SHORT).show();
                        userImage.setImageResource(R.drawable.ic_person_24dp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        closeProgressDialog();
                        Toast.makeText(UserProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                            } else if (isDeleteUser) {
                                deleteUser(mAuth.getCurrentUser());
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

    private void deleteUser(FirebaseUser user) {
        showProgressDialog(this);
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(AppConstant.USER_TABLE)
                                    .document(userTable.getUserId())
                                    .delete();
                            Toast.makeText(UserProfileActivity.this, "User Deleted Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "" +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                userImage.setImageBitmap(selectedImage);
                try {
                    uploadImageToServer(this, rotateImageIfRequired(selectedImage, RealPathUtil.getPath(this, imageUri)), userTable.getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(UserProfileActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openUtilityDialog(this, "External Storage permission is required. Please allow this permission in App Settings.");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1011);
        }
    }

    private void openUtilityDialog(final Context ctx, final String messageID) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(ctx, R.style.Theme_AppCompat_Light);
        dialog.setMessage(messageID);
        dialog.setCancelable(false);
        dialog.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 0);
                } else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    openUtilityDialog(this, "You Have To Give Permission From Your Device Setting To go in Setting Please Click on Settings Button");
                }
                break;
        }

    }
}
