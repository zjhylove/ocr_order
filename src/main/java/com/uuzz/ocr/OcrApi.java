package com.uuzz.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.uuzz.ocr.OcrSetting;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 图文识别接口
 *
 * @author zj
 * @time 2018-8-14
 */
public class OcrApi {


    /**
     * 单例获取ocr接口对象
     */
    private static class OcrHolder {

        private static final AipOcr INSTANCE = new AipOcr(OcrSetting.OCR_APP_ID, OcrSetting.OCR_API_KEY, OcrSetting.OCR_SECRET_KEY);

    }

    public static AipOcr getOcr() {

        return OcrHolder.INSTANCE;
    }

    /**
     * 必要设置
     *
     * @param ocr
     */
    public static void necessarySetting(AipOcr ocr) {
        ocr.setConnectionTimeoutInMillis(Integer.valueOf(OcrSetting.OCR_CONNECTION_TIMEOUT_IN_MILLIS));
        ocr.setSocketTimeoutInMillis(Integer.valueOf(OcrSetting.OCR_SOCKET_TIMEOUT_IN_MILLES));
        if (OcrSetting.OCR_SOCKET_PROXY != null && !OcrSetting.OCR_SOCKET_PROXY.isEmpty()) {
            ocr.setSocketProxy(OcrSetting.OCR_SOCKET_PROXY, 80);
        }
        if (OcrSetting.OCR_HTTP_PROXY != null && !OcrSetting.OCR_HTTP_PROXY.isEmpty()) {
            ocr.setSocketProxy(OcrSetting.OCR_HTTP_PROXY, 80);
        }
    }

    /**
     * 建议参数
     *
     * @return
     */
    public static HashMap<String, String> adviseOptions() {
        HashMap<String, String> options = new HashMap<>(4);
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        return options;
    }

    public static JSONObject discernByPath(String imagePath) {

        AipOcr ocr = getOcr();
        necessarySetting(ocr);
        return ocr.basicAccurateGeneral(imagePath, adviseOptions());
    }

    public static JSONObject discernByBytes(byte[] imageBytes) {

        AipOcr ocr = getOcr();
        necessarySetting(ocr);

        ocr.basicAccurateGeneral(imageBytes, adviseOptions());
        return null;
    }

    public static JSONObject discernByUrl(String imageUrl) {
        AipOcr ocr = getOcr();
        necessarySetting(ocr);
        return ocr.basicGeneralUrl(imageUrl, adviseOptions());
    }

}
