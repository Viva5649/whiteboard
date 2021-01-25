package com.netease.whiteboardandroiddemo.whiteboard;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.whiteboardandroiddemo.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

public class WhiteboardJsInterface {
    private static final String TAG = "WhiteboardJsInterface";
    private final WeakReference<WhiteboardContractView> contractReference;

    private final Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {
        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            WhiteboardContractView contract = contractReference.get();
            if (contract == null) {
                return;
            }
            contract.onKickOut(chatRoomKickOutEvent);
        }
    };

    private final Observer<List<ChatRoomMessage>> receiveMessageObserver = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messageList) {
            WhiteboardContractView contract = contractReference.get();
            if (contract == null) {
                return;
            }
            contract.onReceiveMessage(messageList);
        }
    };

    /** Instantiate the interface and set the context */
    public WhiteboardJsInterface(WhiteboardContractView contractView) {
        this.contractReference = new WeakReference<>(contractView);
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void NativeFunction(String toast) {
        try {
            JSONObject obj = new JSONObject(toast);
            String action = obj.getString("action");
            JSONObject param = obj.getJSONObject("param");

            Log.i(TAG, String.format("called by js, toast=%s, obj=%s, param=%s", toast, obj, param));
            switch (action) {
                // Web页面已经准备好了
                case "webPageLoaded":
                    login();
                    enableDraw();
                    setBrushColor();
                    break;
                // NIM登录成功
                case "webLoginIMSucceed":
                    Log.i(TAG, "login Nim succeed");
                    break;
                // NIM登录失败
                case "webLoginIMFailed":
                // 网络异常
                case "webJoinWBFailed":
                // 网络异常
                case "webCreateWBFailed":
                // 白板信令通道已断开
                case "webLeaveWB":
                // 网络异常
                case "webError":
                    onLogout();
                    break;
                // Js运行时报错
                case "webJsError":
                default:
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "execute native function error", e);
        }
    }

    private void onLogout() {
        WhiteboardContractView contract = contractReference.get();
        if (contract == null) {
            return;
        }
        contract.finish();
    }

    private void login() throws JSONException {
        Log.i(TAG, "login");
        WhiteboardContractView contract = contractReference.get();
        if (contract == null) {
            return;
        }
        JSONObject jsParam = new JSONObject();
        JSONObject param = new JSONObject();
        jsParam.put("action", "jsLoginIMAndJoinWB");
        jsParam.put("param", param);
        param.put("account", contract.getAccount());
        param.put("ownerAccount", contract.getOwnerAccount());
        //这里请传入token
        param.put("token", Constant.TOKEN);
        param.put("channelName", contract.getChannel());
        param.put("record", true);
        param.put("debug", true);
        param.put("platform", "android");
        param.put("appKey", Constant.APP_KEY);
        runJs((jsParam.toString()));
    }

    public void logout() {
        Log.i(TAG, "logout");
        WhiteboardContractView contract = contractReference.get();
        if (contract == null) {
            return;
        }
        JSONObject jsParam = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            jsParam.put("action", "jsLogoutIMAndLeaveWB");
            jsParam.put("param", param);
            runJs((jsParam.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "failed to send logout to js", e);
        }
    }

    private void enableDraw() throws JSONException {
        JSONObject jsParam = new JSONObject();
        JSONObject param = new JSONObject();
        jsParam.put("action", "jsEnableDraw");
        jsParam.put("param", param);
        param.put("enable", true);
        runJs((jsParam.toString()));
    }

    private void setBrushColor() throws JSONException {
        int randomInt = new Random().nextInt(Constant.BUSH_COLOR_ARRAY.length - 2);
        String colorStr = Constant.BUSH_COLOR_ARRAY[randomInt + 2];
        Log.i(TAG, String.format("set bush color %s with %s", colorStr, randomInt));
        JSONObject jsParam = new JSONObject();
        JSONObject param = new JSONObject();
        jsParam.put("action", "jsSetColor");
        jsParam.put("param", param);
        param.put("color", colorStr);
        runJs((jsParam.toString()));
    }

    private void runJs(final String param) {
        WhiteboardContractView contract = contractReference.get();
        if (contract == null) {
            return;
        }
        WebView whiteboardView = contract.getWhiteboardView();
        final String escapedParam = param.replaceAll("\"", "\\\\\"");
        whiteboardView.post(() -> {
            Log.i("native function call js", "javascript:WebJSBridge(\"" + escapedParam + "\")");
            whiteboardView.loadUrl("javascript:WebJSBridge(\"" + escapedParam + "\")");
        });
    }

    public void registerObservers(boolean register) {
        ChatRoomServiceObserver chatRoomServiceObserver = NIMClient.getService(ChatRoomServiceObserver.class);
        chatRoomServiceObserver.observeKickOutEvent(kickOutObserver, register);
        chatRoomServiceObserver.observeReceiveMessage(receiveMessageObserver, register);
    }
}