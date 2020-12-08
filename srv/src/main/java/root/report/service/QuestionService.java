/*
package root.report.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.SynthesizeToUriListener;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.myVoice.XunfeiLib;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class QuestionService {

    private static Logger log = Logger.getLogger(QuestionService.class);

    */
/**
     * 功能描述: 根据JSON数据解析 对应数据，生成问题库记录
     *//*

    public String createQuestion(SqlSession sqlSession,JSONObject jsonObject) throws IOException {
        Map<String,Object> map  = new HashMap<>();
        map.put("ai_question",jsonObject.getString("ai_question"));
        //换成你在讯飞申请的APPID
        SpeechUtility.createUtility("appid=5cac4812");
        //合成监听器
        SynthesizeToUriListener synthesizeToUriListener = XunfeiLib.getSynthesize();
        String fileName=XunfeiLib.getFileName("ttstestq.pcm");
        XunfeiLib.delDone(fileName);
        //1.创建SpeechSynthesizer对象
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer( );
        //2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速，范围0~100
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置语调，范围0~100
        mTts.setParameter(SpeechConstant.VOLUME, "50");//设置音量，范围0~100
        //3.开始合成
        //设置合成音频保存位置（可自定义保存位置），默认保存在“./tts_test.pcm”
        mTts.synthesizeToUri(jsonObject.getString("ai_question"),fileName ,synthesizeToUriListener);
        //设置最长时间
        int timeOut=60;
        int star=0;
        //校验文件是否生成
        while(!XunfeiLib.checkDone(fileName)){
            try {
                Thread.sleep(1000);
                star++;
                if(star>timeOut){
                    throw new Exception("合成超过"+timeOut+"秒！");
                }
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
                break;
            }
        }
        //输出 wav IO流
            File file = new File(fileName);
            int len_l = (int) file.length();
            ByteArrayOutputStream out = null;
            try {
                FileInputStream in = new FileInputStream(file);
                out = new ByteArrayOutputStream();
                //写入WAV文件头信息
                out.write(XunfeiLib.getWAVHeader(len_l,8000,2,16));
                byte[] b = new byte[1024];
                int i = 0;
                while ((i = in.read(b)) != -1) {

                    out.write(b, 0, b.length);
                }
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            byte[] buf = out.toByteArray();
            //删除文件和清除队列信息
            XunfeiLib.delDone(fileName);
            file.delete();
        map.put("fileDataBlob",buf);

        sqlSession.insert("question.createQuestion",map);
        return String.valueOf(map.get("id"));
    }
    */
/**
     * 功能描述: 根据JSON数据解析 对应数据，生成 问题库音频 记录
     *//*

    public String createQuestionAudio(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        map.put("fileDataBlob",jsonObject.get("fileDataBlob"));
        sqlSession.insert("question.createQuestionAudio",map);
        return String.valueOf(map.get("id"));
    }

    // 功能描述 : 修改 问题 表的信息
    public void updateQuestion(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map=new HashMap();
        map.put("ai_question",jsonObject.getString("ai_question"));
        map.put("ai_question_id",jsonObject.getString("ai_question_id"));
        sqlSession.insert("question.updateQuestion",map);
    }
    // 功能描述 : 修改 问题 表音频的信息
    public void updateQuestionAudio(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map=new HashMap();
        map.put("ai_question_id",jsonObject.getString("ai_question_id"));
        map.put("fileDataBlob",jsonObject.get("fileDataBlob"));
        sqlSession.insert("question.updateQuestionAudio",map);
    }
    // 功能描述: 根据ai_question_id 删除信息
    public void deleteQuestion(SqlSession sqlSession,int ai_question_id){
        Map<String,Object> map=new HashMap();
        map.put("ai_question_id",ai_question_id);
        sqlSession.delete("question.deleteQuestion",map);
    }

    // 创建答案表记录
    public String createAnswer(SqlSession sqlSession,JSONObject jsonObject){
        // 只更新  value_name 即可
        Map<String,Object> map = new HashMap<>();
        map.put("question_id",jsonObject.getIntValue("question_id"));
        map.put("current",jsonObject.getString("current"));
        map.put("answer",jsonObject.getString("answer"));
        map.put("creat_by",jsonObject.getString("creat_by"));
        //换成你在讯飞申请的APPID
        SpeechUtility.createUtility("appid=5cac4812");
        //合成监听器
        SynthesizeToUriListener synthesizeToUriListener = XunfeiLib.getSynthesize();
        String fileName=XunfeiLib.getFileName("ttstesta.pcm");
        XunfeiLib.delDone(fileName);
        //1.创建SpeechSynthesizer对象
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer( );
        //2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速，范围0~100
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置语调，范围0~100
        mTts.setParameter(SpeechConstant.VOLUME, "50");//设置音量，范围0~100
        //3.开始合成
        //设置合成音频保存位置（可自定义保存位置），默认保存在“./tts_test.pcm”
        mTts.synthesizeToUri(jsonObject.getString("answer"),fileName ,synthesizeToUriListener);
        //设置最长时间
        int timeOut=60;
        int star=0;
        //校验文件是否生成
        while(!XunfeiLib.checkDone(fileName)){
            try {
                Thread.sleep(1000);
                star++;
                if(star>timeOut){
                    throw new Exception("合成超过"+timeOut+"秒！");
                }
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
                break;
            }
        }
        //输出 wav IO流
        File file = new File(fileName);
        int len_l = (int) file.length();
        ByteArrayOutputStream out = null;
        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            //写入WAV文件头信息
            out.write(XunfeiLib.getWAVHeader(len_l,8000,2,16));
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {

                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] buf = out.toByteArray();
        //删除文件和清除队列信息
        XunfeiLib.delDone(fileName);
        file.delete();
        map.put("fileDataBlob",buf);
        sqlSession.insert("question.createAnswer",map);
        return String.valueOf(map.get("id"));
    }
    // 创建答案表 音频记录
    public String createAnswerAudio(SqlSession sqlSession,JSONObject jsonObject){
        // 只更新  value_name 即可
        Map<String,Object> map = new HashMap<>();
        map.put("question_id",jsonObject.getIntValue("question_id"));
        map.put("fileDataBlob",jsonObject.get("fileDataBlob"));
        sqlSession.insert("question.createAnswerAudio",map);
        return String.valueOf(map.get("id"));
    }
    // 功能描述 : 修改 回答 表的信息
    public  void updateAnswer(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        map.put("answer_id",jsonObject.getIntValue("answer_id"));
        map.put("current",jsonObject.getIntValue("current"));
        map.put("answer",jsonObject.getString("answer"));
        map.put("creat_by",jsonObject.getString("creat_by"));
        sqlSession.update("question.updateAnswer",map);
    }
    // 功能描述 : 修改 回答 表 音频的信息
    public  void updateAnswerAudio(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        map.put("answer_id",jsonObject.getIntValue("answer_id"));
        map.put("fileDataBlob",jsonObject.get("fileDataBlob"));
        sqlSession.update("question.updateAnswerAudio",map);
    }

    // 功能描述: 根据 answer_id 删除回答
    public void deleteAnswer(SqlSession sqlSession,int answer_id){
        sqlSession.delete("question.deleteAnswer",answer_id);
    }

    // 功能描述: 根据 answer_id 全量删除
    public void deleteAnswerByqID(SqlSession sqlSession,int question_id){
        sqlSession.delete("question.deleteAnswerByqID",question_id);
    }

}
*/
