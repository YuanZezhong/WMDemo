package cn.sh.changxing.wmdemo.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.sh.changxing.wmdemo.R;
import cn.sh.changxing.wmdemo.consts.Consts;
import cn.sh.changxing.wmdemo.service.ShowMsgService;
import cn.sh.changxing.yuanyi.logger.LoggerFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mShowToastBtn;
    private Button mAddWindowBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        registerListeners();
    }

    private void initViews() {
        mAddWindowBtn = ((Button) findViewById(R.id.btn_add_window));
        mShowToastBtn = ((Button) findViewById(R.id.btn_toast));
    }

    private void registerListeners() {
        mAddWindowBtn.setOnClickListener(this);
        mShowToastBtn.setOnClickListener(this);
    }

    private void doAddWindow() {
        Intent intent = new Intent(this, ShowMsgService.class);
        intent.putExtra(Consts.INTENT_KEY_WHAT, Consts.INTENT_VALUE_WHAT_ADD);
        intent.putExtra(Consts.INTENT_KEY_TITLE, "标题区域");
        intent.putExtra(Consts.INTENT_KEY_CONTENT, "内容区域");
        startService(intent);
    }

    private void doShowToast() {
        Toast toast = new Toast(this);
        View toastLayout = View.inflate(this, R.layout.layout_toast, null);
        TextView textView = (TextView) toastLayout.findViewById(R.id.tv_toast);
        textView.setOnClickListener(this);
        toast.setView(toastLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        try {
            Field mTNField = Toast.class.getDeclaredField("mTN");
            mTNField.setAccessible(true);
            Object mTN = mTNField.get(toast);
            Field mParamsField = mTN.getClass().getDeclaredField("mParams");
            mParamsField.setAccessible(true);
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mParamsField.get(mTN);
            mParams.windowAnimations = R.style.ToastStyle;  // Toast弹出动画
            mParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;    // 目的是为了让Toast能够接收触摸事件

            /* 以下是更改Toast的显示时长为最少7秒, 会存在一些问题*/
            Field mHandlerField = mTN.getClass().getDeclaredField("mHandler");
            mHandlerField.setAccessible(true);
            final Handler mHandler = (Handler) mHandlerField.get(mTN);
            Field mHideField = mTN.getClass().getDeclaredField("mHide");
            mHideField.setAccessible(true);
            final Runnable old_mHide = (Runnable) mHideField.get(mTN);
            Runnable new_mHide = new Runnable() {
                @Override
                public void run() {
                    mHandler.postDelayed(old_mHide, 7000);
                }
            };
            mHideField.set(mTN, new_mHide);
        } catch (Exception e) {
            LoggerFactory.getDefault().e(e);
        }
        toast.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_window:
                doAddWindow();
                break;
            case R.id.btn_toast:
                doShowToast();
                break;
            case R.id.tv_toast:
                Toast.makeText(this, "Toast受到点击", Toast.LENGTH_SHORT).show();
                LoggerFactory.getDefault().d("clicked toast");
                break;
        }
    }
}
