package com.netease.nim.camellia.mq.isolation.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.camellia.mq.isolation.core.config.MqIsolationConfig;
import com.netease.nim.camellia.mq.isolation.core.domain.ConsumerHeartbeat;
import com.netease.nim.camellia.mq.isolation.core.domain.SenderHeartbeat;
import com.netease.nim.camellia.mq.isolation.core.mq.MqInfo;
import com.netease.nim.camellia.mq.isolation.core.stats.model.ConsumerBizStatsRequest;
import com.netease.nim.camellia.mq.isolation.core.stats.model.SenderBizStatsRequest;
import okhttp3.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caojiajun on 2024/2/20
 */
public class CamelliaMqIsolationController implements MqIsolationController {
    private final String url;
    private final OkHttpClient okHttpClient;

    public CamelliaMqIsolationController(String url) {
        this.url = url;
        this.okHttpClient = new OkHttpClient();
    }

    @Override
    public MqIsolationConfig getMqIsolationConfig(String namespace) {
        try {
            String url = this.url + "/camellia/mq/isolation/config/getMqIsolationConfig?namespace=" + URLEncoder.encode(namespace, "utf-8");
            JSONObject result = invokeGet(okHttpClient, url);
            return result.getObject("data", MqIsolationConfig.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void reportConsumerBizStats(ConsumerBizStatsRequest request) {
        invokePost(okHttpClient, url + "/camellia/mq/isolation/route/reportConsumerBizStats", JSONObject.toJSONString(request));
    }

    @Override
    public void reportSenderBizStats(SenderBizStatsRequest request) {
        invokePost(okHttpClient, url + "/camellia/mq/isolation/route/reportSenderBizStats", JSONObject.toJSONString(request));
    }

    @Override
    public List<MqInfo> selectMqInfo(String namespace, String bizId) {
        try {
            String url = this.url + "/camellia/mq/isolation/config/selectMq?namespace="
                    + URLEncoder.encode(namespace, "utf-8") + "&bizId=" + URLEncoder.encode(bizId, "utf-8");
            JSONObject result = invokeGet(okHttpClient, url);
            JSONArray array = result.getJSONArray("data");
            List<MqInfo> list = new ArrayList<>();
            for (Object o : array) {
                list.add(JSONObject.parseObject(o.toString(), MqInfo.class));
            }
            return list;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void consumerHeartbeat(ConsumerHeartbeat heartbeat) {
        invokePost(okHttpClient, url + "/camellia/mq/isolation/heartbeat/consumerHeartbeat", JSONObject.toJSONString(heartbeat));
    }

    @Override
    public void senderHeartbeat(SenderHeartbeat heartbeat) {
        invokePost(okHttpClient, url + "/camellia/mq/isolation/heartbeat/senderHeartbeat", JSONObject.toJSONString(heartbeat));
    }

    public static JSONObject invokeGet(OkHttpClient okHttpClient, String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            int httpCode = response.code();
            if (httpCode != 200) {
                throw new IllegalStateException("http.code=" + httpCode);
            }
            String string = response.body().string();
            JSONObject json = JSONObject.parseObject(string);
            Integer code = json.getInteger("code");
            if (code == null || code != 200) {
                throw new IllegalStateException("code=" + code);
            }
            return json;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static JSONObject invokePost(OkHttpClient okHttpClient, String url, String body) {
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            int httpCode = response.code();
            if (httpCode != 200) {
                throw new IllegalStateException("http.code=" + httpCode);
            }
            String string = response.body().string();
            JSONObject json = JSONObject.parseObject(string);
            Integer code = json.getInteger("code");
            if (code == null || code != 200) {
                throw new IllegalStateException("code=" + code);
            }
            return json;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
