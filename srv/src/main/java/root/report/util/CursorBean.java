package root.report.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/2/21.
 */
public class CursorBean implements Serializable{


    public List<Map<Object,Object>> v_name;

    public List<Map<Object, Object>> getV_name() {
        return v_name;
    }

    public void setV_name(List<Map<Object, Object>> v_name) {
        this.v_name = v_name;
    }
}
