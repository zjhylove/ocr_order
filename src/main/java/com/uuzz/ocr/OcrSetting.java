package com.uuzz.ocr;

import java.util.ResourceBundle;

/**
 * 图文识别配置关联类
 *
 * @author zj
 * @time 2018-8-14
 */
public class OcrSetting {

    private static volatile ResourceBundle resource;

    private static final String PROPERTY_FILE_NAME = "ocr";

    /**
     * 建立连接的超时时间（单位：毫秒）
     */
    public static final String OCR_CONNECTION_TIMEOUT_IN_MILLIS = getConfigValue("ocr.connectionTimeoutInMillis");

    /**
     * 通过打开的连接传输数据的超时时间（单位：毫秒）
     */
    public static final String OCR_SOCKET_TIMEOUT_IN_MILLES = getConfigValue("ocr.socketTimeoutInMillis");

    /**
     * http代理服务器
     */
    public static final String OCR_HTTP_PROXY = getConfigValue("ocr.httpProxy");

    /**
     * socket代理服务器 （http和socket类型代理服务器只能二选一）
     */
    public static final String OCR_SOCKET_PROXY = getConfigValue("ocr.socketProxy");

    /**
     * 应用id
     */
    public static final String OCR_APP_ID = getConfigValue("ocr.app.id");

    /**
     * 接口关键字
     */
    public static final String OCR_API_KEY = getConfigValue("ocr.api.key");

    /**
     * 应用加密串
     */
    public static final String OCR_SECRET_KEY = getConfigValue("ocr.secret.key");


    /**
     * 根据key值，获取对应的value值
     *
     * @param key String
     * @return value - String
     */
    public static String getConfigValue(String key) {

        return getResource().getString(key);
    }

    /**
     * 单例获取
     *
     * @return
     */
    public static ResourceBundle getResource() {

        if (resource == null) {
            synchronized (OcrSetting.class) {
                if (resource == null) {
                    resource = ResourceBundle.getBundle(PROPERTY_FILE_NAME);
                }
            }
        }
        return resource;
    }
}