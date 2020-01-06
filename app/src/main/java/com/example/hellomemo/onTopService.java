package com.example.hellomemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class onTopService extends Service {
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    View mView;

    private Resources res;

    private View vPop;          //팝업뷰
    private TextView tvText;    //텍스트 (내용)

    private ImageView imgBackground;    //메모 이미지
    private ImageButton imgBtn;         //메모 버튼화 (메모 최소화)
    private ImageButton imgSizeBtn;     //사이즈 조절 버튼

    /** 이미지 리소스 **/
    private int memoBackId = R.drawable.popup_back;
    private int memoPopId = R.drawable.popup_button;
    private int memoSizeId = R.drawable.size_btn;

    // 메모 최소화
    private final int STATUS_BTN = -1;
    private final int STATUS_VIEW = 1;
    // 메모 최소화가 되었느냐 ( -1: 최소화, 1: 뷰모드)
    private int isMemoBtn = STATUS_VIEW;

    //팝업 사이즈 (초기사이즈)
    public int pop_width = 368;
    public int pop_height = 325;

    //사이즈와 메모 이동에 관한 좌표변수
    private float START_X, START_Y;
    private int PREV_X, PREV_Y;


    /*********************************************
     * 팝업뷰 터치 (팝업뷰 이동시 터치 리스너)
     ********************************************** */
    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    START_X = event.getRawX();
                    START_Y = event.getRawY();
                    PREV_X = params.x;
                    PREV_Y = params.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) (event.getRawX() - START_X);
                    int y = (int) (event.getRawY() - START_Y);
                    params.x = PREV_X + x;
                    params.y = PREV_Y + y;
                    wm.updateViewLayout(vPop, params);
                    break;
            }
            return true;
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        vPop = View.inflate(getApplicationContext(), R.layout.service_ontop, null);
        tvText = (TextView) vPop.findViewById(R.id.poptext);
        imgBackground = (ImageView) vPop.findViewById(R.id.popup_back);
        imgBtn = (ImageButton) vPop.findViewById(R.id.popbtn);
        imgSizeBtn = (ImageButton) vPop.findViewById(R.id.sideBtn);

        //팝업뷰 터치 사용
        vPop.setOnTouchListener(mViewTouchListener);


        /*********************************************
         * 팝업뷰 설정
         ********************************************** */
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.width = pop_width;
        params.height = pop_height;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.alpha = 100 / 100.0f;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE); //윈도 매니저
        wm.addView(vPop, params);
        imgBackground.setMaxWidth(params.width);
        imgBackground.setMaxHeight(params.height);

        /*********************************************
         * 이미지 버튼(최소화된) 이벤트 처리
         ********************************************** */
        imgBtn.setOnTouchListener(new View.OnTouchListener() {
            float down_x, down_y, pre_x, pre_y, up_x, up_y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_x = event.getRawX();
                        down_y = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isMemoBtn == STATUS_BTN) {
                            pre_x = up_x;
                            pre_y = up_y;
                            up_x = event.getRawX();
                            up_y = event.getRawY();

                            if (Math.abs(down_x - up_x) > 15 || Math.abs(down_y - up_y) > 15) {
                                params.x = params.x - (int) (pre_x - up_x);
                                params.y = params.y - (int) (pre_y - up_y);
                                wm.updateViewLayout(vPop, params);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        up_x = event.getRawX();
                        up_y = event.getRawY();
                        if (Math.abs(down_x - up_x) < 30 && Math.abs(down_y - up_y) < 30) {
                            isMemoBtn *= STATUS_BTN;
                            if (isMemoBtn == STATUS_BTN) {
                                imgBtn.setImageResource(memoPopId);
                                imgBackground.setVisibility(imgBackground.GONE);
                                tvText.setVisibility(View.GONE);
                                imgSizeBtn.setVisibility(View.GONE);

                                params.width = imgBtn.getWidth();
                                params.height = imgBtn.getHeight();
                                params.alpha = 100 / 100.0f;
                                wm.updateViewLayout(vPop, params);
                            } else {
                                imgBtn.setImageResource(R.drawable.popup_btn);
                                imgBackground.setVisibility(imgBackground.VISIBLE);
                                tvText.setVisibility(View.VISIBLE);
                                imgSizeBtn.setVisibility(View.VISIBLE);
                                params.width = pop_width;
                                params.height = pop_height;
                                //params.alpha = popupData.clearValue / 100.0f;
                                wm.updateViewLayout(vPop, params);
                            }
                        }
                        break;
                }
                return false;
            }
        });


        /*********************************************
         * 이미지 사이즈 크기 조절 이벤트
         ********************************************** */
        imgSizeBtn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (isMemoBtn == STATUS_BTN) return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        START_X = event.getRawX();
                        START_Y = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) (event.getRawX() - START_X);
                        int y = (int) (event.getRawY() - START_Y);
                        if (params.width >= 300) params.width += x;
                        else if (x > 0) params.width += x;
                        if (params.height >= 180) params.height += y;
                        else if (y > 0) params.height += y;
                        imgBackground.setMaxWidth(params.width);
                        imgBackground.setMaxHeight(params.height);
                        pop_width = params.width;
                        pop_height = params.height;
                        START_X = event.getRawX();
                        START_Y = event.getRawY();

                        wm.updateViewLayout(vPop, params);
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }
}
