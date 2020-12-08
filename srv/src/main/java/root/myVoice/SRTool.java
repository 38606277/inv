package  root.myVoice;


import com.iflytek.cloud.speech.*;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.ArrayList;

/**
 * niuhao 2017/12/4.
 */
public class SRTool {
    /** 每次等待时间 */
    private int perWaitTime = 100;

    private StringBuffer mResult = new StringBuffer();

    /** 最大等待时间， 单位ms */
    private int maxWaitTime = 500;

    /** 出现异常时最多重复次数 */
    private int maxQueueTimes = 3;
    /** 音频文件 */
   private  MultipartFile file;
    static {
        SpeechUtility.createUtility("appid=5cac4812");//申请的appid
    }
    /**
       * 输入流转字节流
       * */
    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int ch;
        while ((ch = is.read(buffer)) != -1) {
            bytestream.write(buffer,0,ch);
            }
        byte data[] = bytestream.toByteArray();
        bytestream.close();
        return data;
    }
    public String voice2words(MultipartFile file) throws InterruptedException, IOException {
        //1.创建SpeechRecognizer对象
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer();
        //2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        //3.开始听写
        mIat.startListening(mRecoListener);

      InputStream inputStream=  file.getInputStream();

        byte[] buf=InputStreamToByte(inputStream);
        //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
        ArrayList<byte[]> buffers = splitBuffer(buf, buf.length, 4800);
        for (int i = 0; i < buffers.size(); i++) {
            // 每次写入msc数据4.8K,相当150ms录音数据
            mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
        }
        mIat.stopListening();

        while (mIat.isListening()) {
            Thread.sleep(perWaitTime);
        }

        return mResult + "";
    }

    public String voice2words(byte[] buf) throws InterruptedException, IOException {
        //1.创建SpeechRecognizer对象
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer();
        //2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        //3.开始听写
        mIat.startListening(mRecoListener);
//        InputStream inputStream=  file.getInputStream();
//        byte[] buf=InputStreamToByte(inputStream);
        //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
        ArrayList<byte[]> buffers = splitBuffer(buf, buf.length, 4800);
        for (int i = 0; i < buffers.size(); i++) {
            // 每次写入msc数据4.8K,相当150ms录音数据
            mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
        }
        mIat.stopListening();

        while (mIat.isListening()) {
            Thread.sleep(perWaitTime);
        }

        return mResult + "";
    }

    public String voice2words(String  fileName) throws InterruptedException, IOException {
        //1.创建SpeechRecognizer对象
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer();
        //2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        //3.开始听写
        mIat.startListening(mRecoListener);
        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException("要读取的文件不存在");
        }
        FileInputStream fis = new FileInputStream(file);
        int len = 0;
        byte[] buf = new byte[fis.available()];
        fis.read(buf);
        fis.close();
        //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
        ArrayList<byte[]> buffers = splitBuffer(buf, buf.length, 4800);
        for (int i = 0; i < buffers.size(); i++) {
            // 每次写入msc数据4.8K,相当150ms录音数据
            mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
        }
        mIat.stopListening();

        while (mIat.isListening()) {
            Thread.sleep(perWaitTime);
        }
        if (file.exists()){
            file.delete();
        }
        return mResult + "";
    }

    /**
     * 将字节缓冲区按照固定大小进行分割成数组
     *
     * @param buffer 缓冲区
     * @param length 缓冲区大小
     * @param spsize 切割块大小
     * @return
     */
    private ArrayList<byte[]> splitBuffer(byte[] buffer, int length, int spsize) {
        ArrayList<byte[]> array = new ArrayList<byte[]>();
        if (spsize <= 0 || length <= 0 || buffer == null
                || buffer.length < length)
            return array;
        int size = 0;
        while (size < length) {
            int left = length - size;
            if (spsize < left) {
                byte[] sdata = new byte[spsize];
                System.arraycopy(buffer, size, sdata, 0, spsize);
                array.add(sdata);
                size += spsize;
            } else {
                byte[] sdata = new byte[left];
                System.arraycopy(buffer, size, sdata, 0, left);
                array.add(sdata);
                size += left;
            }
        }
        return array;
    }

    //听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            System.out.println("Result:" + results.getResultString());
            mResult.append(results.getResultString());
        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, String arg3) {
            // TODO Auto-generated method stub

        }

        //会话发生错误回调接口
        @Override
        public void onError(SpeechError error) {
            // TODO Auto-generated method stub
            System.out.println(error.getErrorCode() + "==========" + error.getErrorDesc());
            System.out.println(error);
        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBeginOfSpeech() {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 根据byte数组，生成文件
     * filePath  文件路径
     * fileName  文件名称（需要带后缀，如*.jpg、*.java、*.xml）
     */
    public static Boolean getFile(byte[] bfile, String filePath) {
        boolean ist=false;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
            ist=true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return ist;
    }

    /**
     * @Description MP3转换pcm
     * @param mp3Stream
     *            原始文件流
     * @return 转换后的二进制
     * @throws Exception
     * @author liuyang
     * @blog http://www.pqsky.me
     * @date 2018年1月30日
     */
    public byte[] mp3Convertpcm(InputStream mp3Stream) throws Exception {
        // 原MP3文件转AudioInputStream
        AudioInputStream mp3audioStream = AudioSystem.getAudioInputStream(mp3Stream);
        // 将AudioInputStream MP3文件 转换为PCM AudioInputStream
        AudioInputStream pcmaudioStream = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED,
                mp3audioStream);
        byte[] pcmBytes = IOUtils.toByteArray(pcmaudioStream);
        pcmaudioStream.close();
        mp3audioStream.close();
        return pcmBytes;
    }
}
