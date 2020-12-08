package root.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.dictionary.CustomDictionary;
//import com.mysql.cj.x.json.JsonArray;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.common.RO;
import root.report.control.nlp.SentenceParser;
import root.report.db.DbFactory;
import root.report.util.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NLPService {

    @Autowired
    private QueryService queryService;
    @Autowired
    private DictService dictService;

    //根据语义识别匹配函数，并返回结果
    /*
     *根据匹配度确定调用哪个函数
     * 1、将函数名、输出参数注入到hanlp的字典中
     * 2、hanlp分词
     * 3、匹配函数名称：查找动词和最后一个动宾结构，组成函数名称，并匹配qry_name,qry_out的名称，如果匹配则
     * 4、函数参数匹配：查找名词，并和宾语构成定中结构，将参数在dict表中查找对应发字典，根据字典匹配函数定义对应的数据字典
     * 5、为函数输入参数赋值：
     * 6、执行函数：
     * 7、返回结果
     *
     * */


    public Map ExecNLP(String aText) {

        SentenceParser sentenceParser = SentenceParser.parser1(aText);
        String wordFuncName = sentenceParser.getFunctionName();


        String currentQryId = "";
        String currentQryName = "";
        String currentQryClassId = "";
        String message = "查询失败";
        Map resultMap = new HashMap<String, Object>();


        //1-----------查找函数名在name表中和out表中是否存在-------------------//
        List<Map<String, Object>> listQuery = queryService.getQueryByName(wordFuncName);

        if ((listQuery != null) && (listQuery.size() > 0)) {
            currentQryId = listQuery.get(0).get("qry_id").toString();
            currentQryName = listQuery.get(0).get("qry_name").toString();
            currentQryClassId = listQuery.get(0).get("class_id").toString();

        }
        //如果没找到继续查询out表
        else if (listQuery.size() == 0) {

            List<Map> listOutQuery = this.getQueryByOutName(wordFuncName);
            if ((listOutQuery != null) && (listOutQuery.size() > 0)) {
                currentQryId = listOutQuery.get(0).get("qry_id").toString();
                currentQryName = listOutQuery.get(0).get("qry_name").toString();
                currentQryClassId = listOutQuery.get(0).get("class_id").toString();

            }//如果out表中也没找到则返回不存在
            else if (listOutQuery == null) {
                message = "函数名：" + wordFuncName + "不存在";
                resultMap.put("resultCode", "2000");
                resultMap.put("message", message);
                return resultMap;
            }


        }


        //2---------查找查询的输入输出参数，匹配参数------------------//
        List<CoNLLWord> wordInName = sentenceParser.getInNames();//语言中的
        List<Map> queryParam = this.GetInByQryId(currentQryId);

        //最终的参数,初始化参数为空串
        JSONObject in = new JSONObject();
        for (Map aIn : queryParam) {
            in.put(aIn.get("in_id").toString(), "");
        }

        //参数匹配，字典匹配，参数名匹配，词性匹配
        for (int i = 0; i < wordInName.size(); i++) {
            //根据数据字典匹配参数
            String aWord = wordInName.get(i).LEMMA;

            //根据nlp中的参数匹配数据字典的定义
            Map dict = this.getDictByValue(aWord);
            String aInID = "";

            //名词按字典名称匹配
            aInID = MatchByInDict(dict.get("dict_id").toString(), queryParam);
            if (aInID!=null) {
                in.put(aInID, aWord);
                continue;
            } else {
                //名词按参数名称匹配
                aInID = MatchByInName(dict.get("dict_name").toString(), queryParam);
                if (aInID!=null) {
                    in.put(aInID, aWord);
                    continue;
                }
            }

            //日期按词性匹配参数

            // 如果是代词我，则根据登录用户，查找对应的员工姓名

        }

        //3--------------生成查询的输入参数---------------//
        JSONArray params = new JSONArray();
        JSONObject param = new JSONObject();

        param.put("in", in);
        params.add(param);

        //4--------------执行查询返回结果-----------------------//
        Map map = new HashMap();
        try {
            map = queryService.executeSql(currentQryClassId, currentQryId, JSONArray.toJSONString(params, RO.features));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;

    }

    //根据名称，查找函数
    private String GetFuncName(String aText) {

        Map<String, String> param = new HashMap<String, String>();
        SqlSession sqlSession = DbFactory.Open("form");
        param.put("name", aText);
        List<Map> userList = sqlSession.selectList("function.getFunctionName", param);

        return userList.get(0).toString();


    }

    //根据名称，查找函数
    private String GetInName(String aText) {

        String aFuncName = DbFactory.Open("form").selectOne("function.getFunctionIn", "");
        return aFuncName;


    }

    //根据名字，查找对应的字典
    private String GetDictName(String indict) {


        String aFuncName = DbFactory.Open("form").selectOne("", "");
        return "";
    }

    //加载字典到内hanlp
    public int LoadQryNameAndOutName() {
        //加载name表qry_name
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        resultList = sqlSession.selectList("nlp.getQryNameAndOutName");
        for (int i = 0; i < resultList.size(); i++) {
            CustomDictionary.insert(resultList.get(i).get("qry_name"));
        }
        return 1;
    }

    //加载数据字典到内hanlp
    public int LoadDictValueName() {
        //加载name表qry_name
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        resultList = sqlSession.selectList("nlp.getDictValueName");
        for (int i = 0; i < resultList.size(); i++) {
            CustomDictionary.insert(resultList.get(i).get("value_name"));
        }
        return 1;
    }

    //查询
    public List<Map> GetInByQryId(String qry_id) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        //查找函数定义输入参数 qry_in
        Map<String, String> param = new HashMap<String, String>();
        param.put("qry_id", qry_id);
        List<Map> inList = sqlSession.selectList("query.getInByID", param);
        return inList;
    }


    //按参数名称匹配
    public String MatchByInName(String dict_name, List<Map> params) {

        List<Map> list = params.stream()
                .filter(x -> x.get("in_name").toString().indexOf(dict_name) > -1)
                .collect(Collectors.toList());
        if (list.size() > 0)
            return list.get(0).get("in_id").toString();
        else
            return null;
    }

    //按参数的数据字典匹配
    public String MatchByInDict(String dict_id, List<Map> params) {

        List<Map> list = params.stream()
                .filter(x -> Objects.nonNull(x.get("dict_id")))
                .filter(x -> x.get("dict_id").toString().equals(dict_id))
                .collect(Collectors.toList());
        if (list.size() > 0)
            return list.get(0).get("in_id").toString();
        else
            return null;

    }

    //按参数的数据类型匹配
    public String MatchByInDataType(String word, List<Map> params) {

        List<Map> list = params.stream()
                .filter(x -> x.get("datatype").toString().equals(word))
                .collect(Collectors.toList());



        return list.get(0).get("in_id").toString();
    }

    // 功能描述 : 根据dict_id 查询 func_dict信息
    public Map getDictByValue(String value_name) {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        Map<String, Object> map = new HashMap<>();
        map.put("value_name", value_name);
        Map<String, Object> dict = sqlSession.selectOne("dict.getDictByValue", map);


        return dict;
    }

    // 功能描述 : 根据dict_id 查询 func_dict信息
    public List<Map> getQueryByOutName(String out_name) {
        //去掉查询

        String v_out_name=out_name.replace("查询","");

        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        Map<String, Object> map = new HashMap<>();
        map.put("out_name", v_out_name);
        List<Map> qrys = sqlSession.selectList("nlp.getQryByOutName", map);


        return qrys;
    }

    public static void LoadNlpDict() {

        NLPService nlpService = new NLPService();
        nlpService.LoadQryNameAndOutName();
        nlpService.LoadDictValueName();

    }


}
