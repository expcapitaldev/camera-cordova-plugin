package com.expcapital.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String SUBTITLE_KEY = "SUBTITLE_KEY";
    private static final String MSG_KEY = "MSG_KEY";
    private static final String CANCEL_KEY = "CANCEL_KEY";
    private static final String TYPE_KEY = "TYPE_KEY";
    private static final String TAG = "CameraActivity";

    private CameraView cameraView;
    private ImageView button;
    private TextView cancel;
    private TextView message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        setContentView(res.getIdentifier("activity_camera", "layout", getPackageName()));
        cameraView = findViewById(res.getIdentifier("camera", "id", getPackageName()));
        cameraView.addCameraListener(new CameraListener() {

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                CustomCamera.onError(CustomCamera.ErrorCodes.CAMERA_ERROR);
                CameraActivity.this.finish();
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                try {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File pictureFile = new File(getFilesDir(), File.separator + timeStamp + ".base64");

                    String encodedImage = Base64.encodeToString(jpeg, Base64.NO_WRAP);

                    PrintWriter fos = new PrintWriter(pictureFile);
                    fos.println(encodedImage);
                    fos.close();
                    CustomCamera.onSuccess(pictureFile);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                    CustomCamera.onError(CustomCamera.ErrorCodes.FILE_NOT_FOUND);
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                    CustomCamera.onError(CustomCamera.ErrorCodes.ERROR_ACCESSING_FILE);
                }
                CameraActivity.this.finish();
            }
        });
        button = findViewById(res.getIdentifier("shot", "id", getPackageName()));
        button.setOnClickListener(this);
        message = findViewById(res.getIdentifier("suggest", "id", getPackageName()));

        cancel = findViewById(res.getIdentifier("cancel", "id", getPackageName()));
        cancel.setOnClickListener(this);

        ViewfinderView viewFinder = findViewById(res.getIdentifier("view_finder", "id", getPackageName()));
        TextView number = findViewById(res.getIdentifier("number", "id", getPackageName()));
        TextView side = findViewById(res.getIdentifier("side", "id", getPackageName()));

        ViewfinderView.Type type;
        try {
            Bundle args = getIntent().getExtras();
            if (args != null) {
                String title = args.getString(TITLE_KEY, "");
                String subtitle = args.getString(SUBTITLE_KEY, "");
                String msg = args.getString(MSG_KEY, "");
                String cancl = args.getString(CANCEL_KEY, "");
                if (!TextUtils.isEmpty(cancl)) {
                    cancel.setText(cancl);
                }
                if (!TextUtils.isEmpty(msg)) {
                    message.setText(msg);
                }
                type = ViewfinderView.Type.valueOf(args.getString(TYPE_KEY, ""));
                if (TextUtils.isEmpty(title)) {
                    number.setVisibility(View.GONE);
                } else {
                    number.setText(title);
                }
                side.setText(subtitle);
                viewFinder.setViewfinderType(type);
            }
        } catch (Exception e) {
            // Wrong init..
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
        showSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    @Override
    public void onClick(View v) {
        Resources res = getResources();
        if (v.getId() == res.getIdentifier("shot", "id", getPackageName())) {
            cameraView.capturePicture();
        } else if (v.getId() == res.getIdentifier("cancel", "id", getPackageName())) {
            setResult(Activity.RESULT_CANCELED);
            CameraActivity.this.finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !cameraView.isStarted()) {
            cameraView.start();
        } else {
            CustomCamera.onError(CustomCamera.ErrorCodes.NO_PERMISSION);
            finish();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    public static Intent prepareIntent(Context ctx, String title, String subtitle, String cancel, String msg, ViewfinderView.Type type) {
        Intent intent = new Intent(ctx, CameraActivity.class);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(SUBTITLE_KEY, subtitle);
        intent.putExtra(CANCEL_KEY, cancel);
        intent.putExtra(MSG_KEY, msg);
        intent.putExtra(TYPE_KEY, type.name());
        return intent;
    }

}
