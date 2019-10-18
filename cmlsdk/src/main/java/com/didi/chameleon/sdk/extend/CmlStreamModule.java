package com.didi.chameleon.sdk.extend;

import com.didi.chameleon.sdk.CmlEnvironment;
import com.didi.chameleon.sdk.module.CmlCallback;
import com.didi.chameleon.sdk.module.CmlMethod;
import com.didi.chameleon.sdk.module.CmlModule;
import com.didi.chameleon.sdk.module.CmlParam;

import org.json.JSONObject;

import java.util.Map;

import android.content.Context;

@CmlModule(alias = "stream")
public class CmlStreamModule {


    @CmlMethod(alias = "fetch", uiThread = false)
    public void fetch(Context context, @CmlParam(name = "method") String method, @CmlParam(name = "url") String url,
                      @CmlParam(name = "headers") JSONObject headers, @CmlParam(name = "body") String body,
                      @CmlParam(name = "type") String type, @CmlParam(name = "timeout") int timeout,
                      CmlCallback<Map> callback) {
        CmlEnvironment.getStreamHttp()
                .fetch(context,method, url, headers, body, type, timeout, callback, null);
    }

}
