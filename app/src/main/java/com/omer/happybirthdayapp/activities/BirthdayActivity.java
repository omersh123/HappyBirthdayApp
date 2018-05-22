package com.omer.happybirthdayapp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.omer.happybirthdayapp.R;
import com.omer.happybirthdayapp.models.Baby;
import com.omer.happybirthdayapp.utils.AddImageUtil;
import com.squareup.picasso.Picasso;

import org.joda.time.Interval;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class BirthdayActivity extends AppCompatActivity {

    private static final int THEME_YELLOW = 0;
    private static final int THEME_GREEN = 1;
    private static final int THEME_BLUE = 2;
    private static final long YEAR_IN_MILLIS = 1000l * 60l * 60l * 24l * 365l;
    private static final long MONTH_IN_MILLIS = (1000l * 60l * 60l * 24l * 365l) / 12l;
    private Baby baby = new Baby();
    private Bitmap babyBitmap;

    private AppCompatImageView babyImageView;
    private AppCompatImageView backgroundImageView;
    private AppCompatImageButton closeBtn;
    private AppCompatTextView titleTextView;
    private AppCompatImageView ageImageView;
    private AppCompatTextView subtitleTextView;
    private AppCompatButton shareBtn;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private AppCompatImageButton editImageButton;
    private AppCompatButton cameraBtn;
    private AppCompatButton galleryBtn;
    private int currentTheme = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday);
        reloadData();
        initUI();
        loadButtons();
    }

    private void initUI() {
        babyImageView = (AppCompatImageView) findViewById(R.id.baby_image_view);
        backgroundImageView = (AppCompatImageView) findViewById(R.id.background_image_view);
        closeBtn = (AppCompatImageButton) findViewById(R.id.close_btn);
        titleTextView = (AppCompatTextView) findViewById(R.id.title_text_view);
        ageImageView = (AppCompatImageView) findViewById(R.id.age_image_view);
        subtitleTextView = (AppCompatTextView) findViewById(R.id.subtitle_text_view);
        editImageButton = (AppCompatImageButton) findViewById(R.id.edit_image_button);
        shareBtn = (AppCompatButton) findViewById(R.id.share_btn);
        bottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        cameraBtn = (AppCompatButton) findViewById(R.id.camera_btn);
        galleryBtn = (AppCompatButton) findViewById(R.id.gallery_btn);
        babyImageView.post(new Runnable() {
            @Override
            public void run() {
                babyImageView.getLayoutParams().width = babyImageView.getHeight();
                babyImageView.requestLayout();
            }
        });
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        randomBackgroundUI();
    }

    private void randomBackgroundUI() {
        currentTheme = new Random().nextInt(3);
        switch (currentTheme) {
            case THEME_YELLOW:
                backgroundImageView.setImageResource(R.drawable.android_elephant_popup);
                editImageButton.setImageResource(R.drawable.camera_icon_yellow);
                break;
            case THEME_GREEN:
                backgroundImageView.setImageResource(R.drawable.android_fox_popup);
                editImageButton.setImageResource(R.drawable.camera_icon_green);
                break;
            case THEME_BLUE:
                backgroundImageView.setImageResource(R.drawable.android_pelican_popup);
                editImageButton.setImageResource(R.drawable.camera_icon_blue);
                break;
        }
    }

    private void loadButtons() {
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBtn.setVisibility(View.INVISIBLE);
                shareBtn.setVisibility(View.INVISIBLE);
                editImageButton.setVisibility(View.INVISIBLE);
                Bitmap bitmap = getScreenShot(getWindow().getDecorView().findViewById(android.R.id.content));
                shareImage(bitmap);
                closeBtn.setVisibility(View.VISIBLE);
                shareBtn.setVisibility(View.VISIBLE);
                editImageButton.setVisibility(View.VISIBLE);

            }
        });
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(true);
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(false);
                AddImageUtil.dispatchShowGalleryIntent(BirthdayActivity.this);
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(false);
                AddImageUtil.dispatchTakePictureIntent(BirthdayActivity.this);
            }
        });
    }

    private void reloadUI() {
        titleTextView.setText(getString(R.string.activity_birthday_title, baby.getName()));
        reloadBirthdayToUI();
        if (AddImageUtil.checkGeneralPermissions(this) && !baby.getImageUrl().isEmpty()) {
            final Uri uri = Uri.fromFile(new File(baby.getImageUrl()));
            babyImageView.post(new Runnable() {
                @Override
                public void run() {
                    Picasso.get().load(uri).placeholder(getCurrentPlaceHolder())
                            .resize(babyImageView.getHeight(), babyImageView.getHeight()).centerCrop().into(babyImageView);
                }
            });
        } else {
            switch (currentTheme) {
                case THEME_YELLOW:
                    babyImageView.setImageResource(R.drawable.default_place_holder_yellow);
                    break;
                case THEME_GREEN:
                    babyImageView.setImageResource(R.drawable.default_place_holder_green);
                    break;
                case THEME_BLUE:
                    babyImageView.setImageResource(R.drawable.default_place_holder_blue);
                    break;
            }
        }
    }

    private int getCurrentPlaceHolder() {
        switch (currentTheme) {
            case THEME_YELLOW:
                return R.drawable.default_place_holder_yellow;
            case THEME_GREEN:
                return R.drawable.default_place_holder_green;
            case THEME_BLUE:
                return R.drawable.default_place_holder_blue;
            default:
                return 0;
        }
    }

    private void reloadBirthdayToUI() {
        //Because we are using API19 as min version - we can't use the new JodaTime system (Java 8 only) - so we will use it from gradle.
        Interval interval = new Interval(baby.getBirthday().getTime(), new Date().getTime());
        org.joda.time.Period period = interval.toPeriod();
        if (period.getYears() > 0) {
            loadNumber(period.getYears(), period.getMonths() >= 6);
            if (period.getYears() == 1 && period.getMonths() < 6) {
                subtitleTextView.setText(getString(R.string.activity_birthday_year));
            } else {
                subtitleTextView.setText(getString(R.string.activity_birthday_years));
            }

        } else {
            loadNumber(period.getMonths(), period.getDays() + period.getWeeks()*7 >= 15);
            if (period.getMonths() == 1 && (period.getDays() + period.getWeeks()*7 < 15 )) {
                subtitleTextView.setText(getString(R.string.activity_birthday_month));
            } else {
                subtitleTextView.setText(getString(R.string.activity_birthday_months));
            }
        }
    }

    private void loadNumber(int age, boolean isMoreThenHalf) {
        switch (age) {
            case 0:
                ageImageView.setImageResource(R.drawable.n0);
                break;
            case 1:
                ageImageView.setImageResource(isMoreThenHalf ? R.drawable.n1_half : R.drawable.n1);
                break;
            case 2:
                ageImageView.setImageResource(R.drawable.n2);
                break;
            case 3:
                ageImageView.setImageResource(R.drawable.n3);
                break;
            case 4:
                ageImageView.setImageResource(R.drawable.n4);
                break;
            case 5:
                ageImageView.setImageResource(R.drawable.n5);
                break;
            case 6:
                ageImageView.setImageResource(R.drawable.n6);
                break;
            case 7:
                ageImageView.setImageResource(R.drawable.n7);
                break;
            case 8:
                ageImageView.setImageResource(R.drawable.n8);
                break;
            case 9:
                ageImageView.setImageResource(R.drawable.n9);
                break;
            case 10:
                ageImageView.setImageResource(R.drawable.n10);
                break;
            case 11:
                ageImageView.setImageResource(R.drawable.n11);
                break;
            case 12:
                ageImageView.setImageResource(R.drawable.n12);
                break;
        }
    }

    public void reloadData() {
        try {
            baby.load();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while loading model from remote disk", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveData() {
        try {
            baby.save();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while saving model to remote disk", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void shareImage(Bitmap bitmap) {
        //taken from http://androidtechpoint.blogspot.co.il/2017/09/android-sharing-image-using-share-intent-without-saving-to-external-memory.html
        try {
            File cachePath = new File(this.getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        File imagePath = new File(this.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");
        Uri contentUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddImageUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && AddImageUtil.isFromCamera == null) {
            reloadUI();
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
    public void onBackPressed() {
        if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            showBottomSheet(false);
        else
            super.onBackPressed();
    }

    private void showBottomSheet(boolean shouldShow) {
        bottomSheetBehavior.setState(shouldShow ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);

    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadUI();
    }
}
