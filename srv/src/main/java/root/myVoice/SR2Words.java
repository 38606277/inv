package root.myVoice;
import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class SR2Words {
    public static String sr2words(String jsonString) {
        StringBuffer sb = new StringBuffer();
        String[] split = jsonString.split("}]}]}");
        for (int i = 0; i < split.length; i++) {
            String s = split[i] + "}]}]}";
            System.out.println(s);
            Map parse = (Map) JSON.parse(s);
            List<Map> ws = (List<Map>) parse.get("ws");
            for (int i1 = 0; i1 < ws.size(); i1++) {
                List<Map> cw = (List<Map>) ws.get(i1).get("cw");
                String w = cw.get(0).get("w").toString();
                sb.append(w);
            }

        }
        return sb.toString();
    }
}
