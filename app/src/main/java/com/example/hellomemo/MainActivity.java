package com.example.hellomemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.hellomemo.DataBase.DBAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {

    //퍼미션 코드
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    //옵션 코드
    private static final int ACTIVITY_CREATE=0;

    //데이터 베이스 어답터
    static public DBAdapter mDbHelper;

    //리스트뷰 생성, 어댑터
    private ListView listview ;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //안드로이드 6.0이상에서 권한 획득 요청청
        if( Build.VERSION.SDK_INT>=23) someMethod();

        //DB오픈
        mDbHelper = new DBAdapter (this);
        mDbHelper.open();

        //리스트뷰, 어댑터
        adapter = new MyAdapter() ;
        listview = (ListView) findViewById(R.id.Notelist);
        //setOptionSort(getOptionSort()); //정렬 저장되어있는거 불러오기
        fillData(1);


        /* 메모 추가 */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMemo();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * 안드로이드 6.0 이후 퍼미션
    * */
    @SuppressLint("NewApi")
    public void someMethod() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
        else {}
    }

    private void createMemo() {
        //노트 생성 인텐트
        Intent i = new Intent(this, MemoAddActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /*
    * 모든 노트 보여주기
    * */
    private void fillData(int n) {
        Cursor notesCursor =  mDbHelper.fetchAllNotes(mDbHelper.CHANGED_DATE_VALUE, "");
        // Get all of the notes from the database and create the item list
        startManagingCursor(notesCursor);


        String[] from = new String[] { DBAdapter.TITLE , DBAdapter.BODY, DBAdapter.CHANGED_DATE};
        int[] to = new int[] { R.id.textView1,R.id.textbody,R.id.date_row};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        listview.setAdapter(notes);
    }
}
