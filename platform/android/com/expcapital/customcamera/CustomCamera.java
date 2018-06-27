/**
 */
package com.expcapital.customcamera;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

public class CustomCamera extends CordovaPlugin {

    private static WeakReference<CustomCamera> sInstance;
    private static final String TAG = "CustomCameraPlugin";
    private static final int CAMERA_REQUEST_CODE = 100500;
    private CallbackContext mCallbackContext;

    public enum ErrorCodes {
        CANCELLED, CAMERA_ERROR, FILE_NOT_FOUND, ERROR_PROCESSING_IMAGE, NO_PERMISSION,
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "Initializing CustomCamera");
        sInstance = new WeakReference<CustomCamera>(this);
        cordova.setActivityResultCallback(this);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        if ("takePicture".equals(action)) {
            String title = null;
            String subtitle = null;
            String msg = null;
            String msgproc = null;
            String cancel = null;
            String msgtake = null;
            ViewfinderView.Type type = null;
            try {
                if (args != null && args.length() == 1) {
                    JSONArray params = args.getJSONArray(0);
                    title = params.getString(0);
                    subtitle = params.getString(1);
                    cancel = params.getString(2);
                    msg = params.getString(3);
                    type = ViewfinderView.Type.valueOf(params.getString(4));
                    msgproc = params.optString(5, "Processing");
                    msgtake = params.optString(6, "Taking picture");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            takePicture(title, subtitle, cancel, msg, msgproc, msgtake, type);
        }
        return true;
    }

    private void takePicture(String title, String subtitle, String cancel, String msg, String msgproc, String msgtake, ViewfinderView.Type type) {
        cordova.startActivityForResult(this, CameraActivity.prepareIntent(cordova.getActivity(), title, subtitle, cancel, msg, msgproc, msgtake, type), CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        super.onRestoreStateForActivityResult(state, callbackContext);
        mCallbackContext = callbackContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            mCallbackContext.error("CANCELLED");
        }
    }

    public static void onSuccess(File file) {
        if (sInstance != null) {
            CustomCamera c = sInstance.get();
            if (c != null) {
                c.mCallbackContext.success(file.getAbsolutePath());
            }
        }
    }

    public static void onSuccess(String file) {
        if (sInstance != null) {
            CustomCamera c = sInstance.get();
            if (c != null) {
                c.mCallbackContext.success(file);
            }
        }
    }

    public static void onError(ErrorCodes msg) {
        if (sInstance != null) {
            CustomCamera c = sInstance.get();
            if (c != null) {
                c.mCallbackContext.error(msg.name());
            }
        }
    }
}
