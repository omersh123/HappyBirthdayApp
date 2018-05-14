package com.omer.happybirthdayapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AddImageUtil {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;

    private static final int REQUEST_CAMERA_PERMISSION = 112;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 113;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 114;

    private static File currentImageFile;

    //if null - check for read permission only
    public static Boolean isFromCamera = null;

    public static void dispatchShowGalleryIntent(Activity activity) {
        isFromCamera = false;
        if (!checkPermissions(activity))
            return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                REQUEST_SELECT_PHOTO);
    }

    public static void dispatchTakePictureIntent(Activity activity) {
        isFromCamera = true;
        if (!checkPermissions(activity))
            return;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(activity);
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                currentImageFile = photoFile;
                Uri photoURI = FileProvider.getUriForFile(activity,
                        activity.getPackageName()+".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public static boolean checkGeneralPermissions(Activity activity) {
        isFromCamera = null;
        return checkPermissions(activity);
    }

    private static boolean checkPermissions(Activity activity) {
        if (isFromCamera!=null && isFromCamera && ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                Toast.makeText(activity,"Please enable camera permissions",Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        else if (isFromCamera!=null && ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(activity,"Please enable camera permissions",Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
            return false;
        }
        else if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity,"Please enable camera permissions",Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    private static File createImageFile(Activity activity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && isFromCamera!=null){
            if (isFromCamera) {
                dispatchTakePictureIntent(activity);
            }
            else {
                dispatchShowGalleryIntent(activity);
            }
        }

    }

    public static String onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            return currentImageFile.getAbsolutePath();
        }
        else if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            Uri selectedimg = data.getData();
            try {
                return PathUtil.getPath(context, selectedimg);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error while trying to select new image", Toast.LENGTH_SHORT).show();
                return "";
            }
        }
        return "";
    }
}