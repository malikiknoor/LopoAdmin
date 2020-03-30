package com.iknoortech.lopoadmin.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.activity.ImageViewActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.iknoortech.lopoadmin.util.AppConstant.PROFILE_IMAGE_TABLE;

public class AppUtil {

    private static Dialog builder;

    public static boolean isValidEmail(EditText argEditText) {

        try {
            Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
            Matcher matcher = pattern.matcher(argEditText.getText());
            return matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager mManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mManager.getActiveNetworkInfo();
        return (mNetworkInfo != null) && (mNetworkInfo.isConnected());
    }

    public static void showProgressDialog(Activity activity) {
        builder = new Dialog(activity);
        builder.setContentView(R.layout.wait_dialog);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setCancelable(false);
        builder.show();
    }

    public static void closeProgressDialog() {
        if (builder != null) {
            builder.dismiss();
        }
    }

    public static String setDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void uploadImageToServer(final Context context, Bitmap bitmap, final String id) {
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = mImageStorage.child(PROFILE_IMAGE_TABLE)
                .child(id + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        final ProgressDialog pd = new ProgressDialog(context);
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
                            updateUserImage(downUri.toString(), id);
                        }
                        pd.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateUserImage(String url, String userId) {
        Map<String, Object> user = new HashMap<>();
        user.put("image", url);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstant.USER_TABLE)
                .document(userId)
                .update(user);
    }

    public static Bitmap rotateImageIfRequired(Bitmap bitmap, String selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @SuppressLint("NewApi")
    public static void seeFullImage(Context context, String imageUrl, ImageView imageView) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(imageView, "fullImage");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pairs);
        intent.putExtra("fullImage", imageUrl);
        context.startActivity(intent, options.toBundle());
    }

}
