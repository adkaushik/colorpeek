package com.colorpeeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.lang.annotation.Native;
import java.util.List;

public class NativeActivity extends ReactActivity {

    private static final int REQUEST_CODE_CHOOSE = 1001;
    List<Uri> mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_activity);
        Log.d("NativeActivity", BuildConfig.VERSION_NAME);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomModule.updateBuildVersionString(BuildConfig.VERSION_NAME);
                Matisse.from(NativeActivity.this)
                        .choose(MimeType.ofImage())
                        .countable(true)
                        .maxSelectable(1)
                        .thumbnailScale(0.85f)
                        .imageEngine(new GlideEngine())
                        .showPreview(false) // Default is `true`
                        .forResult(REQUEST_CODE_CHOOSE);
            }
        });

        findViewById(R.id.matisse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matisse.from(NativeActivity.this)
                        .choose(MimeType.ofImage(), true)
                        .countable(true)
                        .maxSelectable(1)
                        .thumbnailScale(0.85f)
                        .showPreview(false) // Default is `true`
                        .forResult(REQUEST_CODE_CHOOSE);
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public String getRealPathFromURI (Uri contentUri) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
            String actualPath = getRealPathFromURI(mSelected.get(0));
            Log.d("Matisse", "mSelected: URL " + actualPath);
            Bitmap bitmap = BitmapFactory.decodeFile(actualPath);
            Log.d("Matisse", "height " + bitmap.getHeight());


            Palette p = createPaletteSync(bitmap);
//            p.getSwatches();

            int darkVibrantInt = p.getDarkVibrantColor(getResources().getColor(android.R.color.transparent));
            String darkVibrant = String.format("#%06X", 0xFFFFFF & darkVibrantInt);

            int darkMutedInt = p.getDarkMutedColor(getResources().getColor(android.R.color.transparent));
            String darkMuted = String.format("#%06X", 0xFFFFFF & darkMutedInt);

            int lightVibrantInt = p.getLightVibrantColor(getResources().getColor(android.R.color.transparent));
            String lightVibrant = String.format("#%06X", 0xFFFFFF & lightVibrantInt);

            int lightMutedColor = p.getLightMutedColor(getResources().getColor(android.R.color.transparent));
            String lightMuted = String.format("#%06X", 0xFFFFFF & lightMutedColor);

            int dominantInit = p.getDominantColor(getResources().getColor(android.R.color.transparent));
            String dominant = String.format("#%06X", 0xFFFFFF & dominantInit);

            Log.d("Matisse: ", darkVibrant);
            Log.d("Matisse: ", darkMuted);
            Log.d("Matisse: ", lightVibrant);
            Log.d("Matisse: ", lightMuted);
            Log.d("Matisse: ", dominant);

            WritableNativeMap colorsList =  new WritableNativeMap();
            colorsList.putString("dominant", dominant);
            colorsList.putString("lightMuted", lightMuted);
            colorsList.putString("lightVibrant", lightVibrant);
            colorsList.putString("darkMuted", darkMuted);
            colorsList.putString("darkVibrant", darkVibrant);


            CustomModule.saveColorsList(colorsList);
            CustomModule.sendImageUri(actualPath);
            finish();
        }
    }
}