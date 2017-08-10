package com.littleeven.xunfei_voice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VoiceActivity extends AppCompatActivity {

    private static String TAG = VoiceActivity.class.getSimpleName();
    // 语音听写对象
    private  SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    int ret = 0; // 函数调用返回值
    private Toast mToast;
    private TextView mText;
    private Button stopBtn;
    private Button startBtn;
    private Button cancelBtn;
    private SharedPreferences mSharedPreferences;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private boolean mTranslateEnable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_voice);
    }

    public void Start(View v){
        // 开始听写
        // 如何判断一次听写结束：OnResult isLast=true 或者 onError
        // 移动数据分析，收集开始听写事件
        FlowerCollector.onEvent(VoiceActivity.this, "iat_recognize");

        mText.setText(null);// 清空显示内容
        mIatResults.clear();
        boolean isShowDialog = mSharedPreferences.getBoolean(
                "iat_show", true);
        if (isShowDialog) {
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            showToast("请开始说话…");
        } else {
            // 不显示听写对话框
            //!
            mIat.startListening((Intent) mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showToast("听写失败,错误码：" + ret);
            } else {
                showToast("请开始说话…");
            }
        }
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            if( mTranslateEnable ){
                printTransResult( results );
            }else{
                printResult(results);
            }

        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            if(mTranslateEnable && error.getErrorCode() == 14002) {
                showToast( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showToast(error.getPlainDescription(true));
            }
        }

    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showToast("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if(mTranslateEnable && error.getErrorCode() == 14002) {
                showToast( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showToast(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showToast("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if( mTranslateEnable ){
                printTransResult( results );
            }else{
                printResult(results);
            }

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showToast("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");

        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            showToast( "解析结果失败，请确认是否已开通翻译功能。" );
        }else{
            mText.setText( "原始语言:\n"+oris+"\n目标语言:\n"+trans );
        }

    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mText.setText(resultBuffer.toString());
//        mText..setSelection(mText.length());
    }

    @Override
    protected void onResume() {
        super.onResume();
        FlowerCollector.onResume(this);
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageStart(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlowerCollector.onPause(this);
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
    }

    private void showToast(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}
