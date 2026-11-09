package com.example.kazanamaziasistani;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    private RelativeLayout rootLayout;
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        

        rootLayout = new RelativeLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        myWebView = new WebView(this);
        myWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings ws = myWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        
        ws.setMediaPlaybackRequiresUserGesture(false);

        myWebView.setWebViewClient(new WebViewClient());

        final ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (false) {
                    if (newProgress == 100) progressBar.setVisibility(View.GONE);
                    else {
                        if (progressBar.getVisibility() == View.GONE) progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(newProgress);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
                mUploadMessage = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "Dosya Seç"), FILE_CHOOSER_RESULT_CODE);
                } catch (Exception e) {
                    mUploadMessage = null;
                    return false;
                }
                return true;
            }

            
        });

        myWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void downloadJson(String fileName, String jsonContent) {
                try {
                    ContentResolver resolver = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (OutputStream os = resolver.openOutputStream(uri)) {
                            if (os != null) {
                                os.write(jsonContent.getBytes("UTF-8"));
                                os.flush();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Yedek dosyası İndirilenler klasörüne kaydedildi!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Kayıt hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "AndroidBridge");

        myWebView.loadUrl("file:///android_asset/index.html");

        rootLayout.addView(myWebView);
        rootLayout.addView(progressBar);
        setContentView(rootLayout);

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                WindowInsetsController controller = getWindow().getInsetsController();
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (mUploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && intent != null) {
                String dataString = intent.getDataString();
                if (dataString != null) results = new Uri[]{Uri.parse(dataString)};
            }
            mUploadMessage.onReceiveValue(results);
            mUploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        
        if (myWebView != null && myWebView.canGoBack()) myWebView.goBack();
        else super.onBackPressed();
        
    }
}
