package com.colorpeeker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;

public class CustomModule extends ReactContextBaseJavaModule {

    private static DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter = null;

    private static WritableNativeMap colorsList;

    ReactApplicationContext reactApplicationContext;

    CustomModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactApplicationContext = reactContext;
    }

    @Override
    public void initialize() {
        super.initialize();

        eventEmitter = getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    static void updateBuildVersionString(String version) {
        eventEmitter.emit("BUILD_VERSION", version);
    }

    static void sendImageUri(String uri) {
        eventEmitter.emit("IMAGE_SOURCE", uri);
    }

    static void saveColorsList(WritableNativeMap list) {
        colorsList = list;
    }

    @NonNull
    @Override
    public String getName() {
        return "CustomModule";
    }

    @ReactMethod
    void shareColorsToWhatsapp(String text, String uri) {

        File file = new File(uri);

        Uri imageUri = FileProvider.getUriForFile(
                reactApplicationContext,
                "com.colorpeeker.provider", //(use your app signature + ".provider" )
                file);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
        sendIntent.setType("image/jpeg");
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        reactApplicationContext.startActivity(sendIntent);
    }

    @ReactMethod
    void getLastColorsList() {
        WritableNativeMap localList = colorsList;
        eventEmitter.emit("COLORS_LIST", localList);
    }

    @ReactMethod
    void navigateToNativeActivity() {
        Intent intent = new Intent(reactApplicationContext, NativeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactApplicationContext.startActivity(intent);
    }

    @ReactMethod
    void copyToClipboard(String label, String color) {

        ClipboardManager clipboard = (ClipboardManager)
                reactApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(label, color);

        clipboard.setPrimaryClip(clip);

    }

}
