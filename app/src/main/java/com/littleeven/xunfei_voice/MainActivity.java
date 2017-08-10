package com.littleeven.xunfei_voice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.iflytek.sunflower.FlowerCollector;

public class MainActivity extends AppCompatActivity {

    private TextView mtv1;
    private TextView mtv2;
    private TextView mtv3;
    private TextView mtv4;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_main);
        initID();
        mtv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,VoiceActivity.class);
                startActivity(i);
            }
        });
        mtv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,GrammarActivity.class);
                startActivity(i);
            }
        });
        mtv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,SemanticsActivity.class);
                startActivity(i);
            }
        });
        mtv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,SpeechActivity.class);
                startActivity(i);
            }
        });
    }

    private void initID() {
        mtv1= (TextView) findViewById(R.id.tv1);
        mtv2= (TextView) findViewById(R.id.tv2);
        mtv3= (TextView) findViewById(R.id.tv3);
        mtv4= (TextView) findViewById(R.id.tv4);
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
}
