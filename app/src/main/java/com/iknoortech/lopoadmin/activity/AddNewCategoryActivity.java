package com.iknoortech.lopoadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.model.category.CategoryTable;
import com.iknoortech.lopoadmin.util.AppConstant;
import com.iknoortech.lopoadmin.util.AppUtil;
import com.iknoortech.lopoadmin.util.RealPathUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.lopoadmin.util.AppConstant.CAETGORY_IMAGE_TABLE;
import static com.iknoortech.lopoadmin.util.AppConstant.PROFILE_IMAGE_TABLE;
import static com.iknoortech.lopoadmin.util.AppUtil.rotateImageIfRequired;
import static com.iknoortech.lopoadmin.util.AppUtil.uploadImageToServer;

public class AddNewCategoryActivity extends AppCompatActivity {

    private ImageView imgBack;
    private EditText edtTitle;
    private Button submit, addImage;
    private TextView tvImgName, tvStatus;
    private String catId = "", imgUrl = "", type = "", status = "";
    private ImageView imgSlected;
    private Uri imageUri;
    private Bitmap selectedImage;
    private CategoryTable categoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_category);

        imgBack = findViewById(R.id.imageView13);
        edtTitle = findViewById(R.id.editText14);
        addImage = findViewById(R.id.button8);
        submit = findViewById(R.id.button7);
        tvImgName = findViewById(R.id.textView13);
        tvStatus = findViewById(R.id.textView14);
        imgSlected = findViewById(R.id.imageView14);

        catId = getIntent().getStringExtra("categoryId");
        type = getIntent().getStringExtra("type");
        if (type.equals("old")) {
            categoryData = (CategoryTable) getIntent().getSerializableExtra("data");
            edtTitle.setText(categoryData.getName());
            status = categoryData.getStatus();
            if (status.equals("0")) {
                tvStatus.setText("Closed");
            } else {
                tvStatus.setText("Open");
            }
            Glide.with(this).load(categoryData.getImage()).into(imgSlected);
            addImage.setText("Update Image");
            submit.setText("Update Category");
            setDropdown();
        } else {
            tvStatus.setVisibility(View.GONE);
        }

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalStoragePermission()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), 0);
                } else {
                    requestExternalStoragePermission();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppUtil.isInternetAvailable(AddNewCategoryActivity.this)) {
                    Toast.makeText(AddNewCategoryActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                } else if (edtTitle.getText().toString().isEmpty()) {
                    Toast.makeText(AddNewCategoryActivity.this, "Please enter category name", Toast.LENGTH_SHORT).show();
                } else {
                    if (type.equals("old")) {
                        if (tvStatus.getText().toString().isEmpty()) {
                            Toast.makeText(AddNewCategoryActivity.this, "Please set category status", Toast.LENGTH_SHORT).show();
                        } else {
                            if (selectedImage != null) {
                                uploadImage();
                            } else {
                                updateCategory();
                            }
                        }
                    } else {
                        if (selectedImage == null) {
                            Toast.makeText(AddNewCategoryActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                        } else {
                            uploadImage();
                        }
                    }
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setDropdown() {
        final PopupMenu menu = new PopupMenu(this, tvStatus);
        menu.getMenu().add("Open");
        menu.getMenu().add("Closed");

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                tvStatus.setText(item.getTitle().toString());
                status = String.valueOf(item.getItemId());
                return false;
            }
        });

        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });
    }

    private void updateCategory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> cat = new HashMap<>();
        cat.put("name", edtTitle.getText().toString());
        cat.put("status", status);
        db.collection(AppConstant.CATEGORY_TABLE)
                .document(catId)
                .update(cat);
        Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void enterData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> cat = new HashMap<>();
        cat.put("name", edtTitle.getText().toString());
        cat.put("image", imgUrl);
        cat.put("categoryId", catId);
        cat.put("status", "1");
        db.collection(AppConstant.CATEGORY_TABLE)
                .document(catId)
                .set(cat);
        Toast.makeText(this, "Category inserted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                tvImgName.setText(RealPathUtil.getPath(this, imageUri));
                imgSlected.setImageBitmap(selectedImage);
                try {
                    selectedImage = rotateImageIfRequired(selectedImage, RealPathUtil.getPath(this, imageUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AddNewCategoryActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(AddNewCategoryActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage() {
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = mImageStorage.child(CAETGORY_IMAGE_TABLE)
                .child("cat_" + catId + ".jpg");

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
                            imgUrl = downUri.toString();
                            if (type.equals("old")) {
                                updateCategory();
                            } else {
                                enterData();
                            }
                        }
                        pd.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewCategoryActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
