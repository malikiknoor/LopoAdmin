package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.RealPathUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.lopoadmin.util.AppConstant.PROFILE_IMAGE_TABLE;
import static com.iknoortech.lopoadmin.util.AppUtil.closeProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.isInternetAvailable;
import static com.iknoortech.lopoadmin.util.AppUtil.isValidEmail;
import static com.iknoortech.lopoadmin.util.AppUtil.rotateImageIfRequired;
import static com.iknoortech.lopoadmin.util.AppUtil.showProgressDialog;
import static com.iknoortech.lopoadmin.util.AppUtil.updateUserImage;
import static com.iknoortech.lopoadmin.util.AppUtil.uploadImageToServer;

public class CreateNewUserActivity extends AppCompatActivity {

    private CircleImageView userImage;
    private ImageView imgEdit, imgBack;
    private EditText edt_name, edt_email, edt_phone, edt_password, edt_conPass;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri imgUri;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_user);

        edt_name = findViewById(R.id.editText9);
        edt_email = findViewById(R.id.editText10);
        edt_phone = findViewById(R.id.editText11);
        edt_password = findViewById(R.id.editText12);
        edt_conPass = findViewById(R.id.editText13);
        userImage = findViewById(R.id.circleImageView1);
        imgEdit = findViewById(R.id.imageView9);
        imgBack = findViewById(R.id.imageView8);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalStoragePermission()) {
                    CharSequence[] items = {"Gallery", "Remove profile pic"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewUserActivity.this);
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
                                if (imgUri != null) {
                                    imgUri = null;
                                    selectedImage = null;
                                    userImage.setImageResource(R.drawable.ic_person_24dp);
                                }
                            }
                        }
                    });
                    builder.show();
                } else {
                    requestExternalStoragePermission();
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void uploadImage() {
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = mImageStorage.child(PROFILE_IMAGE_TABLE)
                .child(mAuth.getCurrentUser().getUid() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading image...");
        pd.setCancelable(false);
        pd.show();

        final UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downUri = task.getResult();
                            updateUserImage(downUri.toString(), mAuth.getCurrentUser().getUid());
                            Toast.makeText(CreateNewUserActivity.this, "New user registered successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        pd.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateNewUserActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imgUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imgUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                userImage.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(CreateNewUserActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(CreateNewUserActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
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

    public void validateRegister(View view) {
        if (edt_name.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        } else if (edt_email.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your email id", Toast.LENGTH_SHORT).show();
        } else if (!isValidEmail(edt_email)) {
            Toast.makeText(this, "Please enter a valid email id", Toast.LENGTH_SHORT).show();
        } else if (edt_phone.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show();
        } else if (edt_phone.getText().toString().length() < 10) {
            Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
        } else if (edt_password.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        } else if (edt_password.getText().toString().length() > 6) {
            Toast.makeText(this, "Password should contains 6 charcters", Toast.LENGTH_SHORT).show();
        } else if (!edt_password.getText().toString().equals(edt_conPass.getText().toString())) {
            Toast.makeText(this, "Password and Confirm password should be same", Toast.LENGTH_SHORT).show();
        } else if (!isInternetAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            registerUserWithFirebase();
        }
    }

    private void registerUserWithFirebase() {
        showProgressDialog(CreateNewUserActivity.this);
        mAuth.createUserWithEmailAndPassword(edt_email.getText().toString(), edt_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        closeProgressDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            registerUser(user);
                        } else {
                            Toast.makeText(CreateNewUserActivity.this, "" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser(final FirebaseUser fUser) {
        showProgressDialog(CreateNewUserActivity.this);
        Map<String, Object> user = new HashMap<>();
        user.put("userId", fUser.getUid());
        user.put("name", edt_name.getText().toString());
        user.put("image", "");
        user.put("password", edt_password.getText().toString());
        user.put("email", edt_email.getText().toString());
        user.put("phone", edt_phone.getText().toString());
        user.put("token", "");
        user.put("isLogin", "No");
        user.put("registrationDate", System.currentTimeMillis());

        db.collection(AppConstant.USER_TABLE)
                .document(fUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        closeProgressDialog();
                        if (imgUri != null) {
                            uploadImage();
                        } else {
                            Toast.makeText(CreateNewUserActivity.this, "New user registered successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        closeProgressDialog();
                        Toast.makeText(CreateNewUserActivity.this, ""
                                + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
