package com.example.lock;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initData();
    }

    private void initView(){
        BrightnessHelper.setActivityBrightness(0f, MainActivity.this);
    }

    private void initEvent(){

    }

    private void initData(){

    }

    @Override
    public void onDestroy(){
        BrightnessHelper.setActivityBrightness(-1f, MainActivity.this);
        super.onDestroy();
    }
}
