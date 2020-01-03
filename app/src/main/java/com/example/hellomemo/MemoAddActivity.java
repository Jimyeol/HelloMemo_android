package com.example.hellomemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.hellomemo.DataBase.DBAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoAddActivity extends Activity {

    public static String strDateChangeValue = "";               //정렬용 Date
    public static String strDateChangeValueView = "";         //보여주기용 (리스트뷰 보여주기) Date
    public static String strDateCreateValueView = "";         //생성된 Date
    public static long longDateChangeValueView = 0;

    //Index
    private Long mRowId;

    //MODE
    private byte nMode = MEMOMODE.MODE_VIEW;

    private Cursor note;

    private EditText mTitleText;
    private String mBeforeSaveTitleText = ""; //수정되기 전의 Title String
    private EditText mBodyText;
    private String mBeforeSaveBodyText = ""; //수정되기 전의 Body String
    private TextView mDateText;

    private ImageButton btnSave;

    private DBAdapter mDbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmemo);

        /**** DB 오픈 ****/
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        /**** 레이아웃 초기화 ****/
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateText = (TextView) findViewById(R.id.notelist_date);
        btnSave = (ImageButton) findViewById(R.id.btn_Memo_Save);

        /**** Index 불러오기 ****/
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(DBAdapter.KEY_ROWID);

        /**** Index 비어있을시 메모 생성 ****/
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();

            mRowId = extras != null ? extras.getLong(DBAdapter.KEY_ROWID)
                    : null;

            //메모 새로만들기
            if( mRowId == null) {
                //nMode = MEMOMODE.MODE_EDIT;
                setNewDate();   //현재 날짜 불러오기
                mBodyText.requestFocus();   //새로운 메모라면 바로 포커스가 몸체 메모로 잡힌다.

                //생성된날짜 저장
                strDateCreateValueView = getDateFormat("y'년'M'월'd'일' H':'m");
            } else {
                //기존의 메모를 수정하는거기 때문에 날짜 불러오기
                loadSavedDate();
            }
        }
        populateFields();

        /**** 더블 클릭시 메모 수정 ****/
        mBodyText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        /**** 메모 저장 ****/
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveState();
                finish();
            }
        });
    }

    /*********************************************
     * 제스쳐 이벤트
     ********************************************** */
    final GestureDetector gestureDetector = new GestureDetector(
            new GestureDetector.SimpleOnGestureListener() {
                //더블탭 눌렸을때 제스쳐 처리
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (nMode == MEMOMODE.MODE_VIEW) {
                        Toast.makeText(getApplicationContext(),
                                R.string.memo_change_mode, Toast.LENGTH_SHORT).show();
                        mTitleText.setFocusable(true);
                        mBodyText.setFocusableInTouchMode(true);    //터치 가능하게 하는 모드
                        mTitleText.setFocusableInTouchMode(true);
                        mBodyText.setFocusable(true);
                        mBodyText.requestFocus();   //포커스가 몸체 메모로 잡힌다.
                    }
                    nMode = MEMOMODE.MODE_EDIT;
                    return super.onDoubleTap(e);
                }
            });

    /*********************************************
     * 에디트 박스
     ********************************************** */
    public static class LineEditText extends AppCompatEditText {
        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }


    /*********************************************
     * 에디트박스 메모 채우기 (메모 클릭시 내용 불러오기)
     ********************************************** */
    private void populateFields() {
        if (mRowId != null) {
            note = mDbHelper.fetchMemo(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.BODY)));
            strDateCreateValueView = (note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.CREATE_DATE)));


            //만약 안에 내용이 있으면 비활성화
            if( mBodyText.getText().toString() != "" || mTitleText.getText().toString() != "") {
                mTitleText.setFocusable(false);
                mBodyText.setFocusable(false);
            }

            mBeforeSaveBodyText = mBodyText.getText().toString();
            mBeforeSaveTitleText = mTitleText.getText().toString();
        }
    }

    /*********************************************
     * 현재 시간, 날짜 불러오기
     ********************************************** */
    private void setNewDate() {
        strDateChangeValue = getDateFormat("yyMMddHHmmss");    //Long을 저장용 데이터
        strDateChangeValueView = getDateFormat("y'.'M'.'d a h':'m");    //뷰에 보여주기 위한

        //정렬을 위한 날짜를 int로 변형합니다. (스트링은 정렬이어려우니까)
        longDateChangeValueView = Long.parseLong(strDateChangeValue);

        mDateText.setText(""+strDateChangeValueView);
    }


    /*********************************************
     * 현재 시간, 날짜 불러오기
     ********************************************** */
    private String getDateFormat(String str) {
        String Date;
        long msTime = System.currentTimeMillis();
        java.util.Date curDateTime = new Date(msTime);

        SimpleDateFormat formatter = new SimpleDateFormat(str);
        Date = formatter.format(curDateTime);

        return Date;
    }

    /*********************************************
     * 저장하기 (DB에 저장)
     ********************************************** */
    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        //입력 없으면 노트 생성 X
        if( title.equals("") && body.equals(""))
            return;

        //수정된게 없으면 X
        if( mBeforeSaveBodyText.equals(mBodyText.getText().toString()) &&
                mBeforeSaveTitleText.equals(mTitleText.getText().toString())) {
            return;
        }

        //수정된게 있으니까
        //날짜를 세팅 다시해줌
        setNewDate();

        //제목 설정 안하면 내용을 제목으로 설정.
        if( title.equals("")) {
            title = body;
            mTitleText.setText(mBodyText.getText());
        }

        //노트 생성
        if(mRowId == null) {
            long id = mDbHelper.createMemo(title, body, strDateChangeValueView, longDateChangeValueView,
                    strDateCreateValueView);
            if (id > 0) {
                mRowId = id;
            } else {
                Log.e("saveState", "노트 생성실패");
            }
        } else {
            if(!mDbHelper.updateMemo(mRowId, title, body, strDateChangeValueView, longDateChangeValueView,
                    strDateCreateValueView)){
                Log.e("saveState","노트 수정실패");
            }
        }

        if( nMode == MEMOMODE.MODE_EDIT) {
            //메모 저장 토스트
            Toast.makeText(getApplicationContext(),
                    R.string.memo_save, Toast.LENGTH_SHORT).show();
        }
    }

    /*********************************************
     * 기존 메모 불러올시, 저장된 날짜 Load
     ********************************************** */
    private void loadSavedDate() {
        if (mRowId != null) {
            note = mDbHelper.fetchMemo(mRowId);

            strDateChangeValueView = note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.CHANGED_DATE));

            //정렬을 위한 날짜를 long으로 변형합니다. (스트링은 정렬이어려우니까)
            longDateChangeValueView = note.getLong(
                    note.getColumnIndexOrThrow(DBAdapter.CHANGED_DATE_VALUE));

            mDateText.setText("" + strDateChangeValueView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DBAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
}
