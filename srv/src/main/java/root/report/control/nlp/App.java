package root.report.control.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.mining.word2vec.DocVectorModel;
import com.hankcs.hanlp.mining.word2vec.WordVectorModel;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import root.report.service.NLPService;

import java.util.Map;

public class App
{
    public static void main( String[] args )

    {
        NLPService nlpService=new NLPService();
        CustomDictionary.add("发生纠纷");
        Map map=nlpService.ExecNLP("双方因黄包车拉客发生纠纷");
        System.out.println(map);


        //        CustomDictionary.add("采购订单");
//        CustomDictionary.add("华为公司");
//        CustomDictionary.add("供应商资质");
//        CustomDictionary.add("部门负责人");
//        CustomDictionary.add("湖北移动");
//        CustomDictionary.add("2018年");

        CustomDictionary.add("可用预算额度");
        CustomDictionary.add("王起航");
        CustomDictionary.add("收单人");
        CustomDictionary.add("订单号");
        CustomDictionary.add("小米6手机");
        CustomDictionary.add("电话号码");
        CustomDictionary.add("邮箱");
        CustomDictionary.add("本月发生额");
        CustomDictionary.add("供应商信息");


        Segment nShortSegment = new NShortSegment();
//                .enableCustomDictionary(true)
//                .enablePlaceRecognize(true)
//                .enableOrganizationRecognize(true);
//        nShortSegment.seg("查询华为公司2018年的采购订单");

//        System.out.println(nShortSegment.seg("查询华为公司大于10000的采购订单"));
//        System.out.println(HanLP.segment("查询华为公司2018年2月的采购订单"));
//        System.out.println(HanLP.segment("查询工程部2018年2月的可用预算额度"));
//        System.out.println(HanLP.segment("查询王起航2018年2月的工资"));
//        System.out.println(HanLP.segment("查询订单号是2000的收单人"));
//        System.out.println(HanLP.segment("查询小米6手机的价格"));
//        System.out.println(HanLP.segment("查询王起航的电话号码"));
//        System.out.println(HanLP.segment("查询王起航的邮箱"));

//        printNearestDocument("体育", documents, docVectorModel);
//        printNearestDocument("农业", documents, docVectorModel);
//        printNearestDocument("我要看比赛", documents, docVectorModel);
//        printNearestDocument("要不做饭吧", documents, docVectorModel);


        // 动态增加

        CoNLLSentence coNLLSentence= HanLP.parseDependency("查询华为的供应商信息");
        System.out.println(coNLLSentence);
//        for(CoNLLWord coNLLWord : coNLLSentence.word)
//        {
//            System.out.println(coNLLWord.ID);
//            System.out.println(coNLLWord.NAME);
////            System.out.println(coNLLWord.CPOSTAG);
//            System.out.println(coNLLWord.DEPREL);
//            System.out.println(coNLLWord.HEAD.ID);
//            System.out.println(coNLLWord.HEAD.NAME);
////            System.out.println(coNLLWord.LEMMA);
////            System.out.println(coNLLWord.POSTAG);
//
//        }
////        System.out.println(coNLLSentence);
//        System.out.println(coNLLSentence);
//        System.out.println(HanLP.segment("查询华为2018年的采购订单"));
//        System.out.println(HanLP.parseDependency("查询华为2018年的采购订单"));
//        System.out.println(CRFDependencyParser.compute("查询刘德华的项目金额信息"));

//        System.out.println(NLPTokenizer.analyze("查询刘德华的项目"));
//        System.out.println(NLPTokenizer.segment("查询刘德华的项目"));
        //System.out.println(HanLP.segment("查询2017年华为有限公司的采购订单"));
        //new Semantic().GetResult("查询2017年银行存款的科目余额");
//        new Semantic().GetResult("查询华为的供应商信息");
//        System.out.println(SentenceParser..parser("删除2015年的财务数据"));
//    	System.out.println(SentenceParser.parser("查询2017年华为有限公司的采购订单"));
//    	System.out.println(SentenceParser.parser("查询2015年电讯盈科的年报"));
//    	System.out.println(SentenceParser.parser("删除2015年的财务数据"));
    }
}
