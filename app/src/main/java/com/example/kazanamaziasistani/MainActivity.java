package com.example.kazanamaziasistani;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        rootLayout = new RelativeLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        myWebView = new WebView(this);
        myWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WebSettings ws = myWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setSupportZoom(false);
        ws.setMediaPlaybackRequiresUserGesture(false);
        myWebView.setWebViewClient(new WebViewClient());
        final ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        myWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
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
                                Toast.makeText(MainActivity.this, "Yedek İndirilenler klasörüne kaydedildi!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AndroidBridge");
        myWebView.loadUrl("file:///android_asset/index.html");
        rootLayout.addView(myWebView);
        rootLayout.addView(progressBar);
        setContentView(rootLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                android.view.WindowInsetsController controller = getWindow().getInsetsController();
                controller.hide(android.view.WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView != null && myWebView.canGoBack()) myWebView.goBack(); else super.onBackPressed();
    }
}