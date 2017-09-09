package cn.sh.changxing.wmdemo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cn.sh.changxing.wmdemo.R;
import cn.sh.changxing.wmdemo.consts.Consts;
import cn.sh.changxing.yuanyi.logger.LoggerFactory;

public class ShowMsgService extends Service
        implements View.OnClickListener, View.OnTouchListener, View.OnKeyListener {
    private static final int MSG_ADD_WINDOW = 0;
    private static final int MSG_REMOVE_WINDOW = 1;

    private BinderImpl mBinder = new BinderImpl();
    private WindowManager mWM;
    private HandlerThread mThread;
    private H mH;
    private View mLayout;
    private Button mTitleBtn;
    private TextView mContentTv;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initViews();
        mThread = new HandlerThread("show_msg_service");
        mThread.start();
        mH = new H(mThread.getLooper());
        mWM = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
    }

    private void initViews() {
        mLayout = View.inflate(this, R.layout.layout_window, null);
        mTitleBtn = ((Button) mLayout.findViewById(R.id.btn_title));
        mContentTv = ((TextView) mLayout.findViewById(R.id.tv_content));

        mLayout.setFocusable(true);
        mLayout.setFocusableInTouchMode(true);

        mTitleBtn.setOnClickListener(this);
        mContentTv.setOnClickListener(this);
        mLayout.setOnClickListener(this);
        mLayout.setOnKeyListener(this);
        mLayout.setOnTouchListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doIntent(Intent intent) {
        int what = intent.getIntExtra(Consts.INTENT_KEY_WHAT, 0);
        switch (what) {
            case Consts.INTENT_VALUE_WHAT_ADD:
                mH.obtainMessage(MSG_ADD_WINDOW, intent).sendToTarget();
                break;
            case Consts.INTENT_VALUE_WHAT_REMOVE:
                mH.obtainMessage(MSG_REMOVE_WINDOW, intent).sendToTarget();
                break;
        }
    }

    private void doAddWindow(Message msg) {
        LoggerFactory.getDefault().beginMethod();
        Intent intent = (Intent) msg.obj;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.TRANSLUCENT;    // 透明
//        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;     // 屏幕长亮
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;     // 此窗口背后的窗口会变得灰暗
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        params.dimAmount = 0.7F;        // 背后的窗口昏暗程度
        params.windowAnimations = R.style.CustomWindow;     // 此窗口的动画
        params.alpha = 0.9F;        // 此窗口的透明度
        params.gravity = Gravity.TOP;   // 此窗口的位置
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = 600;

        mTitleBtn.setText(intent.getStringExtra(Consts.INTENT_KEY_TITLE));
        mContentTv.setText(intent.getStringExtra(Consts.INTENT_KEY_CONTENT));
//        mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mWM.addView(mLayout, params);
        LoggerFactory.getDefault().endMethod();
    }

    private void doRemoveWindow(Message msg) {
        LoggerFactory.getDefault().beginMethod();
        mWM.removeView(mLayout);
        LoggerFactory.getDefault().endMethod();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThread.quit();
        mH = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title:
                LoggerFactory.getDefault().d("title clicked");
                break;
            case R.id.tv_content:
                LoggerFactory.getDefault().d("content clicked");
                break;
            case R.id.ll_root:
                LoggerFactory.getDefault().d("root clicked");
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        LoggerFactory.getDefault().d("onTouch: {}", event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Rect r = new Rect();
            mLayout.getGlobalVisibleRect(r);
            if (!r.contains(x, y)) {
                doRemoveWindow(null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        LoggerFactory.getDefault().d("onKey: {}", event);
        boolean result = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                doRemoveWindow(null);
                result = true;
                break;
        }
        return result;
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_WINDOW:
                    doAddWindow(msg);
                    break;
                case MSG_REMOVE_WINDOW:
                    doRemoveWindow(msg);
                    break;
            }
        }
    }

    class BinderImpl extends Binder {
        public ShowMsgService getWindowService() {
            return ShowMsgService.this;
        }
    }
}
