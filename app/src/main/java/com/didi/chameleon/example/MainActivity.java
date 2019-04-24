package com.didi.chameleon.example;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.didi.chameleon.sdk.CmlEngine;
import com.didi.chameleon.sdk.bundle.CmlBundle;
import com.didi.chameleon.sdk.utils.Util;
import com.didi.chameleon.weex.jsbundlemgr.CmlJsBundleEnvironment;
import com.google.zxing.client.android.CaptureActivity;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0x1;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0x2;
    // 演示打开一般的URL
    private static final String URL_NORMAL = "https://www.didiglobal.com";
    // 这是一个可以正常打开的 JS_BUNDLE
    private static final String URL_JS_BUNDLE_OK = "http://172.29.1.198:8000/cml/h5/index?wx_addr=http%3A%2F%2F172.29.1.198%3A8000%2Fweex%2Fzds.js%3Ft%3D1556090612553&path=%2Fpages%2Findex%2Findex";
    // 这是一个错误的 JS_BUNDLE
    private static final String URL_JS_BUNDLE_ERR = "https://www.didiglobal.com?cml_addr=xxx.js";
    // 这是一个测试预加载的 JS_BUNDLE
    private static final String URL_JS_BUNDLE_PRELOAD = "https://static.didialift.com/pinche/gift/chameleon-ui-builtin/web/chameleon-ui-builtin.html?cml_addr=https%3A%2F%2Fstatic.didialift.com%2Fpinche%2Fgift%2Fchameleon-ui-builtin%2Fweex%2Fchameleon-ui-builtin.js";
    // 演示Module 和 JS 通信
    private static final String URL_MODULE_DEMO = "file:///android_asset/WebTest.html";

    private TextView txtOpenUrl;
    private TextView txtOpenJSBundle;
    private TextView txtPreload;
    private TextView txtDegrade;
    private TextView txtModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOpenUrl = findViewById(R.id.txt_open_url);
        txtOpenJSBundle = findViewById(R.id.txt_open_js_bundle);
        txtPreload = findViewById(R.id.txt_preload);
        txtDegrade = findViewById(R.id.txt_degrade);
        txtModule = findViewById(R.id.txt_module);
        txtOpenUrl.setOnClickListener(this);
        txtOpenJSBundle.setOnClickListener(this);
        txtPreload.setOnClickListener(this);
        txtDegrade.setOnClickListener(this);
        txtModule.setOnClickListener(this);

        // 在业务层设置预加载地址
        CmlEngine.getInstance().initPreloadList(getPreloadList());
        // 执行预加载
        CmlEngine.getInstance().performPreload();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_open_url:
                CmlEngine.getInstance().launchPage(this, URL_NORMAL, null);
                break;
            case R.id.txt_open_js_bundle:
                CmlEngine.getInstance().launchPage(this, URL_JS_BUNDLE_OK, null);
                break;
            case R.id.txt_preload:
//                CmlEngine.getInstance().launchPage(this, URL_JS_BUNDLE_PRELOAD, null);
//                weexdebug();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    startActivity(new Intent(this, CaptureActivity.class));
                }

                break;
            case R.id.txt_degrade:
                CmlEngine.getInstance().launchPage(this, URL_JS_BUNDLE_ERR, null);
                break;
            case R.id.txt_module:
                CmlEngine.getInstance().launchPage(this, URL_MODULE_DEMO, null);
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, CaptureActivity.class));
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "request camara permission fail!", Toast.LENGTH_SHORT).show();
        }
    }
    private void weexdebug(){
        String url = "http://172.28.3.158:8088/devtool_fake.html?_wx_devtool=ws://172.28.3.158:8088/debugProxy/native/f5f277c0-5b68-4f9c-a3f4-6d10f40796b1";
        Uri uri = Uri.parse(url);
        if (uri.getPath().contains("dynamic/replace")) {

        } else if (uri.getQueryParameterNames().contains("_wx_devtool")) {

            WXEnvironment.sDebugServerConnectable = true;
            WXEnvironment.sRemoteDebugMode = true;
            WXEnvironment.sDebugMode = true;
//                    WXBridgeManager.updateGlobalConfig("wson_off");
            WXEnvironment.sRemoteDebugProxyUrl = uri.getQueryParameter("_wx_devtool");

            WXSDKEngine.reload();
        }
    }

    private List<CmlBundle> getPreloadList() {
        CmlJsBundleEnvironment.DEBUG = true;
        List<CmlBundle> cmlModels = new ArrayList<>();
        CmlBundle model = new CmlBundle();
        model.bundle = Util.parseCmlUrl(URL_JS_BUNDLE_PRELOAD);
        model.priority = 2;
        cmlModels.add(model);
        return cmlModels;
    }
}
