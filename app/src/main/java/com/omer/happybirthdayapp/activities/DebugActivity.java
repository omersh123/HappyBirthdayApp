package com.omer.happybirthdayapp.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.omer.happybirthdayapp.R;
import com.omer.happybirthdayapp.models.Baby;
import com.omer.happybirthdayapp.utils.AddImageUtil;
import com.omer.happybirthdayapp.utils.PathUtil;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DebugActivity extends AppCompatActivity {

    private Baby baby = new Baby();
    private AppCompatTextView debugActivityNameTextView;
    private AppCompatTextView debugActivityBirthdayTextView;
    private AppCompatTextView debugActivityImageTextView;
    private AppCompatImageView debugActivityImageView;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private AppCompatButton cameraBtn;
    private AppCompatButton galleryBtn;
    private SimpleDateFormat sdf;
    private Bitmap babyBitmap;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        sdf = new SimpleDateFormat("MM/dd/yyyy");
        reloadData();
        initUI();
        loadButtons();
    }

    private void initUI() {
        debugActivityNameTextView = (AppCompatTextView) findViewById(R.id.debug_activity_name_text_view);
        debugActivityBirthdayTextView = (AppCompatTextView) findViewById(R.id.debug_activity_birthday_text_view);
        debugActivityImageTextView = (AppCompatTextView) findViewById(R.id.debug_activity_image_text_view);
        debugActivityImageView = (AppCompatImageView) findViewById(R.id.debug_activity_image_view);
        bottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        cameraBtn = (AppCompatButton) findViewById(R.id.camera_btn);
        galleryBtn = (AppCompatButton) findViewById(R.id.gallery_btn);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        reloadUI();
    }

    private void loadButtons() {
        debugActivityNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDiaolg();
            }
        });
        debugActivityBirthdayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDateDiaolg();
            }
        });
        View.OnClickListener onImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(true);
            }
        };
        debugActivityImageTextView.setOnClickListener(onImageClickListener);
        debugActivityImageView.setOnClickListener(onImageClickListener);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(false);
                AddImageUtil.dispatchShowGalleryIntent(DebugActivity.this);
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(false);
                AddImageUtil.dispatchTakePictureIntent(DebugActivity.this);
            }
        });
    }

    private void showEditDateDiaolg() {
        Calendar calendar = Calendar.getInstance();
        if (!baby.isBirthdayEmpty())
            calendar.setTime(baby.getBirthday());
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year,month,dayOfMonth);
                baby.setBirthday(calendar.getTime());
                saveData();
                reloadUI();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMaxDate(new Date().getTime());
        datePicker.show();
    }

    private void reloadUI() {
        debugActivityNameTextView.setText(getString(R.string.name_with_hint, baby.getName()));
        debugActivityBirthdayTextView.setText(getString(R.string.birthday_with_hint, baby.isBirthdayEmpty()?"":sdf.format(baby.getBirthday())));
        debugActivityImageTextView.setText(getString(R.string.image_with_hint, baby.getImageUrl()));

        if (AddImageUtil.checkGeneralPermissions(this)) {
            babyBitmap = BitmapFactory.decodeFile(baby.getImageUrl());
            debugActivityImageView.setImageBitmap(babyBitmap);
        }
    }

    private void showEditNameDiaolg(){
        final Dialog dialog = new Dialog(this, R.style.ThemeDialog);
        dialog.setContentView(R.layout.dialog_edit_text);
        final AppCompatEditText dialogEditText = (AppCompatEditText) dialog.findViewById(R.id.dialog_edit_text);
        AppCompatButton dialogSaveButton = (AppCompatButton) dialog.findViewById(R.id.dialog_save_button);

        dialog.setTitle("Set baby's name:");
        dialogSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baby.setName(dialogEditText.getText().toString());
                dialog.dismiss();
                saveData();
                reloadUI();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior!=null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            showBottomSheet(false);
        else
            super.onBackPressed();
    }

    private void showBottomSheet(boolean shouldShow) {
        bottomSheetBehavior.setState(shouldShow?BottomSheetBehavior.STATE_EXPANDED:BottomSheetBehavior.STATE_COLLAPSED);

    }

    public void reloadData(){
        try {
            baby.load();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while loading model from remote disk", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveData(){
        try {
            baby.save();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while saving model to remote disk", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        baby.setImageUrl(AddImageUtil.onActivityResult(this, requestCode, resultCode, data));
        saveData();
        reloadUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        AddImageUtil.onRequestPermissionsResult(this, requestCode,permissions,grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && AddImageUtil.isFromCamera==null) {
            reloadUI();
        }
    }
}
