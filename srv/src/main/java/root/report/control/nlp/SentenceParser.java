package root.report.control.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;

public class SentenceParser {


	public static SentenceParser parser(String input){
		Segment nShortSegment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);

		String testCase = input;
		List<String> paramsString = new ArrayList<String>();
		String functionNameString = "";
		List<Term> outPut = nShortSegment.seg(testCase);
		CoNLLSentence coNLLSentence= HanLP.parseDependency(testCase);
		System.out.println(outPut.toString());
		for(Term term : outPut){
			String wordString = term.word;
			String verbString = term.nature.toString();
			if(verbString == "v" || verbString == "n" || verbString == "vn"){
				functionNameString+=wordString;
			}else if(verbString == "t" || verbString == "nt"||verbString == "ns"||verbString == "nz"){
				paramsString.add(wordString);
			}
		}

		SentenceParser businessParser=new SentenceParser();
		businessParser.setFunctionName(functionNameString);
//		businessParser.setInNames(paramsString);

		return  businessParser;
	}

	public static SentenceParser parser1(String input){

		SentenceParser sentenceParser=new SentenceParser();
		sentenceParser.setFunctionName("不能解析函数名称！");
		List<CoNLLWord> paramsString = new ArrayList<CoNLLWord>();
		List<Term> list=HanLP.segment(input);
		System.out.println(list);
		CoNLLSentence coNLLSentence= HanLP.parseDependency(input);
		System.out.println(coNLLSentence);
 		for(CoNLLWord coNLLWord : coNLLSentence.word) {

			//匹配函数：查找最后一个动宾关系,
			if (coNLLWord.ID == coNLLSentence.word.length) {
				if (coNLLWord.DEPREL.equals("动宾关系")) {
					sentenceParser.setFunctionName(coNLLWord.HEAD.LEMMA + coNLLWord.LEMMA);
					continue;
				}
			}
			//名词参数：
			if(coNLLWord.POSTAG.startsWith("n")
					&&(coNLLWord.ID != coNLLSentence.word.length))
			{
				paramsString.add(coNLLWord);
				continue;
			}
			//代词参数：
			if(coNLLWord.POSTAG.startsWith("r")
					&&(coNLLWord.ID != coNLLSentence.word.length))
			{
				paramsString.add(coNLLWord);
				continue;
			}
			//数词参数：
			if(coNLLWord.POSTAG.startsWith("m")
					&&(coNLLWord.ID != coNLLSentence.word.length))
			{
				paramsString.add(coNLLWord);
				continue;
			}
			//量词参数：
			if(coNLLWord.POSTAG.startsWith("g")
					&&(coNLLWord.ID != coNLLSentence.word.length))
			{
				paramsString.add(coNLLWord);
				continue;
			}
		}
		sentenceParser.setInNames(paramsString);
		return sentenceParser;

	}

	public static String parserFuncName(String input){

		CoNLLSentence coNLLSentence= HanLP.parseDependency(input);
		for(CoNLLWord coNLLWord : coNLLSentence.word) {

			//查找最后一个动宾关系,
 			if (coNLLWord.ID == coNLLSentence.word.length) {
				if (coNLLWord.DEPREL.equals("动宾关系")) {
					return coNLLWord.HEAD.NAME + coNLLWord.NAME;


				}

			}
		}
		return "不能解析查询名称!";

	}


	public String getFunctionName() {
		return functionName;
	}



	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	private String functionName;

	public List<CoNLLWord> getInNames() {
		return inNames;
	}

	public void setInNames(List<CoNLLWord> inNames) {
		this.inNames = inNames;
	}

	private List<CoNLLWord> inNames;




}
