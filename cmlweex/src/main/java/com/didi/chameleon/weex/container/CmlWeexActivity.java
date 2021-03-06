package com.didi.chameleon.weex.container;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.didi.chameleon.sdk.CmlEngine;
import com.didi.chameleon.sdk.CmlEnvironment;
import com.didi.chameleon.sdk.CmlInstanceManage;
import com.didi.chameleon.sdk.ICmlInstance;
import com.didi.chameleon.sdk.ICmlLaunchCallback;
import com.didi.chameleon.sdk.container.CmlContainerActivity;
import com.didi.chameleon.sdk.container.ICmlActivity;
import com.didi.chameleon.sdk.utils.CmlLogUtil;
import com.didi.chameleon.sdk.widget.CmlTitleView;
import com.didi.chameleon.weex.CmlWeexEngine;
import com.didi.chameleon.weex.CmlWeexInstance;
import com.didi.chameleon.weex.R;

import java.util.HashMap;
import java.util.Set;

/**
 * 用来展示 Chameleon Weex 页面，通过{@link Launch} 或者{@link CmlWeexEngine#launchPage(Activity, String, HashMap)}进行启动
 *

 * @since 18/5/24
 */
public class CmlWeexActivity extends CmlContainerActivity implements CmlWeexInstance.ICmlInstanceListener, ICmlActivity {
    private static final String TAG = "CmlWeexActivity";
    protected CmlWeexInstance mWXInstance;

    protected View loadingView;
    private CmlTitleView titleView;
    private View objectView;
    private ViewGroup viewContainer;
    private ViewGroup reload;
    private View refreshView;
    /*
     * 判断view是否有效，我们认为View在onCreate~onDestroy之间为有效
     */
    private boolean mIsViewValid;
    private HashMap<String, Object> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String instanceId = getIntent().getStringExtra(PARAM_INSTANCE_ID);
        final int requestCode = getIntent().getIntExtra(PARAM_REQUEST_CODE, -1);
        mWXInstance = new CmlWeexInstance(this, this, instanceId, requestCode);
        mWXInstance.onCreate();
        mIsViewValid = true;
        setContentView(R.layout.cml_container_activity);
        initView();


        sdcardPermissionCheck(this);
    }

    private void initView() {
        titleView = findViewById(R.id.cml_weex_title_bar);
        loadingView = findViewById(R.id.cml_weex_loading_layout);
        viewContainer = findViewById(R.id.cml_weex_content);
        reload = findViewById(R.id.reload);
        objectView = viewContainer;
//        titleView.showLeftBackDrawable(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        refreshView = findViewById(R.id.refresh_btn);
        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String url = intent.getStringExtra(PARAM_URL);
                if (TextUtils.isEmpty(url)) {
                    CmlLogUtil.e(TAG, "url cant be null !");
                    return;
                }

                mWXInstance.clearCache(url);
                mWXInstance.reload(url);



            }
        });


        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String url = intent.getStringExtra(PARAM_URL);
                if (TextUtils.isEmpty(url)) {
                    CmlLogUtil.e(TAG, "url cant be null !");
                    return;
                }

                reload.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                mWXInstance.reload(url);
            }
        });
    }

    protected  View getRefreshView(){
        return refreshView;
    }

    private boolean mForceUpdate = false;
    public void setForceUpdate(boolean update){
        mForceUpdate = update;
    }

    @Override
    protected void renderByUrl() {
        setLoadingMsg(getString(R.string.cml_loading_msg));

        Intent intent = getIntent();
        String url = intent.getStringExtra(PARAM_URL);
        if (TextUtils.isEmpty(url)) {
            CmlLogUtil.e(TAG, "url cant be null !");
            return;
        }
        if (mWXInstance != null) {
            options = (HashMap<String, Object>) intent.getSerializableExtra(PARAM_OPTIONS);
            options.put("forceupdate", mForceUpdate);
            mWXInstance.renderByUrl(url, options);
        }
    }

    protected void setLoadingMsg(String msg){
        if(loadingView!=null){
            TextView textView = loadingView.findViewById(R.id.loadingMsg);
            textView.setText(msg);
        }
    }


    @Override
    public String getInstanceId() {
        if (null != mWXInstance) {
            return mWXInstance.getInstanceId();
        }
        return null;
    }

    @Override
    public void onRenderSuccess() {
//        loadingView.setVisibility(View.GONE);
//        titleView.setVisibility(View.GONE);
        reload.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mWXInstance != null) {
            mWXInstance.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mWXInstance != null) {
            mWXInstance.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWXInstance != null) {
            mWXInstance.onStop();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsViewValid = false;
        if (mWXInstance != null) {
            mWXInstance.onDestroy();
        }
    }


    public boolean isDevPackage(){
        return true;
    }



    @Override
    public void onException(String url, String errCode, String msg) {
        loadingView.setVisibility(View.GONE);
//        reload.setVisibility(View.VISIBLE);
        try{
            if(CmlEnvironment.getLoggerAdapter() != null){
                CmlEnvironment.getLoggerAdapter().e("bundlemanager", "cmlweexactivity url: " + url + " errcode: " + errCode + " msg: " + msg);
            }
        }catch (Exception e){

        }

    }

    @Override
    public void onDegradeToH5(final String url, final int degradeCode) {
        try{
            if(CmlEnvironment.getLoggerAdapter() != null){
                CmlEnvironment.getLoggerAdapter().e("bundlemanager", "cmlweexactivity  url: " + url + " degradeCode: " + degradeCode);
            }

        }catch (Exception e){

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (CmlEnvironment.getDegradeAdapter() != null) {
                    CmlEnvironment.getDegradeAdapter().degradeActivity(CmlWeexActivity.this, url, CmlWeexActivity.this.options,
                            degradeCode);
                }

                loadingView.setVisibility(View.GONE);
                reload.setVisibility(View.VISIBLE);

            }
        });



//        finish();
    }


    public void showDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onViewCreate(View view) {
        objectView = view;
        viewContainer.addView(view);
        loadingView.setVisibility(View.GONE);
        refreshView.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                titleView.setVisibility(View.GONE);
            }
        },100);


    }

    @Override
    public void updateNaviTitle(String title) {
        // do nothing, web only need to update title
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Nullable
    @Override
    public View getObjectView() {
        return objectView;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean isActivity() {
        return true;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public boolean isInDialog() {
        return false;
    }

    @Override
    public boolean isValid() {
        return mIsViewValid;
    }

    @Override
    public void finishSelf() {
        finish();
    }

    @Override
    public void setPageResult(int resultCode, Intent data) {
        setResult(resultCode, data);
    }

    @Override
    public void overrideAnim(int enterAnim, int exitAnim) {
        overridePendingTransition(enterAnim, exitAnim);
    }

    public static final class Launch {
        private String url;
        private Activity activity;
        private HashMap<String, Object> options;
        private int requestCode;
        private ICmlLaunchCallback launchCallback;

        public Launch(Activity activity, String url) {
            this.url = url;
            this.activity = activity;
        }

        public CmlWeexActivity.Launch addOptions(HashMap<String, Object> options) {
            this.options = options;
            return this;
        }

        public CmlWeexActivity.Launch addRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public CmlWeexActivity.Launch addLaunchCallback(ICmlLaunchCallback launchCallback) {
            this.launchCallback = launchCallback;
            return this;
        }

        private Intent getIntent(){
            if(activity != null && activity.getApplication().getApplicationInfo().packageName.equals("com.didi.chameleon.example")){
                return new Intent(activity, CmlWeexActivity.class);
            }else{
                return new Intent("com.didi.chameleon.weex.action.WEEX");
            }
        }

        private Intent buildIntent(String instanceId) {
            Intent intent = getIntent();
            intent.putExtra(PARAM_URL, url);
            intent.putExtra(PARAM_REQUEST_CODE, requestCode);
            intent.putExtra(PARAM_INSTANCE_ID, instanceId);
            if (options != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(PARAM_OPTIONS, options);
                intent.putExtras(bundle);
            }
            return intent;
        }

        public void launch() {
            final String instanceId = CmlEngine.getInstance().generateInstanceId();
            activity.startActivity(buildIntent(instanceId));
        }

        public Intent buildIntent(){
            String instanceId = CmlEngine.getInstance().generateInstanceId();
            Intent intent = getIntent();
            intent.putExtra(PARAM_URL, url);
            intent.putExtra(PARAM_REQUEST_CODE, requestCode);
            intent.putExtra(PARAM_INSTANCE_ID, instanceId);
            if (options != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(PARAM_OPTIONS, options);
                intent.putExtras(bundle);
            }
            return intent;
        }

        public void launchForResult() {
            final String instanceId = CmlEngine.getInstance().generateInstanceId();
            if (null != launchCallback) {
                // 注册到管理类
                CmlInstanceManage.getInstance().addLaunchCallback(instanceId, new ICmlLaunchCallback() {
                    @Override
                    public void onResult(@NonNull ICmlInstance cmlInstance, int requestCode, int resultCode, String result) {
                        launchCallback.onResult(cmlInstance, requestCode, resultCode, result);
                    }

                    @Override
                    public void onCreate() {
                        launchCallback.onCreate();
                    }

                    @Override
                    public void onResume() {
                        launchCallback.onResume();
                    }

                    @Override
                    public void onPause() {
                        launchCallback.onPause();
                    }

                    @Override
                    public void onStop() {
                        launchCallback.onStop();
                    }

                    @Override
                    public void onDestroy() {
                        CmlInstanceManage.getInstance().removeLaunchCallback(instanceId);
                        launchCallback.onDestroy();
                    }
                });
            }
            activity.startActivityForResult(buildIntent(instanceId),requestCode);
        }
    }
}
