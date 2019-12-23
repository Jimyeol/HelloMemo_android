package com.example.hellomemo;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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

    private Long mRowId;

    private Cursor note;

    private EditText mTitleText;
    private String mBeforeSaveTitleText = ""; //수정된거 확인 여부 String
    private EditText mBodyText;
    private String mBeforeSaveBodyText = ""; //수정된거 확인 여부 String
    private TextView mDateText;

    private ImageButton btnSave;

    private DBAdapter mDbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmemo);

        //DB오픈
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        //레이아웃들 초기화
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateText = (TextView) findViewById(R.id.notelist_date);
        btnSave = (ImageButton) findViewById(R.id.btn_Memo_Save);

        //Index 불러오기
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(DBAdapter.KEY_ROWID);

        //Index가 비어있을시 메모 생성
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();

            //메모 새로만들기
            if( mRowId == null) {
                //nMode = MODE_EDIT;
                setNewDate();   //현재 날짜 불러오기
                mBodyText.requestFocus();   //새로운 메모라면 바로 포커스가 몸체 메모로 잡힌다.

                //생성된날짜 저장
                strDateCreateValueView = getDateFormat("y'년'M'월'd'일' H':'m");
            }
        }
        populateFields();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveState();
                finish();
            }
        });
    }

    /* 에디트박스 */
    public static class LineEditText extends AppCompatEditText {
        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }


    private void populateFields() {
        if (mRowId != null) {
            note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.BODY)));
            strDateCreateValueView = (note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.CREATE_DATE)));


//            //만약 안에 내용이 있으면 비활성화
//            if( mBodyText.getText().toString() != "" || mTitleText.getText().toString() != "") {
//                mTitleText.setFocusable(false);
//                mBodyText.setFocusable(false);
//            }

//            mBeforeSaveBodyText = mBodyText.getText().toString();
//            mBeforeSaveTitleText = mTitleText.getText().toString();
        }
    }

   /*
   * 메모 생성시 현재 날짜 받아오기
   * */
    private void setNewDate() {
        strDateChangeValue = getDateFormat("yyMMddHHmm");    //Long을 저장하기 위해 불러오기
        strDateChangeValueView = getDateFormat("y'.'M'.'d a h':'m");    //뷰에 보여주기 위한

        //정렬을 위한 날짜를 int로 변형합니다. (스트링은 정렬이어려우니까)
        longDateChangeValueView = Long.parseLong(strDateChangeValue);

        mDateText.setText(""+strDateChangeValueView);
    }

    private String getDateFormat(String str) {
        String Date;

        long msTime = System.currentTimeMillis();
        java.util.Date curDateTime = new Date(msTime);

        SimpleDateFormat formatter = new SimpleDateFormat(str);
        Date = formatter.format(curDateTime);

        return Date;
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        //입력 없으면 노트 생성 X
        if( title.equals("") && body.equals(""))
            return;

//        //수정된게 없으면 X
//        if( mBeforeSaveBodyText.equals(mBodyText.getText().toString()) &&
//                mBeforeSaveTitleText.equals(mTitleText.getText().toString())) {
//            return;
//        }

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
            long id = mDbHelper.createNote(title, body, strDateChangeValueView, longDateChangeValueView,
                    strDateCreateValueView);
            if (id > 0) {
                mRowId = id;
            } else {
                Log.e("saveState", "노트 생성실패");
            }
        }

//        }else{
//            if(!mDbHelper.updateNote(mRowId, title, body, strDateChangeValueView, longDateChangeValueView,
//                    strDateCreateValueView)){
//                Log.e("saveState","노트 생성실패");
//            }
//        }

//        if( nMode == MODE_EDIT) {
//            //메모 저장 토스트
//            Toast.makeText(getApplicationContext(),
//                    R.string.memo_save, Toast.LENGTH_SHORT).show();
//        }
    }
}
