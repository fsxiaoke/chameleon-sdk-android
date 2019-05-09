package com.didi.chameleon.weex.jsbundlemgr;

import android.content.Context;

import com.didi.chameleon.sdk.bundle.CmlBundle;
import com.didi.chameleon.weex.jsbundlemgr.code.CmlCodeManager;
import com.didi.chameleon.weex.jsbundlemgr.code.CmlGetCodeStringCallback;
import com.didi.chameleon.weex.jsbundlemgr.utils.CmlLogUtils;
import com.didi.chameleon.weex.jsbundlemgr.utils.CmlUtils;

import java.util.List;

/**
 * limeihong
 * create at 2018/10/15
 */
public class CmlJsBundleEngine implements CmlJsBundleManager {
    private static final String TAG = CmlJsBundleEngine.class.getSimpleName();

    private static final class JS_BUNDLE_ENGINE {
        private static final CmlJsBundleEngine INSTANCE = new CmlJsBundleEngine();
    }


    private CmlJsBundleManager  fsBundleManager;
    private volatile boolean isInit = false;

    private CmlJsBundleEngine() {
    }

    public static CmlJsBundleEngine getInstance() {
        return JS_BUNDLE_ENGINE.INSTANCE;
    }

    /**
     * jsbundleMgr初始化
     *
     * @param context              Context
     * @param cmlJsBundleMgrConfig 预加载的相关配置 {@link CmlJsBundleMgrConfig}
     */
    @Override
    public void initConfig(Context context, CmlJsBundleMgrConfig cmlJsBundleMgrConfig) {
        if (isInit) {
            return;
        }
        if (cmlJsBundleMgrConfig == null) {
            throw new NullPointerException("CmlJsBundleMgrConfig is null");
        }
        if (!CmlUtils.isMainThread()) {
            throw new RuntimeException("请在主线程初始化CmlJsBundleEngine");
        }
        CmlCodeManager.getInstance().init(context, cmlJsBundleMgrConfig);
        try {
            // engine 获取并初始化
            Class cmlFsBundleManager = Class.forName("com.fxiaoke.fscommon.weex.bundle.cmlFsBundleManager");
            if (null == cmlFsBundleManager) {
                return;
            }

            fsBundleManager = (CmlJsBundleManager) cmlFsBundleManager.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        isInit = true;
    }

    @Override
    public void setPreloadList(List<CmlBundle> preloadList) {
        CmlCodeManager.getInstance().setPreloadList(preloadList);
        if(fsBundleManager != null){
            fsBundleManager.setPreloadList(preloadList);
        }
    }

    /**
     * 开始预加载
     */
    @Override
    public void startPreload() {
        if (!isInit) {
            CmlLogUtils.e(TAG, "请先初始化CmlJsBundleEngine");
            return;
        }
        CmlCodeManager.getInstance().startPreload();
        if(fsBundleManager != null){
            fsBundleManager.startPreload();
        }
    }

    /**
     * 获取需要解析的js
     *
     * @param url                      js路径
     * @param cmlGetCodeStringCallback 获取code的回调
     */
    @Override
    public void getWXTemplate(String url, CmlGetCodeStringCallback cmlGetCodeStringCallback) {
        if (!isInit) {
            CmlLogUtils.e(TAG, "请先初始化CmlJsBundleEngine");
            return;
        }

        if(url.startsWith("http://")){
            CmlCodeManager.getInstance().getCode(url, cmlGetCodeStringCallback);
        }else {
            if(fsBundleManager != null){
                fsBundleManager.getWXTemplate(url, cmlGetCodeStringCallback);
            }
        }


    }
}
