package root.myVoice;

import com.baidu.aip.speech.AipSpeech;
import org.json.JSONObject;

/**
 * Created by Administrator on 2019/5/22.
 */
public class BaiduLib {
    /**
     * @Description 调用百度语音识别API
     * @param pcmBytes
     * @return
     * @author liuyang
     * @blog http://www.pqsky.me
     * @date 2018年1月30日
     */
    public static final String APP_ID = "16309438";
    public static final String API_KEY = "KCWcpKpzoQ3tHHzdanWRH9cv";
    public static final String SECRET_KEY = "fA22Gjd0qUrUahUW6IODA1Ql3bBVeOuo";
    public static JSONObject speechBdApi(byte[] pcmBytes) {
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 调用接口
        JSONObject res = client.asr(pcmBytes, "pcm", 16000, null);
        return res;
    }

}
