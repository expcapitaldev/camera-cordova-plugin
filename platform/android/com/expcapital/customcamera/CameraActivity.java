package com.expcapital.customcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String SUBTITLE_KEY = "SUBTITLE_KEY";
    private static final String MSG_KEY = "MSG_KEY";
    private static final String CANCEL_KEY = "CANCEL_KEY";
    private static final String TYPE_KEY = "TYPE_KEY";
    private static final String TAG = "CameraActivity";
    private static final String MSGPROC_KEY = "MSG_PROC_KEY";
    private static final String MSGTAKE_KEY = "MSG_TAKE_KEY";

    private CameraView cameraView;
    private ImageView button;
    private TextView cancel;
    private TextView message;
    private ViewfinderView viewFinder;
    private ViewfinderView.Type type;
    private String msgproc;
    private ProgressBar progress;
    private String take;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();

        setContentView(res.getIdentifier("activity_camera", "layout", getPackageName()));
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        cameraView = findViewById(res.getIdentifier("camera", "id", getPackageName()));
        progress = findViewById(res.getIdentifier("progress_bar", "id", getPackageName()));
        cameraView.setPlaySounds(false);
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
                cameraView.stop();
                message.setText(msgproc);
                CameraUtils.decodeBitmap(jpeg, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(final Bitmap bitmap) {
                        final Handler handler = new Handler();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap cropped = null;
                                if (ViewfinderView.Type.PASSPORT.equals(type) || ViewfinderView.Type.LICENCE.equals(type)) {
                                    try {
                                        int width = bitmap.getWidth();
                                        int height = bitmap.getHeight();
                                        float ratio = type.getAspectRatio();
                                        if (width <= height) {
                                            int croppedHeight = (int) (width * ratio);
                                            int croppedY = (height - croppedHeight) / 2;
                                            cropped = Bitmap.createBitmap(bitmap, 0, croppedY, width, croppedHeight);
                                        } else {
                                            int croppedWidth = (int) (height * ratio);
                                            int croppedX = (width - croppedWidth) / 2;
                                            cropped = Bitmap.createBitmap(bitmap, croppedX, 0, croppedWidth, height);
                                        }
                                        bitmap.recycle(); // Recycle only in case of success
                                    } catch (Exception e) {
                                        // Ignore error, falls back to original image below
                                    }
                                }
                                ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
                                if (cropped != null) {
                                    cropped.compress(Bitmap.CompressFormat.JPEG, 85, jpeg_data);
                                    cropped.recycle();
                                } else {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, jpeg_data);
                                    bitmap.recycle();
                                }
                                final String encodedImage = Base64.encodeToString(jpeg_data.toByteArray(), Base64.NO_WRAP);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            CustomCamera.onSuccess(encodedImage);
                                            CameraActivity.this.finish();
                                        } catch (Exception e) {
                                            CustomCamera.onError(CustomCamera.ErrorCodes.ERROR_PROCESSING_IMAGE);
                                        }
                                    }
                                }, 1000);
                            }
                        });
                    }
                });
            }
        });
        button = findViewById(res.getIdentifier("shot", "id", getPackageName()));
        button.setOnClickListener(this);
        message = findViewById(res.getIdentifier("suggest", "id", getPackageName()));

        cancel = findViewById(res.getIdentifier("cancel", "id", getPackageName()));
        cancel.setOnClickListener(this);

        viewFinder = findViewById(res.getIdentifier("view_finder", "id", getPackageName()));
        TextView number = findViewById(res.getIdentifier("number", "id", getPackageName()));
        TextView side = findViewById(res.getIdentifier("side", "id", getPackageName()));

        try {
            Bundle args = getIntent().getExtras();
            if (args != null) {
                String title = args.getString(TITLE_KEY, "");
                String subtitle = args.getString(SUBTITLE_KEY, "");
                String msg = args.getString(MSG_KEY, "");
                String cancl = args.getString(CANCEL_KEY, "");

                take = args.getString(MSGTAKE_KEY, "");
                msgproc = args.getString(MSGPROC_KEY);
                if (!TextUtils.isEmpty(cancl)) {
                    cancel.setText(cancl);
                }
                if (!TextUtils.isEmpty(msg)) {
                    message.setText(msg);
                }
                type = ViewfinderView.Type.valueOf(args.getString(TYPE_KEY, ""));

                if (ViewfinderView.Type.FACE.equals(type)) {
                  cameraView.setFacing(Facing.FRONT);
                } else {
                  cameraView.setFacing(Facing.BACK);
                }

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
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
            button.setClickable(false);
            message.setText(take);
            progress.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            button.setEnabled(false);
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

    public static Intent prepareIntent(Context ctx, String title, String subtitle, String cancel, String msg, String msgproc, String msgtake, ViewfinderView.Type type) {
        Intent intent = new Intent(ctx, CameraActivity.class);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(SUBTITLE_KEY, subtitle);
        intent.putExtra(CANCEL_KEY, cancel);
        intent.putExtra(MSG_KEY, msg);
        intent.putExtra(MSGPROC_KEY, msgproc);
        intent.putExtra(MSGTAKE_KEY, msgtake);
        intent.putExtra(TYPE_KEY, type.name());
        return intent;
    }

}
