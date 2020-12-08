package  root.myVoice;
import com.iflytek.cloud.speech.*;
/**
 *词表上传
 * @author Administrator
 *
 */

public class UploadUserWords {
	private void uploadUserWords() throws Exception {
		SpeechRecognizer recognizer = SpeechRecognizer.getRecognizer();
		UserWords userwords = new UserWords("单,继,坤");
		byte[] datas = userwords.toString().getBytes("utf-8");
		
		recognizer.setParameter( SpeechConstant.DATA_TYPE, "userword" );
		recognizer.updateLexicon("userwords", userwords.toString(),lexiconListener);
	}
	/**
	 * 词表上传监听器
	 */
	LexiconListener lexiconListener = new LexiconListener() {
		
		@Override
		public void onLexiconUpdated(String arg0, SpeechError arg1) {
			// TODO Auto-generated method stub
			
		}
	};
/*	LexiconListener lexiconListener = new LexiconListener() {
		
		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if (error == null)
				DebugLog.Log("*************上传成功*************");
			else
				DebugLog.Log("*************" + error.getErrorCode()+ "*************");
		}
	};
		*/
}
