package com.petersamokhin.bots.sdk.utils.vkapi;

/**
 * Settings for interacting with Callback API
 */
public class CallbackApiSettings {

    private String host = null, path;
    private int port = 80;
    private boolean autoAnswer = false;
    public final String confirmationCode;

    public CallbackApiSettings(String confCode, String host, int port, String path, boolean autoAnswer, boolean autoSet) {
    	this.confirmationCode = confCode;
        this.host = host;
        this.path = path;
        this.port = port;
        this.autoAnswer = autoAnswer;
        CallbackApiHandler.autoSetEvents = autoSet;
    }

    /* Getters */
    String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    int getPort() {
        return port;
    }

    boolean isAutoAnswer() {
        return autoAnswer;
    }
}
